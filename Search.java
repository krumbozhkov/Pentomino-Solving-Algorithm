import javax.swing.*;
import java.util.*;

public class Search {
    // Constants and configuration parameters
    public static final int PentCount = 12;
    public static int horizontalGridSize = 11;
    public static int verticalGridSize = 5;
    public static String gridXSize = (horizontalGridSize + "x" + verticalGridSize);
    public static int CombCount = 0;  // To count how many combinations were tried
    public static final char[] input = { 'T', 'I', 'L', 'W', 'Z', 'P', 'U', 'N', 'V', 'Y', 'X', 'F' };
    public static int[][] field = new int[horizontalGridSize][verticalGridSize];  // Game grid

    public static int[][] fieldInit = new int[horizontalGridSize][verticalGridSize];

    public static boolean[] pentominoUsed = new boolean[12];  // To keep track of used pentominos
    public static UI ui;  // User interface object to display grid

    // To memoize already tried states
    private static Set<String> visitedStates = new HashSet<>();

    // Mapping of characters to their index
    private static Map<Character, Integer> charToIdMap = new HashMap<>() {
        {
            put('X', 0);
            put('I', 1);
            put('Z', 2);
            put('T', 3);
            put('U', 4);
            put('V', 5);
            put('W', 6);
            put('Y', 7);
            put('L', 8);
            put('P', 9);
            put('N', 10);
            put('F', 11);
            // ... [rest of the mappings]
        }
    };

    // Function to convert a character to its ID (or index)
    private static int characterToID(char character) {
        return charToIdMap.getOrDefault(character, -1);
    }

    // Main function to initiate the search for the solution
    public static void search() {

        for (int i = 0; i < fieldInit.length; i++) {
            for (int j = 0; j < fieldInit[i].length; j++) {

                if((i==1 || i == fieldInit.length - 2) && (j==0 || j == fieldInit[i].length - 1)){
                    fieldInit[i][j] = -3;
                }
                else if((i==0 || i == fieldInit.length - 1) && (j==1 || j == fieldInit[i].length - 2)){
                    fieldInit[i][j] = -3;
                }
                else if(i==0 || i == fieldInit.length - 1){
                    fieldInit[i][j] = -2;
                }
                else if (j==0 || j == fieldInit[i].length - 1){
                    fieldInit[i][j] = -2;
                }
                else {
                    fieldInit[i][j] = -1;
                }
            }
        }



        // Initialize the grid with -1 or -2 (edge pieces) or -3 (edge pieces touching corners), indicating empty cells
        for (int i = 0; i < field.length; i++) {
            for (int j = 0; j < field[i].length; j++) {
                field[i][j] = fieldInit[i][j];
            }
        }
        // Randomize the order of pentominoes to diversify the search
        List<Integer> pentominoOrderList = new ArrayList<>(PentCount);
        for (int i = 0; i < PentCount; i++) {
            pentominoOrderList.add(characterToID(input[i]));
        }
        Collections.shuffle(pentominoOrderList);
        Integer[] pentominoOrder = pentominoOrderList.toArray(new Integer[PentCount]);
        
        // Start solving from the top-left corner of the grid
        if (solve(0, 0, pentominoOrder)) {
            // If a solution is found, display it
            ui.setState(field);
            System.out.println("Solution found");
            System.out.println("Recursive calls made: " + CombCount);
        } else {
            System.out.println("No solution found");
        }

        // Use heuristic to reorder pentominoes for next search attempt
        List<Integer> pentPriority = new ArrayList<>();
        for (int i = 0; i < PentCount; i++) {
            if (!pentominoUsed[i]) {
                pentPriority.add(i);
            }
        }
        pentPriority.sort(Comparator.comparingInt(Search::countValidPositions));
    }

    // Count how many positions a given pentomino can be placed in the current grid
    private static int countValidPositions(int pentID) {
        int count = 0;
        for (int mutation = 0; mutation < PentominoDatabase.data[pentID].length; mutation++) {
            for (int i = 0; i < field.length; i++) {
                for (int j = 0; j < field[i].length; j++) {
                    if (canPlacePiece(i, j, PentominoDatabase.data[pentID][mutation], pentID)) {
                        count++;
                    }
                }
            }
        }
        return count;
    }

