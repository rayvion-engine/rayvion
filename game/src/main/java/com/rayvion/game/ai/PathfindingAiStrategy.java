package com.rayvion.game.ai;

import com.rayvion.engine.ai.AiStrategy;
import com.rayvion.engine.physics.PhysicsBody;
import com.rayvion.engine.physics.PhysicsSystem;
import com.rayvion.engine.transform.Transform;
import com.rayvion.engine.transform.TransformSystem;
import com.rayvion.engine.characteristic.CharacteristicSystem;
import com.rayvion.engine.entity.Entity;
import lombok.extern.slf4j.Slf4j;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.AStarShortestPath;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;

import java.util.ArrayList;
import java.util.List;

/**
 * A smart AI strategy that uses A* pathfinding to chase a target, going around walls.
 */
@Slf4j
public class PathfindingAiStrategy implements AiStrategy {
    private final long targetEntityId;
    private final long worldId;
    private final TransformSystem transformSystem;
    private final PhysicsSystem physicsSystem;
    private final CharacteristicSystem characteristicSystem;
    private final double tileSize;
    private final int gridWidth;
    private final int gridHeight;
    private final double detectionRange;

    private List<Node> currentPath = new ArrayList<>();
    private int currentWaypointIndex = 0;
    private long lastRecalculationTime = 0;
    private double lastTargetX = Double.NaN;
    private double lastTargetY = Double.NaN;

    private static final long RECALCULATION_INTERVAL_MS = 500;
    private static final double TARGET_MOVE_THRESHOLD = 32.0; // Recalculate if target moves > 1 tile

    public PathfindingAiStrategy(long targetEntityId, long worldId, TransformSystem transformSystem, 
                                 PhysicsSystem physicsSystem, CharacteristicSystem characteristicSystem, double tileSize, 
                                 int gridWidth, int gridHeight) {
        this(targetEntityId, worldId, transformSystem, physicsSystem, characteristicSystem, tileSize, gridWidth, gridHeight,
            AiVisibility.DEFAULT_DETECTION_RANGE);
    }

    public PathfindingAiStrategy(long targetEntityId, long worldId, TransformSystem transformSystem,
                                 PhysicsSystem physicsSystem, CharacteristicSystem characteristicSystem, double tileSize,
                                 int gridWidth, int gridHeight, double detectionRange) {
        this.targetEntityId = targetEntityId;
        this.worldId = worldId;
        this.transformSystem = transformSystem;
        this.physicsSystem = physicsSystem;
        this.characteristicSystem = characteristicSystem;
        this.tileSize = tileSize;
        this.gridWidth = gridWidth;
        this.gridHeight = gridHeight;
        this.detectionRange = detectionRange;
    }

    private record Node(int x, int y) {}

    @Override
    public void update(long entityId) {
        if (!transformSystem.hasTransform(entityId) || !transformSystem.hasTransform(targetEntityId)) {
            return;
        }

        Transform enemyTransform = transformSystem.getTransform(entityId);
        Transform targetTransform = transformSystem.getTransform(targetEntityId);

        double ex = enemyTransform.getX();
        double ey = enemyTransform.getY();
        double tx = targetTransform.getX();
        double ty = targetTransform.getY();

        if (!AiVisibility.canDetectTarget(tx - ex, ty - ey, detectionRange)) {
            currentPath = new ArrayList<>();
            currentWaypointIndex = 0;
            PhysicsBody body = physicsSystem.getBody(worldId, entityId);
            if (body != null) {
                body.setVelocity(0, 0);
            }
            return;
        }

        long currentTime = System.currentTimeMillis();
        boolean shouldRecalculate = (currentTime - lastRecalculationTime > RECALCULATION_INTERVAL_MS) ||
                                    Double.isNaN(lastTargetX) ||
                                    Math.sqrt(Math.pow(tx - lastTargetX, 2) + Math.pow(ty - lastTargetY, 2)) > TARGET_MOVE_THRESHOLD;

        if (shouldRecalculate) {
            recalculatePath(ex, ey, tx, ty);
            lastRecalculationTime = currentTime;
            lastTargetX = tx;
            lastTargetY = ty;
        }

        followPath(entityId, ex, ey);
    }

