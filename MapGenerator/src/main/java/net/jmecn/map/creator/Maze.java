package net.jmecn.map.creator;

import static net.jmecn.map.Direction.*;

import java.util.logging.Level;
import java.util.logging.Logger;

import net.jmecn.map.Direction;
import net.jmecn.map.Map2D;
import net.jmecn.map.Tile;

/**
 * About maze you should read this article:
 * http://www.astrolog.org/labyrnth/algrithm.htm
 * @author yanmaoyuan
 *
 */
public class Maze extends MapCreator {

	static Logger logger = Logger.getLogger(Maze.class.getName());
	
	protected class Point {
		int x;
		int y;
		
		Point() {
			x = y = 0;
		}
		
		Point(int x, int y) {
			this.x = x;
			this.y = y;
		}
		
		int index() {
			return x + y * cellCols;
		}
	}
	
	protected class Cell {
		boolean[] door = new boolean[] { false, false, false, false };
	}
	
	private Cell[][] maze;
	private int[] cells;
	private int cellCols;
	private int cellRows;
	private int cellCnt;
	
	private static int ROAD_SIZE = 2;
	
	public Maze(int width, int height) {
		super("creator.maze", width * ROAD_SIZE + 1, height * ROAD_SIZE + 1);
		this.cellCols = width;
		this.cellRows = height;
	}

	@Override
	public void resize(int width, int height) {
		this.map = new Map2D(width * ROAD_SIZE + 1, height * ROAD_SIZE + 1);
		this.width = cellCols * ROAD_SIZE + 1;
		this.height = cellRows * ROAD_SIZE + 1;
		this.cellCols = width;
		this.cellRows = height;
		initRand();
		initialze();
	}
	
	/**
	 * set road size
	 * 
	 * @param size
	 */
	public void setRoadSize(final int size) {
		ROAD_SIZE = size + 1;
	}
	
	@Override
	public void initialze() {
		this.maze = new Cell[cellRows][cellCols];
		for (int y = 0; y < cellRows; y++) {
			for (int x = 0; x < cellCols; x++) {
				maze[y][x] = new Cell();
			}
		}
		maze[0][0].door[West] = true;
		maze[cellRows - 1][cellCols - 1].door[East] = true;

		this.cellCnt = cellCols * cellRows;
		this.cells = new int[cellCnt];
		for (int i = 0; i < cellCnt; i++) {
			cells[i] = -1;
		}
	}

	@Override
	public void create() {
		buildMaze();
		buildTile();
	}
	
	public void buildMaze() {
		int dir = 0;
		
		Point c1 = new Point();
		Point c2 = null;
		
		while (true) {
			c1.x = nextInt(cellCols);
			c1.y = nextInt(cellRows);
			c2 = null;
			
			dir = nextInt(DirectionCount);
			switch (dir) {
			case North:
				if (c1.y > 0)
					c2 = new Point(c1.x, c1.y - 1);
				break;
			case South:
				if (c1.y < cellRows - 1)
					c2 = new Point(c1.x, c1.y + 1);
				break;
			case East:
				if (c1.x < cellCols - 1)
					c2 = new Point(c1.x + 1, c1.y);
				break;
			case West:
				if (c1.x > 0)
					c2 = new Point(c1.x - 1, c1.y);
				break;
			default:
				c2 = null;
				logger.log(Level.WARNING, "Unknown direction:" + dir);
				break;
			}
			
			if (c2 == null)
				continue;
			if (isConnect(c1.index(), c2.index()))
				continue;
			else {
				unionCells(c1.index(), c2.index());
				maze[c1.y][c1.x].door[dir] = true;
				maze[c2.y][c2.x].door[Direction.negative(dir)] = true;
			}
			if (isConnect(0, cellCnt - 1) && allConnect())
				break;
			
		}
	}

	private boolean isConnect(int c1, int c2) {
		while (cells[c1] >= 0)
			c1 = cells[c1];
		while (cells[c2] >= 0)
			c2 = cells[c2];
		if (c1 == c2)
			return true;
		else
			return false;
	}

	private boolean allConnect() {
		int i, count_root = 0;
		for (i = 0; i < cellRows * cellCols; i++) {
			if (cells[i] < 0)
				count_root++;
		}
		if (1 == count_root)
			return true;
		else
			return false;
	}

	/**
	 * if the two adjacent rooms are not connect, remove the wall between them(or fix a door)
	 * @param c1
	 * @param c2
	 */
	private void unionCells(int c1, int c2) {
		while (cells[c1] >= 0)
			c1 = cells[c1];
		while (cells[c2] >= 0)
			c2 = cells[c2];

		// the depth of the tree with c2 is deepper than Tc1, Tc1 attach to Tc2
		if (cells[c1] > cells[c2]) {
			cells[c1] = c2;
		} else {
			if (cells[c1] == cells[c2])
				cells[c1]--;
			cells[c2] = c1;
		}
	}
	
	public void buildTile() {
		for (int col = 0; col < cellCols; col++) {
			for (int row = 0; row < cellRows; row++) {
				Cell cell = maze[row][col];
				int x = col * ROAD_SIZE;
				int y = row * ROAD_SIZE;
				if (!cell.door[North]) {
					makeWall(x, y, x + ROAD_SIZE, y);
				}
				if (!cell.door[East]) {
					makeWall(x + ROAD_SIZE, y, x + ROAD_SIZE, y + ROAD_SIZE);
				}
				if (!cell.door[South]) {
					makeWall(x, y + ROAD_SIZE, x + ROAD_SIZE, y + ROAD_SIZE);
				}
				if (!cell.door[West]) {
					makeWall(x, y, x, y + ROAD_SIZE);
				}
			}
		}

	}
	
	private void makeWall(int x1, int y1, int x2, int y2) {
		for (int y = y1; y <= y2; y++) {
			for (int x = x1; x <= x2; x++) {
				map.set(x, y, Tile.Wall);
			}
		}
	}
	
	public static void main(String[] args) {
		Maze maze = new Maze(30, 14);
		maze.setRoadSize(1);
		maze.setSeed(1654987414656544l);
		maze.setUseSeed(true);
		maze.initialze();
		maze.create();
		maze.getMap().printMapChars();
		maze.getMap().printMapArray();
	}
}
