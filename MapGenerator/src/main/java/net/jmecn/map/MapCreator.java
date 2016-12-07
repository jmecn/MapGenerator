package net.jmecn.map;

import java.util.Arrays;
import java.util.Random;

public abstract class MapCreator {

	protected Map2D map;
	
	protected static Random rand = new Random();
	private long seed = 0;
	private boolean useSeed = false;
	
	public MapCreator(int x, int y) {
		map = new Map2D(x, y);
	}
	
	public void resize(int x, int y) {
		map = new Map2D(x, y);
		initRand();
		initialze();
	}
	
	public void setUseSeed(boolean useSeed) {
		this.useSeed = useSeed;
		initRand();
	}
	
	public void setSeed(long seed) {
		this.seed = seed;
	}
	
	public void initRand() {
		initRand(useSeed);
	}
	
	public void initRand(boolean useSeed) {
		if (useSeed) {
			rand = new Random(seed);
		} else {
			rand = new Random();
		}
	}
	
	protected int nextInt(final int range) {
		return rand.nextInt(range);
	}
	
	protected int[][] copy() {
		int[][] data = map.getMap();
		int w = map.getWidth();
		int h = map.getHeight();
		
		int[][] tmp = new int[h][];
		
		for(int y=0; y<h; y++) {
			tmp[y] = Arrays.copyOf(data[y], w);
		}
		
		return tmp;
	}
	
	public Map2D getMap() {
		return map;
	}

	public abstract void initialze();
	
	public abstract void create();
	
}
