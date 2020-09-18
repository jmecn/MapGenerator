package net.jmecn.map.creator;

import java.util.Arrays;
import java.util.Random;
import java.util.ResourceBundle;

import net.jmecn.map.Map2D;

/**
 * Base class for a tile map creator.
 * 
 * @author yanmaoyuan
 *
 */
public abstract class MapCreator {

	protected String name;// Algorithm name
	protected Map2D map;
	protected int width;
	protected int height;

	protected static Random rand = new Random();
	private long seed = 0;
	private boolean useSeed = false;

	public MapCreator(int width, int height) {
		this.name = "Unknown algorithm#"+rand.nextInt();
		this.map = new Map2D(width, height);
		this.width = width;
		this.height = height;
	}
	
	public MapCreator(String name, int width, int height) {
		setName(name);
		this.map = new Map2D(width, height);
		this.width = width;
		this.height = height;
	}

	public void resize(int width, int height) {
		this.map = new Map2D(width, height);
		this.width = width;
		this.height = height;
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
			rand = new Random(System.currentTimeMillis());
		}
	}

	protected boolean nextBoolean() {
		return rand.nextBoolean();
	}

	protected int nextInt() {
		return rand.nextInt();
	}

	protected int nextInt(final int range) {
		return rand.nextInt(range);
	}

	protected int nextInt(final int min, final int max) {
		return rand.nextInt(max - min) + min;
	}

	protected int[][] copy() {
		int[][] data = map.getMap();
		int w = map.getWidth();
		int h = map.getHeight();

		int[][] tmp = new int[h][];

		for (int y = 0; y < h; y++) {
			tmp[y] = Arrays.copyOf(data[y], w);
		}

		return tmp;
	}

	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		ResourceBundle res = ResourceBundle.getBundle("net.jmecn.map.creator.Name");
		if (res != null) {
			try {
				this.name = res.getString(name);
			} catch (Exception e) {
				// no need to do anything
				this.name = name;
			}
		} else {
			this.name = name;
		}
	}
	
	public Map2D getMap() {
		return map;
	}

	public abstract void initialze();

	public abstract void create();

	@Override
	public String toString() {
		return name;
	}
}
