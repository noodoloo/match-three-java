import java.util.*;

/**
 * A program that handles the internal logic for the gem match game as well as
 * the bookkeeping. Has swap gems and remove gems methods as well as a method
 * that fills the board with the "gems" and a method that finds the matches.
 * @author Brandon Pham
 * For CS251
 */

public class GemManager {

    private static final Random rand = new Random();
    private static final GemTypes.Gems[] gems = GemTypes.Gems.ORIGINAL;
    private final int rows;
    private final int cols;
    protected char[][] board;

    public GemManager(int row, int col) {
        this.rows = row;
        this.cols = col;
        this.board = fillBoard();
    }

    // Checks if a certain spot is on the board by comparing the row and column
    // to the max row and column.
    private static boolean isValidPosition(int row, int col, int maxRow, int maxCol) {
        return row >= 0 && row < maxRow && col >= 0 && col < maxCol;
    }

    /**
     * A method that goes through the board and checks for any gem matches
     * horizontally and vertically, marks those positions, and then adds them to
     * a List of int arrays
     * @param board Board that is going to be checked for matches
     * @return A List of int arrays that has the positions of gems that need
     * removal
     */
    public static List<int[]> findMatches(char[][] board) {

        int rows = board.length;
        int cols = board[0].length;
        // Marks the position of gems that need removal so there is no overlap
        // that can lead to incorrect num of gems that need removal
        boolean[][] mark = new boolean[rows][cols];

        // Check for horizontal line matches
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols - 2; c++) {
                char g = board[r][c];
                if (g != ' ' && g == board[r][c + 1] && g == board[r][c + 2]) {
                    mark[r][c] = mark[r][c + 1] = mark[r][c + 2] = true;
                }
            }
        }
        // Check for vertical line matches
        for (int c = 0; c < cols; c++) {
            for (int r = 0; r < rows - 2; r++) {
                char g = board[r][c];
                if (g != ' ' && g == board[r + 1][c] && g == board[r + 2][c]) {
                    mark[r][c] = mark[r + 1][c] = mark[r + 2][c] = true;
                }
            }
        }
        // If the mark at a certain position is true, add it to the List
        List<int[]> matched = new ArrayList<>();
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (mark[r][c]) {
                    matched.add(new int[]{r, c});
                }
            }
        }
        return matched;
    }

    /**
     * A method that fills a 2d char array with randomly placed gems and checks if
     * any matches happen and if they do, keep retrying until there are no more
     * matches
     * @return A board filled with gems that has no initial matches
     */
    public char[][] fillBoard() {

        board = new char[rows][cols];
        boolean hasMatch;

        do {
            // Fill each cell with a random gem, retrying if placing it would
            // cause a match
            for (int r = 0; r < rows; r++) {
                for (int c = 0; c < cols; c++) {
                    do {
                        board[r][c] = gems[rand.nextInt(gems.length)].gem;
                    } while (
                        // Check if placing this gem creates a horizontal match
                            (c >= 2 && board[r][c] == board[r][c - 1]
                                    && board[r][c] == board[r][c - 2]) ||
                                    // Check if placing this gem creates a vertical match
                                    (r >= 2 && board[r][c] == board[r - 1][c]
                                            && board[r][c] == board[r - 2][c])
                    );
                }
            }
            // After filling the board, check if any matches exist
            hasMatch = !findMatches(board).isEmpty();
            // Repeat the entire process if there is a match
        } while (hasMatch);

        return board;
    }

    /**
     * A method that swaps one gem with another adjacent gem and if the swap does
     * not result in a match, cancel the swap
     * @param initialRow The row of the gem that was picked to be swapped
     * @param initialCol The col of the gem that was picked to be swapped
     * @param otherRow The row of the gem that's going to be swapped with the
     * initial gem
     * @param otherCol The col of the gem that's going to be swapped with the
     * initial gem
     */
    public void swapGems(int initialRow, int initialCol,
                                         int otherRow, int otherCol) {

        // Check if the positions are valid
        if (!isValidPosition(initialRow, initialCol, rows, cols)
                || !isValidPosition(otherRow, otherCol, rows, cols)) {
            return;
        }

        // Check if the two gems are adjacent (horizontal or vertical)
        if (Math.abs(initialRow - otherRow)
                + Math.abs(initialCol - otherCol) != 1) {
            return;
        }

        // Swap the gems
        char tempGem = board[initialRow][initialCol];
        board[initialRow][initialCol] = board[otherRow][otherCol];
        board[otherRow][otherCol] = tempGem;

        // Checks if the swap created any matches, undoes the swap otherwise
        if (findMatches(board).isEmpty()) {
            board[otherRow][otherCol] = board[initialRow][initialCol];
            board[initialRow][initialCol] = tempGem;
        }
    }

    /**
     * A method that removes all currently matched gems from the board, has the
     * remaining gems fall down to fill the gaps, and then refills the empty
     * space at the top of the board with new random gems.
     */
    public void removeGems() {

        // Uses the findMatches() method to check if there are matches
        List<int[]> matches = findMatches(board);
        if (matches.isEmpty()) {
            return;
        }

        // Remove matched gems by marking them as empty
        for (int[] pos : matches) {
            board[pos[0]][pos[1]] = ' ';
        }

        // Make gems fall down to fill empty spaces
        for (int c = 0; c < cols; c++) {
            int fillRow = rows - 1;
            for (int r = rows - 1; r >= 0; r--) {
                if (board[r][c] != ' ') {
                    board[fillRow--][c] = board[r][c];
                }
            }
            // Fill empty top spaces with new random gems
            for (int r = fillRow; r >= 0; r--) {
                board[r][c] = gems[rand.nextInt(gems.length)].gem;
            }
        }
    }

    /**
     * A method that makes a string representation of the board
     * @return A string representation of the board state
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (char[] row : board) {
            for (char gem : row) {
                sb.append(gem).append(' ');
            }
            sb.append('\n');
        }
        return sb.toString();
    }
}
