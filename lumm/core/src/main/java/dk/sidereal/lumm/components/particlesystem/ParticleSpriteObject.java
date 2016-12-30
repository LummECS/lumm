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

package dk.sidereal.lumm.components.particlesystem;

import java.util.Random;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;
import dk.sidereal.lumm.architecture.Lumm;
import dk.sidereal.lumm.architecture.LummScene;
import dk.sidereal.lumm.architecture.concrete.ConcreteLummObject;
import dk.sidereal.lumm.components.renderer.Renderer;
import dk.sidereal.lumm.components.renderer.sprite.SpriteBuilder;

/**
 * GameObjects resembling individual particles. They are made based on
 * {@link ParticleSpriteLayout}.
 *
 * @author Claudiu
 */
public class ParticleSpriteObject extends ConcreteLummObject {

    // region fields

    public Renderer renderable;

    public Random rand;

    /** Overall time that the particle lives */
    public float timeToLive;

    public float timeRemaining;

    public Sprite sprite;

    public String spritePath;

    /** Size of the sprite, will center the sprite based on size */
    public Vector2 size;

    public Vector2 trajectory;

    public float rotation;

    public float speed;

    /** between -1 and 1, falling down at 1 */
    public float gravity;

    public float currGravity;

    public ParticleSpriteLayout particleSprite;

    public ParticleEmitter emitterParent;

    public ParticleEmitter emitterChild;

    // endregion fields

    // region constructors

    public ParticleSpriteObject(LummScene scene, ParticleEmitter emitter) {

        super(scene);

        if (emitter.renderFirst)
            this.position.setRelative(0, 0, -1);
        else
            this.position.setRelative(0, 0, 1);
        rand = new Random();

        this.emitterParent = emitter;

        // speed
        this.speed = emitter.speed + rand.nextFloat() * emitter.speedRandomRange - emitter.speedRandomRange / 2;

        // trajectory
        this.trajectory = new Vector2();
        trajectory.x = emitter.trajectory.x + rand.nextFloat() * emitter.trajectoryRandomRange.x
                - emitter.trajectoryRandomRange.x / 2;
        trajectory.y = emitter.trajectory.x + rand.nextFloat() * emitter.trajectoryRandomRange.x
                - emitter.trajectoryRandomRange.x / 2;

        // gravity
        this.gravity = emitter.gravity + rand.nextFloat() * emitter.gravityRandomRange - emitter.gravityRandomRange / 2;
        this.currGravity = 0;

        this.position.set(emitter.owner.position.getX() + emitter.offsetPosition.x
                        + rand.nextFloat() * emitter.offsetPositionRandomRange.x - emitter.offsetPositionRandomRange.x / 2,
                emitter.owner.position.getY() + emitter.offsetPosition.y
                        + rand.nextFloat() * emitter.offsetPositionRandomRange.y
                        - emitter.offsetPositionRandomRange.y / 2,
                emitter.owner.position.getZ());

        particleSprite = emitter.particleSources.get(rand.nextInt(emitter.particleSources.size()));
        this.spritePath = particleSprite.sprite;
        this.size = particleSprite.size;
        if (particleSprite.sizeRandomRange != null) {
            float sizeRandomizer = rand.nextFloat();

            this.size.x += sizeRandomizer * this.particleSprite.sizeRandomRange.x
                    - particleSprite.sizeRandomRange.x / 2;
            this.size.y += sizeRandomizer * this.particleSprite.sizeRandomRange.y
                    - particleSprite.sizeRandomRange.y / 2;

        }

        if (particleSprite.emitter != null) {
            emitterChild = particleSprite.emitter;
            emitterChild.setOwner(this);
        }

        this.timeRemaining = emitter.particleTime + rand.nextFloat() * emitter.particleTimeRandomRange
                - emitter.particleTimeRandomRange / 2;
        this.timeToLive = this.timeRemaining;

        handleRenderable();
    }

    @Override
    public void onCreate(Object... params) {

        setName("Particle " + System.currentTimeMillis());

    }

    // endregion constructors

    // region methods

    @Override
    public void onUpdate() {

        this.timeRemaining -= Lumm.time.getDeltaTime();
        if (timeRemaining <= 0) {
            getScene().removeobject(this);
            return;
        }
        particleSprite.progressionEvent.run(this);

    }

    public void handleRenderable() {

        renderable = new Renderer(this);

        SpriteBuilder builder = new SpriteBuilder(spritePath).setSize(size.x, size.y)
                .setOffsetPosition(-size.x / 2f, -size.y / 2).setRotation((float) Math.random() * 360)
                .setColor(particleSprite.tintColor);

        renderable.addDrawer("Main", builder);

    }

    // endregion methods
}
