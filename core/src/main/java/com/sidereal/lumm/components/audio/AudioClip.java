package com.sidereal.lumm.components.audio;

import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.sidereal.lumm.architecture.Lumm;
import com.sidereal.lumm.architecture.core.Audio;
import com.sidereal.lumm.architecture.core.Audio.AudioChannel;
import com.sidereal.lumm.components.audio.AudioClipTask.AudioTask;
import com.sidereal.lumm.util.Utility;

/**
 * Abstract class which contains functionality for playing, stopping, pausing,
 * resuming and restarting sounds. Further implementations only have to handle
 * the abstract methods that are part of the functionality, as well as how
 * certain Audio Clip extensions handle changing the internal volume, panning or
 * pitch if applicable.
 * <p>
 * It is OK if extensions do not handle all of the functionality that is
 * presented with this class, but the user should be informed in the
 * documentation.
 * 
 * @author Claudiu Bele
 */
public abstract class AudioClip {

	/**
	 * Handles how the listener for the audio clips is handled and whether to
	 * automatically bind to the currently-selected active listener
	 * 
	 * @author Claudiu Bele
	 */
	public enum AudioListenerInteraction {
		/**
		 * The clip plays with disregard to any audio listeners. Thus, no
		 * panning is applied and the distance between the source(if present)
		 * and the listener (if present) will be disregarded.
		 * <p>
		 * Calling {@link AudioClip#setAudioListener(AudioListener)} will be
		 * useless until the clip's {@link AudioListenerInteraction} is set in
		 * {@link AudioClip#setAudioListenerInteraction(AudioListenerInteraction)}
		 * to {@link AudioListenerInteraction#Target}.
		 */
		None,

		/**
		 * The clip will play with the source calculating the distance to the
		 * currently-enabled audio listener.
		 * <p>
		 * Calling {@link AudioClip#setAudioListener(AudioListener)} will be
		 * useless until the clip's {@link AudioListenerInteraction} is set in
		 * {@link AudioClip#setAudioListenerInteraction(AudioListenerInteraction)}
		 * to {@link AudioListenerInteraction#Target}.
		 */
		Auto,

		/**
		 * The clip will play with the source calculating the distance to the
		 * target audio listener if it is the currently-enabled one. If the
		 * listener is null, the clip will not play.
		 */
		Target
	}

	AudioListenerInteraction audioListenerInteraction;

	/**
	 * Personal volume of the sound to play. May be affected by the system's
	 * volume and the AudioListener, if {@link #ignoreListener} is set to false.
	 */
	private float volume;

	/** Whether the clip is paused or not */
	private boolean paused;

	/**
	 * Whether the clip supports completion events. This can be achieved
	 * manually by having access to the clip's length and saving timestamp to
	 * play, pause , resume and stop, or automatically such as
	 * {@link Music#setOnCompletionListener(com.badlogic.gdx.audio.Music.OnCompletionListener)}
	 * . {@link Sound} instances for example do not support this, thus
	 * {@link SoundClip} has this value set to false.
	 */
	protected boolean supportsCompletionEvents;

	/** The AudioClip's category. Volume changes based on it. */
	private AudioChannel channel;

	/** Whether the audioclip is muted or not */
	private boolean muted;

	/** Whether the audioclip has been unmuted */
	private boolean mustUnmute;

	/** A custom audio channel. Will be used */
	private String customAudioChannel;

	/** Audio listener tied to the Audio source. */
	AudioListener audioListener;

	/**
	 * Parent audiosource. Set when calling
	 * {@link AudioSource#addClip(AudioClip)}.
	 */
	AudioSource audioSource;

	/** path to the audio clip */
	private String filepath;

	public boolean timescaleBasedPitch;

	/**
	 * Rate at which volume, pan and pitch transition to the target values every
	 * updated, in seconds. A value of 1 signifies that a value will change( if
	 * possible) by 1 every second. A value of 0.5 means that a value will
	 * change if possible by 0.5 every second.
	 * <p>
	 * The value is set in the constructor to -1, which is a special value,
	 * meaning that changes are instantaneous
	 */
	public float transitionRate;

	/**
	 * Volume that the clip should have. To be smoothed to in
	 * {@link #onUpdate()}.
	 */
	private float targetVolume;

	/**
	 * Pan that the clip should have. To be smoothed to in {@link #onUpdate()}.
	 */
	private float targetPan;

