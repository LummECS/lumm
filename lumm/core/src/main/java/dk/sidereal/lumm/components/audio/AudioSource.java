package dk.sidereal.lumm.components.audio;

import java.util.Arrays;

import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import dk.sidereal.lumm.architecture.Lumm;
import dk.sidereal.lumm.architecture.LummComponent;
import dk.sidereal.lumm.architecture.LummObject;
import dk.sidereal.lumm.architecture.concrete.ConcreteLummComponent;
import dk.sidereal.lumm.architecture.core.Time;
import dk.sidereal.lumm.architecture.core.Audio;
import dk.sidereal.lumm.architecture.listeners.OnDisposeListener;
import dk.sidereal.lumm.components.audio.AudioClipTask.AudioTask;

/**
 * Component for handling playing audio. It can be done by creating
 * {@link AudioClip} objects and passing the audiosource to the
 * {@link AudioClip#AudioClip(String, Audio.AudioChannel, AudioSource)}
 * or {@link AudioClip#AudioClip(String, String, AudioSource)} constructors.
 * <p>
 * Functionality of <code>AudioClip</code> objects can be achieved in two
 * different ways:
 * <p>
 * By calling the AudioSource's {@link AudioClip#play()},
 * {@link AudioClip#stop()}, {@link AudioClip#pause()},
 * {@link AudioClip#resume()} or {@link AudioClip#restart()}.
 * <p>
 * It can also be achieved by creating an {@link AudioClipTask} using any of the
 * <code>addAudioClipTask</code> AudioSource methods. They support adding events
 * on completion ( if the AudioClip implementation supports it ), as well as a
 * delay which can be triggered in real or game time.
 *
 * @param obj The object to pass the component to.
 */
public class AudioSource extends ConcreteLummComponent {

    final Array<AudioClip> audioClips;

    final Array<AudioClipTask> audioClipTasks;

    private Vector3 prevPosition;

    /**
     * Creates an instance of <code>AudioSource</code>. Sounds must be added to
     * the source in order for the source to be effective, as it is used in
     * volume and panning calculations.
     */
    public AudioSource(LummObject obj) {
        super(obj);
        audioClips = new Array<AudioClip>();
        audioClipTasks = new Array<AudioClipTask>();
        prevPosition = new Vector3(object.position.get());

        onDisposeListener = new OnDisposeListener<LummComponent>() {

            @Override
            public void onDispose(LummComponent caller) {

                for (int i = 0; i < audioClips.size; i++) {
                    if (audioClips.get(i).audioListener != null)
                        audioClips.get(i).audioListener.removeAudioClip(audioClips.get(i));
                }
            }
        };
    }

    @Override
    public void onUpdate() {

        if (prevPosition.x != object.position.getX() || prevPosition.y != object.position.getY()
                || prevPosition.z != object.position.getZ()) {

            prevPosition.set(object.position.get());

        }

        for (int i = 0; i < audioClips.size; i++) {
            audioClips.get(i).updateInternalData();
            audioClips.get(i).onUpdateInternal();

        }

        for (int i = 0; i < audioClipTasks.size; i++) {
            audioClipTasks.get(i).onUpdate();
            if (audioClipTasks.get(i).isDone())
                audioClipTasks.removeIndex(i--);
        }

    }

    /**
     * Internal method for getting the volume in relation to a listener. Is used
     * in {@link AudioClip#updateInternalData()}.
     *
     * @param listener {@link AudioListener} instance tied to the {@link AudioClip}
     *                 calling this method. A null parameter value is accepted,
     *                 although a volume of 1 will be returned.
     * @return volume, between 0 (mute) and 1 (full volume)
     */
    float getListenerBasedVolume(AudioListener listener) {
        if (listener == null || !listener.isActive())
            return 0;

        float distanceToListener = object.position.get().dst(listener.object.position.get());

        if (distanceToListener < listener.getMaxVolumeRadius())
            return 1;

        if (distanceToListener < listener.getVolumeRadius()) {
            float volume = 1 - (distanceToListener - listener.getMaxVolumeRadius())
                    / (listener.getVolumeRadius() - listener.getMaxVolumeRadius());
            volume = Math.min(Math.max(0, volume), 1);
            return volume;
        }

        return 0;
    }

