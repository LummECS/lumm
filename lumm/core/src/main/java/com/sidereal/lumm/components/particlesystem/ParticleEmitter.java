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

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;
import com.sidereal.lumm.architecture.Lumm;
import com.sidereal.lumm.architecture.LummObject;
import com.sidereal.lumm.architecture.core.AppData.ParticleSettings;
import com.sidereal.lumm.architecture.core.AppData.Settings;

/**
 * Emmits particles with a specific lifetime, being able to change multiple
 * particle logic behind individual particles.
 *
 * @author Claudiu
 */
public class ParticleEmitter {

    // region fields

    public boolean mustRemove = false;

    public boolean renderFirst;

    public float gravity;

    public float gravityRandomRange;

    public float timeBetweenParticles;

    public float timeBetweenParticlesRemaining;

    public float particleTime;

    public float particleTimeRandomRange;

    public Vector2 trajectory;

    public Vector2 trajectoryRandomRange;

    public Vector2 offsetPositionRandomRange;

    public Vector2 offsetPosition;

    public float speed;

    public float speedRandomRange;

    public float timeAlive;

    public float defaultTimeAlive;

    public float particlesPerSecond;

    public ArrayList<ParticleSpriteLayout> particleSources;

    public LummObject owner;

    public boolean enabled;

    // endregion fields

    // region constructors

    public ParticleEmitter(ArrayList<ParticleSpriteLayout> spriteSources, LummObject object) {

        this.renderFirst = true;
        this.enabled = true;

        if (object != null) {
            this.owner = object;
        }

        this.offsetPosition = new Vector2();
        this.offsetPositionRandomRange = new Vector2();

        this.particleSources = spriteSources;

        for (int i = 0; i < spriteSources.size(); i++) {
            String sprite = spriteSources.get(i).sprite;
            Lumm.assets.load(sprite, Texture.class);
        }
        speed = 10;
        speedRandomRange = 0;

        gravity = 0;
        gravityRandomRange = 0;

        particleTime = 2;
        particleTimeRandomRange = 0;
        timeAlive = 10;

        trajectory = new Vector2();
        trajectoryRandomRange = new Vector2(2, 2);

        timeBetweenParticles = 0.5f;
        timeBetweenParticlesRemaining = 0;

    }

    public ParticleEmitter(ParticleEmitter emitter, LummObject obj) {

        this.mustRemove = emitter.mustRemove;
        this.renderFirst = emitter.renderFirst;

        this.gravity = emitter.gravity;
        this.gravityRandomRange = emitter.gravityRandomRange;

        this.timeBetweenParticles = emitter.timeBetweenParticles;
        this.timeBetweenParticlesRemaining = emitter.timeBetweenParticlesRemaining;

        this.trajectory = new Vector2(emitter.trajectory);
        this.trajectoryRandomRange = new Vector2(emitter.trajectoryRandomRange);

        this.offsetPositionRandomRange = new Vector2(emitter.offsetPositionRandomRange);
        this.offsetPosition = new Vector2(emitter.offsetPosition);
        // this.position = new Vector3(emitter.o);

        this.speed = emitter.speed;
        this.speedRandomRange = emitter.speedRandomRange;

        this.timeAlive = emitter.timeAlive;
        this.defaultTimeAlive = emitter.defaultTimeAlive;

        this.particleSources = new ArrayList<ParticleSpriteLayout>(emitter.particleSources);

        setOwner(obj);

        this.enabled = emitter.enabled;
    }

    // endregion constructors

    // region methods

    public void setTimeAlive(float timeAlive) {

        this.defaultTimeAlive = timeAlive;
        this.timeAlive = timeAlive;
    }

    public void restart() {

        this.timeAlive = this.defaultTimeAlive;
    }

    public void setOwner(LummObject object) {

        if (object != null) {
            this.owner = object;
        }
    }

    ParticleSpriteObject tempObject;

    public void run() {

        int particleSettings = ((Integer) Lumm.data.getSettings(Settings.PARTICLE_SETTINGS));

        if (ParticleSettings.toString(particleSettings).equals(Settings.PARTICLE_SETTINGS_NONE))
            return;

        if (timeAlive > 0) {
            timeAlive -= Lumm.time.getDeltaTime();
        }

        if ((timeAlive != -1 && timeAlive <= 0) || !enabled) {
            return;
        }

        timeBetweenParticlesRemaining -= Lumm.time.getDeltaTime();

        while (timeBetweenParticlesRemaining <= 0) {

            if (ParticleSettings.toString(particleSettings).equals(Settings.PARTICLE_SETTINGS_LOW)) {
                timeBetweenParticlesRemaining += timeBetweenParticles * 5;
            } else if (ParticleSettings.toString(particleSettings).equals(Settings.PARTICLE_SETTINGS_MEDIUM)) {
                timeBetweenParticlesRemaining += timeBetweenParticles * 2.5f;
            } else if (ParticleSettings.toString(particleSettings).equals(Settings.PARTICLE_SETTINGS_HIGH)) {
                timeBetweenParticlesRemaining += timeBetweenParticles * 1.8f;
            } else if (ParticleSettings.toString(particleSettings).equals(Settings.PARTICLE_SETTINGS_MAX)) {
                timeBetweenParticlesRemaining += timeBetweenParticles;
            }
            tempObject = new ParticleSpriteObject(owner.getScene(), this);
            tempObject.position.setRelative(0, 0, 30);

        }

    }

    // endregion methods
}