	/**
	 * Pitch that the clip should have (handled if {@link #timescaleBasedPitch}
	 * is set to true). To be smoothed to in {@link #onUpdate()}.
	 */
	private float targetPitch;

	private float currentVolume, currentPan, currentPitch;

	/**
	 * Creates an Audio Clip instance which can be used to play music. The audio
	 * clip can be interacted with directly using the {@link AudioClip} public
	 * methods, such as {@link AudioClip#play()}, as well as via using
	 * {@link AudioClipTask} by calling one of the <code>addAudioClipTask</code>
	 * found in {@link AudioSource}. The supported actions are
	 * {@link AudioTask#Pause}, {@link AudioTask#Play}, {@link AudioTask#Pause},
	 * {@link AudioTask#Resume} and {@link AudioTask#Restart}.
	 * <p>
	 * The {@link AudioClip}'s methods must be used for interacting with the
	 * clip only when a null <code>source</code> AudioSource parameter is
	 * provided, as the task handling is done is done only from the AudioSource
	 * component.
	 * 
	 * @param filepath
	 *            path to the file
	 * @param channel
	 *            Audio channel to get sound settings from.
	 * @param source
	 *            source to add the clip to. It can be null if you do not plan
	 *            on using sound panning
	 */
	public AudioClip(String filepath, AudioChannel channel, AudioSource source) {
		audioListenerInteraction = AudioListenerInteraction.None;
		this.channel = channel;
		currentVolume = 1;
		currentPan = 1;
		currentPitch = 1;
		volume = 1;
		transitionRate = -1;
		source.audioClips.add(this);
		updateInternalData();
	}

	/**
	 * Creates an Audio Clip instance which can be used to play music. The audio
	 * clip can be interacted with directly using the {@link AudioClip} public
	 * methods, such as {@link AudioClip#play()}, as well as via using
	 * {@link AudioClipTask} by calling one of the <code>addAudioClipTask</code>
	 * found in {@link AudioSource}. The supported actions are
	 * {@link AudioTask#Pause}, {@link AudioTask#Play}, {@link AudioTask#Pause},
	 * {@link AudioTask#Resume} and {@link AudioTask#Restart}.
	 * <p>
	 * The {@link AudioClip}'s methods must be used for interacting with the
	 * clip only when a null <code>source</code> AudioSource parameter is
	 * provided, as the task handling is done is done only from the AudioSource
	 * component.
	 * 
	 * @param filepath
	 *            path to the file
	 * @param customAudioChannelName
	 *            name of a custom audio channel, for setting different values
	 * @param source
	 *            source to add the clip to. It can be null if you do not plan
	 *            on using sound panning
	 */
	public AudioClip(String filepath, String customAudioChannelName, AudioSource source) {
		this.customAudioChannel = customAudioChannelName;
		audioListenerInteraction = AudioListenerInteraction.None;
		currentVolume = 1;
		currentPan = 1;
		currentPitch = 1;
		transitionRate = -1;
		volume = 1;
		source.audioClips.add(this);
		updateInternalData();
	}

	/**
	 * AudioClip extension implementation for playing a sound. Will be wrapped
	 * around code in {@link #play()} to check if the request makes sense from
	 * the point of view of the flow of the sound, such as sound is playing, is
	 * paused, and so on.
	 */
	protected abstract void onPlay();

	/**
	 * AudioClip extension implementation for stopping a sound. Will be wrapped
	 * around code in {@link #stop()} to check if the request makes sense from
	 * the point of view of the flow of the sound, such as sound is playing, is
	 * paused, and so on.
	 */
	protected abstract void onStop();

	/**
	 * AudioClip extension implementation for pausing a sound. Will be wrapped
	 * around code in {@link #pause()} to check if the request makes sense from
	 * the point of view of the flow of the sound, such as sound is playing, is
	 * paused, and so on.
	 */
	protected abstract void onPause();

	/**
	 * AudioClip extension implementation for playing a sound. Will be wrapped
	 * around code in {@link #resume()} to check if the request makes sense from
	 * the point of view of the flow of the sound, such as sound is playing, is
	 * paused, and so on.
	 */
	protected abstract void onResume();

	/**
	 * Might get called if {@link AudioSource#removeAudioClip(String, boolean)}
	 * boolean parameter is set to true, as well as if
	 * {@link #setFilepath(String, boolean)} boolean parameter is set to true
	 */
	protected abstract void onUnload();

