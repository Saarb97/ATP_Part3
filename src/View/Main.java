package View;

import Model.MyModel;
import ViewModel.MyViewModel;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.util.Optional;


public class Main extends Application {


    public static Stage primaryStage;

    @Override
    public void start(Stage primaryStage) throws Exception{
        Main.primaryStage = primaryStage;

        MyModel model=new MyModel();
        MyViewModel viewModel=new MyViewModel(model);
        model.addObserver(viewModel);

        FXMLLoader fxmlLoader = new FXMLLoader();
        Parent root = fxmlLoader.load(getClass().getResource("MyView.fxml").openStream());
        primaryStage.setTitle("Maze Game");
        Scene scene = new Scene(root, 1350, 700);
        scene.getStylesheets().add(getClass().getResource("MyViewStyle.css").toExternalForm());
        primaryStage.setScene(scene);
        primaryStage.show();

        MyViewController view = fxmlLoader.getController();
        view.setResizeEvent(scene);

        MyViewController mwc = fxmlLoader.getController();
        mwc.setViewModel(viewModel);
        viewModel.addObserver(mwc);

        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            public void handle(WindowEvent windowEvent) {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION,"Are you sure you want to quit the game?");
                Optional<ButtonType> result = alert.showAndWait();
                if (((Optional<?>) result).get() == ButtonType.OK){
                    model.exitGame();
                } else //user didn't click on OK
                    windowEvent.consume();

            }
        });



    }

    public static void main(String[] args) {
        launch(args);
    }

}
