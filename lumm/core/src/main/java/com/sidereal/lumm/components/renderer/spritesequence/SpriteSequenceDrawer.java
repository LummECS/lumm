/*******************************************************************************
 * Copyright 2015 See AUTHORS file.
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

package com.sidereal.lumm.components.renderer.spritesequence;

import java.util.ArrayList;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.ObjectMap;
import com.sidereal.lumm.architecture.AbstractEvent;
import com.sidereal.lumm.architecture.Lumm;
import com.sidereal.lumm.components.renderer.Drawer;
import com.sidereal.lumm.components.renderer.Renderer;

/**
 * ImageConstruct used for rendering sprites in a sequence.
 * <p>
 * Gives the ability to change the sequence to play, as well as adding a
 * {@link SpriteSequencePreference} instance to an order/sequence of sprites,
 * customising size, position and animation duration.
 * <p>
 * The class calculates the time to allocate to individual images, changing the
 * images automatically.
 * <p>
 * Transparency, rotation and tint can also be applied to images, however they
 * persist when changing to different sequences.
 *
 * @see {@link SCMLDrawer} for real animations from Spriter files,
 *      {@link SpriteSequenceDrawer} is only used made to counter the use of
 *      Spriter but is more difficult to work with and more memory-expensive.
 * @see {@link SpriteSequenceBuilder} for easy building of a
 *      {@link SpriteSequenceDrawer} instance.
 *
 * @author Claudiu Bele
 */
public class SpriteSequenceDrawer extends Drawer {

    // region fields

    /** List of sprite Paths that we currently iterate over */
    public ArrayList<String> spritePaths;

    /**
     * Map containing sprite paths, retrievable by name. For example you can
     * have 3 lists of file paths, one for goin down, one for going up, one for
     * attacking.
     * <p>
     * You can get the one for going down and set the {@link #spritePaths} to
     * it.
     */
    public ObjectMap<String, ArrayList<String>> sequenceFilepaths;

    /**
     * Map containing configurations mapped to a sequence. Added in
     * {@link #addPreferences(String, SpriteSequencePreference)} only if the
     * name of the sequence we attach it to is found in
     * {@link #sequenceFilepaths}.
     */
    public ObjectMap<String, SpriteSequencePreference> sequencePreferences;

    /**
     * Currently-selected Sprite Sequence. Set using
     * {@link #setSpriteSequence(String)}.
     */
    private String currentSequence = "";

    /** Current sprite to render */
    private Sprite currSprite;

    /** Time elapsed since the last sprite change in the sequence */
    private float timeSinceSpriteChange;

    private Color spriteColor;

    // region animation events

    /**
     * Map of events with the key being the sprite order for which when the
     * index is 0 in the animation, we handle an event
     */
    private ObjectMap<String, AbstractEvent> eventsOnAnimationStart;

    /**
     * Map of events with the key being the sprite order for which when the
     * index is {@link #spritePaths}'s size -1 in the animation, we handle an
     * event
     */
    private ObjectMap<String, AbstractEvent> eventsOnAnimationEnd;

    // endregion

    /** Current index in the animation */
    public int index = 0;

    // endregion

    // region constructors

    public SpriteSequenceDrawer(Renderer renderer, String name, boolean useRealDeltaTime) {

        super(renderer, name, useRealDeltaTime);
        eventsOnAnimationEnd = new ObjectMap<String, AbstractEvent>();
        eventsOnAnimationStart = new ObjectMap<String, AbstractEvent>();
        sequencePreferences = new ObjectMap<String, SpriteSequencePreference>();
        sequencePreferences.put("Default", new SpriteSequencePreference());

        this.currentSequence = null;
        this.spriteColor = new Color(1, 1, 1, 1);
        this.spritePaths = new ArrayList<String>();
        this.sequenceFilepaths = new ObjectMap<String, ArrayList<String>>();
    }

    // endregion constructors

    // region methods

