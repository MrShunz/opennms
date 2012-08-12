/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.snmp;

import java.io.Serializable;
import java.net.InetAddress;
import java.util.Scanner;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.core.xml.bind.InetAddressXmlAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author (various previous authors not documented)
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 *
 */
@XmlRootElement(name="snmpAgentConfig")
public class SnmpAgentConfig extends SnmpConfiguration implements Serializable {
	
	private static Logger s_logger = LoggerFactory.getLogger(SnmpAgentConfig.class);
    
    private InetAddress m_address;
    private InetAddress m_proxyFor;
    
    public SnmpAgentConfig() {
        this(null);
    }
    
    public SnmpAgentConfig(InetAddress agentAddress) {
        this(agentAddress, SnmpConfiguration.DEFAULTS);
    }
    
    public SnmpAgentConfig(InetAddress agentAddress, SnmpConfiguration defaults) {
        super(defaults);
        m_address = agentAddress;
    }
    
    public static SnmpAgentConfig parseProtocolConfigurationString(String protocolConfigString) {
    	if (!protocolConfigString.startsWith("snmp:")) throw new IllegalArgumentException("Invalid protocol configuration string for SnmpAgentConfig: Expected it to start with snmp:" + protocolConfigString);
    	
    	SnmpAgentConfig agentConfig = new SnmpAgentConfig();
    	
    	

    	String[] attributes = protocolConfigString.substring("snmp:".length()).split(",");
    	
    	for(String attribute : attributes) {
    		String[] pair = attribute.split("=");
    		if (pair.length != 2) {
    			throw new IllegalArgumentException("unexpected format for key value pair in SnmpAgentConfig configuration string"+attribute);
    		}
    		
    		String key = pair[0];
    		String value = pair[1];
    		
    		if ("address".equalsIgnoreCase(key)) {
    			agentConfig.setAddress(InetAddressUtils.addr(value));
    		} else if ("port".equalsIgnoreCase(key)) {
    			agentConfig.setPort(Integer.parseInt(value));
    		} else if ("timeout".equalsIgnoreCase(key)) {
    			agentConfig.setTimeout(Integer.parseInt(value));
    		} else if ("retries".equalsIgnoreCase(key)) {
    			agentConfig.setRetries(Integer.parseInt(value));
    		} else if ("max-vars-per-pdu".equalsIgnoreCase(key)) {
    			agentConfig.setMaxVarsPerPdu(Integer.parseInt(value));
    		} else if ("max-repetitions".equalsIgnoreCase(key)) {
    			agentConfig.setMaxRepetitions(Integer.parseInt(value));
    		} else if ("max-request-size".equalsIgnoreCase(key)) {
    			agentConfig.setMaxRequestSize(Integer.parseInt(value));
    		} else if ("version".equalsIgnoreCase(key)) {
    			agentConfig.setVersionAsString(value);
    		} else if ("security-level".equalsIgnoreCase(key)) {
    			agentConfig.setSecurityLevel(Integer.parseInt(value));
    		} else if ("security-name".equalsIgnoreCase(key)) {
    			agentConfig.setSecurityName(value);
    		} else if ("auth-passphrase".equalsIgnoreCase(key)) {
    			agentConfig.setAuthPassPhrase(value);
    		} else if ("auth-protocol".equalsIgnoreCase(key)) {
    			agentConfig.setAuthProtocol(value);
    		} else if ("priv-passprhase".equalsIgnoreCase(key)) {
    			agentConfig.setPrivPassPhrase(value);
    		} else if ("priv-protocol".equalsIgnoreCase(key)) {
    			agentConfig.setPrivProtocol(value);
    		} else if ("read-community".equalsIgnoreCase(key)) {
    			agentConfig.setReadCommunity(value);
    		} else {
    			s_logger.warn("Unexpected attribute in protocol configuration string for SnmpAgentConfig: '{}'", attribute);
    		}
    	}
    			
    			
    	return agentConfig;
    }
    
    public String toProtocolConfigString() {
        StringBuffer buff = new StringBuffer("snmp:");
        buff.append("address="+InetAddressUtils.str(m_address));
        buff.append(",port="+getPort());
        buff.append(",timeout="+getTimeout());
        buff.append(",retries="+getRetries());
        buff.append(",max-vars-per-pdu="+getMaxVarsPerPdu());
        buff.append(",max-repetitions="+getMaxRepetitions());
        buff.append(",max-request-size="+getMaxRequestSize());
        buff.append(",version="+versionToString(getVersion()));
        if (getVersion() == VERSION3) {
            buff.append(",security-level="+getSecurityLevel());
            buff.append(",security-name="+getSecurityName());
            buff.append(",auth-passphrase="+getAuthPassPhrase());
            buff.append(",auth-protocol="+getAuthProtocol());
            buff.append(",priv-passprhase="+getPrivPassPhrase());
            buff.append(",priv-protocol="+getPrivProtocol());
        } else {
            buff.append(",read-community="+getReadCommunity());
        }
        return buff.toString();
    	
    }

    public String toString() {
        StringBuffer buff = new StringBuffer("AgentConfig[");
        buff.append("Address: "+InetAddressUtils.str(m_address));
        buff.append(", ProxyForAddress: "+InetAddressUtils.str(m_proxyFor));
        buff.append(", Port: "+getPort());
        buff.append(", Community: "+getReadCommunity());
        buff.append(", Timeout: "+getTimeout());
        buff.append(", Retries: "+getRetries());
        buff.append(", MaxVarsPerPdu: "+getMaxVarsPerPdu());
        buff.append(", MaxRepetitions: "+getMaxRepetitions());
        buff.append(", Max request size: "+getMaxRequestSize());
        buff.append(", Version: "+versionToString(getVersion()));
        if (getVersion() == VERSION3) {
            buff.append(", Security level: "+getSecurityLevel());
            buff.append(", Security name: "+getSecurityName());
            buff.append(", auth-passphrase: "+getAuthPassPhrase());
            buff.append(", auth-protocol: "+getAuthProtocol());
            buff.append(", priv-passprhase: "+getPrivPassPhrase());
            buff.append(", priv-protocol: "+getPrivProtocol());
        }
        buff.append("]");
        return buff.toString();
    }


    @XmlJavaTypeAdapter(InetAddressXmlAdapter.class)
    public InetAddress getAddress() {
        return m_address;
    }

    public void setAddress(InetAddress address) {
        m_address = address;
    }

    @XmlJavaTypeAdapter(InetAddressXmlAdapter.class)
    public InetAddress getProxyFor() {
        return m_proxyFor;
    }
    
    public void setProxyFor(InetAddress address) {
        m_proxyFor = address;
    }
    
    @XmlTransient
    public InetAddress getEffectiveAddress() {
    	if (m_proxyFor == null) return m_address;
    	return m_proxyFor;
    }

}
