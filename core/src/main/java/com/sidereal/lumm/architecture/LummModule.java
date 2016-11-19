/*******************************************************************************
 * Copyright 2015 See AUTHORS file.
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

package com.sidereal.lumm.architecture;

import java.util.List;

import com.badlogic.gdx.Gdx;
import com.sidereal.lumm.architecture.core.Input;
import com.sidereal.lumm.architecture.listeners.OnDisposeListener;
import com.sidereal.lumm.architecture.listeners.OnEnableListener;
import com.sidereal.lumm.architecture.listeners.OnPauseListener;

/**
 * Contains Functionality that can be used throughout the entire framework.
 * <p>
 * Certain instances of {@link LummComponent} do not work if a particular
 * {@link LummModule} cannot be found. Modules classes can be passed before
 * {@link Lumm} and a constructor will be called with the
 * {@link LummConfiguration} as a parameter.
 * 
 * @author Claudiu Bele
 */
public abstract class LummModule implements IEnableable {

	public OnEnableListener<LummModule> onEnableListener;
	public OnPauseListener<LummModule> onPauseListener;
	public OnDisposeListener<LummModule> onDisposeListener;
	private boolean initialized;

	private boolean enabled;

	/**
	 * Timestamp of last time the update function has been called in the current
	 * instance of the behavior.
	 */
	private long lastUpdateTimestamp;

	/**
	 * Frequency of update calls. If set to 0, will be called every time the
	 * update thread runs, which runs in tandem with the rendering thread in
	 * normal conditions.
	 */
	private float updateFrequency = 0;

	/**
	 * Module constructor, has to be used in order for the
	 * 
	 * @param updateFrequency
	 */
	public LummModule(LummConfiguration config) {
		this.updateFrequency = 0;
	}

	final void onCreateInternal() {
		Lumm.debug.log("Initializing module: "+getClass().getName(), null);
		onCreate();
		initialized = true;
	}

	boolean isInitialized() {
		return initialized;

	}

	/**
	 * Modules will be traversed, {@link #onCreate()} will only be called if all
	 * dependencies are solved ( checked via {@link #isInitialized()} which is
	 * changed to true after {@link #onCreate()()}.
	 * 
	 * @return
	 */
	public abstract List<Class<? extends LummModule>> getDependencies();

	/**
	 * Method called after {@link Gdx} is initialized. The constructor should
	 * only be used for setting parameters.
	 */
	public abstract void onCreate();

	/**
	 * Updates the module. Certain modules may not need to use this
	 * functionality, but some, such as {@link Input} require updating, so
	 * providing this functionality is rather easy
	 */
	public abstract void onUpdate();

	final void onUpdateInternal() {

		try {

			if (updateFrequency == -1)
				return;

			if (updateFrequency == 0 || System.currentTimeMillis() - lastUpdateTimestamp > updateFrequency) {

				lastUpdateTimestamp = System.currentTimeMillis();
				onUpdate();
			}
		} catch (Exception e) {
			Lumm.net.logThrowable(e);
		}
	}

	/**
	 * Sets the update frequency of the behavior, in milliseconds ( by default
	 * once every frame)
	 * <p>
	 * If you do not want a behavior's update to run, pass -1 as parameter, and
	 * if you want it to run every frame, pass 0 as a parameter
	 * 
	 * @param milliseconds
	 */
	public void setUpdateFrequency(float milliseconds) {

		if (milliseconds < 0 && milliseconds != -1) {
			Lumm.debug.logDebug("Trying to set the number of milliseconds per update to " + milliseconds
					+ " in object of class " + getClass().getName(), null);
			return;
		}

		this.updateFrequency = milliseconds;

	}

	@Override
	public boolean isEnabled() {
		return enabled;
	}

	@Override
	public boolean isEnabledInHierarchy() {
		return true;
	}

	@Override
	public boolean setEnabled(boolean enabled) {
		this.enabled = enabled;
		if (this.onEnableListener != null)
			this.onEnableListener.onEnable(this,enabled);
		return true;
	}

}
