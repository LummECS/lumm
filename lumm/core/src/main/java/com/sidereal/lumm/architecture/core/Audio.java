package com.sidereal.lumm.architecture.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.sidereal.lumm.architecture.Lumm;
import com.sidereal.lumm.architecture.LummConfiguration;
import com.sidereal.lumm.architecture.LummModule;

public class Audio extends LummModule {

    public static final String MASTER_VOLUME_KEY = "Master volume key";

    public static final String MUSIC_VOLUME_KEY = "Music volume key";

    public static final String VOICE_VOLUME_KEY = "Voice volume key";

    public static final String EFFECTS_VOLUME_KEY = "Effects volume key";

    public static final String ENVIRONMENT_VOLUME_KEY = "Environment volume key";

    public static final String UI_VOLUME_KEY = "UI volume key";

    public static enum AudioChannel {
        /**
         * Represents the master volume, a value between 0 and 1 which
         * represents the overall base volume of all sounds
         */
        Master,
        /**
         * Represents the music volume, a value between 0 and 1 which represents
         * the base value of music elements, after using {@link #Master}.
         */
        Music,

        /**
         * Represents the volume of narration, a value between 0 and 1 which
         * represents the base value of voice elements, after using
         * {@link #Master}.
         */
        Voice,

        /**
         * Represents the volume of Effects, a value between 0 and 1 which
         * represents the base value of effects elements, after using
         * {@link #Master}.
         */
        Effects,

        /**
         * Represents the volume of Environment sounds, a value between 0 and 1
         * which represents the base value of environment sounds, after using
         * {@link #Master}.
         */
        Environment,

        /**
         * Represents the volume of sounds made using UI elements ( such as
         * buttons, sliders, windows opening ). The volume is applied after the
         * master volume.
         */
        UI
    }

    public Audio(LummConfiguration config) {
        super(config);
        setUpdateFrequency(-1);
    }

    @Override
    public void onCreate() {
    }

    @Override
    public void onUpdate() {

    }


    /**
     * Sets the volume of one of the predefined {@link AudioChannel} values.
     *
     * @param volume Volume type, to be set after applying
     *               {@link AudioChannel#Master} if not changing that value.
     * @param value  Volume value, between 0(mute) and 1(full volume). Value will
     *               be clamped between those values
     */
    public void setVolume(AudioChannel volume, float value) {
        switch (volume) {
            case Master:
                Lumm.data.audioSettings.put(MASTER_VOLUME_KEY, new Float(value));
                break;
            case Music:
                Lumm.data.audioSettings.put(MUSIC_VOLUME_KEY, new Float(value));
                break;
            case Voice:
                Lumm.data.audioSettings.put(VOICE_VOLUME_KEY, new Float(value));
                break;
            case Effects:
                Lumm.data.audioSettings.put(EFFECTS_VOLUME_KEY, new Float(value));
                break;
            case Environment:
                Lumm.data.audioSettings.put(ENVIRONMENT_VOLUME_KEY, new Float(value));
                break;
            case UI:
                Lumm.data.audioSettings.put(UI_VOLUME_KEY, new Float(value));
                break;
        }
        Lumm.data.save(AppData.AUDIO_SETTINGS_PATH, Lumm.data.audioSettings, true, true);
    }

    public void setVolume(String customAudioChannel, float value) {
        if (customAudioChannel.equals(MASTER_VOLUME_KEY) || customAudioChannel.equals(MUSIC_VOLUME_KEY)
                || customAudioChannel.equals(VOICE_VOLUME_KEY) || customAudioChannel.equals(EFFECTS_VOLUME_KEY)
                || customAudioChannel.equals(ENVIRONMENT_VOLUME_KEY) || customAudioChannel.equals(UI_VOLUME_KEY)) {
            Lumm.debug.logDebug(
                    "Calling Lumm.audio.setvolume for a custom audio channel that has a name equal to a default channel, thus ovverriding channel \""
                            + customAudioChannel + "\"",
                    null);
        }
        Lumm.data.audioSettings.put(customAudioChannel, new Float(value));
        Lumm.data.save(AppData.AUDIO_SETTINGS_PATH, Lumm.data.audioSettings, true, true);

        // disk
    }

    /**
     * Gets the volume for one of the predefined {@link AudioChannel values}
     *
     * @param volume the key for the volume value to return.
     * @return
     */
    public float getVolume(AudioChannel volume) {

        switch (volume) {
            case Master:
                return ((Float) (Lumm.data.audioSettings.get(MASTER_VOLUME_KEY)));
            case Music:
                return ((Float) (Lumm.data.audioSettings.get(MUSIC_VOLUME_KEY)));
            case Voice:
                return ((Float) (Lumm.data.audioSettings.get(VOICE_VOLUME_KEY)));
            case Effects:
                return ((Float) (Lumm.data.audioSettings.get(EFFECTS_VOLUME_KEY)));
            case Environment:
                return ((Float) (Lumm.data.audioSettings.get(ENVIRONMENT_VOLUME_KEY)));
            case UI:
                return ((Float) (Lumm.data.audioSettings.get(UI_VOLUME_KEY)));
            default:
                return ((Float) (Lumm.data.audioSettings.get(MASTER_VOLUME_KEY)));
        }
    }

    public float getVolume(String customAudioChannel) {
        if (!Lumm.data.audioSettings.containsKey(customAudioChannel))
            Lumm.data.audioSettings.put(customAudioChannel, new Float(1));

        return ((Float) (Lumm.data.audioSettings.get(customAudioChannel)));
    }


    @Override
    public List<Class<? extends LummModule>> getDependencies() {
        List<Class<? extends LummModule>> modules = new ArrayList<Class<? extends LummModule>>();
        modules.add(AppData.class);
        return modules;
    }

}
