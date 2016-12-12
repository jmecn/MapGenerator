package net.jmecn.map.creator;

import static net.jmecn.map.Tile.*;
import static net.jmecn.map.Direction.*;

import java.util.ArrayList;
import java.util.List;

import net.jmecn.map.Point;

public class CaveRandomWalk extends MapCreator {

	// this list record all the points which has been visited
	private List<Point> visited;

	public CaveRandomWalk(int width, int height) {
		super("creator.cave.randomwalk", width, height);
		visited = new ArrayList<Point>();
	}

	@Override
	public void initialze() {
		map.fill(Wall);
		visited.clear();
	}

	@Override
	public void create() {

		// make a random position as the start point
		int x = nextInt(width - 2) + 1;
		int y = nextInt(height - 2) + 1;
		map.set(x, y, UpStairs);

		visited.add(new Point(x, y));

		// pick a random visited position, step a random walk
		Point p;
		do {
			p = randomPick();
			randomWalk(p);
		} while (visited.size() < (width * height) / 2);
	}

	private Point randomPick() {
		if (visited.size() == 1)
			return visited.get(0);

		int index = nextInt(visited.size());
		return visited.get(index);
	}

	private void randomWalk(Point p) {

		int dir = nextInt(DirectionCount);
		switch (dir) {
		case North:
			visit(p.x, p.y-1);
			break;
		case South:
			visit(p.x, p.y+1);
			break;
		case East:
			visit(p.x+1, p.y);
			break;
		case West:
			visit(p.x-1, p.y);
			break;
		default:
			// shouldn't happen
			break;
		}
	}
	
	private void visit(int x, int y) {
		Point p = new Point(x, y);
		if (visited.contains(p)) {
			return;
		}
		
		if (map.contains(x, y)) {
			map.set(x, y, Floor);
			visited.add(p);
		}
	}

}