    // Cell class to represent a specific cell in the grid
    public static class Cell {
        int x, y;
        Cell(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }

    // Dynamic priority order based on the number of empty cells in each quadrant
    private static int[] getDynamicPriorityOrder() {
        int[] quadrantEmptyCells = new int[4];
        int midX = horizontalGridSize / 2;
        int midY = verticalGridSize / 2;
        for (int j = 0; j < verticalGridSize; j++) {
            for (int i = 0; i < horizontalGridSize; i++) {
                if (field[i][j] < 0) {
                    // Count how many cells are empty in each quadrant
                    if (i < midX && j < midY)
                        quadrantEmptyCells[0]++;
                    else if (i >= midX && j < midY)
                        quadrantEmptyCells[1]++;
                    else if (i < midX && j >= midY)
                        quadrantEmptyCells[2]++;
                    else if (i >= midX && j >= midY)
                        quadrantEmptyCells[3]++;
                }
            }
        }

        // Priority patterns for each quadrant
        int[][] priorityPatterns = {
            { 0, 1, 2, 3 },
            { 1, 0, 3, 2 },
            { 2, 3, 0, 1 },
            { 3, 2, 1, 0 },
        };

        // Get the quadrant with the highest priority (most empty cells)
        int maxQuadrant = 0;
        for (int i = 1; i < 4; i++) {
            if (quadrantEmptyCells[i] > quadrantEmptyCells[maxQuadrant]) {
                maxQuadrant = i;
            }
        }

        // Return the priority pattern for the quadrant with the most empty cells
        int[] baseOrder = priorityPatterns[maxQuadrant];
        int[] pentominoOrder = new int[PentCount];
        for (int i = 0; i < PentCount; i++) {
            pentominoOrder[i] = baseOrder[i % 4] * 3 + i / 4;
        }
        return pentominoOrder;
    }

    // Main recursive function to try placing pentominoes in the grid
    private static boolean solve(int x, int y, Integer[] pentominoOrder) {
        CombCount++;
        
        // Find the most difficult cell to place a pentomino
        Cell next = findMostDifficultCell();
        x = next.x;
        y = next.y;
        if (isLandLocked()) {
            return false; // Return false if there's an isolated cell
        }
        if (x == -1 && y == -1) {
            return isGridFilled();
        }
        if (field[x][y] >- 0) {
            return solve(x + 1, y, pentominoOrder);  // Move to the next cell if this one is filled
        }
        for (int i : getDynamicPriorityOrder()) {
            // Try each pentomino in order
            int pentID = pentominoOrder[i];
            if (!pentominoUsed[pentID]) {
                // Try each orientation of the pentomino
                for (int mutation = 0; mutation < PentominoDatabase.data[pentID].length; mutation++) {
                    int[][] pieceToPlace = PentominoDatabase.data[pentID][mutation];
                    if (canPlacePiece(x, y, pieceToPlace, pentID)) {
                        // Place the pentomino and move to the next cell
                        addPiece(x, y, pieceToPlace, pentID);
                        pentominoUsed[pentID] = true;
                        ui.setState(field); // Update the UI
                        if (!isLandLocked() && solve(x + 1, y, pentominoOrder)) {
                            return true;
                        }
                        // If we reached here, the placement didn't lead to a solution
                        // So, backtrack by removing the pentomino
                        removePiece(x, y, pieceToPlace);
                        pentominoUsed[pentID] = false;
                    }
                }
            }
        }

        // Memoize the current state so we don't re-check it later
        String state = Arrays.deepToString(field) + Arrays.toString(pentominoUsed);
        if (visitedStates.contains(state)) {
            return false; // Skip this state if we've seen it before
        }
        visitedStates.add(state);

        // If we've tried all possible pentomino placements for this cell, return false
        return false;
    }

    // Heuristic to find the cell that is hardest to fill
    private static Cell findMostDifficultCell() {
        int maxDifficultyScore = -1;
        Cell mostDifficult = new Cell(-1, -1);

        // Loop through each cell and find the one with the highest difficulty score
        for (int i = 0; i < horizontalGridSize; i++) {
            for (int j = 0; j < verticalGridSize; j++) {
                if (field[i][j] < 0) {
                    int currentScore = calculateCellDifficultyScore(i, j);
                    if (currentScore > maxDifficultyScore) {
                        maxDifficultyScore = currentScore;
                        mostDifficult.x = i;
                        mostDifficult.y = j;
                        if (maxDifficultyScore == 4) {
                            // If a cell is surrounded by walls, it's the hardest to fill
                            return mostDifficult;
                        }
                    }
                }
            }
        }

        return mostDifficult;
    }

