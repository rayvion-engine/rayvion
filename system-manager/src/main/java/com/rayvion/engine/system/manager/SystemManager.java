package com.rayvion.engine.system.manager;

import com.rayvion.engine.commons.graph.DefaultDirectedGraph;
import com.rayvion.engine.commons.graph.Graph;
import com.rayvion.engine.system.System;
import org.jgrapht.graph.DefaultEdge;

import java.util.Optional;

/**
 * Central registry and lifecycle controller for all engine {@link System systems}.
 *
 * <p>The {@code SystemManager} maintains a directed dependency graph whose vertices are
 * registered systems and whose edges represent satisfied dependencies between them. When a system
 * is added, the manager:
 * <ol>
 *   <li>Inserts the system as a vertex.</li>
 *   <li>Scans already-registered systems for inbound dependencies (systems that the new
 *       system depends on) using {@link InboundSystemDependencyPolicy}.</li>
 *   <li>Scans already-registered systems for outbound dependencies (systems that depend on the
 *       new system) using {@link OutboundSystemDependencyPolicy}.</li>
 *   <li>Calls {@link System#onDependencyAdded(System)} on the new system for each inbound
 *       dependency found.</li>
 *   <li>Calls {@link System#init()} on the new system.</li>
 *   <li>Notifies existing systems that depend on the new system by calling their
 *       {@link System#onDependencyAdded(System)}.</li>
 * </ol>
 *
 * <p>When a system is removed the manager notifies all outbound dependents first and then
 * removes the vertex from the graph, effectively severing all dependency edges.
 *
 * <p><b>Thread safety:</b> this class is <em>not</em> thread-safe. All mutations should be
 * performed from a single thread.
 */
public class SystemManager {
    private final Graph<System, DefaultEdge> graph = new DefaultDirectedGraph<>(DefaultEdge.class);

    /**
     * Registers a system, resolves its dependencies, and initialises it.
     *
     * <p>After this method returns, the system is fully initialised and any already-registered
     * systems that depended on it have been notified.
     *
     * @param system the system to add; must not be {@code null} and must not already be
     *               registered
     * @throws IllegalArgumentException if {@code system} is already present in the graph
     */
    public void addSystem(System system) {
        graph.addVertex(system);

        graph.addInboundEdges(system, new InboundSystemDependencyPolicy(system));
        graph.addOutboundEdges(system, new OutboundSystemDependencyPolicy(system));

        graph.getInboundVertices(system).forEach(system::onDependencyAdded);
        system.init();
        graph.getOutboundVertices(system).forEach(outboundDependency -> outboundDependency.onDependencyAdded(system));
    }

    /**
     * Deregisters a system and notifies its dependents.
     *
     * <p>Before the system is removed from the graph, every registered system that depends on
     * it receives a call to {@link System#onDependencyRemoved(System)}. The system's own
     * {@link System#dispose()} is <em>not</em> called here; callers are responsible for
     * disposal if required.
     *
     * @param system the system to remove; must not be {@code null}
     */
    public void removeSystem(System system) {
        graph.getOutboundVertices(system).forEach(outboundDependency -> outboundDependency.onDependencyRemoved(system));

        graph.removeVertex(system);
    }

    /**
     * Looks up the first registered system that is an instance of the given class.
     *
     * <p>Useful for retrieving a specific system implementation by its concrete or interface
     * type without holding a direct reference to the instance.
     *
     * @param <T>         the system type to search for
     * @param systemClass the {@link Class} token for the desired system type; must not be
     *                    {@code null}
     * @return an {@link Optional} containing the first matching system, or
     *         {@link Optional#empty()} if none is registered
     */
    public <T extends System> Optional<T> getSystem(Class<T> systemClass) {
        return graph.vertexSet().stream()
                .filter(systemClass::isInstance)
                .map(systemClass::cast)
                .findFirst();
    }
}
