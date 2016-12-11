package net.jmecn.map.creator;

import static net.jmecn.map.Tile.*;

import java.util.ArrayList;
import java.util.List;

import net.jmecn.map.Point;

public class Building extends MapCreator {

	private int iterations;
	final static private int MIN = 3;

	public Building(int width, int height) {
		super("creator.building", width, height);
		this.iterations = 10;
	}

	public void setIterations(int iterations) {
		this.iterations = iterations;
	}

	@Override
	public void initialze() {
		map.fill(Floor);
	}

	@Override
	public void create() {
		Room root = new Room(0, 0, width, height);
		split(root, 0);
		buildWalls(root);
		buildDoors(root);
		map.buildBoundary(Wall);
	}

	private void buildDoors(Room room) {
		if (room.getLeft() != null && room.getRight() != null) {
			List<Point> points = new ArrayList<Point>();
			if (room.isSplitVert()) {
				int x = room.getSplitX();
				for (int y = room.getY(); y < room.getY() + room.getHeight(); y++) {
					if (map.get(x + 1, y) == Floor && map.get(x - 1, y) == Floor && y != 0 && y != height - 1) {
						points.add(new Point(x, y));
					}
				}
			} else {
				int y = room.getSplitY();
				for (int x = room.getX(); x < room.getX() + room.getWidth(); x++) {
					if (map.get(x, y + 1) == Floor && map.get(x, y - 1) == Floor && x != 0 && x != width - 1) {
						points.add(new Point(x, y));
					}
				}
			}

			Point selection = points.get(nextInt(points.size()));
			map.set(selection.x, selection.y, Door);

			buildDoors(room.getLeft());
			buildDoors(room.getRight());
		}
	}

	private void buildWalls(Room room) {
		if (room.getLeft() != null && room.getRight() != null) {
			if (room.isSplitVert()) {
				int x = room.getSplitX();
				for (int y = room.getY(); y < room.getY() + room.getHeight(); y++) {
					map.set(x, y, Wall);
				}
			} else {
				int y = room.getSplitY();
				for (int x = room.getX(); x < room.getX() + room.getWidth(); x++) {
					map.set(x, y, Wall);
				}
			}

			buildWalls(room.getLeft());
			buildWalls(room.getRight());
		}
	}

	private void split(Room room, int iteration) {
		int limit = 2 * MIN + 1;
		if (iteration < iterations && room.getWidth() > limit && room.getHeight() > limit) {
			boolean splitVert = nextBoolean();

			Room leftOrTop;
			Room rightOrBottom;
			int splitPoint;
			if (splitVert) {
				splitPoint = MIN + room.getX() + nextInt(room.getWidth() - limit);
				leftOrTop = new Room(room.getX(), room.getY(), splitPoint - room.getX(), room.getHeight());
				rightOrBottom = new Room(splitPoint, room.getY(), room.getX() + room.getWidth() - splitPoint,
						room.getHeight());
				room.setSplitX(splitPoint);
				room.setSplitY(room.getHeight());
			} else {
				splitPoint = MIN + room.getY() + nextInt(room.getHeight() - limit);
				leftOrTop = new Room(room.getX(), room.getY(), room.getWidth(), splitPoint - room.getY());
				rightOrBottom = new Room(room.getX(), splitPoint, room.getWidth(),
						room.getY() + room.getHeight() - splitPoint);
				room.setSplitX(room.getWidth());
				room.setSplitY(splitPoint);
			}

			room.setSplitVert(splitVert);
			room.setLeft(leftOrTop);
			room.setRight(rightOrBottom);

			iteration++;
			split(leftOrTop, iteration);
			split(rightOrBottom, iteration);
		}
	}

	private static class Room {
		int x, y, width, height, splitX, splitY;
		boolean splitVert;
		Room left, right;

		public Room(int x, int y, int width, int height) {
			this.x = x;
			this.y = y;
			this.width = width;
			this.height = height;
			splitVert = false;
			left = null;
			right = null;
			splitX = -1;
			splitY = -1;
		}

		public void setSplitVert(boolean splitVert) {
			this.splitVert = splitVert;
		}

		public int getX() {
			return x;
		}

		public int getY() {
			return y;
		}

		public int getWidth() {
			return width;
		}

		public int getHeight() {
			return height;
		}

		public int getSplitX() {
			return splitX;
		}

		public int getSplitY() {
			return splitY;
		}

		public boolean isSplitVert() {
			return splitVert;
		}

		public Room getLeft() {
			return left;
		}

		public Room getRight() {
			return right;
		}

		public void setRight(Room right) {
			this.right = right;
		}

		public void setLeft(Room left) {
			this.left = left;
		}

		public void setSplitY(int splitY) {
			this.splitY = splitY;
		}

		public void setSplitX(int splitX) {
			this.splitX = splitX;
		}
	}
}
