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
package org.opennms.netmgt.snmp.snmp4j;

import java.io.IOException;
import java.net.InetAddress;

import org.apache.commons.lang.StringUtils;
import org.opennms.netmgt.snmp.SnmpAgentConfig;
import org.snmp4j.CommunityTarget;
import org.snmp4j.MessageDispatcher;
import org.snmp4j.MessageDispatcherImpl;
import org.snmp4j.PDU;
import org.snmp4j.ScopedPDU;
import org.snmp4j.Snmp;
import org.snmp4j.Target;
import org.snmp4j.TransportMapping;
import org.snmp4j.UserTarget;
import org.snmp4j.mp.MPv1;
import org.snmp4j.mp.MPv2c;
import org.snmp4j.mp.MPv3;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.security.AuthHMAC128SHA224;
import org.snmp4j.security.AuthHMAC192SHA256;
import org.snmp4j.security.AuthHMAC384SHA512;
import org.snmp4j.security.AuthMD5;
import org.snmp4j.security.AuthSHA;
import org.snmp4j.security.PrivAES128;
import org.snmp4j.security.PrivAES192;
import org.snmp4j.security.PrivAES256;
import org.snmp4j.security.PrivDES;
import org.snmp4j.security.SecurityLevel;
import org.snmp4j.security.SecurityProtocols;
import org.snmp4j.security.USM;
import org.snmp4j.security.UsmUser;
import org.snmp4j.smi.Address;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.snmp4j.smi.UdpAddress;
import org.snmp4j.transport.DefaultUdpTransportMapping;

import com.google.common.annotations.VisibleForTesting;

public class Snmp4JAgentConfig {
    
    private SnmpAgentConfig m_config;

    public Snmp4JAgentConfig(SnmpAgentConfig config) {
        m_config = config;
    }

    public InetAddress getInetAddress() {
        return m_config.getAddress();
    }
    
    public OctetString getAuthPassPhrase() {
        return createOctetString(m_config.getAuthPassPhrase());
    }

    public OID getAuthProtocol() {
        return convertAuthProtocol(m_config.getAuthProtocol());
    }

    public int getMaxRequestSize() {
        return m_config.getMaxRequestSize();
    }

    public int getMaxVarsPerPdu() {
        return m_config.getMaxVarsPerPdu();
    }
    
    public int getMaxRepetitions() {
        return m_config.getMaxRepetitions();
    }

    public int getPort() {
        return m_config.getPort();
    }

    public int getRetries() {
        return m_config.getRetries();
    }

    public int getSecurityLevel() {
        return convertSecurityLevel(m_config.getSecurityLevel());
    }

    public OctetString getSecurityName() {
        return convertSecurityName(m_config.getSecurityName());
    }

    public int getTimeout() {
        return m_config.getTimeout();
    }

    public int getVersion() {
        return convertVersion(m_config.getVersion());
    }

    /**
     * Returns a string representation of the SNMP4J version constant
     */
    public String getVersionString() {
        switch (getVersion()) {
        case SnmpConstants.version1:
            return "SNMPv1";
        case SnmpConstants.version2c:
            return "SNMPv2c";
        case SnmpConstants.version3:
            return "SNMPv3";
        default:
            return "unknown: " + getVersion();
        }
    }

    public String getWriteCommunity() {
        return m_config.getWriteCommunity();
    }

    @Override
    public String toString() {
        return m_config.toString();
    }

    /**
     * This method converts an InetAddress to an implementation of an SNMP4J Address
     * (UdpAddress or TcpAddress)
     * 
     * TODO: This needs to be updated when the protocol flag is added to the SNMP Config
     * so that UDP or TCP can be used in v3 operations.
     */
    public static Address convertAddress(InetAddress address, int port) {
        String transportAddress = address.getHostAddress();
        transportAddress += "/" + port;
        return new UdpAddress(transportAddress);
    }

    /**
     * This method adapts the OpenNMS SNMP version constants
     * to SNMP4J defined constants.
     * 
     * @param version
     * @return
     */
    private int convertVersion(int version) {
        switch (version) {
        case SnmpAgentConfig.VERSION3:
            return SnmpConstants.version3;
        case SnmpAgentConfig.VERSION2C:
            return SnmpConstants.version2c;
        default:
            return SnmpConstants.version1;
        }
    }

    private static OctetString createOctetString(String s) {
        
        if (StringUtils.isBlank(s)) {
            return null;
        }
        
        OctetString octetString;
        if (s.startsWith("0x")) {
            octetString = OctetString.fromHexString(s.substring(2), ':');
        } else {
            octetString = new OctetString(s);
        }
        return octetString;
    }
    
    /**
     * Adapts the OpenNMS SNMPv3 security name to an SNMP4J compatible
     * security name (String -> OctetString)
     * 
     * @param securityName
     * @return
     */
    private OctetString convertSecurityName(String securityName) {
        return new OctetString(securityName);
    }

    private OID convertPrivProtocol(String privProtocol) {
        /*
         * Returning null here is okay because the SNMP4J library supports
         * this value as null when creating the SNMP session.
         */
        if (StringUtils.isBlank(privProtocol)) {
            return null;
        }
        
        if (privProtocol.equals("DES")) {
            return PrivDES.ID;
        } else if ((privProtocol.equals("AES128")) || (privProtocol.equals("AES"))) {
            return PrivAES128.ID;
        } else if (privProtocol.equals("AES192")) {
            return PrivAES192.ID;
        } else if (privProtocol.equals("AES256")) {
            return PrivAES256.ID;
        } else {
            throw new IllegalArgumentException("Privacy protocol " + privProtocol + " not supported");
        }
    
    }

