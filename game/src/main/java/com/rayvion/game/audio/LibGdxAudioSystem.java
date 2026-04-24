package com.rayvion.game.audio;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.rayvion.engine.audio.AudioSystem;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public class LibGdxAudioSystem implements AudioSystem {
    private final Map<String, Sound> sounds = new HashMap<>();
    private final Map<String, Music> musics = new HashMap<>();
    private Music currentMusic;
    private float soundVolume = 1.0f;
    private float musicVolume = 0.5f;

    @Override
    public void init() {
        log.info("LibGdxAudioSystem initializing...");
    }

    @Override
    public void playSound(String soundId) {
        Sound sound = sounds.computeIfAbsent(soundId, id -> {
            try {
                String path = "audio/" + id + ".wav";
                log.debug("Loading sound: {}", path);
                return Gdx.audio.newSound(Gdx.files.internal(path));
            } catch (Exception e) {
                log.error("Failed to load sound: {}", id, e);
                return null;
            }
        });

        if (sound != null) {
            sound.play(soundVolume);
        }
    }

    @Override
    public void playMusic(String musicId) {
        if (currentMusic != null) {
            currentMusic.stop();
        }

        currentMusic = musics.computeIfAbsent(musicId, id -> {
            try {
                String path = "audio/" + id + ".wav";
                log.debug("Loading music: {}", path);
                Music music = Gdx.audio.newMusic(Gdx.files.internal(path));
                music.setLooping(true);
                return music;
            } catch (Exception e) {
                log.error("Failed to load music: {}", id, e);
                return null;
            }
        });

        if (currentMusic != null) {
            currentMusic.setVolume(musicVolume);
            currentMusic.play();
        }
    }

    @Override
    public void stopMusic() {
        if (currentMusic != null) {
            currentMusic.stop();
        }
    }

    @Override
    public void setMusicVolume(float volume) {
        this.musicVolume = volume;
        if (currentMusic != null) {
            currentMusic.setVolume(volume);
        }
    }

    @Override
    public void setSoundVolume(float volume) {
        this.soundVolume = volume;
    }

    public void dispose() {
        for (Sound sound : sounds.values()) {
            sound.dispose();
        }
        for (Music music : musics.values()) {
            music.dispose();
        }
        sounds.clear();
        musics.clear();
    }
}
