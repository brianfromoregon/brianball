import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JPanel;

public class BrianBallView extends JPanel implements Observer {
    private BrianBallModel model;
    
    public BrianBallView(BrianBallModel model) {
        this.model = model;
        model.addObserver(this);
    }
    
    public void addComponent(Component c) {
        
    }
    
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        /* draw all balls */
        ArrayList<Ball> balls = model.getBalls();
        for(int i=0; i<balls.size(); i++) {
            Ball b = balls.get(i);
            g.setColor(b.getColor());
            g2.fill(b);
        }
        /* draw all walls */
        ArrayList<Wall> walls = model.getWalls();
        for(int i=0; i<walls.size(); i++) {
            Wall w = walls.get(i);
            g.setColor(w.getColor());
            g2.fill(w);
        }
        /* draw black space */
        ArrayList<Rectangle2D.Double> blk = model.getBlk();
        for(int i=0; i<blk.size(); i++) {
            Rectangle2D.Double r = blk.get(i);
            g.setColor(Color.gray);
            g2.fill(r);
        }
        Wall newWall = model.getNewWall();
        if (newWall != null) {
            g.setColor(newWall.getColor());
            g2.fill(newWall);
        }
    }
    
    public void update(Observable m, Object arg) {
        repaint();
    }
}
