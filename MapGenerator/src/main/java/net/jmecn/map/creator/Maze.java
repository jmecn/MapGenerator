package net.jmecn.map.creator;

import static net.jmecn.map.Direction.*;

import java.util.logging.Level;
import java.util.logging.Logger;

import net.jmecn.map.Direction;
import net.jmecn.map.Map2D;
import net.jmecn.map.Tile;

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
	
	/**
	 * 迷宫中的房间。
	 * 使用4个boolean变量标识各个方向的门是否打开。
	 * @author yan
	 *
	 */
	protected class Cell {
		boolean[] door = new boolean[] { false, false, false, false };
	}
	
	private Cell[][] maze;// 迷宫
	private int[] cells;// 标识每个格子是否连通
	private int cellCols;// 方块列数
	private int cellRows;// 方块行数
	private int cellCnt;// 格子的数量
	
	// 道路宽度
	private static int ROAD_SIZE = 3;
	
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
	 * 设置道路宽度
	 * 
	 * @param size
	 */
	public void setRoadSize(final int size) {
		// 考虑迷宫的道路使用方块隔开，因此实际宽度要+1。
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
		maze[0][0].door[West] = true;// 起点
		maze[cellRows - 1][cellCols - 1].door[East] = true;// 终点

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
	
	/**
	 * 创建迷宫
	 */
	public void buildMaze() {
		int dir = 0;
		
		Point c1 = new Point();
		Point c2 = null;
		
		// 随机选择一面墙
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
			// 判断随机挑选的两个相邻房间是否连通。
			if (isConnect(c1.index(), c2.index()))
				continue;
			else {
				// 移除房间之间的墙壁
				unionCells(c1.index(), c2.index());
				// 更新迷宫
				maze[c1.y][c1.x].door[dir] = true;
				maze[c2.y][c2.x].door[Direction.negative(dir)] = true;
			}
			// 如果起点和终点连通了，就说明迷宫生成成功
			if (isConnect(0, cellCnt - 1) && allConnect())
				break;
			
		}
	}

	/**
	 * 判断两个房间是否连通
	 * @param c1
	 * @param c2
	 * @return
	 */
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

	/**
	 * 判断是否所有的房间都连通了
	 * @return
	 */
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
	
	/**
	 * 生成方块迷宫
	 */
	public void buildTile() {
		// 根据迷宫数据，将整个区域建造墙壁
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
	
	/**
	 * 建造一堵墙
	 * 
	 * @param x1
	 * @param y1
	 * @param x2
	 * @param y2
	 */
	private void makeWall(int x1, int y1, int x2, int y2) {
		for (int y = y1; y <= y2; y++) {
			for (int x = x1; x <= x2; x++) {
				map.set(x, y, Tile.DirtWall);
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
