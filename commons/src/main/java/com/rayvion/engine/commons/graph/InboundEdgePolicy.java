package com.rayvion.engine.commons.graph;

public interface InboundEdgePolicy<TVertex> {
    boolean isSatisfiedBy(TVertex target);
}
