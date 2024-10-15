import javax.swing.*;
import java.util.*;
import java.io.*;

public class Tetris {
    // Constants and configuration parameters
    public static int userSpeed =2; //determines how fast the pieces will fall for the user

    public static int userSpeedBoost = 1;

    public static int botSpeed = 1; //determines how fast the pieces will fall

    public static int horizontalGridSize = 10;
    public static int verticalGridSize = 18;
    public static String gridXSize = (horizontalGridSize + "x" + verticalGridSize);
    public static ArrayList<Integer> userPentAr = new ArrayList<>();
    public static ArrayList<Integer> prev2GeneratedPiecesUser = new ArrayList<>(); //this arraylist will hold the two previously randomly generated pentominos, which will help us prevent any repeated pentominos within 2 pentominos in the list

    public static int[][] field = new int[horizontalGridSize][verticalGridSize];  // Game grid

    public static int[][] fieldInit = new int[horizontalGridSize][verticalGridSize];


    public static TetrisUI ui;  // User interface object to display grid

    public static int userLinesCleared = 0;

    public static int userLevel = 0;
    public static int userScore = 0;
    public static boolean userMovingRight = false;
    public static boolean userMovingLeft = false;

    public static boolean userRotating = false;

    public static boolean isHardDropping = false;

    public static boolean isHolding = false;

    public static boolean gamePaused = false;

    public static boolean pressedEscape = false;

    public static int userHeldPiece = -1;

    public static int userPlacedPieces = 0;

    public static int userPlacedPiecesSnapshot = -1;

    public static long rightPieceCooldownStart = 0;
    public static long leftPieceCooldownStart = 0;
    public static long rotatingCooldownStart = 0;

