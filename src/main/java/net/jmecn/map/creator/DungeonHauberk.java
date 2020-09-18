package net.jmecn.map.creator;

import static net.jmecn.map.Tile.*;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import net.jmecn.map.Point;
import net.jmecn.map.Rect;

/// The random dungeon generator.
///
/// Starting with a stage of solid walls, it works like so:
///
/// 1. Place a number of randomly sized and positioned rooms. If a room
///    overlaps an existing room, it is discarded. Any remaining rooms are
///    carved out.
/// 2. Any remaining solid areas are filled in with mazes. The maze generator
///    will grow and fill in even odd-shaped areas, but will not touch any
///    rooms.
/// 3. The result of the previous two steps is a series of unconnected rooms
///    and mazes. We walk the stage and find every tile that can be a
///    "connector". This is a solid tile that is adjacent to two unconnected
///    regions.
/// 4. We randomly choose connectors and open them or place a door there until
///    all of the unconnected regions have been joined. There is also a slight
///    chance to carve a connector between two already-joined regions, so that
///    the dungeon isn't single connected. <NOTICE: This is spanning tree!>
/// 5. The mazes will have a lot of dead ends. Finally, we remove those by
///    repeatedly filling in any open tile that's closed on three sides. When
///    this is done, every corridor in a maze actually leads somewhere.
///
/// The end result of this is a multiply-connected dungeon with rooms and lots
/// of winding corridors.
///
/// @author https://github.com/munificent
/**
 * 
 * project: https://github.com/munificent/hauberk
 * article: http://journal.stuffwithstuff.com/2014/12/21/rooms-and-mazes/ demo
 * for this article： https://github.com/munificent/rooms-and-mazes
 * source:
 * https://github.com/munificent/hauberk/blob/master/lib/src/content/dungeon.dart
 * 
 * 
 * Another algorithm used in TinyKeep is similar to this one.
 * 
 * http://tinykeep.com/dungen/
 * 
 * https://www.reddit.com/r/gamedev/comments/1dlwc4/procedural_dungeon_generation_algorithm_explained/
 * 
 * https://github.com/adonaac/blog/issues/7
 * 
 * http://www.gamasutra.com/blogs/AAdonaac/20150903/252889/Procedural_Dungeon_Generation_Algorithm.php
 * 
 * Delaunay Triangulation + Graph
 * 
 * Read more:
 * 
 * https://en.wikipedia.org/wiki/Delaunay_triangulation
 * http://paulbourke.net/papers/triangulate/
 * http://www-cs-students.stanford.edu/~amitp/game-programming/polygon-map-generation/
 * https://en.wikipedia.org/wiki/Delaunay_tessellation_field_estimator
 * 
 * 
 * @author yanmaoyuan
 *
 */
public class DungeonHauberk extends MapCreator {

	static Point[] Direction = { new Point(1, 0), new Point(0, 1), new Point(-1, 0), new Point(0, -1) };
	int numRoomTries = 200;

	/// The inverse chance of adding a connector between two regions that have
	/// already been joined. Increasing this leads to more loosely connected
	/// dungeons.
	int extraConnectorChance = 20;// =>20;

	/// Increasing this allows rooms to be larger.
	int roomExtraSize;// =>0;

	int windingPercent = 45;// =>0;

	private List<Rect> rooms = new ArrayList<Rect>();

	/// For each open position in the dungeon, the index of the connected region
	/// that that position is a part of.
	private int[][] regions;

	/// The index of the current region being carved.
	private int currentRegion = -1;

	public DungeonHauberk(int width, int height) {
		super("creator.dungeon.hauberk", width, height);
	}

