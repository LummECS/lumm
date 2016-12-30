/*******************************************************************************
 * Copyright 2014 See AUTHORS file.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package dk.sidereal.lumm.components.audio;

import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import dk.sidereal.lumm.architecture.Lumm;
import dk.sidereal.lumm.architecture.LummObject;
import dk.sidereal.lumm.architecture.concrete.ConcreteLummComponent;

/**
 * Handles listening to audio coming from {@link AudioPlayer} instances, panning
 * the audio based on the position between the listener and the player.
 *
 * @author Claudiu Bele
 */
public class AudioListener extends ConcreteLummComponent {

    // region fields

    public static enum PanType {
        /**
         * Elements to the right of the listener are played on the right,
         * elements to the left are played on the left
         */
        X_Axis,

        /**
         * Elements to the right of the listener are played on the left,
         * elements to the left are played on the right
         */
        X_Axis_Reversed,

        /**
         * Elements above the listener are played on the left, elements below
         * the listener are played on the right
         */
        Y_Axis,

        /**
         * Elements above the listener are played on the right, elements below
         * the listener are played on the left
         */
        Y_Axis_Reversed
    }

    public static Sprite debugSpriteSource;

    private PanType pan;

    private float volumeRadius;

    private float maxVolumeRadius;

    private final Array<AudioClip> audioClips;

    /** Whether the listener is active or not */
    private boolean isActive;

    private Vector3 prevPosition;

    /**
     * The active AudioListener. Only one will be available at any time. The
     * first listener that is created will be set as the active listener, and
     * the following listeners must call {@link #activate()}.
     */
    private static AudioListener activeListener;

    /**
     * Audio clips that have {@link AudioClip#audioListenerInteraction} set to
     * {@link AudioClip.AudioListenerInteraction#Auto}, the target volume, pitch and
     * panning being updated whenever the active listener is called
     */
    static Array<AudioClip> automaticAudioclips = new Array<AudioClip>();

    // endregion fields

    // region constructors

    /**
     * Creates an audio listener component with a designated radius.
     *
     * @param obj
     *            The object to add a the listener to
     * @param radius
     *            The radius of the listener, beyond which audio clips
     *            registered to this listener can't be heard anymore. Clips can
     *            register to a listener using
     *            {@link AudioClip#setAudioListenerInteraction(AudioClip.AudioListenerInteraction)}
     *            and {@link AudioClip#setAudioListener(AudioListener)}.
     */
    public AudioListener(LummObject obj, float radius) {

        this(obj, radius, 0);
    }

    /**
     * Creates an audio listener component with a designated radius.
     *
     * @param obj
     *            The object to add a the listener to
     * @param radius
     *            The radius of the listener, beyond which audio clips
     *            registered to this listener can't be heard anymore. Clips can
     *            register to a listener using
     *            {@link AudioClip#setAudioListenerInteraction(AudioClip.AudioListenerInteraction)}
     *            and {@link AudioClip#setAudioListener(AudioListener)}.
     * @param maxVolumeRadius
     *            The radius of the listener, within which clips registered to
     *            this listener have the listener-based volume multiplier set to
     *            1.
     */
    public AudioListener(LummObject obj, float volumeRadius, float maxVolumeRadius) {
        super(obj);

        setDebugToggleKeys(Keys.SHIFT_LEFT, Keys.Z);
        prevPosition = new Vector3(object.position.get());
        this.volumeRadius = volumeRadius;
        this.maxVolumeRadius = maxVolumeRadius;
        this.audioClips = new Array<AudioClip>();
        if (AudioListener.activeListener == null)
            AudioListener.activeListener = this;
        this.pan = PanType.X_Axis;
    }

    @Override
    protected void initialiseClass() {

        if (!Lumm.debug.isEnabled())
            return;
        if (debugSpriteSource == null) {
            debugSpriteSource = new Sprite(
                    Lumm.assets.get(Lumm.assets.frameworkAssetsFolder + "AudioListener.png", Texture.class));

        }
    }

    // endregion constructors

    // region methods

    @Override
    public void onDebug() {

        debugSpriteSource.setBounds(object.position.getX() - volumeRadius, object.position.getY() - volumeRadius,
                volumeRadius * 2, volumeRadius * 2);
        // debugSpriteSource.setBounds(0, 0, 100, 100);
        debugSpriteSource.draw(object.getSceneLayer().spriteBatch);
    }

    @Override
    public void onUpdate() {

        if (prevPosition.x != object.position.getX() || prevPosition.y != object.position.getY()
                || prevPosition.z != object.position.getZ()) {
            prevPosition.set(object.position.get());
            updateAudioClips();
        }

    }

    /**
     * Sets the volume radius beyond which an AudioSource's {@link AudioClip}s
     * that have the listener set to the instance that you are manipulating can
     * not be heard anymore. The overall system also contains functionality for
     * a radius within which sounds will be played at full volume.
     * <p>
     * From the end of the max volume radius (set via
     * {@link #setVolumeRadius(float)}) and this radius, volume will linearly
     * move from 100% to 0% listener-based volume, but that is a rough indicator
     * of the sound's volume, as it is also affected by the master volume,
     * channel volume and individual clip instance volume.
     *
     * @return the radius. Set in the constructor
     *         {@link #setVolumeRadius(float)}.
     */
    public float getVolumeRadius() {
        return volumeRadius;
    }

