
package uk.co.demon.bifrost.rpg.xmlmap;

import javax.swing.*;
import java.awt.*;
import java.awt.image.*;

public class SiteDialog extends JDialog {
    private Site        site;
    private JTextField  name;
    private JTextField  description;
    private JButton     okay;
    private JButton     cancel;
    
    public String
    getName() {
        if (name != null) {
            return name.getText();
        }
        
        return "";
    }
    
    public String
    getDescription() {
        if (description != null) {
            return description.getText();
        }
        
        return "";
    }
    
    public
    SiteDialog(Site site, JFrame frame) {
        super(frame, "Edit site", true);
        this.site = site;
        
        if (frame != null) {
            // This is very crude positioning.
            // TODO: Centre in parent frame.
            Point   p = frame.getLocation();
            p.translate(80, 40);
            setLocation(p);
        }

        GridBagLayout       gridbag = new GridBagLayout();
        GridBagConstraints  c = new GridBagConstraints();
        JLabel              label = null;
        getContentPane().setLayout(gridbag);

        c.fill = GridBagConstraints.BOTH;
        c.weightx = 1.0;
        c.weighty = 0.0;

        name = new JTextField(site.getName(), 20);
        description = new JTextField(site.getDescription(), 30);

        label = new JLabel("Name");
        c.gridx = 0;
        c.gridy = 0;
        gridbag.setConstraints(label, c);
        getContentPane().add(label);

        c.gridx = 1;
        gridbag.setConstraints(name, c);
        getContentPane().add(name);

        c.gridx=0;
        c.gridy=1;
        label = new JLabel("Description");
        gridbag.setConstraints(label, c);
        getContentPane().add(label);

        c.gridx = 1;
        gridbag.setConstraints(description, c);
        getContentPane().add(description);

        /*
        c.gridy = 2;
        c.gridx = 1;
        c.weightx = 0.0;
        okay = new JButton("Okay");
        gridbag.setConstraints(okay, c);
        getContentPane().add(okay);
        */
        setSize(new Dimension(300,150));
        setVisible(true);
    }

    public static void
    main(String args[]) {
        Site        site = new Site((short)1, "London", "Large city");
        //SiteDialog  dialog = new SiteDialog(site);
    }
}
