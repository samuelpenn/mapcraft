
package uk.co.demon.bifrost.rpg.xmlmap;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.event.*;
import java.awt.*;
import java.awt.image.*;
import java.net.*;
import java.io.*;
import java.util.*;



public class Pane extends JPanel implements ListSelectionListener {
    private ListSelectionListener   editor;
    private Toolkit     toolkit;
    private Terrain[]   terrains;
    private JFrame      frame;
    private String      title;
    private JList       list;
    private JScrollPane scrollPane;
    
    
    /**
     * Class which implements cell rendering for an IconSet.
     * Used in the JList component.
     */
    public class
    ListRenderer extends JLabel implements ListCellRenderer {

        // This is the only method defined by ListCellRenderer.
        // We just reconfigure the JLabel each time we're called.

        public Component
        getListCellRendererComponent(JList list, Object value,
                                    int index, boolean isSelected,
                                    boolean cellHasFocus) {
                                    

            Terrain t = (Terrain)value;
            ImageIcon icon = new ImageIcon("images/medium/"+t.getImagePath());
            setIcon(icon);
            setText(t.getDescription());
            
            setEnabled(list.isEnabled());
            setOpaque(true);
            if (isSelected) {
                setBackground(list.getSelectionBackground());
                setForeground(list.getSelectionForeground());
            } else {
                setBackground(list.getBackground());
                setForeground(list.getForeground());
            }

            return this;
        }
    }

    public String
    toString() {
        return title;
    }

    public void
    valueChanged(ListSelectionEvent e) {
        System.out.println("Pane index: "+e.getFirstIndex()+", "+e.getLastIndex());
    }

    public
    Pane(ListSelectionListener editor, String title) {
        super(true);

        this.editor = editor;
        this.title = title;

        toolkit = Toolkit.getDefaultToolkit();

    }

    public boolean
    isSelected(int index) {
        return list.isSelectedIndex(index);
    }

    public void
    makeFrame() {
        frame = new JFrame(title);
        frame.getContentPane().add(this);
        frame.setSize(new Dimension(120, 300));
        frame.setVisible(true);
    }

    public void
    setPalette(Terrain[] set, boolean label) {
        this.terrains = set;

        list = new JList(set);
        list.setCellRenderer(new ListRenderer());
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        if (editor != null) {
            list.addListSelectionListener(editor);
        } else {
            list.addListSelectionListener(this);
        }
        scrollPane = new JScrollPane(list);

        GridBagLayout       gridbag = new GridBagLayout();
        GridBagConstraints  c = new GridBagConstraints();

        setLayout(gridbag);
        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1.0;
        c.weighty = 1.0;
        gridbag.setConstraints(scrollPane, c);
        add(scrollPane);
        scrollPane.setSize(new Dimension(120, 300));
    }


    public static void
    main(String args[]) {
        Pane        pane = new Pane((ListSelectionListener)null, "Test");
        TerrainSet  items;

        try {
            MapXML      xml = new MapXML("terrain.xml");
            Terrain[]   terrains;

            items = xml.getTerrainSet();
            terrains = items.toArray();

            pane.setPalette(terrains, true);
            pane.makeFrame();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
