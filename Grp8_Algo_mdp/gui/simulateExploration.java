package gui;

import java.io.File;
import java.io.IOException;

import algorithm.Exploration;
import constant.Constants.MOVEMENT;
import entity.Cell;
import entity.Map;
import entity.Robot;
import constant.Constants.DIRECTION;
import util.FileManager;
import util.NextMove;

/**
 *	This is the thread to run the Exploration simulation 
 *
 */
public class simulateExploration implements Runnable{

	
	private MainGUI mGui;
	private Map realMap, exploreMap;
	private Robot robot;
	private Exploration exploration;
	private float playSpeed;
	
	
	public simulateExploration(MainGUI maGUI, Map eMap, Robot rBot, String fileName) {
		this.mGui = maGUI;
		try {
			File mapFile = new File("maps\\" + fileName); 
			realMap = new Map(FileManager.readMapFromFile(mapFile));
		} catch (IOException e) {
			
			e.printStackTrace();
		}
		exploreMap = eMap;					//unexplored map that robot knows, obtain from MainGUI 
		exploration = new Exploration();
		robot = rBot;						//Robot object obtained from MainGUI
		
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		
		mGui.displayMsgToUI("SimulateExplorationThread Started");
		
		playSpeed = 1;
		int moveCounter = 0;
		
		while (!Thread.currentThread().isInterrupted()) {
			try {
				exploreMap.setExploredCells(robot, realMap);
				NextMove nextMove = exploration.nextMove(exploreMap, robot, moveCounter);
                while(nextMove.isHasNextMove()){
                	
                    if(nextMove.getNextMove() == MOVEMENT.FORWARD){
                    
                        robot.move(MOVEMENT.FORWARD);
                        exploreMap.setExploredCells(robot, realMap);                  
                        moveCounter++;
                    }
                    else if(nextMove.getNextMove() == MOVEMENT.RIGHT){
                        robot.turn(MOVEMENT.RIGHT);
                        exploreMap.setExploredCells(robot, realMap);
                        mGui.paintResult();				//repaint GUI to show turning
                        robot.move(MOVEMENT.FORWARD);
                        exploreMap.setExploredCells(robot, realMap);
                        moveCounter++;
                    }
                    else if(nextMove.getNextMove() == MOVEMENT.LEFT){
                        robot.turn(MOVEMENT.LEFT);
                        exploreMap.setExploredCells(robot, realMap);
                        // mGui.paintResult(exploreMap,robot);				//repaint GUI to show turning
                        //robot.move(MOVEMENT.FORWARD);                   
                        //exploreMap.setExploredCells(robot, realMap);
                        moveCounter++;
                    }
                    else if(nextMove.getNextMove() == MOVEMENT.BACKWARD){
                        robot.turn(MOVEMENT.RIGHT);
                        exploreMap.setExploredCells(robot, realMap);
                        mGui.paintResult();				//repaint GUI to show turning
                        robot.turn(MOVEMENT.RIGHT);
                        exploreMap.setExploredCells(robot, realMap);      
                        mGui.paintResult();				//repaint GUI to show turning
                        robot.move(MOVEMENT.FORWARD);
                        exploreMap.setExploredCells(robot, realMap);
                        moveCounter++;
                    }
                    nextMove = exploration.nextMove(exploreMap, robot, moveCounter);
                    mGui.paintResult();					//repaint GUI to show moving forward
                    Thread.sleep((long) (playSpeed * 100));				//increase multiplier value to slowdown speed
                }
					break;  	//break out from thread
			} catch (InterruptedException e) {
				break;
			}
			
			  
		}
		mGui.printFinal();			//Print the final map that robot knows on system console
		mGui.displayMsgToUI("MDF1: " + exploreMap.getMDF1());
		mGui.displayMsgToUI("MDF2: " + exploreMap.getMDF2());
		System.out.println("MDF1: " + exploreMap.getMDF1());
		System.out.println("MDF2: " + exploreMap.getMDF2());
	}

}

