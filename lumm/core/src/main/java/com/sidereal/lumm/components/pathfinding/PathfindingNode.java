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

package com.sidereal.lumm.components.pathfinding;

import java.util.Arrays;

import com.badlogic.gdx.math.Vector2;

/**
 * A node in a {@link PathfindingMap}.
 *
 * @author Claudiu Bele
 */
public class PathfindingNode {

    // region fields

    public int x;

    public int y;

    @SuppressWarnings("unused")
    private PathfindingMap map;

    private Vector2 position;

    private Vector2 nodePosition;

    /**
     * Whether or not if, for example, you can't access the left node, with
     * mirroraccess = true, the left node's access to this one will be false as
     * well
     */

    /** left, right, bottom, top access to keys */
    public boolean[] access;

    /**
     * Connected nodes values to the right, left, top, bottom or the values on
     * the adjacent nodes to the left, right, bottom or top to the object
     */
    public boolean[] adjacent;

    /** Whether or not to handle adjancent nodes. */
    public boolean[] handleAdjacent;

    public static int LEFT_ACCESS = 0;

    public static int RIGHT_ACCESS = 1;

    public static int BOTTOM_ACCESS = 2;

    public static int TOP_ACCESS = 3;

    // endregion fields

    // region constructors

    public PathfindingNode(PathfindingMap map, int nodeX, int nodeY, boolean access) {

        this(map, nodeX, nodeY, new boolean[]{access, access, access, access});
    }

    public PathfindingNode(PathfindingMap map, int nodeX, int nodeY, boolean[] access) {

        this(map, nodeX, nodeY, access, false);
    }

    public PathfindingNode(PathfindingMap map, int nodeX, int nodeY, boolean[] access, boolean mirrorAccess) {

        if (nodeX < 0 || nodeX > map.nodesX - 1)
            throw new RuntimeException("Node X is outside of bounds");
        if (nodeY < 0 || nodeY > map.nodesY - 1)
            throw new RuntimeException("Node X is outside of bounds");
        if (access.length != 4)
            throw new RuntimeException("Node is being passed " + access.length + " values");

        this.access = access;
        if (nodeX == 0)
            this.access[LEFT_ACCESS] = false;
        if (nodeX == map.nodesX - 1)
            this.access[RIGHT_ACCESS] = false;
        if (nodeY == 0)
            this.access[BOTTOM_ACCESS] = false;
        if (nodeY == map.nodesY - 1)
            this.access[TOP_ACCESS] = false;

        this.position = new Vector2(nodeX * map.getNodeSize().x, nodeY * map.getNodeSize().y);
        this.nodePosition = new Vector2(x, y);

        this.x = nodeX;
        this.y = nodeY;
        this.map = map;

        if (mirrorAccess) {
            if (nodeX != 0) {
                map.nodes[nodeX - 1][nodeY].access[RIGHT_ACCESS] = access[LEFT_ACCESS];
            }

            if (nodeX != map.nodesX - 1) {
                map.nodes[nodeX + 1][nodeY].access[LEFT_ACCESS] = access[RIGHT_ACCESS];
            }

            if (nodeY != 0) {
                map.nodes[nodeX][nodeY - 1].access[TOP_ACCESS] = access[BOTTOM_ACCESS];
            }

            if (nodeY != map.nodesY - 1) {
                map.nodes[nodeX][nodeY + 1].access[BOTTOM_ACCESS] = access[TOP_ACCESS];
            }
        }

    }

    public PathfindingNode(PathfindingMap map, int x, int y, boolean[] access, boolean[] adjacentNodesData,
                           boolean[] handleAdjacent) {

        this(map, x, y, access);
        if (adjacentNodesData.length != 4)
            throw new RuntimeException("Adjacent node is being passed " + access.length + " values");
        if (handleAdjacent.length != 4)
            throw new RuntimeException("Adjacent node is being passed " + access.length + " values");

        this.adjacent = adjacentNodesData;
        this.handleAdjacent = handleAdjacent;

        this.position = new Vector2(x * map.getNodeSize().x, y * map.getNodeSize().y);
        this.nodePosition = new Vector2(x, y);

        if (x != 0 && handleAdjacent[LEFT_ACCESS]) {
            map.nodes[x - 1][y].access[RIGHT_ACCESS] = adjacent[LEFT_ACCESS];
        }

        if (x != map.nodesX - 1 && handleAdjacent[RIGHT_ACCESS]) {
            map.nodes[x + 1][y].access[LEFT_ACCESS] = adjacent[RIGHT_ACCESS];
        }

        if (y != 0 && handleAdjacent[BOTTOM_ACCESS]) {
            map.nodes[x][y - 1].access[TOP_ACCESS] = adjacent[BOTTOM_ACCESS];
        }

        if (y != map.nodesY - 1 && handleAdjacent[TOP_ACCESS]) {
            map.nodes[x][y + 1].access[BOTTOM_ACCESS] = adjacent[TOP_ACCESS];
        }
    }

