package net.jmecn.map;

public class Point {
	public int x;
	public int y;
	public int tile;

	public Point() {
		x = y = 0;
		tile = Tile.Unused;
	}

	public Point(int x, int y) {
		this.x = x;
		this.y = y;
		this.tile = Tile.Unused;
	}

	public Point(int x, int y, int tile) {
		this.x = x;
		this.y = y;
		this.tile = tile;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public int getTile() {
		return tile;
	}

	public boolean equals(Object o) {
		if (o == null)
			return false;

		if (o instanceof Point) {
			Point p = (Point) o;
			return (p.x == x && p.y == y);
		}

		return false;
	}

	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + x;
		result = prime * result + y;
		return result;
	}
	
	@Override
	public String toString() {
		return "Point(" + x + ", " + y + ")";
	}
}