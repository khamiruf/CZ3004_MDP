package gui;

import java.util.ArrayList;

import Communication.TCPComm;
import algorithm.FastestPath;
import constant.Constants;
import constant.Constants.MOVEMENT;
import entity.Cell;
import entity.Map;
import entity.Robot;

public class simulateRealRun implements Runnable {

	private MainGUI mGui;
	private Map exploreMap;
	private Robot robot;
	private TCPComm tcpObj;

	public simulateRealRun(MainGUI mGUI, Robot rBot, Map eMap) {
		this.mGui = mGUI;
		this.exploreMap = eMap;
		this.robot = rBot;
		this.tcpObj = TCPComm.getInstance();
	
	}

	@Override
	public void run() {
		
		mGui.displayMsgToUI("RealRunThread Started!");

			try {
				
				establishCommsToRPI();
				checkandPlotSC();	//waiting for start coord from RPI
				displayToUI();
				mGui.displayMsgToUI("Exploration Started, Waiting for sensor data...");
			
				String recMsg = readMsg();
				int forwardCount = 0;
				sendObstacleInfo(exploreMap.setExploredCells(robot, recMsg));
								
				displayToUI();
				
				do {

					if (robot.isMovementValid(exploreMap, MOVEMENT.RIGHT)) {
						robot.turn(MOVEMENT.RIGHT);
						sendMsg("EX|R0"+ exploreMap.rpiImageString(robot));
						recMsg = readMsg();
						sendObstacleInfo(exploreMap.setExploredCells(robot, recMsg));
						displayToUI(); 
						
						robot.move(MOVEMENT.FORWARD);
						sendMsg("EX|F01"+ exploreMap.rpiImageString(robot));
						recMsg = readMsg();
						sendObstacleInfo(exploreMap.setExploredCells(robot, recMsg));
						forwardCount = centerRepos(forwardCount, recMsg);

					} else if (robot.isMovementValid(exploreMap, MOVEMENT.FORWARD)) {
						forwardCount++;
						robot.move(MOVEMENT.FORWARD);
						sendMsg("EX|F01"+ exploreMap.rpiImageString(robot));
						recMsg = readMsg();
						sendObstacleInfo(exploreMap.setExploredCells(robot, recMsg));
						forwardCount = centerRepos(forwardCount, recMsg);

					} else if (robot.isMovementValid(exploreMap, MOVEMENT.LEFT)) {
						robot.turn(MOVEMENT.LEFT);
					
						sendMsg("EX|L0"+ exploreMap.rpiImageString(robot));
						recMsg = readMsg();
						sendObstacleInfo(exploreMap.setExploredCells(robot, recMsg));
						forwardCount = centerRepos(forwardCount, recMsg);
						
					} else if (robot.isMovementValid(exploreMap, MOVEMENT.BACKWARD)) {
						robot.turn(MOVEMENT.RIGHT);
						
						sendMsg("EX|R0"+ exploreMap.rpiImageString(robot));
						recMsg = readMsg();
						sendObstacleInfo(exploreMap.setExploredCells(robot, recMsg));
						displayToUI();
						
						robot.turn(MOVEMENT.RIGHT);
						
						sendMsg("EX|R0"+ exploreMap.rpiImageString(robot));
						recMsg = readMsg();
						sendObstacleInfo(exploreMap.setExploredCells(robot, recMsg));
						displayToUI();
						
						robot.move(MOVEMENT.FORWARD);
					
						sendMsg("EX|F01"+ exploreMap.rpiImageString(robot));
						recMsg = readMsg();
						sendObstacleInfo(exploreMap.setExploredCells(robot, recMsg));
						forwardCount = centerRepos(forwardCount, recMsg);
					}
					else {
						//No Valid movement, therefore throw an exception to display error msg & close connection		
						throw new Exception("No Valid Movement for Exploration! kill thread...");
					}
					displayToUI(); 

				} while (!exploreMap.getStartGoalPosition().getExploredState()
						|| !exploreMap.getEndGoalPosition().getExploredState()
						|| !exploreMap.checkIfRobotAtStartPos(robot));

				ArrayList<Cell> unexploredList = getUnexploredList(exploreMap);
				while (unexploredList.size() > 0) {

					FastestPath fastobj = new FastestPath(robot, exploreMap);
					ArrayList<Cell> cellStep = fastobj.calculateFastestPath2(exploreMap,
							unexploredList.get(0).getRowPos(), unexploredList.get(0).getColPos());
					if (cellStep == null) {
						unexploredList.remove(0);
					} else {
						printMovement(cellStep,true);
						unexploredList = updateUnexploreList(unexploredList);
					}
				}

			
				if (!exploreMap.checkIfRobotAtStartPos(robot)) {
					FastestPath fastobj = new FastestPath(robot, exploreMap);
					ArrayList<Cell> cellStep = fastobj.calculateFastestPath(exploreMap,
							exploreMap.getStartGoalPosition().getRowPos(),
							exploreMap.getStartGoalPosition().getColPos());
					printMovement(cellStep,false);

				}
				
				
				
				//============== Calibrating Robot to face North ============
				faceNorthDirection();
				sendMsg("N|"); 	//Send to Arduino to signify end of exploration		
				
				//============== Sending MDF to Android ===========================
				String mdfvalue1 = exploreMap.getMDF1();
				String mdfvalue2 = exploreMap.getMDF2();
				sendMDFInfo(mdfvalue1,mdfvalue2); // Sending MDF1&2 to RPI
				mGui.printFinal(); // Print the final map that robot knows on system console
				mGui.displayMsgToUI("MDF1: " + exploreMap.getMDF1());
				mGui.displayMsgToUI("MDF2: " + exploreMap.getMDF2());
				System.out.println("MDF1: " + exploreMap.getMDF1());
				System.out.println("MDF2: " + exploreMap.getMDF2());
				
				
				//==================== Fastest Path =========================
				 mGui.displayMsgToUI("Starting Fastest Path..");
				 /*
				 FastestPath fastestPath = new FastestPath(this.robot, exploreMap);
				 ArrayList<Cell> cellsInPath  = fastestPath.calculateFastestPath(exploreMap, exploreMap.getWayPoint().getRowPos(), exploreMap.getWayPoint().getColPos());
				 sendMsg(convertCellsToMovements(cellsInPath));
				 printMovement(cellsInPath, false);
				
				 fastestPath = new FastestPath(this.robot, exploreMap);
				 cellsInPath = fastestPath.calculateFastestPath(exploreMap, exploreMap.getEndGoalPosition().getRowPos(), exploreMap.getEndGoalPosition().getColPos());
				 sendMsg(convertCellsToMovements(cellsInPath));
			     printMovement(cellsInPath, false);
				*/
				FastestPath fastestPath = new FastestPath(this.robot,exploreMap);
				ArrayList<Cell> cellsInPath=fastestPath.findAllWPEndPaths(exploreMap);	
				String movementString = convertCellsToMovements(cellsInPath); //Generate movement string based on cell list.
				waitForFastestPath();	   // Waiting for fastest path command 
				sendMsg(movementString);
				
			} catch (InterruptedException e) {
				System.out.println("RealRun thread InterruptedException" + e.getMessage());
				//mGui.displayMsgToUI("**********   RealRun Thread Interrupted!   **********");
				tcpObj.closeConnection();
				
			}
			catch(Exception e) {
				System.out.println("RealRun thread exception.."+ e.getMessage());
				//mGui.displayMsgToUI("RealRun thread Exception Error: " + e.getMessage());
				tcpObj.closeConnection();
				
			}
				
		mGui.displayMsgToUI("**********   RealRun Thread Ended Successfully!  ************ ");
	}


