package com.rayvion.engine.commons.graph;

public class DefaultDirectedGraph <TVertex, TEdge> extends org.jgrapht.graph.DefaultDirectedGraph<TVertex, TEdge> implements Graph<TVertex, TEdge> {
    public DefaultDirectedGraph(Class<? extends TEdge> edgeClass) {
        super(edgeClass);
    }


    @Override
    public void addInboundEdges(TVertex vertex, InboundEdgePolicy<TVertex> inboundEdgePolicy) {
        vertexSet().forEach(otherVertex -> {
            if(otherVertex != vertex && inboundEdgePolicy.isSatisfiedBy(otherVertex)) addEdge(otherVertex, vertex);
        });
    }

    @Override
    public void addOutboundEdges(TVertex vertex, OutboundEdgePolicy<TVertex> outboundEdgePolicy) {
        vertexSet().forEach(otherVertex -> {
            if(otherVertex != vertex && outboundEdgePolicy.isSatisfiedBy(otherVertex)) addEdge(vertex, otherVertex);
        });
    }
}
