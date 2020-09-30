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
		this.tcpObj = new TCPComm();
		//tcpObj = TCPComm.getInstance();
		//tcpObj.establishConnection(mGui);

	}

	@Override
	public void run() {
		
		mGui.displayMsgToUI("RealRun Started!");

		while (!Thread.currentThread().isInterrupted()) {
			try {
				String obst ="";
				
				establishCommsToRPI();
								
				mGui.displayMsgToUI("Waiting for start coordinate...");
				//String recMsg = tcpObj.readMessage();
				String recMsg = readMsg();
				mGui.displayMsgToUI("Exploration Started, Waiting for sensor data... : ");
				//recMsg = tcpObj.readMessage();
				recMsg = readMsg();
				sendToAndroid(exploreMap.setExploredCells(robot, recMsg));
				
				//Thread.sleep((long) (0.5 * 1000));
				displayToUI();
				
				do {

					if (robot.isMovementValid(exploreMap, MOVEMENT.RIGHT)) {
						robot.turn(MOVEMENT.RIGHT);
						//tcpObj.sendMessage("EX|R0");
						//recMsg = tcpObj.readMessage();
						sendMsg("EX|R0");
						recMsg = readMsg();
						sendToAndroid(exploreMap.setExploredCells(robot, recMsg));
						displayToUI(); 
						
						robot.move(MOVEMENT.FORWARD);
						//tcpObj.sendMessage("EX|F01");
						//recMsg = tcpObj.readMessage();
						sendMsg("EX|F01");
						recMsg = readMsg();
						sendToAndroid(exploreMap.setExploredCells(robot, recMsg));

					} else if (robot.isMovementValid(exploreMap, MOVEMENT.FORWARD)) {
						robot.move(MOVEMENT.FORWARD);
						//tcpObj.sendMessage("EX|F01");
						//recMsg = tcpObj.readMessage();
						sendMsg("EX|F01");
						recMsg = readMsg();
						sendToAndroid(exploreMap.setExploredCells(robot, recMsg));

					} else if (robot.isMovementValid(exploreMap, MOVEMENT.LEFT)) {
						robot.turn(MOVEMENT.LEFT);
						//tcpObj.sendMessage("EX|L0");
						//recMsg = tcpObj.readMessage();
						sendMsg("EX|L0");
						recMsg = readMsg();
						sendToAndroid(exploreMap.setExploredCells(robot, recMsg));
						
					} else if (robot.isMovementValid(exploreMap, MOVEMENT.BACKWARD)) {
						robot.turn(MOVEMENT.RIGHT);
						//tcpObj.sendMessage("EX|R0");
						//recMsg = tcpObj.readMessage();
						sendMsg("EX|R0");
						recMsg = readMsg();
						sendToAndroid(exploreMap.setExploredCells(robot, recMsg));
						displayToUI(); // repaint GUI to show turning
						
						robot.turn(MOVEMENT.RIGHT);
						//tcpObj.sendMessage("EX|R0");
						//recMsg = tcpObj.readMessage();
						sendMsg("EX|R0");
						recMsg = readMsg();
						sendToAndroid(exploreMap.setExploredCells(robot, recMsg));
						displayToUI();
						
						robot.move(MOVEMENT.FORWARD);
						//tcpObj.sendMessage("EX|F01");
						//recMsg = tcpObj.readMessage();
						sendMsg("EX|F01");
						recMsg = readMsg();
						sendToAndroid(exploreMap.setExploredCells(robot, recMsg));

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
						

						int currRow = robot.getPosRow();
						int currCol = robot.getPosCol();

						for (int i = 0; i < cellStep.size(); i++) {
							int destRow = cellStep.get(i).getRowPos();
							int destCol = cellStep.get(i).getColPos();
							switch (robot.getCurrDir()) {
							case NORTH:
								if (currCol == destCol) {
									if (currRow < destRow) {
										robot.move(MOVEMENT.FORWARD);
										sendMsg("EX|F01");
									} else if (currRow > destRow) {
										robot.turn(MOVEMENT.RIGHT);
										sendMsg("EX|R0");
										recMsg = readMsg();
										sendToAndroid(exploreMap.setExploredCells(robot, recMsg));
										displayToUI();
										
										robot.turn(MOVEMENT.RIGHT);
										sendMsg("EX|R0");
										recMsg = readMsg();
										sendToAndroid(exploreMap.setExploredCells(robot, recMsg));
										displayToUI();
										
										robot.move(MOVEMENT.FORWARD);
										sendMsg("EX|F01");
									}
								} else if (currRow == destRow) {
									if (currCol < destCol) {
										robot.turn(MOVEMENT.RIGHT);
										sendMsg("EX|R0");
										recMsg = readMsg();
										sendToAndroid(exploreMap.setExploredCells(robot, recMsg));
										displayToUI();
										
										robot.move(MOVEMENT.FORWARD);
										sendMsg("EX|F01");
									} else if (currCol > destCol) {
										robot.turn(MOVEMENT.LEFT);
										sendMsg("EX|L0");
										recMsg = readMsg();
										sendToAndroid(exploreMap.setExploredCells(robot, recMsg));
										displayToUI();
										
										robot.move(MOVEMENT.FORWARD);
										sendMsg("EX|F01");
									}
								}
								break;
							case SOUTH:
								if (currCol == destCol) {
									if (currRow < destRow) {
										robot.turn(MOVEMENT.RIGHT);
										sendMsg("EX|R0");
										recMsg = readMsg();
										sendToAndroid(exploreMap.setExploredCells(robot, recMsg));
										displayToUI();
										
										robot.turn(MOVEMENT.RIGHT);
										sendMsg("EX|R0");
										recMsg = readMsg();
										sendToAndroid(exploreMap.setExploredCells(robot, recMsg));
										displayToUI();
										
										robot.move(MOVEMENT.FORWARD);
										sendMsg("EX|F01");
										
									} else if (currRow > destRow) {
										robot.move(MOVEMENT.FORWARD);
										sendMsg("EX|F01");
									}
								} else if (currRow == destRow) {
									if (currCol < destCol) {
										robot.turn(MOVEMENT.LEFT);
										sendMsg("EX|L0");
										recMsg = readMsg();
										sendToAndroid(exploreMap.setExploredCells(robot, recMsg));
										displayToUI();
										
										robot.move(MOVEMENT.FORWARD);
										sendMsg("EX|F01");
										
									} else if (currCol > destCol) {
										robot.turn(MOVEMENT.RIGHT);
										sendMsg("EX|R0");
										recMsg = readMsg();
										sendToAndroid(exploreMap.setExploredCells(robot, recMsg));
										displayToUI();
										
										robot.move(MOVEMENT.FORWARD);
										sendMsg("EX|F01");
									}
								}
								break;
							case EAST:
								if (currCol == destCol) {
									if (currRow < destRow) {
										robot.turn(MOVEMENT.LEFT);
										sendMsg("EX|L0");
										recMsg = readMsg();
										sendToAndroid(exploreMap.setExploredCells(robot, recMsg));
										
										displayToUI();
										
										robot.move(MOVEMENT.FORWARD);
										sendMsg("EX|F01");
										
									} else if (currRow > destRow) {
										robot.turn(MOVEMENT.RIGHT);
										sendMsg("EX|R0");
										recMsg = readMsg();
										sendToAndroid(exploreMap.setExploredCells(robot, recMsg));
										displayToUI();
										
										robot.move(MOVEMENT.FORWARD);
										sendMsg("EX|F01");
									}
								} else if (currRow == destRow) {
									if (currCol < destCol) {
										robot.move(MOVEMENT.FORWARD);
										sendMsg("EX|F01");
									} else if (currCol > destCol) {
										robot.turn(MOVEMENT.RIGHT);
										sendMsg("EX|R0");
										recMsg = readMsg();
										sendToAndroid(exploreMap.setExploredCells(robot, recMsg));
										displayToUI();
										
										robot.turn(MOVEMENT.RIGHT);
										sendMsg("EX|R0");
										recMsg = readMsg();
										sendToAndroid(exploreMap.setExploredCells(robot, recMsg));
										displayToUI();
										
										robot.move(MOVEMENT.FORWARD);
										sendMsg("EX|F01");
									}
								}
								break;
							case WEST:
								if (currCol == destCol) {
									if (currRow < destRow) {
										robot.turn(MOVEMENT.RIGHT);
										sendMsg("EX|R0");
										recMsg = readMsg();
										sendToAndroid(exploreMap.setExploredCells(robot, recMsg));
										displayToUI();
										
										robot.move(MOVEMENT.FORWARD);
										sendMsg("EX|F01");
							
									} else if (currRow > destRow) {
										robot.turn(MOVEMENT.LEFT);
										sendMsg("EX|L0");
										recMsg = readMsg();
										sendToAndroid(exploreMap.setExploredCells(robot, recMsg));
										displayToUI();
										
										robot.move(MOVEMENT.FORWARD);
										sendMsg("EX|F01");
									}
								} else if (currRow == destRow) {
									if (currCol < destCol) {
										robot.turn(MOVEMENT.RIGHT);
										sendMsg("EX|R0");
										recMsg = readMsg();
										sendToAndroid(exploreMap.setExploredCells(robot, recMsg));
										displayToUI();
										
										robot.turn(MOVEMENT.RIGHT);
										sendMsg("EX|R0");
										recMsg = readMsg();
										sendToAndroid(exploreMap.setExploredCells(robot, recMsg));
										displayToUI();
										
										robot.move(MOVEMENT.FORWARD);
										sendMsg("EX|F01");
									} else if (currCol > destCol) {
										robot.move(MOVEMENT.FORWARD);
										sendMsg("EX|F01");
									}
								}
								break;
							}
							//recMsg = tcpObj.readMessage();
							recMsg = readMsg();
							sendToAndroid(exploreMap.setExploredCells(robot, recMsg));
							displayToUI(); // repaint GUI to show robot movement on map

							currRow = robot.getPosRow();
							currCol = robot.getPosCol();
							
						}

						unexploredList = updateUnexploreList(unexploredList);
					}
				}

			
				if (!exploreMap.checkIfRobotAtStartPos(robot)) {
					FastestPath fastobj = new FastestPath(robot, exploreMap);
					ArrayList<Cell> cellStep = fastobj.calculateFastestPath(exploreMap,
							exploreMap.getStartGoalPosition().getRowPos(),
							exploreMap.getStartGoalPosition().getColPos());
					printMovement(cellStep);

				}
				
				mGui.printFinal(); // Print the final map that robot knows on system console
				mGui.displayMsgToUI("MDF1: " + exploreMap.getMDF1());
				mGui.displayMsgToUI("MDF2: " + exploreMap.getMDF2());
				System.out.println("MDF1: " + exploreMap.getMDF1());
				System.out.println("MDF2: " + exploreMap.getMDF2());
				
				//=========== Calibrating Robot to face North ============
				faceNorthDirection();
				
				//==================== Fastest Path =========================
				 mGui.displayMsgToUI("Starting Fastest Path..");
				 FastestPath fastestPath = new FastestPath(this.robot, exploreMap);
				 ArrayList<Cell> cellsInPath  = fastestPath.calculateFastestPath(exploreMap, exploreMap.getWayPoint().getRowPos(), exploreMap.getWayPoint().getColPos());
				 sendMsg(convertCellsToMovements(cellsInPath));
				 printMoveONLY(cellsInPath);
				
				 fastestPath = new FastestPath(this.robot, exploreMap);
				 cellsInPath = fastestPath.calculateFastestPath(exploreMap, exploreMap.getEndGoalPosition().getRowPos(), exploreMap.getEndGoalPosition().getColPos());
				 sendMsg(convertCellsToMovements(cellsInPath));
			     printMoveONLY(cellsInPath);
				
				
				break; // break out from thread

			} catch (InterruptedException e) {
				mGui.displayMsgToUI("**********   ReadRun Thread Interrupted!   **********");

				return;
			}

		}
		
		
		mGui.displayMsgToUI("**********   RealRun Thread Ended Successfully!  ************ ");
	}

	//This method is movement of robot WITHOUT reading sensors value
	private void printMovement(ArrayList<Cell> cellStep) throws InterruptedException {
		int currRow = robot.getPosRow();
		int currCol = robot.getPosCol();

		for (int i = 0; i < cellStep.size(); i++) {
			int destRow = cellStep.get(i).getRowPos();
			int destCol = cellStep.get(i).getColPos();
			switch (robot.getCurrDir()) {
			case NORTH:
				if (currCol == destCol) {
					if (currRow < destRow) {

						robot.move(MOVEMENT.FORWARD);
						sendMsg("EX|F01");
					} else if (currRow > destRow) {
						robot.turn(MOVEMENT.RIGHT);
						sendMsg("EX|R0");
						readMsg();
						displayToUI();
						
						robot.turn(MOVEMENT.RIGHT);
						sendMsg("EX|R0");
						readMsg();
						displayToUI();
						
						robot.move(MOVEMENT.FORWARD);
						sendMsg("EX|F01");
					}
				} else if (currRow == destRow) {
					if (currCol < destCol) {
						robot.turn(MOVEMENT.RIGHT);
						sendMsg("EX|R0");
						readMsg();
						displayToUI();
						
						robot.move(MOVEMENT.FORWARD);
						sendMsg("EX|F01");
					} else if (currCol > destCol) {
						robot.turn(MOVEMENT.LEFT);
						sendMsg("EX|L0");
						readMsg();
						displayToUI();
						
						robot.move(MOVEMENT.FORWARD);
						sendMsg("EX|F01");
					}
				}
				break;
			case SOUTH:
				if (currCol == destCol) {
					if (currRow < destRow) {
						robot.turn(MOVEMENT.RIGHT);
						sendMsg("EX|R0");
						readMsg();
						displayToUI();
						
						robot.turn(MOVEMENT.RIGHT);
						sendMsg("EX|R0");
						readMsg();
						displayToUI();
						
						robot.move(MOVEMENT.FORWARD);
						sendMsg("EX|F01");
						
					} else if (currRow > destRow) {
						robot.move(MOVEMENT.FORWARD);
						sendMsg("EX|F01");
					}
				} else if (currRow == destRow) {
					if (currCol < destCol) {
						robot.turn(MOVEMENT.LEFT);
						sendMsg("EX|L0");
						readMsg();
						displayToUI();
						
						robot.move(MOVEMENT.FORWARD);
						sendMsg("EX|F01");
					} else if (currCol > destCol) {
						robot.turn(MOVEMENT.RIGHT);
						sendMsg("EX|R0");
						readMsg();
						displayToUI();
						
						robot.move(MOVEMENT.FORWARD);
						sendMsg("EX|F01");
					}
				}
				break;
			case EAST:
				if (currCol == destCol) {
					if (currRow < destRow) {
						robot.turn(MOVEMENT.LEFT);
						sendMsg("EX|L0");
						readMsg();
						displayToUI();
						
						robot.move(MOVEMENT.FORWARD);
						sendMsg("EX|F01");
					} else if (currRow > destRow) {
						robot.turn(MOVEMENT.RIGHT);
						sendMsg("EX|R0");
						readMsg();
						displayToUI();
						
						robot.move(MOVEMENT.FORWARD);
						sendMsg("EX|F01");
					}
				} else if (currRow == destRow) {
					if (currCol < destCol) {
						robot.move(MOVEMENT.FORWARD);
						sendMsg("EX|F01");
					} else if (currCol > destCol) {
						robot.turn(MOVEMENT.RIGHT);
						sendMsg("EX|R0");
						readMsg();
						displayToUI();
						
						robot.turn(MOVEMENT.RIGHT);
						sendMsg("EX|R0");
						readMsg();
						displayToUI();
						
						robot.move(MOVEMENT.FORWARD);
						sendMsg("EX|F01");
					}
				}
				break;
			case WEST:
				if (currCol == destCol) {
					if (currRow < destRow) {
						robot.turn(MOVEMENT.RIGHT);
						sendMsg("EX|R0");
						readMsg();
						displayToUI();
						
						robot.move(MOVEMENT.FORWARD);
						sendMsg("EX|F01");
					} else if (currRow > destRow) {
						robot.turn(MOVEMENT.LEFT);
						sendMsg("EX|L0");
						readMsg();
						displayToUI();
						
						robot.move(MOVEMENT.FORWARD);
						sendMsg("EX|F01");
					}
				} else if (currRow == destRow) {
					if (currCol < destCol) {
						robot.turn(MOVEMENT.RIGHT);
						sendMsg("EX|R0");
						readMsg();
						displayToUI();
						
						robot.turn(MOVEMENT.RIGHT);
						sendMsg("EX|R0");
						readMsg();
						displayToUI();
						
						robot.move(MOVEMENT.FORWARD);
						sendMsg("EX|F01");
						
					} else if (currCol > destCol) {
						robot.move(MOVEMENT.FORWARD);
						sendMsg("EX|F01");
					}
				}
				break;
			}
			//String recMsg = tcpObj.readMessage();
			//sendMsg(exploreMap.setExploredCells(robot, recMsg));
			readMsg();
			displayToUI(); // repaint GUI to show robot movement on map

			currRow = robot.getPosRow();
			currCol = robot.getPosCol();
			System.out.println("Location: " + currRow + "_" + currCol);
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
		mGui.displayMsgToUI("Calibrating ROBOT!");
		switch(robot.getCurrDir()) {
		case NORTH: break;
		case SOUTH:
			this.robot.turn(MOVEMENT.RIGHT);
			sendMsg("R0");
			readMsg();
			displayToUI();	 
			
			this.robot.turn(MOVEMENT.RIGHT);
			sendMsg("R0");
			readMsg();
			displayToUI();
			
			break;
		case EAST: 
			this.robot.turn(MOVEMENT.LEFT);
			sendMsg("L0");
			readMsg();
			displayToUI();
			break;
		case WEST:
			this.robot.turn(MOVEMENT.RIGHT);
			sendMsg("R0");
			readMsg();
			displayToUI();	
			break;
		}
	}
	
	//==================== FastestPath ==============================
	
	public String parseFPMovement(ArrayList<MOVEMENT> fastestPathMovements){
        int i = 0;
        int counter = 1;
        String result = "FP|";
        
        while(true){
            switch(fastestPathMovements.get(i)){
                case BACKWARD:
                    break;
                case FORWARD:
                    result += "F";
                    break;
                case LEFT:
                    result += "L";
                    break;
                case RIGHT:
                    result += "R";
                    break;
                default:
                    break;
                
            }
            for(int j = i+1; j< fastestPathMovements.size(); j++){
                if(fastestPathMovements.get(i) == fastestPathMovements.get(j)){
                    counter++;
                }
                else{
                    result += Integer.toString(counter);
                    result += "|";
                    counter = 1;
                    i=j;
                    break;
                }
            }
            if(i==fastestPathMovements.size() - 1){
                break;
            }
        }

        //result += "!";
        System.out.println(result);
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
            switch(robot.getCurrDir()){
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
                        	mBot.turn(MOVEMENT.RIGHT); robot.turn(MOVEMENT.RIGHT); 
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

        //printMovementArray(fastestPathMovements);
        String result = parseFPMovement(fastestPathMovements);
        return result;	
    }
	private void printMoveONLY(ArrayList<Cell> cellStep)throws InterruptedException {
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
	                        	readMsg();
	                        	displayToUI();	
	                        	
	                        	robot.turn(MOVEMENT.RIGHT); 
	                        	readMsg();
	                        	displayToUI();	
	                        	
	                        	robot.move(MOVEMENT.FORWARD);
	                        	}
	                    }
	                    else if(currRow == destRow){
	                        if(currCol < destCol){
	                        	robot.turn(MOVEMENT.RIGHT);
	                        	readMsg();
	                        	displayToUI();	
	                        	
	                        	robot.move(MOVEMENT.FORWARD);
	                        }
	                        else if(currCol > destCol){
	                        	robot.turn(MOVEMENT.LEFT); 
	                        	readMsg();
	                        	displayToUI();	
	                        	
	                        	robot.move(MOVEMENT.FORWARD);
	                        }
	                    }
	                    break;
	                case SOUTH:
	                    if(currCol == destCol){
	                        if(currRow < destRow){
	                        	robot.turn(MOVEMENT.RIGHT); 
	                        	readMsg();
	                        	displayToUI();	
	                        	
	                        	robot.turn(MOVEMENT.RIGHT); 
	                        	readMsg();
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
	                        	readMsg();
	                        	displayToUI();	
	                        	
	                        	robot.move(MOVEMENT.FORWARD);
	                        }
	                    }
	                    break;
	                case EAST:
	                    if(currCol == destCol){
	                        if(currRow < destRow){
	                        	robot.turn(MOVEMENT.LEFT); 
	                        	readMsg();
	                        	displayToUI();	
	                        	
	                        	robot.move(MOVEMENT.FORWARD);
	                        }
	                        else if(currRow > destRow){
	                        	robot.turn(MOVEMENT.RIGHT); 
	                        	readMsg();
	                        	displayToUI();	
	                        	robot.move(MOVEMENT.FORWARD);
	                        }
	                    }
	                    else if(currRow == destRow){
	                        if(currCol < destCol){
	                        	robot.move(MOVEMENT.FORWARD);}
	                        else if(currCol > destCol){
	                        	robot.turn(MOVEMENT.RIGHT); 
	                        	readMsg();
	                        	displayToUI();	
	                        	
	                        	robot.turn(MOVEMENT.RIGHT); 
	                        	readMsg();
	                        	displayToUI();	
	                        	
	                        	robot.move(MOVEMENT.FORWARD);}
	                    }
	                    break;
	                case WEST:
	                if(currCol == destCol){
	                    if(currRow < destRow){
	                    	robot.turn(MOVEMENT.RIGHT); 
	                    	readMsg();
	                    	displayToUI();	
	                    	
	                    	robot.move(MOVEMENT.FORWARD);
	                    	}
	                    else if(currRow > destRow){
	                    	robot.turn(MOVEMENT.LEFT); 
	                    	readMsg();
	                    	displayToUI();	
	                    	robot.move(MOVEMENT.FORWARD);
	                    	}
	                }
	                else if(currRow == destRow){
	                    if(currCol < destCol){
	                    	robot.turn(MOVEMENT.RIGHT);
	                    	readMsg();
	                    	displayToUI();	
	                    	
	                    	robot.turn(MOVEMENT.RIGHT); 
	                    	readMsg();
	                    	displayToUI();	
	                    	
	                    	robot.move(MOVEMENT.FORWARD);
	                    	}
	                    else if(currCol > destCol){ 
	                    	robot.move(MOVEMENT.FORWARD);
	                    	}
	                }
	                break;
	            }
	            readMsg();
	            displayToUI();						
        		
	            
	            currRow = robot.getPosRow();
	            currCol = robot.getPosCol();
	            System.out.println("Location: " + currRow + "_" +currCol);
	        }
		
		
	}
	//==================== GUI PAINTING ================================
	
	//Painting the current map result to GUI with delay
	private void displayToUI() throws InterruptedException {

		mGui.paintResult();
		//Thread.sleep((long) (0.1 * 1000));
	}
	
	// ==================== Communication Methods with RPI ==========================
	
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
		}
		while(true);
		
	}
	
	private String readMsg() {
		String msg = "";
		msg = tcpObj.readMessage();
		mGui.displayMsgToUI("Received: " + msg);
		return msg;
	}
	
	private void sendMsg(String msg) {
		String rmsg = tcpObj.sendMessage(msg);
		mGui.displayMsgToUI("Sent: " + rmsg);
		
	}
	
	//==================== Communication to android =================
	
	
	public void sendToAndroid(String rmsg) {
		
		
		if(rmsg.length()!=0) {
			rmsg = "OB|" + rmsg;
			sendMsg(rmsg);
		}
		
		
	}
	
}
