package gui;

public class testingone implements Runnable{
	static final int ROW = 5, COL = 5; 
	int M[][] = new int[][] { { 1, 1, 0, 0, 0 }, 
							  { 0, 1, 0, 0, 1 }, 
                              { 1, 0, 0, 1, 1 }, 
                              { 0, 0, 0, 0, 0 }, 
                              { 1, 0, 1, 0, 1 } }; 
	
	boolean visited[][];
	public testingone() {
		this.visited = new boolean[ROW][COL];
		
		
	}


	@Override
	public void run() {
		// TODO Auto-generated method stub
		int  count=0;
		   for (int i = 0; i < ROW; ++i) 
	            for (int j = 0; j < COL; ++j) 
	                if (M[i][j] == 1 && !visited[i][j]) // If a cell with 
	                { // value 1 is not 
	                    // visited yet, then new island found, Visit all 
	                    // cells in this island and increment island count 
	                    DFS(M, i, j, visited); 
	                    ++count; 
	             } 
		
		System.out.println("L:" + count);
	}
	
	private void DFS(int M[][], int row, int col, boolean visited[][]) 
	{ 
	        // These arrays are used to get row and column numbers 
	        // of 8 neighbors of a given cell 
	        int rowNbr[] = new int[] { -1, 0, 1, 1, 1, 0, -1, -1 }; 
	        int colNbr[] = new int[] { -1, -1, -1, 0, 1, 1, 1, 0 }; 
	  
	        // Mark this cell as visited 
	        visited[row][col] = true; 
	  
	        // Recur for all connected neighbours 
	        for (int k = 0; k < 8; ++k) {
	        	 if (isSafe(M, row + rowNbr[k], col + colNbr[k], visited)) {
	        		 DFS(M, row + rowNbr[k], col + colNbr[k], visited); 
	        	 }             
	        } 
	           
	  } 
	
	private boolean isSafe(int M[][], int row, int col, boolean visited[][]) 
	{ 
   // row number is in range, column number is in range 
   // and value is 1 and not yet visited 
	return (row >= 0) 
			&& (row < ROW) 
			&& (col >= 0) 
			&& (col < COL) 
			&& (M[row][col] == 1 
			&& !visited[row][col]); 
	} 
	

}
