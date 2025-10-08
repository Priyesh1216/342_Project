import javafx.application.Application;

import java.io.FileNotFoundException;
import java.io.IOException;

import gui.App;
import gui.FileSelection;

public class RailwaySystem {

    public static void main(String[] args) {

        System.out.println("Starting RailwaySystem");
        Application.launch(FileSelection.class, args);

        // Have reader read in data once the filepath selected in the GUI is returned
 
        // String dataFilePath = ""; // File path to the rail network CSV file
        // CSVReader reader = new CSVReader(dataFilePath);
        // reader.readData();
    }
}