package net.jmecn.map.creator;

import static net.jmecn.map.Tile.*;

import java.util.logging.Level;
import java.util.logging.Logger;

import static net.jmecn.map.Direction.*;

/**
 * Tyrant's dungeon generate algorithm
 * 
 * https://sourceforge.net/projects/tyrant/
 * 
 * @author yanmaoyuan
 *
 */
public class DungeonTyrant extends MapCreator {

	static Logger logger = Logger.getLogger(DungeonTyrant.class.getName());

	final static int ChanceRoom = 50; // corridorChance = 100 - roomChance
	static final int minRoomSize = 3;
	static final int maxRoomSize = 6;

	static final int minCorridorLength = 3;
	static final int maxCorridorLength = 6;

	private int maxFeatures = 100;

	public DungeonTyrant(int width, int height) {
		super("creator.dungeon.tyrant", width, height);
	}

	@Override
	public void initialze() {
		map.fill(Unused);
	}

	@Override
	public void create() {
		// Make one room in the middle to start things off.
		int randomDir = nextInt(4);
		makeRoom(width / 2, height / 2, 16, 12, randomDir);

		for (int features = 1; features != maxFeatures; ++features) {
			if (!makeFeature()) {
				break;
			}
		}

		if (!makeStairs(UpStairs))
			logger.log(Level.WARNING, "Unable to place up stairs.");

		if (!makeStairs(DownStairs))
			logger.log(Level.WARNING, "Unable to place down stairs.");
	}

	public void setMaxFeatures(int maxFeatures) {
		this.maxFeatures = maxFeatures;
	}

	boolean makeCorridor(int x, int y, int maxLength, int direction) {
		assert (x >= 0 && x < width);
		assert (y >= 0 && y < height);

		assert (maxLength > 0 && maxLength <= Math.max(width, height));

		int length = nextInt(2, maxLength);

		int xStart = x;
		int yStart = y;

		int xEnd = x;
		int yEnd = y;

		if (direction == North)
			yStart = y - length;
		else if (direction == East)
			xEnd = x + length;
		else if (direction == South)
			yEnd = y + length;
		else if (direction == West)
			xStart = x - length;

		if (!map.isXInBounds(xStart) || !map.isXInBounds(xEnd) || !map.isYInBounds(yStart) || !map.isYInBounds(yEnd))
			return false;

		if (!map.isAreaUnused(xStart, yStart, xEnd, yEnd))
			return false;

		map.setCells(xStart, yStart, xEnd, yEnd, Corridor);

		return true;
	}

	boolean makeRoom(int x, int y, int xMaxLength, int yMaxLength, int direction) {
		// Minimum room size of 4x4 tiles (2x2 for walking on, the rest is
		// walls)
		int xLength = nextInt(4, xMaxLength);
		int yLength = nextInt(4, yMaxLength);

		int xStart = x;
		int yStart = y;

		int xEnd = x;
		int yEnd = y;

		if (direction == North) {
			yStart = y - yLength;
			xStart = x - xLength / 2;
			xEnd = x + (xLength + 1) / 2;
		} else if (direction == East) {
			yStart = y - yLength / 2;
			yEnd = y + (yLength + 1) / 2;
			xEnd = x + xLength;
		} else if (direction == South) {
			yEnd = y + yLength;
			xStart = x - xLength / 2;
			xEnd = x + (xLength + 1) / 2;
		} else if (direction == West) {
			yStart = y - yLength / 2;
			yEnd = y + (yLength + 1) / 2;
			xStart = x - xLength;
		}

		if (!map.isXInBounds(xStart) || !map.isXInBounds(xEnd) || !map.isYInBounds(yStart) || !map.isYInBounds(yEnd))
			return false;

		if (!map.isAreaUnused(xStart, yStart, xEnd, yEnd))
			return false;

		map.setCells(xStart, yStart, xEnd, yEnd, Wall);
		map.setCells(xStart + 1, yStart + 1, xEnd - 1, yEnd - 1, Floor);

		return true;
	}

	boolean makeFeature(int x, int y, int xmod, int ymod, int direction) {
		// Choose what to build
		int chance = nextInt(0, 100);

		if (chance <= ChanceRoom) {
			if (makeRoom(x + xmod, y + ymod, 8, 6, direction)) {
				map.set(x, y, Door);

				// Remove wall next to the door.
				map.set(x + xmod, y + ymod, Floor);

				return true;
			}

			return false;
		} else {
			if (makeCorridor(x + xmod, y + ymod, 6, direction)) {
				map.set(x, y, Door);
				return true;
			}

			return false;
		}
	}

	boolean makeFeature() {
		int maxTries = 1000;

		for (int tries = 0; tries != maxTries; ++tries) {
			// Pick a random wall or corridor tile.
			// Make sure it has no adjacent doors (looks weird to have doors
			// next to each other).
			// Find a direction from which it's reachable.
			// Attempt to make a feature (room or corridor) starting at this
			// point.

			int x = nextInt(1, width - 2);
			int y = nextInt(1, height - 2);

			if (map.get(x, y) != Wall && map.get(x, y) != Corridor)
				continue;

			if (map.isAdjacent(x, y, Door))
				continue;

			if (map.get(x, y + 1) == Floor || map.get(x, y + 1) == Corridor) {
				if (makeFeature(x, y, 0, -1, North))
					return true;
			} else if (map.get(x - 1, y) == Floor || map.get(x - 1, y) == Corridor) {
				if (makeFeature(x, y, 1, 0, East))
					return true;
			} else if (map.get(x, y - 1) == Floor || map.get(x, y - 1) == Corridor) {
				if (makeFeature(x, y, 0, 1, South))
					return true;
			} else if (map.get(x + 1, y) == Floor || map.get(x + 1, y) == Corridor) {
				if (makeFeature(x, y, -1, 0, West))
					return true;
			}
		}

		return false;
	}

	boolean makeStairs(int tile) {
		int tries = 0;
		int maxTries = 10000;

		for (; tries != maxTries; ++tries) {
			int x = nextInt(1, width - 2);
			int y = nextInt(1, height - 2);

			int t = map.get(x, y);
			if (!map.isAdjacent(x, y, Floor) || t  == Wall || t  == Door || map.isAdjacent(x, y, Door))
				continue;

			map.set(x, y, tile);

			return true;
		}

		return false;
	}

	public static void main(String[] args) {
		DungeonTyrant dungeon = new DungeonTyrant(79, 24);
		dungeon.initialze();
		dungeon.create();
		dungeon.getMap().printMapChars();
		dungeon.getMap().printMapArray();
	}
}
