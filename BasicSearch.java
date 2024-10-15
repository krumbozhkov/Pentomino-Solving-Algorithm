/**
 * @author Department of Data Science and Knowledge Engineering (DKE)
 * @version 2022.0
 */

import java.util.*;

/**
 * This class includes the methods to support the search of a solution.
 */
public class BasicSearch
{
	//public static final int PentCount = getPentominoCount();
	public static final int PentCount = 12;

    //public static final int horizontalGridSize = getWidthfromInput();
	public static final int horizontalGridSize = 5;

	//public static final int verticalGridSize = ((PentCount * 5)/horizontalGridSize);
	//calculates what the height of the
	//grid should be based on the area of the amount of pentominos the user has chosen
	public static final int verticalGridSize = 6;


	//public static final char[] input = getInputArray();
    public static final char[] input = { 'W', 'Y', 'I', 'T', 'Z', 'L'}; //what pentominoes to use

    //Static UI class to display the board
    public static UI ui = new UI(horizontalGridSize, verticalGridSize, 50);

	/**
	 * Helper function which starts a basic search algorithm
	 */
    public static void search()
    {
        // Initialize an empty board
        int[][] field = new int[horizontalGridSize][verticalGridSize]; //grid for pentominoes to go in

        for(int i = 0; i < field.length; i++) //loops through the width of the grid
        {
            for(int j = 0; j < field[i].length; j++) //loops through the height of the grid
            {
                // -1 in the state matrix corresponds to empty square
                // Any positive number identifies the ID of the pentomino
            	field[i][j] = -1; //erases that space
            }
        }
        //Start the basic search
        basicSearch(field);
    }
	
	/**
	 * Get as input the character representation of a pentomino and translate it into its corresponding numerical value (ID)
	 * @param character a character representating a pentomino
	 * @return	the corresponding ID (numerical value)
	 */
    private static int characterToID(char character) {
    	int pentID = -1; 
    	if (character == 'X') {
    		pentID = 0;
    	} else if (character == 'I') {
    		pentID = 1;
    	} else if (character == 'Z') {
    		pentID = 2;
    	} else if (character == 'T') {
    		pentID = 3;
    	} else if (character == 'U') {
    		pentID = 4;
     	} else if (character == 'V') {
     		pentID = 5;
     	} else if (character == 'W') {
     		pentID = 6;
     	} else if (character == 'Y') {
     		pentID = 7;
    	} else if (character == 'L') {
    		pentID = 8;
    	} else if (character == 'P') {
    		pentID = 9;
    	} else if (character == 'N') {
    		pentID = 10;
    	} else if (character == 'F') {
    		pentID = 11;
    	} 
    	return pentID;
    }
	
	/**
	 * Basic implementation of a search algorithm. It is not a brute force algorithms (it does not check all the posssible combinations)
	 * but randomly takes possible combinations and positions to find a possible solution.
	 * The solution is not necessarily the most efficient one
	 * This algorithm can be very time-consuming
	 * @param field a matrix representing the board to be fulfilled with pentominoes
	 */
    private static void basicSearch(int[][] field){
    	Random random = new Random();
    	boolean solutionFound = false;
    	int count = 0;


		while (!solutionFound) {
    		//Empty board again to find a solution
			for (int i = 0; i < field.length; i++) {
				for (int j = 0; j < field[i].length; j++) {
					field[i][j] = -1;
				}
			}
    		
    		//Put all pentominoes with random rotation/flipping on a random position on the board
    		for (int i = 0; i < input.length; i++) {
    			
    			//Choose a pentomino and randomly rotate/flip it
    			int pentID = characterToID(input[i]); //fetches the ID of the given pentomino
    			int mutation = random.nextInt(PentominoDatabase.data[pentID].length);
    			int[][] pieceToPlace = PentominoDatabase.data[pentID][mutation]; //fetches a particular rotation for the piece
				//by searching in the csv file
    		
    			//Randomly generate a position to put the pentomino on the board
    			int x;
    			int y;
				boolean Overlaps=true;
				int count1 = 0;
				while(Overlaps && count1 < 35){ //this loop will iterate a max of 35 times (kept track of by
					// variable count1), as there may not be any valid non-overlapping solutions which would
					//turn this into an infinite loop
					x = random.nextInt(horizontalGridSize-pieceToPlace.length+1);
					y = random.nextInt(verticalGridSize-pieceToPlace[0].length+1);

					int overlapCount = 0; // keeps track of how many if the pieces blocks are to be put on top of an
					//occupied space
					for(int j = 0; j < pieceToPlace.length; j++) // loop over x position of pentomino
					{
						for (int k = 0; k < pieceToPlace[j].length; k++) // loop over y position of pentomino
						{
							if (pieceToPlace[j][k] == 1)
							{
								// Add the ID of the pentomino to the board if the pentomino occupies this square
								if (field[x + j][y + k] != -1){ //if space on the grid is already taken
									overlapCount++;
								}
							}
						}
					}

					if (overlapCount == 0){
						Overlaps = false;
						addPiece(field, pieceToPlace, pentID, x, y);

					}


					count1++;

				}



				/*
    			if (horizontalGridSize < pieceToPlace.length) {
    				//this particular rotation of the piece is too long for the field
    				x=-1;
    			} else if (horizontalGridSize == pieceToPlace.length) {
    				//this particular rotation of the piece fits perfectly into the width of the field
    				x = 0;
    			} else {
    				//there are multiple possibilities where to place the piece without leaving the field
    				x = random.nextInt(horizontalGridSize-pieceToPlace.length+1);
    			}

    			if (verticalGridSize < pieceToPlace[0].length) {
    				//this particular rotation of the piece is too high for the field
    				y=-1;
    			} else if (verticalGridSize == pieceToPlace[0].length) {
    				//this particular rotation of the piece fits perfectly into the height of the field
    				y = 0;
    			} else {
    				//there are multiple possibilities where to place the piece without leaving the field
    				y = random.nextInt(verticalGridSize-pieceToPlace[0].length+1);
    			}

    		
    			//If there is a possibility to place the piece on the field, do it
    			if (x >= 0 && y >= 0) {
	    			addPiece(field, pieceToPlace, pentID, x, y);
	    		}
				*/
    		}

			boolean CheckWhetherFull = true;
			outer:
			for(int i = 0; i < field.length; i++) //loops through the width of the grid
			{
				for(int j = 0; j < field[i].length; j++) //loops through the height of the grid
				{
					if (field[i][j] == -1){
						CheckWhetherFull = false;
						break outer;
					}
				}
			}

			ui.setState(field); //shows the solutions as the computer is coming up with them, which
			//gives the UI a nice 'loading' effect

			count++;

			if (CheckWhetherFull){
				solutionFound = true;
				System.out.println("Solution found");
				System.out.println("Tried " + count + " combinations");
			}


		}



    }

    
	/**
	 * Adds a pentomino to the position on the field (overriding current board at that position)
	 * @param field a matrix representing the board to be fulfilled with pentominoes
	 * @param piece a matrix representing the pentomino to be placed in the board
	 * @param pieceID ID of the relevant pentomino
	 * @param x x position of the pentomino
	 * @param y y position of the pentomino
	 */
    public static void addPiece(int[][] field, int[][] piece, int pieceID, int x, int y)
    {
        for(int i = 0; i < piece.length; i++) // loop over x position of pentomino
        {
            for (int j = 0; j < piece[i].length; j++) // loop over y position of pentomino
            {
                if (piece[i][j] == 1)
                {
                    // Add the ID of the pentomino to the board if the pentomino occupies this square
                    field[x + i][y + j] = pieceID;
                }
            }
        }
    }

