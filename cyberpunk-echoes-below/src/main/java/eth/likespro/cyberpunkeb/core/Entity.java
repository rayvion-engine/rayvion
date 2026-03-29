package eth.likespro.cyberpunkeb.core;


import eth.likespro.cyberpunkeb.core.component.Component;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
public class Entity {
    @Getter
    private final String id;
    private final Map<Class<? extends Component>, Component> components = new HashMap<>();

    public <T extends Component> void addComponent(T component) {
        components.put(component.getClass(), component);
    }

    public <T extends Component> T getComponent(Class<T> clazz) {
        return clazz.cast(components.get(clazz));
    }

    public boolean hasComponent(Class<? extends Component> type) {
        return components.containsKey(type);
    }
}
