package com.rayvion.engine.commons.task.descriptor;

import com.rayvion.engine.commons.identity.namespace.HierarchyNamespace;
import com.rayvion.engine.commons.identity.namespace.NamespacedIdentity;
import lombok.Getter;

public record TaskIdentity(
        @Getter
        HierarchyNamespace<String> namespace,
        @Getter
        String id
) implements NamespacedIdentity<HierarchyNamespace<String>, String> { }
