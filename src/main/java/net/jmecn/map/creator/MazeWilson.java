package net.jmecn.map.creator;

import static net.jmecn.map.Direction.*;
import static net.jmecn.map.Tile.*;

import java.util.ArrayList;
import java.util.logging.Logger;

import net.jmecn.map.Point;

/**
 * Maze creator using Wilson's algorithm
 * 
 * @author yanmaoyuan
 *
 */
public class MazeWilson extends MapCreator {

	class Cell extends Point {
		int tile;
		Cell(int x, int y) {
			super(x, y);
			tile = Unused;
		}
		Cell(int x, int y, int tile) {
			super(x, y);
			this.tile = tile;
		}
	}
	
	static Logger logger = Logger.getLogger(MazeWilson.class.getName());

	public MazeWilson(int width, int height) {
		super("creator.maze.wilson", width, height);
	}

	@Override
	public void initialze() {
		map.fill(Wall);
	}

	@Override
	public void create() {
		// first spot on maze. walker seeks established maze tiles
		map.set(1, height - 2, Floor);

		// the first start point.
		Cell nextStart = new Cell(3, height - 2);
		while (nextStart != null) {
			erasedLoopWalk(nextStart.x, nextStart.y);
			nextStart = findNextStartCell(nextStart);
		}
	}

	private void erasedLoopWalk(int x, int y) {
		ArrayList<Cell> trail = new ArrayList<Cell>();
		ArrayList<Integer> moveLog = new ArrayList<Integer>(); // should always

		trail.add(new Cell(x, y, Stone));
		map.set(x, y, Stone);

		moveLog.add(UnknownDir);// placeholder that hopefully won't break anything
		while (true) {
			// decide candidates based on borders and previous move
			ArrayList<Integer> candidates = new ArrayList<Integer>();
			int prev = moveLog.get(moveLog.size() - 1);
			if (x - 2 > 0 && prev != East)
				candidates.add(West);
			if (x + 2 < width - 1 && prev != West)
				candidates.add(East);
			if (y - 2 > 0 && prev != South)
				candidates.add(North);
			if (y + 2 < height - 1 && prev != North)
				candidates.add(South);

			int move = candidates.get(rand.nextInt(candidates.size()));

			for (int i = 0; i < 2; i++) { // do twice so that each move moves 2 spaces
				// choose a direction and walk a step
				if (move == West)
					x--;
				if (move == East)
					x++;
				if (move == North)
					y--;
				if (move == South)
					y++;

				trail.add(new Cell(x, y, Stone));// add the new piece of the trail
				map.set(x, y, Stone);// change its tile for collision purposes

				// look for loops and erase back if found
				boolean looped = false;
				if (map.get(x + 1, y) == Stone && move != West) {
					x++;
					looped = true;
				} else if (map.get(x - 1, y) == Stone && move != East) {
					x--;
					looped = true;
				} else if (map.get(x, y + 1) == Stone && move != North) {
					y++;
					looped = true;
				} else if (map.get(x, y - 1) == Stone && move != South) {
					y--;
					looped = true;
				} else if (map.get(x - 1, y - 1) == Stone
						&& map.get(x - 1, y - 1) != trail.get(trail.size() - 3).tile) {
					x--;
					y--;
					looped = true;
				} else if (map.get(x + 1, y - 1) == Stone
						&& map.get(x + 1, y - 1) != trail.get(trail.size() - 3).tile) {
					x++;
					y--;
					looped = true;
				} else if (map.get(x - 1, y + 1) == Stone
						&& map.get(x - 1, y + 1) != trail.get(trail.size() - 3).tile) {
					x--;
					y++;
					looped = true;
				} else if (map.get(x + 1, y + 1) == Stone
						&& map.get(x + 1, y + 1) != trail.get(trail.size() - 3).tile) {
					x++;
					y++;
					looped = true;
				}

				if (looped) {
					int retrace = trail.indexOf(new Cell(x, y));

					for (int j = retrace + 1; j < trail.size(); j++) {
						Cell p = trail.get(j);
						map.set(p.x, p.y, Wall);
					}

					trail.subList(retrace + 1, trail.size()).clear();
					moveLog.subList(retrace / 2 + 1, moveLog.size()).clear();

					break; // breaks the for loop to avoid more collision detection
				}

				// look for connections and commit if found
				if (i == 0 && (map.get(x - 1, y) == Floor || map.get(x + 1, y) == Floor
						|| map.get(x, y - 1) == Floor || map.get(x, y + 1) == Floor)) {
					for (Cell p : trail) {
						map.set(p.x, p.y, Floor);
					}
					return;
				}

			} // end for

			moveLog.add(move);
		}
	}

	private Cell findNextStartCell(Cell lastStart) {
		// search bottom to top, left to right, for a black cell with two
		// adjacent black cells

		boolean firstLoop = true;
		for (int y = height - 2; y >= 1; y--) {
			for (int x = 1; x <= width - 2; x++) {
				if (firstLoop) {
					x = lastStart.x;
					y = lastStart.y;
					firstLoop = false;
				}

				int adjacentBlackCounter = 0;

				for (int offsetY = -1; offsetY <= 1; offsetY++) {
					for (int offsetX = -1; offsetX <= 1; offsetX++) {
						if (offsetX == 0 && offsetY == 0)
							continue;
						if (map.get(x + offsetX, y + offsetY) == Wall) {
							adjacentBlackCounter++;
						}
					}
				}

				if (adjacentBlackCounter == 8) {
					return new Cell(x, y);
				}
			}
		}
		return null;
	}
}
