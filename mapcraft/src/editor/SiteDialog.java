
package uk.co.demon.bifrost.rpg.xmlmap;

import javax.swing.*;
import java.awt.*;
import java.awt.image.*;

public class SiteDialog extends JDialog {
    private Site        site;
    private JTextField  name;
    private JTextField  description;
    
    public String
    getName() {
        if (name != null) {
            return name.getText();
        }
        
        return "";
    }
    
    public
    SiteDialog(Site site) {
        super();
        this.site = site;
        
        GridBagLayout       gridbag = new GridBagLayout();
        GridBagConstraints  c = new GridBagConstraints();
        JLabel              label = null;
        getContentPane().setLayout(gridbag);

        c.fill = GridBagConstraints.BOTH;
        c.weightx = c.weighty = 1.0;

        name = new JTextField("Unnamed", 20);
        description = new JTextField("Unknown", 30);

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


        setSize(new Dimension(300,150));
        setVisible(true);
    }
    
    public static void
    main(String args[]) {
        Site        site = new Site((short)1, "London", "Large city");
        SiteDialog  dialog = new SiteDialog(site);
    }
}
