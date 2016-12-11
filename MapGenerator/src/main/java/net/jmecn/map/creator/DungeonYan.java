package net.jmecn.map.creator;

import static net.jmecn.map.Direction.*;
import static net.jmecn.map.Tile.*;

import java.util.ArrayList;
import java.util.List;

import net.jmecn.map.Point;

/**
 * This is my own dungeon algorithm.
 * 
 * @author yanmaoyuan
 *
 */
public class DungeonYan extends MapCreator {

	private List<Room> rooms;

	public DungeonYan(int width, int height) {
		super("creator.dungeon.yan", width, height);
		rooms = new ArrayList<Room>();
	}

	@Override
	public void initialze() {
		map.fill(Unused);
	}

	@Override
	public void create() {
		rooms.clear();
		Room room;

		int height = nextInt(5) + 9;
		int width = nextInt(5) + 6;

		// the Main room
		room = new Room(width, height);
		room.x = (this.width - width) / 2;
		room.y = (this.height - height) / 2;
		room.tiles[height/2][width/2] = UpStairs;
		room.type = Type.ROOM;
		room.print();
		rooms.add(room);

		// generate rooms and corridors
		for (int i = 0; i < 500; i++) {
			room = randomRoom();

			if (room.type == Type.CORRIDOR) {
				if (nextInt(4) == 0) {
					generateCorridor(room, true);
				} else {
					generateRoom(room);
				}
			} else {
				generateCorridor(room, false);
			}
		}

		// generate downStairs
		do {
			room = randomRoom();
		} while(room.type == Type.CORRIDOR || room.hasStairs());
		map.set(room.x + room.width/2, room.y + room.height / 2, DownStairs);
	}

	/**
	 * get a random room.
	 * 
	 * @return
	 */
	private Room randomRoom() {
		int roomIndex = nextInt(rooms.size());
		return rooms.get(roomIndex);
	}

	private void generateRoom(Room room) {
		// Size rnd(3~5)*2+1
		int halfW = nextInt(2) + 3;
		int halfH = nextInt(2) + 3;
		int width = halfW * 2 + 1;
		int height = halfH * 2 + 1;
		
		Room newRoom = new Room(width, height);
		newRoom.type = Type.ROOM;

		// new room;
		int mid;

		Point door = null;

		int dir = nextInt(DirectionCount);
		switch (dir) {
		case North:
			// NORTH
			mid = nextInt(room.width-2) + 1;
			
			newRoom.x = room.x + mid - halfW;
			newRoom.y = room.y - height + 1;
			door = new Point(halfW, height-1);
			break;
		case South:
			// SOUTH
			mid = nextInt(room.width-2) + 1 + room.x;
			
			newRoom.x = mid - halfW;
			newRoom.y = room.x + room.height - 1;
			door = new Point(halfW, 0);
			break;
		case East:
			// EAST
			mid = nextInt(room.height-2) + 1;
			
			newRoom.x = room.x + room.width - 1;
			newRoom.y = room.y + mid - halfH;
			door = new Point(0, halfH);
			break;
		case West:
			// WEST
			mid = nextInt(room.height-2) + 1 + room.y;
			
			newRoom.x = room.x - width + 1;
			newRoom.y = mid - halfH;
			door = new Point(width-1, halfH);
			break;
		default:
			// Nothing
		}

		if (checkOn(newRoom, dir)) {
			rooms.add(newRoom);
			newRoom.addDoor(door);
			newRoom.print();
		}
	}

