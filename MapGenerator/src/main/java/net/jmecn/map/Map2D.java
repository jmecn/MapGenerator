package net.jmecn.map;

import java.io.OutputStream;
import java.io.PrintStream;

public class Map2D {

	int width;
	int height;
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
		return map[y][x];
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

	public void printMapArray(OutputStream os) {
		PrintStream out = new PrintStream(os);
		out.println("int width = " + width + ";");
		out.println("int height = " + height + ";");
		out.println("int[][] map = {");
		for(int y=0; y<height; y++) {
			out.print("\t{");
			for(int x=0; x<width; x++) {
				out.print(map[y][x]);
				if (x != width-1) {
					out.print(",");
				}
			}
			out.print("}");
			if (y != height-1) {
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
		for(int y=0; y<height; y++) {
			for(int x=0; x<width; x++) {
				if (map[y][x] == 0) {
					out.print("  ");
				} else {
					out.print("[]");
				}
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
