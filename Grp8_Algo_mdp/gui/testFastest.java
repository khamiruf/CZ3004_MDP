package gui;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import Communication.TCPComm2;
import algorithm.FastestPath;
import constant.Constants.MOVEMENT;
import entity.Cell;
import entity.Map;
import entity.Robot;
import util.FileManager;

public class testFastest implements Runnable{

	private TCPComm2 tcpObj;
	private Robot robot;
	private Map exploreMap;
	private MainGUI mGui;
	public testFastest(MainGUI m,Robot r) {
		mGui = m;
		this.robot = r;
		tcpObj = TCPComm2.getInstance();
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		try {
			File mapFile = new File("maps\\EmptyMap.txt"); 
			exploreMap = new Map(FileManager.readMapFromFile(mapFile));
		} catch (IOException e) {
			
			e.printStackTrace();
		}
		sendMsg("N|");
		FastestPath fastestPath = new FastestPath(this.robot, exploreMap);
		ArrayList<Cell> cellsInPath = fastestPath.findAllWPEndPaths(exploreMap);
		String movementString = convertCellsToMovements(cellsInPath); // Generate movement string based on cell
																		// list.
		try {
			waitForFastestPath();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} // Waiting for fastest path command
		sendMsg(movementString);
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
	
	private String readMsg() throws InterruptedException {

		String msg = "";
		do {
			msg = tcpObj.readMessage();
		} while (msg == null || msg.length() == 0);
		
		//if(msg.substring(0,2).equals("N|")) {
			//sendMDFInfo();
		//	throw new InterruptedException();
		//}
		mGui.displayMsgToUI("Received: " + msg);
		return msg;
	}
	// Send String message to RPI
		private void sendMsg(String msg) {
			//String rmsg = tcpObj.sendMessage(msg);
			tcpObj.sendMessage(msg+"!");
			mGui.displayMsgToUI("Sent: " + msg);

		}
}
