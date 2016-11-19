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
import java.util.Collections;
import java.util.Comparator;
import com.badlogic.gdx.math.Vector2;

/**
 * Generates a route for pathfinding in a map, from a source node to a target
 * node.
 * 
 * @author Claudiu Bele
 */
public class PathfindingRoute {

	// region fields

	private PathfindingMap map;

	public ArrayList<NodeData> nodesToTarget;

	private ArrayList<NodeData> openList;

	private ArrayList<NodeData> closedList;

	private NodeData[][] nodeData;

	@SuppressWarnings("unused")
	private PathfindingNode source;

	private PathfindingNode target;

	private PathfindingNode current;

	public static class Path {

		public ArrayList<Vector2> nodes;

		public Vector2 source;

		public Vector2 target;

		public Path(ArrayList<Vector2> nodes, PathfindingMap map, boolean addToList) {

			this.nodes = nodes;
			if (nodes.size() != 0) {
				this.source = nodes.get(0);
				this.target = nodes.get(nodes.size() - 1);
				if (addToList) {
					map.paths.add(this);
				}
			}
		}

		public Path(Vector2 source, Vector2 target, PathfindingMap map) {

			this.source = source;
			this.target = target;
			this.nodes = new ArrayList<Vector2>();
			map.paths.add(this);
		}

		private boolean isSubPath(Vector2 source, Vector2 target) {

			if ((nodes.contains(source) || this.source.equals(source)) && this.target.equals(target))
				return true;

			return false;
		}

		private ArrayList<Vector2> getSubPath(Vector2 source, Vector2 target) {

			if (this.source.equals(source) && this.target.equals(target))
				return this.nodes;

			if (isSubPath(source, target)) {
				int startIndex = nodes.indexOf(source);
				int endIndex = nodes.indexOf(target);

				// if start index is after endindex ( path is reversed )
				if (startIndex > endIndex) {

					// this doesn't work for paths containing ledges.
					return new ArrayList<Vector2>();
				} else {
					ArrayList<Vector2> toReturn = new ArrayList<Vector2>(nodes.subList(startIndex, endIndex + 1));
					return toReturn;
				}

			}
			return new ArrayList<Vector2>();
		}
	}

	public static class NodeData {

		public PathfindingNode node;

		public NodeData parent;

		public int x, y;

		public int distanceFromSource;

		public int distanceToTarget;

		public NodeData(PathfindingNode node, PathfindingNode target) {

			this.node = node;
			this.x = node.x;
			this.y = node.y;
			this.distanceToTarget = getDistanceToTarget(target);
		}

		public int getOverallDistance() {

			return distanceFromSource + distanceToTarget;
		}

		public void setParent(NodeData node) {

			this.parent = node;
			this.distanceFromSource = getDistanceFrom(parent);

		}

		public int getDistanceFrom(NodeData node) {

			if (node == null) {
				return 0;
			} else {
				// px = 0 , x = 0 true != py = 0 , y = -1 false => true
				// px = 0, x = 0 true != (py = 0, y=0) true => false
				// px = 0 , x = 1 false != py = 0 , y =1, false => false
				if ((node.x == this.x) != (node.y == this.y)) {
					return 10 + node.distanceFromSource;
				} else {
					return 14 + node.distanceFromSource;
				}
			}
		}

		public int getDistanceToTarget(PathfindingNode target) {

			return Math.abs(this.x - target.x) * 10 + Math.abs(this.y - target.y) * 10;
		}

		@Override
		public String toString() {

			return x + "," + y;
		}

	}

	// endregion fields

	// region constructors

	public PathfindingRoute(PathfindingMap map) {

		this.map = map;
		openList = new ArrayList<NodeData>();
		closedList = new ArrayList<NodeData>();
		nodeData = new NodeData[map.nodesX][map.nodesY];
	}

	// endregion constructors

	// region methods

