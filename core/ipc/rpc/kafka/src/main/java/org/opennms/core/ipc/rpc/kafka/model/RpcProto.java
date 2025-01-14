/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: kafka-rpc.proto

package org.opennms.core.ipc.rpc.kafka.model;

public final class RpcProto {
  private RpcProto() {}
  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistryLite registry) {
  }

  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistry registry) {
    registerAllExtensions(
        (com.google.protobuf.ExtensionRegistryLite) registry);
  }
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_RpcMessageProto_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_RpcMessageProto_fieldAccessorTable;
  static final com.google.protobuf.Descriptors.Descriptor
    internal_static_RpcMessageProto_TracingInfoEntry_descriptor;
  static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_RpcMessageProto_TracingInfoEntry_fieldAccessorTable;

  public static com.google.protobuf.Descriptors.FileDescriptor
      getDescriptor() {
    return descriptor;
  }
  private static  com.google.protobuf.Descriptors.FileDescriptor
      descriptor;
  static {
    String[] descriptorData = {
      "\n\017kafka-rpc.proto\"\226\002\n\017RpcMessageProto\022\016\n" +
      "\006rpc_id\030\001 \001(\t\022\023\n\013rpc_content\030\002 \001(\014\022\021\n\tsy" +
      "stem_id\030\003 \001(\t\022\027\n\017expiration_time\030\004 \001(\004\022\034" +
      "\n\024current_chunk_number\030\005 \001(\005\022\024\n\014total_ch" +
      "unks\030\006 \001(\005\0227\n\014tracing_info\030\007 \003(\0132!.RpcMe" +
      "ssageProto.TracingInfoEntry\022\021\n\tmodule_id" +
      "\030\010 \001(\t\0322\n\020TracingInfoEntry\022\013\n\003key\030\001 \001(\t\022" +
      "\r\n\005value\030\002 \001(\t:\0028\001B2\n$org.opennms.core.i" +
      "pc.rpc.kafka.modelB\010RpcProtoP\001b\006proto3"
    };
    com.google.protobuf.Descriptors.FileDescriptor.InternalDescriptorAssigner assigner =
        new com.google.protobuf.Descriptors.FileDescriptor.    InternalDescriptorAssigner() {
          public com.google.protobuf.ExtensionRegistry assignDescriptors(
              com.google.protobuf.Descriptors.FileDescriptor root) {
            descriptor = root;
            return null;
          }
        };
    com.google.protobuf.Descriptors.FileDescriptor
      .internalBuildGeneratedFileFrom(descriptorData,
        new com.google.protobuf.Descriptors.FileDescriptor[] {
        }, assigner);
    internal_static_RpcMessageProto_descriptor =
      getDescriptor().getMessageTypes().get(0);
    internal_static_RpcMessageProto_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_RpcMessageProto_descriptor,
        new String[] { "RpcId", "RpcContent", "SystemId", "ExpirationTime", "CurrentChunkNumber", "TotalChunks", "TracingInfo", "ModuleId", });
    internal_static_RpcMessageProto_TracingInfoEntry_descriptor =
      internal_static_RpcMessageProto_descriptor.getNestedTypes().get(0);
    internal_static_RpcMessageProto_TracingInfoEntry_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_RpcMessageProto_TracingInfoEntry_descriptor,
        new String[] { "Key", "Value", });
  }

  // @@protoc_insertion_point(outer_class_scope)
}