    // Heuristic to score how difficult a cell is to fill
    private static int calculateCellDifficultyScore(int x, int y) {
        int score = 0;

        // Add to the score based on how close the cell is to the edges
        score += x;
        score += y;
        score += (horizontalGridSize - 1 - x);
        score += (verticalGridSize - 1 - y);

        // Subtract from the score based on how many neighbors are empty
        if (x > 0 && field[x - 1][y] < 0)
            score--;
        if (x < horizontalGridSize - 1 && field[x + 1][y] < 0)
            score--;
        if (y > 0 && field[x][y - 1] < 0)
            score--;
        if (y < verticalGridSize - 1 && field[x][y + 1] < 0)
            score--;

        return score;
    }

    // Check if a given piece can be placed at a given cell
    private static boolean canPlacePiece(int x, int y, int[][] piece, int pentID) {
        // If the piece is the "X" shape and it's being placed in a corner, return false
        if (isXShape(piece)) {
            if ((x == 0 && y == 0) || (x == 0 && y == verticalGridSize - piece[0].length) ||
                (x == horizontalGridSize - piece.length && y == 0) ||
                (x == horizontalGridSize - piece.length && y == verticalGridSize - piece[0].length)) {
                return false;
            }
        }

        // Loop through each cell of the piece and check if it fits
        for (int i = 0; i < piece.length; i++) {
            for (int j = 0; j < piece[i].length; j++) {
                if (piece[i][j] == 1) {
                    if (x + i < 0 || x + i >= horizontalGridSize || y + j < 0 || y + j >= verticalGridSize || field[x + i][y + j] >= 0) {
                        return false;  // Return false if the piece is out of bounds or overlaps with another piece
                    }
                    if (piece[i][j] == 1 && (pentID == 1) && (field[x + i][y + j] >-2)){
                        return false; //I piece can only go on the edges
                    }
                    if (piece[i][j] == 1 && (pentID == 0) && (field[x + i][y + j] ==-3)){
                        return false; //X piece cannot lock out the corner
                    }
                }
            }
        }
        return true;  // Return true if the piece fits
    }

    // Check if a given piece has the "X" shape
    private static boolean isXShape(int[][] piece) {
        return piece.length == 3 && Arrays.equals(piece[0], new int[]{0, 1, 0}) && Arrays.equals(piece[1], new int[]{1, 1, 1}) && Arrays.equals(piece[2], new int[]{0, 1, 0});
    }

    // Check if there are any cells that are impossible to fill
    private static boolean isLandLocked() {
        for (int i = 0; i < field.length; i++) {
            for (int j = 0; j < field[i].length; j++) {
                if (field[i][j] < 0) {
                    // Check for single-cell landlocked spots
                    boolean check = true;
                    if (i - 1 > 0 && field[i - 1][j] < 0)
                        check = false;
                    if (i + 1 < field.length && field[i + 1][j] < 0)
                        check = false;
                    if (j - 1 > 0 && field[i][j - 1] < 0)
                        check = false;
                    if (j + 1 < field[i].length && field[i][j + 1] < 0)
                        check = false;
                    if (check)
                        return true;

                    // Add more checks for other shapes that can't be filled
                }
            }
        }
        return false;  // Return false if there are no landlocked spots
    }

    // Add a piece to the grid
    private static void addPiece(int x, int y, int[][] piece, int pieceID) {
        for (int i = 0; i < piece.length; i++) {
            for (int j = 0; j < piece[i].length; j++) {
                if (piece[i][j] == 1) {
                    field[x + i][y + j] = pieceID;  // Set the cell to the ID of the piece
                }
            }
        }
    }

    // Remove a piece from the grid
    private static void removePiece(int x, int y, int[][] piece) {
        for (int i = 0; i < piece.length; i++) {
            for (int j = 0; j < piece[i].length; j++) {
                if (piece[i][j] == 1) {
                    field[x + i][y + j] = fieldInit[x+i][y+j];  // Set the cell to -1, indicating it's empty
                }
            }
        }
    }