    /**
     * Adapts the OpenNMS SNMPv3 community name to an SNMP4J compatible
     * community name (String -> OctetString)
     * 
     * @param community
     * @return
     */
    private static OctetString convertCommunity(String community) {
        return new OctetString(community);
    }

    private static OID convertAuthProtocol(String authProtocol) {
        /*
         * Returning null here is okay because the SNMP4J library supports
         * this value as null when creating the SNMP session.
         */
        if (StringUtils.isBlank(authProtocol)) {
            return null;
        }

        switch (authProtocol) {
            case "MD5":
                return AuthMD5.ID;
            case "SHA":
                return AuthSHA.ID;
            case "SHA-224":
                return AuthHMAC128SHA224.ID;
            case "SHA-256":
                return AuthHMAC192SHA256.ID;
            case "SHA-512":
                return AuthHMAC384SHA512.ID;
            default:
                throw new IllegalArgumentException("Authentication protocol unsupported: " + authProtocol);
        }

    }

    @VisibleForTesting
    public Target getTarget() {
        Target target = createTarget();
        target.setVersion(getVersion());
        target.setRetries(getRetries());
        target.setTimeout(getTimeout());
        target.setAddress(getAddress());
        target.setMaxSizeRequestPDU(getMaxRequestSize());
            
        return target;
    }

    private Target createTarget() {
        return (isSnmpV3() ? createUserTarget() : createCommunityTarget());
    }

    boolean isSnmpV3() {
        return m_config.getVersion() == SnmpConstants.version3;
    }

    private Target createCommunityTarget() {
        CommunityTarget target = new CommunityTarget();
        target.setCommunity(getReadCommunity());
        return target;
    }

    private Target createUserTarget() {
        UserTarget target = new UserTarget();
        target.setSecurityLevel(getSecurityLevel());
        target.setSecurityName(getSecurityName());
        return target;
    }

    private OctetString getReadCommunity() {
        return convertCommunity(m_config.getReadCommunity());
    }

    private Address getAddress() {
        return convertAddress(getInetAddress(), getPort());
    }

    public OID getPrivProtocol() {
        return convertPrivProtocol(m_config.getPrivProtocol());
    }

    public OctetString getPrivPassPhrase() {
        return createOctetString(m_config.getPrivPassPhrase());
    }

    public OctetString getContextName() {
        return createOctetString(m_config.getContextName());
    }

    public OctetString getContextEngineID() {
        return createOctetString(m_config.getContextEngineId());
    }

    /**
     * This method adapts the OpenNMS SNMPv3 security level constants
     * to SNMP4J defined constants.
     * 
     * @param securityLevel
     * @return
     */
    private int convertSecurityLevel(int securityLevel) {
        switch (securityLevel) {
        case SnmpAgentConfig.AUTH_NOPRIV :
            securityLevel = SecurityLevel.AUTH_NOPRIV;
            break;
        case SnmpAgentConfig.AUTH_PRIV :
            securityLevel = SecurityLevel.AUTH_PRIV;
            break;
        case SnmpAgentConfig.NOAUTH_NOPRIV :
            securityLevel = SecurityLevel.NOAUTH_NOPRIV;
            break;
        default :
           securityLevel = SecurityLevel.NOAUTH_NOPRIV;
        }
        
        return securityLevel;
    }

    public Snmp createSnmpSession() throws IOException {
        final TransportMapping<?> transport = new DefaultUdpTransportMapping();
        final MessageDispatcher disp = new MessageDispatcherImpl();
        final Snmp session;
        // Here we create the SNMP session, while only adding the message processing
        // models we need for the specific agent
        if (!isSnmpV3()) {
            disp.addMessageProcessingModel(new MPv1());
            disp.addMessageProcessingModel(new MPv2c());
            session = new Snmp(disp, transport);
        } else {
            // Make a new USM
            final USM usm = new USM(SecurityProtocols.getInstance(), new OctetString(MPv3.createLocalEngineID()), 0);
            // Add the specified user to the USM
            usm.addUser(
                getSecurityName(),
                new UsmUser(
                    getSecurityName(),
                    getAuthProtocol(),
                    getAuthPassPhrase(),
                    getPrivProtocol(),
                    getPrivPassPhrase()
                )
            );
            disp.addMessageProcessingModel(new MPv3(usm));
            session = new Snmp(disp, transport);
        }
        return session;
    }


    /**
     * Creates an SNMP4J PDU based on the SNMP4J version constants.
     * A v3 request requires a ScopedPDU.
     * 
     * @param type
     * @return
     */
    public PDU createPdu(int type) {
        PDU pdu = null;
        if (isSnmpV3()) {
            pdu = new ScopedPDU();
            ScopedPDU scopedPDU = (ScopedPDU) pdu;
            OctetString contextName = getContextName();
            if (contextName != null)
                scopedPDU.setContextName(contextName);
            OctetString contextEngineID = getContextEngineID();
            if (contextEngineID != null)
                scopedPDU.setContextEngineID(contextEngineID);
        } else {
            pdu = new PDU();
        }
        pdu.setType(type);
        return pdu;
    }

}