	/**
	 * This method move and send movement commands meant for fastest path. 
	 * @param cellStep ArrayList of cells the robot need to move to.
	 * @param setSensorData To accept sensor data and send obstacle data to android. TRUE is to send. FALSE to ignore sensor
	 * @throws InterruptedException
	 */
		private void printMovement(ArrayList<Cell> cellStep, boolean setSensorData) throws InterruptedException {
			int currRow = robot.getPosRow();
			int currCol = robot.getPosCol();
			String recMsg="";
			int forwardCount = 0;
			for (int i = 0; i < cellStep.size(); i++) {
				int destRow = cellStep.get(i).getRowPos();
				int destCol = cellStep.get(i).getColPos();
				switch (robot.getCurrDir()) {
				case NORTH:
					if (currCol == destCol) {
						if (currRow < destRow) {
							forwardCount++;
							robot.move(MOVEMENT.FORWARD);
							sendMsg("EX|F01"+ exploreMap.rpiImageString(robot));
							recMsg = readMsg();
							forwardCount = centerRepos(forwardCount, recMsg);
							
						} else if (currRow > destRow) {
							robot.turn(MOVEMENT.RIGHT);
							sendMsg("EX|R0"+ exploreMap.rpiImageString(robot));
							recMsg = readMsg();
							if(setSensorData) {sendObstacleInfo(exploreMap.setExploredCells(robot, recMsg));}
							displayToUI();
							
							robot.turn(MOVEMENT.RIGHT);
							sendMsg("EX|R0"+ exploreMap.rpiImageString(robot));
							recMsg = readMsg();
							if(setSensorData) {sendObstacleInfo(exploreMap.setExploredCells(robot, recMsg));}
							displayToUI();
							
							robot.move(MOVEMENT.FORWARD);
							sendMsg("EX|F01"+ exploreMap.rpiImageString(robot));
							recMsg = readMsg();
							forwardCount = centerRepos(forwardCount, recMsg);
						}
					} else if (currRow == destRow) {
						if (currCol < destCol) {
							robot.turn(MOVEMENT.RIGHT);
							sendMsg("EX|R0"+ exploreMap.rpiImageString(robot));
							recMsg = readMsg();
							if(setSensorData) {sendObstacleInfo(exploreMap.setExploredCells(robot, recMsg));}
							displayToUI();
							
							robot.move(MOVEMENT.FORWARD);
							sendMsg("EX|F01"+ exploreMap.rpiImageString(robot));
							recMsg = readMsg();
							forwardCount = centerRepos(forwardCount, recMsg);

						} else if (currCol > destCol) {
							robot.turn(MOVEMENT.LEFT);
							sendMsg("EX|L0"+ exploreMap.rpiImageString(robot));
							recMsg = readMsg();
							if(setSensorData) {sendObstacleInfo(exploreMap.setExploredCells(robot, recMsg));}
							displayToUI();
							
							robot.move(MOVEMENT.FORWARD);
							sendMsg("EX|F01"+ exploreMap.rpiImageString(robot));
							recMsg = readMsg();
							forwardCount = centerRepos(forwardCount, recMsg);
						}
					}
					break;
				case SOUTH:
					if (currCol == destCol) {
						if (currRow < destRow) {
							robot.turn(MOVEMENT.RIGHT);
							sendMsg("EX|R0"+ exploreMap.rpiImageString(robot));
							recMsg = readMsg();
							if(setSensorData) {sendObstacleInfo(exploreMap.setExploredCells(robot, recMsg));}
							displayToUI();
							
							robot.turn(MOVEMENT.RIGHT);
							sendMsg("EX|R0"+ exploreMap.rpiImageString(robot));
							recMsg = readMsg();
							if(setSensorData) {sendObstacleInfo(exploreMap.setExploredCells(robot, recMsg));}
							displayToUI();
							
							robot.move(MOVEMENT.FORWARD);
							sendMsg("EX|F01"+ exploreMap.rpiImageString(robot));
							recMsg = readMsg();
							forwardCount = centerRepos(forwardCount, recMsg);
							
						} else if (currRow > destRow) {
							forwardCount++;
							robot.move(MOVEMENT.FORWARD);
							sendMsg("EX|F01"+ exploreMap.rpiImageString(robot));
							recMsg = readMsg();
							forwardCount = centerRepos(forwardCount, recMsg);
						}
					} else if (currRow == destRow) {
						if (currCol < destCol) {
							robot.turn(MOVEMENT.LEFT);
							sendMsg("EX|L0"+ exploreMap.rpiImageString(robot));
							recMsg = readMsg();
							if(setSensorData) {sendObstacleInfo(exploreMap.setExploredCells(robot, recMsg));}
							displayToUI();
							
							robot.move(MOVEMENT.FORWARD);
							sendMsg("EX|F01"+ exploreMap.rpiImageString(robot));
							recMsg = readMsg();
							forwardCount = centerRepos(forwardCount, recMsg);
						} else if (currCol > destCol) {
							robot.turn(MOVEMENT.RIGHT);
							sendMsg("EX|R0"+ exploreMap.rpiImageString(robot));
							recMsg = readMsg();
							if(setSensorData) {sendObstacleInfo(exploreMap.setExploredCells(robot, recMsg));}
							displayToUI();
							
							robot.move(MOVEMENT.FORWARD);
							sendMsg("EX|F01"+ exploreMap.rpiImageString(robot));
							recMsg = readMsg();
							forwardCount = centerRepos(forwardCount, recMsg);
						}
					}
					break;
				case EAST:
					if (currCol == destCol) {
						if (currRow < destRow) {
							robot.turn(MOVEMENT.LEFT);
							sendMsg("EX|L0"+ exploreMap.rpiImageString(robot));
							recMsg = readMsg();
							if(setSensorData) {sendObstacleInfo(exploreMap.setExploredCells(robot, recMsg));}
							displayToUI();
							
							robot.move(MOVEMENT.FORWARD);
							sendMsg("EX|F01"+ exploreMap.rpiImageString(robot));
							recMsg = readMsg();
							forwardCount = centerRepos(forwardCount, recMsg);
						} else if (currRow > destRow) {
							robot.turn(MOVEMENT.RIGHT);
							sendMsg("EX|R0"+ exploreMap.rpiImageString(robot));
							recMsg = readMsg();
							if(setSensorData) {sendObstacleInfo(exploreMap.setExploredCells(robot, recMsg));}
							displayToUI();
							
							robot.move(MOVEMENT.FORWARD);
							sendMsg("EX|F01"+ exploreMap.rpiImageString(robot));
							recMsg = readMsg();
							forwardCount = centerRepos(forwardCount, recMsg);
						}
					} else if (currRow == destRow) {
						if (currCol < destCol) {
							forwardCount++;
							robot.move(MOVEMENT.FORWARD);
							sendMsg("EX|F01"+ exploreMap.rpiImageString(robot));
							recMsg = readMsg();
							forwardCount = centerRepos(forwardCount, recMsg);
						} else if (currCol > destCol) {
							robot.turn(MOVEMENT.RIGHT);
							sendMsg("EX|R0"+ exploreMap.rpiImageString(robot));
							recMsg = readMsg();
							if(setSensorData) {
								sendObstacleInfo(exploreMap.setExploredCells(robot, recMsg));
							}
							displayToUI();
							
							robot.turn(MOVEMENT.RIGHT);
							sendMsg("EX|R0"+ exploreMap.rpiImageString(robot));
							recMsg = readMsg();
							if(setSensorData) {sendObstacleInfo(exploreMap.setExploredCells(robot, recMsg));}
							displayToUI();
							
							robot.move(MOVEMENT.FORWARD);
							sendMsg("EX|F01"+ exploreMap.rpiImageString(robot));
							recMsg = readMsg();
							forwardCount = centerRepos(forwardCount, recMsg);
						}
					}
					break;
				case WEST:
					if (currCol == destCol) {
						if (currRow < destRow) {
							robot.turn(MOVEMENT.RIGHT);
							sendMsg("EX|R0"+ exploreMap.rpiImageString(robot));
							recMsg = readMsg();
							if(setSensorData) {sendObstacleInfo(exploreMap.setExploredCells(robot, recMsg));}
							displayToUI();
							
							robot.move(MOVEMENT.FORWARD);
							sendMsg("EX|F01"+ exploreMap.rpiImageString(robot));
							recMsg = readMsg();
							forwardCount = centerRepos(forwardCount, recMsg);
						} else if (currRow > destRow) {
							robot.turn(MOVEMENT.LEFT);
							sendMsg("EX|L0"+ exploreMap.rpiImageString(robot));
							recMsg = readMsg();
							if(setSensorData) {sendObstacleInfo(exploreMap.setExploredCells(robot, recMsg));}
							displayToUI();
							
							robot.move(MOVEMENT.FORWARD);
							sendMsg("EX|F01"+ exploreMap.rpiImageString(robot));
							recMsg = readMsg();
							forwardCount = centerRepos(forwardCount, recMsg);
						}
					} else if (currRow == destRow) {
						if (currCol < destCol) {
							robot.turn(MOVEMENT.RIGHT);
							sendMsg("EX|R0"+ exploreMap.rpiImageString(robot));
							recMsg = readMsg();
							if(setSensorData) {sendObstacleInfo(exploreMap.setExploredCells(robot, recMsg));}
							displayToUI();
							
							robot.turn(MOVEMENT.RIGHT);
							sendMsg("EX|R0"+ exploreMap.rpiImageString(robot));
							recMsg = readMsg();
							if(setSensorData) {sendObstacleInfo(exploreMap.setExploredCells(robot, recMsg));}
							displayToUI();
							
							robot.move(MOVEMENT.FORWARD);
							sendMsg("EX|F01"+ exploreMap.rpiImageString(robot));
							recMsg = readMsg();
							forwardCount = centerRepos(forwardCount, recMsg);
							
						} else if (currCol > destCol) {
							forwardCount++;
							robot.move(MOVEMENT.FORWARD);
							sendMsg("EX|F01"+ exploreMap.rpiImageString(robot));
							recMsg = readMsg();
							forwardCount = centerRepos(forwardCount, recMsg);
						}
					}
					break;
				}
			
				recMsg = readMsg();
				if(setSensorData) {sendObstacleInfo(exploreMap.setExploredCells(robot, recMsg));}
				displayToUI(); 

				currRow = robot.getPosRow();
				currCol = robot.getPosCol();
				
			}

		}
		
