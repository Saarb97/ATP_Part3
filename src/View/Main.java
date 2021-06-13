package View;

import Model.MyModel;
import ViewModel.MyViewModel;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;


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
        Scene scene = new Scene(root, 1200, 721);
        scene.getStylesheets().add(getClass().getResource("MyViewStyle.css").toExternalForm());
        primaryStage.setScene(scene);
        primaryStage.show();

        MyViewController view = fxmlLoader.getController();
        view.setResizeEvent(scene);

        MyViewController mwc = fxmlLoader.getController();
        mwc.setViewModel(viewModel);
        viewModel.addObserver(mwc);

        primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            public void handle(WindowEvent we) {
                model.exitGame();
            }
        });

    }

    public static void main(String[] args) {
        launch(args);
    }

}
