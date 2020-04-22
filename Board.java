// Board.java

import java.util.Arrays;

/**
 CS108 Tetris Board.
 Represents a Tetris board -- essentially a 2-d grid
 of booleans. Supports tetris pieces and row clearing.
 Has an "undo" feature that allows clients to add and remove pieces efficiently.
 Does not do any drawing or have any idea of pixels. Instead,
 just represents the abstract 2-d board.
*/
public class Board	{
	// Some ivars are stubbed out for you:
	private int width;
	private int height;
	private boolean[][] grid;
	private boolean DEBUG = true;
	boolean committed;

	private int[] colHeight;
	private int[] rowLength;

	private boolean[][] gridICloud;
	private int[] colHeightICloud;
	private int[] rowLengthICloud;

	private int maxHeight;
	private int maxHeightCloud;

	// Here a few trivial methods are provided:

	/**
	 Creates an empty board of the given width and height
	 measured in blocks.
	*/
	public Board(int width, int height) {
		this.width = width;
		this.height = height;
		grid = new boolean[width][height];
		committed = true;
		colHeight = new int[width];
		rowLength = new int[height];
		maxHeight = 0;

		gridICloud = new boolean[width][height];
		colHeightICloud = new int[width];
		rowLengthICloud = new int[height];

		maxHeightCloud = 0;
	}


	/**
	 Returns the width of the board in blocks.
	*/
	public int getWidth() {
		return width;
	}


	/**
	 Returns the height of the board in blocks.
	*/
	public int getHeight() {
		return height;
	}


	/**
	 Returns the max column height present in the board.
	 For an empty board this is 0.
	*/
	public int getMaxHeight() {
		return maxHeight;
	}


	/**
	 Checks the board for internal consistency -- used
	 for debugging.
	*/
	public void sanityCheck() {
		if (DEBUG) {
			int tmpMaxHeight =0;
			int[] tmpHeigths = new int[getWidth()];
			int[] tmpWidths = new int[getHeight()];
			for(int i=0; i<height ; i++) {
				tmpWidths[i] = 0;
			}
			for(int i=0; i<width ; i++){
				tmpHeigths[i] = 0;
			}

			for(int i=0; i<width ; i++) {
				for(int k=0; k<height; k++) {
					if(getGrid(i, k)){
						tmpWidths[k]++;
						tmpHeigths[i]=k+1;
					}
				}
				if(tmpHeigths[i]>tmpMaxHeight)tmpMaxHeight=tmpHeigths[i];
			}
			if(tmpMaxHeight!=maxHeight){
				throw new RuntimeException("maxHeight incorrect");
			}
			if(!Arrays.equals(tmpHeigths,colHeight)){
				throw new RuntimeException("Heights incorrect");
			}
			if(!Arrays.equals(tmpWidths,rowLength)){
				throw new RuntimeException("widths incorrect");
			}
		}
	}

	/**
	 Given a piece and an x, returns the y
	 value where the piece would come to rest
	 if it were dropped straight down at that x.

	 <p>
	 Implementation: use the skirt and the col heights
	 to compute this fast -- O(skirt length).
	*/
	public int dropHeight(Piece piece, int x) {
		int ret=0;
		for(int i=0; i<piece.getWidth();i++) {
			if(x+i>=0&&x+i<width) {
				if(colHeight[x+i]-piece.getSkirt()[i]>ret)ret=colHeight[x+i]-piece.getSkirt()[i];
			}
		}
		return ret;
	}


	/**
	 Returns the height of the given column --
	 i.e. the y value of the highest block + 1.
	 The height is 0 if the column contains no blocks.
	*/
	public int getColumnHeight(int x) {
		return colHeight[x];
	}


	/**
	 Returns the number of filled blocks in
	 the given row.
	*/
	public int getRowWidth(int y) {
		return rowLength[y];
	}


	/**
	 Returns true if the given block is filled in the board.
	 Blocks outside of the valid width/height area
	 always return true.
	*/
	public boolean getGrid(int x, int y) {
		if(x < 0 || y < 0) {
			return true;
		}else if(x >= width || y >= height) {
			return true;
		}else {
			return grid[x][y];
		}
	}


	public static final int PLACE_OK = 0;
	public static final int PLACE_ROW_FILLED = 1;
	public static final int PLACE_OUT_BOUNDS = 2;
	public static final int PLACE_BAD = 3;

