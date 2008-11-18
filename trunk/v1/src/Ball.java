import java.awt.Color;
import java.awt.geom.Ellipse2D;

public class Ball extends Ellipse2D.Double {
    private boolean justHitWall;
    private double velX, velY, mass, radius;
    private Color color;
    
    public Ball(double x, double y, double velX, double velY, Color color, double mass, double radius) {
        super(x,y,radius*2,radius*2);
        this.mass = mass;
        this.color = color;
        this.velX = velX;
        this.velY = velY;
        this.radius = radius;
    }
    
    public void action() {
        this.x += velX;
        this.y += velY;
    }
    public Color getColor() {
        return color;
    }
    public double getMass() {
        return mass;
    }

    public double getVelX() {
        return velX;
    }

    public double getRadius() {
        return radius;
    }
    

    public double getVelY() {
        return velY;
    }
    

    public void setVelX(double velX) {
        this.velX = velX;
    }
    

    public void setVelY(double velY) {
        this.velY = velY;
    }    
}