    @Override
    public void draw(float delta) {

        if (currentSequence == null) {
            throw new RuntimeException(
                    "You forgot to set the sprite order for the renderer.object " + renderer.object.getName()
                            + " of type " + renderer.object.getType() + " with the sequence name " + name);
        }

        // region index in animation

        // increase time since current sprite by the time since last frame
        timeSinceSpriteChange += delta;

        // increase sprite index if the time since current sprite is more than
        // the expected time per sprite
        // achieved by dividing the overall animation loop time by the number of
        // sprites
        index += (int) (timeSinceSpriteChange / (getCurrPreferences().getTimePerAnimation() / spritePaths.size()));

        // if the time since current sprite is bigger than expected, we use
        // modulus to bring it to a value lower
        // than the expected animation time per second.
        timeSinceSpriteChange = timeSinceSpriteChange
                % (getCurrPreferences().getTimePerAnimation() / spritePaths.size());

        // detects if we reached the end of the animation and the new one starts
        if (index % spritePaths.size() < index) {
            if (this.eventsOnAnimationStart.containsKey(currentSequence)) {
                this.eventsOnAnimationStart.get(currentSequence).run();
            }
        }

        // value at following index will be the end of the animation, run end
        // event.
        if ((index + 1) % spritePaths.size() < index + 1) {
            if (this.eventsOnAnimationEnd.containsKey(currentSequence)) {
                this.eventsOnAnimationEnd.get(currentSequence).run();
            }
        }

        index = index % spritePaths.size();
        // endregion

        Color tint = getCurrPreferences().getTintColor();
        float transparency = getCurrPreferences().getTransparency();
        // image is not visible due to a value of 0 on the color alpha channel,
        // so don't render.
        if (tint.a * transparency == 0)
            return;
        if (tint != null) {
            if (tint.r != spriteColor.r || tint.g != spriteColor.g || tint.b != spriteColor.b
                    || tint.r * transparency != spriteColor.a) {
                spriteColor.set(tint.r, tint.g, tint.b, tint.a * transparency);
            }
        }

        // image is outside of the bounds of the camera wanting to draw it
        if (isOutOfBounds())
            return;

        // checks if the index changed and we have to change the image
        if (!currSprite.getTexture().equals(Lumm.assets.get(spritePaths.get(index), Texture.class))) {
            // get the Image from the Static sprite list
            currSprite.setTexture(Lumm.assets.get(spritePaths.get(index), Texture.class));
        }

        // setting tint color after setting the new texture.
        currSprite.setColor(spriteColor);

        // adjust position based on the renderer.object's position
        float targetPosX = renderer.object.position.getX() + getCurrPreferences().getPositionOffset().x;
        float targetPosY = renderer.object.position.getY() + getCurrPreferences().getPositionOffset().y;
        if (currSprite.getX() != targetPosX || currSprite.getY() != targetPosY) {
            currSprite.setPosition(targetPosX, targetPosY);
        }

        // setting the size if neccesary
        if (currSprite.getWidth() != getCurrPreferences().getSize().x
                || currSprite.getHeight() != getCurrPreferences().getSize().y)
            currSprite.setSize(getCurrPreferences().getSize().x, getCurrPreferences().getSize().y);

        // flips the images
        boolean flipX = getPreferences(currentSequence).isFlipX() != currSprite.isFlipX();
        boolean flipY = getPreferences(currentSequence).isFlipY() != currSprite.isFlipY();
        if (flipX || flipY) {
            currSprite.flip(flipX, flipY);
        }

        // draw image
        currSprite.draw(renderer.object.getSceneLayer().spriteBatch);

    }

    // region utility

    // region events on animation start/end
    public final void setEventOnAnimationStart(String sequenceName, AbstractEvent event) {

        eventsOnAnimationStart.put(sequenceName, event);
    }

    public final void removeEventOnAnimationStart(String sequenceName, AbstractEvent event) {

        if (eventsOnAnimationStart.containsKey(sequenceName)) {
            eventsOnAnimationStart.remove(sequenceName);
        }
    }

    public final void setEventOnAnimationEnd(String sequenceName, AbstractEvent event) {

        eventsOnAnimationEnd.put(sequenceName, event);
    }

    public final void removeEventOnAnimationEnd(String sequenceName, AbstractEvent event) {

        if (eventsOnAnimationEnd.containsKey(sequenceName)) {
            eventsOnAnimationEnd.remove(sequenceName);
        }
    }

    // endregion

    // region sprite sequences

    /**
     * Sets the current sprite sequence to the value of passed parameter as a
     * key from {@link #sequenceFilepaths}.
     * <p>
     * If a Sequence with the passed parameter as a name is not found, an
     * exception will be returned.
     * <p>
     * If the sequence we want to change to is already the one that is set, the
     * change is not handled
     *
     * @param sequenceName
     *            new Sprite Sequence name
     */
    public final void setSpriteSequence(String sequenceName) {

        if (!sequenceFilepaths.containsKey(sequenceName))
            throw new GdxRuntimeException("Sprite Sequence " + sequenceName + " for renderer of object "
                    + renderer.object.getName() + " was not found in Sprite Sequence " + name);

        if (currentSequence == sequenceName && this.spritePaths.equals(this.sequenceFilepaths.get(sequenceName)))
            return;

        this.currentSequence = sequenceName;
        this.index = 0;
        this.spritePaths = this.sequenceFilepaths.get(sequenceName);
        this.currSprite = new Sprite(Lumm.assets.get(spritePaths.get(0), Texture.class));
        this.timeSinceSpriteChange = 0;

        if (sequencePreferences.containsKey("name")) {
            if (sequencePreferences.get(sequenceName).isPlaceAtEnd())
                renderer.placeAtEnd(this.name);

            if (sequencePreferences.get(sequenceName).isPlaceAtStart())
                renderer.placeAtStart(this.name);
        }
    }

