
package uk.co.demon.bifrost.rpg.mapcraft.editor;

import uk.co.demon.bifrost.rpg.mapcraft.map.*;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.event.*;
import java.awt.*;
import java.awt.image.*;
import java.net.*;
import java.io.*;
import java.util.*;

import uk.co.demon.bifrost.utils.Options;


public class MapEditor extends MapViewer
                       implements ActionListener {


    private JFrame      frame;
    private JMenuBar    menuBar;

    private Pane        terrainPane = null;
    private Pane        riverPane = null;
    private Pane        placePane = null;

    private Brush       brush = new Brush();


    /**
     * Event handler for the terrain palette. Provides logic for
     * when an item in the terrain palette (terrainPane) is selected.
     * Changes the 'terrainSelected' field, and changes the painting
     * mode to be MODE_BRUSH.
     */
    public class
    TerrainPalette implements ListSelectionListener {
        private MapEditor editor;

        public
        TerrainPalette(MapEditor editor) {
            this.editor = editor;
        }

        public void
        valueChanged(ListSelectionEvent e) {
            int first, last, index;

            // We get a result for both the item which was
            // selected, and the one unselected, so we need
            // to figure out which is which.
            index = first = e.getFirstIndex();
            last = e.getLastIndex();
            if (terrainPane.isSelected(last)) {
                index = last;
            }

            System.out.println("TerrainPalette: "+index);
            Terrain ta[] = map.getTerrainSet().toArray();
            brush.setSelected(Brush.TERRAIN, ta[index].getId());
            System.out.println("Terrain selected: "+brush.getSelected());

            brush.setType(Brush.TERRAIN);
        }
    }

    public class
    PlacePalette implements ListSelectionListener {
        private MapEditor editor;

        public
        PlacePalette(MapEditor editor) {
            this.editor = editor;
        }

        public void
        valueChanged(ListSelectionEvent e) {
            int first, last, index;

            // We get a result for both the item which was
            // selected, and the one unselected, so we need
            // to figure out which is which.
            index = first = e.getFirstIndex();
            last = e.getLastIndex();
            if (placePane.isSelected(last)) {
                index = last;
            }

            System.out.println("TerrainPalette: "+index);
            Terrain ta[] = map.getPlaceSet().toArray();
            brush.setSelected(Brush.SITES, ta[index].getId());
            System.out.println("Terrain selected: "+brush.getSelected());

            brush.setType(Brush.SITES);
        }
    }


    public void
    applyBrush(int x, int y) {
        try {
            switch (brush.getType()) {
            case Brush.TERRAIN:
                info("Applying terrain brush "+brush.getSelected());
                map.setTerrain(x, y, brush.getSelected());
                break;
            case Brush.SITES:
                if (brush.getSelected() == 0) {
                    map.getTile(x, y).setSite((Site)null);
                } else if (!map.isSite(x, y)) {
                    // Only set site if no site currenty present.
                    Site    site = new Site(brush.getSelected(), "Unnamed", "Unknown");
                    info("Applying site brush "+brush.getSelected());
                    map.getTile(x, y).setSite(site);
                    info("Opening dialog");
                    SiteDialog dialog = new SiteDialog(site, frame);
                    info("Finished dialog");
                    site.setName(dialog.getName());
                    site.setDescription(dialog.getDescription());
                } else {
                    // Do nothing.
                }
                break;
            }
        } catch (MapOutOfBoundsException moobe) {
            warn("Out of bounds!");
        }

        paintTile(x, y);
    }


    /**
     * Class to handle mouse click events.
     */
    public class
    MouseHandler extends MouseAdapter {
        public void
        mousePressed(MouseEvent e) {
            int     x,y;
            int     yp;

            x = e.getX()/tileXSize;
            yp = e.getY();

            if (x%2 != 0) {
                yp -= tileYOffset;
            }
            y = yp/tileYSize;

            applyBrush(x, y);
        }
    }

    /**
     * Class to handle mouse motion events.
     */
    public class
    MouseMotionHandler extends MouseMotionAdapter {
        public void
        mouseDragged(MouseEvent e) {
            int     x,y;
            int     yp;

            if (brush.getType() == Brush.SITES) {
                // Don't allow drag paint for painting of sites.
                return;
            }

            x = e.getX()/tileXSize;
            yp = e.getY();

            if (x%2 != 0) {
                yp -= tileYOffset;
            }
            y = yp/tileYSize;

            applyBrush(x, y);
        }
    }

    public class
    KeyEventHandler extends KeyAdapter {
        public void
        KeyPressed(KeyEvent e) {
            System.out.println("Pressed!");
        }
    }

    /**
     * Class to listen for events from the Edit menu.
     */
    protected class
    MenuEditListener implements ActionListener {
        public void
        actionPerformed(ActionEvent e) {
            String  command = e.getActionCommand();

            info("ActionEvent: Menu edit."+command);

            if (command.equals("double")) {
                map.rescaleMap(map.getScale()/2);
            }
        }
    }

    /**
     * Class to listen for events from the File menu.
     */
    protected class
    MenuFileListener implements ActionListener {
        public void
        actionPerformed(ActionEvent ev) {
            String command = ev.getActionCommand();

            info("ActionEvent: Menu file."+command);

            if (command.equals("load")) {
                // Load a map file.
                try {
                    map.load("kanday.map");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else if (command.equals("save")) {
                // Save current map file.
                try {
                    map.save(map.getFilename());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                // Unimplemented menu option.
                warn("ActionEvent: Unsupported menu file."+command);
            }
        }
    }

    /**
     * Class to listen for events from the File menu.
     */
    protected class
    MenuViewListener implements ActionListener {
        public void
        actionPerformed(ActionEvent ev) {
            String command = ev.getActionCommand();

            info("ActionEvent: MenuViewListener."+command);

            if (command.equals("showGrid")) {
                info("ShowGrid");
                
            } else {
                // Unimplemented menu option.
                warn("ActionEvent: Unsupported MenuViewListener."+command);
            }
        }
    }

    /**
     * Class to listen for events from the Palette menu.
     */
    protected class
    MenuPaletteListener implements ActionListener {
        public void
        actionPerformed(ActionEvent ev) {
            String  command = ev.getActionCommand();

            info("ActionEvent: Menu palette."+command);
            try {
                brush.setSelected(Brush.TERRAIN, (short) Integer.parseInt(command));
            } catch (Exception e) {
                warn("ActionEvent: Palette option not an integer");
            }
        }
    }


    /**
     * Main constructor.
     */
    public
    MapEditor(String filename) {
        super(filename);
        
        this.setSize(new Dimension(2000, 1200));
        JScrollPane scrollPane = new JScrollPane(this,
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);

        frame = new JFrame("Map Editor");
        frame.getContentPane().add(scrollPane);
        frame.setSize(new Dimension(640,400));
        frame.setVisible(true);

        addMouseMotionListener(new MouseMotionHandler());
        addMouseListener(new MouseHandler());

        menuBar = new JMenuBar();
        frame.setJMenuBar(menuBar);
        menuBar.add(createFileMenu());
        menuBar.add(createEditMenu());
        menuBar.add(createViewMenu());
        menuBar.add(createPaletteMenu());
        menuBar.setVisible(true);
        frame.setSize(new Dimension(600, 400));

        frame.addKeyListener(new KeyEventHandler());
        
        terrainPane = new Pane(new TerrainPalette(this), "Terrain");
        terrainPane.setPalette(map.getTerrainSet().toArray(), false);
        terrainPane.makeFrame();
        
        placePane = new Pane(new PlacePalette(this), "Places");
        placePane.setPalette(map.getPlaceSet().toArray(), false);
        placePane.makeFrame();

    }

    public
    MapEditor(int width, int height, int scale) {
    }

    public void
    actionPerformed(ActionEvent e) {
        JMenuItem   source = (JMenuItem)(e.getSource());
        String      option = source.getText();
        System.out.println(source.getText());

        processEvent(option);
    }

    public void
    processEvent(String option) {
    }

    /**
     * Create the 'File' menu for the frame.
     * All these options actually access the database, not files,
     * but it keeps things consistant with other applications.
     *
     * @return	A fully defined menu.
     */
    JMenu
    createFileMenu() {
        JMenu       menu = new JMenu("File");
        JMenuItem   item;
        MenuFileListener    fileListener = new MenuFileListener();

        menu.setMnemonic(KeyEvent.VK_F);

        item = new JMenuItem("New...", KeyEvent.VK_N);
        item.setActionCommand("new");
        item.addActionListener(fileListener);
        menu.add(item);

        item = new JMenuItem("Load...", KeyEvent.VK_R);
        item.setActionCommand("load");
        item.addActionListener(fileListener);
        menu.add(item);

        item = new JMenuItem("Save", KeyEvent.VK_S);
        item.setActionCommand("save");
        item.addActionListener(fileListener);
        menu.add(item);

        item = new JMenuItem("Save as...", KeyEvent.VK_A);
        menu.add(item);

        menu.addSeparator();

        item = new JMenuItem("Properties...", KeyEvent.VK_P);
        menu.add(item);

        menu.addSeparator();

        item = new JMenuItem("Exit", KeyEvent.VK_X);
        menu.add(item);

        return menu;
    }

    /**
     * Create the 'Edit' menu for the frame.
     *
     * @return	A fully defined menu.
     */
    JMenu
    createEditMenu() {
        JMenu               menu = new JMenu("Edit");
        JMenuItem	        item;
        MenuEditListener    editListener = new MenuEditListener();

        menu.setMnemonic(KeyEvent.VK_E);

        item = new JMenuItem("Clear", KeyEvent.VK_C);
        menu.add(item);

        menu.add(item = new JMenuItem("Double detail", KeyEvent.VK_D));
        item.setActionCommand("double");
        item.addActionListener(editListener);

        return menu;
    }
    
    public JMenuItem
    createItem(JMenuItem item, String command, ActionListener l) {
        item.setActionCommand(command);
        item.addActionListener(l);

        return item;
    }

    /**
     * Create the 'View' menu for the frame.
     *
     * @return	A fully defined menu.
     */
    JMenu
    createViewMenu() {
        JMenu               menu = new JMenu("View");
        JMenuItem	        item;
        MenuViewListener    listener = new MenuViewListener();

        menu.setMnemonic(KeyEvent.VK_V);

        menu.add(createItem(new JCheckBoxMenuItem("Show grid", true),
                            "showGrid", listener), KeyEvent.VK_G);
        menu.add(createItem(new JCheckBoxMenuItem("Show sites", true),
                            "showSites", listener), KeyEvent.VK_S);
        menu.add(createItem(new JCheckBoxMenuItem("Show hills", true),
                            "showHills", listener), KeyEvent.VK_H);
        menu.add(createItem(new JCheckBoxMenuItem("Show coasts", true),
                            "showCoasts", listener), KeyEvent.VK_C);

        return menu;
    }

    JMenu
    createPaletteMenu() {
        JMenu       menu = new JMenu("Palette");
        JMenuItem   item;
        MenuPaletteListener listener = new MenuPaletteListener();

        menu.setMnemonic(KeyEvent.VK_P);
        
        TerrainSet  set = map.getTerrainSet();
        Iterator    it = set.iterator();
        
        while (it.hasNext()) {
            Terrain t = (Terrain)it.next();
            System.out.println("Adding terrain "+t.getId()+" ("+t.getDescription()+")");
            menu.add(item = new JMenuItem(t.getDescription()));
            item.setActionCommand(""+t.getId());
            item.addActionListener(listener);

        }

        return menu;
    }


    public static void
    main(String args[]) {
        Options options = new Options(args);
        MapEditor   editor = new MapEditor(options.getString("-load"));

    }

}
