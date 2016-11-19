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

package com.sidereal.lumm.architecture;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import com.badlogic.gdx.Net;
import com.badlogic.gdx.math.Vector2;
import com.sidereal.lumm.architecture.listeners.OnDisposeListener;
import com.sidereal.lumm.architecture.listeners.OnEnableListener;
import com.sidereal.lumm.architecture.listeners.OnParentDisposeListener;
import com.sidereal.lumm.architecture.listeners.OnPauseListener;
import com.sidereal.lumm.architecture.listeners.OnResizeListener;
import com.sidereal.lumm.architecture.pos.Position;
import com.sidereal.lumm.util.LummException;

/**
 * An entity in the framework. It is added in the framework by passing the scene
 * we want to add it to in the constructor
 * {@link #LummObject(LummScene, Object...)}.
 * <p>
 * To add functionality in the form of a {@link LummComponent}, the
 * {@link LummComponent#GameBehavior(LummObject)} constructor has to be called
 * which will add the behavior to the passed object's {@link #components}.
 * <p>
 * Additional custom functionality can be added using methods that can be
 * overriden such as {@link #onCreate(Object...)}, {@link #onUpdate()},
 * {@link #onSceneChange()}, {@link #onResize(float, float, float, float)} and
 * {@link #onPositionChange()}.
 * <p>
 * To make the object exist throughout all scenes until deleted, use
 * {@link #isPersistent}, and to pass the object to the next scene only, use
 * {@link LummScene#passObjectToNextScene(LummObject)} or
 * {@link LummScene#passObjectsToNextScene(ArrayList)}.
 * <p>
 * The {@link LummSceneLayer} can be set using {@link #setSceneLayer(String)},
 * being able to afterwards get SceneLayer data from {@link #SceneLayer}.
 * <p>
 * Removing the object is done using {@link LummScene#removeobject(LummObject)}.
 * 
 * @author Claudiu Bele
 */
public abstract class LummObject implements IEnableable {

	// region fields
	public OnEnableListener<LummObject> onEnableInHierarchyListener;
	public OnPauseListener<LummObject> onPauseListener;
	public OnDisposeListener<LummObject> onDisposeListener;
	public OnResizeListener<LummObject> onResizeListener;
	public OnParentDisposeListener<LummObject, LummObject> onParentDisposeListener;

	public static final OnParentDisposeListener<LummObject, LummObject> ObjectCascade = new OnParentDisposeListener<LummObject, LummObject>() {

		public void onDispose(LummObject parent, LummObject object) {
			parent.onDisposeInternal();

		};
	};

