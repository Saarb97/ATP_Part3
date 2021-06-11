package View;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;

public class NewGameViewController {

    @FXML
    private TextField rowSize;

    @FXML
    private TextField columnSize;

    public int[] processInput() {
        String mazeRowSize = rowSize.getText().trim();
        String mazeColumnSize = columnSize.getText().trim();

        if (illegalInput())
            return new int[]{-1, -1};

        return new int[]{Integer.parseInt(mazeRowSize), Integer.parseInt(mazeColumnSize)};
    }

    private boolean illegalInput() {
        try {
            if (getRow().equals("") || getColumn().equals("")) {
                raiseAlert("Error!",
                        "",
                        "No field can be left empty.");
                return true;
            }
            else if (!isANumber()) {
                raiseAlert("Error!",
                        "",
                        "Please only enter numbers larger than 2 and smaller than 1001");
                return true;
            }
            else if (Integer.parseInt(getRow()) < 2 || Integer.parseInt(getColumn()) < 2) {
                raiseAlert("Error!",
                        "",
                        "You must enter a valid size. \n" +
                                "Please only enter numbers larger than 2 and smaller than 1001");
                return true;
            }
            return false;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private boolean isANumber() {
        try {
            Integer.parseInt(getRow());
            Integer.parseInt(getColumn());
        } catch(NumberFormatException | NullPointerException e) {
            return false;
        }
        return true;
    }

    private String getRow() {
        return rowSize.getText().trim();
    }

    private String getColumn() {
        return columnSize.getText().trim();
    }

    private void raiseAlert(String title, String headerText, String info) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(headerText);
        alert.setContentText(info);
        alert.showAndWait();
    }

}
