package com.rayvion.engine.commons.task.pipeline.plan;

import java.util.List;

public record ExecutionPlan(
        List<ExecutionPlanNode<?>> tasks
) { }
