package gui;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import algorithm.Exploration;
import algorithm.FastestPath;
import constant.Constants.MOVEMENT;
import entity.Cell;
import entity.Map;
import entity.Robot;
import constant.Constants;
import constant.Constants.DIRECTION;
import util.FileManager;
import util.NextMove;


/**
 *	This is the thread to run the Exploration simulation 
 *
 */
public class simulateExploration1 implements Runnable{

	
	
	private MainGUI mGui;
	private Map realMap, exploreMap;
	private Robot robot;
	private float playSpeed;
	private int userPercent;
	private int timeLimit;
	double currentCoverage; 		//storing current explore map coverage(%)
	//Stopwatch mTimer;				//Timer
	Timer mTimer;
	boolean timerStop;
	
	public simulateExploration1(MainGUI maGUI, Map eMap, Robot rBot, String fileName) {
		
		exploreMap = eMap;					//unexplored map that robot knows, obtain from MainGUI 	
		robot = rBot;						//Robot object obtained from MainGUI
		
		this.mGui = maGUI;
		this.userPercent = maGUI.getUserPercentage();
	    this.playSpeed = 1/maGUI.getUserSpeed();
	    this.timeLimit = maGUI.getUserTimeLimit();
	    this.timerStop = false;
	    
	    
	    int[] wayPointArr = maGUI.getUserWayPoint();
	    this.exploreMap.setWayPoint(wayPointArr[0], wayPointArr[1]);
		try {
			File mapFile = new File("maps\\" + fileName); 
			realMap = new Map(FileManager.readMapFromFile(mapFile));
		} catch (IOException e) {
			
			e.printStackTrace();
		}
		
		
	}
	
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		
		mGui.displayMsgToUI("SimulateExplorationThread Started");
		
			try {
				
				initialiseTimer();
				exploreMap.setExploredCells(robot, realMap);
								
				do {
					
					if(robot.isMovementValid(exploreMap, MOVEMENT.RIGHT)){
						 robot.turn(MOVEMENT.RIGHT);
						 
	                      exploreMap.setExploredCells(robot, realMap);
	                      displayToUI();		
	                      robot.move(MOVEMENT.FORWARD);
	                      exploreMap.setExploredCells(robot, realMap);
	                        
			        }
			        else if(robot.isMovementValid(exploreMap, MOVEMENT.FORWARD)){
			        	  robot.move(MOVEMENT.FORWARD);
	                        exploreMap.setExploredCells(robot, realMap);                  
	                      
			        }
			        else if(robot.isMovementValid(exploreMap, MOVEMENT.LEFT)){
			        	 robot.turn(MOVEMENT.LEFT);
	                        exploreMap.setExploredCells(robot, realMap);
	                   
	                      	                      
			        }
			        else if(robot.isMovementValid(exploreMap, MOVEMENT.BACKWARD)){
			        	  robot.turn(MOVEMENT.RIGHT);
	                        exploreMap.setExploredCells(robot, realMap);
	                        displayToUI();					
	                        robot.turn(MOVEMENT.RIGHT);
	                        exploreMap.setExploredCells(robot, realMap);      
	                        displayToUI();					
	                        robot.move(MOVEMENT.FORWARD);
	                        exploreMap.setExploredCells(robot, realMap);
	                      
			        }
					displayToUI();				
	                
					
	                if(checkLimitConditions()) {
	                	this.mTimer.cancel();
	    				this.mTimer.purge();
	                	return;
	                }
				}while(!exploreMap.getStartGoalPosition().getExploredState() ||
					   !exploreMap.getEndGoalPosition().getExploredState() ||
					   !exploreMap.checkIfRobotAtStartPos(robot));
				
				ArrayList<Cell> unexploredList = getUnexploredList(exploreMap);
				while(unexploredList.size()>0) {
					
					
					FastestPath fastobj = new FastestPath(robot, exploreMap);
					ArrayList<Cell> cellStep = fastobj.calculateFastestPath2(exploreMap, unexploredList.get(0).getRowPos(), unexploredList.get(0).getColPos());
					if(cellStep==null) {
						unexploredList.remove(0);
					}
					else {
						printMovement(cellStep);
																	
						 if(checkLimitConditions()) {
								this.mTimer.cancel();
								this.mTimer.purge();
			                	return;
			              }
						unexploredList=updateUnexploreList(unexploredList);
					}
				}
					
					
							
				if(!exploreMap.checkIfRobotAtStartPos(robot)) {
					FastestPath fastobj = new FastestPath(robot, exploreMap);
					ArrayList<Cell> cellStep = fastobj.calculateFastestPath(exploreMap, exploreMap.getStartGoalPosition().getRowPos(), exploreMap.getStartGoalPosition().getColPos());
					printMovement(cellStep);
										
				}
				
				this.mTimer.cancel();
				this.mTimer.purge();
				
					
					
			} catch (InterruptedException e) {
				this.mTimer.cancel();
				this.mTimer.purge();
				mGui.displayMsgToUI("SimulateExplorationThread Interrupted!");
				return;
			}
			catch(Exception e) {
				this.mTimer.cancel();
				this.mTimer.purge();
				mGui.displayMsgToUI("SimulateExplorationThread unknown error: "+ e.getMessage());
				return;
			}
			
			
			this.mTimer.cancel();
			this.mTimer.purge();
			mGui.printFinal();			//Print the final map that robot knows on system console
			mGui.displayMsgToUI("MDF1: " + exploreMap.getMDF1());
			mGui.displayMsgToUI("MDF2: " + exploreMap.getMDF2());
			System.out.println("MDF1: " + exploreMap.getMDF1());
			System.out.println("MDF2: " + exploreMap.getMDF2());
			try {
				this.playSpeed=1;
				faceNorthDirection();
			} catch (InterruptedException e) {
				
				e.printStackTrace();
			}
			
