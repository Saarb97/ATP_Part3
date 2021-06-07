package Model;

import java.util.Observer;

public interface IModel {
    void generateMaze(int rowSize, int columnSize);
    int[][] getMazeBoard();
    int getPlayerRow();
    int getPlayerCol();
    int getGoalRow();
    int getGoalCol();
    void movePlayer(String movement);
    void saveGame(String path);
    boolean isMazeExist();
    void openMazeFile(String path);
    void solveGame();
    int[][] getGameSolution();
    void exitGame();
    void deleteMaze();
}
