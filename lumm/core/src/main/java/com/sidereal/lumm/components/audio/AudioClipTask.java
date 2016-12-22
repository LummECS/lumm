package com.sidereal.lumm.components.audio;

import com.sidereal.lumm.architecture.Lumm;

public class AudioClipTask {

    public static enum AudioTask {
        Play, Stop, Pause, Resume, Restart
    }

    private AudioClip clip;

    private AudioTask task;

    private float delay;

    private boolean useRealTime;

    private Runnable onCompletionEvent;

    private boolean handledTask;

    private boolean handledCompletion;

    public AudioClipTask(AudioClip clip, AudioTask task, float delay, boolean useRealTime, Runnable onCompletionEvent) {
        this.clip = clip;

        if (delay <= 0) {
            handleTask();
            handledTask = true;
        } else
            this.delay = delay;

        this.useRealTime = useRealTime;
        if (onCompletionEvent != null)
            this.onCompletionEvent = onCompletionEvent;

        if (!clip.supportsCompletionEvents && onCompletionEvent != null)
            Lumm.debug.logDebug("Attempting to make an AudioClipTask with a non-nul " + onCompletionEvent
                    + " while the audioClip used does not support it", null);

        handledTask = false;
        handledCompletion = false;

        if (!clip.supportsCompletionEvents || onCompletionEvent == null)
            handledCompletion = true;

        clip.audioSource.audioClipTasks.add(this);
    }

    void onUpdate() {

        if (!handledTask) {
            if (useRealTime)
                delay -= Lumm.time.getRealDeltaTime();
            else
                delay -= Lumm.time.getDeltaTime();

            if (delay < 0) {
                handleTask();
            }
            handledTask = true;
        }

        if (clip.isCompleted() && onCompletionEvent != null && clip.supportsCompletionEvents) {
            onCompletionEvent.run();
            handledCompletion = true;
        }

    }

    public void handleTask() {
        switch (task) {
            case Play:
                clip.play();
                break;
            case Stop:
                clip.stop();
                break;
            case Pause:
                clip.pause();
                break;
            case Resume:
                clip.resume();
                break;
            case Restart:
                clip.restart();
                break;
        }
    }

    boolean isDone() {
        return handledTask && handledCompletion && clip.isCompleted();
    }

}