		private void printFastestPathMovement(String moveString) throws InterruptedException {
			
			// FP|F6|R0|F1|L0|F2
			String[] arr = moveString.split("\\|");
			
			for(int i=1;i<arr.length;i++) {
				switch(arr[i].substring(0,1)) {
					
				case "F":
					for(int y=0;y<Integer.parseInt(arr[i].substring(1,arr[i].length()));y++) {					
						this.robot.move(MOVEMENT.FORWARD);
						mGui.paintResult();
						Thread.sleep((long)500);	
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
							
						}
						break;
					default:
						break;
					}				
				}
			
			
	}
		
	private ArrayList<Cell> getUnexploredList(Map exploredMap) {

		ArrayList<Cell> unexploredList = new ArrayList<Cell>();

		for (int r = 0; r < Constants.MAX_ROW; r++) {
			for (int c = 0; c < Constants.MAX_COL; c++) {
				Cell cell = exploredMap.getMapGrid()[r][c];
				if (cell.getExploredState() == false) {
					unexploredList.add(cell);
				}
			}
		}
		return unexploredList;

	}

	private ArrayList<Cell> updateUnexploreList(ArrayList<Cell> clist) {
		for (int i = 0; i < clist.size(); i++) {
			Cell cObj = clist.get(i);
			if (exploreMap.getMapGrid()[cObj.getRowPos()][cObj.getColPos()].getExploredState()) {
				clist.remove(cObj);
				i--;
			}
		}
		return clist;
	}
	