	/** Whether or not the clip is finished playing */
	protected abstract boolean isCompleted();

	/**
	 * Called when the file path is changes. Is called whenever
	 * {@link #setFilepath(String, boolean)} is called if the filepath parameter
	 * is not null or equal to the previous value.
	 */
	protected abstract void onFilePathChange(String filepath);

	/**
	 * Called whenever the volume of the audio clip changes. The handling of the
	 * new volume must be done in this method ( if applicable to the clip type,
	 * as Music objects do not support pitch shifts for example)
	 * <p>
	 * 
	 * @param volume
	 *            volume value, between 0 and 1.
	 * @param pan
	 *            value, present for convenience if necessary.
	 * @param pitch
	 *            pitch value, present for convenience if necessary.
	 */
	protected abstract void onInternalVolumeChange(float volume, float pan, float pitch);

	/**
	 * Called whenever the pan of the audio clip changes. The handling of the
	 * new volume must be done in this method ( if applicable to the clip type,
	 * as Music objects do not support pitch shifts for example)
	 * 
	 * @param volume
	 *            volume value, present for convenience if necessary.
	 * @param pan
	 *            value, between -1(left) and 1 (right)
	 * @param pitch
	 *            pitch value, present for convenience if necessary.
	 */
	protected abstract void onInternalPanChange(float volume, float pan, float pitch);

	/**
	 * Called whenever the pan of the audio clip changes. The handling of the
	 * new volume must be done in this method ( if applicable to the clip type,
	 * as Music objects do not support pitch shifts for example)
	 * 
	 * @param volume
	 *            volume value, present for convenience if necessary.
	 * @param pan
	 *            value, present for convenience if necessary.
	 * @param pitch
	 *            pitch value, between 0.5 ( half speed) and 2( 2x speed)
	 */
	protected abstract void onInternalPitchChange(float volume, float pan, float pitch);

	/**
	 * Called every update from the AudioSource tied to the object. Before this
	 * value gets called, the volume, panning and pitch shift to their proper
	 * values.
	 * <p>
	 * If additional handling needs to be done every update in classes that
	 * extend AudioClip, it can be done here, however the core functionality
	 * will be called regardless of this being overriden
	 */
	protected void onUpdate() {

	}

	/**
	 * Returns the base volume for the clip implementation ( for example
	 * environmental, music, sound effects, narration, etc.). Values are to be
	 * between 0 (mute) and 1( full volume).
	 * 
	 * @return the specific base volume for the current type of clip. Will be
	 *         used along with the object-specific volume set using
	 *         {@link #setInstanceVolume(float)} and the distance to the
	 *         listener if applicable.
	 */
	public final float getBaseVolume() {
		if (channel != null)
			return Lumm.audio.getVolume(channel);
		else
			return Lumm.audio.getVolume(customAudioChannel);
	}

	/**
	 * Sets the audio listener for the source. The listener will not be handled
	 * if {@link #ignoreListener} is set to false.
	 * <p>
	 * Having a listener enables panning the sound based on the listener. The
	 * volume will also be scaled based on the distance between the listener and
	 * the object. A source can only have one source.
	 * 
	 * @param listener
	 *            The audio listener. If null is passed, the source will roll
	 *            back to ignoring all audio listeners
	 */
	public final void setAudioListener(AudioListener listener) {

		// only handle changing the listener
		if (this.audioListener != listener) {

			// remove the audio clip from the listener
			if (this.audioListenerInteraction == AudioListenerInteraction.Target)
				this.audioListener.removeAudioClip(this);

			this.audioListener = listener;

			// add audio clip to new listener
			if (this.audioListenerInteraction == AudioListenerInteraction.Target && this.audioListener != null)
				this.audioListener.addAudioClip(this);
		}
	}

	/**
	 * Returns the audio listener tied to the source. The listener may not be
	 * null and still not handled if {@link #ignoreListener} is set to true
	 * 
	 * @return the listener tied to the clip. Set in
	 *         {@link #setAudioListener(AudioListener)}
	 */
	public final AudioListener getAudioListener() {
		return audioListener;
	}

	public final AudioListenerInteraction getAudioListenerInteraction() {
		return audioListenerInteraction;
	}

