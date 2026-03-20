package com.rayvion.engine.system.descriptor;

import com.github.zafarkhaja.semver.Version;

public record SystemCoordinate(String namespaceId, String id, Version version) { }
