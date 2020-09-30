package gui;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import algorithm.FastestPath;
import constant.Constants.MOVEMENT;
import entity.Cell;
import entity.Map;
import entity.Robot;

public class simulateFastestPath implements Runnable{

	FastestPath fastestPath;
	Map exploreMap;
	Robot robot;
	MainGUI mGui;
	private float playSpeed;
	Timer mTimer;
	
	public simulateFastestPath(MainGUI maGUI, Robot ro, Map expMap) {
		 
		
		this.exploreMap = expMap;
		this.mGui = maGUI;
		this.robot = ro;
		this.playSpeed = 1/maGUI.getUserSpeed();
		fastestPath = new FastestPath(ro, this.exploreMap);
	}
	
	@Override
	public void run() {
		while(!Thread.currentThread().isInterrupted()) {
			try {
				initialiseTimer();
				mGui.displayMsgToUI("SimulateFastestPathThread Started");
				
				//*
				 ArrayList<Cell> cellsInPath  = fastestPath.calculateFastestPath(exploreMap, exploreMap.getWayPoint().getRowPos(), exploreMap.getWayPoint().getColPos());
				 printMovement(cellsInPath);
				
				 fastestPath = new FastestPath(this.robot, exploreMap);
				 cellsInPath = fastestPath.calculateFastestPath(exploreMap, exploreMap.getEndGoalPosition().getRowPos(), exploreMap.getEndGoalPosition().getColPos());
		         //fastestPath.convertCellsToMovements(robot, cellsInPath);
			     printMovement(cellsInPath);
				/*/
				
				/*
				ArrayList<Cell> cellsInPath = fastestPath.testone(exploreMap);
				printMovement(cellsInPath);
				*/
			     this.mTimer.cancel();
				 this.mTimer.purge();
				
			}
			catch(Exception ex) {
				mGui.displayMsgToUI("FastestPathThread Error: " + ex.getMessage());
				this.mTimer.cancel();
				this.mTimer.purge();
			}
			
			break; //break from thread
		}
		         
	}
	
	private void printMovement(ArrayList<Cell> cellStep)throws InterruptedException {
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
	            displayToUI();						//repaint GUI to show robot movement on map
         		
	            
	            currRow = robot.getPosRow();
	            currCol = robot.getPosCol();
	            System.out.println("Location: " + currRow + "_" +currCol);
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
	private void displayToUI() throws InterruptedException {
		
		 mGui.paintResult();	
		 Thread.sleep((long)(playSpeed * 1000));
	}
	
}