			printVirtualWall();
		
	}
	
	private void printMovement(ArrayList<Cell> cellStep) throws InterruptedException {
		 int currRow = robot.getPosRow();
	     int currCol = robot.getPosCol();
	     
		 for(int i=0; i < cellStep.size(); i++){
	            int destRow = cellStep.get(i).getRowPos();
	            int destCol = cellStep.get(i).getColPos();
	            System.out.println("printmoveRobot:"+cellStep.get(i).getRowPos() +"_"+cellStep.get(i).getColPos()+"-"+ currRow+"_"+currCol +":"+robot.getCurrDir());
				
	            switch(robot.getCurrDir()){
	                case NORTH:
	                    if(currCol == destCol){
	                        if(currRow < destRow){
	                        	
	                        	robot.move(MOVEMENT.FORWARD);
	                        	}
	                        else if(currRow > destRow){
	                        	robot.turn(MOVEMENT.RIGHT); 
	                        	displayToUI();	
	                        	robot.turn(MOVEMENT.RIGHT); 
	                        	displayToUI();	
	                        	robot.move(MOVEMENT.FORWARD);
	                        	}
	                    }
	                    else if(currRow == destRow){
	                        if(currCol < destCol){
	                        	robot.turn(MOVEMENT.RIGHT);
	                        	displayToUI();	
	                        	robot.move(MOVEMENT.FORWARD);
	                        }
	                        else if(currCol > destCol){
	                        	robot.turn(MOVEMENT.LEFT); 
	                        	displayToUI();	
	                        	robot.move(MOVEMENT.FORWARD);
	                        }
	                    }
	                    break;
	                case SOUTH:
	                    if(currCol == destCol){
	                        if(currRow < destRow){
	                        	robot.turn(MOVEMENT.RIGHT); 
	                        	displayToUI();	
	                        	robot.turn(MOVEMENT.RIGHT); 
	                        	displayToUI();	
	                        	robot.move(MOVEMENT.FORWARD);
	                        	}
	                        else if(currRow > destRow){ 
	                        	robot.move(MOVEMENT.FORWARD);
	                        }
	                    }
	                    else if(currRow == destRow){
	                        if(currCol < destCol){
	                        	robot.turn(MOVEMENT.LEFT); 
	                        	displayToUI();	
	                        	robot.move(MOVEMENT.FORWARD);
	                        }
	                        else if(currCol > destCol){ 
	                        	robot.turn(MOVEMENT.RIGHT); 
	                        	displayToUI();	
	                        	robot.move(MOVEMENT.FORWARD);
	                        }
	                    }
	                    break;
	                case EAST:
	                    if(currCol == destCol){
	                        if(currRow < destRow){
	                        	robot.turn(MOVEMENT.LEFT); 
	                        	displayToUI();	
	                        	robot.move(MOVEMENT.FORWARD);
	                        }
	                        else if(currRow > destRow){
	                        	robot.turn(MOVEMENT.RIGHT); 
	                        	displayToUI();	
	                        	robot.move(MOVEMENT.FORWARD);
	                        }
	                    }
	                    else if(currRow == destRow){
	                        if(currCol < destCol){
	                        	robot.move(MOVEMENT.FORWARD);}
	                        else if(currCol > destCol){
	                        	robot.turn(MOVEMENT.RIGHT); 
	                        	displayToUI();	
	                        	robot.turn(MOVEMENT.RIGHT); 
	                        	displayToUI();	
	                        	robot.move(MOVEMENT.FORWARD);}
	                    }
	                    break;
	                case WEST:
	                if(currCol == destCol){
	                    if(currRow < destRow){
	                    	robot.turn(MOVEMENT.RIGHT); 
	                    	displayToUI();	
	                    	robot.move(MOVEMENT.FORWARD);
	                    	}
	                    else if(currRow > destRow){
	                    	robot.turn(MOVEMENT.LEFT); 
	                    	displayToUI();	
	                    	robot.move(MOVEMENT.FORWARD);
	                    	}
	                }
	                else if(currRow == destRow){
	                    if(currCol < destCol){
	                    	robot.turn(MOVEMENT.RIGHT); 
	                    	displayToUI();	
	                    	robot.turn(MOVEMENT.RIGHT); 
	                    	displayToUI();	
	                    	robot.move(MOVEMENT.FORWARD);
	                    	}
	                    else if(currCol > destCol){ 
	                    	robot.move(MOVEMENT.FORWARD);
	                    	}
	                }
	                break;
	            }
	            exploreMap.setExploredCells(robot, realMap);
	            displayToUI();						//repaint GUI to show robot movement on map
          		
	            
	            currRow = robot.getPosRow();
	            currCol = robot.getPosCol();
	            System.out.println("Robot Scan at: " + currRow + "_" +currCol);
	        }
		
		
	}
	
	private void displayToUI() throws InterruptedException {
	
		 mGui.paintResult();	
		 Thread.sleep((long)(playSpeed * 1000));
	}
	
	private ArrayList<Cell> getUnexploredList(Map exploredMap){
		
		ArrayList<Cell> unexploredList = new ArrayList<Cell>();
		
	      for (int r = 0; r < Constants.MAX_ROW; r++) {
	            for (int c = 0; c < Constants.MAX_COL; c++) {
	                Cell cell = exploredMap.getMapGrid()[r][c];
	                if(cell.getExploredState()==false) {
	                	unexploredList.add(cell);
	                }
	            }
	        }
		return unexploredList;
		 
	}

	
	private ArrayList<Cell> updateUnexploreList(ArrayList<Cell> clist) {
		for(int i = 0;i<clist.size();i++) {
			Cell cObj = clist.get(i);
			if(exploreMap.getMapGrid()[cObj.getRowPos()][cObj.getColPos()].getExploredState()) {
				clist.remove(cObj);
				i--;
			}
		}
		return clist;
	}
	
	// Get percentage of explored map
	private double getMapPercentCoverage() {
		
		double percent=0;
		for (int r = 0; r < Constants.MAX_ROW; r++) {
            for (int c = 0; c < Constants.MAX_COL; c++) {
                Cell cell = this.exploreMap.getMapGrid()[r][c];
             
                if(cell.getExploredState()==true) {              	
                 percent++;
                }
            }
        }
	
		return (percent/300)*100 ;
	}
	
	
	private boolean checkLimitConditions() {
		
		currentCoverage = getMapPercentCoverage();
		mGui.displayMapCoverToUI(currentCoverage);
        if(this.userPercent != 100) {	                	
        	if( currentCoverage> userPercent) {                		
        		return true;
        	}
        }
        
        if(this.timeLimit != 0 && timerStop) {
        	return true;
        }
     
		return false;		
	}
	
	private void faceNorthDirection() throws InterruptedException {
		mGui.displayMsgToUI("Calibrating ROBOT!");
		switch(robot.getCurrDir()) {
		case NORTH: break;
		case SOUTH:
			this.robot.turn(MOVEMENT.RIGHT);
			displayToUI();	 
			this.robot.turn(MOVEMENT.RIGHT);
			displayToUI();
			break;
		case EAST: 
			this.robot.turn(MOVEMENT.LEFT);
			displayToUI();
			break;
		case WEST:
			this.robot.turn(MOVEMENT.RIGHT);
			displayToUI();	
			break;
		}
	}
	
	private void initialiseTimer() {
		/* Count up */
		this.mTimer = new Timer();
		this.mTimer.scheduleAtFixedRate(new TimerTask() {
			private long startTime= System.currentTimeMillis(); 
			private long timeElapsed;
		
			/* Update timer every second */
			@Override
			public void run() {
			
				timeElapsed = (System.currentTimeMillis() - startTime) / 1000;
				mGui.displayTimerToUI((int)timeElapsed);
				if(timeLimit != 0) {
			
					if((int)timeElapsed>=timeLimit) {					
						timerStop= true;
					}
				}
						
			}
		}, 0, 1000);
	}
	  public void printVirtualWall() {
	    			
			for(int i=Constants.MAX_ROW-1;i>=0;i--) {
				
				for(int y=0;y<Constants.MAX_COL;y++) {
					Cell cellObj = exploreMap.getMapGrid()[i][y];
					if(cellObj.getExploredState()) {
						if(cellObj.isVirtualWall()&& !cellObj.isObstacle()) {
							System.out.print("W"+" ");
							
						}
						else if(cellObj.isObstacle()) {
							System.out.print("X"+" ");
							
						}
						else
							System.out.print("O"+" ");
					}
					else {
						System.out.print("Z"+" ");
						
					}
				}
				System.out.println("");
				
			}
		
		}
}


