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

package dk.sidereal.lumm.components.triggers;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Rectangle;
import dk.sidereal.lumm.architecture.AbstractEvent;
import dk.sidereal.lumm.architecture.Lumm;
import dk.sidereal.lumm.architecture.LummObject;
import dk.sidereal.lumm.architecture.concrete.ConcreteLummComponent;

/**
 * Handles collision between objects, being able to designate what
 * {@link LummObject#type} the objects have to have and an {@link AbstractEvent}
 * to run for colliding with individual types. Multiple types can be handled and
 * the behavior will ignore collision with himself.
 *
 * @author Claudiu Bele
 */
public class Collider extends ConcreteLummComponent {

    public static int COLLISION_ENTER = 0;
    public static int COLLISION_INSIDE = 1;
    public static int COLLISION_EXIT = 2;

    // region fields

    private final List<TriggerArea> collisionAreas;

    private OnColliderEventListener onColliderEventListener;
    private OnCollisionAreaEventListener onCollisionAreaEventListener;

    private final List<Collider> colliderStatuses;
    private final List<TriggerArea.TriggerAreaConnection> collisionAreaStatuses;

    private final List<Collider> tempColliderStatuses;
    private List<TriggerArea.TriggerAreaConnection> tempCollisionAreaStatuses;

    LummObject objectToFocus;
    private static Sprite debugSpriteSource;
    private Sprite debugSprite;

    // endregion fields

    // region constructors

    public Collider(LummObject obj) {

        super(obj);

        colliderStatuses = new ArrayList<Collider>();
        collisionAreaStatuses = new ArrayList<TriggerArea.TriggerAreaConnection>();
        tempColliderStatuses = new ArrayList<Collider>();
        tempCollisionAreaStatuses = new ArrayList<TriggerArea.TriggerAreaConnection>();

        if (Lumm.debug.isEnabled())
            this.debugSprite = new Sprite(debugSpriteSource);
        this.collisionAreas = new ArrayList<TriggerArea>();
        this.objectToFocus = this.object;

        setDebugToggleKeys(Keys.SHIFT_LEFT, Keys.C);
    }

    // endregion constructors

    // region methods

    @Override
    public void onUpdate() {

        for (int i = 0; i < collisionAreas.size(); i++) {

            collisionAreas.get(i).rect.x = objectToFocus.position.getX() + collisionAreas.get(i).offsetX;
            collisionAreas.get(i).rect.y = objectToFocus.position.getY() + collisionAreas.get(i).offsetY;
        }

        tempColliderStatuses.clear();
        tempCollisionAreaStatuses.clear();

        // both listeners are null, don't handle
        if (onCollisionAreaEventListener == null && onColliderEventListener == null)
            return;

        for (int i = 0; i < object.getSceneLayer().objects.size(); i++) {
            boolean handleClass = false;

            LummObject myObj = object.getSceneLayer().objects.get(i);

            if (onCollisionAreaEventListener != null) {
                if (onCollisionAreaEventListener.filterClasses == null) {
                    handleClass = true;
                } else {

                    for (int j = 0; j < onCollisionAreaEventListener.filterClasses.size(); j++) {
                        if (onCollisionAreaEventListener.filterClasses.get(i).isAssignableFrom(myObj.getClass())) {
                            handleClass = true;
                            break;
                        }
                    }
                }
            }
            if (onColliderEventListener != null) {
                if (onColliderEventListener.filterClasses == null) {
                    handleClass = true;
                } else {
                    for (int j = 0; j < onColliderEventListener.filterClasses.size(); j++) {
                        if (onColliderEventListener.filterClasses.get(i).isAssignableFrom(myObj.getClass())) {
                            handleClass = true;
                            break;
                        }
                    }
                }
            }

            if (!handleClass)
                continue;

            if (myObj.getComponent(Collider.class) != null) {
                Collider otherCollider = myObj.getComponent(Collider.class);
                // hold previous size before trying to add intersecting trigger
                // areas, to know where to search from.
                int prevSize = tempCollisionAreaStatuses.size();
                tempCollisionAreaStatuses
                        .addAll(TriggerArea.getIntersectingTriggerAreas(collisionAreas, otherCollider.collisionAreas));
                tempColliderStatuses.add(otherCollider);

                // there is more than one collider
                if (prevSize != tempCollisionAreaStatuses.size()) {
                    // enter
                    if (!colliderStatuses.contains(otherCollider)) {
                        if (onColliderEventListener != null)
                            onColliderEventListener.onCollisionEvent(this, otherCollider, COLLISION_ENTER);

                        // iterate through collision areas of objects
                        for (int j = prevSize; j < tempCollisionAreaStatuses.size(); j++) {
                            // target collider area of curr collider area status
                            // matches the collider we are iterating through
                            if (onCollisionAreaEventListener != null)
                                onCollisionAreaEventListener.onCollisionAreaEvent(
                                        ((CollisionArea) tempCollisionAreaStatuses.get(j).own),
                                        ((CollisionArea) tempCollisionAreaStatuses.get(j).target), COLLISION_ENTER);
                        }

                    }
                    // inside
                    else {
                        if (onColliderEventListener != null)
                            onColliderEventListener.onCollisionEvent(this, otherCollider, COLLISION_INSIDE);

                        // iterate through collision areas of objects
                        for (int j = prevSize; j < tempCollisionAreaStatuses.size(); j++) {
                            // target collider area of curr collider area status
                            // matches the collider we are iterating through
                            if (onCollisionAreaEventListener != null)
                                onCollisionAreaEventListener.onCollisionAreaEvent(
                                        ((CollisionArea) tempCollisionAreaStatuses.get(j).own),
                                        ((CollisionArea) tempCollisionAreaStatuses.get(j).target), COLLISION_INSIDE);
                        }
                    }

                }
                // no collider, handle remove events if necessary
                else {
                    // it exists, handle
                    if (colliderStatuses.contains(otherCollider)) {
                        colliderStatuses.remove(otherCollider);
                        if (onColliderEventListener != null)
                            onColliderEventListener.onCollisionEvent(this, otherCollider, COLLISION_EXIT);

                    }

                    // iterate through collision areas of objects
                    for (int j = 0; j < collisionAreaStatuses.size(); j++) {
                        // target collider area of curr collider area status
                        // matches the collider we are iterating through
                        if (((CollisionArea) collisionAreaStatuses.get(j).target).getCollider().equals(otherCollider)) {
                            if (onCollisionAreaEventListener != null)
                                onCollisionAreaEventListener.onCollisionAreaEvent(
                                        ((CollisionArea) collisionAreaStatuses.get(j).own),
                                        ((CollisionArea) collisionAreaStatuses.get(j).target), COLLISION_EXIT);
                            collisionAreaStatuses.remove(j--);

                        }
                    }

                }

            }
        }

        colliderStatuses.clear();
        collisionAreaStatuses.clear();
        colliderStatuses.addAll(tempColliderStatuses);
        collisionAreaStatuses.addAll(tempCollisionAreaStatuses);
    }

