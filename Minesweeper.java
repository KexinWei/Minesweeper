import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Random;
import javax.swing.*;

// Class representing a Minesweeper game
public class Minesweeper {

    // UI components and game state variables
    private JFrame frame = new JFrame("Minesweeper");
    private JPanel mainPanel = new JPanel(new BorderLayout());
    private JPanel boardPanel;
    private JLabel statusLabel = new JLabel();
    private JPanel statusPanel = new JPanel();
    
    // Game board dimensions and mine count
    private int rowCount;
    private int columnCount;
    private int mineCount;
    private static final int TILE_SIZE = 70; // Size of each tile
    private TileButton[][] tiles; // Grid of tiles
    private ArrayList<TileButton> mines; // List to track mines
    private Random random = new Random();

    // Game state variables
    private int revealedTilesCount;
    private boolean isGameOver;

    // Timer components
    private JLabel timerLabel = new JLabel("Time: 0", JLabel.CENTER);
    private Timer timer;
    private int elapsedTime = 0;

    // Enum to define difficulty levels
    enum Difficulty {
        EASY(9, 9, 10),
        MEDIUM(16, 16, 40),
        HARD(24, 24, 99);

        final int rows;
        final int columns;
        final int mineCount;

        // Constructor for Difficulty enum
        Difficulty(int rows, int columns, int mineCount) {
            this.rows = rows;
            this.columns = columns;
            this.mineCount = mineCount;
        }
    }

    // Constructor for Minesweeper
    public Minesweeper() {
        setupFrame();
        setupMenu();
        setupBoard(Difficulty.EASY);
        frame.setVisible(true);
    }

