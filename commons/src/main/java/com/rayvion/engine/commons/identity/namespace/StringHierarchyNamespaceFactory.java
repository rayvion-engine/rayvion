package com.rayvion.engine.commons.identity.namespace;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class StringHierarchyNamespaceFactory {
    public static @NotNull HierarchyNamespace<String> parse(@NotNull String hierarchy) {
        String[] parts = hierarchy.split("\\.");
        return new HierarchyNamespace<>(List.of(parts));
    }
}
