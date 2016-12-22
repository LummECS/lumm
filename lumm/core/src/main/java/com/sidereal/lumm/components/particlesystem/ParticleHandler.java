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

package com.sidereal.lumm.components.particlesystem;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Map.Entry;
import java.util.Random;

import com.sidereal.lumm.architecture.LummObject;
import com.sidereal.lumm.architecture.concrete.ConcreteLummComponent;

/**
 * Gives the ability to create {@link ParticleEmitter} instances and update
 * them.
 *
 * @author Claudiu Bele
 */
public class ParticleHandler extends ConcreteLummComponent {

    // region fields

    public Hashtable<String, ParticleEmitter> particleEmitters;

    public ArrayList<String> particleEmittersToRemove;

    public Random random;

    // endregion fields

    // region constructors

    public ParticleHandler(LummObject obj) {

        super(obj);
        particleEmitters = new Hashtable<String, ParticleEmitter>();
        particleEmittersToRemove = new ArrayList<String>();

        random = new Random();
    }

    // endregion constructors

    // region methods

    public void addEmitter(String name, ArrayList<ParticleSpriteLayout> spriteSources) {

        ParticleEmitter emitter = new ParticleEmitter(spriteSources, object);
        particleEmitters.put(name, emitter);
    }

    public void addEmitter(String name, ParticleEmitter emitter) {

        particleEmitters.put(name, emitter);
    }

    public ParticleEmitter getEmitter(String name) {

        return particleEmitters.get(name);
    }

    @Override
    public void onUpdate() {

        for (Entry<String, ParticleEmitter> entry : particleEmitters.entrySet()) {
            entry.getValue().run();

            if (entry.getValue().mustRemove) {
                particleEmittersToRemove.add(entry.getKey());
            }
        }

        for (int i = 0; i < particleEmittersToRemove.size(); i++) {
            particleEmitters.remove(particleEmittersToRemove.get(i));
        }

        particleEmittersToRemove.clear();

    }

    // endregion methods

}
