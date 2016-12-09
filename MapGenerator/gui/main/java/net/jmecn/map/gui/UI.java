package net.jmecn.map.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.security.MessageDigest;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.jmecn.map.creator.Building;
import net.jmecn.map.creator.CaveCellauto;
import net.jmecn.map.creator.CaveSanto;
import net.jmecn.map.creator.DungeonCell;
import net.jmecn.map.creator.DungeonNickgravelyn;
import net.jmecn.map.creator.DungeonTyrant;
import net.jmecn.map.creator.MapCreator;
import net.jmecn.map.creator.Maze;
import net.jmecn.map.creator.MazeWilson;

public class UI extends JFrame {
	/**
	 * 
	 */
	private static final long serialVersionUID = -6014389155626784508L;
	
	ResourceBundle res = ResourceBundle.getBundle("net.jmecn.map.gui.UI");
	
	// MapCreater base
	private MapCreator creator;
	private int width;
	private int height;
	private long seed;
	private boolean isRand;
	
	// Maze only
	private int roadSize;
	
	// Dungeon only
	private int maxFeatures;
	
	private int pixel;
	private Canvas canvas;

	private List<MapCreator> mapCreators;
	
	public UI() {
		try {
			width = Integer.parseInt(res.getString("creator.width"));
			height = Integer.parseInt(res.getString("creator.height"));
			seed = md5(res.getString("creator.seed"));
			isRand = Boolean.valueOf(res.getString("creator.isRand"));
			pixel = Integer.parseInt(res.getString("canvas.pixel"));
		} catch (Exception e) {
			width = 40;
			height = 30;
			seed = md5("yan");
			isRand = false;
			pixel = 12;
		}
		
		initMapCreators();
		
		// use the first one
		this.creator = mapCreators.get(0);
		
		this.canvas = new Canvas(pixel);

		this.setTitle(res.getString("ui.title"));
		this.setSize(1024, 768);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setJMenuBar(getJMenuBar());
		this.setContentPane(getContentPanel());
		
		this.updateMap();

		this.setVisible(true);

	}

	private void initMapCreators() {
		CaveCellauto cellauto = new CaveCellauto(width, height);
		
		CaveSanto caveSanto = new CaveSanto(width, height);
		
		maxFeatures = 100;
		DungeonTyrant tyrant = new DungeonTyrant(width, height);
		tyrant.setMaxFeatures(maxFeatures);
		
		DungeonNickgravelyn nickgravelyn = new DungeonNickgravelyn(width, height);
		DungeonCell cell = new DungeonCell(width, height);
		
		roadSize = 1;
		Maze maze = new Maze(width, height);
		maze.setRoadSize(roadSize);
		
		MazeWilson wmaze = new MazeWilson(width, height);
		
		Building building = new Building(width, height);
		
		
		mapCreators = new ArrayList<MapCreator>();
		mapCreators.add(cellauto);
		mapCreators.add(caveSanto);
		mapCreators.add(tyrant);
		mapCreators.add(nickgravelyn);
		mapCreators.add(cell);
		mapCreators.add(maze);
		mapCreators.add(wmaze);
		mapCreators.add(building);
		
	}
	private JPanel getContentPanel() {
		JPanel panel = new JPanel(new BorderLayout());

		// the renderer
		JScrollPane pane = new JScrollPane();
		pane.setViewportView(canvas);
		panel.add(pane, BorderLayout.CENTER);

		// the toolbar
		panel.add(getJToolBar(), BorderLayout.EAST);

		return panel;
	}

	/**
	 * update map
	 */
	private void updateMap() {
		creator.resize(width, height);
		creator.setSeed(seed);
		creator.setUseSeed(!isRand);
		creator.initialze();
		creator.create();

		updateCanvas();
	}

	void updateCanvas() {
		canvas.setPixel(pixel);
		canvas.setMap(creator.getMap());
		
		canvas.updateUI();
	}
	
