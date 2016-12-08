package net.jmecn.map;

import net.jmecn.map.creator.Dungeon;

public class DungeonTest {
	public static void main(String[] args) {
		Dungeon dungeon = new Dungeon(79, 24);
		dungeon.initialze();
		dungeon.create();
		dungeon.getMap().printMapChars();
		dungeon.getMap().printMapArray();
	}
}
