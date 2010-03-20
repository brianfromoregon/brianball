import java.applet.Applet;
import java.applet.AudioClip;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.geom.Rectangle2D;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Random;

public class BrianBallModel extends Observable {
    public final int SLEEP_TIME = 10;
    public final int MAX_WAIT_TIME = 30;
    public final int WALL_STEPS = 100;
    public final int TICK_SLICE = 50;
    private ArrayList<Ball> balls;
    private ArrayList<Wall> walls;
    private ArrayList<Rectangle2D.Double> blk;
    private Wall newWall;
    private Dimension dim;
    private boolean frozen, running;
    private int lives;
    private int level;
    private int score;
    private Dimension wallSize;
    private boolean upDone, downDone, leftDone, rightDone;
    private Wall upWall, downWall, leftWall, rightWall;
    private int ratio;
    private boolean levelWon;
    private AudioClip ballHit, newWallHit, levelComplete, start;
    private URL ballHitURL, newWallHitURL, levelCompleteURL, startURL;
    private ArrayList<String> hScores;
    private File file;
    private double ballArea;

    public BrianBallModel() {
        hScores = new ArrayList<String>();
        this.dim = BrianBall.dim;
        wallSize = new Dimension((int)(dim.getWidth()/WALL_STEPS)+1,(int)(dim.getHeight()/WALL_STEPS)+1);
        frozen = true;
        score=0;
        try {
            levelCompleteURL = getClass().getResource("lc.bri");
            startURL = getClass().getResource("s.bri");
            ballHitURL = getClass().getResource("bh.bri");
            newWallHitURL = getClass().getResource("wh.bri");
            ballHit = Applet.newAudioClip(ballHitURL);
            newWallHit = Applet.newAudioClip(newWallHitURL);
            start = Applet.newAudioClip(startURL);
            levelComplete = Applet.newAudioClip(levelCompleteURL);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        file = new File("hs.bri");
        try {
            file.createNewFile();
            FileReader fIn = new FileReader(file);
            BufferedReader in = new BufferedReader(fIn);
            String line = in.readLine();
            while (line != null) {
                hScores.add(line);
                line = in.readLine();
            }
            in.close();
        } catch (Throwable e) { e.printStackTrace(); }
    }
    public boolean scoreMakesList() {
        boolean makesIt = false;
        if (hScores.size()==0) {
            hScores.add("Brian's Best");
            hScores.add("685");
            for (int i=0; i<8; i++) {
                hScores.add("Anon");
                hScores.add("0");
            }
        }
        for (int i=1; i<hScores.size(); i+=2) {
            try {
                int num = Integer.parseInt(hScores.get(i));
                if (score > num) {
                    makesIt = true;
                }
            } catch (NumberFormatException e) { makesIt = true; }
        }
        return makesIt;
    }
    public void doHighScore(String name) {
        try {
            if (name == null ||name.equals("")) name = "Anon";
            FileWriter fOut = new FileWriter(file);
            fOut.close();
            fOut = new FileWriter(file);
            BufferedWriter out = new BufferedWriter(fOut);
            if (hScores.size()==0) {
                for (int i=0; i<10; i++) {
                    hScores.add("Anon");
                    hScores.add("0");
                }
            }
            for (int i=1; i<hScores.size(); i+=2) {
                try {
                    int num = Integer.parseInt(hScores.get(i));
                    if (score > num) {
                        hScores.add(i-1,String.valueOf(score));
                        hScores.add(i-1,name);
                        hScores.remove(hScores.size()-1);
                        hScores.remove(hScores.size()-1);
                        break;
                    }
                } catch (NumberFormatException e) {
                    hScores.add(i-1,String.valueOf(score));
                    hScores.add(i-1,name);
                    hScores.remove(hScores.size()-1);
                    hScores.remove(hScores.size()-1);
                    break;
                }
            }
            int cSize = hScores.size();
            for (int i=0; i<cSize; i++) {
                out.write(hScores.get(i)+"\n");
            }
            for (int i=0; i<9-cSize/2; i++) {
                out.write("Anon"+"\n"+"0"+"\n");
                hScores.add("Anon");
                hScores.add("0");
                }
            out.close();
        }
        catch (IOException e) { System.out.println(e.toString()); }
    }
    public void newGame(int level) {
        levelWon = false;
        ratio = 0;
        leftDone = true; rightDone = true; upDone = true; downDone = true;
        /* empty ball array */
        balls = new ArrayList<Ball>();
        /* initial boundary walls */
        walls = new ArrayList<Wall>();
        /* initial black space */
        blk = new ArrayList<Rectangle2D.Double>();
        walls.add(new Wall(0,0,dim.getHeight(),wallSize.getWidth(),Color.black,true));
        walls.add(new Wall(dim.getWidth()-wallSize.getWidth(),0,dim.getHeight(),wallSize.getWidth(),Color.black,true));
        walls.add(new Wall(0,0,dim.getWidth(),wallSize.getHeight(),Color.black,false));
        walls.add(new Wall(0,dim.getHeight()-wallSize.getHeight(),dim.getWidth(),wallSize.getHeight(),Color.black,false));
        /* new walls */
        newWall = null;
        this.level = level;
        lives += level;
        
        Random random = new Random();
        double x, y;
        boolean added;
        for (int i = 0; i<level*2; i++) {
            long time = System.currentTimeMillis();
            do {
                x = random.nextDouble()*dim.getWidth();
                y = random.nextDouble()*dim.getHeight();
                added = addBall(x,y);
                if (System.currentTimeMillis()-time>MAX_WAIT_TIME) {
                    added=true;
                }
            } while (!added);
        }
        ballArea=0;
        for (int i=0; i<balls.size(); i++) {
            Ball b = balls.get(i);
            ballArea += (b.getWidth()*b.getHeight());
        }
        if (level==1) start.play();
    }
    
    public void start() {
        frozen = false;
        running = true;
        while (running) {
            while(!frozen) {
                try{ Thread.sleep(SLEEP_TIME); } catch(Exception e) { }
                tick();
            }
        }
    }
    public boolean addBall(double x, double y) {
        Random random = new Random();
        double mass = random.nextDouble()*245+10;
        double radius = mass/5+1;
        double velX = (random.nextDouble()-.5)*4;
        double velY = (random.nextDouble()-.5)*4;
        Color color;
        do {
            color = new Color((int)(random.nextDouble()*255),(int)(random.nextDouble()*255),(int)(random.nextDouble()*255));
        } while (color.getRed()+color.getBlue()+color.getGreen()>550);
        Ball b1 = new Ball(x-radius,y-radius,velX,velY,color,mass,radius);
        boolean error = false;
        for (int i = 0; i < balls.size(); i++) {
            Ball b2 = balls.get(i);
            double dx = (b1.getX()+b1.getRadius()) - (b2.getX()+b2.getRadius());
            double dy = (b1.getY()+b1.getRadius()) - (b2.getY()+b2.getRadius());
            double dxy = Math.sqrt(dx*dx + dy*dy);
            double maxD = b1.getRadius() + b2.getRadius();
            if (dxy <= maxD) { error=true; }
        }
        for (int i = 0; i < walls.size(); i++) {
            Wall w = walls.get(i);
            if (b1.intersects(w)) error=true;
        }
        if (!error) {
            balls.add(b1);  
        }
        return !error;
    }
    
    public void loseLife() {
        lives--;
    }
    
    public void addWall(double x, double y, boolean vertical) {
        upDone = false; downDone = false; leftDone = false; rightDone = false;
        if (vertical) {
            if (newWall == null) {
                newWall = new Wall(x-wallSize.getWidth()/2,y-1,wallSize.getWidth(),true);
                for (int i=0; i<balls.size();i++) {
                    Ball b = balls.get(i);
                    if (newWall != null && b.intersects(newWall)) newWall = null;
                }
                for (int i=0; i<walls.size();i++) {
                    Wall w = walls.get(i);
                    if (newWall != null && w.intersects(newWall)) newWall = null;
                }
                for (int i=0; i<blk.size();i++) {
                    Rectangle2D.Double wB = blk.get(i);
                    if (newWall != null && wB.intersects(newWall)) newWall = null;
                }
            }
        } else {
            if (newWall == null) {
                newWall = new Wall(x-1,y-wallSize.getHeight()/2,wallSize.getHeight(),false);
                for (int i=0; i<balls.size();i++) {
                    Ball b = balls.get(i);
                    if (newWall != null && b.intersects(newWall)) newWall = null;
                }
                for (int i=0; i<walls.size();i++) {
                    Wall w = walls.get(i);
                    if (newWall != null && w.intersects(newWall)) newWall = null;
                }
                for (int i=0; i<blk.size();i++) {
                    Rectangle2D.Double wB = blk.get(i);
                    if (newWall != null && wB.intersects(newWall)) newWall = null;
                }
            }
        }
    }
    
    public void pause() {
        frozen = true;
    }
    public void resume() {
        frozen = false;
    }
    private void tick() {
        /* This handles what balls will do on a tick */
        for (int i = 0; i < balls.size(); i++) {
            Ball b1 = balls.get(i);
            //collision with another ball
            for (int j = i + 1; j < balls.size(); j++) {
                Ball b2 = balls.get(j);
                checkBallCollision(b1,b2);
            }
            //collsiion with wall
            checkWallCollision(b1);
            b1.action();
        }
        /* this handles new wall growth */
        for (int i = 0; i < walls.size(); i++) {
            Wall w = walls.get(i);
            if (newWall != null && w.intersects(newWall)) {
                if (newWall.isVertical()) {
                    if (newWall.getY()+newWall.getHeight()/2  > w.getY()) {
                        upWall = w;
                        upDone = true;
                    }
                    if (newWall.getY()+newWall.getHeight()/2  <= w.getY()) {
                        downDone = true;
                        downWall = w;
                    }
                    if (upDone && downDone) {
                        newWall.setRect(newWall.getX(),upWall.getY()+upWall.getHeight(),newWall.getWidth(),downWall.getY()-upWall.getY()-upWall.getHeight());
                        doGrayArea();
                        newWall.setColor(Color.black);
                        //newWall.setColor(new Color((int)(new Random().nextDouble()*255),(int)(new Random().nextDouble()*255),(int)(new Random().nextDouble()*255)));
                        walls.add(newWall);
                        newWall = null;
                    }
                } else {
                    if (newWall.getX()+newWall.getWidth()/2 > w.getX()) {
                        leftDone = true;
                        leftWall = w;
                    }
                    if (newWall.getX()+newWall.getWidth()/2  <= w.getX()) {
                        rightDone = true;
                        rightWall = w;
                    }
                    if (leftDone && rightDone) {
                        newWall.setRect(leftWall.getX()+leftWall.getWidth(),newWall.getY(),rightWall.getX()-leftWall.getX()-leftWall.getWidth(),newWall.getHeight());
                        doGrayArea();
                        newWall.setColor(Color.black);
                        //newWall.setColor(new Color((int)(new Random().nextDouble()*255),(int)(new Random().nextDouble()*255),(int)(new Random().nextDouble()*255)));
                        walls.add(newWall);
                        newWall = null;
                    }
                }
            }
        }
        if (newWall != null) {
            newWall.action(downDone,upDone,leftDone,rightDone);
        }
        setChanged();
        notifyObservers();
        if (levelWon) {
            level+=1;
            score+=ratio;
            newGame(level); 
        }
    }
    
    private void doGrayArea() {
        Rectangle2D.Double r1, r2;
        boolean r1Empty=true, r2Empty=true, numDone=false;      
        int num=0;
        if (newWall.isVertical()) {
            r1 = new Rectangle2D.Double(newWall.getX()+newWall.getWidth(),newWall.getY(),num,downWall.getY()-newWall.getY());
            while(!numDone) {
                num++;
                r1.setRect(newWall.getX()+newWall.getWidth(),newWall.getY(),num,downWall.getY()-newWall.getY());
                for (int i=0; i<walls.size();i++) {
                    if (r1.intersects(walls.get(i))) { 
                        numDone = true;
                    }
                }
            }
            num-=1;
            r1.setRect(newWall.getX()+newWall.getWidth(),newWall.getY(),num,downWall.getY()-newWall.getY());
            num = 0;
            numDone=false;
            r2 = new Rectangle2D.Double(num,newWall.getY(),newWall.getX()-num,newWall.getHeight());
            while(!numDone) {
                numDone = true;
                num++;
                r2.setRect(num,newWall.getY(),newWall.getX()-num,newWall.getHeight());
                for (int i=0; i<walls.size();i++) {
                    if (r2.intersects(walls.get(i))) { 
                        numDone = false;
                    }
                }
            }
            num-=1;
            r2.setRect(num,newWall.getY(),newWall.getX()-num,newWall.getHeight());
        } else {
            r1 = new Rectangle2D.Double(newWall.getX(),newWall.getY()+newWall.getHeight(),rightWall.getX()-newWall.getX()+1,num);
            while(!numDone) {
                num++;
                r1.setRect(newWall.getX(),newWall.getY()+newWall.getHeight(),rightWall.getX()-newWall.getX(),num);
                for (int i=0; i<walls.size();i++) {
                    if (r1.intersects(walls.get(i))) { 
                        numDone = true; 
                    }
                }
            }
            num = 0;
            numDone=false;
            r2 = new Rectangle2D.Double(newWall.getX(),num,newWall.getWidth(),newWall.getY()-num);
            while(!numDone) {
                numDone = true;
                num++;
                r2.setRect(newWall.getX(),num,newWall.getWidth(),newWall.getY()-num);
                for (int i=0; i<walls.size();i++) {
                    if (r2.intersects(walls.get(i))) { 
                        numDone = false;
                    }
                }
            }
            num-=1;
            r2.setRect(newWall.getX(),num,newWall.getWidth(),newWall.getY()-num);
        }
        for(int i=0;i<balls.size();i++) {
            Ball b = (Ball)balls.get(i);
            if (r1.intersects(b.getBounds2D())) r1Empty=false;
            if (r2.intersects(b.getBounds2D())) r2Empty=false;
        }
        if (r1Empty) {
            blk.add(r1);
        }
        if (r2Empty) {
            blk.add(r2);
        }
        if (checkWin()) {
            levelComplete.play();
            levelWon=true;
        }
    }
    
    private boolean checkWin() {
        double totalArea, grayArea;
        totalArea = dim.getHeight()*dim.getWidth();
        grayArea=0;
        for (int i=0; i<walls.size(); i++) {
            Wall w = walls.get(i);
            grayArea += w.getHeight()*w.getWidth();
        }
        for (int i=0; i<blk.size(); i++) {
            Rectangle2D.Double r = blk.get(i);
            grayArea += r.getHeight()*r.getWidth();
        }
        ratio = (int)(grayArea*100/(totalArea-ballArea));
        if (ratio >= 75) {
            return true;
        } else return false;
    }
    
    private void checkWallCollision(Ball bNow) {
        //where the center of ball will be next tick
        double nextX = bNow.getX()+bNow.getVelX();
        double nextY = bNow.getY()+bNow.getVelY();
        Ball bNext = (Ball)bNow.clone();
        bNext.setFrame(nextX,nextY,bNow.getWidth(),bNow.getHeight());
        for (int i = 0; i < walls.size(); i++) {
            Wall w = walls.get(i);
            if (!bNow.intersects(w) && bNext.intersects(w)) {
                if (w.isVertical()) bNow.setVelX(-bNow.getVelX());
                else bNow.setVelY(-bNow.getVelY());
            }
        }
        if (newWall != null && bNext.intersects(newWall)) {
            newWallHit.play();
            newWall = null;
            loseLife();
        }
        /* if ball somehow goes outside exterior walls, remove it and try to add new ball */
        if (bNow.getX()+2*bNow.getRadius()<0 || bNow.getY()+2*bNow.getRadius()<0
                || bNow.getX()>dim.getWidth() || bNow.getY()>dim.getHeight()) {
            balls.remove(bNow);
            System.out.println(balls.size());
            double x,y;
            boolean added;
            long time = System.currentTimeMillis();
            Random random = new Random();
            do {
                x = random.nextDouble()*dim.getWidth();
                y = random.nextDouble()*dim.getHeight();
                added = addBall(x,y);
                if (System.currentTimeMillis()-time>MAX_WAIT_TIME) added=true;;
            } while (!added);
        }
    }
    
    public ArrayList<Ball> getBalls() {
        return balls;
    }
    
    private void checkBallCollision(Ball b1, Ball b2) {
        double vx1 = b1.getVelX(), vx2 = b2.getVelX(), vy1 = b1.getVelY(), vy2 = b2.getVelY();
        double ed = 1;
        double dxNow = (b2.getX()+b2.getRadius()) - (b1.getX()+b1.getRadius());
        double dyNow = (b2.getY()+b2.getRadius()) - (b1.getY()+b1.getRadius());
        double dxNext = (b2.getX()+b2.getVelX()+b2.getRadius()) - (b1.getX()+b1.getVelX()+b1.getRadius());
        double dyNext = (b2.getY()+b2.getVelY()+b2.getRadius()) - (b1.getY()+b1.getVelY()+b1.getRadius());
        double dNow = Math.sqrt(dxNow*dxNow+dyNow*dyNow);
        double dNext = Math.sqrt(dxNext*dxNext+dyNext*dyNext);
        if (dNext < b1.getRadius()+b2.getRadius() && dNow > b1.getRadius()+b2.getRadius()) {
            ballHit.play();
            // Unit vector in the direction of the collision
            double ax=dxNext/dNext, ay=dyNext/dNext;
            // Projection of the velocities in these axes
            double va1=(vx1*ax+vy1*ay), vb1=(-vx1*ay+vy1*ax);
            double va2=(vx2*ax+vy2*ay), vb2=(-vx2*ay+vy2*ax);
            // New velocities in these axes (after collision): ed<=1, for elastic collision ed=1
            double vaP1=va1 + (1+ed)*(va2-va1)/(1+b1.getMass()/b2.getMass());
            double vaP2=va2 + (1+ed)*(va1-va2)/(1+b2.getMass()/b1.getMass());
            // Undo the projections
            b1.setVelX(vaP1*ax-vb1*ay); b1.setVelY(vaP1*ay+vb1*ax);
            b2.setVelX(vaP2*ax-vb2*ay); b2.setVelY(vaP2*ay+vb2*ax);
        }
    }
    
    public ArrayList<Wall> getWalls() {
        return walls;
    }
    
    public Wall getNewWall() {
        return newWall;
    }
    
    public int getLives() {
        return lives;
    }

    public ArrayList<Rectangle2D.Double> getBlk() {
        return blk;
    }    
    
    public int getRatio() {
        return ratio;
    }

    public int getLevel() {
        return level;
    }

    public int getScore() {
        return score;
    }
    public void setRunning(boolean running) {
        this.running = running;
    }
    public ArrayList<String> getHScores() {
        return hScores;
    }
    public void setScore(int score) {
        this.score = score;
    }
    public void setLives(int lives) {
        this.lives = lives;
    }
}