	public JMenuBar getJMenuBar() {
		JMenuBar bar = new JMenuBar();

		JMenu fMenu = new JMenu(res.getString("menu.file"));
		bar.add(fMenu);

		JMenuItem export = new JMenuItem(res.getString("menu.exportPng"));
		export.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					String name = creator.getClass().getSimpleName();
					long time = System.currentTimeMillis();
					ImageIO.write(canvas.getImage(), "png", new File(name + "_" + time + ".png"));
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		});
		fMenu.add(export);
		
		export = new JMenuItem(res.getString("menu.exportTxt"));
		export.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					String name = creator.getClass().getSimpleName();
					long time = System.currentTimeMillis();
					
					PrintStream out = new PrintStream(new FileOutputStream(name + "_" + time + ".txt"));
					creator.getMap().printMapChars(out);
					creator.getMap().printMapArray(out);
					out.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		});
		fMenu.add(export);

		return bar;
	}

	public JToolBar getJToolBar() {
		JToolBar toolBar = new JToolBar();
		toolBar.setFloatable(false);
		toolBar.setOrientation(JToolBar.VERTICAL);
		toolBar.setAlignmentY(5);
		
		final JComboBox combo = new JComboBox();
		for(int i=0; i<mapCreators.size(); i++) {
			combo.addItem(mapCreators.get(i).getName());
		}
		
		combo.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int index = combo.getSelectedIndex();
				creator = mapCreators.get(index);
				updateMap();
			}
		});

		addTool(toolBar, combo);
		
		final JLabel l1 = new JLabel(MessageFormat.format(res.getString("label.height"), height));
		addTool(toolBar, l1);

		final JSlider rowSlider = new JSlider(JSlider.HORIZONTAL, 5, 100, height);
		rowSlider.setMajorTickSpacing(10);
		rowSlider.setPaintLabels(true);
		rowSlider.setPaintTicks(true);
		rowSlider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				height = rowSlider.getValue();
				l1.setText(MessageFormat.format(res.getString("label.height"), height));
				updateMap();
			}
		});
		addTool(toolBar, rowSlider);

		final JLabel l2 = new JLabel(MessageFormat.format(res.getString("label.width"), width));
		addTool(toolBar, l2);

		final JSlider colSlider = new JSlider(JSlider.HORIZONTAL, 5, 100, width);
		colSlider.setMajorTickSpacing(10);
		colSlider.setPaintLabels(true);
		colSlider.setPaintTicks(true);
		colSlider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				width = colSlider.getValue();
				l2.setText(MessageFormat.format(res.getString("label.width"), width));
				updateMap();
			}
		});
		addTool(toolBar, colSlider);
		
		final JLabel l5 = new JLabel(MessageFormat.format(res.getString("label.pixel"), pixel));
		addTool(toolBar, l5);

		final JSlider pixelSlider = new JSlider(JSlider.HORIZONTAL, 8, 32, pixel);
		pixelSlider.setPaintLabels(true);
		pixelSlider.setMajorTickSpacing(8);
		pixelSlider.setPaintTicks(true);
		pixelSlider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				pixel = pixelSlider.getValue();
				l5.setText(MessageFormat.format(res.getString("label.pixel"), pixel));
				updateCanvas();
			}
		});
		addTool(toolBar, pixelSlider);

		final JTextField seedText = new JTextField(10);
		seedText.setEnabled(!isRand);
		
		final JCheckBox isRandCheck = new JCheckBox(res.getString("checkbox.random"));
		isRandCheck.setSelected(isRand);
		isRandCheck.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				isRand = isRandCheck.isSelected();
				seedText.setEnabled(!isRand);
			}
		});
		addTool(toolBar, isRandCheck);

		JLabel l4 = new JLabel(res.getString("label.seed"));
		addTool(toolBar, l4);

		seedText.setText(res.getString("creator.seed"));
		addTool(toolBar, seedText);

		JButton refreshBtn = new JButton(res.getString("btn.create"));
		refreshBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (!isRand) {
					String seeds = seedText.getText();
					seed = md5(seeds);
				}
				
				updateMap();
			}
		});
		addTool(toolBar, refreshBtn);
		
		return toolBar;
	}

	private void addTool(JToolBar toolBar, Component comp) {
		JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		panel.add(comp);
		toolBar.add(panel);
	}
	
	private long md5(String seeds) {
		long value = seed;
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			md.update(seeds.getBytes("UTF-8"));
	
			byte byteData[] = md.digest();
	
			// convert the byte to hex format method 2
			StringBuffer hexString = new StringBuffer();
			hexString.append("0x");
			for (int i = 0; i < 7; i++) {
				String hex = Integer.toHexString(0xff & byteData[i]);
				if (hex.length() == 1)
					hexString.append('0');
				hexString.append(hex);
			}
			value = Long.decode(hexString.toString());
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		return value;
	}

	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
		}
		new UI();
	}
}
