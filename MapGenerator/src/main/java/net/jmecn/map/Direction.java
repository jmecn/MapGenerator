package net.jmecn.map;

final public class Direction {

	private Direction() {
	}

	public final static int UnknownDir = -1;
	public final static int East = 0;
	public final static int South = 1;
	public final static int West = 2;
	public final static int North = 3;
	public final static int DirectionCount = 4;
	
	public final static int negative(int dir) {
		return (dir + 2) % DirectionCount;
	}
}