	public static final OnParentDisposeListener<LummObject, LummObject> RemoveAsParent = new OnParentDisposeListener<LummObject, LummObject>() {

		@Override
		public void onDispose(LummObject parent, LummObject object) {
			object.setParent(null);
		}
	};

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
	 * Whether or not the object is persistent through scenes, automatically
	 * being added to the new scene.
	 * <p>
	 * Objects with this value equal to {@link Boolean#TRUE} will be passed to
	 * the next scene regardless of whether or not it is found in the previous
	 * scene's {@link LummScene#toKeepForNextScene}.
	 * <p>
	 * Used in {@link LummScene#LummScene()} when processing the previous screen
	 * to retrieve the objects found inside of it.
	 */
	private boolean isPersistent;

	/**
	 * Set to false,after being added to the scene and in the scene, set to true
	 */
	boolean inScene;

	/**
	 * The variable is true from after adding to the scene until we planned on
	 * removing it. Can be manually set to true or false in
	 * {@link #setEnabled(boolean)} and can be retrieved using
	 * {@link #isEnabled()}.
	 */
	private boolean enabled;

	/**
	 * Whether the object is enabled in the hierarchy. If there is no parent,
	 * {@link #enabled} will be returned, otherwise
	 * {@link #isEnabledInHierarchy()}
	 * 
	 */
	private boolean isEnabledInHierarchy;

	/** The object's target SceneLayer */
	private LummSceneLayer SceneLayer;

	// region parent/children

	/** List of children. When adding a new Children, we add them to the list */
	final ArrayList<LummObject> children;

	/** The parent of a child, assigned when adding a child to a parent. */
	private LummObject parent;

	// endregion

	/**
	 * The name of the object, will be used when finding an object by name or
	 * type. Changing this value without a setter ( using
	 * {@link #setName(String)} ) is only useful if set in
	 * {@link #onCreate(Object...)}.
	 */
	private String name;

	/**
	 * The type of the object,will be used when finding an object by name or
	 * type. Changing this value without a setter ( using
	 * {@link #setType(String)} ) is only useful if set in
	 * {@link #onCreate(Object...)}
	 */
	private String type;

	/** The scene that the object is currently in */
	private LummScene scene;

	/**
	 * The list of behaviors. Will be added by passing the object to the
	 * constructor of a behavior
	 */
	final List<LummComponent> components;

	/**
	 * Comparator used for sorting the behaviors, set based on priority by
	 * default;
	 */
	protected static Comparator<LummComponent> behaviorsComparator = new Comparator<LummComponent>() {

		@Override
		public int compare(LummComponent o1, LummComponent o2) {

			return (o1.priorityLevel - o2.priorityLevel);
		}

	};

	/** Queue of events to run in the main game thread. */
	private final List<AbstractEvent> gameThreadEvents;

	/**
	 * Object that represents the object's position. Assigned in
	 * {@link LummObject#LummObject(LummScene, Object...)}.
	 */
	public final Position position;

	// endregion fields

	// region constructors

	public LummObject() {
		this(Lumm.getScene(), (Object[]) null);
	}

	public LummObject(Object... params) {
		this(Lumm.getScene(), params);
	}

	/**
	 * Default LummObject constructor.
	 * 
	 * @param scene
	 * @param params
	 *            parameters passed to {@link #onCreate(Object...)}
	 */
	public LummObject(LummScene scene, Object... params) {

		if (defaultUpdateFrequencies.containsKey(getClass()))
			setUpdateFrequency(defaultUpdateFrequencies.get(getClass()));
		else
			setUpdateFrequency(0);

		this.isPersistent = false;
		this.components = new ArrayList<LummComponent>();
		this.gameThreadEvents = new ArrayList<AbstractEvent>();
		this.children = new ArrayList<LummObject>();
		this.scene = scene;
		this.position = new Position(this);
		this.type = (getClass().getSimpleName());
		this.name = (this.getType() + " " + System.nanoTime());
		this.enabled = true;
		this.isEnabledInHierarchy = true;

		setSceneLayer(scene.defaultBatch.name);
		onCreate(params);
		scene.addObject(this);
	}

	// endregion

	// region methods

	// region internal

	/**
	 * Disposes all of the assets of a {@link LummObject}, including behaviors
	 * and all descendants.
	 * 
	 * Method to be called when we want an object to be removed from the scene.
	 * It will update the parent's list of children by removing the object from
	 * the parents' list.
	 * <p>
	 * The method first calls {@link #onDisposeInternal()} for all children
	 * found in {@link #children}.
	 * <p>
	 * The list of behaviors found in {@link #components} is iterated over,
	 * calling {@link LummComponent#onDisposeListener} in each of them.
	 * <p>
	 * it also calls {@link #onDisposeListener} before calling it for each
	 * component, which is a method that can be customised from implementation
	 * to implementation
	 */
	final void onDisposeInternal() {

		// removes itself from the parent's list of children if the object has
		// parents
		if (parent != null) {
			if (parent.children != null) {
				synchronized (parent.children) {
					parent.children.remove(this);
				}
			}
		}

		if (children != null) {
			for (int i = 0; i < children.size(); i++) {
				if (children.get(i).onParentDisposeListener != null)
					children.get(i).onParentDisposeListener.onDispose(this, children.get(i));
				children.get(i).onDisposeInternal();
			}
		}

		// calls the same method in all of the chilldren
		if (onDisposeListener != null)
			onDisposeListener.onDispose(this);

		// disposes all of the data that each individual gamebehavior wants to
		// dispose
		if (components != null) {
			for (int i = 0; i < components.size(); i++) { // TODO do same thing
															// with component as
															// with do with
															// children
				if (components.get(i).onDisposeListener != null)
					components.get(i).onDisposeListener.onDispose(components.get(i));
				new WeakReference<LummComponent>(components.get(i));
			}
			components.clear();
		}

		new WeakReference<LummObject>(this);

	}

	/**
	 * Updates the debugging methods in each behavior attached to the LummObject
	 * 
	 * @param info
	 */
	final void onDebugInternal() {
		if (!isEnabled())
			return;
		for (LummComponent behavior : components) {
			if (Lumm.debug.isEnabled() && Lumm.debug.getComponentDebugger(behavior.getClass()).enabled) {
				behavior.onDebug();
			}

		}
	}

	/**
	 * Updates the Game Object, being called every frame
	 * <p>
	 * If the object has a parent, it has it's position set the parent's
	 * position to which we append the object's own {@link #localPosition}
	 * variable of type {@link Vector2}.
	 * <p>
	 * The method calls the extendable method {@link #onUpdate()}, sorts the
	 * behaviors in order of their priority value, as well as updating each of
	 * the behaviors found in {@link #components} by calling
	 * {@link LummComponent#onUpdate()}.
	 */
	final void onUpdateInternal() {

		try {

			if (!isEnabled())
				return;

			if ((updateFrequency == 0 || System.currentTimeMillis() - lastUpdateTimestamp > updateFrequency)
					&& updateFrequency != -1) {

				lastUpdateTimestamp = System.currentTimeMillis();
				try {
					onUpdate();
				} catch (Exception e) {
					Lumm.debug.logError("UpdateInternal in object ( type: " + getType() + ", name:" + getName()
							+ " ), exception:" + e.toString(), null);
				}
			}

			for (int i = 0; i < components.size(); i++) {

				components.get(i).onUpdateInternal();
			}

		} catch (Exception e) {
			Lumm.net.logThrowable(e);
			e.printStackTrace();
		}
	}

	/**
	 * Renders the game object, by calling the update function in each behavior
	 * that is tagged as a rendering behavior
	 */
	final void onRenderInternal() {

		try {

			if (!isEnabled())
				return;

			try {

				onRender();
				while (gameThreadEvents.size() > 0) {
					try {
						gameThreadEvents.remove(0).run();
					} catch (Exception e) {
						Lumm.net.logThrowable(e);
						Lumm.debug.logError(
								"Game Thread Event in object ( type: " + getType() + ", name:" + getName() + ")", e);
					}
				}
			} catch (Exception e) {
				Lumm.debug.logError("RenderInternal in object ( type: " + getType() + ", name:" + getName() + " )", e);
			}

			for (int i = 0; i < components.size(); i++) {

				components.get(i).onRenderInternal();
			}
		} catch (Exception e) {
			Lumm.handleException(e);
		}

	}

	/**
	 * Internally pauses the game, calling {@link #onPause(boolean)} and the
	 * {@link LummComponent#onPause(boolean)} for each component attached to
	 * this object
	 */
	final void onPauseInternal(boolean value) {
		if (onPauseListener != null)
			onPauseListener.onPause(this,value);

		// iterate through scene layer objects' components
		for (int k = 0; k < components.size(); k++) {
			if (components.get(k).onPauseListener != null)
				components.get(k).onPauseListener.onPause(components.get(k), value);
		}
	}

	// endregion internal

	// region override-able methods

	/**
	 * Method called in {@link #LummObject(LummScene, Object...)} which can be
	 * extended to initialize logic in individual Game Object implementations
	 * 
	 * @param params
	 *            parameters passed from
	 *            {@link #LummObject(LummScene, Object...)}
	 */
	protected abstract void onCreate(Object... params);

	/** Method to run in the update loop that can be customised */
	protected abstract void onUpdate();

	/** Method to run in the rendering loop that can be customised */
	protected abstract void onRender();

	/**
	 * Called in {@link Lumm#handleSceneTransition()}, on the objects that were
	 * passed from a scene to another one.
	 */
	protected abstract void onSceneChange();

	// endregion

	// region getters/setters

	// region SceneLayer

	public final void setSceneLayer(String newTag) {

		if (getScene() == null)
			throw new NullPointerException("LummScene attached to LummObject is null in setSceneLayer");

		for (int i = 0; i < scene.sceneLayers.size(); i++) {

			if (scene.sceneLayers.get(i).name.equals(newTag)) {
				if (SceneLayer != null) {
					if (SceneLayer.objects.contains(this))
						SceneLayer.objects.remove(this);
				}
				SceneLayer = scene.sceneLayers.get(i);
				if (!SceneLayer.objects.contains(this)) {
					SceneLayer.objects.add(this);
				}
				return;

			}
		}

		throw new LummException("Didn't find a SceneLayer with the name equal to " + newTag);
	}

	public final LummSceneLayer getSceneLayer() {
		return SceneLayer;
	}

	// endregion SceneLayer

	// region parent

	/**
	 * Set's a Game Object's parent, updating all the object's position and
	 * level based on parent data.
	 * <p>
	 * If the {@link #parent} variable is null when calling this method, the
	 * {@link #localPosition} variable is set to {@link #position}, in order to
	 * have the real object's position saved when updating {@link #position} to
	 * meet the hierarchy's perspective on it.
	 * <p>
	 * 
	 * @param obj
	 */
	public final void setParent(LummObject obj) {

		// if LummObject passed is self, exit method
		if (obj == this)
			return;

		if (obj == null) {
			// if object previously had a parent and we pass a null, update the
			// local position of the object
			// to own hierarchial position.
			if (parent == null)
				return;
			else {
				// local position is hierarchial position
				if (parent.children != null)
					parent.children.remove(this);
				parent = null;
				position.setLocal(position.get());
				updateOnEnable(isEnabled(), isEnabled(), true);
				// updating child position based on parent position
				position.ensureHierarchialMatch();
				return;
			}
		}

		// setting the parent as an own child leads to removing the passed
		// parammeter
		// from the list of children as well as changing the child's parent to
		// null
		if (children.contains(obj)) {
			obj.setParent(null);
			children.remove(obj);
		}

		parent = obj;

		synchronized (obj.children) {
			obj.children.add(this);
		}

		// updating child position based on parent position
		position.ensureHierarchialMatch();
		updateOnEnable(isEnabled(), isEnabled(), parent.isEnabledInHierarchy());

		if (!SceneLayer.equals(obj.SceneLayer))
			this.setSceneLayer(obj.SceneLayer.name);

	}

	public final LummObject getParent() {
		return parent;
	}

	// endregion parent

	// region update frequency

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

	public float getUpdateFrequency() {
		return updateFrequency;
	}

	// endregion

	// region enabled

	/**
	 * Sets the availability of the LummObject. This does not set the value in
	 * the hierarchy, and the LummObject will not update if {@link #parent}
	 * isn't active in the hierarchy as well.
	 * 
	 * Is set to false in {@link #LummObject(LummScene, Object...)} and when
	 * adding the object to {@link LummScene#toRemove}.
	 * <p>
	 * Is set to true in {@link LummScene#addObjects}, when we add objects from
	 * last frame to their respective {@link LummSceneLayer} counterparts.
	 * 
	 * @param avaiable
	 */
	public final boolean setEnabled(boolean enabled) {

		if (!isInScene())
			return false;

		if (this.enabled == enabled)
			return false;

		if (children.size() != 0) {

			if (parent == null)
				updateOnEnable(this.enabled, enabled, true);
			else
				updateOnEnable(this.enabled, enabled, parent.isEnabledInHierarchy());
		} else {

			this.enabled = enabled;
			this.isEnabledInHierarchy = enabled;
			onEnableInHierarchyListener.onEnable(this,enabled);
		}

		return true;
	}

	private final void updateOnEnable(boolean oldValue, boolean newValue, boolean isParentEnabledInHierarchy) {

		this.enabled = newValue;
		boolean newIsEnabledInHierarchy = (isParentEnabledInHierarchy) ? newValue : false;

		if (newIsEnabledInHierarchy != isEnabledInHierarchy) {
			this.isEnabledInHierarchy = newIsEnabledInHierarchy;
			if (onEnableInHierarchyListener != null)
				onEnableInHierarchyListener.onEnable(this,newIsEnabledInHierarchy);

			for (int i = 0; i < components.size(); i++) {
				if (components.get(i).isEnabled() && components.get(i).onEnableInHierarchyListener != null)
					components.get(i).onEnableInHierarchyListener.onEnable(components.get(i),newIsEnabledInHierarchy);

			}

			for (int i = 0; i < children.size(); i++) {
				if (children.get(i).isEnabled() != newValue)
					children.get(i).updateOnEnable(children.get(i).isEnabled(), newValue, newValue);
			}
		}

	}

	/**
	 * Returns the status of activity of a LummObject in the hierarchy. If any
	 * of the ancestors of the LummObject is inactive, this LummObject will also
	 * be inactive. If all of the ancestors are self-active, then it will return
	 * the LummObject's {@link #enabled} variable.
	 * 
	 * This return value decides whether or not to update a LummObject every
	 * frame in {@link #onUpdateInternal()} or not.
	 * 
	 * @return the availability of a LummObject in a hierarchy . Us
	 */
	@Override
	public final boolean isEnabled() {

		if (!isInScene())
			return false;

		return enabled;
	}

	/**
	 * Returns the status of activity of a LummObject in the hierarchy. If any
	 * of the ancestors of the LummObject is inactive, this LummObject will also
	 * be inactive. If all of the ancestors are self-active, then it will return
	 * the LummObject's {@link #enabled} variable.
	 * 
	 * This return value decides whether or not to update a LummObject every
	 * frame in {@link #onUpdateInternal()} or not.
	 * 
	 * @return the availability of a LummObject in a hierarchy . Us
	 */
	@Override
	public boolean isEnabledInHierarchy() {

		if (!isInScene())
			return false;

		return isEnabledInHierarchy;

	}

	public boolean isEnabledInHierarchyInternal() {
		if (!isEnabled())
			return false;

		if (parent != null)
			return parent.isEnabledInHierarchy();

		return true;
	}

	// endregion

	// region persistency

	/**
	 * Sets the persistency of an object throughout multiple scenes. If set to
	 * true, the object will exist in the next scene along with all children (
	 * regardless of whether or not they are persistent, and
	 * {@link #onSceneChange()} will be called. If not, the object and the
	 * behaviors attached to it will be removed
	 * 
	 * @param isPersistent
	 */
	public final void setPersistancy(boolean isPersistent) {
		this.isPersistent = true;
	}

	public final boolean isPersistent() {

		if (isPersistent)
			return true;
		else if (parent != null) {
			return parent.isPersistent();
		}

		return false;
	}

	// endregion persistency

	// region name
	/**
	 * Default name getter
	 * 
	 * @return
	 */
	public final String getName() {

		return name;
	}

	/**
	 * Changes the name of the LummObject and updates the map in which the
	 * LummObject can be found.
	 * <p>
	 * If the name passed is the same as before or the scene is null, we will
	 * not handle the change of the variable.
	 * <p>
	 * This method can be used in {@link #onCreate(Object...)} as well as
	 * outside of it, being an alternative to explicitly changing {@link #name},
	 * removing the object from its' previous place in
	 * {@link LummScene#objectsMap } if necessary.
	 * 
	 * @param name
	 *            The new name of the LummObject. If value passed is the same as
	 *            the old one, the method will not be handled
	 */
	public final void setName(String name) {

		if (this.name.equals(name))
			return;
		if (scene == null)
			return;

		if (!scene.objectsMap.containsKey(this.getType())) {
			scene.objectsMap.put(this.getType(), new HashMap<String, LummObject>());
		} else {
			if (scene.objectsMap.get(this.getType()).containsValue(this))
				scene.objectsMap.get(this.getType()).remove(this.name);
		}

		scene.objectsMap.get(this.getType()).put(name, this);

		this.name = name;
	}

	// endregion

	// region type
	/**
	 * Default type getter
	 * 
	 * @return
	 */
	public final String getType() {

		return type;
	}

	/**
	 * Changes the type of the LummObject and updates the map in which the
	 * LummObject can be found.
	 * <p>
	 * If the type passed is the same as before or the scene is null, we will
	 * not handle the change of the variable.
	 * <p>
	 * This method can be used in {@link #onCreate(Object...)} as well as
	 * outside of it, being an alternative to explicitly changing {@link #type},
	 * removing the object from its' previous place in
	 * {@link LummScene#objectsMap } if necessary. A new Map will be created if
	 * there isn't one already for the new LummObject type.
	 * 
	 * @param type
	 *            The new type of the LummObject. If value passed is the same as
	 *            the old one, the method will not be handled
	 */
	public final void setType(String type) {

		if (this.type.equals(type))
			return;
		if (scene == null)
			return;

		if (scene.objectsMap.containsKey(this.type) && scene.objectsMap.get(this.type).containsValue(this)) {
			scene.objectsMap.get(this.type).remove(this.getName());

		}
		if (!scene.objectsMap.containsKey(type)) {
			scene.objectsMap.put(type, new HashMap<String, LummObject>());
		}
		scene.objectsMap.get(type).put(this.getName(), this);

		this.type = type;

	}

	// endregion type

	// region LummScene

	public final LummScene getScene() {
		return scene;
	}

	public final void setScene(LummScene scene) {
		this.scene = scene;
		setSceneLayer(getSceneLayer().name);

	}

	// endregion LummScene

	// region position

	// endregion position

	// endregion getters/setters

	// region children

	public final ArrayList<LummObject> getChildren() {
		return children;
	}

	// endregion

	// region behaviors
	@SuppressWarnings("unchecked")
	public final <T extends LummComponent> T getComponent(Class<T> component) {

		for (int i = 0; i < components.size(); i++) {
			if (components.get(i).getClass().equals(component))
				return (T) components.get(i);
		}
		return null;

	}

	public final boolean removeComponent(Class<? extends LummComponent> behaviorClass) {

		LummComponent targetBehavior = getComponent(behaviorClass);
		if (targetBehavior == null)
			return false;

		if (targetBehavior.onDisposeListener != null)
			targetBehavior.onDisposeListener.onDispose(targetBehavior);
		return true;
	}

	// endregion

	/**
	 * Returns whether or not the user is to be removed or is in scene.
	 * <p>
	 * Called in {@link LummSceneLayer#onResizeInternal()}
	 * 
	 * @return
	 */
	public final boolean isInScene() {

		return inScene;
	}

	/**
	 * Add events to run on the game thread. This is useful for when you want to
	 * handle actions that require an OpenGL context, such as making a new
	 * scene.
	 * <p>
	 * Possible use can e found when handling response to http requests using
	 * {@link Net#sendHttpRequest(com.badlogic.gdx.Net.HttpRequest, com.badlogic.gdx.Net.HttpResponseListener)}
	 * .
	 * <p>
	 * The event will run at the end of the update loop of the object that this
	 * method was called.
	 * 
	 * @param event
	 */
	public void runOnGameThread(AbstractEvent event) {
		gameThreadEvents.add(event);
	}

	/**
	 * Sets the default update frequency for all new instances of a particular
	 * object.
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

	// endregion

}
