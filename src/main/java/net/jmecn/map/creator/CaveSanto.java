package net.jmecn.map.creator;

import static net.jmecn.map.Tile.*;

import java.util.ArrayList;
import java.util.List;

import net.jmecn.map.Point;

/**
 * https://github.com/scarino/DungeonGenerator/blob/master/src/main/java/com/scarino/dungeongenerator/CaveGenerator.java
 * 
 * 
 * @author yanmaoyuan
 *
 */
public class CaveSanto extends MapCreator {

    private int seedCount;
    private int iterations;
    
	public CaveSanto(int width, int height) {
		super("creator.cave.santo", width, height);
		seedCount = 5;
		iterations = 300;
	}

	public CaveSanto(String name, int width, int height) {
		super(name, width, height);
	}

	@Override
	public void initialze() {
		map.fill(Wall);
	}

	@Override
	public void create() {
		List<Point> seeds = generateSeeds();
        List<Point> dungeonList = new ArrayList<Point>(iterations+seedCount);
        List<Point> potentialTiles = new ArrayList<Point>(iterations+seedCount);

        for(Point pos : seeds){
            dungeonList.add(pos);
            potentialTiles.addAll(getNeighbours(pos));
        }

        int count = 0;
        while(count < iterations){
            int next = nextInt(potentialTiles.size());
            Point pos = potentialTiles.remove(next);
            if(!posExists(pos.getX(), pos.getY(), dungeonList) && !edgePos(pos)) {
                dungeonList.add(pos);
                potentialTiles.addAll(getNeighbours(pos));
                count++;
            }
        }

        connectSeeds(seeds, dungeonList);
        copyListToArray(dungeonList);
	}

    private boolean edgePos(Point pos){
        return pos.getY() == 0 || pos.getY() == this.height - 1 ||
                pos.getX() == 0 || pos.getX() == this.width - 1;

    }

    private List<Point> getNeighbours(Point pos){
        List<Point> neighbours = new ArrayList<Point>();
        for (int offsetY = -1; offsetY <= 1; offsetY++) {
			for (int offsetX = -1; offsetX <= 1; offsetX++) {
				if (offsetX == 0 && offsetY == 0)
					continue;
				neighbours.add(new Point(pos.x + offsetX, pos.y + offsetY));
			}
		}
        return neighbours;
    }

    private void connectSeeds(List<Point> seeds, List<Point> dungeonList){
        Point first = seeds.remove(0);
        Point next;
        while(seeds.size() > 0){
            next = getClosestSeed(first, seeds);
            addConnections(first, next, dungeonList);
            first = next;
        }
    }

    private void addConnections (Point first, Point second, List<Point> dungeonList){
        int firstX = first.getX();
        int secondX = second.getX();
        int firstY = first.getY();
        int secondY = second.getY();

        while(firstX != secondX){
            if(firstX < secondX){
                firstX++;
            }
            else{
                firstX--;
            }

            dungeonList.add(new Point(firstX, firstY));
        }

        while(firstY != secondY){
            if(firstY < secondY){
                firstY++;
            }
            else{
                firstY--;
            }

            dungeonList.add(new Point(firstX, firstY));
        }
    }

    private Point getClosestSeed(Point seed, List<Point> seeds){
        int curPos = -1;
        int curDis = Integer.MAX_VALUE;

        Point current;
        int nextDis;
        for(int i = 0; i < seeds.size(); i++){
            current = seeds.get(i);
            nextDis = distance(seed, current);
            if(nextDis < curDis){
                curPos = i;
                curDis = nextDis;
            }
        }

        return seeds.remove(curPos);
    }

    private int distance(Point seedOne, Point seedTwo){
        int xDis = seedOne.getX() - seedTwo.getX();
        xDis *= xDis;

        int yDis = seedOne.getY() - seedTwo.getY();
        yDis *= yDis;

        return (int)Math.sqrt(xDis + yDis);
    }

    private void copyListToArray(List<Point> dungeonList){
        for(Point pos : dungeonList){
        	map.set(pos.x, pos.y, Floor);
        }
    }

    private List<Point> generateSeeds(){
        List<Point> seeds = new ArrayList<Point>(seedCount);

        int count = 0;
        while(count < seedCount){
            int x = 1 + nextInt(width-2);
            int y = 1 + nextInt(height-2);

            if(!posExists(x, y, seeds)){
                seeds.add(new Point(x, y));
                count++;
            }
        }

        return seeds;
    }

    public boolean posExists(int x, int y, List<Point> positions){

        for(Point pos : positions){
            if(x == pos.getX() && y == pos.getY()){
                return true;
            }
        }

        return false;
    }
}
