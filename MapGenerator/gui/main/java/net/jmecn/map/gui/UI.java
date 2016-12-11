package net.jmecn.map.gui;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.security.MessageDigest;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
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
import net.jmecn.map.creator.Islands;
import net.jmecn.map.creator.MapCreator;
import net.jmecn.map.creator.Maze;
import net.jmecn.map.creator.MazeWilson;

/**
 * The main UI for MapCreator
 * @author yanmaoyuan
 *
 */
public class UI extends JFrame implements ActionListener, ChangeListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = -6014389155626784508L;
	
	ResourceBundle res = ResourceBundle.getBundle("net.jmecn.map.gui.UI");
	
	// MapCreater base
	private List<MapCreator> mapCreators;
	private MapCreator creator;
	private int width;
	private int height;
	private long seed;
	private boolean isRand;
	
	private int pixel;
	private Canvas canvas;
	
	// Components
	private JComboBox creatorList;
	
	private JLabel labelWidth;
	private JSlider sliderWidth;
	
	private JLabel labelHeight;
	private JSlider sliderHeight;
	
	private JLabel labelPixel;
	private JSlider sliderPixel;
	
	private JCheckBox checkIsRand;
	private JTextField seedText;
	
	private JButton btnCreate;
	
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

	private ImageIcon getScaledImage(String filename, int pixel) {
		ImageIcon icon = null;
		InputStream in = UI.class.getResourceAsStream(filename);
		try {
			BufferedImage image = ImageIO.read(in);
			Image img = image.getScaledInstance(pixel, pixel, BufferedImage.SCALE_FAST);
			icon = new ImageIcon(img);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return icon;
	}
	
	private void initMapCreators() {
		Islands islands = new Islands(width, height);
		CaveCellauto cellauto = new CaveCellauto(width, height);
		
		CaveSanto caveSanto = new CaveSanto(width, height);
		
		DungeonTyrant tyrant = new DungeonTyrant(width, height);
		tyrant.setMaxFeatures(100);
		
		DungeonNickgravelyn nickgravelyn = new DungeonNickgravelyn(width, height);
		DungeonCell cell = new DungeonCell(width, height);
		
		Maze maze = new Maze(width, height);
		maze.setRoadSize(1);
		
		MazeWilson wmaze = new MazeWilson(width, height);
		
		Building building = new Building(width, height);
		
		
		mapCreators = new ArrayList<MapCreator>();
		mapCreators.add(islands);
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
		panel.add(getOperationPane(), BorderLayout.EAST);

		// the toolbar
		panel.add(getJToolBar(), BorderLayout.NORTH);

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

	/**
	 * Update canvas, redraw the map.
	 */
	private void updateCanvas() {
		canvas.setPixel(pixel);
		canvas.setMap(creator.getMap());
		
		canvas.updateUI();
	}
	
	/**
	 * Save current BufferedImage to a png file.
	 */
	private void exportPng() {
		try {
			String name = creator.getClass().getSimpleName();
			long time = System.currentTimeMillis();
			ImageIO.write(canvas.getImage(), "png", new File(name + "_" + time + ".png"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Save current Map2D to a txt file.
	 */
	private void exportTxt() {
		try {
			String name = creator.getClass().getSimpleName();
			long time = System.currentTimeMillis();
			
			PrintStream out = new PrintStream(new FileOutputStream(name + "_" + time + ".txt"));
			creator.getMap().printMapChars(out);
			creator.getMap().printMapArray(out);
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public JMenuBar getJMenuBar() {
		JMenuBar bar = new JMenuBar();

		JMenu fMenu = new JMenu(res.getString("menu.file"));
		bar.add(fMenu);

		JMenuItem export = new JMenuItem(res.getString("menu.exportPng"));
		export.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				exportPng();
			}
		});
		
		export.setIcon(getScaledImage("img.png", 16));
		fMenu.add(export);
		
		export = new JMenuItem(res.getString("menu.exportTxt"));
		export.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				exportTxt();
			}
		});
		export.setIcon(getScaledImage("txt.png", 16));
		fMenu.add(export);

		return bar;
	}

	public JToolBar getJToolBar() {
		JToolBar toolBar = new JToolBar();
		toolBar.setFloatable(false);
		toolBar.setOrientation(JToolBar.HORIZONTAL);
		toolBar.setAlignmentY(5);
		toolBar.setLayout(new FlowLayout(FlowLayout.LEFT));

		JButton btnCreate = new JButton(res.getString("btn.create"));
		btnCreate.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (!isRand) {
					String seeds = seedText.getText();
					seed = md5(seeds);
				}
				updateMap();
			}
		});
		btnCreate.setIcon(getScaledImage("create.png", 32));
		
		JButton btnExportPng = new JButton(res.getString("menu.exportPng"));
		btnExportPng.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				exportPng();
			}
		});
		btnExportPng.setIcon(getScaledImage("img.png", 32));
		
		JButton btnExportTxt = new JButton(res.getString("menu.exportTxt"));
		btnExportTxt.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				exportTxt();
			}
		});
		btnExportTxt.setIcon(getScaledImage("txt.png", 32));
		
		toolBar.add(btnCreate);
		toolBar.add(btnExportPng);
		toolBar.add(btnExportTxt);
		
		return toolBar;
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
	
	/**
	 * This is the operation panel where you can change map width/height.
	 * @return
	 */
	private Container getOperationPane() {
		GridBagLayout gridBag =new GridBagLayout();
		JPanel container =  new JPanel(gridBag);
		
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.anchor = GridBagConstraints.WEST;
		container.add(getMapPanel(), gbc);
		
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = GridBagConstraints.RELATIVE;
		gbc.anchor = GridBagConstraints.WEST;
		container.add(getCanvasPanel(), gbc);
		
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = GridBagConstraints.RELATIVE;
		gbc.anchor = GridBagConstraints.WEST;
		
		container.add(getSeedPanel(), gbc);
		
		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = GridBagConstraints.RELATIVE;
		gbc.anchor = GridBagConstraints.WEST;
		
		btnCreate = new JButton(res.getString("btn.create"));
		btnCreate.addActionListener(this);
		btnCreate.setIcon(getScaledImage("create.png", 16));
		
		container.add(btnCreate, gbc);
		
		return container;
	}
	
	/**
	 * In this panel, you choose the map creator to build a random map, use sliders to change map's width and height.
	 * @return
	 */
	private Container getMapPanel() {
		JPanel container = new JPanel (new FlowLayout(FlowLayout.LEFT));
		container.setBorder(BorderFactory.createTitledBorder(res.getString("panel.map")));
		container.setPreferredSize(new Dimension(220, 240));
		container.setMinimumSize(new Dimension(220, 240));
		
		creatorList = new JComboBox();
		for(int i=0; i<mapCreators.size(); i++) {
			creatorList.addItem(mapCreators.get(i).getName());
		}
		creatorList.addActionListener(this);
		container.add(creatorList);
		
		labelWidth = new JLabel(MessageFormat.format(res.getString("label.width"), width));
		labelWidth.setPreferredSize(new Dimension(200, 32));
		container.add(labelWidth);
		
		sliderWidth = new JSlider(JSlider.HORIZONTAL, 5, 100, width);
		sliderWidth.setMajorTickSpacing(10);
		sliderWidth.setPaintLabels(true);
		sliderWidth.setPaintTicks(true);
		sliderWidth.addChangeListener(this);
		container.add(sliderWidth);
		
		labelHeight = new JLabel(MessageFormat.format(res.getString("label.height"), height));
		labelHeight.setPreferredSize(new Dimension(200, 32));
		container.add(labelHeight);

		sliderHeight = new JSlider(JSlider.HORIZONTAL, 5, 100, height);
		sliderHeight.setMajorTickSpacing(10);
		sliderHeight.setPaintLabels(true);
		sliderHeight.setPaintTicks(true);
		sliderHeight.addChangeListener(this);
		container.add(sliderHeight);
		
		return container;
	}
	
	/**
	 * In this panel, you can change the canvas's attributes.
	 * @return
	 */
	private Container getCanvasPanel() {
		
		JPanel container = new JPanel (new FlowLayout(FlowLayout.LEFT));
		container.setBorder(BorderFactory.createTitledBorder(res.getString("panel.canvas")));
		container.setPreferredSize(new Dimension(220, 120));
		container.setMinimumSize(new Dimension(220, 120));
		
		labelPixel = new JLabel(MessageFormat.format(res.getString("label.pixel"), pixel));
		labelPixel.setPreferredSize(new Dimension(200, 32));
		container.add(labelPixel);

		sliderPixel = new JSlider(JSlider.HORIZONTAL, 4, 32, pixel);
		sliderPixel.setPaintLabels(true);
		sliderPixel.setMajorTickSpacing(4);
		sliderPixel.setPaintTicks(true);
		sliderPixel.addChangeListener(this);
		container.add(sliderPixel);
		
		return container;
	}
	
	/**
	 * This panel used to change random settings.
	 * @return
	 */
	private Container getSeedPanel() {
		JPanel container = new JPanel (new FlowLayout(FlowLayout.LEFT));
		container.setBorder(BorderFactory.createTitledBorder(res.getString("panel.random")));
		container.setPreferredSize(new Dimension(220, 120));
		container.setMinimumSize(new Dimension(220, 120));
		
		checkIsRand = new JCheckBox(res.getString("checkbox.random"));
		checkIsRand.setSelected(isRand);
		checkIsRand.addChangeListener(this);
		container.add(checkIsRand);
		
		seedText = new JTextField(16);
		seedText.setText(res.getString("creator.seed"));
		seedText.setEnabled(!isRand);
		JPanel panel = new JPanel (new FlowLayout(FlowLayout.LEFT));
		panel.add(new JLabel(res.getString("label.seed")));
		panel.add(seedText);
		panel.setPreferredSize(new Dimension(200, 32));
		container.add(panel);
		
		return container;
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == creatorList) {
			int index = creatorList.getSelectedIndex();
			creator = mapCreators.get(index);
			updateMap();
		}
		else if (e.getSource() == btnCreate) {
			if (!isRand) {
				String seeds = seedText.getText();
				seed = md5(seeds);
			}
			updateMap();
		}
	}
	
	@Override
	public void stateChanged(ChangeEvent e) {
		if (e.getSource() == checkIsRand) {
			isRand = checkIsRand.isSelected();
			seedText.setEnabled(!isRand);
		}
		else if (e.getSource() == sliderWidth) {
			width = sliderWidth.getValue();
			labelWidth.setText(MessageFormat.format(res.getString("label.width"), width));
			updateMap();
		}
		else if (e.getSource() == sliderHeight) {
			height = sliderHeight.getValue();
			labelHeight.setText(MessageFormat.format(res.getString("label.height"), height));
			updateMap();
		}
		else if (e.getSource() == sliderPixel) {
			pixel = sliderPixel.getValue();
			labelPixel.setText(MessageFormat.format(res.getString("label.pixel"), pixel));
			updateCanvas();
		}
	}
	
	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
		}
		new UI();
	}
}
