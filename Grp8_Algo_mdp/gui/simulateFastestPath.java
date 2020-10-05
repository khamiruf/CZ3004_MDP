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
		
			try {
				initialiseTimer();
				mGui.displayMsgToUI("SimulateFastestPathThread Started");
				
				/*  
				 ArrayList<Cell> cellsInPath  = fastestPath.calculateFastestPath(exploreMap, exploreMap.getWayPoint().getRowPos(), exploreMap.getWayPoint().getColPos());
				 printMovement(cellsInPath);
				
				 fastestPath = new FastestPath(this.robot, exploreMap);
				 cellsInPath = fastestPath.calculateFastestPath(exploreMap, exploreMap.getEndGoalPosition().getRowPos(), exploreMap.getEndGoalPosition().getColPos());
		         //fastestPath.convertCellsToMovements(robot, cellsInPath);
			     printMovement(cellsInPath);
				*/
				
				//*
				ArrayList<Cell> cellsInPath = fastestPath.findAllWPEndPaths(exploreMap);
				//printMovement(cellsInPath);
				String moveString = convertCellsToMovements(cellsInPath);
				System.out.println(moveString);
				printFastestPathMovement(moveString);
				//*/
			     this.mTimer.cancel();
				 this.mTimer.purge();
				
			}
			catch(InterruptedException ex) {
				this.mTimer.cancel();
				this.mTimer.purge();
				mGui.displayMsgToUI("FastestPathThread Interrupted!");
				
			}
			catch(Exception ex) {
				this.mTimer.cancel();
				this.mTimer.purge();
				mGui.displayMsgToUI("FastestPathThread unknown Error: " + ex.getMessage());
				
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
	            displayToUI();					
         		
	            
	            currRow = robot.getPosRow();
	            currCol = robot.getPosCol();
	            //System.out.println("Location: " + currRow + "_" +currCol);
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
			
			}
		}, 0, 1000);
	}
	private void displayToUI() throws InterruptedException {
		
		 mGui.paintResult();	
		 //Thread.sleep((long)(playSpeed * 1000));
		 Thread.sleep((long)( 500));
	}
	
	public String parseFPMovement(ArrayList<MOVEMENT> fastestPathMovements){
        int i = 0;
        int j= 0;
        int counter = 1;
        String result = "FP|";
        for(int x=0;x<fastestPathMovements.size();x++) {
        	   System.out.println("H:" + fastestPathMovements.get(x));
        }
     
        while(i<fastestPathMovements.size()){
        	
            switch(fastestPathMovements.get(i)){
             
                case FORWARD:
                    result += "F";
                    break;
                case LEFT:
                    result += "L";
                    break;
                case RIGHT:
                    result += "R";
                    break;
                case BACKWARD:
                	result += "B";
                    break;
                default:
                    break;
                
            }
            for(j = i+1; j< fastestPathMovements.size(); j++){
                if(fastestPathMovements.get(i)==fastestPathMovements.get(j)){
                	//System.out.println("X:" + fastestPathMovements.get(i));
                    counter++;    
                }      
                else{                            	
                    break;
                }
                
            }
           
            if(fastestPathMovements.get(i)==MOVEMENT.FORWARD && counter<10) {
            	result += "0"+Integer.toString(counter);
            }else {
            	 result += Integer.toString(counter);
            }
            i=j;
            result += "|";
            counter = 1;
                 
            //System.out.println(i +"_");     
       }
        System.out.println("R:" +result);
        return result;
    }

	public String convertCellsToMovements(ArrayList<Cell> cellsInPath){
		
		
		Robot mBot = new Robot(this.robot.getPosRow(),this.robot.getPosCol(),this.robot.getCurrDir());
        int currRow = mBot.getPosRow();
        int currCol = mBot.getPosCol();

        ArrayList<MOVEMENT> fastestPathMovements = new ArrayList<MOVEMENT>();

        for(int i=0; i < cellsInPath.size(); i++){
            int destRow = cellsInPath.get(i).getRowPos();
            int destCol = cellsInPath.get(i).getColPos();
            switch(mBot.getCurrDir()){
                case NORTH:
                    if(currCol == destCol){
                        if(currRow < destRow){
                        	fastestPathMovements.add(MOVEMENT.FORWARD); 
                        	mBot.move(MOVEMENT.FORWARD);}
                        else if(currRow > destRow){
                        	fastestPathMovements.add(MOVEMENT.BACKWARD); 
                        	//fastestPathMovements.add(MOVEMENT.RIGHT);
                        	//fastestPathMovements.add(MOVEMENT.RIGHT);
                        	//fastestPathMovements.add(MOVEMENT.FORWARD);
                        	mBot.turn(MOVEMENT.RIGHT); 
                        	mBot.turn(MOVEMENT.RIGHT);
                        	mBot.move(MOVEMENT.FORWARD);}
                    }
                    else if(currRow == destRow){
                        if(currCol < destCol){
                        	fastestPathMovements.add(MOVEMENT.RIGHT); 
                        	//fastestPathMovements.add(MOVEMENT.FORWARD);
                        	mBot.turn(MOVEMENT.RIGHT); 
                        	mBot.move(MOVEMENT.FORWARD);}
                        else if(currCol > destCol){
                        	fastestPathMovements.add(MOVEMENT.LEFT); 
                        	//fastestPathMovements.add(MOVEMENT.FORWARD);
                        	mBot.turn(MOVEMENT.LEFT); 
                        	mBot.move(MOVEMENT.FORWARD);}
                    }
                    break;
                case SOUTH:
                    if(currCol == destCol){
                        if(currRow < destRow){
                        	fastestPathMovements.add(MOVEMENT.BACKWARD); 
                        	//fastestPathMovements.add(MOVEMENT.RIGHT);
                        	//fastestPathMovements.add(MOVEMENT.RIGHT);
                        	//fastestPathMovements.add(MOVEMENT.FORWARD);
                        	mBot.turn(MOVEMENT.RIGHT); 
                        	mBot.turn(MOVEMENT.RIGHT); 
                        	mBot.move(MOVEMENT.FORWARD);}
                        else if(currRow > destRow){
                        	fastestPathMovements.add(MOVEMENT.FORWARD); 
                        	mBot.move(MOVEMENT.FORWARD);}
                    }
                    else if(currRow == destRow){
                        if(currCol < destCol){
                        	fastestPathMovements.add(MOVEMENT.LEFT); 
                        	//fastestPathMovements.add(MOVEMENT.FORWARD);
                        	mBot.turn(MOVEMENT.LEFT);
                        	mBot.move(MOVEMENT.FORWARD);}
                        else if(currCol > destCol){
                        	fastestPathMovements.add(MOVEMENT.RIGHT);
                        	//fastestPathMovements.add(MOVEMENT.FORWARD);
                        	mBot.turn(MOVEMENT.RIGHT); 
                        	mBot.move(MOVEMENT.FORWARD);}
                    }
                    break;
                case EAST:
                    if(currCol == destCol){
                        if(currRow < destRow){
                        	fastestPathMovements.add(MOVEMENT.LEFT); 
                        	//fastestPathMovements.add(MOVEMENT.FORWARD);
                        	mBot.turn(MOVEMENT.LEFT);
                        	mBot.move(MOVEMENT.FORWARD);}
                        else if(currRow > destRow){
                        	fastestPathMovements.add(MOVEMENT.RIGHT); 
                        	//fastestPathMovements.add(MOVEMENT.FORWARD);
                        	mBot.turn(MOVEMENT.RIGHT); 
                        	mBot.move(MOVEMENT.FORWARD);}
                    }
                    else if(currRow == destRow){
                        if(currCol < destCol){
                        	fastestPathMovements.add(MOVEMENT.FORWARD); 
                        	mBot.move(MOVEMENT.FORWARD);}
                        else if(currCol > destCol){
                        	fastestPathMovements.add(MOVEMENT.BACKWARD);
                        	//fastestPathMovements.add(MOVEMENT.RIGHT);
                        	//fastestPathMovements.add(MOVEMENT.RIGHT);
                        	//fastestPathMovements.add(MOVEMENT.FORWARD);
                        	mBot.turn(MOVEMENT.RIGHT); 
                        	mBot.turn(MOVEMENT.RIGHT); 
                        	mBot.move(MOVEMENT.FORWARD);
                        	}
                    }
                    break;
                case WEST:
                if(currCol == destCol){
                    if(currRow < destRow){
                    	fastestPathMovements.add(MOVEMENT.RIGHT); 
                    	//fastestPathMovements.add(MOVEMENT.FORWARD);
                    	mBot.turn(MOVEMENT.RIGHT); 
                    	mBot.move(MOVEMENT.FORWARD);
                    }
                    else if(currRow > destRow){
                    	fastestPathMovements.add(MOVEMENT.LEFT); 
                    	//fastestPathMovements.add(MOVEMENT.FORWARD);
                    	mBot.turn(MOVEMENT.LEFT); 
                    	mBot.move(MOVEMENT.FORWARD);
                    	}
                }
                else if(currRow == destRow){
                    if(currCol < destCol){
                    	fastestPathMovements.add(MOVEMENT.BACKWARD);
                    	//fastestPathMovements.add(MOVEMENT.RIGHT);
                    	//fastestPathMovements.add(MOVEMENT.RIGHT);
                    	//fastestPathMovements.add(MOVEMENT.FORWARD);
                    	mBot.turn(MOVEMENT.RIGHT);
                    	mBot.turn(MOVEMENT.RIGHT);
                    	mBot.move(MOVEMENT.FORWARD);}
                    else if(currCol > destCol){
                    	fastestPathMovements.add(MOVEMENT.FORWARD);
                    	mBot.move(MOVEMENT.FORWARD);}
                }
                break;
            }
            
            currRow = mBot.getPosRow();
            currCol = mBot.getPosCol();
           
        }

  
        String result = parseFPMovement(fastestPathMovements);
        return result;	
    }
	
	private void printFastestPathMovement(String moveString) throws InterruptedException {
		
		// FP|F6|R0|F1|L0|F2
		String[] arr = moveString.split("\\|");
		for(int y = 0;y<arr.length;y++) {
			System.out.println("N:" + arr[y]);
		}
		try {
			for(int i=1;i<arr.length;i++) {
				//System.out.println("U: "+arr[i]+"_"+arr[i].substring(0,1));
				switch(arr[i].substring(0,1)) {
				
				case "F":
					System.out.println("Fmove times:" + Integer.parseInt(arr[i].substring(1,arr[i].length())));
					for(int y=0;y<Integer.parseInt(arr[i].substring(1,arr[i].length()));y++) {					
						this.robot.move(MOVEMENT.FORWARD);
						mGui.paintResult();
						Thread.sleep((long)500);
						//displayToUI();
					}
					break;
				case "R":
					for(int y=0;y<Integer.parseInt(arr[i].substring(1,arr[i].length()));y++) {
						this.robot.turn(MOVEMENT.RIGHT);
						mGui.paintResult();
						Thread.sleep((long)500);
						this.robot.move(MOVEMENT.FORWARD);
						mGui.paintResult();
						Thread.sleep((long)500);
						//displayToUI();
					}
					break;
				case "L":	
					for(int y=0;y<Integer.parseInt(arr[i].substring(1,arr[i].length()));y++) {					
						this.robot.turn(MOVEMENT.LEFT);
						mGui.paintResult();
						Thread.sleep((long)500);
						this.robot.move(MOVEMENT.FORWARD);
						mGui.paintResult();
						Thread.sleep((long)500);
						//displayToUI();
					}
					break;
				case "B":
					for(int y=0;y<Integer.parseInt(arr[i].substring(1,arr[i].length()));y++) {					
						this.robot.turn(MOVEMENT.RIGHT);
						mGui.paintResult();
						Thread.sleep((long)500);
						this.robot.turn(MOVEMENT.RIGHT);
						mGui.paintResult();
						Thread.sleep((long)500);
						this.robot.move(MOVEMENT.FORWARD);
						mGui.paintResult();
						Thread.sleep((long)500);
						//displayToUI();
					}
					break;
				default:
					break;
				}				
			}
		}
		catch(Exception ex) {
			System.out.println("printfastestPathmovement error:" + ex.getMessage());
		}
		
		
		
		
	}
}
