package com.rayvion.game.audio;

import com.badlogic.gdx.Audio;
import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.files.FileHandle;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class LibGdxAudioSystemTest {

    private LibGdxAudioSystem audioSystem;
    private Audio mockAudio;
    private Files mockFiles;

    @BeforeEach
    void setUp() {
        mockAudio = mock(Audio.class);
        mockFiles = mock(Files.class);
        Gdx.audio = mockAudio;
        Gdx.files = mockFiles;

        audioSystem = new LibGdxAudioSystem();
    }

    @AfterEach
    void tearDown() {
        Gdx.audio = null;
        Gdx.files = null;
    }

    @Test
    void testInit() {
        assertDoesNotThrow(() -> audioSystem.init());
    }

    @Test
    void testPlaySoundSuccess() {
        Sound mockSound = mock(Sound.class);
        FileHandle mockFileHandle = mock(FileHandle.class);
        when(mockFiles.internal(anyString())).thenReturn(mockFileHandle);
        when(mockAudio.newSound(mockFileHandle)).thenReturn(mockSound);

        audioSystem.playSound("test_sound");
        verify(mockAudio, times(1)).newSound(mockFileHandle);
        verify(mockSound, times(1)).play(1.0f);

        // Tst caching
        audioSystem.playSound("test_sound");
        verify(mockAudio, times(1)).newSound(mockFileHandle);
        verify(mockSound, times(2)).play(1.0f);
    }

    @Test
    void testPlaySoundFailure() {
        FileHandle mockFileHandle = mock(FileHandle.class);
        when(mockFiles.internal(anyString())).thenReturn(mockFileHandle);
        when(mockAudio.newSound(mockFileHandle)).thenThrow(new RuntimeException("Load failed"));

        assertDoesNotThrow(() -> audioSystem.playSound("fail_sound"));
        verify(mockAudio, times(1)).newSound(mockFileHandle);
    }

    @Test
    void testPlayMusicSuccess() {
        Music mockMusic = mock(Music.class);
        FileHandle mockFileHandle = mock(FileHandle.class);
        when(mockFiles.internal(anyString())).thenReturn(mockFileHandle);
        when(mockAudio.newMusic(mockFileHandle)).thenReturn(mockMusic);

        audioSystem.playMusic("test_music");
        verify(mockAudio, times(1)).newMusic(mockFileHandle);
        verify(mockMusic, times(1)).setLooping(true);
        verify(mockMusic, times(1)).setVolume(0.5f);
        verify(mockMusic, times(1)).play();

        // Test caching and stopping current music
        Music mockMusic2 = mock(Music.class);
        FileHandle mockFileHandle2 = mock(FileHandle.class);
        when(mockFiles.internal("audio/other_music.wav")).thenReturn(mockFileHandle2);
        when(mockAudio.newMusic(mockFileHandle2)).thenReturn(mockMusic2);
        
        audioSystem.playMusic("other_music");
        verify(mockMusic, times(1)).stop();
        verify(mockMusic2, times(1)).play();
    }

    @Test
    void testPlayMusicFailure() {
        FileHandle mockFileHandle = mock(FileHandle.class);
        when(mockFiles.internal(anyString())).thenReturn(mockFileHandle);
        when(mockAudio.newMusic(mockFileHandle)).thenThrow(new RuntimeException("Load failed"));

        assertDoesNotThrow(() -> audioSystem.playMusic("fail_music"));
    }

    @Test
    void testStopMusic() {
        Music mockMusic = mock(Music.class);
        FileHandle mockFileHandle = mock(FileHandle.class);
        when(mockFiles.internal(anyString())).thenReturn(mockFileHandle);
        when(mockAudio.newMusic(mockFileHandle)).thenReturn(mockMusic);

        audioSystem.playMusic("test_music");
        audioSystem.stopMusic();
        verify(mockMusic, times(1)).stop();

        // No NPE if currentMusic is null
        LibGdxAudioSystem emptySystem = new LibGdxAudioSystem();
        assertDoesNotThrow(emptySystem::stopMusic);
    }

    @Test
    void testSetMusicVolume() {
        Music mockMusic = mock(Music.class);
        FileHandle mockFileHandle = mock(FileHandle.class);
        when(mockFiles.internal(anyString())).thenReturn(mockFileHandle);
        when(mockAudio.newMusic(mockFileHandle)).thenReturn(mockMusic);

        audioSystem.setMusicVolume(0.8f);
        audioSystem.playMusic("test_music");
        verify(mockMusic).setVolume(0.8f);

        audioSystem.setMusicVolume(0.2f);
        verify(mockMusic).setVolume(0.2f);
    }

    @Test
    void testSetSoundVolume() {
        Sound mockSound = mock(Sound.class);
        FileHandle mockFileHandle = mock(FileHandle.class);
        when(mockFiles.internal(anyString())).thenReturn(mockFileHandle);
        when(mockAudio.newSound(mockFileHandle)).thenReturn(mockSound);

        audioSystem.setSoundVolume(0.7f);
        audioSystem.playSound("test_sound");
        verify(mockSound).play(0.7f);
    }

    @Test
    void testDispose() {
        Sound mockSound = mock(Sound.class);
        Music mockMusic = mock(Music.class);
        FileHandle mockFileHandle = mock(FileHandle.class);
        when(mockFiles.internal(anyString())).thenReturn(mockFileHandle);
        when(mockAudio.newSound(any())).thenReturn(mockSound);
        when(mockAudio.newMusic(any())).thenReturn(mockMusic);

        audioSystem.playSound("s1");
        audioSystem.playMusic("m1");

        audioSystem.dispose();
        verify(mockSound).dispose();
        verify(mockMusic).dispose();

        // Verify maps are cleared by checking that next play causes another load
        audioSystem.playSound("s1");
        verify(mockAudio, times(2)).newSound(any());
    }
}
