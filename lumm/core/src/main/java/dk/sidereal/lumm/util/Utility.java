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

package dk.sidereal.lumm.util;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

/**
 * Provides static access to mathematical methods.
 *
 * @author Claudiu Bele
 */
public class Utility {

    public static Vector2 interpolate(Vector2 from, Vector2 to, float factor) {

        factor = Math.max(0, Math.min(1, factor));

        float newX = from.x * (1 - factor) + to.x * factor;
        float newY = from.y * (1 - factor) + to.y * factor;

        from.x = newX;
        from.y = newY;

        return new Vector2(newX, newY);
    }

    public static float lerpTowards(float from, float to, float factor) {

        // clamp the factor
        factor = Math.max(0, Math.min(1, factor));

        return from * (1 - factor) + to * factor;
    }

    public static float lerpTowards(float from, float to, float factor, float limit) {

        float newValue = lerpTowards(from, to, factor);
        if (Math.abs(newValue - to) <= limit) {
            newValue = to;
        }
        return newValue;
    }

    /**
     * Adds the x and y values to the already-assigned values of the vector2
     * passed
     *
     * @param vector
     *            Variable to be updated
     * @param x
     *            the value we want to add on the X axis
     * @param y
     *            the value we want to add on the Y axis
     */
    public static void setRelativePosition(Vector2 vector, float x, float y) {

        vector.x += x;
        vector.y += y;
    }

    public static void setRelativePosition(Vector3 vector, float x, float y, float z) {

        vector.x += x;
        vector.y += y;
        vector.z += z;
    }

    public static Float[] fillArray(int size, Float value) {

        Float[] array = new Float[size];
        for (int i = 0; i < size; i++) {
            array[i] = value;
        }
        return array;

    }

    public static Float[] fillArray(int size, Float initValue, Float increment, boolean goBackFromMiddle) {

        Float[] array = new Float[size];
        for (int i = 0; i < size; i++) {

            if (goBackFromMiddle && i >= size / 2) {
                array[i] = initValue - increment * (i - size / 2);
            } else {
                array[i] = initValue + increment * i;
            }
        }
        return array;

    }

    public static float moveTowards(float from, float to, float factor, boolean onlyTowards) {

        if (from == to)
            return from;

        if (onlyTowards) {
            if (from < to && to - from < to - (from + factor))
                factor *= -1;
            if (from > to && from - to < (from + factor) - to)
                factor *= -1;
        }

        float newValue = from + factor;

        if (from > to && newValue < to)
            newValue = to;
        if (from < to && newValue > to)
            newValue = to;

        return newValue;
    }

    public static final String PLATFORM_Desktop = "Desktop";
    public static final String PLATFORM_IOS = "iOS";
    public static final String PLATFORM_WEB = "Web";
    public static final String PLATFORM_ANDROID = "Android";

    public static String getPlatform() {
        switch (Gdx.app.getType()) {
            case Android:
                return PLATFORM_ANDROID;
            case HeadlessDesktop:
            case Desktop:
                return PLATFORM_Desktop;
            case iOS:
                return PLATFORM_IOS;
            case WebGL:
            case Applet:
                return PLATFORM_WEB;
        }
        return null;
    }
}