	// This method is for calibrating Robot to face North
	private void faceNorthDirection() throws InterruptedException {
		mGui.displayMsgToUI("Calibrating ROBOT..!");
		switch(robot.getCurrDir()) {
		case NORTH: break;
		case SOUTH:
			this.robot.turn(MOVEMENT.RIGHT);
			sendMsg("EX|R0"+ exploreMap.rpiImageString(robot));
			readMsg();
			displayToUI();	 
			
			this.robot.turn(MOVEMENT.RIGHT);
			sendMsg("EX|R0"+ exploreMap.rpiImageString(robot));
			readMsg();
			displayToUI();
			
			break;
		case EAST: 
			this.robot.turn(MOVEMENT.LEFT);
			sendMsg("EX|L0"+ exploreMap.rpiImageString(robot));
			readMsg();
			displayToUI();
			break;
		case WEST:
			this.robot.turn(MOVEMENT.RIGHT);
			sendMsg("EX|R0"+ exploreMap.rpiImageString(robot));
			readMsg();
			displayToUI();	
			break;
		}
		
	}
	
	//============================ FastestPath =======================================
	
	public String parseFPMovement(ArrayList<MOVEMENT> fastestPathMovements){
        int i = 0;
        int j= 0;
        int counter = 1;
        String result = "FP|";
    
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
                           
       }
        System.out.println("parseFPMovements():" +result);
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
                        	mBot.turn(MOVEMENT.RIGHT); 
                        	mBot.turn(MOVEMENT.RIGHT);
                        	mBot.move(MOVEMENT.FORWARD);}
                    }
                    else if(currRow == destRow){
                        if(currCol < destCol){
                        	fastestPathMovements.add(MOVEMENT.RIGHT); 
                        	mBot.turn(MOVEMENT.RIGHT); 
                        	mBot.move(MOVEMENT.FORWARD);}
                        else if(currCol > destCol){
                        	fastestPathMovements.add(MOVEMENT.LEFT); 
                        	mBot.turn(MOVEMENT.LEFT); 
                        	mBot.move(MOVEMENT.FORWARD);}
                    }
                    break;
                case SOUTH:
                    if(currCol == destCol){
                        if(currRow < destRow){
                        	fastestPathMovements.add(MOVEMENT.BACKWARD); 
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
                        	mBot.turn(MOVEMENT.LEFT);
                        	mBot.move(MOVEMENT.FORWARD);}
                        else if(currCol > destCol){
                        	fastestPathMovements.add(MOVEMENT.RIGHT); 
                        	mBot.turn(MOVEMENT.RIGHT); 
                        	mBot.move(MOVEMENT.FORWARD);}
                    }
                    break;
                case EAST:
                    if(currCol == destCol){
                        if(currRow < destRow){
                        	fastestPathMovements.add(MOVEMENT.LEFT); 
                        	mBot.turn(MOVEMENT.LEFT);
                        	mBot.move(MOVEMENT.FORWARD);}
                        else if(currRow > destRow){
                        	fastestPathMovements.add(MOVEMENT.RIGHT); 
                        	mBot.turn(MOVEMENT.RIGHT); 
                        	mBot.move(MOVEMENT.FORWARD);}
                    }
                    else if(currRow == destRow){
                        if(currCol < destCol){
                        	fastestPathMovements.add(MOVEMENT.FORWARD); 
                        	mBot.move(MOVEMENT.FORWARD);}
                        else if(currCol > destCol){
                        	fastestPathMovements.add(MOVEMENT.BACKWARD); 
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
                    	mBot.turn(MOVEMENT.RIGHT); 
                    	mBot.move(MOVEMENT.FORWARD);
                    }
                    else if(currRow > destRow){
                    	fastestPathMovements.add(MOVEMENT.LEFT); 
                    	mBot.turn(MOVEMENT.LEFT); 
                    	mBot.move(MOVEMENT.FORWARD);
                    	}
                }
                else if(currRow == destRow){
                    if(currCol < destCol){
                    	fastestPathMovements.add(MOVEMENT.BACKWARD);
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
	
	
	//======================= GUI PAINTING ===================================
	
	//Painting the current map result to GUI with delay
	private void displayToUI() throws InterruptedException {

		mGui.paintResult();
		//Thread.sleep((long) (0.1 * 1000));
	}
	
	// ==================== Communication Methods with RPI ==========================
	
	//Establish connection with RPI
	private void establishCommsToRPI() throws InterruptedException{
		String msg ="";
		
		do {
			mGui.displayMsgToUI("Establishing connection to RPI..  :D ");
			msg = this.tcpObj.establishConnection();
			if(msg.length()!=0) {
				mGui.displayMsgToUI(msg);
				
				Thread.sleep((long) (1 * 1000));
			
			}
			else {
				mGui.displayMsgToUI("Connected Successfully :DD ");
					break;
			}
		
		}while(!Thread.currentThread().isInterrupted());			
	}
	/*
	private String readMsg() throws InterruptedException {
		String msg = "";
		msg = tcpObj.readMessage();
		mGui.displayMsgToUI("Received: " + msg);
		return msg;
	}
	*/
	//*
	private String readMsg() throws InterruptedException{
	//private String readMsg(){
		String msg ="";
		
		do {
			//if(!Thread.currentThread().isInterrupted()) {
				msg = tcpObj.readMessage();		
			//}
			//else {
			//	throw new InterruptedException("realrun readMsg() interrupt");
			//}
			
		}while(msg==null||msg.length()==0);
		
		mGui.displayMsgToUI("Received: " + msg);
		return msg;
	}
	//*/
	
	//Send String message to RPI
	private void sendMsg(String msg) {
		String rmsg = tcpObj.sendMessage(msg);
		mGui.displayMsgToUI("Sent: " + rmsg);
		
	}
	
	// plot robot according to start coordinates received from RPI
	private void checkandPlotSC() throws Exception {
		String rmsg ="";
		// SC|[1,1]
		mGui.displayMsgToUI("Waiting for start coordinate...");
		do {
			
			 rmsg = readMsg();	
			 if(rmsg.substring(0,3).equals("SC|")) {
				 String[] arr = rmsg.substring(4,7).split(",");
				this.robot.setPosRow(Integer.parseInt(arr[0]));		 
				this.robot.setPosCol(Integer.parseInt(arr[1]));
				return;
			 }
			
		}while(true);
		
		
	}
	//Waiting for fastest path command from RPI
	private void waitForFastestPath() throws Exception{
		String rmsg="";
		mGui.displayMsgToUI("Waiting for command to start FastestPath...");
			do {
				
				 rmsg = readMsg();	
				 if(rmsg.substring(0,3).equals("FP|")) {
					
					return;
				 }
				
			}while(true);
	}
	
	//Plot the WayPoint coordinates received from RPI
	private void checkandPlotWP() throws Exception {
		String rmsg ="";
		// WP|[1,1]  [row ,col]
		mGui.displayMsgToUI("Waiting for WayPoint coordinate...");
		do {
			
			 rmsg = readMsg();	
			 if(rmsg.substring(0,3).equals("WP|")) {
				 String[] arr = rmsg.substring(4,7).split(",");
				 this.exploreMap.setWayPoint(Integer.parseInt(arr[0]), Integer.parseInt(arr[1]));
			
				return;
			 }
			
		}while(true);
		
	}

	//function to check conditions for reposition robot into middle of 3x3
	private int centerRepos(int forwardCount, String sensorDataInString){
		int rightFront = Character.getNumericValue(sensorDataInString.charAt(1));
		int rightBack = Character.getNumericValue(sensorDataInString.charAt(0));
		
		if(forwardCount >= 3 && rightFront == 1 && rightBack == 1){
			sendMsg("EX|P00");
			return 0;
		}

		return forwardCount;
	}

	
	//==================== Communication to android =======================
	
	// Method for sending obstacles location
	private void sendObstacleInfo(String rmsg) {
		
		if(rmsg.length()!=0) {
			rmsg = "OB|" + rmsg;
			sendMsg(rmsg);
		}
		
		
	}
	//Method for sending MDF1 & MDF2 
	private void sendMDFInfo(String mdf1, String mdf2) {
		sendMsg("MDF|"+mdf1+"|"+mdf2);
		
	}
	
}
