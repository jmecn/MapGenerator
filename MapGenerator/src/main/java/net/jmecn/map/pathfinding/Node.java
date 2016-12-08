package net.jmecn.map.pathfinding;

import java.util.LinkedList;

import net.jmecn.map.Point;

public class Node implements Comparable<Node> {
	public Point _Pos; // position of the node
	public int sourcePoint;
	public int destiPoint;
	// public Node _parentnode; //the parent node
	public Node _parentnode;

	int col;
	int row;

	// initialize the NOde
	public Node(Point _Pos, int col, int row) {
		this._Pos = _Pos;
		this.col = col;
		this.row = row;
	}

	// get the cost of the Path
	public int GetCost(Node node) {
		int m = node._Pos.x - _Pos.x;
		int n = node._Pos.y - _Pos.y;
		return (int) Math.sqrt(m * m + n * n);
	}

	// check if the node is the destination point
	public boolean equals(Object node) {
		if (_Pos.x == ((Node) node)._Pos.x && _Pos.y == ((Node) node)._Pos.y) {
			return true;
		}
		return false;
	}

	// get the minist cost
	public int compareTo(Node node) {
		int a1 = sourcePoint + destiPoint;
		int a2 = ((Node) node).sourcePoint + ((Node) node).destiPoint;
		if (a1 < a2) {
			return -1;
		} else if (a1 == a2) {
			return 0;
		} else
			return 1;

	}

	public LinkedList<Node> getLimit() {
		LinkedList<Node> limit = new LinkedList<Node>();
		int x = _Pos.x;
		int y = _Pos.y;
		if (y > 0) {
			limit.add(new Node(new Point(x, y - 1), col, row)); // up
		}
		if (y < row) {
			limit.add(new Node(new Point(x, y + 1), col, row)); // down
		}
		if (x > 0) {
			limit.add(new Node(new Point(x - 1, y), col, row)); // left
		}
		if (x < col) {
			limit.add(new Node(new Point(x + 1, y), col, row)); // right
		}
		return limit;
	}

}
