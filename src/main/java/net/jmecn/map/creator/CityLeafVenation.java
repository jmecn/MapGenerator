package net.jmecn.map.creator;

import static net.jmecn.map.Tile.*;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * An Implementation of City Generation by Leaf Venation
 * 
 * The source was wrote on Python :
 * http://www.roguebasin.com/index.php?title=An_Implementation_of_City_Generation_by_Leaf_Venation
 * 
 * @author yanmaoyuan
 *
 */
public class CityLeafVenation extends MapCreator {

	static Logger logger = Logger.getLogger(CityLeafVenation.class.getName());

	private final static double RADIUS_INCREASE = 10.0;
	private final static double NEW_SITE_DENSITY = 0.0002;
	private final static double AUXIN_BIRTH_THRESHOLD = 2.0;
	private final static double VEIN_BIRTH_THRESHOLD = 2.0;
	private final static double VEIN_DEATH_THRESHOLD = 1.0;
	private final static double NODE_DISTANCE = 1.0;

	private final static int ITERATIONS = 20;

	// DATA
	private List<Auxin> auxinSites;
	private List<Vein> veinSites;
	private int usedVeinIDs = 1;
	private int usedAuxinIDs = 0;
	private double radius = 10.0;

	public CityLeafVenation(int width, int height) {
		super("creator.city.leafvenation", width, height);

		auxinSites = new ArrayList<Auxin>();
		veinSites = new ArrayList<Vein>();
	}

	@Override
	public void initialze() {
		usedVeinIDs = 0;
		usedAuxinIDs = 0;
		radius = 10.0;

		// int start_id = new_vein_id();
		auxinSites.clear();
		veinSites.clear();
		veinSites.add(new Vein(usedVeinIDs++));
	}

	@Override
	public void create() {
		for (int i = 0; i < ITERATIONS; i++) {
			iteration();
		}
		
		draw();
	}

	private void iteration() {
		modifyShape();
		generateNewAuxin();
		List<Vein> new_veins = generateNewVein();
		killVeins(new_veins);
	}

	private void modifyShape() {
		radius += RADIUS_INCREASE;
	}

	private Coords pickSite() {
		double r = rand.nextDouble() * radius;
		double theta = rand.nextDouble() * 2 * Math.PI - Math.PI;
		double x = Math.sqrt(r) * Math.cos(theta);
		double y = Math.sqrt(r) * Math.sin(theta);
		return new Coords(x, y);
	}

	private int newSites() {
		return (int) (NEW_SITE_DENSITY * Math.PI * this.radius * this.radius);
	}

	/**
	 * Generate new auxin nodes
	 */
	private void generateNewAuxin() {
		int n = newSites();
		for (int i = 0; i < n; i++) {
			Coords site = pickSite();

			// Test each auxin node to see if we are too close
			boolean failed = false;
			for (int id = 0; id < usedAuxinIDs; id++) {
				Auxin auxin = auxinSites.get(id);
				if (site.distance(auxin.coords) < AUXIN_BIRTH_THRESHOLD) {
					failed = true;
					break;
				}
			}
			if (failed) {
				continue;
			}
			// Test each vein node to see if we are too close
			for (int id = 0; id < usedVeinIDs; id++) {
				Vein vein = veinSites.get(id);
				if (site.distance(vein.coords) < VEIN_BIRTH_THRESHOLD) {
					failed = true;
					break;
				}
			}
			if (failed) {
				continue;
			}
			// Create new auxin site
			auxinSites.add(new Auxin(usedAuxinIDs++, site));
		}
	}
	
