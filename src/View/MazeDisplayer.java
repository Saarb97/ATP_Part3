package View;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import java.io.FileInputStream;
import java.io.FileNotFoundException;



public class MazeDisplayer extends Canvas{

    private int[][] maze;
    private int characterPositionRow;
    private int characterPositionColumn;
    private int goalPositionRow;
    private int goalPositionColumn;
    MyViewController myViewController;

    public void setMyViewController(MyViewController myViewController) {
        this.myViewController = myViewController;
    }

    public void setMaze(int[][] maze) {
        this.maze = maze;
        redraw();
    }

    public void setPlayerPos(int row, int column) {
        characterPositionRow = row;
        characterPositionColumn = column;
        redraw();
    }

    void redraw() {
        myViewController.disableAll();
        if (maze != null) {
            try {
                initializeCanvasVariables();
                initializeImages();
                clearCanvas();

                for (int row = 0; row < getMazeRowSize(); row++) {
                    for (int column = 0; column < getMazeColumnSize(); column++) {
                        drawCanvasPosition(row, column);
                    }
                }
                drawCharacter();
                drawGoal();
            } catch (Exception e) {
            }
        }
        myViewController.enableAll();
    }

    private GraphicsContext graphicsContext;

    private void drawCanvasPosition(int row, int column) {
        if (isAWall(row, column))
            drawWall(row, column);
        else
            drawPath(row, column);
    }

    private double cellHeight;
    private double cellWidth;

    public double getCellHeight() {
        return cellHeight;
    }

    public double getCellWidth() {
        return cellWidth;
    }

    private void initializeCanvasVariables() {
        graphicsContext = getGraphicsContext2D();
        double canvasHeight = getHeight();
        double canvasWidth = getWidth();
        cellHeight = canvasHeight / getMazeRowSize();
        cellWidth = canvasWidth / getMazeColumnSize();
    }

    private Image wallImage;
    private Image characterImage;
    private Image goalImage;
    private Image pathImage;
    private Image solutionPathImage;

    private void initializeImages() {
        try {
            wallImage = new Image(new FileInputStream(ImageFileNameWall.get()));
            characterImage = new Image(new FileInputStream(ImageFileNameCharacter.get()));
            goalImage = new Image(new FileInputStream(ImageFileNameGoal.get()));
            pathImage = new Image(new FileInputStream(ImageFileNamePath.get()));
        } catch (FileNotFoundException ignored) {
        }
    }

    public void clearCanvas() {
        graphicsContext.clearRect(0, 0, getWidth(), getHeight());
    }

    private boolean isAWall(int row, int column) {
        return maze[row][column] == 1;
    }

    private void drawWall(int row, int column) {
        if (isWallImageExist())
            graphicsContext.drawImage(wallImage, column * cellWidth, row * cellHeight, cellWidth, cellHeight);
        else
            graphicsContext.fillOval(characterPositionColumn * cellHeight, characterPositionRow * cellWidth, cellHeight, cellWidth);
    }

    private boolean isWallImageExist() {
        return wallImage != null;
    }

    private void drawPath(int row, int column) {
        if (isPathImageExist())
            graphicsContext.drawImage(pathImage, column * cellWidth, row * cellHeight, cellWidth, cellHeight);
        else
            graphicsContext.fillOval(characterPositionColumn * cellHeight, characterPositionRow * cellWidth, cellHeight, cellWidth);
    }

    private boolean isPathImageExist() {
        return pathImage != null;
    }

    private void drawGoal() {
        if (isGoalImageExist())
            graphicsContext.drawImage(goalImage, goalPositionColumn * cellWidth, goalPositionRow * cellHeight, cellWidth, cellHeight);
        else
            graphicsContext.fillOval(characterPositionColumn * cellHeight, characterPositionRow * cellWidth, cellHeight, cellWidth);
    }

    private boolean isGoalImageExist() {
        return goalImage != null;
    }

    private void drawCharacter() {
        if (isCharacterImageExist())
            graphicsContext.drawImage(characterImage, characterPositionColumn * cellWidth, characterPositionRow * cellHeight, cellWidth, cellHeight);
        else
            graphicsContext.fillOval(characterPositionColumn * cellHeight, characterPositionRow * cellWidth, cellHeight, cellWidth);
    }

    private boolean isCharacterImageExist() {
        return characterImage != null;
    }

    private StringProperty ImageFileNameWall = new SimpleStringProperty();
    private StringProperty ImageFileNameCharacter = new SimpleStringProperty();
    private StringProperty ImageFileNameGoal = new SimpleStringProperty();
    private StringProperty ImageFileNamePath = new SimpleStringProperty();
    private StringProperty ImageFileNameSolutionPath = new SimpleStringProperty();


    public String getImageFileNamePath() {
        return ImageFileNamePath.get();
    }

    public StringProperty imageFileNamePathProperty() {
        return ImageFileNamePath;
    }

    public void setImageFileNamePath(String imageFileNamePath) {
        this.ImageFileNamePath.set(imageFileNamePath);
    }

    public String getImageFileNameWall() {
        return ImageFileNameWall.get();
    }

    public String getImageFileNameGoal() {
        return ImageFileNameGoal.get();
    }

    public StringProperty imageFileNameGoalProperty() {
        return ImageFileNameGoal;
    }

    public void setImageFileNameGoal(String imageFileNameGoal) {
        this.ImageFileNameGoal.set(imageFileNameGoal);
    }

    public void setImageFileNameWall(String imageFileNameWall) {
        this.ImageFileNameWall.set(imageFileNameWall);
    }

    public String getImageFileNameCharacter() {
        return ImageFileNameCharacter.get();
    }

    public void setImageFileNameCharacter(String imageFileNameCharacter) {
        this.ImageFileNameCharacter.set(imageFileNameCharacter);
    }

    public String getImageFileNameSolutionPath() {
        return ImageFileNameSolutionPath.get();
    }

    public void setImageFileNameSolutionPath(String ImageFileNameSolutionPath) {
        this.ImageFileNameSolutionPath.set(ImageFileNameSolutionPath);
    }

    void setGoalPosition(int goalStateRow, int goalStateColumn) {
        this.goalPositionRow = goalStateRow;
        this.goalPositionColumn = goalStateColumn;
    }

    private int getMazeRowSize() {
        return maze.length;
    }

    private int getMazeColumnSize() {
        return maze[0].length;
    }

    public void drawSolution(int[][] solution) {
        myViewController.disableAll();
        try {
            if (solution == null)
                return;
            for (int i = 1; i < solution.length - 1; i++) {
                drawSolutionPath(solution[i][0], solution[i][1]);
            }
        } catch (Exception e) {}
        finally {
            myViewController.enableAll();
        }
    }

    private void drawSolutionPath(int row, int column) {
        try {
            initializeSolutionPathImage();
            if (isSolutionPathAvailable())
                drawSolutionPathImage(row, column);
            else
                graphicsContext.fillRect(column * cellWidth, row * cellHeight, cellWidth, cellHeight);
        } catch (Exception e) {
        }
    }

    private void initializeSolutionPathImage() {
        try {
            solutionPathImage = new Image(new FileInputStream(ImageFileNameSolutionPath.get()));
        } catch (Exception e) {
        }
    }

    private boolean isSolutionPathAvailable() {
        return solutionPathImage != null;
    }

    private void drawSolutionPathImage(int row, int column) {
        graphicsContext.drawImage(solutionPathImage, column * cellWidth, row * cellHeight, cellWidth, cellHeight);
    }

}
