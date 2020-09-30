package gui;
import constant.Constants.DIRECTION;
import entity.Map;
import entity.Robot;

public class main {
	/*
	static Thread simExplore, simFastest;
	static MainGUI gui;
	static Map initialMap;
	static Robot rBot;
	*/
    public static void main(String args[]) {
    	
        //MainGUI gui = new MainGUI();
        //resetMap();
    	
    	MainGUI gui = MainGUI.getInstance();
    }
    
    /**
     * This method run the simulation of exploration on another thread
     */
    /*
    public static void executeExplorateSimulation(String fileName) {
    	
    	 simExplore = new Thread(new simulateExploration1(gui,initialMap,rBot,fileName));
    	 simExplore.start();
    }
    
    public static void executeFastestPathSimulation() {
    	
    	rBot =  new Robot(1, 1, DIRECTION.NORTH, false);	//Teleport robot back to 1,1
    	simFastest = new Thread(new simulateFastestPath(gui,rBot,initialMap));
    	simFastest.start();
    }
   
    public static void resetMap() {
    	
    	initialMap = new Map(); 				//define a map that is unexplored
    	rBot = new Robot(1,1,DIRECTION.NORTH);	//setting robot back to location 1,1
        gui.paintResult(initialMap, rBot);		//repaint the map UI
    }
    */
}