    // Check if every cell in the grid is filled
    private static boolean isGridFilled() {
        for (int i = 0; i < field.length; i++) {
            for (int j = 0; j < field[i].length; j++) {
                if (field[i][j] <= -1) {
                    return false;  // Return false if there's an empty cell
                }
            }
        }
        return true;  // Return true if every cell is filled
    }

    // Main function to start the program
    public static void main(String[] args) {
        // Check if the grid size is valid
        if (horizontalGridSize * verticalGridSize % 5 == 0 && horizontalGridSize * verticalGridSize <= 60) {
            if (verticalGridSize * horizontalGridSize == 10) {
                System.out.println("Impossible to solve a 2 x 5 grid.");  // Special case for a 2x5 grid
            } else {
                // Create the UI and start the search
                ui = new UI(horizontalGridSize, verticalGridSize, 50);
                long startTime = System.currentTimeMillis();
                search();
                long endTime = System.currentTimeMillis();
                long elapsedTime = endTime - startTime;
                ui.updateElapsedTimeLabel(elapsedTime);
                ui.gridSize(gridXSize);
                System.out.println("That took " + elapsedTime + " milliseconds");
                System.out.println("Number of pentominos: " + PentCount);
                System.out.println("Pentominos used: " + Arrays.toString(input));
                System.out.println("Grid dimensions: " + horizontalGridSize + "x" + verticalGridSize);
            }
        } else {
            System.out.println("Incorrect grid size.");
        }

        visitedStates.clear();  // Clear the memoization set for the next search
    }

    // Get the pentominos from user input
    public static char[] getInputArray() {
        Stack<Character> availablePents = new Stack<Character>();
        availablePents.push('X');
        availablePents.push('I');
        availablePents.push('Z');
        availablePents.push('T');
        availablePents.push('U');
        availablePents.push('V');
        availablePents.push('W');
        availablePents.push('Y');
        availablePents.push('L');
        availablePents.push('P');
        availablePents.push('N');
        availablePents.push('F');
        // ... [rest of the initialization]
        Stack<Character> inputStack = new Stack<Character>();

        // Prompt the user to enter the pentominos they want to use
        Scanner scanner = new Scanner(System.in);
        System.out.println("Please enter the character for the " + PentCount + " pentominos you wish to use (You can only use each once)");

        while (inputStack.size() < PentCount) {
            System.out.println("Available pentominos: " + availablePents);
            char userInput = scanner.next().toUpperCase().charAt(0);
            if (availablePents.contains(userInput)) {
                availablePents.remove(availablePents.indexOf(userInput));
                inputStack.push(userInput);
            } else {
                System.out.println("That pentomino is not available or invalid.");
            }
        }

        // Convert the input stack to an array and return it
        char[] inputArray = new char[PentCount];
        for (int i = 0; i < PentCount; i++) {
            inputArray[i] = inputStack.get(i);
        }
        return inputArray;
    }

    // Get the number of pentominos from user input
    public static int getPentominoCount() {
        boolean valid = false;
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter a number of pentominos, from 3 to 12: ");

        while (!valid) {
            try {
                int pentCount = scanner.nextInt();
                if (pentCount >= 3 && pentCount <= 12) {
                    valid = true;
                    return pentCount;
                } else {
                    System.out.println("Out of bounds. Please enter an integer between 3 and 12: ");
                }
            } catch (InputMismatchException e) {
                System.out.println("Invalid input. Please enter an integer between 3 and 12: ");
            }
        }
        return 0;
    }

    // Get the width of the grid from user input
    public static int getWidthfromInput() {
        boolean valid = false;
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter a width value for the grid: (Integer between 4 and 15)");

        while (!valid) {
            try {
                int width = scanner.nextInt();
                int requiredArea = PentCount * 5;
                if (width >= 4 && width <= 12 && (requiredArea % width) == 0) {
                    valid = true;
                    return width;
                } else {
                    System.out.println("Out of bounds. Please try again: ");
                }
            } catch (InputMismatchException e) {
                System.out.println("Invalid input. Please try again: ");
            }
        }
        return 0;
    }

    // Get the height of the grid from user input
    public static int getHeightFromInput() {
        int requiredArea = PentCount * 5;
        return requiredArea / horizontalGridSize;
    }
}
