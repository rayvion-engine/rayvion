package eth.likespro.cyberpunkeb.core.system;


import eth.likespro.cyberpunkeb.core.World;

/**
 * Logic system in the ECS.
 */
public interface System {
    void update(float delta, World world);
}
