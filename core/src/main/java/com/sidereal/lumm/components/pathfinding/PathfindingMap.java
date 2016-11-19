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

package com.sidereal.lumm.components.pathfinding;

import java.util.ArrayList;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.sidereal.lumm.architecture.LummObject;
import com.sidereal.lumm.components.pathfinding.PathfindingNode.NodePrefab;
import com.sidereal.lumm.components.pathfinding.PathfindingRoute.Path;

/**
 * A map with assymetrical access between nodes. Access can be change using
 * {@link #addNode(PathfindingNode)} or
 * {@link #addPrefab(int, int, NodePrefab, boolean)}.
 * 
 * @author Claudiu Bele
 */
public class PathfindingMap {

	// region fields

	public PathfindingNode[][] nodes;

	public Rectangle bounds;

	public Vector2 centerAnchorPosition;

	private Vector2 nodeSize;

	public int nodesX;

	public int nodesY;

	public ArrayList<Path> paths;

	public long lastUpdate;

	// endregion fields

	// region constructors

	public PathfindingMap(int width, int height) {

		paths = new ArrayList<PathfindingRoute.Path>();
		nodesX = width;
		nodesY = height;

		centerAnchorPosition = Vector2.Zero;
		nodeSize = new Vector2(100, 100);

		nodes = new PathfindingNode[width][height];

		for (int i = 0; i < nodes.length; i++) {
			for (int j = 0; j < nodes[i].length; j++) {
				nodes[i][j] = new PathfindingNode(this, i, j, true);
			}
		}

		bounds = new Rectangle(centerAnchorPosition.x - (nodesX * nodeSize.x) / 2,
				centerAnchorPosition.y - (nodesY * nodeSize.y) / 2, nodesX * nodeSize.x, nodesY * nodeSize.y);
	}

	// endregion constructors

	// region methods

	public Vector2 getNodePosition(int x, int y) {

		return new Vector2(getNodeX(x), getNodeY(y));
	}

	public void setNodeSize(float x, float y) {

		nodeSize.set(x, y);
		bounds = new Rectangle(centerAnchorPosition.x - (nodesX * nodeSize.x) / 2,
				centerAnchorPosition.y - (nodesY * nodeSize.y) / 2, nodesX * nodeSize.x, nodesY * nodeSize.y);

		// update node position
		for (int nodeX = 0; nodeX < nodesX; nodeX++) {
			for (int nodeY = 0; nodeY < nodesY; nodeY++) {

				nodes[nodeX][nodeY].setPosition(getNodeX(nodeX), getNodeY(nodeY));
			}
		}
	}

	public Vector2 getNodeSize() {

		return nodeSize;
	}

	public float getNodeY(int y) {

		float yPos = y - nodesY / 2 + ((nodesY % 2 == 0) ? 0.5f : 0);
		yPos *= nodeSize.y;
		yPos += centerAnchorPosition.y;
		return yPos;
	}

	public float getNodeX(int x) {

		float xPos = x - nodesX / 2 + ((nodesX % 2 == 0) ? 0.5f : 0f);
		xPos *= nodeSize.x;
		xPos += centerAnchorPosition.x;
		return xPos;
	}

	public void addNode(PathfindingNode node) {

		int x = node.x;
		int y = node.y;

		nodes[x][y].access = null;
		nodes[x][y].adjacent = null;
		nodes[x][y].handleAdjacent = null;
		nodes[x][y].x = nodes[x][y].y = 0;

		nodes[x][y] = node;

		if (x == 0) {
			nodes[x][y].access[0] = false;
		}

		if (x == nodesX - 1) {
			nodes[x][y].access[1] = false;
		}

		if (y == 0) {
			nodes[x][y].access[2] = false;
		}

		if (y == nodesY - 1) {
			nodes[x][y].access[3] = false;
		}

		paths.clear();
		lastUpdate = System.currentTimeMillis();
	}

	public void addPrefab(int x, int y, NodePrefab prefab, boolean reverse) {

		for (int i = 0; i < 4; i++) {
			if (prefab.handleAccess[i])
				nodes[x][y].access[i] = (reverse) ? !prefab.access[i] : prefab.access[i];
		}

		if (x != 0 && prefab.handleAdjacent[PathfindingNode.LEFT_ACCESS]) {
			nodes[x - 1][y].access[PathfindingNode.RIGHT_ACCESS] = (reverse)
					? !prefab.adjacent[PathfindingNode.LEFT_ACCESS] : prefab.adjacent[PathfindingNode.LEFT_ACCESS];
		}

		if (x != nodesX - 1 && prefab.handleAdjacent[PathfindingNode.RIGHT_ACCESS]) {
			nodes[x + 1][y].access[PathfindingNode.LEFT_ACCESS] = (reverse)
					? !prefab.adjacent[PathfindingNode.RIGHT_ACCESS] : prefab.adjacent[PathfindingNode.RIGHT_ACCESS];
		}

		if (y != 0 && prefab.handleAdjacent[PathfindingNode.BOTTOM_ACCESS]) {
			nodes[x][y - 1].access[PathfindingNode.TOP_ACCESS] = (reverse)
					? !prefab.adjacent[PathfindingNode.BOTTOM_ACCESS] : prefab.adjacent[PathfindingNode.BOTTOM_ACCESS];
		}

		if (y != nodesY - 1 && prefab.handleAdjacent[PathfindingNode.TOP_ACCESS]) {
			nodes[x][y + 1].access[PathfindingNode.BOTTOM_ACCESS] = (reverse)
					? !prefab.adjacent[PathfindingNode.TOP_ACCESS] : prefab.adjacent[PathfindingNode.TOP_ACCESS];
		}

		// boundries will still be maintained
		if (x == 0) {
			nodes[x][y].access[0] = false;
		}

		if (x == nodesX - 1) {
			nodes[x][y].access[1] = false;
		}

		if (y == 0) {
			nodes[x][y].access[2] = false;
		}

		if (y == nodesY - 1) {
			nodes[x][y].access[3] = false;
		}

		paths.clear();
		lastUpdate = System.currentTimeMillis();
	}

	public Rectangle getBounds() {

		return bounds;
	}

	public PathfindingNode gamePositionToNode(LummObject obj) {

		// outside of map bounds
		if (!getBounds().contains(obj.position.getX(), obj.position.getY()))
			return null;

		int nodeX = (int) ((obj.position.getX() - getBounds().x) / getNodeSize().x);
		int nodeY = (int) ((obj.position.getY() - getBounds().y) / getNodeSize().y);

		return nodes[nodeX][nodeY];

	}

	// endregion methods

}
