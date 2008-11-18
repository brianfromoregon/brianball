import java.awt.Dimension;

import javax.swing.JFrame;

public class BrianBall {
    public static Dimension dim;
    
    
    public static void main(String[] args) {
        dim = new Dimension(700,600);
        /* MVC */
        BrianBallModel model = new BrianBallModel();
        BrianBallView view = new BrianBallView(model);
        view.setPreferredSize(dim);
        BrianBallControl control = new BrianBallControl(model);
        control.createGUI(view);
        
        JFrame.setDefaultLookAndFeelDecorated(true);
        JFrame f = new JFrame("Brian Ball");
        f.getContentPane().add(control);
        f.pack();
        f.setLocationRelativeTo(null);
        f.setResizable(false);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setVisible(true);
        
        model.newGame(1);
        model.start();
        System.exit(0);
    }
    
}
