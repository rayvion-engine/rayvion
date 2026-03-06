package com.rayvion.engine.commons.graph;

public interface OutboundEdgePolicy<TVertex> {
    boolean isSatisfiedBy(TVertex source);
}
