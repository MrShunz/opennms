/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2021 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2021 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.core.ipc.twin.grpc.publisher;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;

import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.opennms.core.grpc.common.GrpcIpcServer;
import org.opennms.core.grpc.common.GrpcIpcUtils;
import org.opennms.core.ipc.twin.common.AbstractTwinPublisher;
import org.opennms.core.ipc.twin.common.LocalTwinSubscriber;
import org.opennms.core.ipc.twin.common.TwinRequestBean;
import org.opennms.core.ipc.twin.common.TwinResponseBean;
import org.opennms.core.ipc.twin.grpc.common.MinionHeader;
import org.opennms.core.ipc.twin.grpc.common.OpenNMSTwinIpcGrpc;
import org.opennms.core.ipc.twin.model.TwinRequestProto;
import org.opennms.core.ipc.twin.model.TwinResponseProto;
import org.opennms.core.logging.Logging;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;
import com.google.protobuf.ByteString;

import io.grpc.stub.StreamObserver;

public class GrpcTwinPublisher extends AbstractTwinPublisher {

    private static final Logger LOG = LoggerFactory.getLogger(GrpcTwinPublisher.class);
    private final GrpcIpcServer grpcIpcServer;
    private Multimap<String, StreamObserver<TwinResponseProto>> sinkStreamsByLocation = LinkedListMultimap.create();
    private Map<String, StreamObserver<TwinResponseProto>> sinkStreamsBySystemId = new HashMap<>();
    private final ThreadFactory twinRpcThreadFactory = new ThreadFactoryBuilder()
            .setNameFormat("twin-rpc-handler-%d")
            .build();
    private final ExecutorService twinRpcExecutor = Executors.newCachedThreadPool(twinRpcThreadFactory);

    public GrpcTwinPublisher(LocalTwinSubscriber twinSubscriber, GrpcIpcServer grpcIpcServer) {
        super(twinSubscriber);
        this.grpcIpcServer = grpcIpcServer;
    }

    @Override
    protected void handleSinkUpdate(TwinResponseBean sinkUpdate) {
        sendTwinResponseForSink(mapTwinResponse(sinkUpdate));
    }

    private synchronized boolean sendTwinResponseForSink(TwinResponseProto twinResponseProto) {
        if (sinkStreamsByLocation.isEmpty()) {
            return false;
        }
        try {
            if (Strings.isNullOrEmpty(twinResponseProto.getLocation())) {
                LOG.debug("Sending sink update for key {} at all locations", twinResponseProto.getConsumerKey());
                sinkStreamsByLocation.values().forEach(stream -> {
                    stream.onNext(twinResponseProto);
                });
            } else {
                String location = twinResponseProto.getLocation();
                sinkStreamsByLocation.get(location).forEach(stream -> {
                    stream.onNext(twinResponseProto);
                    LOG.debug("Sending sink update for key {} at location {}", twinResponseProto.getConsumerKey(), twinResponseProto.getLocation());
                });
            }
        } catch (Exception e) {
            LOG.error("Error while sending Twin response for Sink stream", e);
        }
        return true;
    }

    public void start() throws IOException {
        try (Logging.MDCCloseable mdc = Logging.withPrefixCloseable(GrpcIpcUtils.LOG_PREFIX)) {
            grpcIpcServer.startServer(new StreamHandler());
            LOG.info("Added Twin Service to OpenNMS IPC Grpc Server");
        }

    }


    public void close() throws IOException {
        super.close();
        grpcIpcServer.stopServer();
        LOG.info("Stopped Twin GRPC Server");
        twinRpcExecutor.shutdown();
    }

    private TwinResponseProto mapTwinResponse(TwinResponseBean twinResponseBean) {
        TwinResponseProto.Builder builder = TwinResponseProto.newBuilder();
        if (!Strings.isNullOrEmpty(twinResponseBean.getLocation())) {
            builder.setLocation(twinResponseBean.getLocation());
        }
        builder.setConsumerKey(twinResponseBean.getKey());
        if (twinResponseBean.getObject() != null) {
            builder.setTwinObject(ByteString.copyFrom(twinResponseBean.getObject()));
        }
        return builder.build();
    }

    private class StreamHandler extends OpenNMSTwinIpcGrpc.OpenNMSTwinIpcImplBase {

        @Override
        public io.grpc.stub.StreamObserver<org.opennms.core.ipc.twin.model.TwinRequestProto> rpcStreaming(
                io.grpc.stub.StreamObserver<org.opennms.core.ipc.twin.model.TwinResponseProto> responseObserver) {
            StreamObserver<TwinResponseProto> rpcStream = responseObserver;
            return new StreamObserver<>() {
                @Override
                public void onNext(TwinRequestProto twinRequestProto) {
                    CompletableFuture.runAsync(() -> {
                        TwinRequestBean twinRequestBean = mapTwinRequestProto(twinRequestProto);
                        TwinResponseBean twinResponseBean = getTwin(twinRequestBean);
                        TwinResponseProto twinResponseProto = mapTwinResponse(twinResponseBean);
                        LOG.debug("Sent Twin response for key {} at location {}", twinRequestBean.getKey(), twinRequestBean.getLocation());
                        sendTwinResponse(twinResponseProto, rpcStream);
                    }, twinRpcExecutor);
                }

                @Override
                public void onError(Throwable throwable) {
                    LOG.error("Error in Rpc stream handler", throwable);
                }

                @Override
                public void onCompleted() {
                    LOG.info("Closed Rpc Stream handler");
                }
            };
        }

        private synchronized void sendTwinResponse(TwinResponseProto twinResponseProto, StreamObserver<TwinResponseProto> rpcStream) {
            if (rpcStream != null) {
                rpcStream.onNext(twinResponseProto);
            }
        }

        private synchronized void handleSinkStreamUpdate(MinionHeader request, StreamObserver<TwinResponseProto> responseObserver) {
            if (sinkStreamsBySystemId.containsKey(request.getSystemId())) {
                StreamObserver<org.opennms.core.ipc.twin.model.TwinResponseProto> sinkStream = sinkStreamsBySystemId.remove(request.getSystemId());
                sinkStreamsByLocation.remove(request.getLocation(), sinkStream);
            }
            sinkStreamsByLocation.put(request.getLocation(), responseObserver);
            sinkStreamsBySystemId.put(request.getSystemId(), responseObserver);
            getObjMap().forEach(((sessionKey, twinTracker) -> {
                if(sessionKey.location == null || sessionKey.location.equals(request.getLocation())) {
                    TwinResponseBean twinResponseBean = new TwinResponseBean(sessionKey.key, sessionKey.location, twinTracker.getObj());
                    TwinResponseProto twinResponseProto = mapTwinResponse(twinResponseBean);
                    responseObserver.onNext(twinResponseProto);
                }
            }));
        }

        @Override
        public void sinkStreaming(org.opennms.core.ipc.twin.grpc.common.MinionHeader request,
                                  io.grpc.stub.StreamObserver<org.opennms.core.ipc.twin.model.TwinResponseProto> responseObserver) {
             handleSinkStreamUpdate(request, responseObserver);
        }

        TwinRequestBean mapTwinRequestProto(TwinRequestProto twinRequestProto) {
            TwinRequestBean twinRequestBean = new TwinRequestBean();
            twinRequestBean.setKey(twinRequestProto.getConsumerKey());
            if (!Strings.isNullOrEmpty(twinRequestProto.getLocation())) {
                twinRequestBean.setLocation(twinRequestProto.getLocation());
            }
            return twinRequestBean;
        }

    }

}
