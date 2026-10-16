import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.swing.*;

/**
 * A program that makes the gem match game in a GUI. The GUI has a side panel
 * that shows the current level, the score, moves remaining, multiplier, and a
 * start button that resets game. Also has a timer for matches, so you can see them
 * for a bit until they disappear. It also draws the gems with certain shapes
 * depending on the gem. Also has a method for checking what types of gems were
 * matched and doing the corresponding bookkeeping. Also shows a message depending
 * on whether you won the game or lost.
 * @author Brandon Pham
 * For CS251
 */

public class GemMatchGUI extends JPanel implements ActionListener, MouseListener {

    int panelWidth, panelHeight;
    private final GemManager gemManager;
    private char[][] board;
    private int selectedRow = -1, selectedCol = -1;
    private JLabel levelLabel;
    private JLabel scoreLabel;
    private JLabel movesLabel;
    private JLabel multiplierLabel;
    private int cascadeCount = 0;
    private int score = 0;
    private int moves = 25;
    private double multiplier = 1.00;
    private static final int INITIAL_TARGET_SCORE = 5000;
    private int currentLevel = 1;
    private static final int MAX_LEVEL = 5;
    private static final int[] LEVEL_TARGETS = {5000, 10000, 20000, 35000, 50000};
    private List<int[]> matchedGems;
    private Timer removalTimer;
    private Timer dropTimer;
    private boolean isAnimating = false;
    private static final int ANIMATION_DELAY = 750; // ms delay 

    public GemMatchGUI(int panelWidth, int panelHeight) {
        this.panelWidth = panelWidth;
        this.panelHeight = panelHeight;
        JPanel sidePanel = new JPanel();
        JButton button = new JButton("Start Game");
        gemManager = new GemManager(10, 10);
        board = gemManager.fillBoard();
        scoreLabel = new JLabel("Score: 0/" + INITIAL_TARGET_SCORE);
        movesLabel = new JLabel("Moves: " + moves);
        multiplierLabel = new JLabel("Multiplier: " + multiplier + "x");
        levelLabel = new JLabel("Level: " + currentLevel);

        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(this.panelWidth, this.panelHeight));
        setBackground(Color.BLACK);

        sidePanel.setLayout(new BoxLayout(sidePanel, BoxLayout.Y_AXIS));
        sidePanel.setPreferredSize(new Dimension(200, panelHeight));
        sidePanel.add(levelLabel);
        sidePanel.add(scoreLabel);
        sidePanel.add(movesLabel);
        sidePanel.add(multiplierLabel);
        sidePanel.add(button);

        add(sidePanel, BorderLayout.EAST);
        addMouseListener(this);

