package com.sidereal.lumm.components.audio;

import com.badlogic.gdx.audio.Music;
import com.sidereal.lumm.architecture.Lumm;
import com.sidereal.lumm.architecture.core.Audio.AudioChannel;

public class MusicClip extends AudioClip {

    Music clip;

    public MusicClip(String filepath, AudioChannel category, AudioSource source) {

        super(filepath, category, source);
        clip = Lumm.assets.get(filepath, Music.class);
        supportsCompletionEvents = true;
    }

    public MusicClip(String filepath, String customAudioChannelName, AudioSource source) {
        super(filepath, customAudioChannelName, source);
        clip = Lumm.assets.get(filepath, Music.class);
        supportsCompletionEvents = true;
    }

    @Override
    public void onPlay() {
        clip.play();
    }

    @Override
    public void onPause() {
        clip.pause();
    }

    @Override
    public void onResume() {
        if (clip.getPosition() != 0)
            clip.play();
    }

    @Override
    public void onStop() {
        clip.stop();
    }

    /**
     * Sets whether to loop the clip or not. This will not start playing the
     * clip.
     *
     * @param value
     */
    public void setLooping(boolean value) {
        clip.setLooping(value);
    }

    @Override
    protected boolean isCompleted() {
        return clip.isPlaying();
    }

    @Override
    protected void onInternalVolumeChange(float volume, float pan, float pitch) {
        clip.setVolume(volume);
    }

    @Override
    protected void onInternalPanChange(float volume, float pan, float pitch) {
        clip.setPan(pan, volume);
    }

    @Override
    protected void onInternalPitchChange(float volume, float pan, float pitch) {

    }

    @Override
    protected void onFilePathChange(String filepath) {
        clip = Lumm.assets.get(filepath, Music.class);
    }

    @Override
    protected void onUnload() {
        unload(getFilepath());
    }

    public static void load(String filepath) {
        Lumm.assets.load(filepath, Music.class);
    }

    public static void unload(String filepath) {
        Lumm.assets.unload(filepath);
    }

}
