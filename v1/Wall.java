import java.awt.Color;
import java.awt.geom.Rectangle2D;

public class Wall extends Rectangle2D.Double {
    private boolean vertical;
    private double vel, size;
    private Color color;

    
    public Wall(double x, double y, double size, boolean vertical) {
        super(x,y,size,1);
        this.vertical = vertical;
        if (!vertical) this.setRect(x,y,1,size);
        this.vel=10;
        this.color = Color.red;
    }
    
    /* just used for outer walls */
    public Wall(double x, double y, double length, double size, Color color, boolean vertical) {
        super(x,y,size,length);
        this.vertical = vertical;
        if (!vertical) this.setRect(x,y,length,size);
        this.vel=0;
        this.color = color;
    }
    
    public void action(boolean downDone, boolean upDone, boolean leftDone, boolean rightDone) {
        if (vertical) {
            if (!upDone && !downDone) {
                y -= vel/2;
                height += vel;
            } else if (!upDone && downDone) {
                y -= (vel/2);
                height += (vel/2);
            } else if (upDone && !downDone) {
                height += vel/2;
            }
        } else {
            if (!leftDone && !rightDone) {
                x -= vel/2;
                width += vel;
            } else if (!leftDone && rightDone) {
                x -= vel/2;
                width += vel/2;
            } else if (leftDone && !rightDone) {
                width += vel/2;
            }
        }
    }

    public boolean isVertical() {
        return vertical;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }
}
