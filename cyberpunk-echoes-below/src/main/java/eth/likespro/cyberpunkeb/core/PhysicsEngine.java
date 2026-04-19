package eth.likespro.cyberpunkeb.core;

public interface PhysicsEngine {
    void init();
    void update(float delta);
    void addEntity(Entity entity);
    void removeEntity(Entity entity);
    void applyForce(Entity entity, float fx, float fy);
    void setVelocity(Entity entity, float vx, float vy);
}
