package net.jmecn.map;

final public class Tile {

	private Tile() {
	}

	public final static int Unused = -1;
	public final static int Floor = 0;
	public final static int Wall = 1;
	public final static int Stone = 2;
	public final static int Corridor = 3;
	public final static int Door = 4;
	public final static int UpStairs = 5;
	public final static int DownStairs = 6;
	public final static int Chest = 7;
	// terrain
	public final static int Water = 8;
	public final static int Grass = 9;
	public final static int Dirt = 10;
	public final static int Moss = 11;
	public final static int Tree = 12;

	public final static char getChar(int value) {
		switch (value) {
		case Unused:
			return ' ';
		case Wall:
			return '#';
		case Floor:
			return '.';
		case Stone:
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
		case Water:
			return 'w';
		case Grass:
			return '`';
		case Dirt:
			return 'o';
		case Moss:
			return '~';
		case Tree:
			return 'T';
		default:
			throw new RuntimeException("Unknown tile value=" + value);
		}
	}
}