    public PathfindingNode(PathfindingMap map, int nodeX, int nodeY, NodePrefab prefab) {

        this(map, nodeX, nodeY, Arrays.copyOf(prefab.access, 4), Arrays.copyOf(prefab.adjacent, 4),
                Arrays.copyOf(prefab.handleAdjacent, 4));
    }

    // endregion constructors

    // region methods

    @Override
    public String toString() {

        return "(" + x + "," + y + ")";
    }

    public void setPosition(float x, float y) {

        this.position.set(x, y);
    }

    public Vector2 getPosition() {

        return this.position;
    }

    public Vector2 getNodePosition() {

        return nodePosition;
    }

    public static class NodePrefab {

        public static NodePrefab RIGHT_WALL = new NodePrefab(new boolean[]{true, false, true, true},
                new boolean[]{false, true, false, false}, new boolean[]{false, false, false, false},
                new boolean[]{false, true, false, false});

        public static NodePrefab LEFT_WALL = new NodePrefab(new boolean[]{false, true, true, true},
                new boolean[]{true, false, false, false}, new boolean[]{false, false, false, false},
                new boolean[]{true, false, false, false});

        public static NodePrefab BOTTOM_WALL = new NodePrefab(new boolean[]{true, true, false, true},
                new boolean[]{false, false, true, false}, new boolean[]{false, false, false, false},
                new boolean[]{false, false, true, false});

        public static NodePrefab TOP_WALL = new NodePrefab(new boolean[]{true, true, true, false},
                new boolean[]{false, false, false, true}, new boolean[]{false, false, false, false},
                new boolean[]{false, false, false, true});

        public static NodePrefab RIGHT_LEDGE = new NodePrefab(new boolean[]{true, false, true, true},
                new boolean[]{false, true, false, false}, new boolean[]{false, false, false, false},
                new boolean[]{false, false, false, false});

        public static NodePrefab LEFT_LEDGE = new NodePrefab(new boolean[]{false, true, true, true},
                new boolean[]{true, false, false, false}, new boolean[]{false, false, false, false},
                new boolean[]{false, false, false, false});

        public static NodePrefab BOTTOM_LEDGE = new NodePrefab(new boolean[]{true, true, false, true},
                new boolean[]{false, false, true, false}, new boolean[]{false, false, false, false},
                new boolean[]{false, false, false, false});

        public static NodePrefab TOP_LEDGE = new NodePrefab(new boolean[]{true, true, true, false},
                new boolean[]{false, false, false, true}, new boolean[]{false, false, false, false},
                new boolean[]{false, false, false, false});

        public static NodePrefab WALL = new NodePrefab(new boolean[]{false, false, false, false},
                new boolean[]{true, true, true, true}, new boolean[]{false, false, false, false},
                new boolean[]{true, true, true, true});

        public static NodePrefab INNER_WALL = new NodePrefab(new boolean[]{false, false, false, false},
                new boolean[]{true, true, true, true}, new boolean[]{false, false, false, false},
                new boolean[]{false, false, false, false});

        public boolean access[];

        public boolean handleAccess[];

        public boolean adjacent[];

        public boolean handleAdjacent[];

        public NodePrefab(boolean[] access, boolean[] handleAccess, boolean[] adjacent, boolean handleAdjacent[]) {

            if (access.length != 4)
                throw new RuntimeException("Node is being passed " + access.length + " values");
            if (adjacent.length != 4)
                throw new RuntimeException("Node is being passed " + access.length + " values");
            if (handleAdjacent.length != 4)
                throw new RuntimeException("Node is being passed " + access.length + " values");

            this.access = access;
            this.handleAccess = handleAccess;
            this.adjacent = adjacent;
            this.handleAdjacent = handleAdjacent;
        }
    }

    // endregion methods
}