	/**
	 * Create new veins
	 * @return
	 */
	private List<Vein> generateNewVein() {
		List<Vein> newVeins = new ArrayList<Vein>();
		int veinLen = veinSites.size();
		for (int veinId = 0; veinId<veinLen; veinId++) {
			Vein vein = veinSites.get(veinId);
			
			// Identify close auxin nodes
			List<Integer> linkedAuxin = new ArrayList<Integer>();
			for (int auxinId = 0; auxinId<usedAuxinIDs; auxinId++) {
				Auxin auxin = auxinSites.get(auxinId);
				// Only allow tagged veins to grow towards dead auxin
				if (auxin.tag && !vein.tags.contains(auxinId)) {
					continue;
				}
				
				// Check to see if there are any closer veins
				double distance = auxin.distanceSquared(vein);
				boolean closer = false;
				for (int testVeinId = 0; testVeinId < veinLen; testVeinId++) {
					if (testVeinId == veinId) {
						continue;
					}
					
					Vein testVein = veinSites.get(testVeinId);
					double distance1 = auxin.distanceSquared(testVein);
					double distance2 = vein.distanceSquared(testVein);
					if (distance1 < distance && distance2 < distance) {
						closer = true;
						break;
					}
				}
				if (!closer) {
					linkedAuxin.add(auxinId);
					auxin.linked.add(veinId);
				}
			}

			// Tag cleanup
			// vein_sites.get(vein_id).tags = filter(lambda tag: tag in linked_auxin, vein.tags);
			List<Integer> filter = new ArrayList<Integer>();
			for (int i = 0; i < vein.tags.size(); i++) {
				int tag = vein.tags.get(i);
				if (linkedAuxin.contains(tag)) {
					filter.add(tag);
				}
			}
			vein.tags.clear();
			vein.tags.addAll(filter);
			
			// Calculate the new co-ords
			if (linkedAuxin.size() == 0) {
				continue;
			}
			double sum_x = 0;
			double sum_y = 0;
			int total = linkedAuxin.size();
			for (int j = 0; j < total; j++) {
				int auxin_id = linkedAuxin.get(j);
				Auxin auxin = auxinSites.get(auxin_id);
				double dx = auxin.coords.x - vein.coords.x;
				double dy = auxin.coords.y - vein.coords.y;
				double scale = NODE_DISTANCE / Math.sqrt(dx * dx + dy * dy);
				sum_x += dx * scale;
				sum_y += dy * scale;
			}

			double avgX = sum_x / total + vein.coords.x;
			double avgY = sum_y / total + vein.coords.y;
			Coords new_coords = new Coords(avgX, avgY);
			// Create the new vein node
			newVeins.add(new Vein(usedVeinIDs++, new_coords, veinId, vein.tags));
			vein.tags.clear();
		}

		veinSites.addAll(newVeins);
		return newVeins;
	}

	private void killVeins(List<Vein> new_veins) {
		int auxin_sites_len = auxinSites.size();
		int vein_sites_len = veinSites.size();
		int new_veins_len = new_veins.size();
		for (int auxin_id = 0; auxin_id < auxin_sites_len; auxin_id++) {
			Auxin auxin = auxinSites.get(auxin_id);
			// Find overly close auxin nodes
			if (!auxin.tag) {
				for (int vein_id = 0; vein_id < vein_sites_len; vein_id++) {
					Vein vein = veinSites.get(vein_id);
					if (auxin.coords.distance(vein.coords) < VEIN_DEATH_THRESHOLD) {
						// Kill the node
						int newID = usedVeinIDs++;
						this.veinSites.add(new Vein(newID, auxin.coords));
						auxin.vein = newID;
						auxin.tag = true;
						// affected_veins = filter(lambda ID: ID in auxin['LINKED'], this.vein_sites);
						List<Integer> affected_veins = new ArrayList<Integer>();
						for (int id = 0; id < vein_sites_len; id++) {
							if (auxin.linked.contains(id)) {
								affected_veins.add(id);
							}
						}

						for (int new_vein_id = 0; new_vein_id < new_veins_len; new_vein_id++) {
							Vein new_vein = veinSites.get(new_vein_id);
							if ((affected_veins.size() + new_vein.root.size()) > 0) {
								new_vein.tags.add(auxin_id);
							}
						}
						break;
					}
				}
			}
			// Process dead auxin nodes
			if (auxin.tag) {
				for (int vein_id = 0; vein_id < vein_sites_len; vein_id++) {
					Vein vein = veinSites.get(vein_id);
					if (!vein.tags.contains(auxin_id))
						continue;
					if (auxin.coords.distance(vein.coords) < VEIN_DEATH_THRESHOLD) {
						vein.tags.remove(auxin_id);
						veinSites.get(auxin.vein).root.add(vein_id);
						veinSites.get(auxin.vein).tags.addAll(vein.tags);
						vein.tags.clear();
					}
				}
			}
			// Clear for next cycle
			auxin.linked.clear();
		}
	}

	class Coords {
		public double x;
		public double y;

		public Coords() {
			x = y = 0;
		}

		public Coords(double x, double y) {
			this.x = x;
			this.y = y;
		}

		public double distance(Coords p) {
			double dx = this.x - p.x;
			double dy = this.y - p.y;
			return Math.sqrt(dx * dx + dy * dy);
		}

