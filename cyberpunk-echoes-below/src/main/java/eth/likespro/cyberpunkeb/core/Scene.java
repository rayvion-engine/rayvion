package eth.likespro.cyberpunkeb.core;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.ArrayList;

@Getter
public class Scene {
    private final Camera camera = new Camera(0, 0);
    @Setter
    private List<Entity> entities = new ArrayList<>();
    @Setter
    private String backgroundId;
}
