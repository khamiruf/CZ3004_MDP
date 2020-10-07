package gui;

import entity.Cell;
import entity.Map;
import entity.Robot;
import util.FileManager;

import javax.swing.*;

import Communication.TCPComm;
import constant.Constants;
import constant.Constants.DIRECTION;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;


public class MainGUI extends JFrame{
	
    private JPanel controlPanel;
    private settingPanel acontrolPanel;
    private MapPanel simulationMap;
    private JPanel designMap;
    private JLabel[][] resultMap; 			//Use to display simulation
    
 
    //ZW NEWLY ADD
     Thread simExplore, simFastest, simRealRun, simb4;
	 Map initialMap;
	 Robot rBot;
	 static MainGUI mGui=null;
    //------------------------------------------
    
    /**
     * Creates the MainGUI.
     */
    private MainGUI() {
    	
    	 setMapRobotObj();
    	 initLayout();
    	 paintResult();
    }
    
    private void setMapRobotObj() {
    	initialMap = new Map();
    	rBot = new Robot(1,1,DIRECTION.SOUTH);
    }
    public static MainGUI getInstance() {
    	if(mGui==null) {
    		mGui= new MainGUI();
    	}
    	return mGui;
    }
    
    private void initLayout() {
        // Creates and set up a frame window
      
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        simulationMap = new MapPanel(false);
        setupControlPanel();
        designMap = new MapPanel(true);
        acontrolPanel = new settingPanel();
        resultMap = simulationMap.getJLabelMap();
        
        setLayout(new FlowLayout());
        setTitle("CX3004 MDP Group 8");
        add(simulationMap);
        add(controlPanel);
        //add(designMap);
        add(acontrolPanel);
        pack();
        setVisible(true);
       
    }

