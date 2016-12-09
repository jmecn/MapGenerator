package net.jmecn.map.creator;
import static net.jmecn.map.Tile.*;

import java.util.ArrayList;
import java.util.List;

import net.jmecn.map.Map2D;

public class DungeonCell extends MapCreator {

	final static private int cellWidth = 10, cellHeight = 8;
	final static private int ROOM_MIN = 5;
	
	private int cellsX;
	private int cellsY;
	private double prob = 0.65;
	
	public DungeonCell(int width, int height) {
		super("creator.dungeon.cell", cellWidth * width, cellHeight * height);
		this.cellsX = width;
		this.cellsY = height;
		prob = 0.65;
	}

	@Override
	public void resize(int width, int height) {
		this.map = new Map2D(cellWidth * width, cellHeight * height);
		this.width = cellWidth * width;
		this.height = cellHeight * height;
		this.cellsX = width;
		this.cellsY = height;
		initRand();
		initialze();
	}

	@Override
	public void initialze() {
		map.fill(DirtWall);
	}

	@Override
	public void create() {
        List<Room> rooms = generateRooms();
        copyListToArray(rooms);
        connectRooms(rooms);
	}
	
    private void connectRooms(List<Room> rooms){
        Room first = rooms.remove(0);
        Room next;
        while(rooms.size() > 0){
            next = getClosestRoom(first, rooms);
            addConnections(first, next);
            first = next;
        }
    }

    private void addConnections (Room first, Room second){
        int firstX = first.centerX();
        int secondX = second.centerX();
        int firstY = first.centerY();
        int secondY = second.centerY();

        while(firstX != secondX){
            if(firstX < secondX){
                firstX++;
            }
            else{
                firstX--;
            }
            map.set(firstX, firstY, DirtFloor);
        }

        while(firstY != secondY){
            if(firstY < secondY){
                firstY++;
            }
            else{
                firstY--;
            }
            map.set(firstX, firstY, DirtFloor);
        }

    }

    private Room getClosestRoom(Room room, List<Room> rooms){
        int curPos = -1;
        int curDis = Integer.MAX_VALUE;

        Room current;
        int nextDis;
        for(int i = 0; i < rooms.size(); i++){
            current = rooms.get(i);
            nextDis = distance(room, current);
            if(nextDis < curDis){
                curPos = i;
                curDis = nextDis;
            }
        }

        return rooms.remove(curPos);
    }

    private int distance(Room roomOne, Room roomTwo){
        int xDis = roomOne.centerX() - roomTwo.centerX();
        xDis *= xDis;

        int yDis = roomOne.centerY() - roomTwo.centerY();
        yDis *= yDis;

        return (int)Math.sqrt(xDis + yDis);
    }

    private List<Room> generateRooms(){
        List<Room> rooms = new ArrayList<Room>(width*height);

        int roomWidth, roomHeight;
        for(int y = 0; y < cellsY; y++){
            for(int x = 0; x < cellsX; x++){
                if(rand.nextDouble() < prob) {
                    roomWidth = ROOM_MIN + nextInt(cellWidth - 6);
                    roomHeight = ROOM_MIN + nextInt(cellHeight - 6);
                    rooms.add(new Room(x * cellWidth, y * cellHeight, roomWidth, roomHeight));
                }
            }
        }

        return rooms;
    }

    private void copyListToArray(List<Room> rooms){
        for(Room room : rooms) {
            for (int y = room.getY()+1; y < room.getY() + room.getHeight()-1; y++) {
                for(int x = room.getX()+1; x < room.getX() + room.getWidth()-1; x++){
                	map.set(x, y, DirtFloor);
                }
            }
        }
    }

    private static class Room {
        int x, y, width, height;

        public Room(int x, int y, int width, int height){
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }

        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }

        public int getWidth() {
            return width;
        }

        public int getHeight() {
            return height;
        }

        public int centerX(){
            return x + (width / 2);
        }

        public int centerY(){
            return y + (height / 2);
        }
    }
}
