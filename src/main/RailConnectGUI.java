
// Java standard library imports
import java.io.File;
import java.util.List;
import java.util.Set;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.time.DayOfWeek;

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
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
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
    private ComboBox<DayOfWeek> startDayCombo;
    private DayOfWeek lastSelectedStartDay;

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

    private Trip selectedTripForBooking;
    private List<Trip> currentSearchResults;
    private TripCollection tripCollection; // Collection for storing booked trips

    public void start(Stage primaryStage) {
        system = new RailwaySystem();
        tripCollection = new TripCollection(); // Initialize the collection (container for booked trips)

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

        // Departure day
        Label startDayLabel = new Label("Departing Day:");
        startDayCombo = new ComboBox<>();
        startDayCombo.getItems().addAll(
                DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
                DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY);
        startDayCombo.setValue(DayOfWeek.MONDAY);
        startDayCombo.setMaxWidth(Double.MAX_VALUE);

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
                startDayLabel, startDayCombo,
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

        // ADD BOOKING CONTROLS
        javafx.scene.layout.HBox bookingControls = new javafx.scene.layout.HBox(10);
        bookingControls.setPadding(new Insets(10, 0, 0, 0));

        Label tripNumLabel = new Label("Trip #:");
        TextField tripNumField = new TextField();
        tripNumField.setPrefWidth(60);
        tripNumField.setPromptText("1");

        Button bookButton = new Button("Book This Trip");
        bookButton.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-weight: bold;");
        bookButton.setOnAction(e -> {
            String tripNumStr = tripNumField.getText().trim();
            if (tripNumStr.isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setContentText("Please enter a trip number.");
                alert.showAndWait();
                return;
            }

            try {
                int tripNum = Integer.parseInt(tripNumStr);
                if (currentSearchResults == null || currentSearchResults.isEmpty()) {
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setContentText("No trips found. Please search first.");
                    alert.showAndWait();
                    return;
                }

                if (tripNum < 1 || tripNum > currentSearchResults.size()) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setContentText("Invalid trip number. Please enter a number between 1 and " +
                            currentSearchResults.size());
                    alert.showAndWait();
                    return;
                }

                // Select the trip and open booking dialog
                selectedTripForBooking = currentSearchResults.get(tripNum - 1);
                openBookingDialog();

            } catch (NumberFormatException ex) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setContentText("Please enter a valid number.");
                alert.showAndWait();
            }
        });

        bookingControls.getChildren().addAll(tripNumLabel, tripNumField, bookButton);

        resultsPanel.getChildren().addAll(resultsTitle, resultsArea, bookingControls);
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

            DayOfWeek startDay = startDayCombo.getValue();
            this.lastSelectedStartDay = startDay;
            // Call search with all parameters
            List<Trip> trips = system.searchConnections(
                    depCity.isEmpty() ? null : depCity,
                    arrCity.isEmpty() ? null : arrCity,
                    depTime.isEmpty() ? null : depTime,
                    arrTime.isEmpty() ? null : arrTime,
                    trainType.isEmpty() ? null : trainType,
                    days,
                    firstClass,
                    maxStopsCount,
                    startDay);

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
        this.currentSearchResults = trips;

        // Clear the results area
        resultsArea.clear();

        if (trips.isEmpty()) {
            resultsArea.setText("No trips found matching the criteria.\n");
        } else {
            StringBuilder sb = new StringBuilder();
            sb.append("═══════════════════════════════════════════════════════════════════════════════\n");
            sb.append(String.format("                    FOUND %d TRIP(S)\n", trips.size()));
            sb.append("═══════════════════════════════════════════════════════════════════════════════\n\n");
            sb.append("Enter trip number below to book.\n\n");

            int num = 1;
            for (Trip trip : trips) {
                sb.append("[TRIP #" + num + "]");
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

                DayOfWeek currentDepartureDay = (lastSelectedStartDay != null) ? lastSelectedStartDay
                        : DayOfWeek.MONDAY;

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

                    DayOfWeek currentArrivalDay = arrivalDayFor(conn, currentDepartureDay);

                    // Calculate and display transfer time between segments
                    if (i < trip.getSegments().size() - 1) {
                        Segment nextSeg = trip.getSegments().get(i + 1);
                        Connection nextConn = nextSeg.getConnection();

                        int transferTime = layoverMinutesAcrossDays(conn, nextConn, currentArrivalDay);
                        totalTransferTime += transferTime;
                        sb.append("     Transfer time (including days wait): " + formatDuration(transferTime) + "\n");

                        int arrMin = toMinutes(conn.getArrivalTime());
                        int depMin = toMinutes(nextConn.getDepartureTime());
                        int daysWaited = (transferTime + arrMin > depMin)
                                ? ((transferTime + arrMin - depMin + (24 * 60 - 1)) / (24 * 60))
                                : 0;
                        DayOfWeek nextDepartureDay = plusDays(currentArrivalDay, daysWaited);

                        currentDepartureDay = nextDepartureDay;
                    }

                    segNum++;
                }

                if (totalTransferTime > 0) {
                    sb.append("\nTotal transfer time: " + formatDuration(totalTransferTime) + "\n");
                }

                sb.append("\n───────────────────────────────────────────────────────────────────────────────\n\n");
                num++;
            }

            resultsArea.setText(sb.toString());
            resultsArea.positionCaret(0);
        }
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
        startDayCombo.setValue(DayOfWeek.MONDAY);
        maxStopsCombo.setValue("Max 2 Stops");
        sortCombo.setValue("Sort by Duration");
        firstClassCheck.setSelected(false);
        resultsArea.clear();
        statusLabel.setText("Ready. Enter search criteria.");
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

    // creates a set of days from the day range
    private Set<DayOfWeek> parseDaysOfOperation(String daysOpRaw) {
        Set<DayOfWeek> out = new HashSet<>();
        if (daysOpRaw == null || daysOpRaw.isBlank())
            return out;

        // Normalize common dash characters and lowercase everything
        String s = daysOpRaw.trim()
                .replace("–", "-")
                .replace("—", "-")
                .toLowerCase();

        if (s.equals("daily")) {
            out.addAll(Arrays.asList(DayOfWeek.values()));
            return out;
        }

        // Handle ranges like "mon-fri", "fri-sun", etc.
        String[] parts = s.split(",");
        for (String part : parts) {
            String token = part.trim();
            if (token.contains("-")) {
                String[] range = token.split("-");
                if (range.length == 2) {
                    DayOfWeek start = parseDayToken(range[0].trim());
                    DayOfWeek end = parseDayToken(range[1].trim());
                    if (start != null && end != null) {
                        addInclusiveRange(out, start, end);
                    }
                }
            } else {
                DayOfWeek d = parseDayToken(token);
                if (d != null)
                    out.add(d);
            }
        }

        return out;
    }

    private DayOfWeek parseDayToken(String tokenRaw) {
        if (tokenRaw == null)
            return null;
        switch (tokenRaw.substring(0, 3)) {
            case "mon":
                return DayOfWeek.MONDAY;
            case "tue":
                return DayOfWeek.TUESDAY;
            case "wed":
                return DayOfWeek.WEDNESDAY;
            case "thu":
                return DayOfWeek.THURSDAY;
            case "fri":
                return DayOfWeek.FRIDAY;
            case "sat":
                return DayOfWeek.SATURDAY;
            case "sun":
                return DayOfWeek.SUNDAY;
            default:
                return null;
        }
    }

    // adds days between two days inclusive
    private void addInclusiveRange(Set<DayOfWeek> out, DayOfWeek start, DayOfWeek end) {
        int i = start.getValue() - 1;
        int j = end.getValue() - 1;
        for (int k = 0; k < 7; k++) {
            int idx = (i + k) % 7;
            out.add(DayOfWeek.of(idx + 1));
            if (idx == j)
                break;
        }
    }

    private DayOfWeek plusDays(DayOfWeek d, int add) {
        int idx = (d.getValue() - 1 + add) % 7;
        if (idx < 0)
            idx += 7;
        return DayOfWeek.of(idx + 1);
    }

    private int toMinutes(java.time.LocalTime t) {
        return t.getHour() * 60 + t.getMinute();
    }

    private int layoverMinutesAcrossDays(Connection firstConn, Connection secondConn, DayOfWeek arrivalDay) {
        int arrMin = toMinutes(firstConn.getArrivalTime());
        java.util.Set<DayOfWeek> ops = parseDaysOfOperation(secondConn.getDaysOfOperation());
        int depMin = toMinutes(secondConn.getDepartureTime());

        for (int add = 0; add < 7; add++) {
            DayOfWeek candidate = plusDays(arrivalDay, add);
            if (!ops.contains(candidate))
                continue;

            if (add == 0) {
                if (depMin >= arrMin)
                    return depMin - arrMin;
                continue;
            } else {
                return add * 24 * 60 + (depMin - arrMin);
            }
        }

        return Integer.MAX_VALUE / 4;
    }

    private DayOfWeek arrivalDayFor(Connection conn, DayOfWeek departureDay) {
        int add = conn.isNextDay() ? 1 : 0;
        return plusDays(departureDay, add);
    }

    // Open booking and ask for number of passengers
    private void openBookingDialog() {
        Stage bookingStage = new Stage();
        bookingStage.initModality(Modality.APPLICATION_MODAL);
        bookingStage.setTitle("Book Trip");

        VBox mainLayout = new VBox(20);
        mainLayout.setPadding(new Insets(25));

        // Header
        Label headerTitle = new Label("Book Your Trip");
        headerTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        // Trip info
        Label tripInfo = new Label("Route: " + selectedTripForBooking.getDepartureCity() +
                " → " + selectedTripForBooking.getArrivalCity());
        tripInfo.setStyle("-fx-font-size: 14px;");

        Label depLabel = new Label("Departure: " + selectedTripForBooking.getDepartureTime());
        Label arrLabel = new Label("Arrival: " + selectedTripForBooking.getArrivalTime());
        Label durationLabel = new Label("Duration: " + selectedTripForBooking.getFormattedDuration());

        double pricePerPerson = firstClassCheck.isSelected() ? selectedTripForBooking.getTotalFirstClassPrice()
                : selectedTripForBooking.getTotalSecondClassPrice();
        String classType = firstClassCheck.isSelected() ? "First Class" : "Second Class";

        Label classLabel = new Label("Class: " + classType);
        Label priceLabel = new Label("Price per person: €" + String.format("%.2f", pricePerPerson));
        priceLabel.setStyle("-fx-font-weight: bold;");

        // Separator
        javafx.scene.control.Separator separator = new javafx.scene.control.Separator();

        // Number of travelers section
        Label travelerLabel = new Label("How many travelers?");
        travelerLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");

        // Single traveler button
        Button singleTravelerBtn = new Button("1 Traveler (Single Person)");
        singleTravelerBtn.setPrefWidth(250);
        singleTravelerBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; " +
                "-fx-font-weight: bold; -fx-font-size: 13px; -fx-padding: 15;");
        singleTravelerBtn.setOnAction(e -> {
            bookingStage.close();
            openSingleTravelerDialog(pricePerPerson);
        });

        // Multiple travelers button
        Button multipleTravelersBtn = new Button("Multiple Travelers (2-10 people)");
        multipleTravelersBtn.setPrefWidth(250);
        multipleTravelersBtn.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; " +
                "-fx-font-weight: bold; -fx-font-size: 13px; -fx-padding: 15;");
        multipleTravelersBtn.setOnAction(e -> {
            bookingStage.close();
            openMultipleTravelersDialog(pricePerPerson);
        });

        // Cancel button
        Button cancelBtn = new Button("Cancel");
        cancelBtn.setOnAction(e -> bookingStage.close());

        // Layout
        VBox tripDetailsBox = new VBox(5);
        tripDetailsBox.getChildren().addAll(tripInfo, depLabel, arrLabel, durationLabel, classLabel, priceLabel);

        VBox buttonBox = new VBox(10);
        buttonBox.getChildren().addAll(
                travelerLabel,
                singleTravelerBtn,
                multipleTravelersBtn);

        mainLayout.getChildren().addAll(
                headerTitle,
                tripDetailsBox,
                separator,
                buttonBox,
                cancelBtn);

        Scene scene = new Scene(mainLayout, 450, 500);
        bookingStage.setScene(scene);
        bookingStage.showAndWait();
    }

    private void openSingleTravelerDialog(double price) {
        Stage detailsStage = new Stage();
        detailsStage.initModality(Modality.APPLICATION_MODAL);
        detailsStage.setTitle("Single Traveler - Passenger Details");

        javafx.scene.layout.GridPane grid = new javafx.scene.layout.GridPane();
        grid.setPadding(new Insets(20));
        grid.setHgap(10);
        grid.setVgap(10);

        Label titleLabel = new Label("Single Traveler Booking");
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        grid.add(titleLabel, 0, 0, 2, 1);

        // First Name
        Label fnLabel = new Label("First Name:");
        grid.add(fnLabel, 0, 1);

        TextField firstNameField = new TextField();
        firstNameField.setPromptText("Enter first name");
        firstNameField.setPrefWidth(250);
        grid.add(firstNameField, 1, 1);

        // Last Name
        Label lnLabel = new Label("Last Name:");
        grid.add(lnLabel, 0, 2);

        TextField lastNameField = new TextField();
        lastNameField.setPromptText("Enter last name");
        lastNameField.setPrefWidth(250);
        grid.add(lastNameField, 1, 2);

        // Age
        Label ageLabel = new Label("Age:");
        grid.add(ageLabel, 0, 3);

        TextField ageField = new TextField();
        ageField.setPromptText("Enter age");
        ageField.setPrefWidth(250);
        grid.add(ageField, 1, 3);

        // ID Number
        Label idLabel = new Label("ID Number:");
        grid.add(idLabel, 0, 4);

        TextField idField = new TextField();
        idField.setPromptText("Enter ID number");
        idField.setPrefWidth(250);
        grid.add(idField, 1, 4);

        // Price
        Label priceLabel = new Label("Total: €" + String.format("%.2f", price));
        priceLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        grid.add(priceLabel, 0, 5, 2, 1);

        // Buttons
        Button confirmBtn = new Button("Confirm Booking");
        confirmBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold;");
        confirmBtn.setOnAction(e -> {
            String firstName = firstNameField.getText().trim();
            String lastName = lastNameField.getText().trim();
            String ageStr = ageField.getText().trim();
            String id = idField.getText().trim();

            if (firstName.isEmpty() || lastName.isEmpty() || ageStr.isEmpty() || id.isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setContentText("Please fill in all fields.");
                alert.showAndWait();
                return;
            }

            int age;
            try {
                age = Integer.parseInt(ageStr);
                if (age <= 0 || age > 150) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setContentText("Please enter a valid age (1-150).");
                    alert.showAndWait();
                    return;
                }
            } catch (NumberFormatException ex) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setContentText("Please enter a valid number for age.");
                alert.showAndWait();
                return;
            }

            Client client = new Client(firstName, lastName, age, id);
            List<Client> clients = new java.util.ArrayList<>();
            clients.add(client);

            BookedTrip result = processBooking(clients);
            if (result != null) {
                detailsStage.close();
            }
        });

        Button backBtn = new Button("Back");
        backBtn.setOnAction(e -> {
            detailsStage.close();
            openBookingDialog();
        });

        HBox buttonBox = new HBox(10);
        buttonBox.getChildren().addAll(confirmBtn, backBtn);
        grid.add(buttonBox, 0, 6, 2, 1);

        Scene scene = new Scene(grid, 450, 350);
        detailsStage.setScene(scene);
        detailsStage.show();

        // Request focus after showing
        firstNameField.requestFocus();
    }

    // For both single and multi
    private BookedTrip processBooking(List<Client> clients) {
        // Validate
        if (clients == null || clients.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("No client information provided.");
            alert.showAndWait();
            return null;
        }

        // Validate all clients
        for (Client client : clients) {
            if (!client.isValid()) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setContentText("Invalid information for: " + client.getFullName());
                alert.showAndWait();
                return null;
            }
        }

        // Get the first connection from the selected trip
        Connection connection = selectedTripForBooking.getSegments().get(0).getConnection();

        // Check if any client already has a reservation for this connection
        for (Client client : clients) {
            if (tripCollection.hasReservationForConnection(client.getId(), connection)) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setContentText(client.getFullName() + " already has a reservation for this connection.");
                alert.showAndWait();
                return null;
            }
        }

        // Create the booked trip
        BookedTrip bookedTrip = new BookedTrip(selectedTripForBooking, firstClassCheck.isSelected());

        // Add a reservation for each client
        for (Client client : clients) {
            Reservation reservation = new Reservation(client, connection, firstClassCheck.isSelected());
            bookedTrip.addReservation(reservation);

            // Generate ticket for this reservation
            Ticket ticket = new Ticket(reservation);
            System.out.println("Generated ticket #" + ticket.getTicketId() + " for " + client.getFullName());
        }

        // Save to collection
        tripCollection.saveTrip(bookedTrip);

        // Show confirmation
        StringBuilder confirmMsg = new StringBuilder();
        confirmMsg.append("Booking Successful!\n\n");
        confirmMsg.append("Trip ID: ").append(bookedTrip.getTripId()).append("\n");
        confirmMsg.append("Route: ").append(selectedTripForBooking.getDepartureCity())
                .append(" → ").append(selectedTripForBooking.getArrivalCity()).append("\n\n");
        confirmMsg.append("Passengers (").append(clients.size()).append("):\n");

        for (int i = 0; i < clients.size(); i++) {
            confirmMsg.append("  ").append(i + 1).append(". ")
                    .append(clients.get(i).getFullName()).append("\n");
        }

        confirmMsg.append("\nTotal Price: €").append(String.format("%.2f", bookedTrip.getTotalPrice()));

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Booking Confirmed");
        alert.setHeaderText("Trip Booked Successfully!");
        alert.setContentText(confirmMsg.toString());
        alert.showAndWait();

        return bookedTrip;
    }

    private void showAlert(String message){
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void openMultipleTravelersDialog(double pricePerPerson) {
        Stage multiStage = new Stage();
        multiStage.initModality(Modality.APPLICATION_MODAL);
        multiStage.setTitle("Multiple Travelers Booking");

        VBox mainLayout = new VBox(20);
        mainLayout.setPadding(new Insets(30));

        Label titleLabel = new Label("Multiple Travelers Booking");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        Label infoLabel = new Label("Enter infromation for each traveler (2-10 people): ");
        infoLabel.setStyle("-fx-font-size: 14px;");

        HBox countBox = new HBox(10);
        Label countLabel = new Label("Number of Travelers: ");
        TextField countField = new TextField();
        countField.setPromptText("e.g. 4");
        countField.setPrefWidth(60);
        Button generateBtn = new Button("Generate Fields");
        countBox.getChildren().addAll(countLabel, countField, generateBtn);

        VBox travelersBox = new VBox(15);
        ScrollPane scrollPane = new ScrollPane(travelersBox);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(250);

        HBox bottomButtons = new HBox(10);
        Button confirmBtn = new Button("Confirm Booking");
        confirmBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold");
        Button cancelBtn = new Button("Cancel");
        bottomButtons.getChildren().addAll(confirmBtn, cancelBtn);

        generateBtn.setOnAction(e -> {
            travelersBox.getChildren().clear();
            String countStr = countField.getText().trim();
            int count;
            try{
                count = Integer.parseInt(countStr);
                if(count < 2 || count > 10){
                    showAlert("Please enter a number between 2 and 10 travellers.");
                    return;
                }
            } catch (NumberFormatException ex){
                showAlert("Invalid number of travelers.");
                return;
            }

            for(int i = 1; i <= count; i++){
                VBox travelerForm = new VBox(5);
                travelerForm.setStyle("-fx-border-color:#ccc; -fx-border-width: 1; -fx-padding:10;");
                Label travelerTitle = new Label("Traveler " + i);
                travelerTitle.setStyle("-fx-font-weight: bold;");

                TextField fnField = new TextField();
                fnField.setPromptText("First Name");

                TextField lnField = new TextField();
                lnField.setPromptText("Last Name");

                TextField ageField = new TextField();
                ageField.setPromptText("Age");

                TextField idField = new TextField();
                idField.setPromptText("ID Number");

                travelerForm.getChildren().addAll(travelerTitle, fnField, lnField, ageField, idField);
                travelersBox.getChildren().add(travelerForm);
            }
        });

        confirmBtn.setOnAction(e -> {
            List<Client> clients = new java.util.ArrayList<>();

            for(javafx.scene.Node node : travelersBox.getChildren()){
                if(node instanceof VBox travelerForm){
                    List<TextField> fields = travelerForm.getChildren()
                    .stream()
                    .filter(n -> n instanceof TextField)
                    .map(n -> (TextField) n)
                    .toList();

                    if(fields.size() != 4) continue;
                    String fn = fields.get(0).getText().trim();
                    String ln = fields.get(1).getText().trim();
                    String ageStr = fields.get(2).getText().trim();
                    String id = fields.get(3).getText().trim();

                    if(fn.isEmpty() || ln.isEmpty() || ageStr.isEmpty() || id.isEmpty()){
                        showAlert("Please fill in all fields for all travelers.");
                        return;
                    }

                    int age;
                    try{
                        age = Integer.parseInt(ageStr);
                        if(age <= 0 || age > 150) {
                            showAlert("Please enter a valid age (1-150).");
                            return;
                        }
                    } catch(NumberFormatException ex){
                        showAlert("Invalid age for " + fn +" " + ln);
                        return;
                    }

                    clients.add(new Client(fn, ln, age, id));
                }
            }

            if(clients.isEmpty()){
                showAlert("No traveler data entered");
                return;
            }

            BookedTrip result = processBooking(clients);
            if(result != null) {
                multiStage.close();
            }
        });

        cancelBtn.setOnAction(e -> {
            multiStage.close();
            openBookingDialog();
        });

        Label priceLabel = new Label("Price per traveler: €" + String.format("%.2f", pricePerPerson));
        priceLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");

        mainLayout.getChildren().addAll(titleLabel, infoLabel, countBox, scrollPane, priceLabel, bottomButtons);
        Scene scene = new Scene(mainLayout, 500, 600);
        multiStage.setScene(scene);
        multiStage.initModality(Modality.APPLICATION_MODAL);
        multiStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}