
// Java standard library imports
import java.io.File;
import java.util.List;

// JavaFX imports
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class RailConnectGUI extends Application {
    private RailwaySystem system;
    private Label statusLabel;

    // Search fields
    private TextField depCityField;
    private TextField arrCityField;
    private TextField depTimeField; // NEW: Departure time
    private TextField arrTimeField; // NEW: Arrival time
    private TextField trainTypeField; // NEW: User can type train type
    private ComboBox<String> daysCombo; // NEW: Days dropdown
    private CheckBox firstClassCheck;

    // Results area
    private TextArea resultsArea;

    public void start(Stage primaryStage) {
        system = new RailwaySystem();

        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10));

        // Top menu
        MenuBar menuBar = new MenuBar();
        Menu fileMenu = new Menu("File");
        MenuItem loadItem = new MenuItem("Load CSV...");
        loadItem.setOnAction(e -> loadFile(primaryStage));
        fileMenu.getItems().add(loadItem);
        menuBar.getMenus().add(fileMenu);
        root.setTop(menuBar);

        // Left search panel
        VBox searchPanel = new VBox(10);
        searchPanel.setPadding(new Insets(10));
        searchPanel.setPrefWidth(300);
        searchPanel.setStyle("-fx-background-color: #f9f9f9; -fx-border-color: #cccccc; -fx-border-width: 0 1 0 0;");

        Label titleLabel = new Label("Search Connections");
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        // Departure City
        Label depCityLabel = new Label("Departure City:");
        depCityField = new TextField();
        depCityField.setPromptText("e.g., Paris");

        // Arrival City
        Label arrCityLabel = new Label("Arrival City:");
        arrCityField = new TextField();
        arrCityField.setPromptText("e.g., Berlin");

        // NEW: Departure Time
        Label depTimeLabel = new Label("Earliest Departure (HH:MM):");
        depTimeField = new TextField();
        depTimeField.setPromptText("e.g., 09:00 (optional)");

        // NEW: Arrival Time
        Label arrTimeLabel = new Label("Latest Arrival (HH:MM):");
        arrTimeField = new TextField();
        arrTimeField.setPromptText("e.g., 18:00 (optional)");

        // NEW: Train Type (user can type)
        Label trainTypeLabel = new Label("Train Type:");
        trainTypeField = new TextField();
        trainTypeField.setPromptText("e.g., ICE, TGV (optional)");

        // NEW: Days of Operation (dropdown)
        Label daysLabel = new Label("Days of Operation:");
        daysCombo = new ComboBox<>();
        daysCombo.getItems().addAll(
                "Any", // No filter
                "Daily",
                "Mon-Fri",
                "Mon",
                "Tue",
                "Wed",
                "Thu",
                "Fri",
                "Sat",
                "Sun");
        daysCombo.setValue("Any");
        daysCombo.setMaxWidth(Double.MAX_VALUE);

        // First Class checkbox
        firstClassCheck = new CheckBox("First Class");

        // Search button
        Button searchButton = new Button("Search");
        searchButton.setMaxWidth(Double.MAX_VALUE);
        searchButton.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold;");
        searchButton.setOnAction(e -> search());

        // Clear button
        Button clearButton = new Button("Clear");
        clearButton.setMaxWidth(Double.MAX_VALUE);
        clearButton.setOnAction(e -> clear());

        // Add all to search panel
        searchPanel.getChildren().addAll(
                titleLabel,
                new Label(" "), // Spacer
                depCityLabel, depCityField,
                arrCityLabel, arrCityField,
                depTimeLabel, depTimeField,
                arrTimeLabel, arrTimeField,
                trainTypeLabel, trainTypeField,
                daysLabel, daysCombo,
                new Label(" "), // Spacer
                firstClassCheck,
                new Label(" "), // Spacer
                searchButton,
                clearButton);
        root.setLeft(searchPanel);

        // Center results
        VBox resultsPanel = new VBox(10);
        resultsPanel.setPadding(new Insets(10));

        Label resultsTitle = new Label("Search Results");
        resultsTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        resultsArea = new TextArea();
        resultsArea.setEditable(false);
        resultsArea.setWrapText(false);
        resultsArea.setStyle("-fx-font-family: 'Courier New'; -fx-font-size: 12px;");
        VBox.setVgrow(resultsArea, Priority.ALWAYS);

        resultsPanel.getChildren().addAll(resultsTitle, resultsArea);
        root.setCenter(resultsPanel);

        // Bottom status
        statusLabel = new Label("Ready. Load a CSV file to begin.");
        statusLabel.setStyle("-fx-padding: 5px; -fx-background-color: #f0f0f0;");
        root.setBottom(statusLabel);

        Scene scene = new Scene(root, 1200, 700);
        primaryStage.setTitle("Railway Connection Search System");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void loadFile(Stage stage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV", "*.csv"));
        File file = fileChooser.showOpenDialog(stage);

        if (file != null) {
            try {
                int count = system.loadConnectionData(file.getAbsolutePath());
                statusLabel.setText("Loaded " + count + " connections");
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setContentText("Loaded " + count + " connections successfully!");
                alert.showAndWait();
            } catch (Exception e) {
                statusLabel.setText("Error: " + e.getMessage());
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setContentText("Error loading file: " + e.getMessage());
                alert.showAndWait();
            }
        }
    }

    private void search() {
        // Check if data is loaded
        if (system.getConnectionCount() == 0) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setContentText("Please load a CSV file first");
            alert.showAndWait();
            return;
        }

        // Collect search parameters
        String depCity = depCityField.getText().trim();
        String arrCity = arrCityField.getText().trim();
        String depTime = depTimeField.getText().trim();
        String arrTime = arrTimeField.getText().trim();
        String trainType = trainTypeField.getText().trim();
        String days = daysCombo.getValue();
        boolean firstClass = firstClassCheck.isSelected();

        // Validate: at least one city must be entered
        if (depCity.isEmpty() && arrCity.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Invalid Search");
            alert.setContentText("Please enter at least a departure or arrival city.");
            alert.showAndWait();
            return;
        }

        try {
            statusLabel.setText("Searching...");

            // Convert "Any" to null for no filter
            String daysFilter = days.equals("Any") ? null : days;

            // Call search with all parameters (empty strings become null)
            List<Trip> trips = system.searchConnections(
                    depCity.isEmpty() ? null : depCity,
                    arrCity.isEmpty() ? null : arrCity,
                    depTime.isEmpty() ? null : depTime,
                    arrTime.isEmpty() ? null : arrTime,
                    trainType.isEmpty() ? null : trainType,
                    daysFilter,
                    firstClass);

            // Display results
            showResults(trips);
            statusLabel.setText("Found " + trips.size() + " trip(s)");

        } catch (Exception e) {
            statusLabel.setText("Search error: " + e.getMessage());
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Search Error");
            alert.setContentText("Error: " + e.getMessage());
            alert.showAndWait();
        }
    }

    private void showResults(List<Trip> trips) {
        StringBuilder sb = new StringBuilder();

        if (trips.isEmpty()) {
            sb.append("No trips found matching the criteria.\n");
        } else {
            sb.append("═══════════════════════════════════════════════════════════════════════════════\n");
            sb.append(String.format("                    FOUND %d TRIP(S)\n", trips.size()));
            sb.append("═══════════════════════════════════════════════════════════════════════════════\n\n");

            int num = 1;
            for (Trip trip : trips) {
                sb.append("TRIP #" + num);
                if (trip.getStopCount() == 0) {
                    sb.append(" - DIRECT\n");
                } else if (trip.getStopCount() == 1) {
                    sb.append(" - 1 STOP\n");
                } else {
                    sb.append(" - " + trip.getStopCount() + " STOPS\n");
                }

                sb.append(trip.getDepartureCity() + " → " + trip.getArrivalCity() + "\n");
                sb.append("Departs: " + trip.getDepartureTime() + " | Arrives: " + trip.getArrivalTime() + "\n");
                sb.append("Duration: " + trip.getFormattedDuration() + "\n");

                if (firstClassCheck.isSelected()) {
                    sb.append("Price: €" + String.format("%.2f", trip.getTotalFirstClassPrice()) + " (First Class)\n");
                } else {
                    sb.append(
                            "Price: €" + String.format("%.2f", trip.getTotalSecondClassPrice()) + " (Second Class)\n");
                }

                sb.append("\nSegments:\n");
                int segNum = 1;
                for (Segment seg : trip.getSegments()) {
                    Connection conn = seg.getConnection();
                    sb.append("  " + segNum + ". " + conn.getDepartureCity().getName() +
                            " → " + conn.getArrivalCity().getName() + "\n");
                    sb.append("     " + conn.getDepartureTime() + " - " + conn.getArrivalTime());
                    if (conn.isNextDay()) {
                        sb.append(" (+1d)");
                    }
                    sb.append("\n");
                    sb.append("     Train: " + conn.getTrain().getType() +
                            " | Days: " + conn.getDaysOfOperation() +
                            " | Duration: " + conn.getFormattedDuration() + "\n");
                    segNum++;
                }

                sb.append("\n───────────────────────────────────────────────────────────────────────────────\n\n");
                num++;
            }
        }

        resultsArea.setText(sb.toString());
        resultsArea.positionCaret(0);
    }

    private void clear() {
        depCityField.clear();
        arrCityField.clear();
        depTimeField.clear();
        arrTimeField.clear();
        trainTypeField.clear();
        daysCombo.setValue("Any");
        firstClassCheck.setSelected(false);
        resultsArea.clear();
        statusLabel.setText("Ready. Enter search criteria.");
    }

    public static void main(String[] args) {
        launch(args);
    }
}