package com.rayvion.engine.commons.graph;

import org.jgrapht.graph.DefaultEdge;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DefaultDirectedGraphTest {

    private DefaultDirectedGraph<String, DefaultEdge> graph;

    @BeforeEach
    void setUp() {
        graph = new DefaultDirectedGraph<>(DefaultEdge.class);
    }

    @Test
    void testAddInboundEdges() {
        graph.addVertex("v1");
        graph.addVertex("v2");
        graph.addVertex("v3");
        graph.addVertex("target");

        // Policy that only matches "v1" and "v2"
        InboundEdgePolicy<String> policy = vertex -> vertex.equals("v1") || vertex.equals("v2");

        graph.addInboundEdges("target", policy);

        assertTrue(graph.containsEdge("v1", "target"), "Should have edge from v1 to target");
        assertTrue(graph.containsEdge("v2", "target"), "Should have edge from v2 to target");
        assertFalse(graph.containsEdge("v3", "target"), "Should not have edge from v3 to target");
    }

    @Test
    void testAddInboundEdgesAvoidSelfLoop() {
        graph.addVertex("v1");
        graph.addVertex("target");

        // Policy that matches everything including the target itself
        InboundEdgePolicy<String> policy = vertex -> true;

        graph.addInboundEdges("target", policy);

        assertTrue(graph.containsEdge("v1", "target"), "Should have edge from v1 to target");
        assertFalse(graph.containsEdge("target", "target"), "Should NOT have self-loop edge");
    }

    @Test
    void testAddOutboundEdges() {
        graph.addVertex("v1");
        graph.addVertex("v2");
        graph.addVertex("v3");
        graph.addVertex("source");

        // Policy that only matches "v2" and "v3"
        OutboundEdgePolicy<String> policy = vertex -> vertex.equals("v2") || vertex.equals("v3");

        graph.addOutboundEdges("source", policy);

        assertFalse(graph.containsEdge("source", "v1"), "Should not have edge from source to v1");
        assertTrue(graph.containsEdge("source", "v2"), "Should have edge from source to v2");
        assertTrue(graph.containsEdge("source", "v3"), "Should have edge from source to v3");
    }

    @Test
    void testAddOutboundEdgesAvoidSelfLoop() {
        graph.addVertex("v1");
        graph.addVertex("source");

        // Policy that matches everything including the source itself
        OutboundEdgePolicy<String> policy = vertex -> true;

        graph.addOutboundEdges("source", policy);

        assertTrue(graph.containsEdge("source", "v1"), "Should have edge from source to v1");
        assertFalse(graph.containsEdge("source", "source"), "Should NOT have self-loop edge");
    }

    @Test
    void testGetInboundAndOutboundVertices() {
        graph.addVertex("v1");
        graph.addVertex("v2");
        graph.addVertex("v3");

        graph.addEdge("v1", "v2");
        graph.addEdge("v3", "v2");
        graph.addEdge("v2", "v1");

        assertEquals(2, graph.getInboundVertices("v2").size());
        assertTrue(graph.getInboundVertices("v2").contains("v1"));
        assertTrue(graph.getInboundVertices("v2").contains("v3"));

        assertEquals(1, graph.getOutboundVertices("v2").size());
        assertTrue(graph.getOutboundVertices("v2").contains("v1"));
    }

    @Test
    void testAddEdges() {
        graph.addVertex("v1");
        graph.addVertex("v2");
        graph.addVertex("v3");
        graph.addVertex("vertex");

        // Inbound policy that matches "v1"
        InboundEdgePolicy<String> inboundPolicy = vertex -> vertex.equals("v1");
        // Outbound policy that matches "v2"
        OutboundEdgePolicy<String> outboundPolicy = vertex -> vertex.equals("v2");

        graph.addEdges("vertex", inboundPolicy, outboundPolicy);

        assertTrue(graph.containsEdge("v1", "vertex"), "Should have inbound edge from v1 to vertex");
        assertFalse(graph.containsEdge("v3", "vertex"), "Should NOT have inbound edge from v3 to vertex");

        assertTrue(graph.containsEdge("vertex", "v2"), "Should have outbound edge from vertex to v2");
        assertFalse(graph.containsEdge("vertex", "v3"), "Should NOT have outbound edge from vertex to v3");
    }

    @Test
    void testAddEdgesWithCommonVertex() {
        graph.addVertex("v1");
        graph.addVertex("v2");
        graph.addVertex("vertex");

        // Policies that both match "v1"
        InboundEdgePolicy<String> inboundPolicy = vertex -> vertex.equals("v1");
        OutboundEdgePolicy<String> outboundPolicy = vertex -> vertex.equals("v1");

        graph.addEdges("vertex", inboundPolicy, outboundPolicy);

        assertTrue(graph.containsEdge("v1", "vertex"), "Should have inbound edge from v1 to vertex");
        assertTrue(graph.containsEdge("vertex", "v1"), "Should have outbound edge from vertex to v1");
    }

    @Test
    void testAddEdgesAvoidSelfLoop() {
        graph.addVertex("vertex");

        // Policies that match everything including the vertex itself
        InboundEdgePolicy<String> inboundPolicy = vertex -> true;
        OutboundEdgePolicy<String> outboundPolicy = vertex -> true;

        graph.addEdges("vertex", inboundPolicy, outboundPolicy);

        assertFalse(graph.containsEdge("vertex", "vertex"), "Should NOT have self-loop edge");
    }
}