	public final void setAudioListenerInteraction(AudioListenerInteraction audioListenerInteraction) {

		if (this.audioListenerInteraction != audioListenerInteraction) {

			if (this.audioListener != null) {

				// remove the clip from the previous listener that it was tied
				// to
				if (this.audioListenerInteraction == AudioListenerInteraction.Target)
					this.audioListener.removeAudioClip(this);

				// remove from list of automatically-bound clips
				else if (this.audioListenerInteraction == AudioListenerInteraction.Auto)
					AudioListener.automaticAudioclips.removeValue(this, true);

			}

			this.audioListenerInteraction = audioListenerInteraction;

			if (this.audioListener != null) {

				// add the clip to the currently-set clip that it was tied to
				if (this.audioListenerInteraction == AudioListenerInteraction.Target)
					this.audioListener.addAudioClip(this);

				// add the list to the automatically-set clips
				else if (this.audioListenerInteraction == AudioListenerInteraction.Auto)
					AudioListener.addAutomaticClip(this);
			}
		}
	}

	/**
	 * Sets the audio category tied to the audio clip instance. May affect
	 * volume based on the preferences of the user
	 * 
	 * @param category
	 */
	public final void setAudioChannel(AudioChannel category) {
		this.channel = category;
		updateInternalData();
	}

	/**
	 * Returns the sound category tied to the audio clip instance.
	 * 
	 * @return
	 */
	public final AudioChannel getAudioChannel() {
		return channel;
	}

	/**
	 * Sets the custom audio channel bound to the audio clip. If the channel has
	 * not been made using {@link Audio#setVolume(String, float)}, when
	 * requesting the volume from it, the entry will be created and saved so it
	 * loads on startup every time the app is opened. This method sets the
	 * current {@link #getAudioChannel()} value to null, using the volume value
	 * of the custom channel instead of one of the premade audio channels( if
	 * previously set).
	 * 
	 * @param customAudioChannel
	 */
	public final void setCustomAudioChannel(String customAudioChannel) {
		this.channel = null;
		this.customAudioChannel = customAudioChannel;
		updateInternalData();

	}

	/**
	 * Returns the custom audio channel bound to the audio clip. The custom
	 * audio channel is set when creating the object via
	 * {@link AudioClip#AudioClip(String, String, AudioSource)}, as well as when
	 * calling {@link #setCustomAudioChannel(String)}.
	 * 
	 * @return the custom audio channel, or null if it has not been set
	 */
	public final String getCustomAudioChannel() {
		return customAudioChannel;
	}

	/**
	 * Sets the volume of the individual sound instance.
	 * 
	 * @param volume
	 *            value between 0 (mute) and 1 (full volume)
	 */
	public final void setInstanceVolume(float volume) {
		this.volume = volume;
		updateInternalData();
	}

	/**
	 * Returns the volume of the individual custom sound instance, before
	 * applying base volume and listener offsets( if applicable)
	 * 
	 * @return instance-specific volume as the user set it
	 */
	public final float getInstanceVolume() {
		return volume;
	}

	/**
	 * Sets the file path to the audio clip, loading the new file on demand if
	 * necessary. The current sound is stopped using {@link #stop()} and
	 * {@link #onUnload()} is called if <code>unloadPreviousFile</code>
	 * parameter is set to true
	 * 
	 * @param filepath
	 */
	public final void setFilepath(String filepath, boolean unloadPreviousFile) {
		if (filepath == null) {
			Lumm.debug.logError("Passed null filepath parameter to audio clip in setFilePath(String, boolean)", null);
			return;
		}

		if (filepath.equals(getFilepath())) {
			Lumm.debug.logDebug("Calling AudioClip.setFilepath with a filepath parameter equal to old one, ignored.",
					null);
			return;
		}
		stop();
		if (unloadPreviousFile)
			onUnload();
		this.filepath = filepath;
		onFilePathChange(filepath);
	}

	/**
	 * Sets a forced pan on the audio clip. This method will call
	 * {@link #onInternalPanChange(float)}, however to ensure that the pan is
	 * consistent, {@link #ignoreListener} should be set to true, as changes in
	 * the position of either the source or the listener will update the pan.
	 * 
	 * @param pan
	 */
	public final void setForcedPan(float pan) {
		targetPan = pan;
		onInternalPanChange(currentVolume, pan, currentPitch);
	}

	public final String getFilepath() {
		return filepath;
	}

