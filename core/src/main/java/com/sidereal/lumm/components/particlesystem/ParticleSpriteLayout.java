/*******************************************************************************
 * Copyright 2014 See AUTHORS file.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.sidereal.lumm.components.particlesystem;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Vector2;
import com.sidereal.lumm.architecture.AbstractEvent;
import com.sidereal.lumm.architecture.Lumm;
import com.sidereal.lumm.architecture.LummObject;
import com.sidereal.lumm.components.renderer.sprite.SpriteDrawer;
import com.sidereal.lumm.util.Utility;

/**
 * Blueprint for {@link ParticleSpriteObject}
 * 
 * @author Claudiu Bele
 */
public class ParticleSpriteLayout {

	// region fields

	public String sprite;

	/** Size of the sprite, will center the sprite based on size */
	public Vector2 size;

	public Vector2 sizeRandomRange;

	public ParticleEmitter emitter;

	public Color tintColor;

	public AbstractEvent progressionEvent;

	// endregion fields

	// region constructors

	public ParticleSpriteLayout(String sprite, Vector2 size, ParticleEmitter emitter) {

		this(sprite, size);
		this.emitter = emitter;
	}

	public ParticleSpriteLayout(String sprite, Vector2 size) {

		this.sprite = sprite;
		this.size = size;
		this.tintColor = Color.WHITE;
		this.progressionEvent = new AbstractEvent() {

			@Override
			public void run(Object... params) {

				LummObject obj = (LummObject) params[0];

				if (obj instanceof ParticleSpriteObject) {

					((ParticleSpriteObject) obj).currGravity = Utility.lerpTowards(
							((ParticleSpriteObject) obj).currGravity, ((ParticleSpriteObject) obj).gravity, 0.1f);

					((ParticleSpriteObject) obj).position
							.setRelative(((ParticleSpriteObject) obj).trajectory.x * ((ParticleSpriteObject) obj).speed
									* Lumm.time.getDeltaTime(), Math
											.min(1, Math.max(-1,
													((ParticleSpriteObject) obj).currGravity
															+ ((ParticleSpriteObject) obj).trajectory.y))
											* ((ParticleSpriteObject) obj).speed * Lumm.time.getDeltaTime());

					((ParticleSpriteObject) obj).renderable.getDrawer("Main", SpriteDrawer.class).setTransparency(
							((((ParticleSpriteObject) obj).timeRemaining / ((ParticleSpriteObject) obj).timeToLive)),
							false);

				}
				return;
			}
		};
	}

	public ParticleSpriteLayout(String sprite, Vector2 size, ParticleEmitter emitter, Vector2 sizeOffset) {

		this(sprite, size, emitter);
		this.sizeRandomRange = sizeOffset;
	}

	// endregion constructors

	// region methods

	public void handleProgression(ParticleSpriteObject pso) {

	}

	// endregion methods
}
