package net.jmecn.map.creator;

/// The random dungeon generator.
///
/// Starting with a stage of solid walls, it works like so:
///
/// 1. Place a number of randomly sized and positioned rooms. If a room
///    overlaps an existing room, it is discarded. Any remaining rooms are
///    carved out.
/// 2. Any remaining solid areas are filled in with mazes. The maze generator
///    will grow and fill in even odd-shaped areas, but will not touch any
///    rooms.
/// 3. The result of the previous two steps is a series of unconnected rooms
///    and mazes. We walk the stage and find every tile that can be a
///    "connector". This is a solid tile that is adjacent to two unconnected
///    regions.
/// 4. We randomly choose connectors and open them or place a door there until
///    all of the unconnected regions have been joined. There is also a slight
///    chance to carve a connector between two already-joined regions, so that
///    the dungeon isn't single connected.
/// 5. The mazes will have a lot of dead ends. Finally, we remove those by
///    repeatedly filling in any open tile that's closed on three sides. When
///    this is done, every corridor in a maze actually leads somewhere.
///
/// The end result of this is a multiply-connected dungeon with rooms and lots
/// of winding corridors.
///
/// @author https://github.com/munificent
/**
 * 
 * project:
 * https://github.com/munificent/hauberk
 * 
 * article:
 * http://journal.stuffwithstuff.com/2014/12/21/rooms-and-mazes/
 * demo for this article：
 * https://github.com/munificent/rooms-and-mazes
 * 
 * source:
 * https://github.com/munificent/hauberk/blob/db360d9efa714efb6d937c31953ef849c7394a39/lib/src/content/dungeon.dart
 * 
 * 
 * 
 * 
 * 
 * @author yanmaoyuan
 *
 */
public class DungeonHauberk extends MapCreator {

	public DungeonHauberk(int width, int height) {
		super("creator.dungeon.hauberk", width, height);
	}

	@Override
	public void initialze() {
		// TODO Auto-generated method stub

	}

	@Override
	public void create() {
		// TODO Auto-generated method stub

	}

}