	/**
	 * Plays the clip. If the clip is currently playing and this is called
	 * nothing will happen. To play the sound from the start use
	 * {@link #onStop()} first and then call {@link #onPlay()} again. Resuming
	 * is done in {@link #onResume()}, and this method will not handle paused
	 * clips.
	 */
	public final void play() {
		if (paused)
			return;
		onPlay();
	}

	/**
	 * Stops the clip. If the clip is paused, it will be stopped so playing the
	 * game again will be from the start of the sound. Use {@link #pause} for
	 * pausing.
	 */
	public final void stop() {
		paused = false;
		onStop();
	}

	/**
	 * Pauses the clip, if the clip is playing. If the The clip can be resumed
	 * only from {@link #resume()}.
	 */
	public final void pause() {
		if (paused)
			return;
		paused = true;
		onPause();
	}

	/** Resumes the clip, if the clip has been previously paused. */
	public final void resume() {
		if (!paused)
			return;
		paused = false;
		onResume();
	}

	/** Restarts the clip, by stopping it and playing it again */
	public final void restart() {
		stop();
		play();
	}

	/**
	 * Mutes the audio clip, storing the volume the sound previously had for
	 * unmuting.
	 */
	public final void mute() {
		muted = true;
	}

	/**
	 * Unmutes the audio clip, setting the clip's audio based on the value it
	 * has when {@link #mute()} was called. The volume is still passed through
	 * the all of the volume multiplyers
	 */
	public final void unmute() {
		muted = false;
	}

	@SuppressWarnings("incomplete-switch")
	final void updateInternalData() {

		// master volume
		float volume = Lumm.audio.getVolume(AudioChannel.Master);

		// designated category
		if (channel != null)
			volume *= Lumm.audio.getVolume(channel);
		else
			volume *= Lumm.audio.getVolume(customAudioChannel);

		// listener volume
		switch (audioListenerInteraction) {
		case Target:
			volume *= audioSource.getListenerBasedVolume(audioListener);
			break;
		case Auto:
			volume *= audioSource.getListenerBasedVolume(AudioListener.getActiveListener());
			break;
		}

		// individual object volume
		volume *= getInstanceVolume();

		targetVolume = volume;

		float pan = 0;
		switch (audioListenerInteraction) {
		case Target:
			pan = audioSource.getListenerBasedPan(audioListener);
			break;
		case Auto:
			pan = audioSource.getListenerBasedPan(AudioListener.getActiveListener());
			break;
		}
		targetPan = pan;

		if (timescaleBasedPitch)
			targetPitch = (float) Math.min(2, Math.max(-0.5, Lumm.time.getTimeScale()));
		else
			targetPitch = 1;

	}

	/**
	 * Called every update from the AudioSource tied to the object. Smoothing of
	 * volume and panning should be handled here, using {@link #targetPan} and
	 * {@link #targetVolume}.
	 */
	protected void onUpdateInternal() {
		float prevPan = currentPan;
		if (transitionRate == -1)
			currentPan = targetPan;
		else
			currentPan = Utility.moveTowards(currentPan, targetPan, this.transitionRate * Lumm.time.getRealDeltaTime(),
					true);
		if (prevPan != currentPan)
			onInternalPanChange(currentVolume, currentPan, currentPitch);

		float prevVolume = currentVolume;
		if (transitionRate == -1)
			currentVolume = targetVolume;
		else
			currentVolume = Utility.moveTowards(currentVolume, targetVolume,
					this.transitionRate * Lumm.time.getRealDeltaTime(), true);

		if (muted && !mustUnmute) {
			mustUnmute = true;
			onInternalVolumeChange(0, currentPan, currentPitch);
		} else if (prevVolume != currentVolume && !muted) {

			// just unmuted
			if (mustUnmute) {
				mustUnmute = false;
				onInternalVolumeChange(currentVolume, currentPan, currentPitch);

			} else if (prevVolume != currentVolume) {
				onInternalVolumeChange(currentVolume, currentPan, currentPitch);

			}

		}

		float prevPitch = currentPitch;
		if (transitionRate == -1)
			currentPitch = targetPitch;
		else
			currentPitch = Utility.moveTowards(currentPitch, targetPitch,
					this.transitionRate * Lumm.time.getRealDeltaTime(), true);
		if (prevPitch != currentPitch)
			onInternalPitchChange(currentVolume, currentPan, currentPitch);

		onUpdate();
	}

}
