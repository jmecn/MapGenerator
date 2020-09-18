package net.jmecn.map.creator;

import static net.jmecn.map.Tile.*;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import net.jmecn.map.Point;

/**
 * 
 * https://github.com/munificent/hauberk/blob/master/lib/src/content/forest.dart
 * 
 * @author yanmaoyuan
 *
 */
public class ForestHauberk extends MapCreator {

	/// A forest is a collection of grassy meadows surrounded by trees and
	/// connected by passages.
	private int numMeadows = 10;

	/// The number of iterations of Lloyd's algorithm to run on the points.
	///
	/// Fewer results in clumpier, less evenly spaced points. More results in
	/// more evenly spaced but can eventually look too regular.
	private int voronoiIterations = 10;

	public ForestHauberk(int width, int height) {
		super("creator.forest.hauberk", width, height);
	}

	public void setNumMeadows(int numMeadows) {
		if (numMeadows < 10) numMeadows = 10;
		this.numMeadows = numMeadows;
	}

	public void setVoronoiIterations(int voronoiIterations) {
		if (voronoiIterations < 5) voronoiIterations = 5;
		this.voronoiIterations = voronoiIterations;
	}

	@Override
	public void initialze() {
		map.fill(Wall);

	}

	@Override
	public void create() {
		// Randomly position the meadows.
		LinkedList<Point> meadows = new LinkedList<Point>();
		for (int i = 0; i < numMeadows; i++) {
			int x = nextInt(width);
			int y = nextInt(height);
			meadows.add(new Point(x, y));
		}

		// Space them out more evenly by moving each point to the centroid of
		// its cell in the Voronoi diagram of the points. In other words, for each
		// point, we find the (approximate) region of the stage where that point
		// is the closest one. Then we move the point to the center of that
		// region.
		// http://en.wikipedia.org/wiki/Lloyd%27s_algorithm
		for (int i = 0; i < voronoiIterations; i++) {
			// For each cell in the stage, determine which point it's nearest to.
			List<List<Point>> regions = new ArrayList<List<Point>>(numMeadows);
			for (int j = 0; j < numMeadows; j++) {
				regions.add(new ArrayList<Point>());
			}

			for (int y = 0; y < height; y++) {
				for (int x = 0; x < width; x++) {
					int nearest = -1;
					int nearestDistanceSquared = 99999999;
					for (int j = 0; j < numMeadows; j++) {
						Point p = meadows.get(j);
						int dx = p.x - x;
						int dy = p.y - y;
						int lengthSquared = dx * dx + dy * dy;
						if (lengthSquared < nearestDistanceSquared) {
							nearestDistanceSquared = lengthSquared;
							nearest = j;
						}
					}

					regions.get(nearest).add(new Point(x, y));
				}
			}

			// Now move each point to the centroid of its region. The centroid
			// is just the average of all of the cells in the region.
			for (int j = 0; j < numMeadows; j++) {
				List<Point> region = regions.get(j);
				int len = region.size();

				if (len == 0)
					continue;
				int a = 0;
				int b = 0;
				for (int k = 0; k < len; k++) {
					Point p = region.get(k);
					a += p.x;
					b += p.y;
				}
				meadows.get(j).x = a / len;
				meadows.get(j).y = b / len;
			}
		}

		// Connect all of the points together.
		// Use Prim's algorithm to generate a minimum spanning tree.
		List<Point> connected = new ArrayList<Point>(numMeadows);
		
		// the root node
		connected.add(meadows.removeLast());
		
		while (!meadows.isEmpty()) {
			Point bestFrom = null;
			Point bestTo = null;
			int bestDistance = -1;
			
			int cLen = connected.size();
			int mLen = meadows.size();
			for(int p=0; p<cLen; p++) {
				Point from = connected.get(p);
				for(int q=0; q<mLen; q++) {
					// skip the same point
					if (q == p) continue;
					Point to = meadows.get(q);
					
					int dx = from.x - to.x;
					int dy = from.y - to.y;
					int distance = dx * dx + dy * dy;
					if (bestDistance == -1 || distance < bestDistance) {
						bestFrom = from;
						bestTo = to;
						bestDistance = distance;
					}
				}
			}
			
			carvePath(bestFrom, bestTo);
			meadows.remove(bestTo);
			connected.add(bestTo);
		}

		meadows.addAll(connected);
		connected.clear();
		
		// Carve out the meadows.
		for(int p=0; p<numMeadows; p++) {
			Point point = meadows.get(p);
			carveCircle(point, 4);
		}
		
		// put stairs
		Point bestFrom = null;
		Point bestTo = null;
		int bestDistance = -1;
		int len = meadows.size();
		for(int p=0; p<len; p++) {
			Point from = meadows.get(p);
			for(int q=0; q<len; q++) {
				// skip the same point
				if (q == p) continue;
				Point to = meadows.get(q);
				
				int dx = from.x - to.x;
				int dy = from.y - to.y;
				int distance = dx * dx + dy * dy;
				if (distance > bestDistance) {
					bestFrom = from;
					bestTo = to;
					bestDistance = distance;
				}
			}
		}
		map.set(bestFrom.x, bestFrom.y, UpStairs);
		map.set(bestTo.x, bestTo.y, DownStairs);
		
		// make wall
		erode(10000, Floor, Wall);

		// Plant trees
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				if (map.get(x, y) == Wall) {
					map.set(x, y, Tree);
				}
			}
		}
	}

	/// Randomly turns some [wall] tiles into [floor] and vice versa.
	private void erode(int iterations, int floor, int wall) {
		for (int i = 0; i < iterations; i++) {
			// TODO: This way this works is super inefficient. Would be better to
			// keep track of the floor tiles near open ones and choose from them.
			// pos
			int x = nextInt(1, width - 1);
			int y = nextInt(1, height - 1);

			int here = map.get(x, y);
			if (here != wall)
				continue;

			// Keep track of how many floors we're adjacent too. We will only
			// erode
			// if we are directly next to a floor.
			int floors = 0;
			if (map.get(x - 1, y) == floor)
				floors++;
			if (map.get(x + 1, y) == floor)
				floors++;
			if (map.get(x, y - 1) == floor)
				floors++;
			if (map.get(x, y + 1) == floor)
				floors++;

			// Prefer to erode tiles near more floor tiles so the erosion isn't
			// too
			// spiky.
			if (floors < 2)
				continue;
			if (nextInt(9 - floors) == 0)
				map.set(x, y, floor);
		}
	}

	private void carvePath(Point from, Point to) {

		int a = Math.abs(to.x - from.x);
		int b = Math.abs(to.y - from.y);

		if (a >= b) {
			int xStep = 1;
			if (to.x < from.x)
				xStep = -1;

			float yStep = (float) (to.y - from.y) / a;
			// use xStep
			int x = from.x;
			int y = from.y;
			for (int i = 0; i <= a; i++) {
				x = from.x + xStep * i;
				y = from.y +(int)(yStep * i);
				// Make slightly wider passages.
				map.set(x, y, Floor);
				map.set(x + 1, y, Floor);
				map.set(x, y + 1, Floor);
			}

		} else {
			// use yStep
			int yStep = 1;
			if (to.y < from.y)
				yStep = -1;

			float xStep = (float) (to.x - from.x) / b;
			// use xStep
			int x = from.x;
			int y = from.y;
			for (int i = 0; i <= b; i++) {
				x = from.x + (int) (xStep * i);
				y = from.y + yStep * i;
				// Make slightly wider passages.
				map.set(x, y, Floor);
				map.set(x + 1, y, Floor);
				map.set(x, y + 1, Floor);
			}
		}
	}

	private void carveCircle(Point center, int radius) {
		// bounds
		int left = Math.max(1, center.x - radius);
		int top = Math.max(1, center.y - radius);
		int right = Math.min(center.x + radius, width - 2);
		int bottom = Math.min(center.y + radius, height - 2);

		for (int y = top; y <= bottom; y++) {
			for (int x = left; x <= right; x++) {
				int dx = x - center.x;
				int dy = y - center.y;
				if (Math.sqrt(dx * dx + dy * dy) <= radius)
					map.set(x, y, Floor);
			}
		}
	}
}
