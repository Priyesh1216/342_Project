
// Java standard library imports
import java.io.File;
import java.util.List;
import java.util.Comparator;

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
import javafx.scene.control.ScrollPane;
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
    private TextField depTimeField;
    private TextField arrTimeField;
    private TextField trainTypeField;
    private CheckBox firstClassCheck;
    private ComboBox<String> maxStopsCombo;
    private ComboBox<String> sortCombo;

    // Day checkboxes
    private CheckBox mondayCheck;
    private CheckBox tuesdayCheck;
    private CheckBox wednesdayCheck;
    private CheckBox thursdayCheck;
    private CheckBox fridayCheck;
    private CheckBox saturdayCheck;
    private CheckBox sundayCheck;
    private CheckBox dailyCheck;
    private CheckBox monFriCheck;

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

        // Departure Time
        Label depTimeLabel = new Label("Earliest Departure (HH:MM):");
        depTimeField = new TextField();
        depTimeField.setPromptText("e.g., 09:00 (optional)");

        // Arrival Time
        Label arrTimeLabel = new Label("Latest Arrival (HH:MM):");
        arrTimeField = new TextField();
        arrTimeField.setPromptText("e.g., 18:00 (optional)");

        // Train Type
        Label trainTypeLabel = new Label("Train Type:");
        trainTypeField = new TextField();
        trainTypeField.setPromptText("e.g., ICE, TGV (optional)");

        // Days of Operation - CHECKBOXES
        Label daysLabel = new Label("Days of Operation:");
        daysLabel.setStyle("-fx-font-weight: bold;");

        // Quick select options
        dailyCheck = new CheckBox("Daily (all days)");
        monFriCheck = new CheckBox("Mon-Fri (weekdays)");

        Label individualDaysLabel = new Label("Or select specific days:");
        individualDaysLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #666;");

        // Individual day checkboxes
        mondayCheck = new CheckBox("Monday");
        tuesdayCheck = new CheckBox("Tuesday");
        wednesdayCheck = new CheckBox("Wednesday");
        thursdayCheck = new CheckBox("Thursday");
        fridayCheck = new CheckBox("Friday");
        saturdayCheck = new CheckBox("Saturday");
        sundayCheck = new CheckBox("Sunday");

        // Group individual days in a VBox
        VBox individualDaysBox = new VBox(5);
        individualDaysBox.setPadding(new Insets(0, 0, 0, 15));
        individualDaysBox.getChildren().addAll(
                mondayCheck, tuesdayCheck, wednesdayCheck, thursdayCheck,
                fridayCheck, saturdayCheck, sundayCheck);

        // Max Stops dropdown
        Label maxStopsLabel = new Label("Max Stops:");
        maxStopsCombo = new ComboBox<>();
        maxStopsCombo.getItems().addAll(
                "Max 0 Stops",
                "Max 1 Stop",
                "Max 2 Stops");
        maxStopsCombo.setValue("Max 2 Stops");
        maxStopsCombo.setMaxWidth(Double.MAX_VALUE);

        // First Class checkbox
        firstClassCheck = new CheckBox("First Class");

        // Sort dropdown
        Label sortLabel = new Label("Sort By:");
        sortCombo = new ComboBox<>();
        sortCombo.getItems().addAll(
                "Sort by Duration",
                "Sort by Price");
        sortCombo.setValue("Sort by Duration");
        sortCombo.setMaxWidth(Double.MAX_VALUE);

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
                daysLabel,
                dailyCheck,
                monFriCheck,
                individualDaysLabel,
                individualDaysBox,
                maxStopsLabel, maxStopsCombo,
                sortLabel, sortCombo,
                new Label(" "), // Spacer
                firstClassCheck,
                new Label(" "), // Spacer
                searchButton,
                clearButton);

        // Wrap search panel in a ScrollPane
        ScrollPane searchScrollPane = new ScrollPane(searchPanel);
        searchScrollPane.setFitToWidth(true);
        searchScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        searchScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        root.setLeft(searchScrollPane);

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
        boolean firstClass = firstClassCheck.isSelected();
        String maxStops = maxStopsCombo.getValue();

        // Build days filter from checkboxes
        StringBuilder daysBuilder = new StringBuilder();
        if (dailyCheck.isSelected()) {
            daysBuilder.append("Daily");
        } else if (monFriCheck.isSelected()) {
            daysBuilder.append("Monday-Friday");
        } else {
            // Individual days
            if (mondayCheck.isSelected())
                daysBuilder.append("Monday,");
            if (tuesdayCheck.isSelected())
                daysBuilder.append("Tuesday,");
            if (wednesdayCheck.isSelected())
                daysBuilder.append("Wednesday,");
            if (thursdayCheck.isSelected())
                daysBuilder.append("Thursday,");
            if (fridayCheck.isSelected())
                daysBuilder.append("Friday,");
            if (saturdayCheck.isSelected())
                daysBuilder.append("Saturday,");
            if (sundayCheck.isSelected())
                daysBuilder.append("Sunday,");

            // Remove trailing comma
            if (daysBuilder.length() > 0) {
                daysBuilder.setLength(daysBuilder.length() - 1);
            }
        }

        String days = daysBuilder.length() > 0 ? daysBuilder.toString() : null;

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

            // Convert max stops selection to integer
            int maxStopsCount = 2;
            if (maxStops.equals("Max 0 Stops")) {
                maxStopsCount = 0;
            } else if (maxStops.equals("Max 1 Stop")) {
                maxStopsCount = 1;
            }

            // Call search with all parameters
            List<Trip> trips = system.searchConnections(
                    depCity.isEmpty() ? null : depCity,
                    arrCity.isEmpty() ? null : arrCity,
                    depTime.isEmpty() ? null : depTime,
                    arrTime.isEmpty() ? null : arrTime,
                    trainType.isEmpty() ? null : trainType,
                    days,
                    firstClass,
                    maxStopsCount);

            // Sort trips based on selected option
            String sortOption = sortCombo.getValue();
            if (sortOption.equals("Sort by Duration")) {
                trips.sort(Comparator.comparingInt(Trip::getTotalDurationMinutes));
            } else if (sortOption.equals("Sort by Price")) {
                trips.sort((trip1, trip2) -> {
                    double price1 = firstClass ? trip1.getTotalFirstClassPrice() : trip1.getTotalSecondClassPrice();
                    double price2 = firstClass ? trip2.getTotalFirstClassPrice() : trip2.getTotalSecondClassPrice();
                    return Double.compare(price1, price2);
                });
            }

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
                int totalTransferTime = 0;

                for (int i = 0; i < trip.getSegments().size(); i++) {
                    Segment seg = trip.getSegments().get(i);
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

                    // Calculate and display transfer time between segments
                    if (i < trip.getSegments().size() - 1) {
                        Segment nextSeg = trip.getSegments().get(i + 1);
                        Connection nextConn = nextSeg.getConnection();

                        int transferTime = calculateTransferTime(conn, nextConn);
                        totalTransferTime += transferTime;
                        sb.append("     Transfer time: " + formatDuration(transferTime) + "\n");
                    }

                    segNum++;
                }

                if (totalTransferTime > 0) {
                    sb.append("\nTotal transfer time: " + formatDuration(totalTransferTime) + "\n");
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
        dailyCheck.setSelected(false);
        monFriCheck.setSelected(false);
        mondayCheck.setSelected(false);
        tuesdayCheck.setSelected(false);
        wednesdayCheck.setSelected(false);
        thursdayCheck.setSelected(false);
        fridayCheck.setSelected(false);
        saturdayCheck.setSelected(false);
        sundayCheck.setSelected(false);
        maxStopsCombo.setValue("Max 2 Stops");
        sortCombo.setValue("Sort by Duration");
        firstClassCheck.setSelected(false);
        resultsArea.clear();
        statusLabel.setText("Ready. Enter search criteria.");
    }

    private int calculateTransferTime(Connection firstConn, Connection secondConn) {
        int arrivalMinutes = firstConn.getArrivalTime().getHour() * 60 + firstConn.getArrivalTime().getMinute();
        int departureMinutes = secondConn.getDepartureTime().getHour() * 60 + secondConn.getDepartureTime().getMinute();

        // Handle next day scenarios
        if (departureMinutes < arrivalMinutes) {
            departureMinutes += 24 * 60;
        }

        return departureMinutes - arrivalMinutes;
    }

    private String formatDuration(int minutes) {
        int hours = minutes / 60;
        int mins = minutes % 60;

        if (hours > 0) {
            return hours + "h " + mins + "m";
        } else {
            return mins + "m";
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}