    private void recalculatePath(double startX, double startY, double endX, double endY) {
        int sx = (int) (startX / tileSize);
        int sy = (int) (startY / tileSize);
        int gx = (int) (endX / tileSize);
        int gy = (int) (endY / tileSize);

        // Clamp to grid
        sx = Math.max(0, Math.min(gridWidth - 1, sx));
        sy = Math.max(0, Math.min(gridHeight - 1, sy));
        gx = Math.max(0, Math.min(gridWidth - 1, gx));
        gy = Math.max(0, Math.min(gridHeight - 1, gy));

        if (sx == gx && sy == gy) {
            currentPath = List.of(new Node(gx, gy));
            currentWaypointIndex = 0;
            return;
        }

        SimpleWeightedGraph<Node, DefaultWeightedEdge> graph = new SimpleWeightedGraph<>(DefaultWeightedEdge.class);

        // Add nodes and edges
        for (int y = 0; y < gridHeight; y++) {
            for (int x = 0; x < gridWidth; x++) {
                if (!isBlocked(x, y)) {
                    Node node = new Node(x, y);
                    graph.addVertex(node);
                    
                    // Check neighbors (up, down, left, right)
                    if (x > 0 && !isBlocked(x - 1, y)) {
                        Node neighbor = new Node(x - 1, y);
                        graph.addVertex(neighbor);
                        DefaultWeightedEdge edge = graph.addEdge(node, neighbor);
                        if (edge != null) graph.setEdgeWeight(edge, 1.0);
                    }
                    if (y > 0 && !isBlocked(x, y - 1)) {
                        Node neighbor = new Node(x, y - 1);
                        graph.addVertex(neighbor);
                        DefaultWeightedEdge edge = graph.addEdge(node, neighbor);
                        if (edge != null) graph.setEdgeWeight(edge, 1.0);
                    }
                }
            }
        }

        Node startNode = new Node(sx, sy);
        Node goalNode = new Node(gx, gy);

        if (!graph.containsVertex(startNode) || !graph.containsVertex(goalNode)) {
            log.warn("Start or goal node is blocked or out of bounds! Start: {}, Goal: {}", startNode, goalNode);
            currentPath = new ArrayList<>();
            return;
        }

        AStarShortestPath<Node, DefaultWeightedEdge> astar = new AStarShortestPath<>(graph, (n1, n2) -> 
            Math.sqrt(Math.pow(n1.x - n2.x, 2) + Math.pow(n1.y - n2.y, 2))
        );

        GraphPath<Node, DefaultWeightedEdge> path = astar.getPath(startNode, goalNode);
        if (path != null) {
            currentPath = path.getVertexList();
            currentWaypointIndex = 1; // Start from the first waypoint after the current position
            log.trace("AI Strategy: Path found with {} nodes", currentPath.size());
        } else {
            log.warn("AI Strategy: No path found!");
            currentPath = new ArrayList<>();
        }
    }

    private boolean isBlocked(int x, int y) {
        double centerX = x * tileSize + tileSize / 2.0;
        double centerY = y * tileSize + tileSize / 2.0;
        return physicsSystem.isPointBlocked(worldId, centerX, centerY);
    }

    private void followPath(long entityId, double ex, double ey) {
        PhysicsBody body = physicsSystem.getBody(worldId, entityId);
        if (body == null) return;

        if (currentPath.isEmpty() || currentWaypointIndex >= currentPath.size()) {
            body.setVelocity(0, 0);
            return;
        }

        Node waypoint = currentPath.get(currentWaypointIndex);
        double wx = waypoint.x * tileSize + tileSize / 2.0;
        double wy = waypoint.y * tileSize + tileSize / 2.0;

        double dx = wx - ex;
        double dy = wy - ey;
        double distance = Math.sqrt(dx * dx + dy * dy);

        if (distance < 5.0) { // Waypoint reached
            currentWaypointIndex++;
            if (currentWaypointIndex >= currentPath.size()) {
                body.setVelocity(0, 0);
            } else {
                // Move to next waypoint immediately
                followPath(entityId, ex, ey);
            }
        } else {
            double speed = characteristicSystem.getValue(new Entity(entityId), "speed");
            double vx = (dx / distance) * speed;
            double vy = (dy / distance) * speed;
            body.setVelocity(vx, vy);
            body.setRotation(Math.atan2(vy, vx));
        }
    }
}
