package net.jmecn.map.creator;

import static net.jmecn.map.Tile.*;

/**
 * https://gamedevelopment.tutsplus.com/tutorials/generate-random-cave-levels-using-cellular-automata--gamedev-9664
 * 
 * http://pixelenvy.ca/wa/ca_cave.html
 * 
 * @author yanmaoyuan
 *
 */
public class CaveCellauto extends MapCreator {

	private int fillprob = 40;
	final static private int r1Cutoff = 5;
	final static private int r2Cutoff = 2;

	public CaveCellauto(int width, int height) {
		super("creator.cave.cellauto", width, height);
	}

	protected int randPick() {
		if (rand.nextInt(100) < fillprob) {
			return Wall;
		} else {
			return Floor;
		}
	}

	@Override
	public void initialze() {
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				if (x == 0 || y == 0 || x == width - 1 || y == height - 1) {
					map.set(x, y, Wall);
				} else {
					map.set(x, y, randPick());
				}
			}
		}

	}

	@Override
	public void create() {
		do {
			initialze();
			for (int i = 0; i < 7; i++) {
				generation(i);
			}
		} while(!(floodFill()));

		// fill disconnected cave with wall
		for (int y = 1; y < height-1; y++) {
			for (int x = 1; x < width-1; x++) {
				if (map.get(x, y) == Unused) {
					map.set(x, y, Floor);
				} else if (map.get(x, y) == Floor) {
					map.set(x, y, Wall);
				}
			}
		}
	}

	private int sum = 0;

	private boolean floodFill() {
		// get a random start point
		int x, y;
		do {
			x = nextInt(width);
			y = nextInt(height);
		} while (map.get(x, y) != Floor);

		//
		sum = 0;
		floodFill8(x, y, Unused, Floor);
		
		// at least 45% place are floor
		double percent = 100.0 * sum/(width * height);
		return percent >= 45.0;
	}

	private void floodFill8(int x, int y, int newTile, int oldTile) {

		// skip boundary
		if (x < 0 || x > width - 1 || y < 0 || y > height - 1 || map.get(x, y) != oldTile || map.get(x, y) == newTile) {
			return;
		}
		map.set(x, y, newTile);
		sum++;

		floodFill8(x - 1, y - 1, newTile, oldTile);
		floodFill8(x - 1, y, newTile, oldTile);
		floodFill8(x - 1, y + 1, newTile, oldTile);
		floodFill8(x, y - 1, newTile, oldTile);
		floodFill8(x, y + 1, newTile, oldTile);
		floodFill8(x + 1, y - 1, newTile, oldTile);
		floodFill8(x + 1, y, newTile, oldTile);
		floodFill8(x + 1, y + 1, newTile, oldTile);

	}

	protected void generation(int gen) {
		int[][] tmp = copy();
		for (int y = 1; y < height - 1; y++) {
			for (int x = 1; x < width - 1; x++) {

				int m = 0;
				for (int offsety = -1; offsety <= 1; offsety++) {
					for (int offsetx = -1; offsetx <= 1; offsetx++) {
						if (map.get(offsetx + x, offsety + y) == Wall) {
							m++;
						}
					}
				}

				int n = 0;
				for (int offsety = -2; offsety <= 2; offsety++) {
					for (int offsetx = -2; offsetx <= 2; offsetx++) {
						if (Math.abs(offsetx) == 2 && Math.abs(offsety) == 2) {
							continue;
						}

						if (map.get(offsetx + x, offsety + y) == Wall) {
							n++;
						}
					}
				}

				if (gen < 4) {
					if (m >= r1Cutoff || n <= r2Cutoff) {
						tmp[y][x] = Wall;
					} else {
						tmp[y][x] = Floor;
					}
				} else {
					if (m >= r1Cutoff) {
						tmp[y][x] = Wall;
					} else {
						tmp[y][x] = Floor;
					}
				}

			}
		}

		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				map.set(x, y, tmp[y][x]);
			}
		}
	}

	public void setFillprob(int fillprob) {
		this.fillprob = fillprob;
	}

	public static void main(String[] args) {
		CaveCellauto cave = new CaveCellauto(30, 30);
		cave.setSeed(1654987414656544l);
		cave.setUseSeed(true);
		cave.initialze();
		cave.create();
		cave.getMap().printMapChars();
		cave.getMap().printMapArray();
	}
}
