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

package dk.sidereal.lumm.architecture.pos;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import dk.sidereal.lumm.architecture.LummObject;

/**
 * Encapsulates local and hierarchial position of a {@link LummObject} object.
 * Accessible from {@link LummObject#position}
 *
 * @author Claudiu Bele
 */
public class Position {

    // region fields

    private float x;

    private float y;

    private float z;

    private float localX;

    private float localY;

    private float localZ;

    private LummObject object;

    // endregion

    // region constructor

    public Position(LummObject object) {

        this.object = object;
    }

    // endregion

    // region methods

    // region local position

    public final float getLocalX() {

        return localX;
    }

    public final float getLocalY() {

        return localY;
    }

    public final float getLocalZ() {

        return localZ;
    }

    public final Vector2 getLocalXY() {

        return new Vector2(localX, localY);
    }

    public final Vector3 getLocal() {

        return new Vector3(localX, localY, localZ);
    }

    // region 1 param

    public final void setLocalX(float x) {

        updateChildren(setX(x, true), false, false);
    }

    public final void setLocalY(float y) {

        updateChildren(false, setY(y, true), false);
    }

    public final void setLocalZ(float z) {

        updateChildren(false, false, setZ(z, true));

    }

    // endregion

    // region 2 params

    public final void setLocal(Vector2 localXY) {

        setLocal(localXY.x, localXY.y);
    }

    public final void setLocal(float x, float y) {

        updateChildren(setX(x, true), setY(y, true), false);
    }

    // endregion

    // region 3 params

    public final void setLocal(Vector3 pos) {

        setLocal(pos.x, pos.y, pos.z);
    }

    /**
     * Sets the local position of the Game Object without changing the object's
     * reference.
     * <p>
     * Sets the {@link #localPosition} object's x and y to match the parameters
     * given, after which the hierarchical position is updated
     * <p>
     * If the object does not have a {@link #parent}, the {@link #position}'s x
     * and y are set to the parameters given. Otherwise,
     * {@link #updatePositionInHierarchy(boolean, boolean, boolean)} is called
     * on the object whose local position we are changing.
     * <p>
     * After changing the object's variables, the position of all getChildren()
     * will be updated using
     * {@link #updatePositionInHierarchy(boolean, boolean, boolean)}.
     *
     * @param x
     *            The x axis value
     * @param y
     *            The y axis value
     */
    public final void setLocal(float x, float y, float z) {

        updateChildren(setX(x, true), setY(y, true), setZ(z, true));
    }

    // endregion

    // endregion

    // region position

    // region getters

    public final float getX() {

        return x;
    }

    public final float getY() {

        return y;
    }

    public final float getZ() {

        return z;
    }

    public final Vector2 getXY() {

        return new Vector2(x, y);
    }

    public final Vector3 get() {

        return new Vector3(x, y, z);
    }

    // endregion

    // region setters

    // region 1 param

    public final void setX(float x) {

        updateChildren(setX(x, false), false, false);
    }

    public final void setY(float y) {

        updateChildren(false, setY(y, false), false);
    }

    public final void setZ(float z) {

        updateChildren(false, false, setZ(z, false));
    }

    // endregion

    // region 2 params

    public final void set(Vector2 XY) {

        set(XY.x, XY.y);
    }

    public final void set(float x, float y) {

        updateChildren(setX(x, false), setY(y, false), false);
    }

    // endregion

    // region 3 params

    public final void set(Vector3 pos) {

        set(pos.x, pos.y, pos.z);
    }

    public final void set(float x, float y, float z) {

        updateChildren(setX(x, false), setY(y, false), setZ(z, false));
    }

    public final void set(Position position) {
        set(position.x, position.y, position.z);
    }

    // endregion

    // endregion

    // endregion

    // region misc

    // region relative

    /**
     * Sets the relative position of the Game Object, in regards to its' current
     * position.
     * <p>
     * The
     *
     * @param x
     *            the value to be appended to the x-axis position of the Game
     *            Object
     * @param y
     *            the value to be appended to the y-axis position of the Game
     *            Object
     */
    public final void setRelative(float x, float y, float z) {

        setLocal(localX + x, localY + y, localZ + z);
    }

    public final void setRelative(float x, float y) {

        setRelative(x, y, 0);
    }

    // endregion

    // region internal methods

    // region setters

    private final boolean setX(float value, boolean isLocal) {

        if ((isLocal && value == localX) || (!isLocal && value == x))
            return false;

        if (isLocal)
            localX = value;
        else
            x = value;

        if (object.getParent() == null) {
            if (isLocal)
                x = value;
            else
                localX = value;
        } else {
            if (isLocal)
                x = value + object.getParent().position.getX();
            else
                localX = value - object.getParent().position.getX();
        }
        return true;
    }

    private final boolean setY(float value, boolean isLocal) {

        if ((isLocal && value == localY) || (!isLocal && value == y))
            return false;

        if (isLocal)
            localY = value;
        else
            y = value;

        if (object.getParent() == null) {
            if (isLocal)
                y = value;
            else
                localY = value;
        } else {
            if (isLocal)
                y = value + object.getParent().position.getY();
            else
                localY = value - object.getParent().position.getY();
        }

        return true;
    }

    private final boolean setZ(float value, boolean isLocal) {

        if ((isLocal && value == localZ) || (!isLocal && value == z))
            return false;

        if (isLocal)
            localZ = value;
        else
            z = value;

        if (object.getParent() == null) {
            if (isLocal)
                z = value;
            else
                localZ = value;
        } else {
            if (isLocal)
                z = value + object.getParent().position.getZ();
            else
                localZ = value - object.getParent().position.getZ();
        }
        return true;
    }

    // endregion

    // region hierarchy update

    public final void ensureHierarchialMatch() {
        object.position.updateHierarchialX();
        object.position.updateHierarchialY();
        object.position.updateHierarchialZ();
    }

    private final void updateChildren(boolean x, boolean y, boolean z) {

        for (int i = 0; i < object.getChildren().size(); i++) {
            if (x)
                object.getChildren().get(i).position.updateHierarchialX();
            if (y)
                object.getChildren().get(i).position.updateHierarchialY();
            if (z)
                object.getChildren().get(i).position.updateHierarchialZ();

            object.getChildren().get(i).position.updateChildren(x, y, z);
        }
    }

    private final void updateHierarchialX() {

        setX(object.getParent().position.x + localX, false);
    }

    private final void updateHierarchialY() {

        setY(object.getParent().position.y + localY, false);
    }

    private final void updateHierarchialZ() {

        setZ(object.getParent().position.z + localZ, false);
    }

    // endregion

    // endregion

    // endregion

    // endregion
}
