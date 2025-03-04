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
package org.opennms.smoketest.topo;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertTrue;
import static org.opennms.smoketest.TopologyIT.waitForTransition;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.features.topology.link.Layout;
import org.opennms.features.topology.link.TopologyLinkBuilder;
import org.opennms.features.topology.link.TopologyProvider;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.smoketest.OpenNMSSeleniumIT;
import org.opennms.smoketest.TopologyIT;
import org.opennms.smoketest.graphml.GraphmlDocument;
import org.opennms.smoketest.utils.RestClient;
import org.openqa.selenium.ElementNotInteractableException;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.TimeoutException;

import com.google.common.collect.Lists;

/**
 * Tests the 'GraphML' Topology Provider
 *
 * @author mvrueden
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class GraphMLTopologyIT extends OpenNMSSeleniumIT {

    public static final String LABEL = "GraphML Topology Provider (test-graph)";

    private final GraphmlDocument graphmlDocument = new GraphmlDocument("test-topology.xml", "/topology/graphml/test-topology.xml");

    private TopologyIT.TopologyUIPage topologyUIPage;

    private RestClient restClient;

    @Before
    public void setUp() throws IOException, InterruptedException {
        restClient = stack.opennms().getRestClient();

        // Sometimes a previous run did not clean up properly, so we do that before we
        // import a graph
        if (existsGraph()) {
            deleteGraph();
        }

        // Generating dummy nodes for the verifyCanFilterByCategory test method
        this.createDummyNodes();

        importGraph();
        topologyUIPage = new TopologyIT.TopologyUIPage(this, getBaseUrlInternal());
        topologyUIPage.open();
        // Select EnLinkd, otherwise the "GraphML Topology Provider (test-graph)" is always pre-selected due to history restoration
        topologyUIPage.selectTopologyProvider(TopologyProvider.ENLINKD);
        // if Layers is opened then close to set initial condition
        topologyUIPage.closeLayerSelectionComponent();
        assertTrue(!topologyUIPage.isLayoutComponentVisible());
    }

    @After
    public void tearDown() throws IOException, InterruptedException {
        deleteGraph();
    }

    @Test
    public void canUseTopology() throws IOException {
        topologyUIPage.selectTopologyProvider(() -> LABEL);
        topologyUIPage.defaultFocus();

        List<TopologyIT.FocusedVertex> focusedVertices = topologyUIPage.getFocusedVertices();
        assertEquals(4, focusedVertices.size());
        assertEquals(4, topologyUIPage.getVisibleVertices().size());
        assertEquals(1, topologyUIPage.getSzl());
        focusedVertices.sort(Comparator.comparing(TopologyIT.FocusedVertex::getNamespace).thenComparing(TopologyIT.FocusedVertex::getLabel));
        assertEquals(
                Lists.newArrayList(
                        focusVertex(topologyUIPage, "Acme:regions:", "East Region"),
                        focusVertex(topologyUIPage, "Acme:regions:", "North Region"),
                        focusVertex(topologyUIPage, "Acme:regions:", "South Region"),
                        focusVertex(topologyUIPage, "Acme:regions:", "West Region")
                ), focusedVertices);

        // Search for and select a region
        final String regionName = "South";
        TopologyIT.TopologyUISearchResults searchResult = topologyUIPage.search(regionName);
        assertEquals(5, searchResult.countItemsThatContain(regionName));
        searchResult.selectItemThatContains("South Region");

        // Focus should not have changed
        assertEquals(4, focusedVertices.size());
        assertEquals(4, topologyUIPage.getVisibleVertices().size());

        // Verify that the layout is the D3 Layout as this layer does not provide a preferredLayout
        assertEquals(Layout.D3, topologyUIPage.getSelectedLayout());

        // Switch Layer
        topologyUIPage.selectLayer("Markets");
        assertTrue(topologyUIPage.isLayoutComponentVisible());
        assertEquals(0, topologyUIPage.getSzl());
        assertEquals(1, topologyUIPage.getFocusedVertices().size());
        assertEquals("North 4", topologyUIPage.getFocusedVertices().get(0).getLabel());
    }

    @Test
    public void verifySwitchesLayerOnSearchProperly() {
        topologyUIPage.selectTopologyProvider(() -> LABEL);
        TopologyIT.TopologyUISearchResults searchResult = topologyUIPage.search("South");
        assertEquals(5, searchResult.countItemsThatContain("South"));
        searchResult.selectItemThatContains("South 3");
        assertEquals(1, topologyUIPage.getVisibleVertices().size());
        assertEquals(1, topologyUIPage.getFocusedVertices().size());
        assertEquals("South 3", topologyUIPage.getFocusedVertices().get(0).getLabel());
        assertEquals("South 3", topologyUIPage.getVisibleVertices().get(0).getLabel());
    }

    @Test
    public void verifyNavigateToAndBreadcrumbs() {
        topologyUIPage.selectTopologyProvider(() -> LABEL);
        topologyUIPage.findVertex("East Region").contextMenu().click("Navigate To", "Markets (East Region)");

        final ArrayList<TopologyIT.FocusedVertex> marketsVertcies = Lists.newArrayList(
                focusVertex(topologyUIPage, "Acme:markets:", "East 1"),
                focusVertex(topologyUIPage, "Acme:markets:", "East 2"),
                focusVertex(topologyUIPage, "Acme:markets:", "East 3"),
                focusVertex(topologyUIPage, "Acme:markets:", "East 4"));
        assertEquals(marketsVertcies, topologyUIPage.getFocusedVertices());
        assertEquals("Markets", topologyUIPage.getSelectedLayer());
        assertEquals(Lists.newArrayList("regions", "East Region"), topologyUIPage.getBreadcrumbs().getLabels());

        // Click on last element should add all vertices to focus
        topologyUIPage.getFocusedVertices().get(0).removeFromFocus(); // remove an element from focus
        topologyUIPage.getBreadcrumbs().click("East Region");
        assertEquals(marketsVertcies, topologyUIPage.getFocusedVertices());

        // Click on 1st element, should switch layer and add "child" to focus
        topologyUIPage.getBreadcrumbs().click("regions");
        assertEquals(Lists.newArrayList("regions"), topologyUIPage.getBreadcrumbs().getLabels());
        assertEquals(Lists.newArrayList(focusVertex(topologyUIPage, "Acme:regions:", "East Region")), topologyUIPage.getFocusedVertices());

        // Click on last element should add all elements to focus
        topologyUIPage.getBreadcrumbs().click("regions");
        List<TopologyIT.FocusedVertex> focusedVertices = topologyUIPage.getFocusedVertices();
        focusedVertices.sort(Comparator.comparing(TopologyIT.FocusedVertex::getNamespace).thenComparing(TopologyIT.FocusedVertex::getLabel));
        assertEquals(Lists.newArrayList(
                focusVertex(topologyUIPage, "Acme:regions:", "East Region"),
                focusVertex(topologyUIPage, "Acme:regions:", "North Region"),
                focusVertex(topologyUIPage, "Acme:regions:", "South Region"),
                focusVertex(topologyUIPage, "Acme:regions:", "West Region")
        ), focusedVertices); // all elements should be focused
    }

    @Test
    public void verifySaveLayoutButton() {
        topologyUIPage.selectTopologyProvider(() -> LABEL);
        assertEquals(false, topologyUIPage.getSaveLayoutButton().isEnabled()); // it should be disabled

        topologyUIPage.selectLayout(Layout.MANUAL);
        assertEquals(true, topologyUIPage.getSaveLayoutButton().isEnabled()); // now it should be enabled
        topologyUIPage.getSaveLayoutButton().click();
        assertEquals(false, topologyUIPage.getSaveLayoutButton().isEnabled()); // it should be disabled after save
    }

    /**
     * This method tests whether the GraphMLTopologyProvider can work with categories - searching, collapsing and expanding
     */

    @Test
    public void verifyCanFilterByCategory() throws IOException, InterruptedException {
        topologyUIPage.selectTopologyProvider(() -> LABEL);
        topologyUIPage.defaultFocus();

        topologyUIPage.selectLayer("Markets");
        assertTrue(topologyUIPage.isLayoutComponentVisible());
        topologyUIPage.setSzl(0);
        topologyUIPage.clearFocus();

        // Search for the first category
        topologyUIPage.search("Routers").selectItemThatContains("Routers");
        Assert.assertNotNull(topologyUIPage.getVisibleVertices());
        Assert.assertEquals(2, topologyUIPage.getVisibleVertices().size());
        Assert.assertEquals("North 2", topologyUIPage.getVisibleVertices().get(0).getLabel());
        Assert.assertEquals("North 3", topologyUIPage.getVisibleVertices().get(1).getLabel());

        // Collapse and verify that collapsing works and that the category is visible while the vertex - not
        topologyUIPage.getFocusedVertices().get(0).collapse();
        Assert.assertEquals(1, topologyUIPage.getVisibleVertices().size());
        Assert.assertEquals("Routers", topologyUIPage.getVisibleVertices().get(0).getLabel());

        // Search for the second category
        //TODO Theoretically it should display 2 vertices - one for each category. But it does not due to a bug (see issue NMS-9423)
        topologyUIPage.search("Servers").selectItemThatContains("Servers");
        Assert.assertNotNull(topologyUIPage.getVisibleVertices());
        Assert.assertEquals(1, topologyUIPage.getVisibleVertices().size());
        Assert.assertEquals("Routers", topologyUIPage.getVisibleVertices().get(0).getLabel());

        // Expand and verify that vertices are visible again (and not duplicated)
        topologyUIPage.getFocusedVertices().get(0).expand();
        Assert.assertEquals(2, topologyUIPage.getVisibleVertices().size());
        Assert.assertEquals("North 2", topologyUIPage.getVisibleVertices().get(0).getLabel());
        Assert.assertEquals("North 3", topologyUIPage.getVisibleVertices().get(1).getLabel());

        // Collapse all and verify that vertices are not visible and that both categories are visible
        for (TopologyIT.FocusedVertex vertex : topologyUIPage.getFocusedVertices()) {
            vertex.collapse();
        }
        Assert.assertEquals(2, topologyUIPage.getVisibleVertices().size());
        Assert.assertEquals("Routers", topologyUIPage.getVisibleVertices().get(0).getLabel());
        Assert.assertEquals("Servers", topologyUIPage.getVisibleVertices().get(1).getLabel());

        topologyUIPage.clearFocus();
    }

    @Test
    @Ignore("Flapping. Icon does now show, and context menu does not have 'Change Icon' option, only 'Clear Focus' and 'Refresh Now'")
    public void verifyCanChangeIcon() throws IOException, InterruptedException {
        // Select Meta Topology and select target Topology
        topologyUIPage.selectTopologyProvider(() -> LABEL);
        topologyUIPage.findVertex("North Region")
                .contextMenu()
                .click("Navigate To", "Markets (North Region)");

        final String vertexName = "North 1";
        final String currentIconName = topologyUIPage.findVertex(vertexName).getIconName();
        final String newIconName = "microwave_backhaul_1";
        final String otherIconName = "router";

        // Ensure icon is not yet changed
        if (newIconName.equals(currentIconName)) {
            // We're already set to the target icon type, let's change it to something else before proceeding
            topologyUIPage.findVertex(vertexName).changeIcon(otherIconName);
            Assert.assertEquals(otherIconName, topologyUIPage.findVertex(vertexName).getIconName());
        }

        // Change icon
        topologyUIPage.findVertex(vertexName).changeIcon(newIconName);

        // Verify icon has changed
        Assert.assertEquals(newIconName, topologyUIPage.findVertex(vertexName).getIconName());
    }

    // See NMS-10451
    @Test
    public void verifyCanSelectNonVisibleVertex() {
        // Ensure nothing is visible for now
        Assert.assertEquals(0, topologyUIPage.getVisibleVertices().size());

        // Select Nodes tab and select node
        final TopologyIT.BrowserTab browserTab = topologyUIPage.getTab(TopologyIT.Tabs.Nodes);
        browserTab.click();
        browserTab.getRowByLabel("North 2").click();

        // Verify that now the vertex is visible and in focus
        final List<TopologyIT.VisibleVertex> visibleVertices = topologyUIPage.getVisibleVertices();
        Assert.assertEquals(1, visibleVertices.size());
        Assert.assertEquals("North 2", visibleVertices.get(0).getLabel());
        Assert.assertEquals(1, topologyUIPage.getFocusedVertices().size());
    }

    @Test
    public void verifyCanSetLayerViaUrlParameter() {
        adminPage(); // leave topology page to ensure the link actually works
        final String namespace = "acme:markets";
        final String searchTokenNamespace = "Acme:markets:";
        final String url = new TopologyLinkBuilder()
                .provider(() -> LABEL)
                .focus("north.2", "north.3")
                .szl(0)
                .layer(namespace)
                .getLink();
        getDriver().get(getBaseUrlInternal() + url.substring(1) /* ignore leading / */);

        // verify that the page loaded properly
        // DO NOT invoke .open()
        topologyUIPage = new TopologyIT.TopologyUIPage(this, getBaseUrlInternal());
        waitForTransition(this);
        Assert.assertThat(topologyUIPage.getSzl(), is(0));
        Assert.assertThat(topologyUIPage.getFocusedVertices(), hasItems(
                focusVertex(topologyUIPage, searchTokenNamespace, "North 2"),
                focusVertex(topologyUIPage, searchTokenNamespace, "North 3")));
    }

    /**
     * Creates and publishes a requisition with 2 dummy nodes with predefined parameters
     */
    private void        createDummyNodes() throws IOException, InterruptedException {

        // First node has foreign ID "node1", label - "North 2" and category "Routers"
        // Second node has foreign ID "node2", label - "North 3" and categories "Routers" and "Servers"

        final String foreignSourceXML = "<foreign-source name=\"" + OpenNMSSeleniumIT.REQUISITION_NAME + "\">\n" +
                "<scan-interval>1d</scan-interval>\n" +
                "<detectors/>\n" +
                "<policies/>\n" +
                "</foreign-source>";
        createForeignSource(REQUISITION_NAME, foreignSourceXML);

        String requisitionXML = "<model-import foreign-source=\"" + OpenNMSSeleniumIT.REQUISITION_NAME + "\">" +
                                "   <node foreign-id=\"node1\" node-label=\"North 2\">" +
                                "       <interface ip-addr=\"127.0.0.1\" status=\"1\" snmp-primary=\"N\">" +
                                "           <monitored-service service-name=\"ICMP\"/>" +
                                "       </interface>" +
                                "       <category name=\"Routers\"/>" +
                                "   </node>" +
                                "   <node foreign-id=\"node2\" node-label=\"North 3\">" +
                                "       <interface ip-addr=\"127.0.0.1\" status=\"1\" snmp-primary=\"N\">" +
                                "           <monitored-service service-name=\"ICMP\"/>" +
                                "       </interface>" +
                                "       <category name=\"Routers\"/>" +
                                "       <category name=\"Servers\"/>" +
                                "   </node>" +
                                "</model-import>";
        createRequisition(REQUISITION_NAME, requisitionXML, 2);
        // Send an event to force reload of topology
        final EventBuilder builder = new EventBuilder(EventConstants.RELOAD_TOPOLOGY_UEI, getClass().getSimpleName());
        builder.setParam(EventConstants.PARAM_TOPOLOGY_NAMESPACE, "all");
        final Event event = builder.getEvent();
        // Erase the dates so we don't run into unmarshaling issues
        event.setCreationTime(null);
        event.setTime(null);
        sendPost("/rest/events", JaxbUtils.marshal(event), 202);
        Thread.sleep(5000); // Wait to allow the event to be processed
    }

    private boolean existsGraph() {
        return graphmlDocument.exists(restClient);
    }

    private void importGraph() throws InterruptedException {
        graphmlDocument.create(restClient);

        // We wait to give the GraphMLMetaTopologyFactory the chance to initialize the new Topology
        Thread.sleep(20000);
    }

    private void deleteGraph() throws InterruptedException {
        graphmlDocument.delete(restClient);

        // We wait to give the GraphMLMetaTopologyFactory the chance to clean up afterwards
        Thread.sleep(20000);
    }

    private static TopologyIT.FocusedVertex focusVertex(TopologyIT.TopologyUIPage topologyUIPage, String namespace, String label) {
        return new TopologyIT.FocusedVertex(topologyUIPage, namespace, label);
    }

    @Test
    @Ignore("this has been flapping :( ex https://app.circleci.com/pipelines/github/OpenNMS/opennms-prime/5532/workflows/2cf99655-819e-4bec-b1ab-77ce6a4e53fb/jobs/38645/tests")
    public void testNMS14379() throws Exception {
        importGraph();
        topologyUIPage.open();
        topologyUIPage.selectTopologyProvider(() -> LABEL);
        topologyUIPage.defaultFocus();
        topologyUIPage.findVertex("East Region").contextMenu().click("Navigate To", "Markets (East Region)");
        frontPage();
        deleteGraph();
        topologyUIPage.open();
        Thread.sleep(5000);
        try {
            // if dialog is not yet visible, try to interact with a node
            topologyUIPage.findVertex("East 1").select();
            topologyUIPage.findVertex("East 2").select();
            topologyUIPage.findVertex("East 3").select();
            topologyUIPage.findVertex("East 4").select();
        } catch (NoSuchElementException | TimeoutException | ElementNotInteractableException e) {
            // ignore if dialog is already visible
        }
        Thread.sleep(5000);
        findElementByXpath("//div[text() = 'Clicking okay will switch to the default topology provider.']");
        findElementByXpath("//span[@class='v-button-caption' and text() = 'ok']").click();
    }
}
