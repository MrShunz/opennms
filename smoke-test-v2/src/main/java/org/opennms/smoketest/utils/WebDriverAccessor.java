/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019-2019 The OpenNMS Group, Inc.
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

package org.opennms.smoketest.utils;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.NoSuchElementException;
import java.util.Objects;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.opennms.smoketest.containers.FirefoxWebdriverContainer;
import org.opennms.smoketest.env.MinimalEnvironment;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.BrowserWebDriverContainer;

import com.github.dockerjava.api.model.Network;
import com.google.common.base.Strings;

/**
 * Helper to access the underlying {@link RemoteWebDriver}.
 *
 * Allows to run a dedicated Firefox container for debug purposes.
 * Also see the OpenNMSSeleniumDebugIT.
 *
 */
public class WebDriverAccessor implements TestRule {

    private final MinimalEnvironment env = new MinimalEnvironment();

    private URL webdriverUrl;

    public BrowserWebDriverContainer firefox = new FirefoxWebdriverContainer();

    public WebDriverAccessor() {

    }

    public WebDriverAccessor(final String webdriverUrl){
        try {
            this.webdriverUrl = Objects.requireNonNull(new URL(webdriverUrl));
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Statement apply(Statement base, Description description) {
        // If no webdriverUrl is provided, a new one is started
        if (webdriverUrl == null) {
            firefox = new FirefoxWebdriverContainer();

            // Ensure the containers can talk to each other
            final String networkName = env.getDockerBridgeName();
            if (!Strings.isNullOrEmpty(networkName)) {
                final Network network = DockerClientFactory.instance().client().listNetworksCmd().withNameFilter(networkName).exec()
                        .stream().findAny()
                        .orElseThrow(() -> new NoSuchElementException("No network bridge with name '" + networkName + "' was found"));
                firefox.withNetwork(new org.testcontainers.containers.Network() {
                    @Override
                    public Statement apply(Statement base, Description description) {
                        return base; // TODO MVR not so sure about this :D
                    }
                    @Override
                    public String getId() {
                        return network.getId();
                    }
                    @Override
                    public void close() {
                    }
                });
            }
            return firefox.apply(base, description);
        }
        // Otherwise just continue without starting a container
        return base;
    }

    public RemoteWebDriver getWebDriver() {
        if (webdriverUrl != null) { // if a url is provided, simply use that
            return new RemoteWebDriver(webdriverUrl, FirefoxWebdriverContainer.getFirefoxOptions());
        }
        if (firefox != null) {
            return firefox.getWebDriver();
        }
        throw new IllegalStateException("FirefoxContainer was not initialized properly and is null. Bailing");
    }
}
