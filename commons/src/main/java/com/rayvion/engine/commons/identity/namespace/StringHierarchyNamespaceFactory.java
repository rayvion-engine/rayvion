package com.rayvion.engine.commons.identity.namespace;

import java.util.List;

public class StringHierarchyNamespaceFactory {
    public static HierarchyNamespace<String> parse(String hierarchy) {
        String[] parts = hierarchy.split("\\.");
        return new HierarchyNamespace<>(List.of(parts));
    }
}
