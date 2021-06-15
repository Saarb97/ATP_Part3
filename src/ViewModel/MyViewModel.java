package ViewModel;

import Model.IModel;
import Model.MyModel;
import View.Main;
import javafx.scene.control.Alert;
import javafx.scene.input.KeyCode;
import javafx.stage.FileChooser;
import java.io.File;
import java.util.Observable;
import java.util.Observer;
import javafx.scene.input.MouseEvent;
import View.MazeDisplayer;


public class MyViewModel extends Observable implements Observer {

    IModel model;
    private int[][] maze = null;
    private int currentRowIndex;
    private int currentColumnIndex;
    public MazeDisplayer mazeDisplayer;


    public MyViewModel(MyModel viewModel) {
        this.model = viewModel;
    }

    @Override
    public void update(Observable o, Object arg) {
        if (o == model) {
            maze = model.getMazeBoard();
            setCurrentPosition();
            setGoalPosition();
            setChanged();
            notifyObservers((String) arg);
        }
    }

    private void setCurrentPosition() {
        currentRowIndex = model.getPlayerRow();
        currentColumnIndex = model.getPlayerCol();
    }

    private void setGoalPosition() {
    }


    private boolean isLegalSize(int rowSize, int columnSize) {
        return rowSize >= 10 || columnSize >= 10;
    }

    public int[][] getMaze() {
        return model.getMazeBoard();
    }

    public int getCharacterPositionRow() {
        return model.getPlayerRow();
    }

    public int getCharacterPositionColumn() {
        return model.getPlayerCol();
    }

    public int getGoalStateRow() {
        return model.getGoalRow();
    }

    public int getGoalStateColumn() {
        return model.getGoalCol();
    }


    private void raiseAlert(String title, String headerText, String info) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(headerText);
        alert.setContentText(info);
        alert.showAndWait();
    }

    public void moveCharacter(KeyCode movement) {
        if (isLegalMove(movement)) {
            model.movePlayer(getMovement(movement));
        }
        else{
            notifyObservers("EnableAll");
        }
    }
    public void solveGame() throws Exception {
        if (!model.isMazeExist()) {
            notifyObservers("EnableAll");
            raiseAlert("Warning",
                    "",
                    "Solution can't be created without a game.");
            throw new Exception();
        }
        else
            model.solveGame();
    }
    public void mouseDragged(MouseEvent mouseEvent) {
        int maxSize = Math.max(model.getMazeBoard()[0].length, model.getMazeBoard().length);
        double cellHeight = mazeDisplayer.getHeight() / maxSize;
        double cellWidth = mazeDisplayer.getWidth() / maxSize;
        double canvasHeight = mazeDisplayer.getHeight();
        double canvasWidth = mazeDisplayer.getWidth();
        int rowMazeSize = model.getMazeBoard().length;
        int colMazeSize = model.getMazeBoard()[0].length;
        double startRow = (canvasHeight / 2-(cellHeight * rowMazeSize / 2)) / cellHeight;
        double startCol = (canvasWidth / 2-(cellWidth * colMazeSize / 2)) / cellWidth;
        double mouseX = (int) ((mouseEvent.getX()) / (mazeDisplayer.getWidth() / maxSize)-startCol);
        double mouseY = (int) ((mouseEvent.getY()) / (mazeDisplayer.getHeight() / maxSize)-startRow);

        if (mouseY < model.getPlayerRow() && mouseX == model.getPlayerCol())
            moveCharacter(KeyCode.UP);
        if (mouseY > model.getPlayerRow() && mouseX == model.getPlayerCol())
            moveCharacter(KeyCode.DOWN);
        if (mouseX < model.getPlayerCol() && mouseY == model.getPlayerRow())
            moveCharacter(KeyCode.LEFT);
        if (mouseX > model.getPlayerCol() && mouseY == model.getPlayerRow())
            moveCharacter(KeyCode.RIGHT);
    }

    private boolean isLegalMove(KeyCode movement) {
        switch (getMovement(movement)) {
            case "UP":
                return isLegalMove(currentRowIndex - 1, currentColumnIndex);
            case "DOWN":
                return isLegalMove(currentRowIndex + 1, currentColumnIndex);
            case "RIGHT":
                return isLegalMove(currentRowIndex, currentColumnIndex + 1);
            case "LEFT":
                return isLegalMove(currentRowIndex, currentColumnIndex - 1);
            case "UP-LEFT":
                return isLegalMove(currentRowIndex - 1, currentColumnIndex - 1) && (isLegalMove(currentRowIndex - 1, currentColumnIndex) || isLegalMove(currentRowIndex , currentColumnIndex - 1));
            case "UP-RIGHT":
                return isLegalMove(currentRowIndex - 1, currentColumnIndex + 1) && (isLegalMove(currentRowIndex - 1, currentColumnIndex) || isLegalMove(currentRowIndex , currentColumnIndex + 1));
            case "DOWN-LEFT":
                return isLegalMove(currentRowIndex + 1, currentColumnIndex - 1) && (isLegalMove(currentRowIndex + 1, currentColumnIndex) || isLegalMove(currentRowIndex , currentColumnIndex - 1));
            case "DOWN-RIGHT":
                return isLegalMove(currentRowIndex + 1, currentColumnIndex + 1) && (isLegalMove(currentRowIndex + 1, currentColumnIndex) || isLegalMove(currentRowIndex , currentColumnIndex + 1));
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
        return maze[row][column] == 0;
    }

    private boolean isInBorders(int row, int column) {
        return row >= 0 && row < maze.length && column >= 0 && column < maze[0].length;
    }

    public void saveGame() {
        if (!model.isMazeExist()) {
            raiseAlert("Warning!",
                    "Warning!",
                    "Maze does not exist, cannot be saved.");
            return;
        }
        FileChooser fc = new FileChooser();
        setFileChooser(fc, "Save maze", "resources/MazeGames");
        fc.getExtensionFilters().addAll(new FileChooser.ExtensionFilter(".Maze", "*.Maze"));
        File file = fc.showSaveDialog(Main.primaryStage);
        if (file != null)
            model.saveGame(file.getAbsolutePath());
    }


    public void openGame() {
        FileChooser fc = new FileChooser();
        setFileChooser(fc, "Open maze", "resources/MazeGames");
        fc.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("*.Maze", "*.Maze"));
        try {
            File selectedFile = fc.showOpenDialog(null);
            if (selectedFile != null)
                model.openMazeFile(selectedFile.getAbsolutePath());
            else
                notifyObservers("EnableAll");
        } catch (Exception e) {
            notifyObservers("EnableAll");
            raiseAlert("Warning",
                    "",
                    "Only .Maze files can be opened");
        }
    }

    private void setFileChooser(FileChooser fc, String title, String initialDirectory) {
        fc.setTitle(title);
        fc.setInitialDirectory(new File(initialDirectory));
    }

    public void generateMaze(int rowSize, int columnSize) throws Exception {
        if (isLegalSize(rowSize, columnSize))
            model.generateMaze(rowSize, columnSize);
        else
            throw new Exception();
    }


    public int[][] getGameSolution() {
        return model.getGameSolution();
    }

    public void exitGame() {
        model.exitGame();
    }

    public void deleteGame() {
        model.deleteMaze();
    }

}