    @Override
    protected void initialiseClass() {

        if (!Lumm.debug.isEnabled())
            return;

        debugSpriteSource = new Sprite(Lumm.assets.get(Lumm.assets.frameworkAssetsFolder + "White.png", Texture.class));
        debugSpriteSource.setColor(new Color(0, 1, 0, 0.5f));
        super.initialiseClass();
    }

    @Override
    public void onDebug() {

        for (int i = 0; i < collisionAreas.size(); i++) {
            debugSprite.setBounds(collisionAreas.get(i).rect.x, collisionAreas.get(i).rect.y,
                    collisionAreas.get(i).rect.width, collisionAreas.get(i).rect.height);
            debugSprite.draw(object.getSceneLayer().spriteBatch);
        }
    }

    /**
     * Sets the {@link CollisionArea}-level event listener. Read more on
     * {@link OnCollisionAreaEventListener}. *
     * <p>
     * The listener's relevant callback is
     * {@link OnCollisionAreaEventListener#onCollisionAreaEvent(CollisionArea, CollisionArea, int)}
     * , having the 1st parameter the source collision area, the second
     * parameter as the target collision area, and the third parameter the
     * interaction status between them ( can be {@link #COLLISION_ENTER},
     * {@link #COLLISION_EXIT} or {@link #COLLISION_INSIDE})
     *
     * @param listener
     *            Listener to use for handling collision between individual
     *            <code>CollisionArea</code> instances. Null can be passed to
     *            not handle those type of events.
     */
    public void setOnCollisionAreaEventListener(OnCollisionAreaEventListener listener) {
        this.onCollisionAreaEventListener = listener;
    }

    /**
     * Sets the {@link Collider}-level event listener. Read more on
     * {@link OnColliderEventListener}.
     * <p>
     * The listener's relevant callback is
     * {@link OnColliderEventListener#onCollisionEvent(Collider, Collider, int)}
     * , having the 1st parameter the source collider, the second parameter as
     * the target collider, and the third parameter the interaction status
     * between them ( can be {@link #COLLISION_ENTER}, {@link #COLLISION_EXIT}
     * or {@link #COLLISION_INSIDE})
     *
     * @param listener
     *            Listener to use for handling collision between individual
     *            <code>Collider</code> instances. Null can be passed to not
     *            handle those type of events. The collider will still be
     *            subject to being handled in other colliders.
     */
    public void setOnColliderEventListener(OnColliderEventListener listener) {
        this.onColliderEventListener = listener;
    }

    // region collide area add

    // TODO comment
    public void addCollisionArea(CollisionArea area) {
        if (area == null)
            throw new NullPointerException("Parameter area of type ColliderArea in Collider.addColliderArea is null");
        area.collider = this;
        area.rect.setX(objectToFocus.position.getX() + area.offsetX);
        area.rect.setY(objectToFocus.position.getY() + area.offsetY);

    }

    // TODO comment
    public CollisionArea[] getCollisionAreas() {
        return (CollisionArea[]) collisionAreas.toArray();
    }

    public boolean removeCollisionArea(int index) {
        if (index < 0 && index > collisionAreas.size() - 1)
            return false;

        collisionAreas.remove(index);
        return true;
    }

    // TODO comment
    public boolean removeCollisionArea(CollisionArea area) {
        if (area == null) {
            Lumm.debug.logDebug("Parameter area of type ColliderArea in Collider.removeColliderArea is null", null);
            return false;
        } else {
            if (collisionAreas.contains(area)) {
                collisionAreas.remove(area);
                return true;
            }
            return false;
        }
    }

    // endregion

    public ArrayList<Integer> getRectanglesThatCollide(Rectangle rect) {

        ArrayList<Integer> toReturn = new ArrayList<Integer>();
        for (int i = 0; i < collisionAreas.size(); i++) {
            if (collisionAreas.get(i).rect.overlaps(rect)) {
                toReturn.add(i);
            }
        }
        return toReturn;
    }

    public LummObject getObjectToFocus() {

        return objectToFocus;
    }

    public void setObjectToFocus(LummObject objectToFocus) {

        this.objectToFocus = objectToFocus;
    }

    // endregion methods
}
