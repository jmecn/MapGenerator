package net.jmecn.map;

import net.jmecn.map.creator.Maze;

public class MazeTest {
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
