import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

public class RailwaySystem {
    private Connections connections;
    private TripCollection tripCollection;
    private DatabaseManager dbManager;

    public RailwaySystem(TripCollection tripCollection) {
        this.connections = new Connections();
        this.tripCollection = tripCollection;
        this.dbManager = new DatabaseManager();

        connections.setDatabaseManager(dbManager);
        tripCollection.setDatabaseManager(dbManager, connections);

        System.out.println("System initialized with database");
    }

    public int loadConnectionData(String filepath) {
        // Check if database already has connections
        try {
            int dbCount = dbManager.getConnectionCount();
            if (dbCount > 0) {
                System.out.println("Database already contains " + dbCount + " connections.");
                System.out.println("Clear the database first if you want to reload from CSV.");
                return dbCount;
            }
        } catch (Exception e) {
            System.err.println("Error checking database: " + e.getMessage());
        }

        connections.clear();

        CSVReader reader = new CSVReader(filepath);
        reader.readData();

        ArrayList<ArrayList<String>> data = reader.getData(); // One line of the CSV file (many columns)
        int loadedCount = 0;

        int startRow = 0;

        if (data.size() > 0) {
            ArrayList<String> firstRow = data.get(0);

            // Skip the header is it's the first row
            if (firstRow.size() > 0 && firstRow.get(0).toLowerCase().contains("route")) {
                startRow = 1;
            }
        }

        for (int i = startRow; i < data.size(); i++) {
            ArrayList<String> row = data.get(i);

            if (row.size() < 9) {
                continue;
            }

            try {
                String routeID = row.get(0).trim();
                String depCityName = row.get(1).trim();
                String arrCityName = row.get(2).trim();
                String depTimeStr = row.get(3).trim();
                String arrTimeStr = row.get(4).trim();
                String trainType = row.get(5).trim();
                String daysOfOp = row.get(6).trim();
                String firstClassStr = row.get(7).trim();
                String secondClassStr = row.get(8).trim();

                if (routeID.isEmpty() || depCityName.isEmpty() || arrCityName.isEmpty()) {
                    continue;
                }

                // Check if arrival time has (+1d)
                boolean isNextDay = arrTimeStr.contains("(+1d)");

                // Remove (+1d) before parsing the times
                String cleanDepTime = depTimeStr.replaceAll("\\s*\\(.*?\\)", "").trim();
                String cleanArrTime = arrTimeStr.replaceAll("\\s*\\(.*?\\)", "").trim();

                LocalTime depTime = LocalTime.parse(cleanDepTime);
                LocalTime arrTime = LocalTime.parse(cleanArrTime);

                // Parse prices as doubles
                double firstClassPrice = Double.parseDouble(firstClassStr.trim());
                double secondClassPrice = Double.parseDouble(secondClassStr.trim());

                City depCity = connections.findOrCreateCity(depCityName);
                City arrCity = connections.findOrCreateCity(arrCityName);
                Train train = connections.findOrCreateTrain(trainType);

                // Create new connection object (for each valid row)
                Connection connection = new Connection(routeID, depCity, arrCity, depTime, arrTime,
                        train, daysOfOp, firstClassPrice, secondClassPrice, isNextDay);

                connections.add(connection);
                loadedCount++;

            }

            catch (Exception e) {
                System.err.println("Error on row " + i + ": " + e.getMessage());
            }
        }

        System.out.println("Loaded " + loadedCount + " connections"); // Verify that all connections were loaded
        return loadedCount;
    }

