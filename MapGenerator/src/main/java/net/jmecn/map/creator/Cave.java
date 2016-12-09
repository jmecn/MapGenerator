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
public class Cave extends MapCreator {

	private int fillprob = 45;
	final static private int r1Cutoff = 5;
	final static private int r2Cutoff = 2;
	
	public Cave(int width, int height) {
		super("creator.cave.cellauto", width, height);
	}

	protected int randPick() {
		if (rand.nextInt(100) < fillprob) {
			return DirtWall;
		} else {
			return DirtFloor;
		}
	}
	
	@Override
	public void initialze() {
		for(int y=0; y<height; y++) {
			for(int x=0; x<width; x++) {
				if (x == 0 || y == 0 || x == width - 1 || y == height - 1) {
					map.set(x, y, DirtWall);
				} else {
					map.set(x, y, randPick());
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
		for(int y=1; y<height-1; y++) {
			for(int x=1; x<width-1; x++) {
				
				int m = 0;
				for(int offsety=-1; offsety<=1; offsety++) {
					for(int offsetx=-1; offsetx<=1; offsetx++) {
						if (map.get(offsetx+x, offsety+y) == DirtWall) {
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
						
						if (map.get(offsetx+x, offsety+y) == DirtWall) {
							n++;
						}
					}
				}
				
				if (m >= r1Cutoff || n <= r2Cutoff) {
					tmp[y][x] = DirtWall;
				} else {
					tmp[y][x] = DirtFloor;
				}
				
			}
		}
		
		for(int y=0; y<height; y++) {
			for(int x=0; x<width; x++) {
				map.set(x, y, tmp[y][x]);
			}
		}
	}

	public void setFillprob(int fillprob) {
		this.fillprob = fillprob;
	}
	
	public static void main(String[] args) {
		Cave cave = new Cave(30, 30);
		cave.setSeed(1654987414656544l);
		cave.setUseSeed(true);
		cave.initialze();
		cave.create();
		cave.getMap().printMapChars();
		cave.getMap().printMapArray();
	}
}
