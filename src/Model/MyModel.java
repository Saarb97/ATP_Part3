package Model;

import Client.*;
import IO.MyCompressorOutputStream;
import IO.MyDecompressorInputStream;
import Server.Server;
import algorithms.mazeGenerators.Maze;
import java.io.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Observable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import Server.*;
import algorithms.mazeGenerators.Position;
import algorithms.search.AState;
import algorithms.search.MazeState;
import algorithms.search.Solution;
import javafx.application.Platform;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;


public class MyModel extends Observable implements IModel {

    private Maze maze = null;
    private int[][] mazeSolution;
    private ExecutorService executor;
    private Server mazeGeneratingServer;
    private Server solveSearchProblemServer;

    public MyModel() {
        executor = Executors.newFixedThreadPool(5);
        try {
            mazeGeneratingServer = new Server(5400, 1000, new ServerStrategyGenerateMaze());
            mazeGeneratingServer.start();
            solveSearchProblemServer = new Server(5401, 1000, new ServerStrategySolveSearchProblem());
            solveSearchProblemServer.start();
        } catch (Exception ignored) {
        }
    }

    public void generateMaze(int rowSize, int columnSize) {
        executor.execute(new Thread(() -> {
            try {
                Client client = new Client(InetAddress.getLocalHost(), 5400, (inFromServer, outToServer) -> {
                    try {
                        ObjectOutputStream toServer = new ObjectOutputStream(outToServer);
                        ObjectInputStream fromServer = new ObjectInputStream(inFromServer);
                        toServer.flush();
                        int[] mazeDimensions = new int[]{rowSize, columnSize};
                        toServer.writeObject(mazeDimensions);
                        toServer.flush();
                        byte[] compressedMaze = (byte[]) fromServer.readObject();
                        InputStream is = new MyDecompressorInputStream(new ByteArrayInputStream(compressedMaze));
                        byte[] decompressedMaze = new byte[1000000];
                        is.read(decompressedMaze);
                        maze = new Maze(decompressedMaze);
                    } catch (Exception var10) {
                    }

                });
                client.communicateWithServer();
                setChanged();
                notifyObservers("generateMaze");
            } catch (UnknownHostException var1) {
            }
        }));
    }

    public int[][] getMazeBoard() {

        return maze.getMazeArr();
    }

    private void setMazeValue(int[][] newMaze, int row, int column) {
        newMaze[row][column] = maze.getMazeArr()[row][column];
    }

    public int getPlayerRow() {
        return getPositionRow(maze.getStartPosition());
    }

    private int getPositionRow(Position position) {
        return position.getRowIndex();
    }

    public int getPlayerCol() {
        return maze.getStartPosition().getColumnIndex();
    }

    public int getGoalRow() {
        return maze.getGoalPosition().getRowIndex();
    }

    public int getGoalCol() {
        return maze.getGoalPosition().getColumnIndex();
    }

    public void initiatePlayerMove(KeyCode movement) {
        if (isLegalMove(movement)) {
            movePlayer(getMovement(movement));
        }
        else{
            notifyObservers("EnableAll");
        }
    }


    public boolean isLegalMove(KeyCode movement) {
        switch (getMovement(movement)) {
            case "UP":
                return isLegalMove(getPlayerRow() - 1, getPlayerCol());
            case "DOWN":
                return isLegalMove(getPlayerRow() + 1, getPlayerCol());
            case "RIGHT":
                return isLegalMove(getPlayerRow(), getPlayerCol() + 1);
            case "LEFT":
                return isLegalMove(getPlayerRow(), getPlayerCol() - 1);
            case "UP-LEFT":
                return isLegalMove(getPlayerRow() - 1, getPlayerCol() - 1) && (isLegalMove(getPlayerRow() - 1, getPlayerCol()) || isLegalMove(getPlayerRow() , getPlayerCol() - 1));
            case "UP-RIGHT":
                return isLegalMove(getPlayerRow() - 1, getPlayerCol() + 1) && (isLegalMove(getPlayerRow() - 1, getPlayerCol()) || isLegalMove(getPlayerRow() , getPlayerCol() + 1));
            case "DOWN-LEFT":
                return isLegalMove(getPlayerRow() + 1, getPlayerCol() - 1) && (isLegalMove(getPlayerRow() + 1, getPlayerCol()) || isLegalMove(getPlayerRow() , getPlayerCol() - 1));
            case "DOWN-RIGHT":
                return isLegalMove(getPlayerRow() + 1, getPlayerCol() + 1) && (isLegalMove(getPlayerRow() + 1, getPlayerCol()) || isLegalMove(getPlayerRow() , getPlayerCol() + 1));
            default:
                notifyObservers("EnableAll");
                return false;
        }
    }


    private String getMovement(KeyCode movement) {
        if (movement == KeyCode.UP || movement == KeyCode.NUMPAD8)
            return "UP";
        else if (movement == KeyCode.DOWN || movement == KeyCode.NUMPAD2)
            return "DOWN";
        else if (movement == KeyCode.LEFT || movement == KeyCode.NUMPAD4)
            return "LEFT";
        else if (movement == KeyCode.RIGHT || movement == KeyCode.NUMPAD6)
            return "RIGHT";
        else if (movement == KeyCode.NUMPAD7)
            return "UP-LEFT";
        else if (movement == KeyCode.NUMPAD9)
            return "UP-RIGHT";
        else if (movement == KeyCode.NUMPAD1)
            return "DOWN-LEFT";
        else if (movement == KeyCode.NUMPAD3 )
            return "DOWN-RIGHT";
        return "STAY";
    }

    private boolean isLegalMove(int row, int column) {
        return isInBorders(row, column) && isACorridor(row, column);
    }

    private boolean isACorridor(int row, int column) {
        return maze.getMazeArr()[row][column] == 0;
    }