    public Tetris(){
        ui = new TetrisUI(horizontalGridSize, verticalGridSize, 35, this); //including 'this' allows the TetrisUI class to access the methods and variables of this class.
    }

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
    public static void userGame() {


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

        boolean gameOver = false;

        while(!gameOver){

            Random randomPentGen = new Random();
            int upperbound = 12; // The upperbound for the random number generator is set at 12 so it will generate one of the 12 pentominos


            for(int i = userPentAr.size(); i < 4; i++){ //Always have 4 input pentominos ready at the queue, 1 which will be placed in the grid and 3 others to display in the 'next pieces' section in the UI

                int randomPent = randomPentGen.nextInt(upperbound);
                while(prev2GeneratedPiecesUser.contains(randomPent)){
                    randomPent = randomPentGen.nextInt(upperbound); //generates another random pentomino if the one current one was already selected 1 or 2 pieces before
                }
                userPentAr.add(randomPent);
                if(prev2GeneratedPiecesUser.size() > 2){
                    prev2GeneratedPiecesUser.remove(0);
                }
                prev2GeneratedPiecesUser.add(randomPent);
            }


            int currentLine = 0;
            int currentColumn = 0;
            int mutation = 0;
            int clockCount = 1;
            int currentClockCount = 1;
            long placePieceCooldown = 0;

            isHardDropping = false;

            field = removeClearedLines(field);
            ui.setState(field);




            int pentID = userPentAr.get(0); //fetches the ID of the given pentomino
            userPentAr.remove(0); // removes the pentomino at the front of the queue from the next pentominos ArrayList

            outer:
            for(int i = clockCount; i >-1; i++){ //infinite for loop, until it is broken when a piece fits into place


                //checks if it is possible to place the piece at the current line
                boolean possibleOnThisLine = canPlacePieceUser(currentColumn, currentLine, PentominoDatabase.data[pentID][mutation], pentID);

                //checks if it is possible to place the piece on the next line down. This allows us to determine when to stop moving the piece.
                boolean possibleOnNextLine = canPlacePieceUser(currentColumn, currentLine + 1, PentominoDatabase.data[pentID][mutation], pentID);



                if(i % 30 == 0 && i != 0){ //every 30 clock counts, this loop runs.

                    if(!possibleOnThisLine && currentLine == 0){
                        System.out.println("Game Over");
                        gameOver = true;
                        break;
                    }

                    if(possibleOnThisLine){

                        addPiece(currentColumn, currentLine, PentominoDatabase.data[pentID][mutation], pentID);
                        ui.setState(field);

                        //check if block can travel one more step down
                        if(possibleOnNextLine){
                            removePiece(currentColumn, currentLine, PentominoDatabase.data[pentID][mutation]);
                            currentLine++;
                        }
                        else{
                            if(i - currentClockCount > 30){
                                userPlacedPieces++;
                                break outer; //place the piece
                            }
                        }


                    }

                }

                if(!possibleOnNextLine && (System.currentTimeMillis() - placePieceCooldown > 1000) && (userRotating || userMovingLeft || userMovingRight || userSpeedBoost != 1)){
                //if not possible to move down 1 line more, but the user is moving the piece, start a timer which will let the user make adjustments to piece for a little bit longer
                    currentClockCount = i;
                    placePieceCooldown = System.currentTimeMillis();
                }

                if(possibleOnThisLine){ //updates the UI outside with every clock tick as opposed to only every time the piece changes lines
                    addPiece(currentColumn, currentLine, PentominoDatabase.data[pentID][mutation], pentID);
                    ui.setState(field);
                    removePiece(currentColumn, currentLine, PentominoDatabase.data[pentID][mutation]);
                }

                if(isHolding && (userPlacedPieces - userPlacedPiecesSnapshot > 0)){ //if the user pressed c and at least 1 piece has been placed since the last time the user held a piece
                    userPlacedPiecesSnapshot = userPlacedPieces;

                    if(userHeldPiece != -1){
                        userPentAr.add(0, userHeldPiece); //If a piece is currently on hold, the piece is placed at the front of the arrayList holding the pieces that are coming up
                    }
                    userHeldPiece = pentID; //current piece is stored in the userHeldPieceVariable

                    break outer;
                }

                boolean possibleToMoveRight = canPlacePieceUser(currentColumn + 1, currentLine, PentominoDatabase.data[pentID][mutation], pentID);
                if(userMovingRight && possibleToMoveRight && (System.currentTimeMillis() - rightPieceCooldownStart > 200)){
                    currentColumn++;
                    addPiece(currentColumn, currentLine, PentominoDatabase.data[pentID][mutation], pentID);
                    ui.setState(field);
                    removePiece(currentColumn, currentLine, PentominoDatabase.data[pentID][mutation]);
                    rightPieceCooldownStart = System.currentTimeMillis();
                }

                boolean possibleToMoveLeft = canPlacePieceUser(currentColumn - 1, currentLine, PentominoDatabase.data[pentID][mutation], pentID);
                if(userMovingLeft && possibleToMoveLeft && (System.currentTimeMillis() - leftPieceCooldownStart > 200)){
                    currentColumn--;
                    addPiece(currentColumn, currentLine, PentominoDatabase.data[pentID][mutation], pentID);
                    ui.setState(field);
                    removePiece(currentColumn, currentLine, PentominoDatabase.data[pentID][mutation]);
                    leftPieceCooldownStart = System.currentTimeMillis();
                }


                boolean possibleToRotate;
                boolean possibleToRotatePrevLine;
                boolean possibleToRotatePrevCol;
                boolean possibleToRotateNextCol;

                if(mutation <= 3){
                    possibleToRotate = canPlacePieceUser(currentColumn, currentLine, PentominoDatabase.data[pentID][mutation + 1], pentID);
                    possibleToRotatePrevLine = canPlacePieceUser(currentColumn, currentLine - 1, PentominoDatabase.data[pentID][mutation + 1], pentID);
                    possibleToRotatePrevCol = canPlacePieceUser(currentColumn - 1, currentLine, PentominoDatabase.data[pentID][mutation + 1], pentID);
                    possibleToRotateNextCol = canPlacePieceUser(currentColumn + 1, currentLine, PentominoDatabase.data[pentID][mutation + 1], pentID);

                }
                else{
                    possibleToRotate = canPlacePieceUser(currentColumn, currentLine, PentominoDatabase.data[pentID][0], pentID);
                    possibleToRotatePrevLine = canPlacePieceUser(currentColumn, currentLine - 1, PentominoDatabase.data[pentID][0], pentID);
                    possibleToRotatePrevCol = canPlacePieceUser(currentColumn - 1, currentLine, PentominoDatabase.data[pentID][0], pentID);
                    possibleToRotateNextCol = canPlacePieceUser(currentColumn + 1, currentLine, PentominoDatabase.data[pentID][0], pentID);
                }

                if(userRotating && (System.currentTimeMillis() - rotatingCooldownStart > 200)){

                    if(possibleToRotate){
                        int tempMutation = mutation;
                        if(mutation < 3){ //There are 4 different piece rotations. This if statement increases the mutation by 1, going to the next rotation, and returns it to the first mutation after it gets to the fourth rotation
                            mutation++;
                        }
                        else{
                            mutation = 0;
                        }
                        if(canPlacePieceUser(currentColumn, currentLine, PentominoDatabase.data[pentID][mutation], pentID)) {
                            addPiece(currentColumn, currentLine, PentominoDatabase.data[pentID][mutation], pentID);
                            ui.setState(field);
                            removePiece(currentColumn, currentLine, PentominoDatabase.data[pentID][mutation]);
                        }
                        else{
                            mutation = tempMutation;
                        }
                        rotatingCooldownStart = System.currentTimeMillis();
                    }
                    else{
                        if(possibleToRotatePrevLine){ //if it is not currently possible to rotate the piece but it was in the previous line
                            if(mutation < 3){
                                mutation++;
                            }
                            else{
                                mutation = 0;
                            }
                            currentLine --;
                            if(canPlacePieceUser(currentColumn, currentLine, PentominoDatabase.data[pentID][mutation], pentID)) {
                                addPiece(currentColumn, currentLine, PentominoDatabase.data[pentID][mutation], pentID);
                                ui.setState(field);
                                removePiece(currentColumn, currentLine, PentominoDatabase.data[pentID][mutation]);
                            }
                            else{
                                currentLine++;
                            }
                            rotatingCooldownStart = System.currentTimeMillis();
                        }
                        else if(possibleToRotatePrevCol){
                            if(mutation < 3){
                                mutation++;
                            }
                            else{
                                mutation = 0;
                            }
                            currentColumn --;
                            if(canPlacePieceUser(currentColumn, currentLine, PentominoDatabase.data[pentID][mutation], pentID)){
                                addPiece(currentColumn, currentLine, PentominoDatabase.data[pentID][mutation], pentID);
                                ui.setState(field);
                                removePiece(currentColumn, currentLine, PentominoDatabase.data[pentID][mutation]);
                            }
                            else{
                                currentColumn++;
                            }
                            rotatingCooldownStart = System.currentTimeMillis();
                        }
                        else if(possibleToRotateNextCol){
                            if(mutation < 3){
                                mutation++;
                            }
                            else{
                                mutation = 0;
                            }
                            currentColumn ++;
                            if(canPlacePieceUser(currentColumn, currentLine, PentominoDatabase.data[pentID][mutation], pentID)){
                                addPiece(currentColumn, currentLine, PentominoDatabase.data[pentID][mutation], pentID);
                                ui.setState(field);
                                removePiece(currentColumn, currentLine, PentominoDatabase.data[pentID][mutation]);
                            }
                            else{
                                currentColumn--;
                            }
                            rotatingCooldownStart = System.currentTimeMillis();
                        }
                        else if(userMovingRight && !possibleToRotate && pentID == 1 && (mutation == 0 || mutation == 2) && canPlacePieceUser(currentColumn + 2, currentLine, PentominoDatabase.data[pentID][1], pentID)){
                        //if it is possible to rotate the I piece two columns to the right and the user is pressing right, move it to that space and rotate it
                            mutation = 1;
                            currentColumn +=2;
                            addPiece(currentColumn, currentLine, PentominoDatabase.data[pentID][mutation], pentID);
                            ui.setState(field);
                            removePiece(currentColumn, currentLine, PentominoDatabase.data[pentID][mutation]);
                        }
                        else if(userMovingLeft && !possibleToRotate && pentID == 1 && (mutation == 0 || mutation == 2) && canPlacePieceUser(currentColumn - 2, currentLine, PentominoDatabase.data[pentID][1], pentID)){
                            //if it is possible to rotate the I piece two columns to the left, move it to that space and rotate it
                            mutation = 1;
                            currentColumn -=2;
                            addPiece(currentColumn, currentLine, PentominoDatabase.data[pentID][mutation], pentID);
                            ui.setState(field);
                            removePiece(currentColumn, currentLine, PentominoDatabase.data[pentID][mutation]);
                        }
                        else if(userMovingRight && !possibleToRotate && pentID == 1 && (mutation == 0 || mutation == 2) && canPlacePieceUser(currentColumn + 3, currentLine, PentominoDatabase.data[pentID][1], pentID)){
                            //if it is possible to rotate the I piece three columns to the right and the user is pressing right, move it to that space and rotate it
                            mutation = 1;
                            currentColumn +=3;
                            addPiece(currentColumn, currentLine, PentominoDatabase.data[pentID][mutation], pentID);
                            ui.setState(field);
                            removePiece(currentColumn, currentLine, PentominoDatabase.data[pentID][mutation]);
                        }
                        else if(userMovingLeft && !possibleToRotate && pentID == 1 && (mutation == 0 || mutation == 2) && canPlacePieceUser(currentColumn - 3, currentLine, PentominoDatabase.data[pentID][1], pentID)){
                            //if it is possible to rotate the I piece two columns to the left, move it to that space and rotate it
                            mutation = 1;
                            currentColumn -=3;
                            addPiece(currentColumn, currentLine, PentominoDatabase.data[pentID][mutation], pentID);
                            ui.setState(field);
                            removePiece(currentColumn, currentLine, PentominoDatabase.data[pentID][mutation]);
                        }
                    }
                }


                if(isHardDropping){

                    while(canPlacePieceUser(currentColumn, currentLine + 1, PentominoDatabase.data[pentID][mutation], pentID)){
                        currentLine++;
                    }
                    if(canPlacePieceUser(currentColumn, currentLine, PentominoDatabase.data[pentID][mutation], pentID)){
                        addPiece(currentColumn, currentLine, PentominoDatabase.data[pentID][mutation], pentID);
                        ui.setState(field);
                        userPlacedPieces++;
                        break outer;
                    }

                }


                while(gamePaused){
                    ui.setState(fieldInit); //While in the pause menu, display an empty grid like in the real tetris game so the user doesn't use it to cheat
                    try {
                        Thread.sleep(1000/(userSpeed * 30)/userSpeedBoost); //the speed at which the clockCount increases or the loop iterates is determined by the "speed" variable as well as the speedboost (when soft-dropping)
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }



                try {
                    Thread.sleep(1000/(userSpeed * 30)/userSpeedBoost); //the speed at which the clockCount increases or the loop iterates is determined by the "speed" variable as well as the speedboost (when soft-dropping)
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }





            }


        }

        
    }
    
    // Cell class to represent a specific cell in the grid
    public static class Cell {
        int x, y;
        Cell(int x, int y) {
            this.x = x;
            this.y = y;
        }
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

    public static int[][] removeClearedLines(int[][] field){
        ArrayList<Integer> arr = new ArrayList<>();

        int linesClearedInstance = 0;

        for(int i = field[0].length - 1; i >= 0; i--){ //iterate over rows, starting with the bottom row
            for(int j = 0; j < field.length; j++){ //iterate over columns
                arr.add(field[j][i]);
            }
        }

        for(int i = field[0].length - 1; i >= 0; i--){ //for every row
            boolean lineCleared = true;
            for(int j = 0; j < field.length; j++){
                if (arr.get((field.length * (field[0].length - 1 - i)) + j) < 0) { //if any of the blocks in the row is empty
                    lineCleared = false;
                    break;
                }
            }
            if(lineCleared){
                userLinesCleared++;
                linesClearedInstance++;

                for(int j = 0; j < field.length; j++){
                    arr.set((field.length * (field[0].length - 1 - i)) + j , 20); //sets all the spots to be removed to 20
                }
            }
        }

        if(userLinesCleared == 0){
            userLevel = 1;
        }
        else{
            userLevel = (int)(Math.log10(userLinesCleared) + 1);//Every 10 lines cleared, the level increases by 1
        }

        if(linesClearedInstance == 1){
            userScore += 500*userLevel*linesClearedInstance;
        }
        else if(linesClearedInstance == 2){
            userScore += 600*userLevel*linesClearedInstance;
        }
        else if(linesClearedInstance == 3){
            userScore += 700*userLevel*linesClearedInstance;
        }
        else if(linesClearedInstance == 4){
            userScore += 800*userLevel*linesClearedInstance;
        }
        else if(linesClearedInstance == 5){
            userScore += 1000*userLevel*linesClearedInstance;
        } //As more lines are cleared simultaneously, each line adds more points to the score.



        while(arr.contains(20)){
            arr.remove(Integer.valueOf(20)); //remove all pieces to be cleared from the arraylist
        }

        int[][] newField = new int[field.length][field[0].length];

        for(int i = newField[0].length - 1; i >= 0; i--){
            for(int j = 0; j < newField.length; j++){
                int currentIndex = ((field.length * (field[0].length - 1 - i)) + j);
                if(currentIndex < arr.size()){
                    if(arr.get(currentIndex)>=0){
                        newField[j][i] = arr.get(currentIndex);
                    }
                    else{
                        newField[j][i] = fieldInit[j][i];
                    }
                }
                else{
                    newField[j][i] = fieldInit[j][i];
                }

            }

        }

        return newField;
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

    private static boolean canPlacePieceUser(int x, int y, int[][] piece, int pentID) {
        // Loop through each cell of the piece and check if it fits
        for (int i = 0; i < piece.length; i++) {
            for (int j = 0; j < piece[i].length; j++) {
                if (piece[i][j] == 1) {
                    if (x + i < 0 || x + i >= horizontalGridSize || y + j < 0 || y + j >= verticalGridSize || field[x + i][y + j] >= 0) {
                        return false;  // Return false if the piece is out of bounds or overlaps with another piece
                    }
                }
            }
        }
        return true;  // Return true if the piece fits
    }


    // Remove a piece from the grid
    private static void removePiece(int x, int y, int[][] piece) {
        for (int i = 0; i < piece.length; i++) {
            for (int j = 0; j < piece[i].length; j++) {
                if (piece[i][j] == 1) {
                    field[x + i][y + j] = fieldInit[x+i][y+j];  // Set the cell to its corresponding value in fieldInit, indicating it's empty
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



        Tetris tetrisInstance = new Tetris(); //create an Instance of this class for the UI class to be able to access it

        // Check if the grid size is valid
        long startTime = System.currentTimeMillis();
        userGame();

        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter your initials (3 letters): ");
        String initials = scanner.nextLine().toUpperCase();


        while(true){ //loop ensures that formatting of initials is valid
            if(initials.length()==3){
                break;
            }
            else{
                System.out.println("Invalid initials. Please enter three letters: ");
                scanner = new Scanner(System.in);
                initials = scanner.nextLine().toUpperCase();

            }
        }


        System.out.println("Lines cleared: " + userLinesCleared);
        System.out.println("Level: " + userLevel);
        System.out.println("Score: " + userScore);
        System.out.println("Initials: " + initials);


        HiScore hiscore = new HiScore(userScore, initials);
        manageHiScores.updateHiScores(hiscore);


        List<HiScore> hiScores= manageHiScores.getHiScores();
        System.out.println("Hi-scores: ");
        for(int i = 0; i < hiScores.size(); i++){
            HiScore currentHiScore = hiScores.get(i);
            System.out.println(currentHiScore.convertToString());
        }


        long endTime = System.currentTimeMillis();
        long elapsedTime = endTime - startTime;
        ui.updateElapsedTimeLabel(elapsedTime);
        ui.gridSize(gridXSize);

        visitedStates.clear();  // Clear the memoization set for the next search
    }
    

    
}