    public List<Trip> searchConnections(String depCity, String arrCity, String depTime, String arrTime,
            String trainType, String daysOp, boolean firstClass, int maxStops, java.time.DayOfWeek startDay) {
        List<Trip> allTrips = new ArrayList<>();

        // Parse the time into minutes
        Integer depTimeMinutes = parseTime(depTime);
        Integer arrTimeMinutes = parseTime(arrTime);

        // Find direct connections with ALL filters
        List<Connection> directConnections = connections.findMatching(depCity, arrCity,
                depTimeMinutes, arrTimeMinutes, trainType, daysOp);

        if (startDay != null) {
            directConnections.removeIf(conn -> !parseDays(conn.getDaysOfOperation()).contains(startDay));
        }
        // Create a trip for each direct connection
        for (Connection conn : directConnections) {
            Trip trip = new Trip();
            trip.addConnection(conn);
            trip.computeTotals(firstClass, 0); // No transfer time because direct
            allTrips.add(trip);
        }

        // Make sure that both the arrival and departure cities are filled in because
        // otherwise there will be too many combinations
        if (depCity != null && !depCity.trim().isEmpty() && arrCity != null && !arrCity.trim().isEmpty()) {
            if (maxStops >= 1) {
                List<Trip> oneStopTrips = findOneStopTrips(depCity, arrCity, depTimeMinutes, arrTimeMinutes,
                        trainType, daysOp, firstClass, startDay);
                allTrips.addAll(oneStopTrips);
            }

            if (maxStops >= 2) {
                List<Trip> twoStopTrips = findTwoStopTrips(depCity, arrCity, depTimeMinutes, arrTimeMinutes,
                        trainType, daysOp, firstClass, directConnections, startDay);
                allTrips.addAll(twoStopTrips);
            }
        }

        return allTrips;
    }

    private List<Trip> findOneStopTrips(String depCity, String arrCity, Integer depTime, Integer arrTime,
            String trainType, String daysOp, boolean firstClass, java.time.DayOfWeek startDay) {
        List<Trip> trips = new ArrayList<>();

        // Find all first legs departing from origin
        List<Connection> firstSegments = connections.findMatching(depCity, null, depTime, null, trainType, daysOp);

        // Define restrictions on layover times
        short minLayoverMinutes = 30; // At least 30 mins to allow for passengers to switch trains
        short maxDayLayoverMinutes = 540; // At most 9 hours during the day

        if (startDay != null) {
            firstSegments.removeIf(conn -> !parseDays(conn.getDaysOfOperation()).contains(startDay));
        }
        for (Connection firstSegment : firstSegments) {
            List<Connection> secondSegments = connections.findMatching(firstSegment.getArrivalCity().getName(), arrCity,
                    null, arrTime, trainType, daysOp);

            for (Connection secondSegment : secondSegments) {
                int transferTime = calculateTransferTime(firstSegment, secondSegment);

                // Handle layover time limits
                if (transferTime < minLayoverMinutes || transferTime > maxDayLayoverMinutes) {
                    continue;
                }

                Trip trip = new Trip();
                trip.addConnection(firstSegment);
                trip.addConnection(secondSegment);

                trip.computeTotals(firstClass, transferTime);
                trips.add(trip);
            }
        }
        return trips;
    }

    private List<Trip> findTwoStopTrips(String depCity, String arrCity, Integer depTime, Integer arrTime,
            String trainType, String daysOp, boolean firstClass, List<Connection> directConnections,
            java.time.DayOfWeek startDay) {
        List<Trip> trips = new ArrayList<>();

        // Find all first legs departing from origin
        List<Connection> firstSegments = connections.findMatching(depCity, null, depTime, null, trainType, daysOp);

        // Define restrictions on layover times
        short minLayoverMinutes = 30; // At least 30 mins to allow for passengers to switch trains
        short maxDayLayoverMinutes = 540; // At most 9 hours during the day

        if (startDay != null) {
            firstSegments.removeIf(conn -> !parseDays(conn.getDaysOfOperation()).contains(startDay));
        }
        for (Connection firstSegment : firstSegments) {

            if (isDirectConnection(firstSegment, directConnections)) {
                continue;
            }
            List<Connection> secondSegments = connections.findMatching(firstSegment.getArrivalCity().getName(), null,
                    null, null, trainType, daysOp);

            for (Connection secondSegment : secondSegments) {

                if (isDirectConnection(secondSegment, directConnections)) {
                    continue;
                }
                List<Connection> thirdSegments = connections.findMatching(secondSegment.getArrivalCity().getName(),
                        arrCity, null, arrTime, trainType, daysOp);
                for (Connection thirdSegment : thirdSegments) {
                    if (isDirectConnection(thirdSegment, directConnections)) {
                        continue;
                    }
                    int transferTime1 = calculateTransferTime(firstSegment, secondSegment);
                    int transferTime2 = calculateTransferTime(secondSegment, thirdSegment);

                    // Handle layover time limits
                    if (transferTime1 < minLayoverMinutes || transferTime1 > maxDayLayoverMinutes) {
                        continue; // Skip adding this segment
                    } else if (transferTime2 < minLayoverMinutes || transferTime2 > maxDayLayoverMinutes) {
                        continue;
                    }

                    Trip trip = new Trip();
                    trip.addConnection(firstSegment);
                    trip.addConnection(secondSegment);
                    trip.addConnection(thirdSegment);

                    trip.computeTotals(firstClass, transferTime1 + transferTime2);
                    trips.add(trip);
                }
            }
        }
        return trips;
    }

