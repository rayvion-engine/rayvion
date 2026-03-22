package com.rayvion.engine.commons.identity.namespace;

import com.rayvion.engine.commons.identity.Identity;

public interface NamespacedIdentity<TNamespace, TId> extends Identity {
    TNamespace getNamespace();
    TId getId();
}
