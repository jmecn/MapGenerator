package net.jmecn.map.creator;

import java.util.ArrayList;
import java.util.List;

import net.jmecn.map.Point;
import net.jmecn.map.Rect;

import static net.jmecn.map.Direction.*;
import static net.jmecn.map.Tile.*;

/**
 * A dungeon algorithm in JavaScript
 * 
 * http://nickgravelyn.github.io/dungeon/
 * 
 * @author yanmaoyuan
 *
 */
public class DungeonNickgravelyn extends MapCreator {

	private int minRoomSize;
	private int maxRoomSize;
	private int maxNumRooms;
	private int maxRoomArea;
	private boolean addStairsUp;
	private boolean addStairsDown;
	private List<Room> rooms;
	private List<Room>[][] roomGrid;

	@SuppressWarnings("unchecked")
	public DungeonNickgravelyn(int width, int height) {
		super("creator.dungeon.nickgravelyn", width, height);

		this.minRoomSize = 5;
		this.maxRoomSize = 15;
		this.maxNumRooms = 50;
		this.maxRoomArea = 150;

		this.addStairsUp = true;
		this.addStairsDown = true;

		this.rooms = new ArrayList<Room>();
		this.roomGrid = new ArrayList[height][width];
	}

	@Override
	public void initialze() {
		map.fill(Unused);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void create() {
		// clear
		this.rooms = new ArrayList<Room>();
		this.roomGrid = new ArrayList[height][width];
		for(int y=0; y<height; y++) {
			for(int x=0; x<width; x++) {
				roomGrid[y][x] = new ArrayList<Room>();
			}
		}

		// seed the map with a starting randomly sized room in the center of the
		// map
		Room room = this.createRandomRoom();
		room.x = (int) (Math.floor(width / 2) - Math.floor(room.width / 2));
		room.y = (int) (Math.floor(height / 2) - Math.floor(room.height / 2));
		this.addRoom(room);

		// continue generating rooms until we hit our cap or have hit our
		// maximum iterations (generally
		// due to not being able to fit any more rooms in the map)
		int iter = maxNumRooms * 5;
		while ((maxNumRooms <= 0 || rooms.size() < maxNumRooms) && iter-- > 0) {
			generateRoom();
		}

		// now we want to randomly add doors between some of the rooms and other
		// rooms they touch
		for (int i = 0; i < this.rooms.size(); i++) {
			// find all rooms that we could connect with this one
			List<Room> targets = getPotentiallyTouchingRooms(rooms.get(i));
			for (int j = 0; j < targets.size(); j++) {
				// make sure the rooms aren't already connected with a door
				if (!areRoomsConnected(rooms.get(i), targets.get(j))) {
					// 20% chance we add a door connecting the rooms
					if (Math.random() < 0.2) {
						addDoor(findNewDoorLocation(rooms.get(i), targets.get(j)));
					}
				}
			}
		}

		// add stairs if desired
		if (addStairsDown) {
			addStairs(DownStairs);
		}
		if (addStairsUp) {
			addStairs(UpStairs);
		}
		
		
		for(int i=0; i<rooms.size(); i++) {
			Room r = rooms.get(i);
			
			for(int y = 0; y<r.height; y++) {
				for(int x = 0; x<r.width; x++) {
					map.set(x+r.x, y+r.y, r.tiles[y][x]);
				}
			}
		}
	}

	private boolean roomIntersect(Room room1, Room room2) {
		int x1 = room1.x;
		int y1 = room1.y;
		int w1 = room1.width;
		int h1 = room1.height;

		int x2 = room2.x;
		int y2 = room2.y;
		int w2 = room2.width;
		int h2 = room2.height;

		// the +1/-1 here are to allow the rooms one tile of overlap. this is to
		// allow the rooms to share walls
		// instead of always ending up with two walls between the rooms
		if (x1 + w1 <= x2 + 1 || x1 >= x2 + w2 - 1 || y1 + h1 <= y2 + 1 || y1 >= y2 + h2 - 1) {
			return false;
		}

		return true;
	}

	private boolean canFitRoom(Room room) {
		// make sure the room fits inside the dungeon
		if (room.x < 0 || room.x + room.width > this.width - 1) {
			return false;
		}
		if (room.y < 0 || room.y + room.height > this.height - 1) {
			return false;
		}

		// make sure this room doesn't intersect any existing rooms
		for (int i = 0; i < this.rooms.size(); i++) {
			Room r = this.rooms.get(i);
			if (this.roomIntersect(room, r)) {
				return false;
			}
		}

		return true;
	}

	private List<Room> touchingRooms;
	private List<Room> getPotentiallyTouchingRooms(Room room) {
		touchingRooms = new ArrayList<Room>();

		// iterate the north and south walls, looking for other rooms in those
		// tile locations
		for (int x = room.x + 1; x < room.x + room.width - 1; x++) {
			checkRoomList(x, room.y, room);
			checkRoomList(x, room.y + room.height - 1, room);
		}

		// iterate the west and east walls, looking for other rooms in those
		// tile locations
		for (int y = room.y + 1; y < room.y + room.height - 1; y++) {
			checkRoomList(room.x, y, room);
			checkRoomList(room.x + room.width - 1, y, room);
		}

		return touchingRooms;
	}

	// function that checks the list of rooms at a point in our grid for any
	// potential touching rooms
	private void checkRoomList(int x, int y, Room room) {
		List<Room> r = roomGrid[y][x];
		for (int i = 0; i < r.size(); i++) {
			// make sure this room isn't the one we're searching around and that
			// it isn't already in the list
			if (r.get(i) != room && touchingRooms.indexOf(r.get(i)) < 0) {
				// make sure this isn't a corner of the room (doors can't go
				// into corners)
				int lx = x - r.get(i).x;
				int ly = y - r.get(i).y;
				if ((lx > 0 && lx < r.get(i).width - 1) || (ly > 0 && ly < r.get(i).height - 1)) {
					touchingRooms.add(r.get(i));
				}
			}
		}
	};

	private Point findNewDoorLocation(Room room1, Room room2) {
		Point doorPos = new Point(-1, -1);

		// figure out the direction from room1 to room2
		int dir = UnknownDir;

		// north
		if (room1.y == room2.y - room1.height + 1) {
			dir = North;
		}
		// west
		else if (room1.x == room2.x - room1.width + 1) {
			dir = West;
		}
		// east
		else if (room1.x == room2.x + room2.width - 1) {
			dir = East;
		}
		// south
		else if (room1.y == room2.y + room2.height - 1) {
			dir = South;
		}

		// use the direction to find an appropriate door location
		switch (dir) {
		// north
		case North:
			doorPos.x = nextInt(Math.max(room2.x, room1.x) + 1,
					Math.min(room2.x + room2.width, room1.x + room1.width) - 1);
			doorPos.y = room2.y;
			break;
		// west
		case West:
			doorPos.x = room2.x;
			doorPos.y = nextInt(Math.max(room2.y, room1.y) + 1,
					Math.min(room2.y + room2.height, room1.y + room1.height) - 1);
			break;
		// east
		case East:
			doorPos.x = room1.x;
			doorPos.y = nextInt(Math.max(room2.y, room1.y) + 1,
					Math.min(room2.y + room2.height, room1.y + room1.height) - 1);
			break;
		// south
		case South:
			doorPos.x = nextInt(Math.max(room2.x, room1.x) + 1,
					Math.min(room2.x + room2.width, room1.x + room1.width) - 1);
			doorPos.y = room1.y;
			break;
		}

		return doorPos;
	}

	private int random(List<?> list) {
		return nextInt(list.size());
	}

	private Result findRoomAttachment(Room room) {
		// pick a room, any room
        Room r = rooms.get(random(rooms));
        
        Point pos = new Point();
        
        // randomly position this room on one of the sides of the random room
        switch (nextInt(0, 4)) {
            // north
            case North:
                pos.x = nextInt(r.x - room.width + 3, r.x + r.width - 2);
                pos.y = r.y - room.height + 1;
                break;
            // west
            case West:
                pos.x = r.x - room.width + 1;
                pos.y = nextInt(r.y - room.height + 3, r.y + r.height - 2);
                break;
            // east
            case East:
                pos.x = r.x + r.width - 1;
                pos.y = nextInt(r.y - room.height + 3, r.y + r.height - 2);
                break;
            // south
            case South:
                pos.x = nextInt(r.x - room.width + 3, r.x + r.width - 2);
                pos.y = r.y + r.height - 1;
                break;
        }
        
        Result result = new Result();
        result.position = pos;
        result.target = r;
        // return the position for this new room and the target room
        return result;
	}
	class Result {
		Room target;
		Point position;
	}

	boolean addRoom(Room room) {
		// if the room won't fit, we don't add it
		if (!canFitRoom(room)) {
			return false;
		}

		// add it to our main rooms list
		this.rooms.add(room);

		// update all tiles to indicate that this room is sitting on them. this
		// grid is used
		// when placing doors so all rooms in a space can be updated at the same
		// time.
		for (int y = room.y; y < room.y + room.height; y++) {
			for (int x = room.x; x < room.x + room.width; x++) {
				List<Room> list = this.roomGrid[y][x];
				list.add(room);
				this.roomGrid[y][x] = list;
			}
		}

		return true;
	};

	void addDoor(Point doorPos) {
		// get all the rooms at the location of the door
		List<Room> rooms = this.roomGrid[doorPos.y][doorPos.x];
		for (int i = 0; i < rooms.size(); i++) {
			Room r = rooms.get(i);

			// convert the door position from world space to room space
			int x = doorPos.x - r.x;
			int y = doorPos.y - r.y;

			// set the tile to be a door
			r.tiles[y][x] = Door;
		}
	};

	Room createRandomRoom() {
		int width = 0;
		int height = 0;
		int area = 0;

		// find an acceptable width and height using our min/max sizes while
		// keeping under
		// the maximum area
		do {
			width = nextInt(this.minRoomSize, this.maxRoomSize);
			height = nextInt(this.minRoomSize, this.maxRoomSize);
			area = width * height;
		} while (this.maxRoomArea > 0 && area > this.maxRoomArea);

		// create the room
		return new Room(width, height);
	}

	private void generateRoom() {
		// create the randomly sized room
		Room room = createRandomRoom();

		// only allow 150 tries at placing the room
		int iter = 150;
		while (iter-- > 0) {
			// attempt to find another room to attach this one to
			Result result = this.findRoomAttachment(room);

			// update the position of this room
			room.x = result.position.x;
			room.y = result.position.y;

			// try to add it. if successful, add the door between the rooms and
			// break the loop
			if (addRoom(room)) {
				addDoor(findNewDoorLocation(room, result.target));
				break;
			}
		}
	}

	private void addStairs(int type) {
        Room room = null;
        
        // keep picking random rooms until we find one that has only one door and doesn't already have stairs in it
        do { room = rooms.get(random(rooms)); } 
        while (room.getDoorLocations().size() > 1 || room.hasStairs());
        
        // build a list of all locations in the room that qualify for stairs
        List<Point> candidates = new ArrayList<Point>();
        for (int y = 1; y < room.height - 2; y++) {
            for (int x = 1; x < room.width - 2; x++) {
                // only put stairs on the floor
                if (room.tiles[y][x] != Floor) { continue; }
                
                // make sure this floor isn't right next to a door
                if (room.tiles[y - 1][x] == Door ||
                    room.tiles[y + 1][x] == Door ||
                    room.tiles[y][x - 1] == Door ||
                    room.tiles[y][x + 1] == Door) { continue; }
                    
                // add it to the candidate list
                candidates.add(new Point(x, y));
            }
        }
        
        // pick a random candidate location and make it the stairs
        Point loc = candidates.get(random(candidates));
        room.tiles[loc.y][loc.x] = type;
	};

	private boolean areRoomsConnected(Room room1, Room room2) {
		// iterate the doors in room1 and see if any are also a door in room2
		List<Point> doors = room1.getDoorLocations();
		for (int i = 0; i < doors.size(); i++) {
			Point d = doors.get(i);

			// move the door into "world space" using room1's position
			d.x += room1.x;
			d.y += room1.y;

			// move the door into room2 space by subtracting room2's position
			d.x -= room2.x;
			d.y -= room2.y;

			// make sure the position is valid for room2's tiles array
			if (d.x < 0 || d.x > room2.width - 1 || d.y < 0 || d.y > room2.height - 1) {
				continue;
			}

			// see if the tile is a door; if so this is a door from room1 to
			// room2 so the rooms are connected
			if (room2.tiles[d.y][d.x] == Door) {
				return true;
			}
		}

		return false;
	}

	private static class Room extends Rect {
		int[][] tiles;

		Room(int width, int height) {
			super(0, 0, width, height);
			tiles = new int[height][width];
			// surround the room with walls, and fill the rest with floors.
			for (int y = 0; y < height; y++) {
				for (int x = 0; x < width; x++) {
					if (y == 0 || y == height - 1 || x == 0 || x == width - 1) {
						tiles[y][x] = Wall;
					} else {
						tiles[y][x] = Floor;
					}
				}
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

		List<Point> getDoorLocations() {
			List<Point> doors = new ArrayList<Point>();

			// find all the doors and add their positions to the list
			for (int y = 0; y < height; y++) {
				for (int x = 0; x < width; x++) {
					if (this.tiles[y][x] == Door) {
						doors.add(new Point(x, y));
					}
				}
			}

			return doors;
		}
	}

}
