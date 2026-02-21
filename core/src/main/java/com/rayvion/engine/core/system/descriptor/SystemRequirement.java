package com.rayvion.engine.core.system.descriptor;

import com.github.zafarkhaja.semver.Version;

public record SystemRequirement(String id, Version version) { }