	public Path getPath(PathfindingNode source, PathfindingNode target) {

		// region check if data is faulty or if it's impossible to make a path
		if (source == null) {
			return new Path(new ArrayList<Vector2>(), map, false);
		}

		if (target == null) {
			return new Path(new ArrayList<Vector2>(), map, false);
		}
		if (source.x == target.x && source.y == target.y) {
			return new Path(new ArrayList<Vector2>(), map, false);
		}

		if (source.access[0] == false && source.access[1] == false && source.access[2] == false
				&& source.access[3] == false) {
			return new Path(source.getNodePosition(), target.getNodePosition(), map);
		}
		if (target.access[0] == false && target.access[1] == false && target.access[2] == false
				&& target.access[3] == false) {
			return new Path(source.getNodePosition(), target.getNodePosition(), map);
		}
		// endregion

		openList.clear();
		closedList.clear();
		nodeData = new NodeData[map.nodesX][map.nodesY];

		for (int i = 0; i < map.paths.size(); i++) {
			if (map.paths.get(i).isSubPath(new Vector2(source.x, source.y), new Vector2(target.x, target.y))) {

				Path newPath = new Path(
						map.paths.get(i).getSubPath(new Vector2(source.x, source.y), new Vector2(target.x, target.y)),
						map, true);
				if (newPath.nodes.size() != 0)
					return newPath;
			}
		}

		this.source = source;
		this.target = target;

		current = source;
		nodeData[current.x][current.y] = new NodeData(current, target);
		nodeData[current.x][current.y].setParent(null);
		do {

			addNearbyNodes(nodeData[current.x][current.y]);
			openList.remove(nodeData[current.x][current.y]);
			closedList.add(nodeData[current.x][current.y]);
			Collections.sort(openList, new Comparator<NodeData>() {

				@Override
				public int compare(NodeData o1, NodeData o2) {

					return o1.getOverallDistance() - o2.getOverallDistance();
				}
			});
			if (openList.size() != 0)
				current = openList.get(0).node;

		} while (!current.equals(target) && openList.size() != 0);

		if (!current.equals(target)) {
			return new Path(source.getNodePosition(), target.getNodePosition(), map);

		}

		ArrayList<Vector2> temp = new ArrayList<Vector2>();

		do {
			temp.add(new Vector2(current.x, current.y));
			current = nodeData[current.x][current.y].parent.node;

		} while (nodeData[current.x][current.y].parent != null);

		temp.add(new Vector2(source.x, source.y));
		Collections.reverse(temp);

		Path pathToReturn = new Path(temp, map, true);
		return pathToReturn;
	}

	private void addNearbyNodes(NodeData current) {

		// left
		if (current.node.access[PathfindingNode.LEFT_ACCESS] == true) {

			updateAdjacentNode(current, -1, 0);

			// left bottom
			if (current.node.access[PathfindingNode.BOTTOM_ACCESS] == true
					&& map.nodes[current.x - 1][current.y].access[PathfindingNode.BOTTOM_ACCESS] == true
					&& map.nodes[current.x][current.y - 1].access[PathfindingNode.LEFT_ACCESS] == true) {
				updateAdjacentNode(current, -1, -1);
			}
			// left top
			if (current.node.access[PathfindingNode.TOP_ACCESS] == true
					&& map.nodes[current.x - 1][current.y].access[PathfindingNode.TOP_ACCESS] == true
					&& map.nodes[current.x][current.y + 1].access[PathfindingNode.LEFT_ACCESS] == true) {
				updateAdjacentNode(current, -1, 1);

			}
		}

		// right
		if (current.node.access[PathfindingNode.RIGHT_ACCESS] == true) {

			updateAdjacentNode(current, 1, 0);
			// right bottom
			if (current.node.access[PathfindingNode.BOTTOM_ACCESS] == true
					&& map.nodes[current.x + 1][current.y].access[PathfindingNode.BOTTOM_ACCESS] == true
					&& map.nodes[current.x][current.y - 1].access[PathfindingNode.RIGHT_ACCESS] == true) {
				updateAdjacentNode(current, 1, -1);
			}
			// right top
			if (current.node.access[PathfindingNode.TOP_ACCESS] == true
					&& map.nodes[current.x + 1][current.y].access[PathfindingNode.TOP_ACCESS] == true
					&& map.nodes[current.x][current.y + 1].access[PathfindingNode.RIGHT_ACCESS] == true) {
				updateAdjacentNode(current, 1, 1);

			}
		}
		// down
		if (current.node.access[PathfindingNode.BOTTOM_ACCESS] == true) {
			updateAdjacentNode(current, 0, -1);
		}
		// up
		if (current.node.access[PathfindingNode.TOP_ACCESS] == true) {
			updateAdjacentNode(current, 0, 1);

		}

	}

	private void updateAdjacentNode(NodeData from, int xOffset, int yOffset) {

		int x = from.x + xOffset;
		int y = from.y + yOffset;

		// hasn't been accessed yet
		if (nodeData[x][y] == null) {
			nodeData[x][y] = new NodeData(map.nodes[x][y], target);
			nodeData[x][y].setParent(from);

			openList.add(nodeData[x][y]);
		}
		// has been accessed before
		else {
			// if it is already in the close list, don't handle it
			if (closedList.contains(nodeData[x][y]))
				return;

			// if overall distance is more than what it would be with a new
			// parent, do that.
			if (nodeData[x][y].getOverallDistance() > nodeData[x][y].getDistanceFrom(from)
					+ nodeData[x][y].getDistanceToTarget(target)) {
				nodeData[x][y].setParent(from);
			}

		}
	}

	// endregion methods
}