    // Parse the time intominutes for easy comparison and operations
    private Integer parseTime(String timeStr) {
        if (timeStr == null || timeStr.trim().isEmpty()) {
            return null;
        }

        try {
            LocalTime time = LocalTime.parse(timeStr.trim());
            return time.getHour() * 60 + time.getMinute();
        }

        catch (Exception e) {
            return null;
        }
    }

    public int calculateTransferTime(Connection firstSegment, Connection secondSegment) {
        int arrivalMinutes = firstSegment.getArrivalTime().getHour() * 60 + firstSegment.getArrivalTime().getMinute();
        int departureMinutes = secondSegment.getDepartureTime().getHour() * 60
                + secondSegment.getDepartureTime().getMinute();

        if (departureMinutes < arrivalMinutes) {
            departureMinutes += 24 * 60;
        }
        return departureMinutes - arrivalMinutes;
    }

    public int getConnectionCount() {
        return connections.getCount();
    }

    private boolean isDirectConnection(Connection conn, List<Connection> directConnections) {
        for (Connection directConn : directConnections) {
            if (conn.getDepartureCity().equals(directConn.getDepartureCity()) &&
                    conn.getArrivalCity().equals(directConn.getArrivalCity()) &&
                    conn.getDepartureTime().equals(directConn.getDepartureTime()) &&
                    conn.getArrivalTime().equals(directConn.getArrivalTime())) {
                return true;
            }
        }
        return false;
    }

    public Map<String, List<BookedTrip>> viewTrips(String lastName, String id) {
        List<BookedTrip> matchingTrips = tripCollection.findTripsByCredentials(lastName, id);

        List<BookedTrip> currentTrips = new ArrayList<>();
        List<BookedTrip> pastTrips = new ArrayList<>();

        for (BookedTrip trip : matchingTrips) {
            if (trip.isFuture()) {
                currentTrips.add(trip);
            } else {
                pastTrips.add(trip);
            }
        }

        Map<String, List<BookedTrip>> categorized = new HashMap<>();
        categorized.put("currentTrips", currentTrips);
        categorized.put("pastTrips", pastTrips);
        return categorized;
    }

    // parses selected days of the week
    // turns day range to list of days
    private java.util.Set<java.time.DayOfWeek> parseDays(String raw) {
        java.util.Set<java.time.DayOfWeek> out = new java.util.HashSet<>();
        if (raw == null || raw.isBlank())
            return out;
        String s = raw.trim().toLowerCase()
                .replace("–", "-").replace("—", "-"); // normalize en/em dash to hyphen

        if (s.equals("daily")) {
            for (var d : java.time.DayOfWeek.values())
                out.add(d);
            return out;
        }

        for (String part : s.split(",")) {
            String token = part.trim();
            if (token.contains("-")) {
                String[] ab = token.split("-");
                if (ab.length == 2) {
                    var a = parseDay(ab[0]);
                    var b = parseDay(ab[1]);
                    if (a != null && b != null)
                        addRange(out, a, b);
                }
            } else {
                var d = parseDay(token);
                if (d != null)
                    out.add(d);
            }
        }
        return out;
    }

