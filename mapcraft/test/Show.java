
import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;
import java.awt.image.*;
import java.net.*;
import java.io.*;

import java.util.*;
import java.awt.geom.Line2D;

import com.sun.image.codec.jpeg.*;


public class Show implements ImageObserver {
    public static Toolkit toolkit = Toolkit.getDefaultToolkit();
    public boolean gotImage = false;
    public
    Show() {
    }

    public boolean
    imageUpdate(Image img, int infoflags, int x, int y, int w, int h) {
        System.out.println("Available: "+x+","+y+","+w+","+h);

        if (h == 96) gotImage = true;

        return true;
    }

    public Image
    modify(JFrame frame, Image image) {
        Image       draw = frame.createImage(96, 96);
        Graphics2D  g = (Graphics2D)draw.getGraphics();

        toolkit.prepareImage(image, -1, -1, this);

        do {
            try {
                Thread.sleep(250);
            } catch (Exception e) {}
        } while (!gotImage);

//        System.out.println(g.drawImage(image, 0, 0, this));

        g.drawImage(image, 0, 0, this);

        g.setBackground(new Color(255, 255, 255, 255));
        g.clearRect(0, 0, 75, 25);

        //g.setPaintMode();
        g.setColor(new Color(0,0,0,255));
        g.fillRect(0, 0, 50, 50);
        /*

        g.setColor(new Color(0, 255, 0, 255));
        g.fillRect(0, 0, 75, 25);
        g.setColor(new Color(0, 0, 0, 50));
        g.fillRect(0, 0, 25, 50);
*/
        return draw;
    }

    public static void
    main(String args[]) {
        JFrame  frame = new JFrame("Hello");
        Image   icon = toolkit.getImage("sea.png");
        Image   hex;
        Show    show = new Show();

        frame.setSize(200,200);
        frame.setVisible(true);
        hex = show.modify(frame, icon);

        //frame.getContentPane().add(new JButton(new ImageIcon(icon)));
        frame.getContentPane().add(new JButton(new ImageIcon(hex)));
        frame.setVisible(true);
    }
}
