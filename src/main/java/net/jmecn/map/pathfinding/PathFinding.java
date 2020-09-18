package net.jmecn.map.pathfinding;

import java.util.LinkedList;
import java.util.List;

import net.jmecn.map.Point;

public class PathFinding {
	
	private OpenedList openedList;
	private LinkedList<Node> closedList;
	private int[][] _map;
	private int[] _limit;

	private int col;
	private int row;

	public PathFinding(int[][] map, int[] limit) {
		_map = map;
		_limit = limit;

		col = map[0].length - 1;
		row = map.length - 1;

		openedList = new OpenedList();
		closedList = new LinkedList<Node>();

	}

	public List<Node> searchPath(Point startPos, Point destiPos) {
		Node startNode = new Node(startPos, col, row);
		Node destiNode = new Node(destiPos, col, row);
		startNode.sourcePoint = 0;
		startNode.destiPoint = startNode.GetCost(destiNode);
		startNode._parentnode = null;
		openedList.add(startNode);
		while (!openedList.isEmpty()) {
			// remove the initialized component
			Node firstNode = (Node) openedList.removeFirst();
			// check the equality
			if (firstNode.equals(destiNode)) {
				//
				return makePath(firstNode);
			} else {
				//
				// add to the closedList
				closedList.add(firstNode);
				// get the mobile area of firstNode
				LinkedList<Node> _limit = firstNode.getLimit();
				// visit
				for (int i = 0; i < _limit.size(); i++) {
					// get the adjacent node
					Node neighborNode = (Node) _limit.get(i);
					//
					boolean isOpen = openedList.contains(neighborNode);
					// check if it can work
					boolean isClosed = closedList.contains(neighborNode);
					//
					boolean isHit = isHit(neighborNode._Pos.x, neighborNode._Pos.y);
					// all of them are negative
					if (!isOpen && !isClosed && !isHit) {
						// set the costFromStart
						neighborNode.sourcePoint = firstNode.sourcePoint + 1;
						// set the costToObject
						neighborNode.destiPoint = neighborNode.GetCost(destiNode);
						// change the neighborNode's parent nodes
						neighborNode._parentnode = firstNode;
						// add to level
						openedList.add(neighborNode);
					}
				}
			}

		}
		// clear the data
		openedList.clear();
		closedList.clear();
		//
		return null;
	}

	private LinkedList<Node> makePath(Node node) {
		LinkedList<Node> path = new LinkedList<Node>();
		while (node._parentnode != null) {
			path.addFirst(node);
			node = node._parentnode;
		}
		path.addFirst(node);
		return path;
	}

	private boolean isHit(int x, int y) {
		for (int i = 0; i < _limit.length; i++) {
			if (_map[y][x] == _limit[i]) {
				return true;
			}
		}
		return false;
	}

	private class OpenedList extends LinkedList<Node> {
		private static final long serialVersionUID = 1L;

		public boolean add(Node node) {
			for (int i = 0; i < size(); i++) {
				if (node.compareTo(get(i)) <= 0) {
					add(i, node);
					return false;
				}
			}
			addLast(node);

			return true;
		}
	}
}