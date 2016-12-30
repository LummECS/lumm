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

package dk.sidereal.lumm.architecture;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;

import dk.sidereal.lumm.architecture.listeners.OnDisposeListener;
import dk.sidereal.lumm.architecture.listeners.OnEnableListener;
import dk.sidereal.lumm.architecture.listeners.OnPauseListener;
import dk.sidereal.lumm.architecture.listeners.OnResizeListener;

/**
 * The logic that is to be attached to an {@link LummObject}. The behavior is
 * attached to the abstract object by passing the object in the constructor.
 * <p>
 * Behaviors can be customised using {@link #onUpdate()} and {@link #onDebug()}
 * in a {@link LummComponent} implementation, and both inside and outside them
 * using {@link #setEventOnStartOfUpdate(AbstractEvent)} and
 * {@link #setEventOnEndOfUpdate(AbstractEvent)} which run before and after
 * {@link #onUpdate()}.
 *
 * @author Claudiu Bele
 */
public abstract class LummComponent implements IEnableable {

    // region fields

    public OnPauseListener<LummComponent> onPauseListener;
    public OnEnableListener<LummComponent> onEnableInHierarchyListener;
    public OnResizeListener<LummComponent> onResizeListener;
    public OnDisposeListener<LummComponent> onDisposeListener;

    private static HashMap<Class<? extends LummComponent>, Float> defaultUpdateFrequencies = new HashMap<Class<? extends LummComponent>, Float>();

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
    private float updateFrequency;

    /**
     * The value used for sorting the priority . 0 by default . 1 would be
     * executed later.
     */
    public int priorityLevel;

    /**
     * The variable is true from after adding to the scene until we planned on
     * removing it. Can be manually set to true or false in
     * {@link #setEnabled(boolean)} and can be retrieved using
     * {@link #isEnabled()}.
     */
    private boolean enabled;

    /**
     * The object that the behavior is attached to. Assigned in the constructor.
     */
    public final LummObject object;

    // endregion

    // region constructors

    public LummComponent(LummObject obj) {

        if (defaultUpdateFrequencies.containsKey(getClass()))
            setUpdateFrequency(defaultUpdateFrequencies.get(getClass()));
        else
            setUpdateFrequency(0);

        this.object = obj;
        obj.components.add(this);
        Collections.sort(obj.components, LummObject.behaviorsComparator);

        initialiseClass();

        // registering the class for debugging, only first object to add a
        // specific behavior triggers this for each behavior
        if (!Lumm.debug.containsComponentDebugger(getClass())) {
            Lumm.debug.addComponentDebugger(getClass());
        }
        this.enabled = true;

    }

    // endregion

    // region methods

    /**
     * Runs the behavior. Will be run from {@link LummObject#onUpdateInternal()}
     * from the object's thread.
     */
    final void onUpdateInternal() {

        try {

            if (updateFrequency == -1)
                return;

            if (updateFrequency == 0 || System.currentTimeMillis() - lastUpdateTimestamp > updateFrequency) {

                lastUpdateTimestamp = System.currentTimeMillis();

                try {
                    if (enabled && object.isEnabled()) {
                        onUpdate();
                    }
                } catch (Exception e) {
                    Lumm.debug.logError(getClass().getName() + "(onUpdate), type: " + object.getType() + ", name:"
                            + object.getName(), e);
                }

            }
        } catch (Exception e) {
            Lumm.net.logThrowable(e);
            e.printStackTrace();
        }

    }

    final void onRenderInternal() {

        try {
            if (enabled && object.isEnabled()) {
                onRender();
            }
        } catch (Exception e) {
            Lumm.net.logThrowable(e);
            e.printStackTrace();
        }
    }

    /**
     * Method to be optionally overriden. It is called the first time a behavior
     * of a particular type is created. Called in
     * {@link #GameBehavior(LummObject)}
     */
    protected void initialiseClass() {

    }

    /**
     * Method that is customised in each individual behavior's implementation,
     * which runs in the update thread
     */
    protected abstract void onUpdate();

    /** Method to run frame by frame in the rendering thread */
    protected abstract void onRender();

    /**
     * Method that runs in the rendering thread, after we are done with each
     * behavior's {@link #onRender()} each frame.
     */
    protected abstract void onDebug();

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

    /**
     * Sets the default update frequency for all new instances of a particular
     * behavior.
     *
     * @param behavior
     *            The class of the target behavior
     * @param milliseconds
     *            number of milliseconds, or 0 for every frame, or -1 for never.
     */
    public static void setDefaultUpdateFrequency(Class<? extends LummComponent> behavior, float milliseconds) {
        if (defaultUpdateFrequencies.containsKey(behavior) && defaultUpdateFrequencies.get(behavior) == milliseconds)
            return;

        defaultUpdateFrequencies.put(behavior, milliseconds);

    }

    /**
     * Sets the key bindings for a particular behavior for debugging. Should be
     * called in the contructor of a behavior if you want it to handle
     * debugging. If a behavior does not call this method enabling the debugging
     * for the behavior will not be possible.
     * <p>
     * {@link LummConfiguration#debugEnabled} has to be set to true in order for
     * debugging to be available.
     *
     * @param keys
     *            the keys that have to be pressed to trigger the toggling
     */
    protected void setDebugToggleKeys(Integer... keys) {

        if (Lumm.debug.getComponentDebugger(getClass()).keysToActivate == null) {
            Lumm.debug.getComponentDebugger(getClass()).keysToActivate = new ArrayList<Integer>(Arrays.asList(keys));
        }

    }

    @Override
    public boolean isEnabled() {
        if (!object.isInScene())
            return false;

        return enabled;
    }

    @Override
    public boolean isEnabledInHierarchy() {
        return object.isEnabledInHierarchy() && this.enabled;
    }

    @Override
    public boolean setEnabled(boolean enabled) {
        if (this.enabled == enabled)
            return false;

        this.enabled = enabled;
        if (object.isEnabledInHierarchy()) {
            if (onEnableInHierarchyListener != null)
                onEnableInHierarchyListener.onEnable(this, this.enabled);
        }
        return true;
    }

    // endregion
}