    /**
     * Sets the volume radius beyond which an AudioSource's {@link AudioClip}s
     * that have the listener set to the instance that you are manipulating can
     * not be heard anymore. The overall system also contains functionality for
     * a radius within which sounds will be played at full volume.
     * <p>
     * From the end of the max volume radius (set via
     * {@link #setVolumeRadius(float)}) and this radius, volume will linearly
     * move from 100% to 0% listener-based volume, but that is a rough indicator
     * of the sound's volume, as it is also affected by the master volume,
     * channel volume and individual clip instance volume.
     *
     * @param volumeRadius
     *            The circle of radius
     */
    public void setVolumeRadius(float volumeRadius) {
        boolean valueChanged = (this.volumeRadius != volumeRadius);
        this.volumeRadius = volumeRadius;
        if (valueChanged)
            updateAudioClips();
    }

    /**
     * Returns the maximum volume radius, which represents the area around the
     * listener around which any {@link AudioClip}'s listener-based volume
     * multiplier is set to a maximum of 1 ( indicating full volume before the
     * other multipliers are applied. For more information on volume radius and
     * how the max volume radius works towards the linearly-decreasing of the
     * overall volume of audioclips check {@link #setVolumeRadius(float)} and
     * {@link #getVolumeRadius()}.
     */
    public float getMaxVolumeRadius() {
        return maxVolumeRadius;
    }

    /**
     * Sets the maximum volume radius, which represents the area around the
     * listener around which any {@link AudioClip}'s listener-based volume
     * multiplier is set to a maximum of 1 ( indicating full volume before the
     * other multipliers are applied. For more information on volume radius and
     * how the max volume radius works towards the linearly-decreasing of the
     * overall volume of audioclips check {@link #setVolumeRadius(float)} and
     * {@link #getVolumeRadius()}.
     *
     * @param maxVolumeRadius
     *            The radius around which clips tied to this listener are played
     *            with a listener-based multiplyer of 1.
     */
    public void setMaxVolumeRadius(float maxVolumeRadius) {
        boolean valueChanged = (this.maxVolumeRadius != maxVolumeRadius);
        this.maxVolumeRadius = maxVolumeRadius;
        if (valueChanged)
            updateAudioClips();
    }

    /**
     * Returns the pan type that will be applied to clips that use a listener
     * and do not have a forced pan. By default it is set to
     * {@link PanType#X_Axis}.
     */
    public PanType getPan() {
        return pan;
    }

    /**
     * Sets the pan type that will be applied to clips that use a listener and
     * do not have a forced pan. By default it is set to {@link PanType#X_Axis}.
     */
    public void setPan(PanType pan) {
        this.pan = pan;
        updateAudioClips();
    }

    /**
     * Returns the currently active listener. There can either be 0 or 1
     * listeners active at any time. Sounds that are registered to listeners
     * will be played only if the listener they are tied to is active.
     * <p>
     * If an audio clip has the {@link AudioClip#getAudioListenerInteraction()}
     * set to {@link AudioClip.AudioListenerInteraction#Auto}, the clips will get their
     * volume and panning updated based on the active listener.
     * <p>
     * To activate the listener, call {@link #activate()}, which will deactivate
     * the previously-activated clip if there was any.
     *
     * @return The currently-active listener or null if no listener has been
     *         activated.
     */
    public static AudioListener getActiveListener() {
        return activeListener;
    }

    /**
     * Activates the listener and deactivates the previous active listener if
     * there is any, updating the target volume and pan for all clips that have
     * the listener set to any of the 2 listeners.
     * <p>
     * If an audio clip has the {@link AudioClip#getAudioListenerInteraction()}
     * set to {@link AudioClip.AudioListenerInteraction#Auto}, the clips will get their
     * volume and panning updated based on the active listener.
     **/
    public void activate() {
        if (activeListener != null) {
            activeListener.isActive = false;
            activeListener.updateAudioClips();
        }

        activeListener = this;
        this.isActive = true;
        this.updateAudioClips();
        AudioListener.updateAutomaticAudioClips();

    }

    /**
     * Returns whether the listener is the active listener.
     * <p>
     * For more information on active listeners, see
     * {@link AudioListener#getActiveListener()} and
     * {@link AudioListener#activate()}.
     */
    public boolean isActive() {
        return isActive;
    }

    void addAudioClip(AudioClip clip) {
        if (!audioClips.contains(clip, true)) {
            audioClips.add(clip);
            clip.updateInternalData();
        }
    }

    static void addAutomaticClip(AudioClip clip) {
        if (!automaticAudioclips.contains(clip, true)) {
            automaticAudioclips.add(clip);
            clip.updateInternalData();
        }
    }

    void removeAudioClip(AudioClip clip) {
        int index = audioClips.indexOf(clip, true);
        if (index != -1)
            audioClips.removeIndex(index);
    }

    static void removeAutomaticAudioClip(AudioClip clip) {
        int index = automaticAudioclips.indexOf(clip, true);
        if (index != -1)
            automaticAudioclips.removeIndex(index);
    }

    private void updateAudioClips() {
        for (int i = 0; i < audioClips.size; i++) {
            if (audioClips.get(i).audioListenerInteraction == AudioClip.AudioListenerInteraction.Target)
                audioClips.get(i).updateInternalData();
        }
    }

    static void updateAutomaticAudioClips() {
        for (int i = 0; i < automaticAudioclips.size; i++) {
            if (automaticAudioclips.get(i).audioListenerInteraction == AudioClip.AudioListenerInteraction.Auto)
                automaticAudioclips.get(i).updateInternalData();
        }
    }

    // endregion methods
}
