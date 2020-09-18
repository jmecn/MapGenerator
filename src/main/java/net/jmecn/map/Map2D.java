package net.jmecn.map;

import java.io.OutputStream;
import java.io.PrintStream;

public class Map2D {

	// max size of the map
	int width;
	int height;

	// map data
	int[][] map;

	public Map2D(int width, int height) {
		this.width = width;
		this.height = height;
		this.map = new int[height][width];
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	public void set(int x, int y, int value) {
		if (contains(x, y)) {
			map[y][x] = value;
		}
	}

	public int get(int x, int y) {
		if (contains(x, y))
			return map[y][x];
		else
			return -1;
	}

	/**
	 * Fill the map with a tile
	 * @param tile
	 */
	public void fill(int tile) {
		for (int y = 0; y < height; y++)
			for (int x = 0; x < width; x++)
				map[y][x] = tile;
	}
	
	/**
	 * Build a boundary with given tile.
	 * 
	 * @param tile
	 */
	public void buildBoundary(int tile) {
		for (int x = 0; x < width; x++) {
			map[0][x] = tile;
			map[height - 1][x] = tile;
		}
		for (int y = 0; y < height; y++) {
			map[y][0] = tile;
			map[y][width - 1] = tile;
		}
	}

	public void setCells(int xStart, int yStart, int xEnd, int yEnd, int cellType) {
		assert (xStart <= xEnd);
		assert (yStart <= yEnd);

		for (int y = yStart; y != yEnd + 1; ++y)
			for (int x = xStart; x != xEnd + 1; ++x)
				map[y][x] = cellType;
	}

	public boolean isXInBounds(int x) {
		return x >= 0 && x < width;
	}

	public boolean isYInBounds(int y) {
		return y >= 0 && y < height;
	}

	public boolean isAreaUnused(int xStart, int yStart, int xEnd, int yEnd) {
		assert (isXInBounds(xStart) && isXInBounds(xEnd));
		assert (isXInBounds(yStart) && isXInBounds(yEnd));

		assert (xStart <= xEnd);
		assert (yStart <= yEnd);

		for (int y = yStart; y != yEnd + 1; ++y)
			for (int x = xStart; x != xEnd + 1; ++x)
				if (map[y][x] != Tile.Unused)
					return false;

		return true;
	}

	public boolean isAdjacent(int x, int y, int tile) {
		assert (isXInBounds(x - 1) && isXInBounds(x + 1));
		assert (isXInBounds(y - 1) && isXInBounds(y + 1));

		return map[y][x - 1] == tile || map[y][x + 1] == tile || map[y - 1][x] == tile || map[y + 1][x] == tile;
	}

	public int[][] getMap() {
		return map;
	}

	public boolean contains(int x, int y) {
		return (x >= 0 && y >= 0 && x < width && y < height);
	}

	public void printMapArray() {
		printMapArray(System.out);
	}

	/**
	 * used to print the map on the screen
	 * 
	 * @param os
	 */
	public void printMapArray(OutputStream os) {
		PrintStream out = new PrintStream(os);
		out.println("int width = " + width + ";");
		out.println("int height = " + height + ";");
		out.println("int[][] map = {");
		for (int y = 0; y < height; y++) {
			out.print("\t{");
			for (int x = 0; x < width; x++) {
				out.print(map[y][x]);
				if (x != width - 1) {
					out.print(",");
				}
			}
			out.print("}");
			if (y != height - 1) {
				out.print(",");
			}
			out.println();
		}
		out.println("};");
	}

	public void printMapChars() {
		printMapChars(System.out);
	}

	public void printMapChars(OutputStream os) {
		PrintStream out = new PrintStream(os);
		out.println("/* preview");
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				out.print(Tile.getChar(map[y][x]));
			}
			out.println();
		}
		out.println("*/");
	}

	public static void main(String[] args) {
		Map2D map = new Map2D(40, 20);
		map.printMapArray();
	}

}