	/**
	 * Main function. Needs to be executed to start the basic search algorithm
	 */
    public static void main(String[] args)
    {
		//System.out.print(horizontalGridSize);

		System.out.println("Number of pentominos: " + PentCount);
		System.out.println("Pentominos used: " + Arrays.toString(input));
		System.out.println("Grid dimensions: " + horizontalGridSize + "x" + verticalGridSize);

        search();
    }

	public static int getPentominoCount(){

		boolean valid = false;
		Scanner scanner = new Scanner(System.in);
		System.out.println("Enter a number of pentominos, from 3 to 12: ");

		while(!valid){

			try {
				int pentCount = scanner.nextInt();

				if(pentCount >= 3 && pentCount <= 12){
					valid = true;
					return pentCount;
				}
				else{
					System.out.println("Out of bounds. Please enter an integer between 3 and 12: ");
					scanner = new Scanner(System.in);
				}



			} catch (InputMismatchException e) {
				System.out.println("Invalid input. Please enter an integer between 3 and 12: ");
				scanner = new Scanner(System.in);
			}

		}
		return 0;

	}

	public static int getWidthfromInput(){



		boolean valid = false;
		Scanner scanner = new Scanner(System.in);
		System.out.println("Enter a width value for the grid: (Integer between 4 and 15, bounds can change depending on number of pentominos)");

		while(!valid){

			try {
				int width = scanner.nextInt();
				int requiredArea = PentCount * 5;

				if(width >= 4 && width <= 12 && (requiredArea % width) == 0){ //if width is within bounds and area of pentominos is divisible by grid
					valid = true;
					return width;
				} else if ((requiredArea % width) != 0) {
					System.out.println("That width value is incompatible with the number of pentominos" +
							"\nyou selected, please enter a multiple of " + requiredArea + ": ");
					scanner = new Scanner(System.in);
				} else{
					System.out.println("Out of bounds. Please try again: ");
					scanner = new Scanner(System.in);
				}



			} catch (InputMismatchException e) {
				System.out.println("Invalid input. Please enter an integer between 4 and 15: ");
				scanner = new Scanner(System.in);
        }

		}
        return 0;
    }

	public static char[] getInputArray(){

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

		Stack<Character> inputStack = new Stack<Character>();

		Scanner scanner = new Scanner(System.in);
		System.out.println("Please enter the character for the " + PentCount + " pentominos you wish" +
				" to use (You can only use each once)");

		while(inputStack.size()<PentCount){
			System.out.println("Available pentominos: " + availablePents);
			char userInput = scanner.next().toUpperCase().charAt(0); //gets the first character of the input
			if(availablePents.contains(userInput)){
				availablePents.remove(availablePents.indexOf(userInput));
				inputStack.push(userInput);
			}
			else{
				System.out.println("That pentomino is not available or invalid.");
			}
		}

		char[] inputArray = new char[PentCount];

		for (int i = 0; i < PentCount; i++){
			inputArray[i] = inputStack.get(i);
		}

		return inputArray;




	}


}