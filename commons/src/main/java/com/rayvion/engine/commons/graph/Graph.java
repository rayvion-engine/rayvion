package com.rayvion.engine.commons.graph;

import org.jgrapht.Graphs;

import java.util.List;

public interface Graph <TVertex, TEdge> extends org.jgrapht.Graph<TVertex, TEdge> {
    default void addEdges(TVertex vertex, InboundEdgePolicy<TVertex> inboundEdgePolicy, OutboundEdgePolicy<TVertex> outboundEdgePolicy) {
        addInboundEdges(vertex, inboundEdgePolicy);
        addOutboundEdges(vertex, outboundEdgePolicy);
    }
    void addInboundEdges(TVertex vertex, InboundEdgePolicy<TVertex> inboundEdgePolicy);
    void addOutboundEdges(TVertex vertex, OutboundEdgePolicy<TVertex> outboundEdgePolicy);

    default List<TVertex> getInboundVertices(TVertex vertex) {
        return Graphs.predecessorListOf(this, vertex);
    }
    default List<TVertex> getOutboundVertices(TVertex vertex) {
        return Graphs.successorListOf(this, vertex);
    }
}
