package util;

import constant.Constants;
import entity.Cell;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class FileManager {

    /**
     * This method create a 2D array of cell object which represent the arena from specified file location.
     *
     * @param mapFile File object which contains information of the arena
     * @return 2D array of cell which represent the arena.
     * @throws IOException If the file is not read.
     */
    public static Cell[][] readMapFromFile(File mapFile) throws IOException {
        Cell[][] tempGrid = new Cell[Constants.MAX_ROW][Constants.MAX_COL];

        for (int r = 0; r < Constants.MAX_ROW; r++) {
            for (int c = 0; c < Constants.MAX_COL; c++) {
                tempGrid[r][c] = new Cell(r, c);
            }
        }

        BufferedReader br = new BufferedReader(new FileReader(mapFile));

        String line;
        int readRow = Constants.MAX_ROW - 1;
        while ((line = br.readLine()) != null) {
            for (int readCol = 0; readCol < Constants.MAX_COL; readCol++) {
                if (line.toCharArray()[readCol] == '0') {
                    tempGrid[readRow][readCol].setObstacle(false);

                } else if (line.toCharArray()[readCol] == '1') {
                    tempGrid[readRow][readCol].setObstacle(true);
                }

            }
            readRow--;
        }

        return tempGrid;
    }

    /**
     * This method retrieve the names of all files that exist in the maps folder
     * in this project file
     *
     * @return Array of string that contains file names.
     */
    public static String[] getAllFileNames() {
        File folder = new File("maps\\");
        File[] listOfFiles = folder.listFiles();
        String[] fileNameList = new String[listOfFiles.length];
        for (int i = 0; i < listOfFiles.length; i++) {
            if (listOfFiles[i].isFile()) {
                fileNameList[i] = listOfFiles[i].getName();
            }
        }

        return fileNameList;
    }
}