	@Override
	public void initialze() {
		if (width % 2 == 0 || height % 2 == 0) {
			// throw new RuntimeException("The map must be odd-sized.");
			return;
		}

		// init map
		map.fill(Wall);

		// init regions
		currentRegion = Unused;
		regions = new int[height][width];
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				regions[y][x] = Unused;
			}
		}

		// clear rooms
		rooms.clear();
	}

	@Override
	public void create() {
		if (width % 2 == 0 || height % 2 == 0) {
			// throw new RuntimeException("The map must be odd-sized.");
			return;
		}
		
		addRooms();

		addMazes();

		connectRegions();

		removeDeadEnds();

	}

	/**
	 * Places rooms ignoring the existing maze corridors.
	 */
	private void addRooms() {
		for (int i = 0; i < numRoomTries; i++) {
			// Pick a random room size. The funny math here does two things:
			// - It makes sure rooms are odd-sized to line up with maze.
			// - It avoids creating rooms that are too rectangular: too tall and
			// narrow or too wide and flat.
			// TODO: This isn't very flexible or tunable. Do something better
			// here.
			int size = nextInt(1, 3 + roomExtraSize) * 2 + 1;
			int rectangularity = nextInt(0, 1 + size / 2) * 2;
			int width = size;
			int height = size;
			if (nextInt(2) == 0) {
				width += rectangularity;
			} else {
				height += rectangularity;
			}

			int x = nextInt((this.width - width) / 2) * 2 + 1;
			int y = nextInt((this.height - height) / 2) * 2 + 1;

			Rect room = new Rect(x, y, width, height);

			boolean overlaps = false;
			for (Rect other : rooms) {
				if (room.overlap(other)) {
					overlaps = true;
					break;
				}
			}

			if (overlaps) {
				continue;
			}

			rooms.add(room);

			startRegion();
			for (int yy = y; yy < y + height; yy++) {
				for (int xx = x; xx < x + width; xx++) {
					carve(xx, yy);
				}
			}
		}
	}

	/**
	 * Fill in all of the empty space with mazes.
	 */
	private void addMazes() {
		// Fill in all of the empty space with mazes.
		for (int y = 1; y < height; y += 2) {
			for (int x = 1; x < width; x += 2) {
				Point pos = new Point(x, y);
				if (map.get(x, y) != Wall)
					continue;
				growMaze(pos);
			}
		}
	}

	/**
	 * Implementation of the "growing tree" algorithm from here:
	 * http://www.astrolog.org/labyrnth/algrithm.htm.
	 * 
	 * This is a general algorithm, capable of creating Mazes of different
	 * textures. It requires storage up to the size of the Maze. Each time you
	 * carve a cell, add that cell to a list. Proceed by picking a cell from the
	 * list, and carving into an unmade cell next to it. If there are no unmade
	 * cells next to the current cell, remove the current cell from the list.
	 * The Maze is done when the list becomes empty. The interesting part that
	 * allows many possible textures is how you pick a cell from the list. For
	 * example, if you always pick the most recent cell added to it, this
	 * algorithm turns into the recursive backtracker. If you always pick cells
	 * at random, this will behave similarly but not exactly to Prim's
	 * algorithm. If you always pick the oldest cells added to the list, this
	 * will create Mazes with about as low a "river" factor as possible, even
	 * lower than Prim's algorithm. If you usually pick the most recent cell,
	 * but occasionally pick a random cell, the Maze will have a high "river"
	 * factor but a short direct solution. If you randomly pick among the most
	 * recent cells, the Maze will have a low "river" factor but a long windy
	 * solution.
	 * 
	 * @param start
	 */
	private void growMaze(Point start) {
		LinkedList<Point> cells = new LinkedList<Point>();
		Point lastDir = null;

		startRegion();
		carve(start.x, start.y);

		cells.add(start);
		while (!cells.isEmpty()) {
			Point cell = cells.getLast();

			// See which adjacent cells are open.
			ArrayList<Point> unmadeCells = new ArrayList<Point>();
			for (Point dir : Direction) {
				if (canCarve(cell, dir))
					unmadeCells.add(dir);
			}

			if (!unmadeCells.isEmpty()) {
				// Based on how "windy" passages are, try to prefer carving in
				// the same direction.
				Point dir;
				if (unmadeCells.contains(lastDir) && nextInt(100) > windingPercent) {
					dir = lastDir;
				} else {
					int index = nextInt(unmadeCells.size());
					dir = unmadeCells.get(index);
				}

				// carve(cell + dir)
				carve(cell.x + dir.x, cell.y + dir.y);
				// carve(cell + dir*2)
				carve(cell.x + dir.x * 2, cell.y + dir.y * 2);

				cells.add(new Point(cell.x + dir.x * 2, cell.y + dir.y * 2));
				lastDir = dir;
			} else {
				// No adjacent uncarved cells.
				cells.removeLast();

				// This path has ended.
				lastDir = null;
			}
		}
	}

	class Graph {
		Edge[][] matrix;
		Graph(int vCnt) {
			matrix = new Edge[vCnt][vCnt];
		}
		
		Edge get(int x, int y) {
			return matrix[x][y];
		}
		
		void union(int x, int y, Point p) {
			if (matrix[x][y] == null) {
				Edge e = new Edge();
				matrix[x][y] = matrix[y][x] = e;
				
				if (x > y) {
					e.a = y;
					e.b = x;
				} else {
					e.a = x;
					e.b = y;
				}
			}
			matrix[x][y].add(p);
		}
		
	}
	
	class Edge {
		int a, b;
		ArrayList<Point> points;
		
		Edge() {
			points = new ArrayList<Point>();
		}

		void add(Point p) {
			points.add(p);
		}
		
		Point rand() {
			int index = nextInt(points.size());
			return points.get(index);
		}

	}
	
	private void connectRegions() {
		
		// Find all of the tiles that can connect two (or more) regions.
		Graph graph = new Graph(currentRegion+1);
		for (int y = 1; y < height - 1; y++) {
			for (int x = 1; x < width - 1; x++) {
				// Can't already be part of a region.
				if (map.get(x, y) != Wall)
					continue;

				int a = -1;
				int b = -1;
				for (Point dir : Direction) {
					int region = regions[y+dir.y][x+dir.x];
					if (region != Unused) {
						
						if (a == -1) {
							a = region;
						} else {
							if (b == -1 && a != region) {
								b = region;
							}
						}
					}
				}

				if (a != -1 && b != -1) {
					graph.union(a, b, new Point(x, y));
				}
			}
		}

		// Keep track of which regions have been merged. This maps an original
		// region index to the one it has been merged to.
		List<Integer> openRegions = new ArrayList<Integer>();
		for (int i = 0; i <= currentRegion; i++) {
			openRegions.add(i);
		}
		
		int len = openRegions.size();
		int root = openRegions.get(nextInt(len));
		openRegions.remove(new Integer(root));
		
		List<Integer> tree = new ArrayList<Integer>();
		tree.add(root);
		
		// Keep connecting regions until we're down to one.
		while (openRegions.size() > 0) {
			
			len = tree.size();
			root = tree.get(nextInt(len));
			
			List<Edge> edges = new ArrayList<Edge>();
			len = openRegions.size();
			for(int i=0; i<len; i++) {
				int b = openRegions.get(i);
				Edge e = graph.get(root, b);
				if (e != null) {
					edges.add(e);
				}
			}
//			len = currentRegion + 1;
//			for(int i=0; i<=currentRegion; i++) {
//				if (i==root)
//					continue;
//				
//				Edge e = graph.get(root, i);
//				if (e != null) {
//					if (e.points.size() > 0)
//						edges.add(e);
//				}
//			}
			
			if (edges.size() == 0) {
				continue;
			}
			else {
				len = edges.size();
				Edge e = edges.get(nextInt(len));
				
				int b = e.b;
				if (b == root) b = e.a;
				
				
				if (openRegions.remove(new Integer(b))) {
					tree.add(b);
//				} else {
//					if (nextInt(extraConnectorChance) != 0) {
//						continue;
//					}
				}
				
				List<Point> points = e.points;
				// Carve the connection.
				len = points.size();
				Point p = points.get(nextInt(len));
				addJunction(p);
				points.clear();
			}

		}
	}

	private void addJunction(Point pos) {
		map.set(pos.x, pos.y, Door);
	}

	private void removeDeadEnds() {
		boolean done = false;

		while (!done) {
			done = true;

			for (int y = 1; y < height - 1; y++) {
				for (int x = 1; x < width - 1; x++) {
					if (map.get(x, y) == Wall)
						continue;

					// If it only has one exit, it's a dead end.
					int exits = 0;
					for (Point dir : Direction) {
						if (map.get(x + dir.x, y + dir.y) != Wall)
							exits++;
					}

					if (exits != 1)
						continue;

					done = false;
					map.set(x, y, Wall);
				}
			}
		}
	}

	/**
	 * Gets whether or not an opening can be carved from the given starting
	 * [Cell] at [pos] to the adjacent Cell facing [direction]. Returns `true`
	 * if the starting Cell is in bounds and the destination Cell is filled (or
	 * out of bounds).</returns>
	 * 
	 * @param pos
	 * @param direction
	 * @return
	 */
	private boolean canCarve(Point pos, Point direction) {
		// Must end in bounds.
		int x = pos.x + direction.x * 3;
		int y = pos.y + direction.y * 3;
		if (!map.contains(x, y))
			return false;

		// Destination must not be open.
		x = pos.x + direction.x * 2;
		y = pos.y + direction.y * 2;
		return map.get(x, y) == Wall;
	}

	private void startRegion() {
		currentRegion++;
	}

	private void carve(int x, int y) {
		map.set(x, y, Floor);
		regions[y][x] = currentRegion;
	}

}