	private void generateCorridor(Room room, boolean junction) {
		
		if (room.doors.size() > 2) {
			return;
		}
		
		int mid, height, width;

		Room newRoom = null;
		Point door = null;
		Point junctionDoor = null;
		
		int dir = nextInt(DirectionCount);
		switch (dir) {
		case North:
			if (room.width <= 3) {
				mid = 1;
			} else {
				mid = nextInt(room.width - 2) + 1;
			}
			height = nextInt(4) + 5;

			newRoom = new Room(3, height);
			newRoom.type = Type.CORRIDOR;
			newRoom.x = room.x + mid - 1;
			newRoom.y = room.y - height + 1;
			
			door = new Point(1, height-1);
			junctionDoor = new Point(mid, 0);
			break;	
		case South:
			if (room.width <= 3) {
				mid = 1;
			} else {
				mid = nextInt(room.width - 2) + 1;
			}
			height = nextInt(4) + 5;
			
			newRoom = new Room(3, height);
			newRoom.type = Type.CORRIDOR;
			newRoom.x = room.x + mid - 1;
			newRoom.y = room.y + room.height - 1;
			
			door = new Point(1, 0);
			junctionDoor = new Point(1, room.height-1);
			break;	
		case East:
			if (room.height <= 3) {
				mid = 1;
			} else {
				mid = nextInt(room.height - 2) + 1;
			}
			width = nextInt(4) + 5;
			
			newRoom = new Room(width, 3);
			newRoom.type = Type.CORRIDOR;
			newRoom.x = room.x + room.width - 1;
			newRoom.y = room.y + mid - 1;
			door = new Point(0, 1);
			junctionDoor = new Point(room.width-1, mid);
			break;	
		case West:
			if (room.height <= 3) {
				mid = 1;
			} else {
				mid = nextInt(room.height - 2) + 1;
			}
			width = nextInt(4) + 5;
			
			newRoom = new Room(width, 3);
			newRoom.type = Type.CORRIDOR;
			newRoom.x = room.x - width + 1;
			newRoom.y = room.y + mid - 1;

			door = new Point(width-1, 1);
			junctionDoor = new Point(0, mid);
			break;	
		default:
			break;	
		}
		
		if (checkOn(newRoom, dir)) {
			rooms.add(newRoom);
			newRoom.addDoor(door);
			if (junction) {
				room.addDoor(junctionDoor);
			}
			newRoom.print();
		}
	}

	private boolean checkOn(Room room, int dir) {
		if (room.x < 0 || room.y < 0 || (room.x + room.width) >= this.width || (room.y + room.height) >= this.height)
			return false;

		int xstart = 0, ystart = 0;
		int xend = 0, yend = 0;
		switch (dir) {
		case North:
			yend = 1;
			break;
		case South:
			ystart = 1;
			break;
		case East:
			xstart = 1;
			break;
		case West:
			xend = 1;
			break;
		}
		for (int y = ystart; y < room.height-yend; y++) {
			for (int x = xstart; x < room.width-xend; x++) {
				if (!(map.get(room.x+x, room.y+y) == Unused))
					return false;
			}
		}
		return true;
	}

	enum Type {
		ROOM, CORRIDOR;
	}

	private class Room {
		Type type;

		// size
		int width;
		int height;

		// pos
		int x = 0;
		int y = 0;

		// tiles
		int[][] tiles;

		// doors
		List<Point> doors;

		Room(int width, int height) {
			assert width > 3;
			assert height > 3;

			this.width = width;
			this.height = height;
			tiles = new int[height][width];

			doors = new ArrayList<Point>();
			// surround the room with walls, and fill the rest with floors.
			for (int y = 0; y < height; y++) {
				for (int x = 0; x < width; x++) {
					if (y == 0 || y == height-1 || x == 0 || x == width-1) {
						tiles[y][x] = Wall;
					} else {
						tiles[y][x] = Floor;
					}
				}
			}
		}

		void addDoor(Point door) {
			if (door != null) {
				doors.add(door);
				tiles[door.y][door.x] = Door;
			}
		}

		/**
		 * find out if we have any stair tiles in the room
		 * 
		 * @return
		 */
		boolean hasStairs() {
			for (int y = 0; y < height; y++) {
				for (int x = 0; x < width; x++) {
					if (this.tiles[y][x] == DownStairs || this.tiles[y][x] == UpStairs) {
						return true;
					}
				}
			}
			return false;
		};

		void print() {
			for (int y = 0; y < height; y++) {
				for (int x = 0; x < width; x++) {
					map.set(this.x + x, this.y + y, tiles[y][x]);
				}
			}
		}
	}
}
