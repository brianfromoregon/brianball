import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.UIManager;

public class BrianBallControl extends JPanel implements Observer, MouseListener, KeyListener{
    private BrianBallModel model;
    private JLabel lives,grayedOut,score,level;
    Font f1,f2,f3;
    
    public BrianBallControl(BrianBallModel model) {
        this.model = model;
        model.addObserver(this);
        f1 = new Font("Arial",Font.BOLD,16);
        f2 = new Font("Arial",Font.BOLD,13);
        f3 = new Font("Arial",Font.PLAIN,16);
        UIManager.put("OptionPane.font", f2);
        UIManager.put("OptionPane.messageFont", f2);
        UIManager.put("OptionPane.buttonFont", f2);
    }
    
    public void update(Observable m, Object arg) {
        score.setText(String.valueOf(model.getScore()));
        level.setText(String.valueOf(model.getLevel()));
        if (model.getLives()==-1){
            model.pause();
            lives.setText("DEAD!");
            if (model.scoreMakesList()) {
                String name; 
                name = JOptionPane.showInputDialog(this,"You made the high score list!!\nEnter your name:",
                    "Congratulations",JOptionPane.PLAIN_MESSAGE);
                model.doHighScore(name);
            }
            StringBuffer scores = new StringBuffer();
            ArrayList<String> hScores = model.getHScores();
            for (int i=0; i<hScores.size();i+=2) {
                scores.append((i+2)/2+".     "+hScores.get(i)+" - "+hScores.get(i+1)+"\n");
            }
            JOptionPane.showMessageDialog(this,scores,"Brian Ball High Score List",JOptionPane.INFORMATION_MESSAGE);
            int response = JOptionPane.showConfirmDialog(this,"Would you like to play again?","Brian Ball",JOptionPane.YES_NO_OPTION,JOptionPane.QUESTION_MESSAGE);
            if (response == 1) {
                model.setRunning(false);
            } else {
                model.setScore(0);
                model.setLives(0);
                model.newGame(1);
                model.resume();
            }
            
        } else if (model.getLives()==0){
            lives.setText("NONE"); 
        } else {
            lives.setText(String.valueOf(model.getLives()));
        }
        grayedOut.setText(model.getRatio()+"%");
        if (model.getRatio() >= 75) {
            model.pause();
            JOptionPane.showMessageDialog(this,"Niiice, level "+model.getLevel()+" completed." +
                    "\n\nHere we go again...","Good Work",JOptionPane.INFORMATION_MESSAGE);
            model.resume();
        }
    }
    
    public void createGUI(BrianBallView view) {
        this.setLayout(new BorderLayout());
        view.addMouseListener(this);
        view.addKeyListener(this);
        this.add(view, BorderLayout.CENTER);
        JPanel status = new JPanel();
        status.setFont(f1);
        JLabel label1 = new JLabel("Lives Remaining:");
        label1.setFont(f3);
        JLabel label2 = new JLabel("of screen cleared           ");
        label2.setFont(f3);
        JLabel label3 = new JLabel("           Score:");
        label3.setFont(f3);
        JLabel label4 = new JLabel("           Level:");
        label4.setFont(f3);
        score = new JLabel("0");
        score.setForeground(Color.red);
        score.setFont(f1);
        lives = new JLabel("1");
        lives.setForeground(Color.red);
        lives.setFont(f1);
        grayedOut = new JLabel("0%");
        grayedOut.setForeground(Color.red);
        grayedOut.setFont(f1);
        level = new JLabel("1");
        level.setForeground(Color.red);
        level.setFont(f1);
        status.setLayout(new FlowLayout());
        status.add(grayedOut);
        status.add(label2);
        status.add(label1);
        status.add(lives);
        status.add(label3);
        status.add(score);
        status.add(label4);
        status.add(level);
        view.setFocusable(true);
        this.add(status, BorderLayout.SOUTH);
        JOptionPane.showMessageDialog(this,"Trap the balls into smaller and smaller areas by creating new walls around them." +
                "\n\n- Goal of the game is to \"gray out\" 75% of the screen." +
                "\n- Left click to add a vertical wall, right click to add a horizontal wall." +
                "\n\nGood Luck!!","Brian Ball",JOptionPane.INFORMATION_MESSAGE);
        
    }
    public void mouseClicked(MouseEvent e) {}
    public void mouseEntered(MouseEvent e) {}
    public void mouseExited(MouseEvent e) {}
    public void mousePressed(MouseEvent e) {
        if (e.getButton()==MouseEvent.BUTTON1) {
            model.addWall((double)e.getX(),(double)e.getY(),true);
        } else if (e.getButton()==MouseEvent.BUTTON3) {
            model.addWall((double)e.getX(),(double)e.getY(),false);
        }
    }
    public void mouseReleased(MouseEvent e) {}
    public void keyTyped(KeyEvent e) {}
    public void keyPressed(KeyEvent e) { 
        if (e.getKeyCode()==113) {
            model.pause();
            model.setScore(0);
            model.newGame(1);
            model.resume();
        }
    }
    public void keyReleased(KeyEvent e) {}
    
}
