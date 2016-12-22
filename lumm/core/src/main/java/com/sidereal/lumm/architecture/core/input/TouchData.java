/*******************************************************************************
 * Copyright 2014 See AUTHORS file.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 ******************************************************************************/

package com.sidereal.lumm.architecture.core.input;

import com.badlogic.gdx.math.Vector2;
import com.sidereal.lumm.architecture.core.Input;
import com.sidereal.lumm.architecture.core.Input.InputStatus;

/**
 * Represents the data related to one touch action . Is created if it does not
 * exist whenever the input listener detects the action has been used. If an
 * instance for a specific action has not been created and the data for that
 * input action is requested by the user via
 * {@link Input#getTouchInputData(int)}, an instance is created and added to the
 * input system.
 *
 * @author Claudiu Bele
 */
public class TouchData extends ActionData {

    /** Current touch position */
    private Vector2 position;

    /** Start position of the touch. Set in the constructor */
    private Vector2 startPosition;

    /**
     * The amount of drag for a particular pointer from the moment of press. If
     * the button is not pressed, value is {@link Vector2#Zero}
     */
    private Vector2 drag;

    private Vector2 deltaDrag;

    public TouchData(int code) {
        super(code);
        startPosition = new Vector2(0, 0);
        position = new Vector2();
        drag = new Vector2();
        deltaDrag = new Vector2();
    }

    // region methods

    /**
     * Updates the data related to one touch button. The x and y position of the
     * finger is processed for calculating the drag and delta drag, which can be
     * retrieved using {@link #getDrag()} and {@link #getDeltaDrag()}
     *
     * @param inputStatus
     *            The input status of the finger ( None, Down, Press, Up )
     * @param x
     *            the x-axis position of the finger
     * @param y
     *            the y-axis position of the finger
     */
    public void update(InputStatus inputStatus, float x, float y) {
        this.inputStatus = inputStatus;
        if (inputStatus.equals(InputStatus.Down)) {
            startPosition.set(x, y);
            position.set(x, y);
        } else {
            deltaDrag.set(x - position.x, y - position.y);
            position.set(x, y);
        }

        drag.set(position.x - startPosition.x, position.y - startPosition.y);
        super.update(inputStatus);

    }

    // region setters/getters

    public Vector2 getDrag() {
        // return a copy so as to not be able to change it
        return drag.cpy();
    }

    public Vector2 getDeltaDrag() {
        // return a copy so as to not be able to change it
        return deltaDrag.cpy();
    }

    public Vector2 getPosition() {
        return position.cpy();
    }

    public Vector2 getStartPosition() {
        return startPosition.cpy();
    }

    // endregion setters/getters

    // endregion methods
}
