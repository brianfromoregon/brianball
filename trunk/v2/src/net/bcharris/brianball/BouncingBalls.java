package net.bcharris.brianball;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.jbox2d.collision.CircleDef;
import org.jbox2d.collision.MassData;
import org.jbox2d.collision.PolygonDef;
import org.jbox2d.collision.Shape;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.World;
import org.jbox2d.dynamics.contacts.ContactPoint;
import org.jbox2d.testbed.AbstractExample;
import org.jbox2d.testbed.TestbedMain;

/**
 * TODO
 * don't let walls be created if they collide with solid wall instantly
 * verify that velocity is the same for diagonal walls and vert/horiz walls
 * @author brian
 */
public class BouncingBalls extends AbstractExample {

    private static final float PI = (float) Math.PI;
    private static final float PI_OVER_TWO = PI / 2f;
    private static final float TWO_PI = PI * 2f;
    private static final String SOLID_WALL_ID = "solid_wall";
    private static final String GROWING_WALL_ID = "growing_wall";
    private static final String BALL_ID = "ball";

    class GrowingWall {

        public static final float growthStep = .08f;
        public Vec2 start;
        public float length;
        public boolean destroyed, solidified;
        public float width;
        private float angle; // In radians
        private Vec2 angleParts; // [cos,sin]
        private Body body;
        public Shape shape;
        public GrowingWall partner;

        public GrowingWall(Vec2 start, float angle) {
            this.angle = angle;
            this.angleParts = new Vec2((float) Math.cos(angle), (float) Math.sin(angle));
            // Slightly push the start position in the direction we're heading so we
            // don't get on the wrong side of our partner
            this.start = start.add(angleParts.mul(.001f));
            this.width = 1f;
            this.destroyed = false;
        }

        public void grow() {
            length += growthStep;
        }

        public void selfDestruct(World w) {
            body.destroyShape(shape);
            w.destroyBody(body);
            shape = null;
            body = null;
        }

        public void updateInWorld(World w) {
            if (body == null) {
                BodyDef bodyDef = new BodyDef();
                bodyDef.angle = angle;
                bodyDef.position = start;
                MassData massData = new MassData();
                massData.mass = 10000; // Going too large causes weird boundary violations
                bodyDef.massData = massData;
                this.body = m_world.createBody(bodyDef);
                body.setLinearVelocity(angleParts.mul(growthStep * settings.hz / 2));
            }

            if (shape != null) {
                body.destroyShape(shape);
            }
            PolygonDef polyDef = new PolygonDef();
            polyDef.friction = 0;
            polyDef.userData = GROWING_WALL_ID;
            polyDef.setAsBox(length / 2, width / 2);
            shape = body.createShape(polyDef);
        }
    }
    private List<GrowingWall> growingWalls = new ArrayList<GrowingWall>();

    public BouncingBalls(TestbedMain _parent) {
        super(_parent);
    }

    @Override
    public void create() {
        m_world.setGravity(new Vec2(0, 0));
        newSolidWall(new Vec2(0.0f, 0.0f), PI_OVER_TWO, 2, 60);
        newSolidWall(new Vec2(0.0f, 30.0f), PI_OVER_TWO, 2, 60);
        newSolidWall(new Vec2(-30.0f, 15.0f), 0, 2, 30);
        newSolidWall(new Vec2(30.0f, 15.0f), 0, 2, 30);

        CircleDef sd = new CircleDef();
        sd.friction = 0f;
        sd.radius = 1f;
        sd.density = 5.0f;
        sd.userData = BALL_ID;

        BodyDef bd = new BodyDef();

        for (int i = 0; i < 8; ++i) {
            sd.restitution = 1.0f;
            bd.position = new Vec2(-10.0f + 3.0f * i, 10.0f);
            Body myBody = m_world.createBody(bd);
            myBody.createShape(sd);
            myBody.setMassFromShapes();
        }

    }

    @Override
    public void eventlessClick(Vec2 p) {
        if (parent.mouseButton != parent.LEFT)
            return;
        
        Vec2 down = super.lastMouseDown;
        Vec2 up = super.mouseWorld;

        if (up.x == down.x && up.y == down.y)
            return;
        
        float angle = normalizeRadians(up.atan2(down));
        float oppositeAngle = normalizeRadians(angle + PI);

        GrowingWall w1 = new GrowingWall(down, angle);
        GrowingWall w2 = new GrowingWall(down, oppositeAngle);
        w1.partner = w2;
        w2.partner = w1;
        growingWalls.add(w1);
        growingWalls.add(w2);
    }

    @Override
    public void preStep() {
        super.preStep();

        // Need to grow any walls?
        for (GrowingWall w : growingWalls) {
            w.grow();
            w.updateInWorld(m_world);
        }
    }

    @Override
    public void postStep() {
        super.postStep();

        for (Iterator<GrowingWall> it = growingWalls.iterator(); it.hasNext();) {
            GrowingWall w = it.next();
            if (w.destroyed) {
                // Need to destroy?
                w.selfDestruct(m_world);
                it.remove();
            } else if (w.solidified) {
                // Need to solidify?
                makeSolidWall(w);
                it.remove();
            }
        }
    }

    @Override
    public void contact(ContactPoint point) {
        super.contact(point);
        for (GrowingWall w : growingWalls) {
            if (partnerCollision(point, w))
                continue;
            if (point.shape1 == w.shape || point.shape2 == w.shape) {
                Object id = point.shape1 == w.shape ? point.shape2.m_userData : point.shape1.m_userData;
                if (id == SOLID_WALL_ID) {
                    w.solidified = true;
                } else if (id == BALL_ID) {
                    w.destroyed = true;
                }
            }
        }
    }

    private static boolean partnerCollision(ContactPoint p, GrowingWall w) {
        if (w != null && w.partner != null && (w.shape == p.shape1 && w.partner.shape == p.shape2 || w.shape == p.shape2 && w.partner.shape == p.shape1)) {
            return true;
        }
        return false;
    }

    private void makeSolidWall(GrowingWall w) {
        w.shape.m_userData = SOLID_WALL_ID;
        w.body.setLinearVelocity(new Vec2(0, 0));
        MassData md = new MassData();
        w.body.setMass(md);
    }

    private void newSolidWall(Vec2 position, float angle, float width, float length) {
        PolygonDef sd = new PolygonDef();
        sd.friction = 0;
        sd.setAsBox(width / 2, length / 2);
        sd.userData = SOLID_WALL_ID;
        BodyDef bd = new BodyDef();
        bd.angle = angle;
        bd.position = position;
        m_world.createBody(bd).createShape(sd);
    }

    private static float normalizeRadians(float radians) {
        while (radians >= TWO_PI) {
            radians -= TWO_PI;
        }
        while (radians < 0) {
            radians += TWO_PI;
        }

        return radians;
    }

    // Assumed that r1 and r2 are normalized
    private static float radianDistance(float r1, float r2) {
        float ret = r1 > r2 ? r1 - r2 : r2 - r1;
        return ret > PI ? TWO_PI - ret : ret;
    }

    public String getName() {
        return "Bouncing Balls";
    }
}
