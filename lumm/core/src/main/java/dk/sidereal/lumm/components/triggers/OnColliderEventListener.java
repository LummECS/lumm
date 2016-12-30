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
 */

package dk.sidereal.lumm.components.triggers;

import java.util.ArrayList;

import dk.sidereal.lumm.architecture.LummObject;

/**
 * Listener used for handling collision between {@link Collider} objects. Added
 * to the collider via
 * {@link Collider#setOnColliderEventListener(OnColliderEventListener)}.
 * <p>
 * CollisionArea-level collision listening is handled via
 * {@link OnCollisionAreaEventListener}.
 * <p>
 * {@link CollisionArea} collisions can also be queries by using
 * {@link CollisionArea#getCollidingAreas(Collider)} on the array returned from
 * {@link Collider#getCollisionAreas()}.
 *
 * <p>
 * Collisions will be handled in the class for objects that have a class equal
 * to or children of one of the parameters passed in the constructor, or all of
 * them in case a null parameter is passed, read more on
 * {@link #OnColliderEventListener(Class...)}. *
 * <p>
 * The status of the collision, can be {@link Collider#COLLISION_ENTER},
 * {@link Collider#COLLISION_INSIDE} or {@link Collider#COLLISION_EXIT}.
 *
 * @see {@link OnCollisionAreaEventListener} for a {@link CollisionArea}-level
 *      collision detection.
 *
 * @author Claudiu Bele
 *
 */
public abstract class OnColliderEventListener {

    ArrayList<Class<? extends LummObject>> filterClasses;

    /**
     * Constructor for OnColliderEventListener. The
     * {@link #onCollisionEvent(Collider, Collider, int)} will be called every
     * frame for every collision event related to other Colliders intersecting
     * with the {@link Collider} it is attached to. Collision events include
     * entering, exiting and staying inside collision status.
     * <p>
     * If both this and a {@link OnCollisionAreaEventListener} are not set in
     * the collider( or set to null in the respective setters), collision will
     * not be checked from the collider of the object, but the collider of other
     * classes can still pick up on collision with the object without listeners.
     * This is done to improve performance and has no functional drawbacks.
     * <p>
     * The status of the collision can be {@link Collider#COLLISION_ENTER},
     * {@link Collider#COLLISION_INSIDE} or {@link Collider#COLLISION_EXIT}.
     *
     * @param filterClasses
     *            The classes for which to filter the collision.Only objects
     *            which have a class equal to or a subclass of one of the values
     *            found as parameters will be handled ( if not passing a null
     *            parameter).
     *            <p>
     *            It is used to increase performance, limiting the objects in
     *            which to search collisions. Parent classes will also work for
     *            subclasses. Pass a null array( pass a cast null as an array of
     *            <code>Class<? extends LummObject></code> to handle
     *            everything).
     */
    public OnColliderEventListener(Class<? extends LummObject>... filterClasses) {
        if (filterClasses == null)
            return;
        this.filterClasses = new ArrayList<Class<? extends LummObject>>();
        for (int i = 0; i < filterClasses.length; i++) {
            this.filterClasses.add(filterClasses[i]);
        }
    }

    /**
     * Gets called every time a collision event( started, in progress, done)
     * between two {@link Collider} objects is triggered.
     *
     * @param ownCollider
     *            The collider object that the listener is added to.
     * @param targetCollider
     *            The collider area that <code>ownCollider</code> has an event
     *            with, whose {@link CollisionArea} objects can be queried using
     *            {@link Collider#getCollisionAreas()}.
     * @param collisionStatus
     *            The status of the collision, can be
     *            {@link Collider#COLLISION_ENTER},
     *            {@link Collider#COLLISION_INSIDE} or
     *            {@link Collider#COLLISION_EXIT}.
     */
    public abstract void onCollisionEvent(Collider ownCollider, Collider targetCollider, int collisionStatus);

}
