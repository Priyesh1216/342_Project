package gui;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.io.File;

public class FileSelection extends Application {
    
    private String selectedFilePath = null;  // Store the file path
    
    @Override
    public void start(Stage primaryStage) {
        // Create the button
        Button chooseFileButton = new Button("Choose File");
        
        // Label to display selected file path
        Label filePathLabel = new Label("No file selected");
        
        // Set up the button action
        chooseFileButton.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Select a File");
            
            // Optional: Set initial directory
            // fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
            
            // Optional: Add file filters
            fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("All Files", "*.*"),
                new FileChooser.ExtensionFilter("Text Files", "*.txt"),
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
            );
            
            // Open the file chooser dialog
            File selectedFile = fileChooser.showOpenDialog(primaryStage);
            
            // Check if a file was selected
            if (selectedFile != null) {
                selectedFilePath = selectedFile.getAbsolutePath();
                filePathLabel.setText("Selected: " + selectedFilePath);
                System.out.println("File path: " + selectedFilePath);
            } else {
                filePathLabel.setText("No file selected");
            }
        });
        
        // Layout
        VBox layout = new VBox(10);
        layout.getChildren().addAll(chooseFileButton, filePathLabel);
        
        Scene scene = new Scene(layout, 500, 200);
        primaryStage.setScene(scene);
        primaryStage.setTitle("File Selection");
        primaryStage.show();
    }
}