        // Resets board state and starts a new game
        button.addActionListener(e -> {
            currentLevel = 1;
            score = 0;
            moves = 25;
            multiplier = 1.00;
            cascadeCount = 0;
            board = gemManager.fillBoard();
            levelLabel.setText("Level: " + currentLevel);
            scoreLabel.setText("Score: 0/" + INITIAL_TARGET_SCORE);
            movesLabel.setText("Moves: " + moves);
            multiplierLabel.setText("Multiplier: 1.00x");
            repaint();
        });
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        draw(g);
    }

    // Getter that gets the color needed for any gem
    private Color getGemColor(char gem) {
        return switch (gem) {
            case '+' -> Color.WHITE; // Jade
            case '.' -> Color.MAGENTA; // Amethyst
            case '*' -> Color.YELLOW; // Star
            case '@' -> Color.RED; // Ruby
            case 'v' -> Color.CYAN; // Diamond
            case '=' -> Color.GREEN; // Emerald
            default -> Color.GRAY;
        };
    }

    /**
     * Draws the gems on the board. Draws different types of shapes depending on
     * the gem type except for jade and amethyst.
     * @param g How the method draws the shapes for the game
     */
    public void draw(Graphics g) {

        int rows = board.length;
        int cols = board[0].length;
        int cellWidth = (panelWidth - 211) / cols;
        int cellHeight = (panelHeight - 40) / rows;
        int space = 5; // Space between gems

        // Loop through the board and draw each gem
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                char gem = board[row][col];
                g.setColor(getGemColor(gem));

                // Calculate position and size for the gem
                int x = col * cellWidth + space;
                int y = row * cellHeight + space;
                int width = cellWidth - 2 * space;
                int height = cellHeight - 2 * space;

                // Draw different shapes based on gem type
                if (gem == GemTypes.Gems.EMERALD.gem) {
                    // Oval for emerald
                    g.fillOval(x, y, width, height);
                } else if (gem == GemTypes.Gems.DIAMOND.gem) {
                    // Triangle for diamond
                    int[] xPoints = {
                            x + width / 2,    // top point
                            x + width,      // right point
                            x              // left point
                    };
                    int[] yPoints = {
                            y,             // top point
                            y + height,    // right point
                            y + height     // left point
                    };
                    g.fillPolygon(xPoints, yPoints, 3);
                } else if (gem == GemTypes.Gems.STAR.gem) {
                    // "Star"
                    int[] xPoints = {
                            x + width / 2,
                            x + width,
                            x + width / 2,
                            x,
                            x + width / 2
                    };
                    int[] yPoints = {
                            y,
                            y + height / 2,
                            y + height,
                            y + height / 2,
                            y
                    };
                    g.fillPolygon(xPoints, yPoints, 4);
                } else if (gem == GemTypes.Gems.RUBY.gem) {
                    g.fillRoundRect(x, y, width, height, 25, 25);
                } else {
                    // Draw rectangle for other gems
                    g.fillRect(x, y, width, height);
                }

                // Highlight selected gem
                if (row == selectedRow && col == selectedCol) {
                    g.setColor(Color.WHITE);
                    g.drawRect(col * cellWidth, row * cellHeight,
                            cellWidth, cellHeight);
                    g.drawRect(col * cellWidth + 1, row * cellHeight + 1,
                            cellWidth - 2, cellHeight - 2);
                }
            }
        }

    }

    /**
     * Plays a little jingle when any match happens. Prints an error message
     * with what the error is
     */
    public void playMatchSound() {
        try {
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(
                    new File("GemMatch\\src\\matchSound5.wav"));
            Clip clip = AudioSystem.getClip();
            clip.open(audioInputStream);
            clip.start();
        } catch (Exception e) {
            System.err.println("Error playing sound: " + e.getMessage());
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
    }

    /**
     * When you pass a level or win the whole game, pops up a window saying so.
     * If a level was passed, also does the bookkeeping necessary to reset for
     * the next level
     */
    public void showWinMessage() {

        isAnimating = true; // Prevent further moves

        if (currentLevel < MAX_LEVEL) {

            JOptionPane.showMessageDialog(this,
                    "Congratulations! You progressed to the next level!",
                    "Level " + currentLevel,
                    JOptionPane.INFORMATION_MESSAGE);

            // Progress to next level
            currentLevel++;
            score = 0; // Reset score
            moves = 25; // Reset moves
            multiplier = multiplier - 1.00; // Nerf multiplier
            board = gemManager.fillBoard(); // New board
            levelLabel.setText("Level: " + currentLevel);
            scoreLabel.setText("Score: " + score + "/" +
                    LEVEL_TARGETS[currentLevel - 1]);
            movesLabel.setText("Moves: " + moves);
            multiplierLabel.setText("Multiplier: " + multiplier + "x");
            isAnimating = false;
            repaint();
        } else {
            // Game complete
            JOptionPane.showMessageDialog(this,
                    "Congratulations! You've completed all of the levels! " +
                            "Click 'Start Game' to play again.",
                    "You Win!",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }

    /**
     * Pops up a window saying you lose when you run out of moves and don't make
     * it to the target score for that level
     */
    public void showLoseMessage() {

        isAnimating = true; // Prevent further moves
        JOptionPane.showMessageDialog(this,
                "You've run out of moves! Better luck next time!",
                "Game Over!", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Processes all matched gems on the board and then updates the game state.
     * Also handles the scoring system, special gem effects, and UI updates.
     * @param matches List of int arrays, where each array contains coords of
     * matched gems
     */
    public void processMatches(List<int[]> matches) {

        // If no matches exist no need to come here
        if (matches.isEmpty()) {
            return;
        }

        // Play sound when match happens
        playMatchSound();

        // Count matches of each type
        Map<Character, Integer> matchCounts = new HashMap<>();
        for (int[] pos : matches) {
            char gem = board[pos[0]][pos[1]];
            // Combines gem counts by adding values when the same gem type is found.
            matchCounts.merge(gem, 1, Integer::sum);
        }

        // Process each type of match
        for (Map.Entry<Character, Integer> entry : matchCounts.entrySet()) {
            char gem = entry.getKey();
            int count = entry.getValue();

            if (gem == GemTypes.Gems.DIAMOND.gem) {
                // Diamond gem: increase multiplier
                // (gives 0.25 for each 3 match + 0.15 for each extra)
                multiplier += 0.25 + (Math.max(0, count - 3) * 0.15);
            } else if (gem == GemTypes.Gems.EMERALD.gem) {
                // Emerald gem: add moves
                // (gives 1 for each 3 match + 1 for each extra)
                moves += 1 + Math.max(0, count - 3);
            } else {
                // Regular gems: add points
                int basePoints = 50 * count; // Base points per match
                int comboBonus = cascadeCount * 80; // Bonus for cascades
                score += (basePoints + comboBonus) * multiplier;
            }
        }

        // Update UI
        scoreLabel.setText("Score: " + score + "/" +
                LEVEL_TARGETS[currentLevel - 1]);
        movesLabel.setText("Moves: " + moves);
        multiplierLabel.setText(String.format("Multiplier: %.2fx", multiplier));

        // Check win/lose condition
        if (score >= LEVEL_TARGETS[currentLevel - 1]) {
            showWinMessage();
        }
        if (moves == 0) {
            showLoseMessage();
        }
    }

    /**
     * Uses remove gems from GemManager to create the animation for the game.
     * Uses Timer
     */
    public void startRemovalAnimation() {

        // Create and start removal timer
        removalTimer = new Timer(ANIMATION_DELAY, e -> {
            processMatches(matchedGems); // Process matches before removing
            gemManager.removeGems();
            board = gemManager.board;
            repaint();

            // Start drop timer after removal
            dropTimer = new Timer(ANIMATION_DELAY, e2 -> {
                // Check for new matches
                matchedGems = GemManager.findMatches(board);
                if (!matchedGems.isEmpty()) {
                    cascadeCount++;
                    startRemovalAnimation(); // Recursively call itself for new matches
                } else {
                    isAnimating = false; // Animation complete
                    cascadeCount = 0; // Reset cascade count
                }
                ((Timer) e2.getSource()).stop();
            });
            dropTimer.setRepeats(false);
            dropTimer.start();

            ((Timer) e.getSource()).stop();
        });
        removalTimer.setRepeats(false);
        removalTimer.start();
    }

    /**
     * When mouse clicks happen this method describes what to do. First checks if
     * the click was on the board and then swaps the two gems if they're able to
     * be swapped. Then uses findMatches() from GemManager to find matches and
     * then if it does find matches, subtracts a move and then starts the
     * removal process
     * @param e the event to be processed
     */
    @Override
    public void mouseClicked(MouseEvent e) {

        // Prevent user interaction during animation
        if (isAnimating || moves <= 0) {
            return;
        }

        int rows = board.length;
        int cols = board[0].length;
        int cellWidth = (panelWidth - 200) / cols; // Adjust for side panel
        int cellHeight = panelHeight / rows;

        // Calculate clicked row and column
        int clickedCol = e.getX() / cellWidth;
        int clickedRow = e.getY() / cellHeight;

        // Check if the click is within the board
        if (clickedRow >= 0 && clickedRow < rows && clickedCol >= 0
                && clickedCol < cols) {
            if (selectedRow == -1 && selectedCol == -1) {
                // First gem selected
                selectedRow = clickedRow;
                selectedCol = clickedCol;
            } else {
                // Second gem selected, attempt to swap
                gemManager.swapGems(selectedRow, selectedCol,
                        clickedRow, clickedCol);
                board = gemManager.board; // Update the board after swap

                // Find matches after swap
                matchedGems = GemManager.findMatches(board);
                if (!matchedGems.isEmpty()) {
                    moves--;
                    movesLabel.setText("Moves: " + moves);
                    isAnimating = true;
                    startRemovalAnimation();
                }
                // Reset selection
                selectedRow = -1;
                selectedCol = -1;
            }
            repaint(); // Redraw the board
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
    }

    @Override
    public void mouseReleased(MouseEvent e) {
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

}