	/**
	 Attempts to add the body of a piece to the board.
	 Copies the piece blocks into the board grid.
	 Returns PLACE_OK for a regular placement, or PLACE_ROW_FILLED
	 for a regular placement that causes at least one row to be filled.

	 <p>Error cases:
	 A placement may fail in two ways. First, if part of the piece may falls out
	 of bounds of the board, PLACE_OUT_BOUNDS is returned.
	 Or the placement may collide with existing blocks in the grid
	 in which case PLACE_BAD is returned.
	 In both error cases, the board may be left in an invalid
	 state. The client can use undo(), to recover the valid, pre-place state.
	*/
	public int place(Piece piece, int x, int y) {
		// flag !committed problem
		if (!committed) throw new RuntimeException("place commit problem");

		if(x<0||y<0) {
			return PLACE_OUT_BOUNDS;
		}else if(x+piece.getWidth()>width ||y+piece.getHeight()>height) {
			return PLACE_OUT_BOUNDS;
		}
		for(TPoint it : piece.getBody()) {
			if(grid[x+it.x][y+it.y])return PLACE_BAD;
		}
		cloud();
		committed = false;
		int result = PLACE_OK;
		for(TPoint it : piece.getBody()) {
			grid[x+it.x][y+it.y]=true;
			rowLength[y+it.y]++;
			//if(rowLength[y+it.y] == width) result = PLACE_ROW_FILLED;
			if(y+it.y+1>colHeight[x+it.x]) {
				colHeight[x+it.x]=y+it.y+ 1;
				if(colHeight[x+it.x]>maxHeight) {
					maxHeight=colHeight[x+it.x];
				}
			}

		}
		sanityCheck();
		return result;

	}


	/**
	 Deletes rows that are filled all the way across, moving
	 things above down. Returns the number of rows cleared.
	*/
	public int clearRows() {
		int rowsCleared = 0;
		committed = false;
		cloud();
		for(int i=0; i<getMaxHeight(); i++) {
			boolean delete = false;
			if(rowLength[i]==width) {
				delete=true;
			}
			if(delete) {
				rowsCleared++;
			}else {
				for(int k=0; k<getWidth(); k++) {
					grid[k][i-rowsCleared]=grid[k][i];
				}
				rowLength[i-rowsCleared]=rowLength[i];
			}
		}
		for(int i=getMaxHeight()-rowsCleared; i<getMaxHeight(); i++) {
			for(int k=0; k<grid.length; k++) {
				grid[k][i]=false;
			}
			rowLength[i]=0;
		}
		int tmpMax=0;
		for(int i = 0; i < width; i++) {
			colHeight[i]=0;
			for(int k=0; k<getMaxHeight(); k++) {
				if(grid[i][k])colHeight[i]=k+1;
			}
			if(tmpMax<colHeight[i]) tmpMax=colHeight[i];
		}
		maxHeight=tmpMax;
		sanityCheck();
		return rowsCleared;
	}



	/**
	 Reverts the board to its state before up to one place
	 and one clearRows();
	 If the conditions for undo() are not met, such as
	 calling undo() twice in a row, then the second undo() does nothing.
	 See the overview docs.
	*/
	public void undo() {
		if(committed) {
			return;
		}else {
			for(int i = 0; i<getWidth(); i++){
				System.arraycopy(gridICloud[i], 0, grid[i], 0, grid[i].length);
			}
			System.arraycopy(colHeightICloud, 0, colHeight, 0, colHeight.length);
			System.arraycopy(rowLengthICloud, 0, rowLength, 0, rowLength.length);
			maxHeight = maxHeightCloud;
		}
		commit();
		sanityCheck();
	}


	/**
	 Puts the board in the committed state.
	*/
	public void commit() {
		committed = true;
	}



	/*
	 Renders the board state as a big String, suitable for printing.
	 This is the sort of print-obj-state utility that can help see complex
	 state change over time.
	 (provided debugging utility)
	 */
	public String toString() {
		StringBuilder buff = new StringBuilder();
		for (int y = height-1; y>=0; y--) {
			buff.append('|');
			for (int x=0; x<width; x++) {
				if (getGrid(x,y)) buff.append('+');
				else buff.append(' ');
			}
			buff.append("|\n");
		}
		for (int x=0; x<width+2; x++) buff.append('-');
		return(buff.toString());
	}

	private void cloud() {
		for(int i = 0; i < getWidth(); i++){
			System.arraycopy(grid[i], 0, gridICloud[i], 0, grid[i].length);
		}
		System.arraycopy(colHeight, 0, colHeightICloud, 0, colHeight.length);
		System.arraycopy(rowLength, 0, rowLengthICloud, 0, rowLength.length);
		maxHeightCloud = maxHeight;
	}

}


