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

package com.sidereal.lumm.architecture;

/**
 * Class used for handling events when certain actions are triggered by
 * {@link LummComponent} and {@link LummObject} implementations.
 * <p>
 * The class is abstract so as to prompt the user to create an anonymous inner
 * class, customising the {@link #runr(Object...)} and {@link #run(Object...)}
 * methods at will, without having to create additional classes.
 */
public abstract class AbstractEvent {

    // region constructors

    public AbstractEvent() {

    }

    // endregion

    // region methods

    /**
     * Runs an event with a varying number of arguments, returning a varying
     * number of arguments. If no values are to be returned use
     * {@link #run(Object...)}.
     *
     * @param objects
     *            to pass to the method
     * @return an array of type {@link Object}. Can be null.
     */
    public Object[] runr(Object... objects) {

        return null;
    }

    /**
     * Runs an event with a varying number of arguments. If values are to be
     * returned use {@link #runr(Object...)}.
     *
     * @param objects
     */
    public void run(Object... objects) {

    }

    // endregion

}