    /**
     * Internal method for getting the pan in relation to a listener. Is used in
     * {@link AudioClip#updateInternalData()}.
     *
     * @param listener {@link AudioListener} instance tied to the {@link AudioClip}
     *                 calling this method. A null parameter value is accepted,
     *                 although a pan of 0 will be returned in this conditions.
     * @return pan, between -1 (full left) and 1 (full right)
     */
    float getListenerBasedPan(AudioListener listener) {
        if (listener == null || !listener.isActive())
            return 0;
        float pan = 0;

        float xDiff = listener.object.position.getX() - object.position.getX();
        float yDiff = listener.object.position.getY() - object.position.getY();

        switch (listener.getPan()) {
            case X_Axis:
                if (Math.abs(xDiff) > listener.getVolumeRadius())
                    if (xDiff > 0)
                        return 1;
                    else
                        return -1;
                else if (Math.abs(xDiff) < listener.getMaxVolumeRadius())
                    return 0;

                return (xDiff - listener.getMaxVolumeRadius())
                        / (listener.getVolumeRadius() - listener.getMaxVolumeRadius());

            case X_Axis_Reversed:
                if (Math.abs(xDiff) > listener.getVolumeRadius())
                    if (xDiff > 0)
                        return -1;
                    else
                        return 1;
                else if (Math.abs(xDiff) < listener.getMaxVolumeRadius())
                    return 0;

                return -(xDiff - listener.getMaxVolumeRadius())
                        / (listener.getVolumeRadius() - listener.getMaxVolumeRadius());

            case Y_Axis:

                if (Math.abs(yDiff) > listener.getVolumeRadius())
                    if (xDiff > 0)
                        return -1;
                    else
                        return 1;
                else if (Math.abs(yDiff) < listener.getMaxVolumeRadius())
                    return 0;

                return -(yDiff - listener.getMaxVolumeRadius())
                        / (listener.getVolumeRadius() - listener.getMaxVolumeRadius());

            case Y_Axis_Reversed:

                if (Math.abs(yDiff) > listener.getVolumeRadius())
                    if (xDiff > 0)
                        return 1;
                    else
                        return -1;
                else if (Math.abs(yDiff) < listener.getMaxVolumeRadius())
                    return 0;

                return (yDiff - listener.getMaxVolumeRadius())
                        / (listener.getVolumeRadius() - listener.getMaxVolumeRadius());

        }

        return pan;
    }

    /**
     * Returns the first {@link AudioClip} instance for the filepath parameter.
     * The parameter is supposed to be the same as the filepath parameter passed
     * in the
     * {@link AudioClip#AudioClip(String, Audio.AudioChannel, AudioSource)}
     * AudioClip constructor.
     * <p>
     * To potentially find all clips with the same filepath,
     * {@link #getAudioClips()} can be called.
     *
     * @param filepath
     * @return
     */
    public AudioClip getAudioClip(String filepath) {

        for (int i = 0; i < audioClips.size; i++)
            if (audioClips.get(i).getFilepath().equals(filepath))
                return audioClips.get(i);

        return null;
    }

    /**
     * Removes the first {@link AudioClip} instance with a filepath equal to the
     * filepath parameter. For removing a particular clip, the filepath
     * parameter should be the same one as the one used in the {@link AudioClip}
     * extension constructor, or the paramater used in
     * {@link AudioClip#setFilepath(String, boolean)} if it has been called
     * after the clip has been initialised.
     *
     * @param filepath path to the file
     * @param unload   whether to unload to asset or not
     * @return whether the clip has been removed or not
     */
    public boolean removeAudioClip(String filepath, boolean unload) {
        for (int i = 0; i < audioClips.size; i++)
            if (audioClips.get(i).getFilepath().equals(filepath)) {
                if (audioClips.get(i).audioListener != null)
                    audioClips.get(i).audioListener.removeAudioClip(audioClips.get(i));
                if (unload)
                    audioClips.get(i).onUnload();
                audioClips.removeIndex(i);
                return true;
            }
        return false;
    }

    /**
     * Returns a copy of the audio clips internally used in the
     * {@link AudioSource}. Modifying this array has no impact on the underlying
     * list of clips, although modifying the clips in the array will.
     *
     * @return An array representing a copy of list of AudioClips used in
     * {@link AudioSource}.
     */
    public AudioClip[] getAudioClips() {
        return Arrays.copyOf(audioClips.items, audioClips.size);
    }

    /**
     * Adds an {@link AudioClipTask} to the {@link AudioSource} that the
     * {@link AudioClip} has. This can be used for delaying events. This method
     * will use real time for delaying as opposed to the game time. For using
     * game time, call
     * {@link #addAudioClipTask(String, AudioTask, float, boolean)} or
     * {@link #addAudioClipTask(String, AudioTask, float, boolean, Runnable)}.
     * <p>
     * When a task is done, it will be removed from the internal list of audio
     * clip tasks. The {@link AudioClip} used in the task will not be removed.
     *
     * @param clipPath  The path to the clip you want to add a task for
     * @param audioTask The type of task you want the system to perform
     * @param delay     The delay in seconds from calling this method to running the
     *                  task
     */
    public void addAudioClipTask(String clipPath, AudioTask audioTask, float delay) {
        addAudioClipTask(clipPath, audioTask, delay, true);
    }