		public double distanceSquared(Coords p) {
			double dx = this.x - p.x;
			double dy = this.y - p.y;
			return dx * dx + dy * dy;
		}
	}

	class Auxin {
		protected int id;
		protected Coords coords;
		protected int vein;
		protected boolean tag;
		protected List<Integer> linked;

		public Auxin() {
			this.id = 0;
			this.coords = new Coords();
			this.vein = -1;
			this.tag = false;
			this.linked = new ArrayList<Integer>();
		}

		public Auxin(int id, Coords coords) {
			this.id = id;
			this.coords = coords;
			this.vein = -1;
			this.tag = false;
			this.linked = new ArrayList<Integer>();
		}

		public Auxin(int id, Coords coords, boolean tag, List<Integer> linked) {
			this.id = id;
			this.coords = coords;
			this.vein = -1;
			this.tag = tag;
			this.linked = linked;
		}
		
		public double distanceSquared(Vein v) {
			return this.coords.distanceSquared(v.coords);
		}
	}

	class Vein {
		protected int id;
		protected Coords coords;
		protected List<Integer> root;
		protected List<Integer> tags;

		public Vein() {
			id = 0;
			coords = new Coords(0, 0);
			root = new ArrayList<Integer>();
			tags = new ArrayList<Integer>();
		}

		public Vein(int id) {
			this.id = id;
			coords = new Coords(0, 0);
			root = new ArrayList<Integer>();
			tags = new ArrayList<Integer>();
		}

		public Vein(int id, Coords coords) {
			this.id = id;
			this.coords = coords;
			this.root = new ArrayList<Integer>();
			this.tags = new ArrayList<Integer>();
		}

		public Vein(int id, Coords coords, int root, List<Integer> tags) {
			this.id = id;
			this.coords = coords;
			this.root = new ArrayList<Integer>();
			this.root.add(root);
			this.tags = new ArrayList<Integer>();
			if (tags != null) {
				tags.addAll(tags);
			}
		}
		public double distanceSquared(Vein v) {
			return this.coords.distanceSquared(v.coords);
		}
	}

	/**
	 * Draw the city
	 */
	private void draw() {
		int centreX = (int) (width * 0.5f + 0.5f);
		int centreY = (int) (height * 0.5f + 0.5f);
		
		// find the farest coords to calculate scale
		Coords centre = new Coords(0, 0);
		double maxLength = 1;
		for (Vein vein : veinSites) {
			Coords coords = vein.coords;
			double dis = coords.distance(centre); 
			if (dis > maxLength) {
				maxLength = dis;
			}
		}
		double scale = Math.min(centreX-2, centreY-2) / maxLength;
		
		map.fill(Floor);
		map.buildBoundary(Stone);
		
		for (Vein vein : veinSites) {
			Coords coords = vein.coords;
			int x = (int) (centreX + coords.x * scale);
			int y = (int) (centreY + coords.y * scale);
			for (int root_id : vein.root) {
				Coords root_coords = veinSites.get(root_id).coords;
				int root_x = (int) (centreX + root_coords.x * scale);
				int root_y = (int) (centreY + root_coords.y * scale);
				drawLine(root_x, root_y, x, y, Corridor);
			}
		}
	}
	
	private void drawLine(int x1, int y1, int x2, int y2, int tile) {
		int a = Math.abs(x2 - x1);
		int b = Math.abs(y2 - y1);

		if (a >= b) {
			int xStep = 1;
			if (x2 < x1) xStep = -1;
			float yStep = (float) (y2- y1) / a;
			// use xStep
			int x = x1;
			int y = y1;
			for (int i = 0; i <= a; i++) {
				x = x1 + xStep * i;
				y = y1 +(int)(yStep * i);
				map.set(x, y, tile);
			}

		} else {
			// use yStep
			int yStep = 1;
			if (y2 < y1) yStep = -1;
			float xStep = (float) (x2 - x1) / b;
			// use xStep
			int x = x1;
			int y = y1;
			for (int i = 0; i <= b; i++) {
				x = x1 + (int) (xStep * i);
				y = y1 + yStep * i;
				map.set(x, y, tile);
			}
		}
	}
	
	public static void main(String[] args) {
		CityLeafVenation leaf = new CityLeafVenation(40, 40);
		leaf.initialze();
		leaf.create();
		leaf.getMap().printMapChars();
	}
}
