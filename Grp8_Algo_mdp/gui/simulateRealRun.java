package gui;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import Communication.TCPComm;
import Communication.TCPComm2;
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
	private TCPComm2 tcpObj;


	public simulateRealRun(MainGUI mGUI, Robot rBot, Map eMap) {
		this.mGui = mGUI;
		this.exploreMap = eMap;
		this.robot = rBot;
		this.tcpObj = TCPComm2.getInstance();

	}

	@Override
	public void run() {

		mGui.displayMsgToUI("RealRunThread Started!");

		try {
			establishCommsToRPI();
			checkandPlotSC(); // waiting for start coord from RPI
			checkandPlotWP();
			displayToUI();
			mGui.displayMsgToUI("Exploration Started, Waiting for sensor data...");

			sendMsg("EX|V0|(0),(0)|(0),(0)");
			//sendMsg("EX|L0|(0),(0)|(0),(0)");
			String recMsg = readMsg();
			//recMsg = readMsg();
			int forwardCount = 0;
			sendObstacleInfo(exploreMap.setExploredCells(robot, recMsg));

			displayToUI();

			do {

				if (robot.isMovementValid(exploreMap, MOVEMENT.RIGHT)) {
					robot.turn(MOVEMENT.RIGHT);
					sendMsg("EX|R0" + exploreMap.rpiImageString(robot));
					recMsg = readMsg();
					sendObstacleInfo(exploreMap.setExploredCells(robot, recMsg));
					displayToUI();

					forwardCount++;
					robot.move(MOVEMENT.FORWARD);
					// sendMsg("EX|F01"+ exploreMap.rpiImageString(robot));
					chooseForward(recMsg, exploreMap.rpiImageString(robot));
					recMsg = readMsg();
					sendObstacleInfo(exploreMap.setExploredCells(robot, recMsg));
					forwardCount = centerRepos(forwardCount, recMsg);

				} else if (robot.isMovementValid(exploreMap, MOVEMENT.FORWARD)) {
					forwardCount++;
					robot.move(MOVEMENT.FORWARD);
					// sendMsg("EX|F01"+ exploreMap.rpiImageString(robot));
					chooseForward(recMsg, exploreMap.rpiImageString(robot));
					recMsg = readMsg();
					sendObstacleInfo(exploreMap.setExploredCells(robot, recMsg));
					forwardCount = centerRepos(forwardCount, recMsg);

				} else if (robot.isMovementValid(exploreMap, MOVEMENT.LEFT)) {
					robot.turn(MOVEMENT.LEFT);

					sendMsg("EX|L0" + exploreMap.rpiImageString(robot));
					recMsg = readMsg();
					sendObstacleInfo(exploreMap.setExploredCells(robot, recMsg));
					forwardCount = centerRepos(forwardCount, recMsg);

				} else if (robot.isMovementValid(exploreMap, MOVEMENT.BACKWARD)) {
					robot.turn(MOVEMENT.RIGHT);

					sendMsg("EX|R0" + exploreMap.rpiImageString(robot));
					recMsg = readMsg();
					sendObstacleInfo(exploreMap.setExploredCells(robot, recMsg));
					displayToUI();

					robot.turn(MOVEMENT.RIGHT);

					sendMsg("EX|R0" + exploreMap.rpiImageString(robot));
					recMsg = readMsg();
					sendObstacleInfo(exploreMap.setExploredCells(robot, recMsg));
					displayToUI();

					forwardCount++;
					robot.move(MOVEMENT.FORWARD);

					// sendMsg("EX|F01"+ exploreMap.rpiImageString(robot));
					chooseForward(recMsg, exploreMap.rpiImageString(robot));
					recMsg = readMsg();
					sendObstacleInfo(exploreMap.setExploredCells(robot, recMsg));
					forwardCount = centerRepos(forwardCount, recMsg);
				} else {
					// No Valid movement, therefore throw an exception to display error msg & close
					// connection
					throw new Exception("No Valid Movement for Exploration! kill thread...");
				}
				displayToUI();

			} while (!exploreMap.getStartGoalPosition().getExploredState()
					|| !exploreMap.getEndGoalPosition().getExploredState()
					|| !exploreMap.checkIfRobotAtStartPos(robot));
			
			ArrayList<Cell> unexploredList = getUnexploredList(exploreMap);
			while (unexploredList.size() > 0) {

				FastestPath fastobj = new FastestPath(robot, exploreMap);
				ArrayList<Cell> cellStep = fastobj.calculateFastestPath2(exploreMap, unexploredList.get(0).getRowPos(),
						unexploredList.get(0).getColPos());
				//System.out.println("Xsize:" + cellStep.size());
				if (cellStep == null) {
					//System.out.println("cellstepnull");
					unexploredList.remove(0);
				} else {
					//System.out.println("cellstepISNOTnull");
					printMovement(cellStep, unexploredList.get(0), true);

					unexploredList = updateUnexploreList(unexploredList);
					//System.out.println("ysize:" + unexploredList.size());
				}
			}
			 	
			if (!exploreMap.checkIfRobotAtStartPos(robot)) {
				FastestPath fastobj = new FastestPath(robot, exploreMap);

				ArrayList<Cell> cellStep = fastobj.calculateFastestPath(exploreMap,
						exploreMap.getStartGoalPosition().getRowPos(), exploreMap.getStartGoalPosition().getColPos());
				if (cellStep != null) {
					printMovement(cellStep, null, true);
				}

			}
			// ============= Cancel timer ==============================
			//this.mTimer.cancel();
			//this.mTimer.purge();
			// ============== Calibrating Robot to face North ============
			faceNorthDirection();
			sendMsg("N|"); // Send to Arduino to signify end of exploration

			// ============== Sending MDF to Android ===========================
			//String mdfvalue1 = exploreMap.getMDF1();
			//String mdfvalue2 = exploreMap.getMDF2();
			sendMDFInfo(); // Sending MDF1&2 to RPI
			mGui.printFinal(); // Print the final map that robot knows on system console
			//mGui.displayMsgToUI("MDF1: " + exploreMap.getMDF1());
			//mGui.displayMsgToUI("MDF2: " + exploreMap.getMDF2());
			System.out.println("MDF1: " + exploreMap.getMDF1());
			System.out.println("MDF2: " + exploreMap.getMDF2());

			// ==================== Fastest Path =========================
			mGui.displayMsgToUI("Starting Fastest Path..");
			/*
			 * FastestPath fastestPath = new FastestPath(this.robot, exploreMap);
			 * ArrayList<Cell> cellsInPath = fastestPath.calculateFastestPath(exploreMap,
			 * exploreMap.getWayPoint().getRowPos(), exploreMap.getWayPoint().getColPos());
			 * sendMsg(convertCellsToMovements(cellsInPath)); printMovement(cellsInPath,
			 * false);
			 * 
			 * fastestPath = new FastestPath(this.robot, exploreMap); cellsInPath =
			 * fastestPath.calculateFastestPath(exploreMap,
			 * exploreMap.getEndGoalPosition().getRowPos(),
			 * exploreMap.getEndGoalPosition().getColPos());
			 * sendMsg(convertCellsToMovements(cellsInPath)); printMovement(cellsInPath,
			 * false);
			 */
			FastestPath fastestPath = new FastestPath(this.robot, exploreMap);
			ArrayList<Cell> cellsInPath = fastestPath.findAllWPEndPaths(exploreMap);
			String movementString = convertCellsToMovements(cellsInPath); // Generate movement string based on cell
																			// list.
			waitForFastestPath(); // Waiting for fastest path command
			sendMsg(movementString);

		} catch (InterruptedException e) {
			System.out.println("RealRun thread InterruptedException" + e.getMessage());
			// mGui.displayMsgToUI("********** RealRun Thread Interrupted! **********");
			tcpObj.closeConnection();

		} catch (Exception e) {
			System.out.println("RealRun thread exception.." + e.getMessage());
			// mGui.displayMsgToUI("RealRun thread Exception Error: " + e.getMessage());
			tcpObj.closeConnection();

		}

		mGui.displayMsgToUI("****** RealRun Thread Ended Successfully! ******** ");
		
	}

	/**
	 * This method move and send movement commands meant for fastest path.
	 * 
	 * @param cellStep      ArrayList of cells the robot need to move to.
	 * @param setSensorData To accept sensor data and send obstacle data to android.
	 *                      TRUE is to send. FALSE to ignore sensor
	 * @throws InterruptedException
	 */
	private void printMovement(ArrayList<Cell> cellStep, Cell unexpl, boolean setSensorData)
			throws InterruptedException {
		int currRow = robot.getPosRow();
		int currCol = robot.getPosCol();
		String recMsg = "";
		int forwardCount = 0;
		//System.out.println("printmovesize:" + cellStep.size());
		for (int i = 0; i < cellStep.size(); i++) {
			int destRow = cellStep.get(i).getRowPos();
			int destCol = cellStep.get(i).getColPos();
			//System.out.println("printmoveRobot:" + cellStep.get(i).getRowPos() + "_" + cellStep.get(i).getColPos() + "-"
			//		+ currRow + "_" + currCol + ":" + robot.getCurrDir());
			switch (robot.getCurrDir()) {
			case NORTH:
				if (currCol == destCol) {
					if (currRow < destRow) {
						forwardCount++;
						robot.move(MOVEMENT.FORWARD);
						sendMsg("EX|F01" + exploreMap.rpiImageString(robot));
						recMsg = readMsg();
						forwardCount = centerRepos(forwardCount, recMsg);
						if (setSensorData) {
							sendObstacleInfo(exploreMap.setExploredCells(robot, recMsg));
						}

					} else if (currRow > destRow) {
						robot.turn(MOVEMENT.RIGHT);
						sendMsg("EX|R0" + exploreMap.rpiImageString(robot));
						recMsg = readMsg();
						if (setSensorData) {
							sendObstacleInfo(exploreMap.setExploredCells(robot, recMsg));
						}
						displayToUI();

						robot.turn(MOVEMENT.RIGHT);
						sendMsg("EX|R0" + exploreMap.rpiImageString(robot));
						recMsg = readMsg();
						if (setSensorData) {
							sendObstacleInfo(exploreMap.setExploredCells(robot, recMsg));
						}
						displayToUI();
						forwardCount++;
						robot.move(MOVEMENT.FORWARD);
						sendMsg("EX|F01" + exploreMap.rpiImageString(robot));
						recMsg = readMsg();
						forwardCount = centerRepos(forwardCount, recMsg);
						if (setSensorData) {
							sendObstacleInfo(exploreMap.setExploredCells(robot, recMsg));
						}
					}
				} else if (currRow == destRow) {
					if (currCol < destCol) {
						robot.turn(MOVEMENT.RIGHT);
						sendMsg("EX|R0" + exploreMap.rpiImageString(robot));
						recMsg = readMsg();
						if (setSensorData) {
							sendObstacleInfo(exploreMap.setExploredCells(robot, recMsg));
						}
						displayToUI();
						forwardCount++;
						robot.move(MOVEMENT.FORWARD);
						sendMsg("EX|F01" + exploreMap.rpiImageString(robot));
						recMsg = readMsg();
						forwardCount = centerRepos(forwardCount, recMsg);
						if (setSensorData) {
							sendObstacleInfo(exploreMap.setExploredCells(robot, recMsg));
						}

					} else if (currCol > destCol) {
						robot.turn(MOVEMENT.LEFT);
						sendMsg("EX|L0" + exploreMap.rpiImageString(robot));
						recMsg = readMsg();
						if (setSensorData) {
							sendObstacleInfo(exploreMap.setExploredCells(robot, recMsg));
						}
						displayToUI();
						forwardCount++;
						robot.move(MOVEMENT.FORWARD);
						sendMsg("EX|F01" + exploreMap.rpiImageString(robot));
						recMsg = readMsg();
						forwardCount = centerRepos(forwardCount, recMsg);
						if (setSensorData) {
							sendObstacleInfo(exploreMap.setExploredCells(robot, recMsg));
						}
					}
				}
				break;
			case SOUTH:
				if (currCol == destCol) {
					if (currRow < destRow) {
						robot.turn(MOVEMENT.RIGHT);
						sendMsg("EX|R0" + exploreMap.rpiImageString(robot));
						recMsg = readMsg();
						if (setSensorData) {
							sendObstacleInfo(exploreMap.setExploredCells(robot, recMsg));
						}
						displayToUI();

						robot.turn(MOVEMENT.RIGHT);
						sendMsg("EX|R0" + exploreMap.rpiImageString(robot));
						recMsg = readMsg();
						if (setSensorData) {
							sendObstacleInfo(exploreMap.setExploredCells(robot, recMsg));
						}
						displayToUI();
						forwardCount++;
						robot.move(MOVEMENT.FORWARD);
						sendMsg("EX|F01" + exploreMap.rpiImageString(robot));
						recMsg = readMsg();
						forwardCount = centerRepos(forwardCount, recMsg);
						if (setSensorData) {
							sendObstacleInfo(exploreMap.setExploredCells(robot, recMsg));
						}

					} else if (currRow > destRow) {
						forwardCount++;
						robot.move(MOVEMENT.FORWARD);
						sendMsg("EX|F01" + exploreMap.rpiImageString(robot));
						recMsg = readMsg();
						forwardCount = centerRepos(forwardCount, recMsg);
						if (setSensorData) {
							sendObstacleInfo(exploreMap.setExploredCells(robot, recMsg));
						}
					}
				} else if (currRow == destRow) {
					if (currCol < destCol) {
						robot.turn(MOVEMENT.LEFT);
						sendMsg("EX|L0" + exploreMap.rpiImageString(robot));
						recMsg = readMsg();
						if (setSensorData) {
							sendObstacleInfo(exploreMap.setExploredCells(robot, recMsg));
						}
						displayToUI();
						forwardCount++;
						robot.move(MOVEMENT.FORWARD);
						sendMsg("EX|F01" + exploreMap.rpiImageString(robot));
						recMsg = readMsg();
						forwardCount = centerRepos(forwardCount, recMsg);
						if (setSensorData) {
							sendObstacleInfo(exploreMap.setExploredCells(robot, recMsg));
						}
					} else if (currCol > destCol) {
						robot.turn(MOVEMENT.RIGHT);
						sendMsg("EX|R0" + exploreMap.rpiImageString(robot));
						recMsg = readMsg();
						if (setSensorData) {
							sendObstacleInfo(exploreMap.setExploredCells(robot, recMsg));
						}
						displayToUI();
						forwardCount++;
						robot.move(MOVEMENT.FORWARD);
						sendMsg("EX|F01" + exploreMap.rpiImageString(robot));
						recMsg = readMsg();
						forwardCount = centerRepos(forwardCount, recMsg);
						if (setSensorData) {
							sendObstacleInfo(exploreMap.setExploredCells(robot, recMsg));
						}
					}
				}
				break;
			case EAST:
				if (currCol == destCol) {
					if (currRow < destRow) {
						robot.turn(MOVEMENT.LEFT);
						sendMsg("EX|L0" + exploreMap.rpiImageString(robot));
						recMsg = readMsg();
						if (setSensorData) {
							sendObstacleInfo(exploreMap.setExploredCells(robot, recMsg));
						}
						displayToUI();
						forwardCount++;
						robot.move(MOVEMENT.FORWARD);
						sendMsg("EX|F01" + exploreMap.rpiImageString(robot));
						recMsg = readMsg();
						forwardCount = centerRepos(forwardCount, recMsg);
						if (setSensorData) {
							sendObstacleInfo(exploreMap.setExploredCells(robot, recMsg));
						}
					} else if (currRow > destRow) {
						robot.turn(MOVEMENT.RIGHT);
						sendMsg("EX|R0" + exploreMap.rpiImageString(robot));
						recMsg = readMsg();
						if (setSensorData) {
							sendObstacleInfo(exploreMap.setExploredCells(robot, recMsg));
						}
						displayToUI();
						forwardCount++;
						robot.move(MOVEMENT.FORWARD);
						sendMsg("EX|F01" + exploreMap.rpiImageString(robot));
						recMsg = readMsg();
						forwardCount = centerRepos(forwardCount, recMsg);
						if (setSensorData) {
							sendObstacleInfo(exploreMap.setExploredCells(robot, recMsg));
						}
					}
				} else if (currRow == destRow) {
					if (currCol < destCol) {
						forwardCount++;
						robot.move(MOVEMENT.FORWARD);
						sendMsg("EX|F01" + exploreMap.rpiImageString(robot));
						recMsg = readMsg();
						forwardCount = centerRepos(forwardCount, recMsg);
						if (setSensorData) {
							sendObstacleInfo(exploreMap.setExploredCells(robot, recMsg));
						}
					} else if (currCol > destCol) {
						robot.turn(MOVEMENT.RIGHT);
						sendMsg("EX|R0" + exploreMap.rpiImageString(robot));
						recMsg = readMsg();
						if (setSensorData) {
							sendObstacleInfo(exploreMap.setExploredCells(robot, recMsg));
						}
						displayToUI();

						robot.turn(MOVEMENT.RIGHT);
						sendMsg("EX|R0" + exploreMap.rpiImageString(robot));
						recMsg = readMsg();
						if (setSensorData) {
							sendObstacleInfo(exploreMap.setExploredCells(robot, recMsg));
						}
						displayToUI();
						forwardCount++;
						robot.move(MOVEMENT.FORWARD);
						sendMsg("EX|F01" + exploreMap.rpiImageString(robot));
						recMsg = readMsg();
						forwardCount = centerRepos(forwardCount, recMsg);
						if (setSensorData) {
							sendObstacleInfo(exploreMap.setExploredCells(robot, recMsg));
						}
					}
				}
				break;
			case WEST:
				if (currCol == destCol) {
					if (currRow < destRow) {
						System.out.println("westD");
						robot.turn(MOVEMENT.RIGHT);
						sendMsg("EX|R0" + exploreMap.rpiImageString(robot));
						recMsg = readMsg();
						if (setSensorData) {
							sendObstacleInfo(exploreMap.setExploredCells(robot, recMsg));
						}
						displayToUI();
						forwardCount++;
						robot.move(MOVEMENT.FORWARD);
						sendMsg("EX|F01" + exploreMap.rpiImageString(robot));
						recMsg = readMsg();
						forwardCount = centerRepos(forwardCount, recMsg);
						if (setSensorData) {
							sendObstacleInfo(exploreMap.setExploredCells(robot, recMsg));
						}
					} else if (currRow > destRow) {
						System.out.println("westC");
						robot.turn(MOVEMENT.LEFT);
						sendMsg("EX|L0" + exploreMap.rpiImageString(robot));
						recMsg = readMsg();
						if (setSensorData) {
							sendObstacleInfo(exploreMap.setExploredCells(robot, recMsg));
						}
						displayToUI();
						forwardCount++;
						robot.move(MOVEMENT.FORWARD);
						sendMsg("EX|F01" + exploreMap.rpiImageString(robot));
						recMsg = readMsg();
						forwardCount = centerRepos(forwardCount, recMsg);
						if (setSensorData) {
							sendObstacleInfo(exploreMap.setExploredCells(robot, recMsg));
						}
					}
				} else if (currRow == destRow) {
					if (currCol < destCol) {
						System.out.println("westB");
						robot.turn(MOVEMENT.RIGHT);
						sendMsg("EX|R0" + exploreMap.rpiImageString(robot));
						recMsg = readMsg();
						if (setSensorData) {
							sendObstacleInfo(exploreMap.setExploredCells(robot, recMsg));
						}
						displayToUI();

						robot.turn(MOVEMENT.RIGHT);
						sendMsg("EX|R0" + exploreMap.rpiImageString(robot));
						recMsg = readMsg();
						if (setSensorData) {
							sendObstacleInfo(exploreMap.setExploredCells(robot, recMsg));
						}
						displayToUI();
						forwardCount++;
						robot.move(MOVEMENT.FORWARD);
						sendMsg("EX|F01" + exploreMap.rpiImageString(robot));
						recMsg = readMsg();
						forwardCount = centerRepos(forwardCount, recMsg);
						if (setSensorData) {
							sendObstacleInfo(exploreMap.setExploredCells(robot, recMsg));
						}

					} else if (currCol > destCol) {
						System.out.println("westA");
						forwardCount++;
						robot.move(MOVEMENT.FORWARD);
						sendMsg("EX|F01" + exploreMap.rpiImageString(robot));

						recMsg = readMsg();
						forwardCount = centerRepos(forwardCount, recMsg);
						if (setSensorData) {
							sendObstacleInfo(exploreMap.setExploredCells(robot, recMsg));
						}
					}
				}
				break;
			}

			displayToUI();

			if (i == cellStep.size() - 1) {
				if (unexpl != null && !unexpl.getExploredState()) {
					destRow = unexpl.getRowPos();
					destCol = unexpl.getColPos();
					
					switch (robot.getCurrDir()) {
					case NORTH:
						if (currCol == destCol) {
							if (currRow < destRow) {
								// do nothing
							} else if (currRow > destRow) {
								robot.turn(MOVEMENT.RIGHT);
								sendMsg("EX|R0" + exploreMap.rpiImageString(robot));
								recMsg = readMsg();
								if (setSensorData) {
									sendObstacleInfo(exploreMap.setExploredCells(robot, recMsg));
								}
								displayToUI();

								robot.turn(MOVEMENT.RIGHT);
								sendMsg("EX|R0" + exploreMap.rpiImageString(robot));
								recMsg = readMsg();
								if (setSensorData) {
									sendObstacleInfo(exploreMap.setExploredCells(robot, recMsg));
								}
								displayToUI();
							}
						} else if (currRow == destRow) {
							if (currCol < destCol) {
								robot.turn(MOVEMENT.RIGHT);
								sendMsg("EX|R0" + exploreMap.rpiImageString(robot));
								recMsg = readMsg();
								if (setSensorData) {
									sendObstacleInfo(exploreMap.setExploredCells(robot, recMsg));
								}
								displayToUI();

							} else if (currCol > destCol) {
								robot.turn(MOVEMENT.LEFT);
								sendMsg("EX|L0" + exploreMap.rpiImageString(robot));
								recMsg = readMsg();
								if (setSensorData) {
									sendObstacleInfo(exploreMap.setExploredCells(robot, recMsg));
								}
								displayToUI();
							}
						}
						break;
					case SOUTH:
						if (currCol == destCol) {
							if (currRow < destRow) {
								robot.turn(MOVEMENT.RIGHT);
								sendMsg("EX|R0" + exploreMap.rpiImageString(robot));
								recMsg = readMsg();
								if (setSensorData) {
									sendObstacleInfo(exploreMap.setExploredCells(robot, recMsg));
								}
								displayToUI();

								robot.turn(MOVEMENT.RIGHT);
								sendMsg("EX|R0" + exploreMap.rpiImageString(robot));
								recMsg = readMsg();
								if (setSensorData) {
									sendObstacleInfo(exploreMap.setExploredCells(robot, recMsg));
								}
								displayToUI();

							} else if (currRow > destRow) {
								// do nothing
							}
						} else if (currRow == destRow) {
							if (currCol < destCol) {
								robot.turn(MOVEMENT.LEFT);
								sendMsg("EX|L0" + exploreMap.rpiImageString(robot));
								recMsg = readMsg();
								if (setSensorData) {
									sendObstacleInfo(exploreMap.setExploredCells(robot, recMsg));
								}
								displayToUI();
							} else if (currCol > destCol) {
								robot.turn(MOVEMENT.RIGHT);
								sendMsg("EX|R0" + exploreMap.rpiImageString(robot));
								recMsg = readMsg();
								if (setSensorData) {
									sendObstacleInfo(exploreMap.setExploredCells(robot, recMsg));
								}
								displayToUI();
							}
						}
						break;
					case EAST:
						if (currCol == destCol) {
							if (currRow < destRow) {
								robot.turn(MOVEMENT.LEFT);
								sendMsg("EX|L0" + exploreMap.rpiImageString(robot));
								recMsg = readMsg();
								if (setSensorData) {
									sendObstacleInfo(exploreMap.setExploredCells(robot, recMsg));
								}
								displayToUI();
							} else if (currRow > destRow) {
								robot.turn(MOVEMENT.RIGHT);
								sendMsg("EX|R0" + exploreMap.rpiImageString(robot));
								recMsg = readMsg();
								if (setSensorData) {
									sendObstacleInfo(exploreMap.setExploredCells(robot, recMsg));
								}
								displayToUI();
							}
						} else if (currRow == destRow) {
							if (currCol < destCol) {
								// do nothing
							} else if (currCol > destCol) {
								robot.turn(MOVEMENT.RIGHT);
								sendMsg("EX|R0" + exploreMap.rpiImageString(robot));
								recMsg = readMsg();
								if (setSensorData) {
									sendObstacleInfo(exploreMap.setExploredCells(robot, recMsg));
								}
								displayToUI();

								robot.turn(MOVEMENT.RIGHT);
								sendMsg("EX|R0" + exploreMap.rpiImageString(robot));
								recMsg = readMsg();
								if (setSensorData) {
									sendObstacleInfo(exploreMap.setExploredCells(robot, recMsg));
								}
								displayToUI();
							}
						}
						break;
					case WEST:
						if (currCol == destCol) {
							if (currRow < destRow) {
								robot.turn(MOVEMENT.RIGHT);
								sendMsg("EX|R0" + exploreMap.rpiImageString(robot));
								recMsg = readMsg();
								if (setSensorData) {
									sendObstacleInfo(exploreMap.setExploredCells(robot, recMsg));
								}
								displayToUI();
							} else if (currRow > destRow) {
								robot.turn(MOVEMENT.LEFT);
								sendMsg("EX|L0" + exploreMap.rpiImageString(robot));
								recMsg = readMsg();
								if (setSensorData) {
									sendObstacleInfo(exploreMap.setExploredCells(robot, recMsg));
								}
								displayToUI();
							}
						} else if (currRow == destRow) {
							if (currCol < destCol) {
								robot.turn(MOVEMENT.RIGHT);
								sendMsg("EX|R0" + exploreMap.rpiImageString(robot));
								recMsg = readMsg();
								if (setSensorData) {
									sendObstacleInfo(exploreMap.setExploredCells(robot, recMsg));
								}
								displayToUI();

								robot.turn(MOVEMENT.RIGHT);
								sendMsg("EX|R0" + exploreMap.rpiImageString(robot));
								recMsg = readMsg();
								if (setSensorData) {
									sendObstacleInfo(exploreMap.setExploredCells(robot, recMsg));
								}
								displayToUI();

							} else if (currCol > destCol) {
								// do nothing
							}
						}
						break;
					}
				}

			

			}
			currRow = robot.getPosRow();
			currCol = robot.getPosCol();
		}
	}

	private void printFastestPathMovement(String moveString) throws InterruptedException {

		// FP|F6|R0|F1|L0|F2
		String[] arr = moveString.split("\\|");

		for (int i = 1; i < arr.length; i++) {
			switch (arr[i].substring(0, 1)) {

			case "F":
				for (int y = 0; y < Integer.parseInt(arr[i].substring(1, arr[i].length())); y++) {
					this.robot.move(MOVEMENT.FORWARD);
					mGui.paintResult();
					Thread.sleep((long) 500);
				}
				break;
			case "R":
				for (int y = 0; y < Integer.parseInt(arr[i].substring(1, arr[i].length())); y++) {
					this.robot.turn(MOVEMENT.RIGHT);
					mGui.paintResult();
					Thread.sleep((long) 500);
					this.robot.move(MOVEMENT.FORWARD);
					mGui.paintResult();
					Thread.sleep((long) 500);

				}
				break;
			case "L":
				for (int y = 0; y < Integer.parseInt(arr[i].substring(1, arr[i].length())); y++) {
					this.robot.turn(MOVEMENT.LEFT);
					mGui.paintResult();
					Thread.sleep((long) 500);
					this.robot.move(MOVEMENT.FORWARD);
					mGui.paintResult();
					Thread.sleep((long) 500);

				}
				break;
			case "B":
				for (int y = 0; y < Integer.parseInt(arr[i].substring(1, arr[i].length())); y++) {
					this.robot.turn(MOVEMENT.RIGHT);
					mGui.paintResult();
					Thread.sleep((long) 500);
					this.robot.turn(MOVEMENT.RIGHT);
					mGui.paintResult();
					Thread.sleep((long) 500);
					this.robot.move(MOVEMENT.FORWARD);
					mGui.paintResult();
					Thread.sleep((long) 500);

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
		switch (robot.getCurrDir()) {
		case NORTH:
			break;
		case SOUTH:
			this.robot.turn(MOVEMENT.RIGHT);
			sendMsg("EX|R0" + exploreMap.rpiImageString(robot));
			readMsg();
			displayToUI();

			this.robot.turn(MOVEMENT.RIGHT);
			sendMsg("EX|R0" + exploreMap.rpiImageString(robot));
			readMsg();
			displayToUI();

			break;
		case EAST:
			this.robot.turn(MOVEMENT.LEFT);
			sendMsg("EX|L0" + exploreMap.rpiImageString(robot));
			readMsg();
			displayToUI();
			break;
		case WEST:
			this.robot.turn(MOVEMENT.RIGHT);
			sendMsg("EX|R0" + exploreMap.rpiImageString(robot));
			readMsg();
			displayToUI();
			break;
		}

	}

	// ================ FastestPath ===========================

	public String parseFPMovement(ArrayList<MOVEMENT> fastestPathMovements) {
		int i = 0;
		int j = 0;
		int counter = 1;
		String result = "FP|";

		while (i < fastestPathMovements.size()) {

			switch (fastestPathMovements.get(i)) {

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
			for (j = i + 1; j < fastestPathMovements.size(); j++) {
				if (fastestPathMovements.get(i) == fastestPathMovements.get(j)) {
					counter++;
				} else {
					break;
				}
			}

			if (fastestPathMovements.get(i) == MOVEMENT.FORWARD && counter < 10) {
				result += "0" + Integer.toString(counter);
			} else {
				result += Integer.toString(counter);
			}
			i = j;
			result += "|";
			counter = 1;

		}
		System.out.println("parseFPMovements():" + result);
		return result;
	}

	public String convertCellsToMovements(ArrayList<Cell> cellsInPath) {

		Robot mBot = new Robot(this.robot.getPosRow(), this.robot.getPosCol(), this.robot.getCurrDir());
		int currRow = mBot.getPosRow();
		int currCol = mBot.getPosCol();

		ArrayList<MOVEMENT> fastestPathMovements = new ArrayList<MOVEMENT>();

		for (int i = 0; i < cellsInPath.size(); i++) {
			int destRow = cellsInPath.get(i).getRowPos();
			int destCol = cellsInPath.get(i).getColPos();
			switch (mBot.getCurrDir()) {
			case NORTH:
				if (currCol == destCol) {
					if (currRow < destRow) {
						fastestPathMovements.add(MOVEMENT.FORWARD);
						mBot.move(MOVEMENT.FORWARD);
					} else if (currRow > destRow) {
						fastestPathMovements.add(MOVEMENT.BACKWARD);
						mBot.turn(MOVEMENT.RIGHT);
						mBot.turn(MOVEMENT.RIGHT);
						mBot.move(MOVEMENT.FORWARD);
					}
				} else if (currRow == destRow) {
					if (currCol < destCol) {
						fastestPathMovements.add(MOVEMENT.RIGHT);
						mBot.turn(MOVEMENT.RIGHT);
						mBot.move(MOVEMENT.FORWARD);
					} else if (currCol > destCol) {
						fastestPathMovements.add(MOVEMENT.LEFT);
						mBot.turn(MOVEMENT.LEFT);
						mBot.move(MOVEMENT.FORWARD);
					}
				}
				break;
			case SOUTH:
				if (currCol == destCol) {
					if (currRow < destRow) {
						fastestPathMovements.add(MOVEMENT.BACKWARD);
						mBot.turn(MOVEMENT.RIGHT);
						mBot.turn(MOVEMENT.RIGHT);
						mBot.move(MOVEMENT.FORWARD);
					} else if (currRow > destRow) {
						fastestPathMovements.add(MOVEMENT.FORWARD);
						mBot.move(MOVEMENT.FORWARD);
					}
				} else if (currRow == destRow) {
					if (currCol < destCol) {
						fastestPathMovements.add(MOVEMENT.LEFT);
						mBot.turn(MOVEMENT.LEFT);
						mBot.move(MOVEMENT.FORWARD);
					} else if (currCol > destCol) {
						fastestPathMovements.add(MOVEMENT.RIGHT);
						mBot.turn(MOVEMENT.RIGHT);
						mBot.move(MOVEMENT.FORWARD);
					}
				}
				break;
			case EAST:
				if (currCol == destCol) {
					if (currRow < destRow) {
						fastestPathMovements.add(MOVEMENT.LEFT);
						mBot.turn(MOVEMENT.LEFT);
						mBot.move(MOVEMENT.FORWARD);
					} else if (currRow > destRow) {
						fastestPathMovements.add(MOVEMENT.RIGHT);
						mBot.turn(MOVEMENT.RIGHT);
						mBot.move(MOVEMENT.FORWARD);
					}
				} else if (currRow == destRow) {
					if (currCol < destCol) {
						fastestPathMovements.add(MOVEMENT.FORWARD);
						mBot.move(MOVEMENT.FORWARD);
					} else if (currCol > destCol) {
						fastestPathMovements.add(MOVEMENT.BACKWARD);
						mBot.turn(MOVEMENT.RIGHT);
						mBot.turn(MOVEMENT.RIGHT);
						mBot.move(MOVEMENT.FORWARD);
					}
				}
				break;
			case WEST:
				if (currCol == destCol) {
					if (currRow < destRow) {
						fastestPathMovements.add(MOVEMENT.RIGHT);
						mBot.turn(MOVEMENT.RIGHT);
						mBot.move(MOVEMENT.FORWARD);
					} else if (currRow > destRow) {
						fastestPathMovements.add(MOVEMENT.LEFT);
						mBot.turn(MOVEMENT.LEFT);
						mBot.move(MOVEMENT.FORWARD);
					}
				} else if (currRow == destRow) {
					if (currCol < destCol) {
						fastestPathMovements.add(MOVEMENT.BACKWARD);
						mBot.turn(MOVEMENT.RIGHT);
						mBot.turn(MOVEMENT.RIGHT);
						mBot.move(MOVEMENT.FORWARD);
					} else if (currCol > destCol) {
						fastestPathMovements.add(MOVEMENT.FORWARD);
						mBot.move(MOVEMENT.FORWARD);
					}
				}
				break;
			}

			currRow = mBot.getPosRow();
			currCol = mBot.getPosCol();
		}

		String result = parseFPMovement(fastestPathMovements);
		return result;
	}

	// ===================== Arduino Commands ===============================

	// function to check conditions for reposition robot into middle of 3x3
	private int centerRepos(int forwardCount, String sensorDataInString) {
		int rightFront = Character.getNumericValue(sensorDataInString.charAt(1));
		int rightBack = Character.getNumericValue(sensorDataInString.charAt(0));
		int frontRight = Character.getNumericValue(sensorDataInString.charAt(3));
		int frontCenter = Character.getNumericValue(sensorDataInString.charAt(4));
		int frontLeft = Character.getNumericValue(sensorDataInString.charAt(5));

		if (rightFront == 1 && rightBack == 1 && frontRight == 1 && frontCenter == 1 && frontLeft == 1) {
			sendMsg("EX|V0|(0),(0)|(0),(0)");
			return 0;
		}
		
		else if (frontRight == 1 && frontCenter == 1 && frontLeft == 1) {
			sendMsg("EX|Q0|(0),(0)|(0),(0)");
		}
		
		//yaowen testing
		/*
		else if (frontRight == 1 || frontCenter == 1 || frontLeft == 1) {
			sendMsg("EX|Q0|(0),(0)|(0),(0)");
		}
		*/
		else if (forwardCount >= 4 && rightFront == 1 && rightBack == 1) {
			sendMsg("EX|P0|(0),(0)|(0),(0)");
			return 0;
		}

		else if (rightFront == 1 && rightBack == 1) {
			sendMsg("EX|E0|(0),(0)|(0),(0)");
		}

		return forwardCount;
	}

	
	private void chooseForward(String sensorDataInString, String rpiImageString) {
		int frontRight = Character.getNumericValue(sensorDataInString.charAt(3));
		int frontCenter = Character.getNumericValue(sensorDataInString.charAt(4));
		int frontLeft = Character.getNumericValue(sensorDataInString.charAt(5));

		if (frontRight == 2 || frontCenter == 2 || frontLeft == 2) {
			//sendMsg("EX|G0" + rpiImageString); actual code
			sendMsg("EX|F01" + rpiImageString); // This is testing purpose
		} else {
			sendMsg("EX|F01" + rpiImageString);
		}
	}
	
	// ======================= GUI PAINTING ===================================

	// Painting the current map result to GUI with delay
	private void displayToUI() {
		mGui.paintResult();
		// Thread.sleep((long) (0.1 * 1000));
	}

	// ================= Communication Methods with RPI =======================

	// Establish connection with RPI
	private void establishCommsToRPI() throws InterruptedException {
		String msg = "";

		do {
			mGui.displayMsgToUI("Establishing connection to RPI..  :D ");
			msg = this.tcpObj.establishConnection();
			if (msg.length() != 0) {
				mGui.displayMsgToUI(msg);

				Thread.sleep((long) (1 * 1000));

			} else {
				mGui.displayMsgToUI("Connected Successfully :DD ");
				break;
			}

		} while (!Thread.currentThread().isInterrupted());
	}

	private String readMsg() throws InterruptedException {

		String msg = "";
		do {
			msg = tcpObj.readMessage();
		} while (msg == null || msg.length() == 0);
		
		if(msg.substring(0,2).equals("N|")) {
			sendMDFInfo();
			throw new InterruptedException();
		}
		mGui.displayMsgToUI("Received: " + msg);
		return msg;
	}

	// Send String message to RPI
	private void sendMsg(String msg) {
		//String rmsg = tcpObj.sendMessage(msg);
		tcpObj.sendMessage(msg+"!");
		mGui.displayMsgToUI("Sent: " + msg);

	}

	// ==================== Communication Methods with Android =======================

	// plot robot according to start coordinates received from RPI
	private void checkandPlotSC() throws Exception {
		String rmsg = "";
		// SC|[1,1] = 8, SC|[10,10] = 10
		mGui.displayMsgToUI("Waiting for start coordinate...");
		do {

			rmsg = readMsg();
			
			if (rmsg.substring(0, 3).equals("SC|")) {
				String[] arr = rmsg.substring(4, rmsg.length() - 2).split(",");
				this.robot.setPosRow(Integer.parseInt(arr[0]));
				this.robot.setPosCol(Integer.parseInt(arr[1]));
				return;
			}

		} while (true);

	}

	// Waiting for fastest path command from RPI
	private void waitForFastestPath() throws Exception {
		String rmsg = "";
		mGui.displayMsgToUI("Waiting for command to start FastestPath...");
		do {
			rmsg = readMsg();
			if (rmsg.substring(0, 3).equals("FP|")) {
				return;
			}
		} while (true);
	}

	// Plot the WayPoint coordinates received from RPI
	private void checkandPlotWP() throws Exception {
		String rmsg = "";
		// WP|[1,1] [row ,col]
		mGui.displayMsgToUI("Waiting for WayPoint coordinate...");
		do {
			rmsg = readMsg();		
			if (rmsg.substring(0, 3).equals("WP|")) {
				String[] arr = rmsg.substring(4, rmsg.length() - 2).split(",");
				this.exploreMap.setWayPoint(Integer.parseInt(arr[1]), Integer.parseInt(arr[0]));
				return;
			}
		} while (true);
	}

	// Method for sending obstacles location
	private void sendObstacleInfo(String rmsg) {
		if (rmsg.length() != 0) {
			//rmsg = "OB|" + rmsg + "!MDF|" + exploreMap.getMDF1(); //original
			//sendMsg(rmsg);										//original
			sendMsg("OB|"+rmsg);
			sendMsg("MDF|"+exploreMap.getMDF1());
		} else {
			rmsg = "MDF|" + exploreMap.getMDF1();
			sendMsg(rmsg);
		}
	}

	// Method for sending MDF1 & MDF2
	private void sendMDFInfo() {
		String mdf1 = exploreMap.getMDF1();
		String mdf2 = exploreMap.getMDF2();
		sendMsg("MDF|" + mdf1 + "|" + mdf2);
	}

	
}