    private void setupControlPanel() {
        // Define control buttons
    	
    	JComboBox fileDDL;
        JButton exploreBtn = new JButton("Explore");
        JButton fastestBtn = new JButton("Fastest Path");
        JButton resetBtn = new JButton("Reset");
        JButton realrunBtn = new JButton("Real Run");
        JButton b3 = new JButton("button 3");
        
        JButton b4 = new JButton("button 4");
        exploreBtn.setPreferredSize(new Dimension(70, 70));
        
        String[] fileList = FileManager.getAllFileNames();
        fileDDL = new JComboBox(fileList);
                  
    	exploreBtn.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
						
				simExplore = new Thread(new simulateExploration1(mGui,initialMap,rBot,fileDDL.getSelectedItem().toString()));
			    simExplore.start();
				
			    exploreBtn.setEnabled(false);	
				}
			});
        
    	fastestBtn.addActionListener(new ActionListener() {

    		@Override
    		public void actionPerformed(ActionEvent e) {
		
 			
    	    	simFastest = new Thread(new simulateFastestPath(mGui,rBot,initialMap));
    	    	simFastest.start();
    	    	
    	    	fastestBtn.setEnabled(false);
    		}
    		
    	});
    	
    	realrunBtn.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
			
				simRealRun = new Thread(new simulateRealRun(mGui,rBot,initialMap));
		    	simRealRun.start();
		    	
		    	realrunBtn.setEnabled(false);
			}
    		
    	});
    	resetBtn.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				
				if(simExplore!=null) {
					simExplore.interrupt();
					simExplore = null;
				}
				if(simFastest != null) {
					simFastest.interrupt();
					simFastest = null;
				}
				if(simRealRun != null) {
					System.out.println("RealRunGUIinterrupt");
					
					TCPComm.getInstance().closeConnection();
					simRealRun.interrupt();
					simRealRun = null;
				}
				
				if(simb4!=null) {
					System.out.println("Enter");
					simb4.interrupt();
					simb4 = null;
				}
				
				
				setMapRobotObj();
		        paintResult();							//repaint the map UI
		       
		        
		        //reseting UI control		        
		        acontrolPanel.waypointRow_cb.setSelectedIndex(0);
		        acontrolPanel.waypointCol_cb.setSelectedIndex(0);
		        acontrolPanel.lb_percentDisplay.setText("0%");
		        acontrolPanel.lb_timerDisplay.setText("0");
		        exploreBtn.setEnabled(true);
		        fastestBtn.setEnabled(true);
		        realrunBtn.setEnabled(true);
		        displayMsgToUI("Map and robot has been reset!");
			}
    		
    	});
    	b3.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				
				//main.executeExplorateSimulation(fileDDL.getSelectedItem().toString());
				Thread simTest;
				//rBot.setPosRow(7);
				//rBot.setPosCol(7);
				simTest = new Thread(new testMain(mGui,initialMap,rBot));
			    simTest.start();
				
			    	
				}
			});
    	b4.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				
				simb4 = new Thread(new testingone());
			    simb4.start();
					
				}
			});
    	
        // Define panels to hold the control buttons
        controlPanel = new JPanel();
        controlPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        controlPanel.setLayout(new GridLayout(11, 1, 0, 10));
        
        controlPanel.add(fileDDL);
        controlPanel.add(exploreBtn);
        controlPanel.add(fastestBtn);
        controlPanel.add(resetBtn);
        controlPanel.add(realrunBtn);
        controlPanel.add(b4);
      
        
        controlPanel.setPreferredSize(new Dimension(300, 700));
        controlPanel.setBackground(Color.blue);
    }
    
    private class settingPanel extends JPanel {
    	
    	int mainXcoord = 0;
    	int mainYcoord = 0;
    	
    	JLabel lb_movecontrol = new JLabel("Robot Control:");
    	JLabel lb_waypoint = new JLabel("WayPoint(Y,X):");
    	JLabel lb_percent = new JLabel("Coverage Percentage:");
    	JLabel lb_speed = new JLabel("Speed(steps/sec):");
    	JLabel lb_timelimit = new JLabel("Time Limit:");
    	JLabel lb_timerDisplayHeader = new JLabel("Timer(sec) : ");
    	JLabel lb_percentDisplayHeader = new JLabel("Current Coverage: ");
    	JLabel lb_percentDisplay = new JLabel("0%");
    	JLabel lb_timerDisplay = new JLabel("0");
    	
    	JTextArea ta_percent = new JTextArea("100");
    	JTextArea ta_speed = new JTextArea("10");
    	JTextArea ta_timelimit = new JTextArea("0");
    	
    	//JButton upMoveBtn = new JButton("↑");
    	//JButton leftMoveBtn = new JButton("←");
    	//JButton rightMoveBtn= new JButton("→");
    	//JButton downMoveBtn = new JButton("↓");
    	
    	
    	JComboBox waypointRow_cb = new JComboBox(getNumString(1,18));
    	JComboBox waypointCol_cb = new JComboBox(getNumString(1,13));
    	
    	JTextArea editTextArea;
    	JScrollPane scroll;
    	
    	public settingPanel() {
    		
    	scroll = new JScrollPane (JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        editTextArea = new JTextArea ();
        editTextArea.setFont(new Font("Monospaced",Font.PLAIN,15));
        editTextArea.setLineWrap(true);
        editTextArea.setEditable(false);
        scroll.getViewport().add(editTextArea);
        
       // upMoveBtn.setFont(new Font("Monospaced",Font.PLAIN,10));
       // leftMoveBtn.setFont(new Font("Monospaced",Font.PLAIN,10));
       // downMoveBtn.setFont(new Font("Monospaced",Font.PLAIN,10));
       // rightMoveBtn.setFont(new Font("Monospaced",Font.PLAIN,10));
        
        lb_percentDisplayHeader.setBounds(mainXcoord +10, mainYcoord +10,120,40);
        lb_percentDisplay.setBounds(mainXcoord+150,mainYcoord+10,120,40);
        
        lb_timerDisplayHeader.setBounds(mainXcoord +10, mainYcoord +60,100,40);
        lb_timerDisplay.setBounds(mainXcoord + 150,mainYcoord+60,120,40);
        
         // Set the size (x, y, width, height) of the UI label
        //lb_movecontrol.setBounds(mainXcoord + 10, mainYcoord + 120,100,20);	
        //  upMoveBtn.setBounds(mainXcoord + 210, mainYcoord + 110,40,30);	
        // leftMoveBtn.setBounds(mainXcoord + 150, mainYcoord + 150,45,30);	
        //  downMoveBtn.setBounds(mainXcoord + 210, mainYcoord + 150,40,30);	
        // rightMoveBtn.setBounds(mainXcoord + 270, mainYcoord + 150,45,30);	
        
         lb_waypoint.setBounds(mainXcoord + 10, mainYcoord + 110,100,20);
         waypointRow_cb.setBounds(mainXcoord + 150, mainYcoord + 110,100,30);
         waypointCol_cb.setBounds(mainXcoord + 300, mainYcoord + 110,100,30);        
         
         lb_percent.setBounds(mainXcoord + 10, mainYcoord + 150,150,20);
         ta_percent.setBounds(mainXcoord + 150, mainYcoord + 150,150,20);
           
         lb_speed.setBounds(mainXcoord + 10, mainYcoord + 180,100,20);
         ta_speed.setBounds(mainXcoord + 150, mainYcoord + 180,100,20);
         
         lb_timelimit.setBounds(mainXcoord+ 10, mainYcoord + 210,100,20);
         ta_timelimit.setBounds(mainXcoord+ 150, mainYcoord + 210,100,20);
         
         scroll.setBounds(mainXcoord + 10, mainYcoord + 250,550,500);
        
         lb_percentDisplay.setPreferredSize(new Dimension(300, 700));
     	lb_percentDisplay.setOpaque(true);
     	lb_percentDisplay.setBackground(Color.WHITE);
     	lb_percentDisplay.setFont(new Font("Monospaced",Font.BOLD,25));
     	lb_percentDisplay.setHorizontalAlignment(JLabel.CENTER);
      
        lb_timerDisplay.setPreferredSize(new Dimension(300, 700));
        lb_timerDisplay.setOpaque(true);
        lb_timerDisplay.setBackground(Color.WHITE);
        lb_timerDisplay.setFont(new Font("Monospaced",Font.BOLD,25));
        lb_timerDisplay.setHorizontalAlignment(JLabel.CENTER);
     	
         setLayout(null);
         setBackground(Color.yellow);
         setPreferredSize(new Dimension(576, 756));
         add(lb_percentDisplayHeader);
         add(lb_timerDisplayHeader);
         add(lb_timerDisplay);
         add(lb_percentDisplay);
                
         //add(lb_movecontrol);
         //add(upMoveBtn);
         //add(rightMoveBtn);
         //add(leftMoveBtn);
         //add(downMoveBtn);
             
         add(lb_waypoint);
         add(waypointRow_cb);
         add(waypointCol_cb);
         
         add(lb_percent);
         add(ta_percent);
         
         add(lb_speed);
         add(ta_speed);
         
         add(lb_timelimit);
         add(ta_timelimit);
         
         add(scroll);
         
         waypointRow_cb.addItemListener(new ItemListener() {

             @Override
             public void itemStateChanged(ItemEvent e) {
            	 initialMap.setWayPoint(Integer.parseInt(waypointRow_cb.getSelectedItem().toString())
            			 ,Integer.parseInt(waypointCol_cb.getSelectedItem().toString()) );
            	 paintResult();
             }
         });
         
         waypointCol_cb.addItemListener(new ItemListener() {

             @Override
             public void itemStateChanged(ItemEvent e) {
            	 initialMap.setWayPoint(Integer.parseInt(waypointRow_cb.getSelectedItem().toString())
            			 ,Integer.parseInt(waypointCol_cb.getSelectedItem().toString()) );
            	 paintResult();
             }
         });
         }
         
      	
         private String[] getNumString(int startNum, int endNum) {
        	 String[] numArr = new String[endNum-startNum+1];
        	 int counter = 0;
        	 for(int i=startNum;i<=endNum;i++) {
        		 numArr[counter] = Integer.toString(i);
        			counter++;	 
        	 }
        	 return numArr;
         }   
    }
    
	//This method get user selected waypoint.  int[0] store row, int[1] store col.
	public int[] getUserWayPoint() {
		int[] waypointArr = new int[2];
		waypointArr[0] = Integer.parseInt(this.acontrolPanel.waypointRow_cb.getSelectedItem().toString());
		waypointArr[1] = Integer.parseInt(this.acontrolPanel.waypointCol_cb.getSelectedItem().toString());
		
		return waypointArr;
	}
	//This method get percentage set by user on UI 
	public int getUserPercentage() {
		int coverage=-1;
		
		coverage = Integer.parseInt(this.acontrolPanel.ta_percent.getText().toString());
		return coverage;
	}
	//This method get timelimit set by user on UI
	public int getUserTimeLimit() {
		int timeLimit = 0;
		timeLimit = Integer.parseInt(this.acontrolPanel.ta_timelimit.getText().toString());
		return timeLimit;
	}
	//This method get step/sec for robot movement set by user on UI
	public float getUserSpeed() {
		float speed = 1;
		speed = Float.parseFloat(this.acontrolPanel.ta_speed.getText().toString());
		return speed;
	}
	
	//This method display msg to the UI Scroll EditText
    public void displayMsgToUI(String msg) {
    	this.acontrolPanel.editTextArea.append(msg + "\n");
    	this.acontrolPanel.editTextArea.setCaretPosition(this.acontrolPanel.editTextArea.getText().length());
    }
    //This method display current map coverage
    public void displayMapCoverToUI(double percent) {
    	this.acontrolPanel.lb_percentDisplay.setText((int)percent + " %");
    }
    
    public void displayTimerToUI(int timeValue) {
    	this.acontrolPanel.lb_timerDisplay.setText(String.valueOf(timeValue));
    }
    /**
     * This method updates the GUI on how the current map looks like with the robot on it. 
     * It does not modifies the map object with robot object
     * @param iMap  The current Map that is going to be display
     * @param robot The current robot object
     */
    public void paintResult() {
	
		for(int i=0;i<Constants.MAX_ROW;i++) {
			for(int y=0;y<Constants.MAX_COL;y++) {
				Cell cellObj = initialMap.getMapGrid()[i][y];
				
				//resultMap[i+1][y+1].setText(Character.toString(cellObj.getCellType()));
				if(cellObj.isObstacle()) {
					resultMap[i+1][y+1].setBackground(getMapColorForCell('W'));
				}
				else if(cellObj.getExploredState()) {
					resultMap[i+1][y+1].setBackground(getMapColorForCell('P'));
				}
				else {
					//Unexplored
					resultMap[i+1][y+1].setBackground(getMapColorForCell('U'));
				}
				
				
			}
		
		}
		Cell startZone = initialMap.getStartGoalPosition();
		Cell endZone =initialMap.getEndGoalPosition();
		//print waypoint on label
		
		resultMap[initialMap.getWayPoint().getRowPos()+1][initialMap.getWayPoint().getColPos()+1].setBackground(getMapColorForCell('B'));
		
		for(int i: Constants.WITHIN_3BY3) {
			for(int y: Constants.WITHIN_3BY3) {
				
				resultMap[startZone.getRowPos()+i+1][startZone.getColPos()+y+1].setBackground(getMapColorForCell('S'));
				resultMap[endZone.getRowPos()+i+1][endZone.getColPos()+y+1].setBackground(getMapColorForCell('E'));
				//resultMap[robot.getPosRow()+i][robot.getPosCol()+y].setBackground(getMapColorForCell('R'));
				
			}
		}
		
		for(int i: Constants.WITHIN_3BY3) {
			for(int y: Constants.WITHIN_3BY3) {
				resultMap[rBot.getPosRow()+i + 1][rBot.getPosCol()+y+ 1].setBackground(getMapColorForCell('R'));
				
			}
		}
		
	
		
		switch(rBot.getCurrDir().toString()) {
		
		case "NORTH":	resultMap[rBot.getPosRow()+2][rBot.getPosCol()+1].setBackground(getMapColorForCell('H'));
						//resultMap[robot.getPosRow()+2][robot.getPosCol()+1].setText("F");
						break;
		case "EAST": 	resultMap[rBot.getPosRow()+1][rBot.getPosCol()+2].setBackground(getMapColorForCell('H'));
						//resultMap[robot.getPosRow()+1][robot.getPosCol()+2].setText("F");
						break;
		case "SOUTH":   resultMap[rBot.getPosRow()][rBot.getPosCol()+1].setBackground(getMapColorForCell('H'));
						//resultMap[robot.getPosRow()][robot.getPosCol()+1].setText("F");
						break;
		case "WEST": 	resultMap[rBot.getPosRow()+1][rBot.getPosCol()].setBackground(getMapColorForCell('H'));
					 	//resultMap[robot.getPosRow()+1][robot.getPosCol()].setText("F");
					 	break;
		
		}
		
	}
    
    /**
     * This method print the map based what each cell represents and the explored/unexplored state on System Console
     * @param m
     */
    public void printFinal() {
    	
    	System.out.println("Way Point -  Row: "+initialMap.getWayPoint().getRowPos()+ " , Col: "+initialMap.getWayPoint().getColPos());
    	String msg = "Way Point -  Row: "+initialMap.getWayPoint().getRowPos()+ " , Col: "+initialMap.getWayPoint().getColPos()+"\n";
    	
		System.out.println("-------------------Map--------------------------");
		 msg += "----------------Map---------------------\n";
		
		for(int i=Constants.MAX_ROW-1;i>=0;i--) {
			
			for(int y=0;y<Constants.MAX_COL;y++) {
				Cell cellObj = initialMap.getMapGrid()[i][y];
				if(cellObj.getExploredState()) {
					if(cellObj.isObstacle()) {
						System.out.print("W"+" ");
						msg += "W ";
					}
					
					else {
						System.out.print("0"+" ");
						msg += "0 ";
					}
				}
				else {
					System.out.print("X"+" ");
					msg += "X ";
				}
			}
			System.out.println("");
			msg+="\n";
		}
		System.out.println("X - Unexplored, 0 - ExploredPath, W - Wall/Obstacles");
		msg+="X - Unexplored, 0 - ExploredPath, W - Wall/Obstacles \n\n ";
		
		System.out.println("----------------ExploreState----------------------");
		msg += "----------------ExploreState----------------------\n";
		for(int i=Constants.MAX_ROW-1;i>=0;i--) {
			
			for(int y=0;y<Constants.MAX_COL;y++) {
				Cell cellObj = initialMap.getMapGrid()[i][y];
				if(cellObj.getExploredState()) {
					System.out.print("O" + " ");
					msg += "O ";
				}
				else {
					System.out.print("X"+" ");
					msg += "X ";
				}
				
			}
			System.out.println("");
			msg+="\n";
		}
		System.out.println("X - Unexplored" +"  " +"O - Explored");
		msg += "X - Unexplored, O - Explored";
		displayMsgToUI(msg);
	}
	
    /**
     * This method return the color of what each celltype represents
     * @param cellType 
     * @return
     */
    private Color getMapColorForCell(char cellType) {
		
    	Color cellColor = null;
		switch(cellType) {
			case 'U': cellColor = Color.white; break;	    //Unexplored color
			case 'R' : cellColor = Color.cyan; break;		//Robot color
			case 'S' : cellColor = Color.orange; break;		//startZone color
			case 'E' : cellColor = Color.yellow; break;		//endZone color
			case 'P' : cellColor = Color.green; break;		//Explored path color
			case 'W' : cellColor = Color.red; break;		//Wall,Obstacles color
			case 'H' : cellColor = Color.pink;break;		//Robot head color
			case 'B' : cellColor = Color.gray; break;		//Waypoint color
			default: cellColor = Color.black; 				//Error color
		}
		return cellColor;
    }
    
    private class MapPanel extends JPanel {
        private JLabel[][] cellLabels;
        private boolean isClickable; // Allows mouse click on map when true

        /**
         * Creates a MapPanel
         *
         * @param isClickable Allow for mouse click when true.
         */
        public MapPanel(boolean isClickable) {
            super(new GridLayout(Constants.MAX_ROW + 1, Constants.MAX_COL + 1));
            this.isClickable = isClickable;
            this.setPreferredSize(new Dimension(576, 756));
            populateMapPanel();
            if (isClickable)
                setupClick();
        }
        
        public JLabel[][] getJLabelMap(){
        	return this.cellLabels;
        }
        
        private void populateMapPanel() {
            cellLabels = new JLabel[Constants.MAX_ROW + 1][Constants.MAX_COL + 1];

            for (int row = Constants.MAX_ROW; row >= 0; row--) {
                for (int col = 0; col < Constants.MAX_COL + 1; col++) {
                    int xCoord = col - 1;
                    int yCoord = row - 1;
                    cellLabels[row][col] = new JLabel("", SwingConstants.CENTER);

                    if (row == 0 && col == 0)
                        // Set label of bottom left most cell to be empty
                        cellLabels[row][col].setText("");
                    else if (row == 0)
                        // Labels for x axis
                        cellLabels[row][col].setText(Integer.toString(xCoord));
                    else if (col == 0)
                        // Labels for y axis
                        cellLabels[row][col].setText(Integer.toString(yCoord));

                    // Border for the map's cell
                    if (row != 0 && col != 0)
                        cellLabels[row][col].setBorder(BorderFactory.createLineBorder(Color.BLACK));
                    
                    cellLabels[row][col].setOpaque(true);
                    this.add(cellLabels[row][col]);
                }
            }
        }

        private void setupClick() {
            for (int row = Constants.MAX_ROW; row >= 0; row--) {
                for (int col = 0; col < Constants.MAX_COL + 1; col++) {
                    // Adding mouse listener to the labels to handle mouse click event
                    cellLabels[row][col].addMouseListener(new MouseListener() {
                        @Override
                        public void mouseClicked(MouseEvent e) {

                        }

                        @Override
                        public void mousePressed(MouseEvent e) {
                            Object source = e.getSource();
                            for (int row = Constants.MAX_ROW; row >= 0; row--) {
                                for (int col = 0; col < Constants.MAX_COL + 1; col++) {
                                    if (cellLabels[row][col] == source) {
                                        cellLabels[row][col].setText("X");

                                    }
                                }
                            }
                        }

                        @Override
                        public void mouseReleased(MouseEvent e) {

                        }

                        @Override
                        public void mouseEntered(MouseEvent e) {

                        }

                        @Override
                        public void mouseExited(MouseEvent e) {

                        }
                    });
                }
            }
        }
    }

}
