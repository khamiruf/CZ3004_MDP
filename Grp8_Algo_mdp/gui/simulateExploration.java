package gui;

import algorithm.FastestPath;
import constant.Constants;
import constant.Constants.MOVEMENT;
import entity.Cell;
import entity.Map;
import entity.Robot;
import util.FileManager;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

/**
 * This is the thread to run the Exploration simulation
 */
public class simulateExploration implements Runnable {


    private MainGUI mGui;
    private Map realMap, exploreMap;
    private Robot robot;
    private float playSpeed;
    private int userPercent;
    private int timeLimit;
    double currentCoverage;        // Storing current explore map coverage(%)
    Timer mTimer;
    boolean timerStop;

    /**
     * This method is the non-default constructor to create simulateExploration thread class
     *
     * @param maGUI    The GUI object where the result should be displayed to.
     * @param eMap     The Map object that the robot have explored.
     * @param rBot     The robot object which specifies the detail of robot.
     * @param fileName The name of txt file which contain information of actual arena map
     *                 to be read by simulator
     */
    public simulateExploration(MainGUI maGUI, Map eMap, Robot rBot, String fileName) {

        exploreMap = eMap;                  // Unexplored map that robot knows, obtain from MainGUI
        robot = rBot;                       // Robot object obtained from MainGUI

        this.mGui = maGUI;
        this.userPercent = maGUI.getUserPercentage();
        this.playSpeed = 1 / maGUI.getUserSpeed();
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

    /**
     * This method represent the starting method for this thread to execute when called from GUI.
     * It will perform the entire exploration(right-wall and a* search algorithm)
     * and move the virtual robot accordingly.
     */
    @Override
    public void run() {

        mGui.displayMsgToUI("SimulateExplorationThread Started");

        try {

            initialiseTimer();
            exploreMap.setExploredCells(robot, realMap);

            do {

                if (robot.isMovementValid(exploreMap, MOVEMENT.RIGHT)) {
                    robot.turn(MOVEMENT.RIGHT);

                    exploreMap.setExploredCells(robot, realMap);
                    displayToUI();
                    robot.move(MOVEMENT.FORWARD);
                    exploreMap.setExploredCells(robot, realMap);

                } else if (robot.isMovementValid(exploreMap, MOVEMENT.FORWARD)) {
                    robot.move(MOVEMENT.FORWARD);
                    exploreMap.setExploredCells(robot, realMap);

                } else if (robot.isMovementValid(exploreMap, MOVEMENT.LEFT)) {
                    robot.turn(MOVEMENT.LEFT);
                    exploreMap.setExploredCells(robot, realMap);


                } else if (robot.isMovementValid(exploreMap, MOVEMENT.BACKWARD)) {
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


                if (checkLimitConditions()) {
                    this.mTimer.cancel();
                    this.mTimer.purge();
                    return;
                }
            } while (!exploreMap.getStartGoalPosition().getExploredState() ||
                    !exploreMap.getEndGoalPosition().getExploredState() ||
                    !exploreMap.checkIfRobotAtStartPos(robot));

            ArrayList<Cell> unexploredList = getUnexploredList(exploreMap);
            while (unexploredList.size() > 0) {

                FastestPath fastobj = new FastestPath(robot, exploreMap);
                ArrayList<Cell> cellStep = fastobj.calculateFastestPath2(exploreMap, unexploredList.get(0).getRowPos(), unexploredList.get(0).getColPos());
                if (cellStep == null) {
                    unexploredList.remove(0);
                } else {
                    printMovement(cellStep);

                    if (checkLimitConditions()) {
                        this.mTimer.cancel();
                        this.mTimer.purge();
                        return;
                    }
                    unexploredList = updateUnexploreList(unexploredList);
                }
            }


            if (!exploreMap.checkIfRobotAtStartPos(robot)) {
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
        } catch (Exception e) {
            this.mTimer.cancel();
            this.mTimer.purge();
            mGui.displayMsgToUI("SimulateExplorationThread unknown error: " + e.getMessage());
            return;
        }


        this.mTimer.cancel();
        this.mTimer.purge();
        mGui.printFinal();            //Print the final map that robot knows on system console
        mGui.displayMsgToUI("MDF1: " + exploreMap.getMDF1());
        mGui.displayMsgToUI("MDF2: " + exploreMap.getMDF2());

        try {
            this.playSpeed = 1;
            faceNorthDirection();
        } catch (InterruptedException e) {

            e.printStackTrace();
        }


    }

    /**
     * This method take in path (List of cells) and guide the robot to move from cell to cell in the list.
     *
     * @param cellStep ArrayList of cells the robot need to move to.
     * @throws InterruptedException If the connection gets interrupted.
     */
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
                        } else if (currRow > destRow) {
                            robot.turn(MOVEMENT.RIGHT);
                            displayToUI();
                            robot.turn(MOVEMENT.RIGHT);
                            displayToUI();
                            robot.move(MOVEMENT.FORWARD);
                        }
                    } else if (currRow == destRow) {
                        if (currCol < destCol) {
                            robot.turn(MOVEMENT.RIGHT);
                            displayToUI();
                            robot.move(MOVEMENT.FORWARD);
                        } else if (currCol > destCol) {
                            robot.turn(MOVEMENT.LEFT);
                            displayToUI();
                            robot.move(MOVEMENT.FORWARD);
                        }
                    }
                    break;
                case SOUTH:
                    if (currCol == destCol) {
                        if (currRow < destRow) {
                            robot.turn(MOVEMENT.RIGHT);
                            displayToUI();
                            robot.turn(MOVEMENT.RIGHT);
                            displayToUI();
                            robot.move(MOVEMENT.FORWARD);
                        } else if (currRow > destRow) {
                            robot.move(MOVEMENT.FORWARD);
                        }
                    } else if (currRow == destRow) {
                        if (currCol < destCol) {
                            robot.turn(MOVEMENT.LEFT);
                            displayToUI();
                            robot.move(MOVEMENT.FORWARD);
                        } else if (currCol > destCol) {
                            robot.turn(MOVEMENT.RIGHT);
                            displayToUI();
                            robot.move(MOVEMENT.FORWARD);
                        }
                    }
                    break;
                case EAST:
                    if (currCol == destCol) {
                        if (currRow < destRow) {
                            robot.turn(MOVEMENT.LEFT);
                            displayToUI();
                            robot.move(MOVEMENT.FORWARD);
                        } else if (currRow > destRow) {
                            robot.turn(MOVEMENT.RIGHT);
                            displayToUI();
                            robot.move(MOVEMENT.FORWARD);
                        }
                    } else if (currRow == destRow) {
                        if (currCol < destCol) {
                            robot.move(MOVEMENT.FORWARD);
                        } else if (currCol > destCol) {
                            robot.turn(MOVEMENT.RIGHT);
                            displayToUI();
                            robot.turn(MOVEMENT.RIGHT);
                            displayToUI();
                            robot.move(MOVEMENT.FORWARD);
                        }
                    }
                    break;
                case WEST:

                    if (currCol == destCol) {
                        if (currRow < destRow) {
                            robot.turn(MOVEMENT.RIGHT);
                            displayToUI();
                            robot.move(MOVEMENT.FORWARD);
                        } else if (currRow > destRow) {
                            robot.turn(MOVEMENT.LEFT);
                            displayToUI();
                            robot.move(MOVEMENT.FORWARD);
                        }
                    } else if (currRow == destRow) {
                        if (currCol < destCol) {
                            robot.turn(MOVEMENT.RIGHT);
                            displayToUI();
                            robot.turn(MOVEMENT.RIGHT);
                            displayToUI();
                            robot.move(MOVEMENT.FORWARD);
                        } else if (currCol > destCol) {
                            robot.move(MOVEMENT.FORWARD);
                        }
                    }
                    break;
            }
            exploreMap.setExploredCells(robot, realMap);
            displayToUI();

