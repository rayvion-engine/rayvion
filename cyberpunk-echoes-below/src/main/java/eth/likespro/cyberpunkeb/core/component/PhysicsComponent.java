package eth.likespro.cyberpunkeb.core.component;

public class PhysicsComponent implements Component {
    public boolean isStatic;
    public float velocityX;
    public float velocityY;

    public PhysicsComponent(boolean isStatic) {
        this.isStatic = isStatic;
    }
}
