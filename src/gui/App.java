package gui;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class App extends Application {
    @Override
    public void start(Stage stage) {
        StackPane root = new StackPane();
        Scene scene = new Scene(root, 300, 200, Color.WHITE);

        Text text = new Text("Welcome to RailwaySystem");
        text.setFont(Font.font("Verdana", 50));
        text.setFill(Color.web("#9d7a6aff"));

        root.getChildren().add(text);

        stage.setMaximized(true);
        
        stage.setTitle("Railway System");
        stage.setScene(scene);
        stage.show();
    }
}