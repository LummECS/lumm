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

package dk.sidereal.lumm.architecture.core.input;

import dk.sidereal.lumm.architecture.core.Input;

/**
 * Class encapsulating an event to run when pressing, holding and releasing
 * keys, buttons and fingers. An easy way to assign a specific key, button,
 * finger to trigger an action is to use {@link ActionEventCodes}.
 * <p>
 * To listen to any actions to trigger an event( for uses such as key bindings),
 * use {@link Input.ActionCodes#ANY_ACTION}
 * <p>
 * User must implement the {@link #run(TouchData)} method , getting access to
 * individual finger press position, initial position, delta position.
 *
 * @author Claudiu Bele
 */
public abstract class OnActionEventListener {

    /**
     * Method to run whenever a user presses, holds or releases an action( mouse
     * button, keyboard, touch ). If the user decides that the data was not
     * handled or the purpose of the method has not been achieved due to some
     * external reasons, a false must be returned, whereas if the wanted
     * functionality has been achieved, return true.
     * <p>
     * This is done in order for events to be potentially handled in the next
     * input processor found in the Input Multiplexer.
     *
     * @param inputData
     * @return whether data was handled as intended
     */
    public boolean run(ActionData inputData) {

        return false;
    }

}
