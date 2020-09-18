package net.jmecn.map.creator;

import static net.jmecn.map.Tile.*;

/**
 * Diffusion-limited aggregation
 * 
 * http://www.roguebasin.com/index.php?title=Diffusion-limited_aggregation
 * 
 * @author yanmaoyuan
 *
 */
public class CaveDLA extends MapCreator {

	public CaveDLA(int width, int height) {
		super("creator.cave.dla", width, height);
	}

	@Override
	public void initialze() {
		map.fill(Wall);
	}

	@Override
	public void create() {
		int y = 0, x = 0;
		int builderSpawned = 0;
		int builderMoveDirection = 0;
		int allocatedBlocks = 0; // variable used to track the percentage of the map filled
		int rootX = width / 2, rootY = height / 2; // this is where the growth starts from. Currently center of map
		int stepped = 0; // this is how long corridors can be
		boolean orthogonalAllowed = false; // Orthogonal movement allowed? If not, it carves a wider cooridor on diagonal

		int[][] map = this.map.getMap();

		/* The Diffusion Limited Aggregation Loop */
		while (allocatedBlocks < ((width * height) / 8)) { // quit when an eighth of the map is filled
			if (builderSpawned != 1) {
				// Spawn at random position
				x = nextInt(width - 2) + 1;
				y = nextInt(height - 2) + 1;
				// See if builder is ontop of root
				if (Math.abs(rootX - y) <= 0 && Math.abs(rootY - x) <= 0) {
					// builder was spawned too close to root, clear that floor
					// and respawn
					if (map[y][x] != Floor) {
						map[y][x] = Floor;
						allocatedBlocks++;
					} // end if
				} else {
					builderSpawned = 1;
					builderMoveDirection = nextInt(8);
					stepped = 0;
				} // end if
			} else {
				// builder already spawned and knows it's direction, move
				// builder
				/* North */ if (builderMoveDirection == 0 && x > 0) {
					x--;
					stepped++;
					/* East */ } else if (builderMoveDirection == 1 && y < height - 1) {
					y++;
					stepped++;
					/* South */ } else if (builderMoveDirection == 2 && x < width - 1) {
					x++;
					stepped++;
					/* West */ } else if (builderMoveDirection == 3 && y > 0) {
					y++;
					stepped++;
					/* Northeast */ } else if (builderMoveDirection == 4 && y < height - 1 && x > 0) {
					x--;
					y++;
					stepped++;
					/* Southeast */ } else if (builderMoveDirection == 5 && y < height - 1 && x < width - 1) {
					x++;
					y++;
					stepped++;
					/* Southwest */ } else if (builderMoveDirection == 6 && y > 0 && x < width - 1) {
					x++;
					y--;
					stepped++;
					/* Northwest */ } else if (builderMoveDirection == 7 && y > 0 && x > 0) {
					x--;
					y--;
					stepped++;
				}
				/* ensure that the builder is touching an existing spot */
				if (y < height - 1 && x < width - 1 && y > 0 && x > 0 && stepped <= 5) {
					/* East */ if (map[y + 1][x] == Floor) {
						if (map[y][x] != Floor) {
							map[y][x] = Floor;
							allocatedBlocks++;
						}
						/* West */ } else if (map[y - 1][x] == Floor) {
						if (map[y][x] != Floor) {
							map[y][x] = Floor;
							allocatedBlocks++;
						}
						/* South */ } else if (map[y][x + 1] == Floor) {
						if (map[y][x] != Floor) {
							map[y][x] = Floor;
							allocatedBlocks++;
						}
						/* North */ } else if (map[y][x - 1] == Floor) {
						if (map[y][x] != Floor) {
							map[y][x] = Floor;
							allocatedBlocks++;
						}
						/* Northeast */ } else if (map[y + 1][x - 1] == Floor) {
						if (map[y][x] != Floor) {
							map[y][x] = Floor;
							allocatedBlocks++;
							if (!orthogonalAllowed) {
								map[y + 1][x] = Floor;
								allocatedBlocks++;
							}
						}
						/* Southeast */ } else if (map[y + 1][x + 1] == Floor) {
						if (map[y][x] != Floor) {
							map[y][x] = Floor;
							allocatedBlocks++;
							if (!orthogonalAllowed) {
								map[y + 1][x] = Floor;
								allocatedBlocks++;
							}
						}
						/* Southwest */ } else if (map[y - 1][x + 1] == Floor) {
						if (map[y][x] != Floor) {
							map[y][x] = Floor;
							allocatedBlocks++;
							if (!orthogonalAllowed) {
								map[y - 1][x] = Floor;
								allocatedBlocks++;
							}
						}
						/* Northwest */ } else if (map[y - 1][x - 1] == Floor) {
						if (map[y][x] != Floor) {
							map[y][x] = Floor;
							allocatedBlocks++;
							if (!orthogonalAllowed) {
								map[y - 1][x] = Floor;
								allocatedBlocks++;
							}
						}
					}
				} else {
					builderSpawned = 0;
				}
			} // end if
		} // end while
	}

}
