
package uk.co.demon.bifrost.rpg.xmlmap;

import javax.swing.*;
import java.awt.event.*;
import java.awt.*;
import java.awt.image.*;
import java.net.*;
import java.io.*;


public class MapEditor extends MapViewer implements ActionListener {
    private JFrame      frame;
    private JMenuBar    menuBar;

    private short       terrainSelected = 3;
    
    private void
    log(int level, String message) {
        System.out.println(message);
    }

    private void debug(String message) { log(4, message); }
    private void trace(String message) { log(3, message); }
    private void warn(String message)  { log(2, message); }
    private void error(String message) { log(1, message); }
    private void fatal(String message) { log(0, message); }


    /**
     * Class to handle mouse click events.
     */
    public class
    MouseHandler extends MouseAdapter {
        public void
        mousePressed(MouseEvent e) {
            int     x,y;

            x = e.getX()/tileXSize;
            y = e.getY()/tileYSize;

            try {
                map.setTerrain(x, y, terrainSelected);
            } catch (MapOutOfBoundsException moobe) {
                log(0, "Out of bounds!");
            }

            repaint();

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

            x = e.getX()/tileXSize;
            y = e.getY()/tileYSize;

            log(0, "Dragged ("+x+","+y+") = "+terrainSelected);
            try {
                map.setTerrain(x, y, terrainSelected);
            } catch (MapOutOfBoundsException moobe) {
                log(0, "Out of bounds!");
            }
            paintTile(x, y);
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

            trace("ActionEvent: Menu edit."+command);

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

            trace("ActionEvent: Menu file."+command);

            if (command.equals("load")) {
                // Load a map file.
                try {
                    map.load("map.cart");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else if (command.equals("save")) {
                // Save current map file.
                try {
                    map.save("map.cart");
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
     * Class to listen for events from the Palette menu.
     */
    protected class
    MenuPaletteListener implements ActionListener {
        public void
        actionPerformed(ActionEvent ev) {
            String  command = ev.getActionCommand();

            trace("ActionEvent: Menu palette."+command);
            try {
                terrainSelected = (short) Integer.parseInt(command);
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

        frame = new JFrame("Map Editor");
        frame.getContentPane().add(this);
        frame.setSize(new Dimension(1400,900));
        frame.setVisible(true);

        addMouseMotionListener(new MouseMotionHandler());

        menuBar = new JMenuBar();
        frame.setJMenuBar(menuBar);
        menuBar.add(createFileMenu());
        menuBar.add(createEditMenu());
        menuBar.add(createViewMenu());
        menuBar.add(createPaletteMenu());

        frame.addKeyListener(new KeyEventHandler());

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
        if (option.equals("Ocean")) {
            terrainSelected = 1;
        } else if (option.equals("Sea")) {
            terrainSelected = 2;
        } else if (option.equals("Plains")) {
            terrainSelected = 3;
        } else if (option.equals("Woods")) {
            terrainSelected = 4;
        }
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

    /**
     * Create the 'View' menu for the frame.
     *
     * @return	A fully defined menu.
     */
    JMenu
    createViewMenu() {
        JMenu		menu = new JMenu("View");
        JMenuItem	item;

        menu.setMnemonic(KeyEvent.VK_V);

        item = new JMenuItem("Show grid", KeyEvent.VK_G);
        menu.add(item);

        return menu;
    }

    JMenu
    createPaletteMenu() {
        JMenu       menu = new JMenu("Palette");
        JMenuItem   item;
        MenuPaletteListener listener = new MenuPaletteListener();

        menu.setMnemonic(KeyEvent.VK_P);

        menu.add(item = new JMenuItem("Ocean"));
        item.setActionCommand("1");
        item.addActionListener(listener);
        menu.add(item = new JMenuItem("Sea"));
        item.setActionCommand("2");
        item.addActionListener(listener);
        menu.add(item = new JMenuItem("Plains"));
        item.setActionCommand("3");
        item.addActionListener(listener);
        menu.add(item = new JMenuItem("Woods"));
        item.setActionCommand("4");
        item.addActionListener(listener);

        return menu;
    }


    public static void
    main(String args[]) {
        MapEditor   editor = new MapEditor("kanday.map");

    }

}