    private boolean isInBorders(int row, int column) {
        return row >= 0 && row < maze.getMazeArr().length && column >= 0 && column < maze.getMazeArr()[0].length;
    }



    public void movePlayer(String movement) {
        switch (movement) {
            case "UP":
                maze.getStartPosition().setRow(maze.getStartPosition().getRowIndex() - 1);
                break;
            case "DOWN":
                maze.getStartPosition().setRow(maze.getStartPosition().getRowIndex() + 1);
                break;
            case "RIGHT":
                maze.getStartPosition().setColumn(maze.getStartPosition().getColumnIndex() + 1);
                break;
            case "LEFT":
                maze.getStartPosition().setColumn(maze.getStartPosition().getColumnIndex() - 1);
                break;
            case "UP-LEFT":
                maze.getStartPosition().setRow(maze.getStartPosition().getRowIndex() - 1);
                maze.getStartPosition().setColumn(maze.getStartPosition().getColumnIndex() - 1);
                break;
            case "UP-RIGHT":
                maze.getStartPosition().setRow(maze.getStartPosition().getRowIndex() - 1);
                maze.getStartPosition().setColumn(maze.getStartPosition().getColumnIndex() + 1);
                break;
            case "DOWN-LEFT":
                maze.getStartPosition().setRow(maze.getStartPosition().getRowIndex() + 1);
                maze.getStartPosition().setColumn(maze.getStartPosition().getColumnIndex() - 1);
                break;
            case "DOWN-RIGHT":
                maze.getStartPosition().setRow(maze.getStartPosition().getRowIndex() + 1);
                maze.getStartPosition().setColumn(maze.getStartPosition().getColumnIndex()+ 1);
                break;
        }

        setChanged();
        notifyObservers("displayGame");
    }

    public void saveGame(String path) {
        MyCompressorOutputStream myCompressorOutputStream;

        try {
            myCompressorOutputStream = new MyCompressorOutputStream(new FileOutputStream(path));
            myCompressorOutputStream.write(maze.toByteArray());
            myCompressorOutputStream.close();
            notifyObservers("SaveGame");
        } catch (IOException e) {
            notifyObservers("EnableAll");
        }
    }

    public boolean isMazeExist() {
        return maze != null;
    }

    public void openMazeFile(String path) {
        try {
            MyDecompressorInputStream in = new MyDecompressorInputStream(new FileInputStream(path));
            byte[] savedMazeBytes = new byte[1000000];
            in.read(savedMazeBytes);
            in.close();
            maze = new Maze(savedMazeBytes);
            setChanged();
            notifyObservers("generateMaze");
        } catch (IOException e) {
            notifyObservers("EnableAll");
        }
    }

    public void solveGame() {
        executor.execute(new Thread(() -> {
            try {
                Client client = new Client(InetAddress.getLocalHost(), 5401, (inFromServer, outToServer) -> {
                    try {
                        ObjectOutputStream toServer = new ObjectOutputStream(outToServer);
                        ObjectInputStream fromServer = new ObjectInputStream(inFromServer);
                        toServer.flush();
                        toServer.writeObject(maze);
                        toServer.flush();
                        Solution mazeSolutionFromServer = (Solution) fromServer.readObject();
                        ArrayList<AState> mazeSolutionSteps = mazeSolutionFromServer.getSolutionPath();
                        mazeSolution = new int[mazeSolutionFromServer.getSolutionPath().size()][2];

                        for (int i = 0; i < mazeSolutionSteps.size(); ++i) {
                            mazeSolution[i][0] = ((MazeState)mazeSolutionSteps.get(i)).getCurrentPos().getRowIndex();
                            mazeSolution[i][1] = ((MazeState)mazeSolutionSteps.get(i)).getCurrentPos().getColumnIndex();
                        }
                        setChanged();
                        notifyObservers("getSolution");
                    } catch (Exception var10) {
                        var10.printStackTrace();
                    }
                });
                client.communicateWithServer();
            } catch (UnknownHostException var1) {
                notifyObservers("EnableAll");
            }
        }));
    }

    public int[][] getGameSolution() {
        return mazeSolution;
    }

    public void exitGame() {
        executor.shutdown();
        mazeGeneratingServer.stop();
        solveSearchProblemServer.stop();
        Platform.exit();
        System.exit(0);
    }

    public void deleteMaze()
    {
        maze = null;
    }

    public void mouseDrag(MouseEvent me,double height, double width) {
        int[][] mazeArr = maze.getMazeArr();
        int maxSize = Math.max(mazeArr[0].length, mazeArr.length);
        double cellHeight = height / maxSize;
        double cellWidth = width / maxSize;
        double canvasHeight = height;
        double canvasWidth = width;
        int rowMazeSize = mazeArr.length;
        int colMazeSize = mazeArr[0].length;
        double startRow = (canvasHeight / 2-(cellHeight * rowMazeSize / 2)) / cellHeight;
        double startCol = (canvasWidth / 2-(cellWidth * colMazeSize / 2)) / cellWidth;
        double mouseX = (int) ((me.getX()) / (width / maxSize)-startCol);
        double mouseY = (int) ((me.getY()) / (height / maxSize)-startRow);

        if (mouseY < getPlayerRow() && mouseX ==getPlayerCol())
            initiatePlayerMove(KeyCode.UP);
        if (mouseY >getPlayerRow() && mouseX == getPlayerCol())
            initiatePlayerMove(KeyCode.DOWN);
        if (mouseX < getPlayerCol() && mouseY == getPlayerRow())
            initiatePlayerMove(KeyCode.LEFT);
        if (mouseX > getPlayerCol() && mouseY == getPlayerRow())
            initiatePlayerMove(KeyCode.RIGHT);
    }
}

