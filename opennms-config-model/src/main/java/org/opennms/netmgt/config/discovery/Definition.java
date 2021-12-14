/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.config.discovery;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.*;

public class Definition implements Serializable {

    private static final long serialVersionUID = 5369200192316960658L;

    private String location;

    /**
     * The number of times a ping is retried for this
     *  specific address. If there is no response after the first ping
     *  to an address, it is tried again for the specified number of
     *  retries. This retry count overrides the default.
     */
    private Integer retries;

    /**
     * The timeout on each poll for this specific
     *  address. This timeout overrides the default.
     */
    private Long timeout;

    private String foreignSource;

    @JsonProperty("detectors")
    // align with xsd format
    private Map<String, List<Detector>> detectorsMap = new HashMap();
    @JsonIgnore
    private static final String DETECTOR_KEY = "detector";

    /**
     * the specific addresses for discovery
     */
    @JsonProperty("specific")
    private List<Specific> specifics = new ArrayList<>();

    /**
     * the range of addresses for discovery
     */
    @JsonProperty("include-range")
    private List<IncludeRange> includeRanges = new ArrayList<>();

    /**
     * the range of addresses to be excluded from the
     * discovery
     */
    @JsonProperty("exclude-range")
    private List<ExcludeRange> excludeRanges = new ArrayList<>();

    /**
     * a file URL holding specific addresses to be
     *  polled
     */
    @JsonProperty("include-url")
    private List<IncludeUrl> includeUrls = new ArrayList<>();

    /**
     * a file URL holding specific addresses to be
     *  excluded
     */
    @XmlElement(name = "exclude-url")
    private List<ExcludeUrl> excludeUrls = new ArrayList<>();


    public Optional<String> getLocation() {
        return Optional.ofNullable(location);
    }

    @JsonIgnore
    public String getLocationName() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Optional<Integer> getRetries() {
        return Optional.ofNullable(this.retries);
    }

    public void setRetries(Integer retries) {
        this.retries = retries;
    }

    public Optional<Long> getTimeout() {
        return Optional.ofNullable(this.timeout);
    }

    public void setTimeout(Long timeout) {
        this.timeout = timeout;
    }

    public String getForeignSource() {
        return foreignSource;
    }

    public void setForeignSource(String foreignSource) {
        this.foreignSource = foreignSource;
    }

    public List<Specific> getSpecifics() {
        return specifics;
    }

    public void setSpecifics(List<Specific> specifics) {
        this.specifics = specifics;
    }

    public void addSpecific(Specific specific) {
        this.specifics.add(specific);
    }

    public List<IncludeRange> getIncludeRanges() {
        return includeRanges;
    }

    public void setIncludeRanges(List<IncludeRange> includeRanges) {
        this.includeRanges = includeRanges;
    }

    public void addIncludeRange(IncludeRange includeRange) {
        this.includeRanges.add(includeRange);
    }

    public List<ExcludeRange> getExcludeRanges() {
        return excludeRanges;
    }

    public void setExcludeRanges(List<ExcludeRange> excludeRanges) {
        this.excludeRanges = excludeRanges;
    }

    public void addExcludeRange(ExcludeRange excludeRange) {
        this.excludeRanges.add(excludeRange);
    }

    @JsonIgnore
    public List<Detector> getDetectors() {
        return detectorsMap.get(DETECTOR_KEY);
    }

    @JsonIgnore
    public void setDetectors(List<Detector> detectors) {
        this.detectorsMap.put(DETECTOR_KEY, detectors);
    }

    public void addDetector(Detector detector) {
        detectorsMap.computeIfAbsent(DETECTOR_KEY, value -> new ArrayList<>());
        this.detectorsMap.get(DETECTOR_KEY).add(detector);
    }

    public List<IncludeUrl> getIncludeUrls() {
        return includeUrls;
    }

    public void setIncludeUrls(final List<IncludeUrl> includeUrls) {
        if (includeUrls == this.includeUrls) return;
        this.includeUrls.clear();
        if (includeUrls != null) this.includeUrls.addAll(includeUrls);
    }

    public void addIncludeUrl(final IncludeUrl includeUrl) {
        includeUrls.add(includeUrl);
    }

    public boolean removeIncludeUrl(final IncludeUrl includeUrl) {
        return includeUrls.remove(includeUrl);
    }

    public List<ExcludeUrl> getExcludeUrls() {
        return excludeUrls;
    }

    public void setExcludeUrls(final List<ExcludeUrl> excludeUrls) {
        if (excludeUrls.equals(this.excludeUrls)) return;
        this.excludeUrls.clear();
        if (excludeUrls != null) this.excludeUrls.addAll(excludeUrls);
    }

    public void addExcludeUrl(final ExcludeUrl excludeUrl) {
        excludeUrls.add(excludeUrl);
    }

    public boolean removeExcludeUrl(final ExcludeUrl excludeUrl) {
        return excludeUrls.remove(excludeUrl);
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Definition that = (Definition) o;
        return Objects.equals(location, that.location) &&
                Objects.equals(specifics, that.specifics) &&
                Objects.equals(includeRanges, that.includeRanges) &&
                Objects.equals(excludeRanges, that.excludeRanges) &&
                Objects.equals(detectors, that.detectors) &&
                Objects.equals(includeUrls, that.includeUrls) &&
                Objects.equals(excludeUrls, that.excludeUrls);
    }

    @Override
    public int hashCode() {
        return Objects.hash(location, specifics, includeRanges, excludeRanges, detectors, includeUrls, excludeUrls);
    }
}
