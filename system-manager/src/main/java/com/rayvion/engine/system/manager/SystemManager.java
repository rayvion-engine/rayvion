package com.rayvion.engine.system.manager;

import com.rayvion.engine.commons.graph.DefaultDirectedGraph;
import com.rayvion.engine.commons.graph.Graph;
import com.rayvion.engine.system.System;
import org.jgrapht.graph.DefaultEdge;

public class SystemManager {
    private final Graph<System, DefaultEdge> graph = new DefaultDirectedGraph<>(DefaultEdge.class);

    public void addSystem(System system) {
        graph.addVertex(system);

        graph.addInboundEdges(system, new InboundSystemDependencyPolicy(system));
        graph.addOutboundEdges(system, new OutboundSystemDependencyPolicy(system));

        graph.getInboundVertices(system).forEach(system::onDependencyAdded);
        graph.getOutboundVertices(system).forEach(outboundDependency -> outboundDependency.onDependencyAdded(system));
    }
}
