package View;

import ViewModel.MyViewModel;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.MenuItem;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.BorderPane;
import java.io.File;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.function.Function;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class MyViewController implements IView, Observer, Initializable {

    MyViewModel viewModel;
    boolean isUnderCalculations = false;
    static Media sound;
    private static boolean isSongPlaying = false;
    public static MediaPlayer mediaPlayer;
    MazeDisplayer mazeDisplay;
    public Hashtable<String, Function> commandTable;

    @FXML
    private BorderPane mainBorderPane;
    @FXML
    MazeDisplayer mazeDisplayer;
    @FXML
    MenuItem getSolution;
    @FXML
    MenuItem newGame;
    @FXML
    MenuItem openGame;
    @FXML
    MenuItem saveGame;



    public static void startPlayingMusic() {
        stopMusic();
        isSongPlaying = true;
        String musicFile = "resources/sounds/gamesound.mp3";
        sound = new Media(new File(musicFile).toURI().toString());
        mediaPlayer = new MediaPlayer(sound);
        mediaPlayer.setVolume(0.1); //TEMP
        mediaPlayer.play();

    }

    public static void startWinningMusic() {
        stopMusic();
        String musicFile = "resources/sounds/victorysound.mp3";
        sound = new Media(new File(musicFile).toURI().toString());
        mediaPlayer = new MediaPlayer(sound);
        mediaPlayer.setVolume(0.1); //TEMP
        mediaPlayer.play();

    }

    public static void stopMusic() {
        if (isSongPlaying == true)
            mediaPlayer.stop();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        startPlayingMusic();
        mazeDisplayer.setMyViewController(this);
        mazeDisplayer.addEventFilter(MouseEvent.MOUSE_CLICKED, (e) -> mazeDisplayer.requestFocus());
        this.commandTable= new Hashtable<>();
        createGetSolution();
        createDisplayGame();
        createGenerateMaze();
        createSaveGame();
        createEnableAll();

    }

    @Override
    public void update(Observable o, Object arg) {
        System.out.println("update called");
        if (arg == null)
            return;
        isUnderCalculations = false;
        enableAll();
        executeCommand((String) arg);
        handleReachGoal();
    }

    public void handleReachGoal() {
        if (isCurrentStateAtGoalState()) {
            startWinningMusic();
            raiseAlert("Maze Solved", "Congratulations! \nYou solved the maze!", "Cleaning the maze");
            viewModel.deleteGame();
            mazeDisplayer.clearCanvas();
            enableAll();
        }
    }


    public void addDialogIconTo(Alert alert) {
        final Image APPLICATION_ICON = new Image("");
        Stage dialogStage = (Stage) alert.getDialogPane().getScene().getWindow();
        dialogStage.getIcons().add(APPLICATION_ICON);
    }

    private boolean isCurrentStateAtGoalState() {
        return viewModel.getCharacterPositionRow() == viewModel.getGoalStateRow() &&
                viewModel.getCharacterPositionColumn() == viewModel.getGoalStateColumn();
    }

    private void raiseAlert(String title, String headerText, String info) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(headerText);
        alert.setContentText(info);
        alert.showAndWait();
    }

    public void displayMaze(int[][] maze) {
        mazeDisplayer.addEventFilter(MouseEvent.MOUSE_CLICKED, (e) -> mazeDisplayer.requestFocus());
        setMazeDisplayerData(maze);
        mazeDisplayer.setPlayerPos(viewModel.getCharacterPositionRow(), viewModel.getCharacterPositionColumn());
        mazeDisplayer.redraw();
    }

    private void setMazeDisplayerData(int[][] maze) {
        mazeDisplayer.setMaze(maze);
        mazeDisplayer.setPlayerPos(viewModel.getCharacterPositionRow(), viewModel.getCharacterPositionColumn());
        mazeDisplayer.setGoalPosition(viewModel.getGoalStateRow(), viewModel.getGoalStateColumn());
    }

    void setViewModel(MyViewModel viewModel) {
        this.viewModel = viewModel;
    }

    @FXML
    public void showNewGameDialog() {

        Dialog<ButtonType> dialog = new Dialog<>();
        setDialog(dialog);
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(getClass().getResource("NewGameView.fxml"));
        loadFXML(dialog, fxmlLoader);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);
        Optional<ButtonType> result = dialog.showAndWait();
        handleOKClicked(result, fxmlLoader);
    }

    private void setDialog(Dialog<ButtonType> dialog) {
        dialog.initOwner(mainBorderPane.getScene().getWindow());
        dialog.setTitle("Create new game");
        dialog.setHeaderText("");
    }

    private void loadFXML(Dialog<ButtonType> dialog, FXMLLoader fxmlLoader) {
        try {
            dialog.getDialogPane().setContent(fxmlLoader.load());
        } catch (IOException e) {
        }
    }

    private void handleOKClicked(Optional<ButtonType> result, FXMLLoader fxmlLoader) {
        if (isUnderCalculations)
            return;
        if (result.isPresent() && result.get() == ButtonType.OK) {
            NewGameViewController controller = fxmlLoader.getController();
            int[] newGameSize = controller.processInput();

            try {
                viewModel.generateMaze(newGameSize[0], newGameSize[1]);
                disableAll();
            } catch (Exception e) {
                enableAll();
            }
        }
    }

    public void KeyPressed(KeyEvent keyEvent) {
        if (isUnderCalculations)
            return;
        viewModel.moveCharacter(keyEvent.getCode());
        keyEvent.consume();
    }

    public void mouseClicked() {
        this.mazeDisplay.requestFocus();
    }

    public void saveGameAction() {
        if (isUnderCalculations)
            return;
        viewModel.saveGame();
    }


    public void openGameAction() {
        viewModel.openGame();
    }

    public void getSolutionAction() {
        try {
            disableAll();
            viewModel.solveGame();
        } catch (Exception e) {
            enableAll();
        }
    }

    public void propertiesAction() { //TODO changeable properties
        raiseAlert("Properties", "", "Number of threads : " + 5 + "\n" +
                "Maze Generation Algorithm : " + "Prim Algorithm" + "\n" +
                "Maze Solving Algorithm :  Breadth first search" );
    }

    public void exitGameAction() {
        viewModel.exitGame();
    }

    public void gameInstructions() {
        raiseAlert("Game instructions",
                "",
                "This game is a basic maze solving game. \n" +
                        "The character can be moved in all 8 directions, but not through the maze walls!  \n" +
                        "You can create a new game, load or save a game in the file menu. \n" +
                        "If you need some help, you can display the solution by clicking on the solution button in the menu.");
    }

    public void gameRulesAction() {
        raiseAlert("Game rules",
                "",
                "guide your character through the maze. \n " +
                        "your character's location is marked with a picture of it.\n" +
                        "The exit point is also marked. \n" +
                        "Move your character with the numpad. \n" +
                        "Enjoy!");
    }

    public void solvingAlgorithmsAction() {
        raiseAlert("Maze Solving algorithm: ",
                "",
                "Breadth first search algorithm"); //TODO changeable settings
    }

    public void ownersAction() {
        raiseAlert("Game Creators:",
                "",
                "Saar and Daniel");
    }

    void setResizeEvent(Scene scene) {
        scene.widthProperty().addListener((observableValue, oldSceneWidth, newSceneWidth) -> {
            mazeDisplayer.setWidth(newSceneWidth.doubleValue());
            mazeDisplayer.redraw();
        });
        scene.heightProperty().addListener((observableValue, oldSceneHeight, newSceneHeight) -> {
            mazeDisplayer.setHeight(newSceneHeight.doubleValue());
            mazeDisplayer.redraw();
        });
    }

    public void zoomMaze(ScrollEvent scrollEvent) {
        double zoomFactor = 0.001;
        double deltaY = scrollEvent.getDeltaY() * zoomFactor;
        Node p = mazeDisplayer;
        if (scrollEvent.isControlDown()) {
            p.setScaleX(p.getScaleX() + deltaY);
            p.setScaleY(p.getScaleY() + deltaY);
        }
    }

    public void disableAll() {
        isUnderCalculations = true;
        saveGame.setDisable(true);
        openGame.setDisable(true);
        newGame.setDisable(true);
        getSolution.setDisable(true);
    }

    public void enableAll() {
        isUnderCalculations = false;
        saveGame.setDisable(false);
        openGame.setDisable(false);
        newGame.setDisable(false);
        getSolution.setDisable(false);
    }

    public void addCommand(Function<int[][], Boolean> command, String commandName) {
        commandTable.put(commandName, command);
    }

    private void createGenerateMaze() {
        Function<int[][], Boolean> getSolutionFunc = (int[][] maze) -> {
            startPlayingMusic();
            displayMaze(viewModel.getMaze());
            return true;
        };
        addCommand(getSolutionFunc, "generateMaze");
    }

    private void createSaveGame() {
        Function<int[][], Boolean> getSolutionFunc = (int[][] maze) -> {
            enableAll();
            return true;
        };
        addCommand(getSolutionFunc, "SaveGame");
    }

    private void createEnableAll() {
        Function<int[][], Boolean> getSolutionFunc = (int[][] maze) -> {
            enableAll();
            return true;
        };
        addCommand(getSolutionFunc, "EnableAll");
    }

    private void createGetSolution() {
        Function<int[][], Boolean> getSolutionFunc = (int[][] maze) -> {
            mazeDisplayer.drawSolution(viewModel.getGameSolution());
            return true;
        };
        addCommand(getSolutionFunc, "getSolution");
    }

    private void createDisplayGame() {
        Function<int[][], Boolean> displayGame = (int[][] maze) -> {
            displayMaze(viewModel.getMaze());
            return true;
        };
        addCommand(displayGame, "displayGame");

    }

    private void executeCommand(String command) {
        commandTable.get(command).apply(viewModel.getMaze());
    }




}