package net.jmecn.map;

final public class Tile {

	private Tile() {
	}

	public final static int Unused = 0;
	public final static int DirtWall = 1;
	public final static int DirtFloor = 2;
	public final static int StoneWall = 3;
	public final static int Corridor = 4;
	public final static int Door = 5;
	public final static int Upstairs = 6;
	public final static int Downstairs = 7;
	public final static int Chest = 8;

	public final static char getChar(int value) {
		switch (value) {
		case Unused:
			return ' ';
		case DirtWall:
			return '#';
		case DirtFloor:
			return '.';
		case StoneWall:
			return 'S';
		case Corridor:
			return '.';
		case Door:
			return '+';
		case Upstairs:
			return '<';
		case Downstairs:
			return '>';
		case Chest:
			return 'C';
		default:
			throw new RuntimeException("Unknown tile value=" + value);
		}
	}
}