    private java.time.DayOfWeek parseDay(String t) {
        if (t == null)
            return null;
        t = t.trim().toLowerCase();
        if (t.length() >= 3)
            t = t.substring(0, 3);
        switch (t) {
            case "mon":
                return java.time.DayOfWeek.MONDAY;
            case "tue":
                return java.time.DayOfWeek.TUESDAY;
            case "wed":
                return java.time.DayOfWeek.WEDNESDAY;
            case "thu":
                return java.time.DayOfWeek.THURSDAY;
            case "fri":
                return java.time.DayOfWeek.FRIDAY;
            case "sat":
                return java.time.DayOfWeek.SATURDAY;
            case "sun":
                return java.time.DayOfWeek.SUNDAY;
            default:
                return null;
        }
    }

    private void addRange(java.util.Set<java.time.DayOfWeek> out, java.time.DayOfWeek a, java.time.DayOfWeek b) {
        int i = a.getValue() - 1, j = b.getValue() - 1;
        for (int k = 0; k < 7; k++) {
            int idx = (i + k) % 7;
            out.add(java.time.DayOfWeek.of(idx + 1));
            if (idx == j)
                break;
        }
    }

    // Force reload from CSV, clearing existing database data
    public int forceLoadConnectionData(String filepath) {
        System.out.println("Force loading from CSV - clearing existing data...");
        connections.clear();

        CSVReader reader = new CSVReader(filepath);
        reader.readData();

        ArrayList<ArrayList<String>> data = reader.getData();
        int loadedCount = 0;

        int startRow = 0;

        if (data.size() > 0) {
            ArrayList<String> firstRow = data.get(0);
            if (firstRow.size() > 0 && firstRow.get(0).toLowerCase().contains("route")) {
                startRow = 1;
            }
        }

        for (int i = startRow; i < data.size(); i++) {
            ArrayList<String> row = data.get(i);

            if (row.size() < 9) {
                continue;
            }

            try {
                String routeID = row.get(0).trim();
                String depCityName = row.get(1).trim();
                String arrCityName = row.get(2).trim();
                String depTimeStr = row.get(3).trim();
                String arrTimeStr = row.get(4).trim();
                String trainType = row.get(5).trim();
                String daysOfOp = row.get(6).trim();
                String firstClassStr = row.get(7).trim();
                String secondClassStr = row.get(8).trim();

                if (routeID.isEmpty() || depCityName.isEmpty() || arrCityName.isEmpty()) {
                    continue;
                }

                boolean isNextDay = arrTimeStr.contains("(+1d)");
                String cleanDepTime = depTimeStr.replaceAll("\\s*\\(.*?\\)", "").trim();
                String cleanArrTime = arrTimeStr.replaceAll("\\s*\\(.*?\\)", "").trim();

                LocalTime depTime = LocalTime.parse(cleanDepTime);
                LocalTime arrTime = LocalTime.parse(cleanArrTime);

                double firstClassPrice = Double.parseDouble(firstClassStr.trim());
                double secondClassPrice = Double.parseDouble(secondClassStr.trim());

                City depCity = connections.findOrCreateCity(depCityName);
                City arrCity = connections.findOrCreateCity(arrCityName);
                Train train = connections.findOrCreateTrain(trainType);

                Connection connection = new Connection(routeID, depCity, arrCity, depTime, arrTime,
                        train, daysOfOp, firstClassPrice, secondClassPrice, isNextDay);

                connections.add(connection);
                loadedCount++;

            } catch (Exception e) {
                System.err.println("Error on row " + i + ": " + e.getMessage());
            }
        }

        System.out.println("Loaded " + loadedCount + " connections from CSV to database");
        return loadedCount;
    }

    // Clean up database connection when done
    public void cleanup() {
        if (dbManager != null) {
            dbManager.close();
        }
    }
}