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
 * 画板
 * @author yan
 *
 */
public class Canvas extends JPanel {
	private BufferedImage image;// 图像缓冲区

	private int SIZE = 12;
	
	private Color wallColor = new Color(96, 96, 96);
	private Color floorColor = new Color(255, 242, 182);
	
	public Canvas() {
		this(12);// 默认12像素
	}
	
	/**
	 * 根据用户设置的像素值初始化画板。
	 * @param px
	 */
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
			
			// 设置方块坐标
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
		
		private boolean lPressed = false;// 鼠标左键
		private boolean rPressed = false;// 鼠标右键
		
		public void mousePressed(MouseEvent e) {
			switch (e.getButton()) {
			case MouseEvent.BUTTON1 : // left
				lPressed = true;
				changeMap(e.getX(), e.getY(), Tile.DirtWall);
				break;
			case MouseEvent.BUTTON3 : // right
				rPressed = true;
				changeMap(e.getX(), e.getY(), Tile.DirtFloor);
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
				changeMap(e.getX(), e.getY(), Tile.DirtWall);
			} else if (rPressed) {
				changeMap(e.getX(), e.getY(), Tile.DirtFloor);
			}
		}
	};
	/**
	 * 设置像素宽度
	 * 至少7 px
	 * @param px
	 */
	public void setPixel(int px) {
		if (px < 7) px = 7;
		SIZE = px;
	}
	/**
	 * 
	 */
	private static final long serialVersionUID = -3767183299577116646L;

	int[][] map;
	int col = 0;
	int row = 0;

	int width = 0;
	int height = 0;

	/**
	 * 设置迷宫地图
	 * 
	 * @param map
	 */
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

	/**
	 * 将迷宫地图画到缓冲区中。
	 */
	private void drawImage() {
		// 清屏
		image.flush();

		Graphics g = image.getGraphics();
		
		// 填充底色
		g.setColor(Color.white);
		g.fillRect(0, 0, width, height);
		
		// 循环绘制界面
		for (int y = 0; y < row; y++) {
			g.setColor(wallColor);
			for (int x = 0; x < col; x++) {
				// 绘制界面方块
				switch (map[y][x]) {
				case Tile.Unused: {
					// skip it
					break;
				}
				case Tile.DirtFloor: {
					drawDirtFloor(g, x, y);
					break;
				}
				case Tile.DirtWall: {
					drawDirtWall(g, x, y);
					break;
				}
				case Tile.StoneWall: {
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
				}
			}
		}
	}

	/**
	 * 绘制单个方块
	 * 
	 * @param g
	 *            画笔
	 * @param posX
	 *            横坐标
	 * @param posY
	 *            纵坐标
	 */
	private void drawDirtFloor(Graphics g, int posX, int posY) {
		// 设置方块坐标
		int x = 5 + posX * SIZE;
		int y = 5 + posY * SIZE;

		// 绘制方块底色
		g.setColor(floorColor);
		g.fillRect(x, y , SIZE, SIZE);
	}
	
	/**
	 * 绘制单个方块
	 * 
	 * @param g
	 *            画笔
	 * @param posX
	 *            横坐标
	 * @param posY
	 *            纵坐标
	 */
	private void drawDirtWall(Graphics g, int posX, int posY) {
		// 设置方块坐标
		int x = 5 + posX * SIZE;
		int y = 5 + posY * SIZE;

		// 绘制方块底色
		g.setColor(wallColor);
		g.fillRect(x, y , SIZE, SIZE);
	}

	/**
	 * 绘制单个方块
	 * 
	 * @param g
	 *            画笔
	 * @param posX
	 *            横坐标
	 * @param posY
	 *            纵坐标
	 */
	private void drawStoneWall(Graphics g, int posX, int posY) {
		// 设置方块坐标
		int x = 5 + posX * SIZE;
		int y = 5 + posY * SIZE;

		// 绘制方块底色
		g.setColor(new Color(0xEE, 0xEE, 0xFF));
		g.fillRect(x + 1, y + 1 , SIZE - 2, SIZE - 2);
		
		// 设置线段颜色
		g.setColor(Color.BLACK);
		// 绘制线段
		g.drawLine(x + 1, y, x + SIZE - 2, y);
		g.drawLine(x + 1, y + SIZE - 1, x + SIZE - 2, y + SIZE - 1);
		g.drawLine(x, y + 1, x, y + SIZE - 2);
		g.drawLine(x + SIZE - 1, y + 1, x + SIZE - 1, y + SIZE - 2);
		// 绘制立体的线段
		g.drawLine(x + 2, y + SIZE - 3, x + SIZE - 3, y + SIZE - 3);
		g.drawLine(x + SIZE - 3, y + 2, x + SIZE - 3, y + SIZE - 3);
	}

	/**
	 * 绘制门
	 * 
	 * @param g
	 *            画笔
	 * @param posX
	 *            横坐标
	 * @param posY
	 *            纵坐标
	 */
	private void drawDoor(Graphics g, int posX, int posY) {
		// 设置方块坐标
		int x = 5 + posX * SIZE;
		int y = 5 + posY * SIZE;

		// 绘制方块底色
		g.setColor(Color.orange);
		g.fillRect(x + 1, y + 1 , SIZE - 2, SIZE - 2);
		g.setColor(Color.darkGray);
		g.drawRect(x, y, SIZE - 1, SIZE - 1);
		g.drawLine(x+2, y+3, x+2, y+4);
		
	}
	
	/**
	 * 绘制路径点
	 * 
	 * @param g
	 *            画笔
	 * @param posX
	 *            横坐标
	 * @param posY
	 *            纵坐标
	 */
	private void drawCorridor(Graphics g, int posX, int posY) {
		// 设置方块坐标
		int x = 5 + posX * SIZE;
		int y = 5 + posY * SIZE;

		// 绘制方块底色
		g.setColor(Color.GRAY);
		g.fillRect(x+2, y+2, SIZE-5, SIZE-5);
	}
	
	/**
	 * 绘制门
	 * 
	 * @param g
	 *            画笔
	 * @param posX
	 *            横坐标
	 * @param posY
	 *            纵坐标
	 */
	private void drawUpStairs(Graphics g, int posX, int posY) {
		// 设置方块坐标
		int x = 5 + posX * SIZE;
		int y = 5 + posY * SIZE;

		// 绘制方块底色
		g.setColor(Color.green);
		g.fillRect(x + 2, y + 2 , SIZE - 5, SIZE - 5);
	}
	
	/**
	 * 绘制门
	 * 
	 * @param g
	 *            画笔
	 * @param posX
	 *            横坐标
	 * @param posY
	 *            纵坐标
	 */
	private void drawDownStairs(Graphics g, int posX, int posY) {
		// 设置方块坐标
		int x = 5 + posX * SIZE;
		int y = 5 + posY * SIZE;

		// 绘制方块底色
		g.setColor(Color.RED);
		g.fillRect(x + 2, y + 2 , SIZE - 5, SIZE - 5);
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
