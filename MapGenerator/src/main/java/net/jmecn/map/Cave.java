package net.jmecn.map;

import static net.jmecn.map.Tile.*;

public class Cave extends MapCreator {

	final static private int fillprob = 45;
	final static private int r1Cutoff = 5;
	final static private int r2Cutoff = 2;
	
	public Cave(int x, int y) {
		super(x, y);
	}

	protected int randPick() {
		if (rand.nextInt(100) < fillprob) {
			return WALL;
		} else {
			return FLOOR;
		}
	}
	
	@Override
	public void initialze() {
		int[][] data = map.getMap();
		int w = map.getWidth();
		int h = map.getHeight();
		for(int y=0; y<h; y++) {
			for(int x=0; x<w; x++) {
				if (x == 0 || y == 0 || x == w - 1 || y == h - 1) {
					data[y][x] = WALL;
				} else {
					data[y][x] = randPick();
				}
			}
		}
		
	}

	@Override
	public void create() {
		for(int i=0; i<5; i++) {
			generation();
		}
	}

	protected void generation() {
		int[][] tmp = copy();
		int[][] data = map.getMap();
		int w = map.getWidth();
		int h = map.getHeight();
		
		for(int y=1; y<h-1; y++) {
			for(int x=1; x<w-1; x++) {
				
				int m = 0;
				for(int offsety=-1; offsety<=1; offsety++) {
					for(int offsetx=-1; offsetx<=1; offsetx++) {
						if (data[offsety+y][offsetx+x] == WALL) {
							m++;
						}
					}
				}
				
				int n = 0;
				for(int offsety=-2; offsety<=2; offsety++) {
					for(int offsetx=-2; offsetx<=2; offsetx++) {
						if (Math.abs(offsetx) == 2 && Math.abs(offsety) == 2) {
							continue;
						}
						
						if (map.contains(x+offsetx, y+offsety) && data[offsety+y][offsetx+x] == WALL) {
							n++;
						}
					}
				}
				
				
				if (m >= r1Cutoff || n <= r2Cutoff) {
					tmp[y][x] = WALL;
				} else {
					tmp[y][x] = FLOOR;
				}
				
			}
		}
		
		for(int y=0; y<h; y++) {
			for(int x=0; x<w; x++) {
				data[y][x] = tmp[y][x];
			}
		}
	}
}
