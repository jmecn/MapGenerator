package net.jmecn.map;

public class CaveTest {

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
