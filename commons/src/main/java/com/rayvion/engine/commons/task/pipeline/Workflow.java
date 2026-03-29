package com.rayvion.engine.commons.task.pipeline;

import com.rayvion.engine.commons.graph.DefaultDirectedGraph;
import com.rayvion.engine.commons.graph.Graph;
import com.rayvion.engine.commons.identity.namespace.StringHierarchyNamespaceFactory;
import com.rayvion.engine.commons.task.Task;
import com.rayvion.engine.commons.task.descriptor.TaskDescriptor;
import com.rayvion.engine.commons.task.descriptor.TaskIdentity;
import lombok.Getter;
import org.jgrapht.graph.DefaultEdge;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class Workflow {
    public static final Task<Object> ROOT_TASK = new Task<Object>(
            new TaskDescriptor<>(
                    new TaskIdentity(StringHierarchyNamespaceFactory.parse(""), ""),
                    Set.of(),
                    Set.of()
            ),
            (Map<TaskDescriptor<?>, ?> dependenciesResults) -> CompletableFuture.completedFuture(null)
    );

    @Getter
    private final Graph<Task<?>, DefaultEdge> graph = new DefaultDirectedGraph<>(DefaultEdge.class);

    public Workflow() {
        graph.addVertex(ROOT_TASK);
    }


    public void addTask(Task<?> task) {
        graph.addVertex(task);

        graph.addInboundEdges(task, new InboundTaskDependencyPolicy(task));
        graph.addOutboundEdges(task, new OutboundTaskDependencyPolicy(task));

        if(graph.getInboundVertices(task).isEmpty()) {
            graph.addEdge(ROOT_TASK, task);
        }
    }

    public void removeTask(Task<?> task) {
        graph.removeVertex(task);
    }
}
