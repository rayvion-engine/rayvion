package com.rayvion.engine.audio;

import com.github.zafarkhaja.semver.Version;
import com.rayvion.engine.system.System;
import com.rayvion.engine.system.descriptor.SystemDescriptor;
import com.rayvion.engine.system.descriptor.SystemCoordinate;
import com.rayvion.engine.system.trait.SystemTraitCoordinate;

import java.util.Collections;
import java.util.Set;

public interface AudioSystem extends System {
    SystemTraitCoordinate TRAIT = new SystemTraitCoordinate("com.rayvion.engine", "audio", Version.parse("0.1.0"));

    @Override
    default SystemDescriptor getDescriptor() {
        return new SystemDescriptor(
                new SystemCoordinate("com.rayvion.engine", "audio", Version.parse("0.1.0")),
                Collections.emptySet(),
                Set.of(TRAIT)
        );
    }

    void playSound(String soundId);
    
    void playMusic(String musicId);
    
    void stopMusic();
    
    void setMusicVolume(float volume);
    
    void setSoundVolume(float volume);
}
