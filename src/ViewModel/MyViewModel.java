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
        model.initiatePlayerMove(movement);
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
    public void mouseDragged(MouseEvent me,double height,double width) {
        model.mouseDrag(me,height,width);
    }



    public void saveGame(File file) {
        if (!model.isMazeExist()) {
            raiseAlert("Warning!",
                    "",
                    "No maze to be saved");
            return;
        }
        model.saveGame(file.getAbsolutePath());
    }


    public void openGame(File file) {
        model.openMazeFile(file.getAbsolutePath());
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