    /**
     * Adds a new sequence of sprite with the first parameter as the name of the
     * sequence. the file paths have to all contain the the root folder relative
     * to the project, file name and extensions
     * <p>
     * If no sprite sequence is set to run, the sequence we add will be the one
     * we will run.
     *
     * @see {@link #addSpriteSequence(String, String, String[], String)} for a
     *      simpler way.
     * @param sequenceName
     *            name of the new sequence
     * @param files
     *            list of files in order with the path relative to the root
     *            folder
     */
    public final void addSpriteSequence(String sequenceName, String[] files) {

        this.sequenceFilepaths.put(sequenceName, new ArrayList<String>());

        for (int i = 0; i < files.length; i++) {
            this.sequenceFilepaths.get(sequenceName).add(files[i]);
        }

        if (this.currentSequence == null)
            setSpriteSequence(sequenceName);

    }

    /**
     * Adds a new sequence of sprite with the first parameter as the name of the
     * sequence.
     * <p>
     * The filePath for a texture at index i will be folder+files[i]+extension.
     * <p>
     * The root folder of the files, file extensions and individual file name
     * relative to that folder are in individual parameters for the sake of
     * simplicity.
     * <p>
     * If no sprite sequence is set to run, the sequence we add will be the one
     * we will run.
     *
     * @param sequenceName
     *            name of the new sequence
     * @param folder
     *            the root folder of the files
     * @param files
     *            list of files in order with the path relative to the root
     *            folder
     * @param extension
     *            file extensions, so as to not have to write .png for all and
     *            so on.
     */
    public final void addSpriteSequence(String sequenceName, String folder, String[] files, String extension) {

        this.sequenceFilepaths.put(sequenceName, new ArrayList<String>());
        this.sequenceFilepaths.get(sequenceName).clear();

        for (int i = 0; i < files.length; i++) {
            this.sequenceFilepaths.get(sequenceName).add(folder + files[i] + extension);
        }

        if (this.currentSequence == null)
            setSpriteSequence(sequenceName);

    }

    /**
     * Adds a sprite setting to the {@link #sequencePreferences}. This will be
     * used when {@link #currentSequence } is equal to the first parameter. The
     * order or category is set using {@link #setSpriteSequence(String)}.
     *
     * @param name
     *            The name of the sprite order to add a list of
     *            preferences/settings for
     * @param settings
     *            The renderer.object containing the settings data
     */
    public final void addPreferences(String name, SpriteSequencePreference settings) {

        sequencePreferences.put(name, settings);
    }

    /**
     * Returns the {@link SpriteSequencePreference} from
     * {@link #sequencePreferences} at the passed parameter key.
     *
     * @param name
     *            The name used when adding a sprite order setting in
     *            {@link #addPreferences(String, SpriteSequencePreference)}.
     * @return the value at the specific key. If that can't be found, returns
     *         the default settings.
     */
    public final SpriteSequencePreference getPreferences(String name) {

        if (sequencePreferences.containsKey(name)) {
            return sequencePreferences.get(name);
        } else {
            return sequencePreferences.get("Default");
        }
    }

    /**
     * Sets the default {@link SpriteSequencePreference}. Clears the current
     * preferences if clear is set to true in order to assure that those are the
     * only settings that are going to be used.
     * <p>
     * Afterwards, the settings for the default settings are updated to the new
     * value.
     *
     * @param settings
     *            The new default sprite settings. If null, will not be used
     * @param whether
     *            to clear previous settings or not.
     */
    public final void setDefaultPreferences(SpriteSequencePreference settings, boolean clear) {

        if (settings == null)
            return;
        if (clear)
            sequencePreferences.clear();
        sequencePreferences.put("Default", settings);
    }

    /**
     * Returns the default sequence preferences. Those preferences run only for
     * sequences that we have not added a custom
     * {@link SpriteSequencePreference} for using {@link #addPreferences(String,
     * SpriteSequencePreference).
     *
     * @return default preferences.
     */
    public final SpriteSequencePreference getDefaultPreferences() {

        return sequencePreferences.get("Default");
    }

    private final SpriteSequencePreference getCurrPreferences() {

        if (sequencePreferences.containsKey(currentSequence)) {
            return sequencePreferences.get(currentSequence);
        } else {
            return sequencePreferences.get("Default");
        }
    }

    // endregion

    @Override
    public final void dispose() {

        spriteColor = null;
        sequenceFilepaths.clear();
        sequencePreferences.clear();
        spritePaths.clear();
        currSprite = null;
    }

    // region out of bounds
    @Override
    protected final boolean isOutOfBounds() {
        float x = renderer.object.position.getX() + getCurrPreferences().getPositionOffset().x;
        float y = renderer.object.position.getY() + getCurrPreferences().getPositionOffset().y;
        Rectangle r = renderer.object.getSceneLayer().renderingArea;
        boolean overlaps = x < r.x + r.width && x + getCurrPreferences().getSize().x > r.x && y < r.y + r.height
                && y + getCurrPreferences().getSize().y > r.y;

        return !overlaps;
    }

    // endregion

    // endregion

    // endregion
}
