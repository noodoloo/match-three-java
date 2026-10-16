import javax.swing.*;

/**
 * Class that holds the main method to run the game. Extends JFrame to set the
 * width and height and other necessary arrangements.
 * @author Brandon Pham
 * For CS251
 */


public class GemMatch extends JFrame {
    public static void main(String[] args) {
        int defaultWidth = 1100;
        int defaultHeight = 750;

        JFrame frame = new JFrame("Gem Match");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(defaultWidth, defaultHeight);

        GemMatchGUI gemMatchGUI = new GemMatchGUI(defaultWidth, defaultHeight);
        frame.add(gemMatchGUI);

        frame.setVisible(true);
        frame.setResizable(false);
        frame.setLocationRelativeTo(null);

    }
}