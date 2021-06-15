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
                maze.print();
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
        notifyObservers("SaveGame");
        try {
            myCompressorOutputStream = new MyCompressorOutputStream(new FileOutputStream(path));
            myCompressorOutputStream.write(maze.toByteArray());
            myCompressorOutputStream.close();
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
}
