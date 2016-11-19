package com.sidereal.lumm.components.audio;

import com.badlogic.gdx.audio.Sound;
import com.sidereal.lumm.architecture.Lumm;
import com.sidereal.lumm.architecture.core.Audio.AudioChannel;

public class SoundClip extends AudioClip {

	Sound sound;

	private float volume, pan, pitch;
	
	private long soundId;

	public SoundClip(String filepath, AudioChannel channel, AudioSource source) {
		super(filepath, channel, source);
		sound = Lumm.assets.get(filepath, Sound.class);
		supportsCompletionEvents = false;
	}

	public SoundClip(String filepath, String customAudioChannelName, AudioSource source) {

		super(filepath, customAudioChannelName, source);
		sound = Lumm.assets.get(filepath, Sound.class);
		supportsCompletionEvents = false;
	}

	@Override
	public void onPlay() {
		soundId = sound.play(volume, pitch, pan);
	}

	@Override
	public void onPause() {
		sound.pause(soundId);
	}

	@Override
	public void onResume() {
		sound.resume(soundId);
	}

	@Override
	public void onStop() {
		sound.stop(soundId);
	}

	@Override
	protected boolean isCompleted() {
		// Sound clips do not support knowing when a sound is done, return true
		// so AudioTask.iSDone will always return true.
		return true;
	}

	@Override
	protected void onFilePathChange(String filepath) {
		sound = Lumm.assets.get(filepath, Sound.class);
	}

	@Override
	protected void onUnload() {
		unload(getFilepath());

	}

	@Override
	protected void onInternalVolumeChange(float volume, float pan, float pitch) {
		sound.setVolume(soundId, volume);
		this.volume = volume;
	}

	@Override
	protected void onInternalPanChange(float volume, float pan, float pitch) {
		sound.setPan(soundId, pan, volume);
		this.pan = pan;
	}

	@Override
	protected void onInternalPitchChange(float volume, float pan, float pitch) {
		sound.setPitch(soundId, pitch);
		this.pitch = pitch;
	}

	public static void load(String filepath) {
		Lumm.assets.load(filepath, Sound.class);
	}

	public static void unload(String filepath) {
		Lumm.assets.unload(filepath);
	}

}
