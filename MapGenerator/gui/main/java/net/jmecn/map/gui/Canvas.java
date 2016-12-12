package net.jmecn.map.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

import net.jmecn.map.Map2D;
import net.jmecn.map.Tile;

/**
 * The renderer
 * @author yan
 *
 */
public class Canvas extends JPanel {
	private BufferedImage image;

	private int SIZE = 12;
	
	private Color wallColor = new Color(96, 96, 96);
	private Color floorColor = new Color(255, 242, 182);
	
	public Canvas() {
		this(12);
	}
	
	public Canvas(int px) {
		setPixel(px);
		
		this.addMouseListener(listener);
		this.addMouseMotionListener(listener);
	}
	
	MouseAdapter listener = new MouseAdapter() {
		private void changeMap( int x, int y, int type) {
			if (map == null) {
				return;
			}
			
			x -= 5;
			y -= 5;
			x /= SIZE;
			y /= SIZE;
			
			if (x >= 0 && y >= 0 && x < col && y < row) {
				if (map[y][x] != type) {
					map[y][x] = type;
					drawImage();
					updateUI();
				}
			}
		}
		
		private boolean lPressed = false;
		private boolean rPressed = false;
		
		public void mousePressed(MouseEvent e) {
			switch (e.getButton()) {
			case MouseEvent.BUTTON1 : // left
				lPressed = true;
				changeMap(e.getX(), e.getY(), Tile.Wall);
				break;
			case MouseEvent.BUTTON3 : // right
				rPressed = true;
				changeMap(e.getX(), e.getY(), Tile.Floor);
				break;
			}
		}
		public void mouseReleased(MouseEvent e) {
			switch (e.getButton()) {
			case MouseEvent.BUTTON1 : // left
				lPressed = false;
				break;
			case MouseEvent.BUTTON3 : // right
				rPressed = false;
				break;
			}
		}
		public void mouseDragged(MouseEvent e) {
			
			if (lPressed) {
				changeMap(e.getX(), e.getY(), Tile.Wall);
			} else if (rPressed) {
				changeMap(e.getX(), e.getY(), Tile.Floor);
			}
		}
	};
	
	public void setPixel(int px) {
		if (px < 7) px = 7;
		SIZE = px;
	}
	private static final long serialVersionUID = -3767183299577116646L;

	int[][] map;
	int col = 0;
	int row = 0;

	int width = 0;
	int height = 0;

	public void setMap(Map2D map) {
		this.map = map.getMap();
		this.row = map.getHeight();
		this.col = map.getWidth();

		width = col * SIZE + 9;
		height = row * SIZE + 9;

		image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

		drawImage();
		this.setPreferredSize(new Dimension(width, height));
	}

	private void drawImage() {
		image.flush();

		Graphics g = image.getGraphics();
		
		g.setColor(Color.white);
		g.fillRect(0, 0, width, height);
		
		for (int y = 0; y < row; y++) {
			g.setColor(wallColor);
			for (int x = 0; x < col; x++) {
				switch (map[y][x]) {
				case Tile.Unused: {
					// skip it
					break;
				}
				case Tile.Floor: {
					drawDirtFloor(g, x, y);
					break;
				}
				case Tile.Wall: {
					drawWall(g, x, y);
					break;
				}
				case Tile.Stone: {
					drawStoneWall(g, x, y);
					break;
				}
				case Tile.Corridor: {
					drawCorridor(g, x, y);
					break;
				}
				case Tile.Door: {
					drawDoor(g, x, y);
					break;
				}
				case Tile.UpStairs: {
					drawUpStairs(g, x, y);
					break;
				}
				case Tile.DownStairs: {
					drawDownStairs(g, x, y);
					break;
				}
				case Tile.Water: {
					drawWater(g, x, y);
					break;
				}
				case Tile.Grass: {
					drawGrass(g, x, y);
					break;
				}
				case Tile.Tree: {
					drawTree(g, x, y);
					break;
				}
				}
			}
		}
	}

