package net.jmecn.map.creator;

import static net.jmecn.map.Tile.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import net.jmecn.map.Point;

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
///    the dungeon isn't single connected.
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
 * 
 * article: http://journal.stuffwithstuff.com/2014/12/21/rooms-and-mazes/ demo
 * for this article： https://github.com/munificent/rooms-and-mazes
 * 
 * source:
 * https://github.com/munificent/hauberk/blob/db360d9efa714efb6d937c31953ef849c7394a39/lib/src/content/dungeon.dart
 * 
 * 
 * 
 * 
 * 
 * @author yanmaoyuan
 *
 */
public class DungeonHauberk extends MapCreator {

	class Rect {
		int x, y, width, height;

		public Rect() {
			x = y = width = height = 0;
		}

		public Rect(int x, int y, int width, int height) {
			this.x = x;
			this.y = y;
			this.width = width;
			this.height = height;
		}

		boolean valueInRange(int value, int min, int max) {
			return (value <= max) && (value >= min);
		}

		boolean overlap(Rect B) {
			boolean xOverlap = valueInRange(x, B.x, B.x + B.width) || valueInRange(B.x, x, x + width);

			boolean yOverlap = valueInRange(y, B.y, B.y + B.height) || valueInRange(B.y, y, y + height);

			return xOverlap && yOverlap;
		}

	}

	static Point[] Direction = { new Point(1, 0), new Point(0, 1), new Point(-1, 0), new Point(0, -1) };
	int numRoomTries;

	/// The inverse chance of adding a connector between two regions that have
	/// already been joined. Increasing this leads to more loosely connected
	/// dungeons.
	int extraConnectorChance;// =>20;

	/// Increasing this allows rooms to be larger.
	int roomExtraSize;// =>0;

	int windingPercent;// =>0;

	private List<Rect> rooms = new ArrayList<Rect>();

	/// For each open position in the dungeon, the index of the connected region
	/// that that position is a part of.
	private Integer[][] regions;

	/// The index of the current region being carved.
	private int currentRegion = -1;

	public DungeonHauberk(int width, int height) {
		super("creator.dungeon.hauberk", width, height);
	}

	@Override
	public void initialze() {
		if (width % 2 == 0 || height % 2 == 0) {
			throw new RuntimeException("The map must be odd-sized.");
		}

		map.fill(Wall);

	}

	@Override
	public void create() {
		regions = new Integer[height][width];

		addRooms();

		// Fill in all of the empty space with mazes.
		for (int y = 1; y < height; y += 2) {
			for (int x = 1; x < width; x += 2) {
				Point pos = new Point(x, y);
				if (map.get(x, y) != Wall)
					continue;
				_growMaze(pos);
			}
		}

		connectRegions();
		removeDeadEnds();

	}

	/// Implementation of the "growing tree" algorithm from here:
	/// http://www.astrolog.org/labyrnth/algrithm.htm.
	void _growMaze(Point start) {
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
				// the
				// same direction.
				Point dir;
				if (unmadeCells.contains(lastDir) && nextInt(100) > windingPercent) {
					dir = lastDir;
				} else {
					int index = nextInt(unmadeCells.size());
					dir = unmadeCells.get(index);
				}

				carve(cell.x + dir.x, cell.y + dir.y);
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

	/// Places rooms ignoring the existing maze corridors.
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

			if (overlaps)
				continue;

			rooms.add(room);

			startRegion();
			for (int yy = y; yy < height; yy++) {
				for (int xx = x; xx < width; xx++) {
					carve(xx, yy);
				}
			}
		}
	}

	private void connectRegions() {
		// Find all of the tiles that can connect two (or more) regions.
		HashMap<Point, Set<Integer>> connectorRegions = new HashMap<Point, Set<Integer>>();

		for (int y = 1; y < height - 1; y++) {
			for (int x = 1; x < width - 1; x++) {
				// Can't already be part of a region.
				if (map.get(x, y) != Wall)
					continue;

				Set<Integer> regionss = new TreeSet<Integer>();
				for (Point dir : Direction) {
					int a = x + dir.x;
					int b = y + dir.y;
					Integer region = regions[b][a];
					if (region != null)
						regionss.add(region);
				}

				if (regionss.size() < 2)
					continue;

				Point pos = new Point(x, y);
				connectorRegions.put(pos, regionss);
			}
		}

		List<Point> connectors = new ArrayList<Point>();
		connectors.addAll(connectorRegions.keySet());

		// Keep track of which regions have been merged. This maps an original
		// region index to the one it has been merged to.
		int[] merged = new int[currentRegion];
		Set<Integer> openRegions = new TreeSet<Integer>();
		for (int i = 0; i <= currentRegion; i++) {
			merged[i] = i;
			openRegions.add(i);
		}

		// Keep connecting regions until we're down to one.
		while (openRegions.size() > 1) {
			Point connector = connectors.get(nextInt(connectors.size()));

			// Carve the connection.
			addJunction(connector);

			// Merge the connected regions. We'll pick one region (arbitrarily)
			// and
			// map all of the other regions to its index.
			List<Integer> regions = new ArrayList<Integer>();
			for (Integer region : connectorRegions.get(connector)) {
				if (merged[region] != 0) {
					regions.add(region);
				}
			}

			Integer dest = regions.get(0);
			List<Integer> sources = regions.subList(1, regions.size() - 1);

			// Merge all of the affected regions. We have to look at *all* of
			// the
			// regions because other regions may have previously been merged
			// with
			// some of the ones we're merging now.
			for (int i = 0; i <= currentRegion; i++) {
				if (sources.contains(merged[i])) {
					merged[i] = dest;
				}
			}

			// The sources are no longer in use.
			openRegions.removeAll(sources);

			// Remove any connectors that aren't needed anymore.

			Iterator<Point> it = connectors.iterator();
			while (it.hasNext()) {
				Point pos = it.next();
				// Don't allow connectors right next to each other.
				int dx = connector.x - pos.x;
				int dy = connector.y - pos.y;
				if (Math.sqrt(dx * dx + dy * dy) < 2)
					it.remove();

				// If the connector no long spans different regions, we don't
				// need it.
				regions.clear();
				for (Integer region : connectorRegions.get(pos)) {
					if (merged[region] != 0) {
						regions.add(region);
					}
				}

				if (regions.size() > 1)
					continue;

				// This connecter isn't needed, but connect it occasionally so
				// that the
				// dungeon isn't singly-connected.
				if (nextInt(extraConnectorChance) == 0)
					addJunction(pos);

				it.remove();

			}
		}
	}

	private void addJunction(Point pos) {
		if (nextInt(4) == 0) {
			map.set(pos.x, pos.y, nextInt(3) == 0 ? Door : Floor);
		} else {
			map.set(pos.x, pos.y, Door);
		}
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

	/// Gets whether or not an opening can be carved from the given starting
	/// [Cell] at [pos] to the adjacent Cell facing [direction]. Returns `true`
	/// if the starting Cell is in bounds and the destination Cell is filled
	/// (or out of bounds).</returns>
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
