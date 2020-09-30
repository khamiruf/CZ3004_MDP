package gui;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;

import algorithm.FastestPath;
import constant.Constants;
import constant.Constants.MOVEMENT;
import entity.Cell;
import entity.Map;
import entity.Robot;


public class testMain implements Runnable{


	private MainGUI mGui;
	private Map exploreMap;
	private Robot robot;
	private float playSpeed;
	private int userPercent;
	private int timeLimit;
	double currentCoverage; 		//storing current explore map coverage(%)
	Scanner sc ;
	Timer mTimer;
	boolean timerStop;
	
	public testMain(MainGUI maGUI, Map eMap, Robot rBot) {
		
		exploreMap = eMap;					//unexplored map that robot knows, obtain from MainGUI 	
		robot = rBot;						//Robot object obtained from MainGUI
		
		this.mGui = maGUI;
		this.userPercent = maGUI.getUserPercentage();
	    this.playSpeed = 1/maGUI.getUserSpeed();
	    this.timeLimit = maGUI.getUserTimeLimit();
	    this.timerStop = false;
	    
	    sc = new Scanner(System.in);
	    
	    int[] wayPointArr = maGUI.getUserWayPoint();
	    this.exploreMap.setWayPoint(wayPointArr[0], wayPointArr[1]);
	
		
		
	}
	
		

		
		@Override
		public void run() {
			// TODO Auto-generated method stub
			
			mGui.displayMsgToUI("SimulateExplorationThread Started");
			
			String recMsg="";
				
	
			while (!Thread.currentThread().isInterrupted()) {
				try {
					
					initialiseTimer();
					System.out.println("Enter sensor: ");
					recMsg = sc.nextLine();
					exploreMap.setExploredCells(robot, recMsg);
					displayToUI();			
					do {
						System.out.println("Enter sensor: ");
						recMsg = sc.nextLine();
						if(robot.isMovementValid(exploreMap, MOVEMENT.RIGHT)){
							 robot.turn(MOVEMENT.RIGHT);
							 
		                        exploreMap.setExploredCells(robot, recMsg);
		                        displayToUI();			//repaint GUI to show turning
		                        robot.move(MOVEMENT.FORWARD);
		                        exploreMap.setExploredCells(robot, recMsg);
		                        
				        }
				        else if(robot.isMovementValid(exploreMap, MOVEMENT.FORWARD)){
				        	  robot.move(MOVEMENT.FORWARD);
		                        exploreMap.setExploredCells(robot, recMsg);                  
		                      
				        }
				        else if(robot.isMovementValid(exploreMap, MOVEMENT.LEFT)){
				        	 robot.turn(MOVEMENT.LEFT);
		                        exploreMap.setExploredCells(robot, recMsg);
		                       // mGui.paintResult();				//repaint GUI to show turning
		                       // robot.move(MOVEMENT.FORWARD);                   
		                       // exploreMap.setExploredCells(robot, recMsg);
		                      
				        }
				        else if(robot.isMovementValid(exploreMap, MOVEMENT.BACKWARD)){
				        	  robot.turn(MOVEMENT.RIGHT);
		                        exploreMap.setExploredCells(robot, recMsg);
		                        displayToUI();					//repaint GUI to show turning
		                        robot.turn(MOVEMENT.RIGHT);
		                        exploreMap.setExploredCells(robot, recMsg);      
		                        displayToUI();					//repaint GUI to show turning
		                        robot.move(MOVEMENT.FORWARD);
		                        exploreMap.setExploredCells(robot, recMsg);
		                      
				        }
						displayToUI();					//repaint GUI to show moving forward
		                
					
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
							//printMovement(cellStep);
							
							int currRow = robot.getPosRow();
						     int currCol = robot.getPosCol();
						     
							 for(int i=0; i < cellStep.size(); i++){
						            int destRow = cellStep.get(i).getRowPos();
						            int destCol = cellStep.get(i).getColPos();
						            switch(robot.getCurrDir()){
						                case NORTH:
						                    if(currCol == destCol){
						                        if(currRow < destRow){					                        	
						                        	robot.move(MOVEMENT.FORWARD);
						                        	}
						                        else if(currRow > destRow){
						                        	robot.turn(MOVEMENT.RIGHT); displayToUI();	
						                        	robot.turn(MOVEMENT.RIGHT); displayToUI();	
						                        	robot.move(MOVEMENT.FORWARD);
						                        	}
						                    }
						                    else if(currRow == destRow){
						                        if(currCol < destCol){
						                        	robot.turn(MOVEMENT.RIGHT);displayToUI();	
						                        	robot.move(MOVEMENT.FORWARD);
						                        }
						                        else if(currCol > destCol){
						                        	robot.turn(MOVEMENT.LEFT);displayToUI();	
						                        	robot.move(MOVEMENT.FORWARD);
						                        }
						                    }
						                    break;
						                case SOUTH:
						                    if(currCol == destCol){
						                        if(currRow < destRow){
						                        	robot.turn(MOVEMENT.RIGHT);displayToUI();	
						                        	robot.turn(MOVEMENT.RIGHT);displayToUI();	
						                        	robot.move(MOVEMENT.FORWARD);
						                        	}
						                        else if(currRow > destRow){ 
						                        	robot.move(MOVEMENT.FORWARD);
						                        }
						                    }
						                    else if(currRow == destRow){
						                        if(currCol < destCol){
						                        	robot.turn(MOVEMENT.LEFT);displayToUI();	
						                        	robot.move(MOVEMENT.FORWARD);
						                        }
						                        else if(currCol > destCol){ 
						                        	robot.turn(MOVEMENT.RIGHT);displayToUI();	
						                        	robot.move(MOVEMENT.FORWARD);
						                        }
						                    }
						                    break;
						                case EAST:
						                    if(currCol == destCol){
						                        if(currRow < destRow){
						                        	robot.turn(MOVEMENT.LEFT); displayToUI();	
						                        	robot.move(MOVEMENT.FORWARD);
						                        }
						                        else if(currRow > destRow){
						                        	robot.turn(MOVEMENT.RIGHT); displayToUI();	
						                        	robot.move(MOVEMENT.FORWARD);
						                        }
						                    }
						                    else if(currRow == destRow){
						                        if(currCol < destCol){
						                        	robot.move(MOVEMENT.FORWARD);}
						                        else if(currCol > destCol){
						                        	robot.turn(MOVEMENT.RIGHT); displayToUI();	
						                        	robot.turn(MOVEMENT.RIGHT); displayToUI();	
						                        	robot.move(MOVEMENT.FORWARD);}
						                    }
						                    break;
						                case WEST:
						                if(currCol == destCol){
						                    if(currRow < destRow){
						                    	robot.turn(MOVEMENT.RIGHT); displayToUI();	
						                    	robot.move(MOVEMENT.FORWARD);
						                    	}
						                    else if(currRow > destRow){
						                    	robot.turn(MOVEMENT.LEFT); displayToUI();	
						                    	robot.move(MOVEMENT.FORWARD);
						                    	}
						                }
						                else if(currRow == destRow){
						                    if(currCol < destCol){
						                    	robot.turn(MOVEMENT.RIGHT); displayToUI();	
						                    	robot.turn(MOVEMENT.RIGHT); displayToUI();	
						                    	robot.move(MOVEMENT.FORWARD);
						                    	}
						                    else if(currCol > destCol){ 
						                    	robot.move(MOVEMENT.FORWARD);
						                    	}
						                }
						                break;
						            }
						            exploreMap.setExploredCells(robot, recMsg);
						            displayToUI();						//repaint GUI to show robot movement on map
					          							            
						            currRow = robot.getPosRow();
						            currCol = robot.getPosCol();
						            System.out.println("Location: " + currRow + "_" +currCol);
						        }
							
							//exploreMap.setExploredCells(robot, recMsg);
							//displayToUI();
							
							//Check coverage percentage
							/*
							currentCoverage = getMapPercentCoverage();
							mGui.displayMapCoverToUI(currentCoverage);
			                if(this.userPercent != 100) {	                	
			                	if( currentCoverage> userPercent) {                		
			                		return;
			                	}
			                }
			                */
							
							 if(checkLimitConditions()) {
									this.mTimer.cancel();
									this.mTimer.purge();
				                	return;
				              }
							unexploredList=updateUnexploreList(unexploredList);
						}
					}
						
						
					
					
					/*
					Cell unexploredCell = getUnexploredCell(exploreMap);
					list
					if(unexploredCell!=null) {
						FastestPath fastobj = new FastestPath(robot, exploreMap);
						
						while(unexploredCell != null) {
						System.out.println("UnexploredLoc:" + unexploredCell.getRowPos() + "_"+ unexploredCell.getColPos());	
						ArrayList<Cell> cellStep = fastobj.calculateFastestPath2(exploreMap, unexploredCell.getRowPos(), unexploredCell.getColPos());
						if(null){add UnexploredCell to a list}
						printMovement(cellStep);
						exploreMap.setExploredCells(robot, recMsg);
						mGui.paintResult(exploreMap, robot);
						unexploredCell = getUnexploredCell(exploreMap);
						fastobj.setMockRobot(robot);	
						}
					}
					*/
					//System.out.println("ZW: " + robot.getPosRow() +"_"+ robot.getPosCol());
					if(!exploreMap.checkIfRobotAtStartPos(robot)) {
						FastestPath fastobj = new FastestPath(robot, exploreMap);
						ArrayList<Cell> cellStep = fastobj.calculateFastestPath(exploreMap, exploreMap.getStartGoalPosition().getRowPos(), exploreMap.getStartGoalPosition().getColPos());
						printMovement(cellStep);
											
					}
					
					this.mTimer.cancel();
					this.mTimer.purge();
					
						break;  	//break out from thread
						
				} catch (InterruptedException e) {
					mGui.displayMsgToUI("SimulateExplorationThread Interrupted!");
					this.mTimer.cancel();
					this.mTimer.purge();
					return;
				}
				
				
			}
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
		}
		
		private void printMovement(ArrayList<Cell> cellStep) throws InterruptedException {
			 int currRow = robot.getPosRow();
		     int currCol = robot.getPosCol();
		     
			 for(int i=0; i < cellStep.size(); i++){
		            int destRow = cellStep.get(i).getRowPos();
		            int destCol = cellStep.get(i).getColPos();
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
		            String recMsg="";
		            System.out.println("Enter sensor:");
		            recMsg = sc.nextLine();
		            exploreMap.setExploredCells(robot, recMsg);
		            displayToUI();						//repaint GUI to show robot movement on map
	          		
		            
		            currRow = robot.getPosRow();
		            currCol = robot.getPosCol();
		            System.out.println("Location: " + currRow + "_" +currCol);
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
		
		private Cell getUnexploredCell(Map exploredMap) {
			
			Cell unexploredCell;
			
		      for (int r = 0; r < Constants.MAX_ROW; r++) {
		            for (int c = 0; c < Constants.MAX_COL; c++) {
		                Cell cell = exploredMap.getMapGrid()[r][c];
		                if(cell.getExploredState()==false) {
		                 return cell;
		                }
		            }
		        }
			return null;
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
	        /*
	        mGui.displayTimerToUI((int)mTimer.getElapsedTime());
	        if(this.timeLimit != 0) {                	
	        	if((int)mTimer.getElapsedTime()>=this.timeLimit) {
	        		return true;
	        	}
	        	
	        }
	        */
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
						//System.out.println((int)timeElapsed +"-" +timeLimit);
						if((int)timeElapsed>=timeLimit) {
							//System.out.println("Ent");
							timerStop= true;
						}
					}
					/*
					long timeLimitMillis = TimeUnit.MINUTES.toMillis(timeLimit[0])
							+ TimeUnit.SECONDS.toMillis(timeLimit[1]);

					if (timeLimitMillis != 0) {
						long timeDifference = timeLimitMillis - timeElapsed;
						long minutesLeft = TimeUnit.MILLISECONDS.toMinutes(timeDifference);
						long secondsLeft = TimeUnit.MILLISECONDS.toSeconds(timeDifference + 10);

						if (secondsLeft <= 0) {
							System.out.println("Time limit reached - stop exploration.");
							timerStop = true;
						}

						gui.setTimer(String.format("%02d", minutesLeft) + ":" + String.format("%02d", secondsLeft % 60));
					} else {
						gui.setTimer(String.format("%02d", TimeUnit.MILLISECONDS.toMinutes(timeElapsed)) + ":"
								+ String.format("%02d", TimeUnit.MILLISECONDS.toSeconds(timeElapsed) % 60));
					}*/
					
					
				}
			}, 0, 1000);
		}
		

		
	

}
