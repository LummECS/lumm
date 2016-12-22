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

package com.sidereal.lumm.architecture.pos;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

/**
 * Conveniance method for retrieving data between multiple {@link Position}
 * objects. All methods are static. Methods appended with a '2' are in 2D space,
 * and methods appended with a '3' are in 3D space
 *
 * @author Claudiu
 */
public class Positions {

    public static float getDist2(Position p1, Position p2) {
        final float x = p2.getX() - p1.getX();
        final float y = p2.getY() - p1.getY();
        return (float) Math.sqrt(x * x + y * y);
    }

    public static float getDist3(Position p1, Position p2) {
        final float x = p2.getX() - p1.getX();
        final float y = p2.getY() - p1.getY();
        final float z = p2.getZ() - p1.getZ();
        return (float) Math.sqrt(x * x + y * y + z * z);
    }

    public static Vector2 getTrajectory2(Position p1, Position p2) {
        double angle = Math.atan2(p1.getY() - p2.getY(), p1.getX() - p2.getX());

        float trajX = (float) Math.sin(angle);
        float trajY = (float) Math.sin(angle);

        return new Vector2(trajX, trajY);
    }

    public static double getAngle2(Position p1, Position p2) {
        float crossProd = p1.getX() * p2.getY() - p1.getY() * p2.getX();
        float dot = p1.getX() * p2.getX() + p1.getY() * p2.getY();
        return (float) Math.atan2(crossProd, dot) * MathUtils.radiansToDegrees;
    }

}