    // Sets up the main game frame
    private void setupFrame() {
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());
        frame.add(mainPanel, BorderLayout.CENTER);
        frame.pack();
        frame.setLocationRelativeTo(null);
    }

    // Sets up the game menu
    private void setupMenu() {
        JMenuBar menuBar = new JMenuBar();
        JMenu menu = new JMenu("Difficulty");
        for (Difficulty difficulty : Difficulty.values()) {
            JMenuItem menuItem = new JMenuItem(difficulty.name());
            menuItem.addActionListener(e -> resetBoard(difficulty));
            menu.add(menuItem);
        }
        menuBar.add(menu);
        frame.setJMenuBar(menuBar);
    }

    // Sets up the game board based on difficulty
    private void setupBoard(Difficulty difficulty) {
        initializeGame(difficulty);
        mainPanel.add(boardPanel, BorderLayout.CENTER);
        frame.pack();
    }

    // Resets the game board with a new difficulty level
    private void resetBoard(Difficulty difficulty) {
        stopTimer();
        elapsedTime = 0;
        timerLabel.setText("Time: 0");
        mainPanel.remove(boardPanel);
        setupBoard(difficulty);
    }

    // Initializes game variables and sets up the game panel
    private void initializeGame(Difficulty difficulty) {
        rowCount = difficulty.rows;
        columnCount = difficulty.columns;
        mineCount = difficulty.mineCount;
        int boardWidth = columnCount * TILE_SIZE;
        int boardHeight = rowCount * TILE_SIZE;
        tiles = new TileButton[rowCount][columnCount];
        revealedTilesCount = 0;
        isGameOver = false;

        setupBoardPanel(boardWidth, boardHeight);
        setupStatusLabel();
        placeMines();
    }

    // Starts the game timer
    private void startTimer() {
        timer = new Timer(1000, e -> updateTimer());
        timer.start();
    }

    // Stops the game timer
    private void stopTimer() {
        if (timer != null) {
            timer.stop();
        }
    }

    // Sets up the board panel with tiles
    private void setupBoardPanel(int width, int height) {
        boardPanel = new JPanel();
        boardPanel.setPreferredSize(new Dimension(width, height));
        boardPanel.setLayout(new GridLayout(rowCount, columnCount));
        createTiles();
    }

    // Sets up the status label
    private void setupStatusLabel() {
        statusLabel.setFont(new Font("Arial", Font.BOLD, 25));
        statusLabel.setHorizontalAlignment(JLabel.CENTER);
        statusLabel.setText("Minesweeper");
        statusLabel.setOpaque(true);

        statusPanel.setLayout(new BorderLayout());
        statusPanel.add(statusLabel, BorderLayout.CENTER);
        timerLabel.setFont(new Font("Arial", Font.BOLD, 20));
        statusPanel.add(timerLabel, BorderLayout.EAST);
        frame.add(statusPanel, BorderLayout.NORTH);
    }

    // Creates tiles for the board
    private void createTiles() {
        for (int r = 0; r < rowCount; r++) {
            for (int c = 0; c < columnCount; c++) {
                TileButton tile = new TileButton(r, c);
                tiles[r][c] = tile;
                tile.setPreferredSize(new Dimension(TILE_SIZE, TILE_SIZE));
                tile.addMouseListener(new TileMouseListener());
                boardPanel.add(tile);
            }
        }
    }

    // Places mines randomly on the board
    private void placeMines() {
        mines = new ArrayList<>();
        int minesLeft = mineCount;
        while (minesLeft > 0) {
            int r = random.nextInt(rowCount);
            int c = random.nextInt(columnCount);
            TileButton tile = tiles[r][c];
            if (!mines.contains(tile)) {
                mines.add(tile);
                minesLeft--;
            }
        }
    }

    // Updates the timer every second
    private void updateTimer() {
        elapsedTime++;
        timerLabel.setText("Time: " + elapsedTime);
    }

    // Inner class for tile buttons
    private class TileButton extends JButton {
        final int row;
        final int column;

        // Constructor for TileButton
        public TileButton(int row, int column) {
            this.row = row;
            this.column = column;
        }
    }

    // Inner class for handling mouse events on tiles
    private class TileMouseListener extends MouseAdapter {
        @Override
        public void mousePressed(MouseEvent e) {
            if (isGameOver) return;

            TileButton clickedTile = (TileButton) e.getSource();
            if (e.getButton() == MouseEvent.BUTTON1) {
                if (revealedTilesCount == 0) startTimer();
                handleLeftClick(clickedTile);
            } else if (e.getButton() == MouseEvent.BUTTON3) {
                handleRightClick(clickedTile);
            }
        }
    }

    // Handles left-click events on tiles
    private void handleLeftClick(TileButton tile) {
        if (tile.getText().isEmpty()) {
            if (mines.contains(tile)) {
                revealMines();
            } else {
                checkTile(tile.row, tile.column);
            }
        }
    }

    // Handles right-click events on tiles
    private void handleRightClick(TileButton tile) {
        if (tile.getText().isEmpty() && tile.isEnabled()) {
            tile.setText("🚩");
        } else if (tile.getText().equals("🚩")) {
            tile.setText("");
        }
    }

    // Reveals all mines and ends the game
    private void revealMines() {
        for (TileButton mine : mines) {
            mine.setText("💣");
        }
        isGameOver = true;
        statusLabel.setText("Game Over!");
        stopTimer();
    }

    // Checks a tile for mines or empty space
    private void checkTile(int row, int column) {
        if (!isValidPosition(row, column)) return;

        TileButton tile = tiles[row][column];
        if (!tile.isEnabled()) return;

        tile.setEnabled(false);
        revealedTilesCount++;

        int adjacentMines = countAdjacentMines(row, column);
        if (adjacentMines > 0) {
            tile.setText(Integer.toString(adjacentMines));
        } else {
            tile.setText("");
            revealAdjacentTiles(row, column);
        }

        checkWinCondition();
    }

    // Checks if the given position is within board bounds
    private boolean isValidPosition(int row, int column) {
        return row >= 0 && row < rowCount && column >= 0 && column < columnCount;
    }

    // Counts the number of adjacent mines around a given tile
    private int countAdjacentMines(int row, int column) {
        int mineCount = 0;
        for (int r = row - 1; r <= row + 1; r++) {
            for (int c = column - 1; c <= column + 1; c++) {
                if (isValidPosition(r, c) && mines.contains(tiles[r][c])) {
                    mineCount++;
                }
            }
        }
        return mineCount;
    }

    // Reveals adjacent tiles when an empty tile is clicked
    private void revealAdjacentTiles(int row, int column) {
        for (int r = row - 1; r <= row + 1; r++) {
            for (int c = column - 1; c <= column + 1; c++) {
                if (isValidPosition(r, c)) {
                    checkTile(r, c);
                }
            }
        }
    }

    // Checks if all non-mine tiles are revealed to determine if the player wins
    private void checkWinCondition() {
        if (revealedTilesCount == rowCount * columnCount - mines.size()) {
            isGameOver = true;
            statusLabel.setText("Mines Cleared!");
            stopTimer();
        }
    }

    // Main method
    public static void main(String[] args) {
         SwingUtilities.invokeLater(() -> new Minesweeper());
    }
}
