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

import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.ObjectMap.Entry;
import com.sidereal.lumm.architecture.AbstractEvent;
import com.sidereal.lumm.components.renderer.DrawerBuilder;

/**
 * Builder for {@link SpriteSequenceDrawer}, which creates animations out of
 * multiple files. For a more cohesive and less memory-intensive applcation use
 * {@link SCMLDrawer}.
 *
 * @author Claudiu Bele.
 * @see {@link SCMLBuilder} for Spriter-based animations.
 */
public class SpriteSequenceBuilder extends DrawerBuilder<SpriteSequenceDrawer> {

    // region fields

    private ObjectMap<String, AbstractEvent> eventsOnStart;

    private ObjectMap<String, AbstractEvent> eventsOnEnd;

    private ObjectMap<String, SpriteSequencePreference> preferences;

    private SpriteSequencePreference defaultPreference;

    private ObjectMap<String, String[]> sequenceFilepaths;

    private String targetSpriteSequence;

    // endregion fields

    // region constructors

    public SpriteSequenceBuilder() {

        super();
        eventsOnEnd = new ObjectMap<String, AbstractEvent>();
        eventsOnStart = new ObjectMap<String, AbstractEvent>();
        preferences = new ObjectMap<String, SpriteSequencePreference>();
        sequenceFilepaths = new ObjectMap<String, String[]>();
    }

    // endregion constructors

    // region methods

    @Override
    protected SpriteSequenceDrawer build(String name) {

        SpriteSequenceDrawer drawer = new SpriteSequenceDrawer(renderer, name, getUseRealTime());
        for (Entry<String, String[]> entry : sequenceFilepaths.entries()) {
            drawer.addSpriteSequence(entry.key, entry.value);
        }
        if (defaultPreference != null)
            drawer.setDefaultPreferences(defaultPreference, false);
        for (Entry<String, SpriteSequencePreference> entry : preferences.entries())
            drawer.addPreferences(entry.key, entry.value);
        for (Entry<String, AbstractEvent> entry : eventsOnEnd.entries())
            drawer.setEventOnAnimationEnd(entry.key, entry.value);
        for (Entry<String, AbstractEvent> entry : eventsOnStart.entries())
            drawer.setEventOnAnimationStart(entry.key, entry.value);
        if (targetSpriteSequence != null)
            drawer.setSpriteSequence(targetSpriteSequence);

        return drawer;

    }

    public SpriteSequenceBuilder addSpriteSequence(String sequenceName, String folder, String[] files,
                                                   String extension) {

        for (int i = 0; i < files.length; i++) {
            files[i] = folder + files[i] + extension;
        }
        sequenceFilepaths.put(sequenceName, files);
        return this;
    }

    public SpriteSequenceBuilder addSpriteSequence(String sequenceName, String[] files) {

        sequenceFilepaths.put(sequenceName, files);
        return this;
    }

    public SpriteSequenceBuilder addSequencePreference(String sequenceName, SpriteSequencePreference preference) {

        preferences.put(sequenceName, preference);
        return this;
    }

    public SpriteSequenceBuilder setDefaultPreference(SpriteSequencePreference preference) {

        defaultPreference = preference;
        return this;
    }

    public SpriteSequenceBuilder addEventOnAnimationStart(String sequenceName, AbstractEvent event) {

        eventsOnStart.put(sequenceName, event);
        return this;
    }

    public SpriteSequenceBuilder addEventOnAnimationEnd(String sequenceName, AbstractEvent event) {

        eventsOnEnd.put(sequenceName, event);
        return this;
    }

    public SpriteSequenceBuilder setSpriteSequence(String sequenceName) {

        this.targetSpriteSequence = sequenceName;
        return this;
    }

    // endregion methods

}