    /**
     * Adds an {@link AudioClipTask} to the {@link AudioSource} that the
     * {@link AudioClip} has. This can be used for delaying events. This method
     * will use real time for delaying as opposed to the game time. For using
     * game time, call
     * {@link #addAudioClipTask(AudioClip, AudioTask, float, boolean)} or
     * {@link #addAudioClipTask(AudioClip, AudioTask, float, boolean, Runnable)}
     * .
     * <p>
     * When a task is done, it will be removed from the internal list of audio
     * clip tasks. The {@link AudioClip} used in the task will not be removed.
     *
     * @param clip      The clip tied to the task you want to perform
     * @param audioTask The type of task you want the system to perform
     * @param delay     The delay in seconds from calling this method to running the
     *                  task
     */
    public void addAudioClipTask(AudioClip clip, AudioTask audioTask, float delay) {
        addAudioClipTask(clip, audioTask, delay, true);
    }

    /**
     * Adds an {@link AudioClipTask} to the {@link AudioSource} that the
     * {@link AudioClip} has. This can be used for delaying events.
     * <p>
     * When a task is done, it will be removed from the internal list of audio
     * clip tasks. The {@link AudioClip} used in the task will not be removed.
     *
     * @param clipPath    The path to the clip you want to add a task for
     * @param audioTask   The type of task you want the system to perform
     * @param delay       The delay in seconds from calling this method to running the
     *                    task
     * @param useRealTime Whether to use real time for delaying ( as opposed to the game
     *                    time, which is affected by {@link Time#getTimeScale()}.
     */
    public void addAudioClipTask(String clipPath, AudioTask audioTask, float delay, boolean useRealTime) {
        addAudioClipTask(clipPath, audioTask, delay, useRealTime, null);
    }

    /**
     * Adds an {@link AudioClipTask} to the {@link AudioSource} that the
     * {@link AudioClip} has. This can be used for delaying events.
     * <p>
     * When a task is done, it will be removed from the internal list of audio
     * clip tasks. The {@link AudioClip} used in the task will not be removed.
     *
     * @param clip        The clip tied to the task you want to perform
     * @param audioTask   The type of task you want the system to perform
     * @param delay       The delay in seconds from calling this method to running the
     *                    task
     * @param useRealTime Whether to use real time for delaying ( as opposed to the game
     *                    time, which is affected by {@link Time#getTimeScale()}.
     */
    public void addAudioClipTask(AudioClip clip, AudioTask audioTask, float delay, boolean useRealTime) {
        addAudioClipTask(clip, audioTask, delay, useRealTime, null);
    }

    /**
     * Adds an {@link AudioClipTask} to the {@link AudioSource} that the
     * {@link AudioClip} has. This can be used for delaying events and running
     * other events on completion, if the {@link AudioClip} implementation can
     * handle completion, found by querying
     * {@link AudioClip#supportsCompletionEvents}.
     * <p>
     * When a task is done, it will be removed from the internal list of audio
     * clip tasks. The {@link AudioClip} used in the task will not be removed
     *
     * @param clipPath          The path to the clip you want to add a task for
     * @param audioTask         The type of task you want the system to perform
     * @param delay             The delay in seconds from calling this method to running the
     *                          task
     * @param useRealTime       Whether to use real time for delaying ( as opposed to the game
     *                          time, which is affected by {@link Time#getTimeScale()}.
     * @param onCompletionEvent Event to run on completion. Some {@link AudioClip}
     *                          implementations can't handle it, so events will run on clips
     *                          that have {@link AudioClip#supportsCompletionEvents} set to
     *                          true. Can be null
     */
    public void addAudioClipTask(String clipPath, AudioTask audioTask, float delay, boolean useRealTime,
                                 Runnable onCompletionEvent) {
        AudioClip targetAudioClip = getAudioClip(clipPath);
        addAudioClipTask(targetAudioClip, audioTask, delay, useRealTime, onCompletionEvent);
    }

    /**
     * Adds an {@link AudioClipTask} to the {@link AudioSource} that the
     * {@link AudioClip} has. This can be used for delaying events and running
     * other events on completion, if the {@link AudioClip} implementation can
     * handle completion, found by querying
     * {@link AudioClip#supportsCompletionEvents}.
     * <p>
     * When a task is done, it will be removed from the internal list of audio
     * clip tasks. The {@link AudioClip} used in the task will not be removed
     *
     * @param clip              The clip tied to the task you want to perform
     * @param audioTask         The type of task you want the system to perform
     * @param delay             The delay in seconds from calling this method to running the
     *                          task
     * @param useRealTime       Whether to use real time for delaying ( as opposed to the game
     *                          time, which is affected by {@link Time#getTimeScale()}.
     * @param onCompletionEvent Event to run on completion. Some {@link AudioClip}
     *                          implementations can't handle it, so events will run on clips
     *                          that have {@link AudioClip#supportsCompletionEvents} set to
     *                          true. Can be null
     */
    public void addAudioClipTask(AudioClip clip, AudioTask audioTask, float delay, boolean useRealTime,
                                 Runnable onCompletionEvent) {
        if (clip == null) {
            Lumm.debug.logError("Calling addAudioTask with a null clip parameter", null);
            return;
        }
        @SuppressWarnings("unused")
        AudioClipTask task = new AudioClipTask(clip, audioTask, delay, useRealTime, onCompletionEvent);
    }

}