	private void drawDirtFloor(Graphics g, int posX, int posY) {
		drawBox(g, posX, posY, floorColor);
	}
	
	private void drawWall(Graphics g, int posX, int posY) {
		drawBox(g, posX, posY, wallColor);
	}

	private void drawStoneWall(Graphics g, int posX, int posY) {
		int x = 5 + posX * SIZE;
		int y = 5 + posY * SIZE;

		g.setColor(new Color(0xEE, 0xEE, 0xFF));
		g.fillRect(x + 1, y + 1 , SIZE - 2, SIZE - 2);
		
		g.setColor(Color.BLACK);
		
		g.drawLine(x + 1, y, x + SIZE - 2, y);
		g.drawLine(x + 1, y + SIZE - 1, x + SIZE - 2, y + SIZE - 1);
		g.drawLine(x, y + 1, x, y + SIZE - 2);
		g.drawLine(x + SIZE - 1, y + 1, x + SIZE - 1, y + SIZE - 2);
		
		g.drawLine(x + 2, y + SIZE - 3, x + SIZE - 3, y + SIZE - 3);
		g.drawLine(x + SIZE - 3, y + 2, x + SIZE - 3, y + SIZE - 3);
	}

	private void drawDoor(Graphics g, int posX, int posY) {
		int x = 5 + posX * SIZE;
		int y = 5 + posY * SIZE;

		g.setColor(Color.orange);
		g.fillRect(x + 1, y + 1 , SIZE - 2, SIZE - 2);
		g.setColor(Color.darkGray);
		g.drawRect(x, y, SIZE - 1, SIZE - 1);
		g.drawLine(x+2, y+3, x+2, y+4);
		
	}
	
	private void drawCorridor(Graphics g, int posX, int posY) {
		int x = 5 + posX * SIZE;
		int y = 5 + posY * SIZE;

		g.setColor(Color.GRAY);
		g.drawRect(x+1, y+1, SIZE-2, SIZE-2);
	}
	
	private void drawUpStairs(Graphics g, int posX, int posY) {
		int x = 5 + posX * SIZE;
		int y = 5 + posY * SIZE;

		g.setColor(Color.green);
		g.fillRect(x + 1, y + 1 , SIZE - 2, SIZE - 2);
	}
	
	private void drawDownStairs(Graphics g, int posX, int posY) {
		int x = 5 + posX * SIZE;
		int y = 5 + posY * SIZE;

		g.setColor(Color.RED);
		g.fillRect(x + 1, y + 1 , SIZE - 2, SIZE - 2);
	}
	
	private void drawWater(Graphics g, int posX, int posY) {
		drawBox(g, posX, posY, Color.BLUE);
	}
	
	private void drawGrass(Graphics g, int posX, int posY) {
		drawBox(g, posX, posY, new Color(0, 126, 0));
	}
	
	private void drawTree(Graphics g, int posX, int posY) {
		int x = 5 + posX * SIZE;
		int y = 5 + posY * SIZE;
		
		int size = SIZE;
		if (SIZE % 2 == 0) {
			size--;
		}
		
		g.setColor(Color.GREEN);
		g.fillOval(x, y , size, SIZE/2+2);
		g.setColor(Color.DARK_GRAY);
		g.fillRect(x+size/2-1, y+SIZE/2+1, 3, SIZE/2);
	}
	
	private void drawBox(Graphics g, int posX, int posY, Color color) {
		int x = 5 + posX * SIZE;
		int y = 5 + posY * SIZE;
		
		g.setColor(color);
		g.fillRect(x, y , SIZE, SIZE);
	}
	
	@Override
	public void paint(Graphics g) {
		super.paint(g);
		g.drawImage(image, 0, 0, null);
	}
	
	@Override
	public void update(Graphics g) {
		paint(g);
	}
	
	public BufferedImage getImage() {
		return image;
	}
}