            currRow = robot.getPosRow();
            currCol = robot.getPosCol();

        }
    }

    /**
     *
     */

    /**
     * This method will paint the current map object perceived by the robot and its current location to GUI
     * for the users to see. The frame rate changes according to the specified steps by the user.
     *
     * @throws InterruptedException If the connection gets interrupted.
     */
    private void displayToUI() throws InterruptedException {
        mGui.paintResult();
        Thread.sleep((long) (playSpeed * 1000));
    }

    /**
     * This method return a list of cells that are unexplored in the arena.
     *
     * @param exploredMap The arena/map object
     * @return ArrayList of cell that contain all unexplored cell of the input arena/map.
     */
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

    /**
     * This method return an updated list of cells that are unexplored.
     *
     * @param clist An existing arraylist of cell that are unexplored.
     * @return ArrayList of unexplored cell that are updated according to the latest arena/map object.
     */
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

    /**
     * This method compute the current percentage of explored map by robot
     *
     * @return the percentage value of the current coverage in data type daouble.
     */
    private double getMapPercentCoverage() {

        double percent = 0;
        for (int r = 0; r < Constants.MAX_ROW; r++) {
            for (int c = 0; c < Constants.MAX_COL; c++) {
                Cell cell = this.exploreMap.getMapGrid()[r][c];

                if (cell.getExploredState() == true) {
                    percent++;
                }
            }
        }

        return (percent / 300) * 100;
    }

    /**
     * This method specifies the condition for the simulated exploration algorithm to stop
     *
     * @return true if condition to stop have been fulfilled, else false .
     */
    private boolean checkLimitConditions() {

        currentCoverage = getMapPercentCoverage();
        mGui.displayMapCoverToUI(currentCoverage);
        if (this.userPercent != 100) {
            if (currentCoverage > userPercent) {
                return true;
            }
        }

        if (this.timeLimit != 0 && timerStop) {
            return true;
        }

        return false;
    }

    /**
     * This method rotate the robot to face north direction regardless of its current direction
     *
     * @throws InterruptedException If the connection gets interrupted.
     */
    private void faceNorthDirection() throws InterruptedException {
        mGui.displayMsgToUI("Calibrating ROBOT!");
        switch (robot.getCurrDir()) {
            case NORTH:
                break;
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

    /**
     * This method create a timer object to display the time elapsed on the GUi.
     */
    private void initialiseTimer() {
        /* Count up */
        this.mTimer = new Timer();
        this.mTimer.scheduleAtFixedRate(new TimerTask() {
            private long startTime = System.currentTimeMillis();
            private long timeElapsed;

            /* Update timer every second */
            @Override
            public void run() {

                timeElapsed = (System.currentTimeMillis() - startTime) / 1000;
                mGui.displayTimerToUI((int) timeElapsed);
                if (timeLimit != 0) {

                    if ((int) timeElapsed >= timeLimit) {
                        timerStop = true;
                    }
                }

            }
        }, 0, 1000);
    }

}


