package eth.likespro.cyberpunkeb.physics;

import eth.likespro.cyberpunkeb.core.Entity;
import eth.likespro.cyberpunkeb.core.PhysicsEngine;
import eth.likespro.cyberpunkeb.core.component.BoundsComponent;
import eth.likespro.cyberpunkeb.core.component.PhysicsComponent;
import eth.likespro.cyberpunkeb.core.component.TransformComponent;
import org.dyn4j.dynamics.Body;
import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.geometry.Geometry;
import org.dyn4j.geometry.MassType;
import org.dyn4j.geometry.Vector2;
import org.dyn4j.world.World;

import java.util.HashMap;
import java.util.Map;

public class Dyn4jPhysicsAdapter implements PhysicsEngine {
    private World<Body> world;
    private Map<Entity, Body> bodyMap;

    @Override
    public void init() {
        world = new World<>();
        world.setGravity(World.ZERO_GRAVITY);
        bodyMap = new HashMap<>();
    }

    @Override
    public void update(float delta) {
        world.update(delta);
        
        for (Map.Entry<Entity, Body> entry : bodyMap.entrySet()) {
            Entity entity = entry.getKey();
            Body body = entry.getValue();
            
            TransformComponent transform = entity.getComponent(TransformComponent.class);
            if (transform != null) {
                Vector2 position = body.getTransform().getTranslation();
                transform.x = (float) position.x;
                transform.y = (float) position.y;
            }
        }
    }

    @Override
    public void addEntity(Entity entity) {
        TransformComponent transform = entity.getComponent(TransformComponent.class);
        BoundsComponent bounds = entity.getComponent(BoundsComponent.class);
        PhysicsComponent physics = entity.getComponent(PhysicsComponent.class);
        
        if (transform == null || bounds == null || physics == null) return;

        Body body = new Body();
        BodyFixture fixture = body.addFixture(Geometry.createRectangle(bounds.width, bounds.height));
        fixture.setFriction(0.2);
        fixture.setRestitution(0.0);
        
        body.translate(transform.x, transform.y);
        
        if (physics.isStatic) {
            body.setMass(MassType.INFINITE);
        } else {
            body.setMass(MassType.NORMAL);

            body.setAtRestDetectionEnabled(false);
        }
        
        world.addBody(body);
        bodyMap.put(entity, body);
    }

    @Override
    public void removeEntity(Entity entity) {
        Body body = bodyMap.remove(entity);
        if (body != null) {
            world.removeBody(body);
        }
    }

    @Override
    public void applyForce(Entity entity, float fx, float fy) {
        Body body = bodyMap.get(entity);
        if (body != null) {
            body.applyForce(new Vector2(fx, fy));
        }
    }

    @Override
    public void setVelocity(Entity entity, float vx, float vy) {
        Body body = bodyMap.get(entity);
        if (body != null) {
            body.setLinearVelocity(new Vector2(vx, vy));
        }
    }
}
