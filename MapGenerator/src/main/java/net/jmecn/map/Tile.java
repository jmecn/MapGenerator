package net.jmecn.map;

final public class Tile {

	private Tile() {
	}

	public final static int Unused = -1;
	public final static int DirtFloor = 0;
	public final static int DirtWall = 1;
	public final static int StoneWall = 2;
	public final static int Corridor = 3;
	public final static int Door = 4;
	public final static int UpStairs = 5;
	public final static int DownStairs = 6;
	public final static int Chest = 7;

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
		case UpStairs:
			return '<';
		case DownStairs:
			return '>';
		case Chest:
			return 'C';
		default:
			throw new RuntimeException("Unknown tile value=" + value);
		}
	}
}
