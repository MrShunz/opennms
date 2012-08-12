/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2011 The OpenNMS Group, Inc.
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

package org.opennms.nrtg.api.model;

import java.io.Serializable;
import java.util.Date;

/**
 * Result of a single metric on a given node/interface to a given time.
 * <p/>
 * User: chris
 * Date: 19.06.12
 * Time: 14:43
 * To change this template use File | Settings | File Templates.
 */
public interface Measurement extends Serializable {

    public void setNodeId(int nodeId);

    public void setNetInterface(String theInterface);

    public void setService(String service);

    public void setMetricId(String metricId);

    public void setValue(String value);

    public void setTimestamp(Date timestamp);

    public int getNodeId();

    public String getNetInterface();

    public String getService();

    public String getMetricId();

    public String getValue();

    public Date getTimestamp();
}
