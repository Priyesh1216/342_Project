import java.util.ArrayList;
import java.util.List;

public class Connections {
    private List<Connection> connections;
    private List<City> cities;
    private List<Train> trains;
    private DatabaseManager dbManager;

    public Connections() {
        this.connections = new ArrayList<>();
        this.cities = new ArrayList<>();
        this.trains = new ArrayList<>();
        this.dbManager = null;
    }

    public void setDatabaseManager(DatabaseManager dbManager) {
        this.dbManager = dbManager;
        loadFromDatabase();
    }

    private void loadFromDatabase() {
        if (dbManager == null)
            return;

        try {
            int dbCount = dbManager.getConnectionCount();
            if (dbCount > 0) {
                System.out.println("Loading connections from database...");
                List<Connection> loaded = dbManager.loadAllConnections(this);
                connections.addAll(loaded);
                System.out.println("Loaded " + loaded.size() + " connections from database.");
            }
        } catch (Exception e) {
            System.err.println("Error loading from database: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Create city object if it does not already exist
    public City findOrCreateCity(String name) {
        String lowerName = name.trim().toLowerCase();

        for (City city : cities) {
            if (city.getName().toLowerCase().equals(lowerName)) {
                return city;
            }
        }

        City newCity = new City(name.trim());
        cities.add(newCity);
        return newCity;
    }

    // Create train object if it does not already exist
    public Train findOrCreateTrain(String type) {
        String lowerType = type.trim().toLowerCase();

        for (Train train : trains) {
            if (train.getType().toLowerCase().equals(lowerType)) {
                return train;
            }
        }

        Train newTrain = new Train(type.trim());
        trains.add(newTrain);
        return newTrain;
    }

    public void add(Connection connection) {
        connections.add(connection);

        // Save to database if available
        if (dbManager != null) {
            try {
                int depCityId = dbManager.saveCityIfNotExists(connection.getDepartureCity().getName());
                int arrCityId = dbManager.saveCityIfNotExists(connection.getArrivalCity().getName());
                int trainId = dbManager.saveTrainIfNotExists(connection.getTrain().getType());
                dbManager.saveConnection(connection, depCityId, arrCityId, trainId);
            } catch (Exception e) {
                System.err.println("Error saving connection to database: " + e.getMessage());
            }
        }
    }

    public List<Connection> getAll() {
        return new ArrayList<>(connections);
    }

    public int getCount() {
        return connections.size();
    }

    public List<Connection> findMatching(String depCity, String arrCity,
            Integer depTime, Integer arrTime, String trainType, String daysOp) {
        List<Connection> matches = new ArrayList<>();

        // Check each connection against all filters
        for (Connection conn : connections) {
            boolean match = true;

            // Filter by departure city (case-insensitive)
            if (depCity != null && !depCity.trim().isEmpty()) {
                if (!conn.getDepartureCity().getName().equalsIgnoreCase(depCity.trim())) {
                    match = false;
                }
            }

            // Filter by arrival city (case-insensitive)
            if (arrCity != null && !arrCity.trim().isEmpty()) {
                if (!conn.getArrivalCity().getName().equalsIgnoreCase(arrCity.trim())) {
                    match = false;
                }
            }

            // Filter by minimum departure time (>= filter)
            if (depTime != null) {
                int connDepMinutes = conn.getDepartureTime().getHour() * 60 + conn.getDepartureTime().getMinute();
                if (connDepMinutes < depTime) {
                    match = false;
                }
            }

            // Filter by maximum arrival time (<= filter)
            if (arrTime != null) {
                int connArrMinutes = conn.getArrivalTime().getHour() * 60 + conn.getArrivalTime().getMinute();
                if (connArrMinutes > arrTime) {
                    match = false;
                }
            }

            // Filter by train type
            if (trainType != null && !trainType.trim().isEmpty()) {
                if (!conn.getTrain().getType().equalsIgnoreCase(trainType.trim())) {
                    match = false;
                }
            }

            // Filter by days of operation
            if (daysOp != null && !daysOp.trim().isEmpty()) {
                if (!daysMatch(conn.getDaysOfOperation(), daysOp)) {
                    match = false;
                }
            }

            // If all filters passed, add to results
            if (match) {
                matches.add(conn);
            }
        }

        return matches;
    }

    private boolean daysMatch(String connectionDays, String filterDays) {
        if (connectionDays == null || filterDays == null) {
            return true;
        }

        String connDays = connectionDays.trim();
        String filter = filterDays.trim();

        // "Daily" connections match any filter
        if (connDays.equalsIgnoreCase("Daily")) {
            return true;
        }

        // If filter is "Daily", only match Daily connections
        if (filter.equalsIgnoreCase("Daily")) {
            return connDays.equalsIgnoreCase("Daily");
        }

        // Expand any day ranges in connection days (e.g., "Fri-Sun" →
        // "Friday,Saturday,Sunday")
        String expandedConnDays = expandDayRanges(connDays);

        // Handle "Monday-Friday" filter from GUI
        if (filter.equalsIgnoreCase("Monday-Friday")) {
            // Match if connection is Daily, Mon-Fri, or any weekday
            if (connDays.equalsIgnoreCase("Daily") || connDays.equalsIgnoreCase("Mon-Fri")) {
                return true;
            }
            // Check if expanded connection days contain any weekday
            String[] expandedDays = expandedConnDays.split(",");
            for (String day : expandedDays) {
                String normalized = normalizeDayName(day.trim());
                if (normalized.equals("Monday") || normalized.equals("Tuesday") ||
                        normalized.equals("Wednesday") || normalized.equals("Thursday") ||
                        normalized.equals("Friday")) {
                    return true;
                }
            }
        }

        // Exact match
        if (connDays.equalsIgnoreCase(filter)) {
            return true;
        }

        // Match "Mon-Fri" connection with individual weekdays or Mon-Fri filter
        if (connDays.equalsIgnoreCase("Mon-Fri")) {
            if (filter.equalsIgnoreCase("Mon-Fri") || filter.equalsIgnoreCase("Monday-Friday")) {
                return true;
            }
            // Check against short and long day names
            if (filter.equalsIgnoreCase("Mon") || filter.equalsIgnoreCase("Monday") ||
                    filter.equalsIgnoreCase("Tue") || filter.equalsIgnoreCase("Tuesday") ||
                    filter.equalsIgnoreCase("Wed") || filter.equalsIgnoreCase("Wednesday") ||
                    filter.equalsIgnoreCase("Thu") || filter.equalsIgnoreCase("Thursday") ||
                    filter.equalsIgnoreCase("Fri") || filter.equalsIgnoreCase("Friday")) {
                return true;
            }
        }

        // Check if filter has multiple days: comma-separated values taken from the GUI
        // checkboxes)
        if (filter.contains(",")) {
            String[] filterDaysList = filter.split(",");
            String[] expandedDaysList = expandedConnDays.split(",");

            for (String filterDay : filterDaysList) {
                for (String connDay : expandedDaysList) {
                    if (dayNamesMatch(connDay.trim(), filterDay.trim())) {
                        return true; // Found overlap = matching
                    }
                }
            }
        }

        else {
            // Single day filter - check against expanded connection days
            String[] expandedDaysList = expandedConnDays.split(",");
            for (String connDay : expandedDaysList) {
                if (dayNamesMatch(connDay.trim(), filter)) {
                    return true;
                }
            }
        }

        return false; // No match
    }

    // Expand day ranges like "Fri-Sun" to Friday, Saturday, Sunday for example
    private String expandDayRanges(String days) {
        String trimmed = days.trim();

        // Handle Mon-Fri - will expand to all weekdays
        if (trimmed.equalsIgnoreCase("Mon-Fri") || trimmed.equalsIgnoreCase("Monday-Friday")) {
            return "Monday,Tuesday,Wednesday,Thursday,Friday";
        }

        // If theres no hypen there is no range
        if (!trimmed.contains("-")) {
            return trimmed;
        }

        // Split on hyphen and trim each part should only give 2 parts
        String[] parts = trimmed.split("-");
        if (parts.length != 2) {
            return trimmed; // Not valid
        }

        // Convert to full day names (so like Fri to Friday)
        String startDay = normalizeDayName(parts[0].trim());
        String endDay = normalizeDayName(parts[1].trim());

        String[] weekDays = { "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday" };
        int startIdx = -1;
        int endIdx = -1;

        for (int i = 0; i < weekDays.length; i++) {
            if (weekDays[i].equalsIgnoreCase(startDay))
                startIdx = i;
            if (weekDays[i].equalsIgnoreCase(endDay))
                endIdx = i;
        }

        StringBuilder result = new StringBuilder();
        if (startIdx <= endIdx) {
            // Normal range the start will come beofre the end in the week (Fri-Sun is
            // Fri=4, Sun=6, so 4 <= 6)
            for (int i = startIdx; i <= endIdx; i++) {
                if (result.length() > 0)
                    result.append(",");
                result.append(weekDays[i]);
            }
        }

        else {
            // Wrap-around for day ranges like Friday-Monday (5 <= 0)

            // Add days from start to the end of the week
            for (int i = startIdx; i < weekDays.length; i++) {
                if (result.length() > 0)
                    result.append(",");
                result.append(weekDays[i]);
            }

            // After that, add the days from the start of the week to the end day
            for (int i = 0; i <= endIdx; i++) {
                if (result.length() > 0)
                    result.append(",");
                result.append(weekDays[i]);
            }
        }

        return result.toString();
    }

    // Match day names
    private boolean dayNamesMatch(String day1, String day2) {
        if (day1.equalsIgnoreCase(day2)) {
            return true;
        }

        // Normalize to full day names to compare
        String normalized1 = normalizeDayName(day1);
        String normalized2 = normalizeDayName(day2);

        return normalized1.equalsIgnoreCase(normalized2);
    }

    // Normalize day names to full names
    private String normalizeDayName(String day) {
        String d = day.trim().toLowerCase();

        switch (d) {
            case "mon":
                return "Monday";
            case "monday":
                return "Monday";
            case "tue":
                return "Tuesday";
            case "tuesday":
                return "Tuesday";
            case "wed":
                return "Wednesday";
            case "wednesday":
                return "Wednesday";
            case "thu":
                return "Thursday";
            case "thursday":
                return "Thursday";
            case "fri":
                return "Friday";
            case "friday":
                return "Friday";
            case "sat":
                return "Saturday";
            case "saturday":
                return "Saturday";
            case "sun":
                return "Sunday";
            case "sunday":
                return "Sunday";
            default:
                return day;
        }
    }

    public void clear() {
        connections.clear();
        cities.clear();
        trains.clear();

        // Clear from database if available
        if (dbManager != null) {
            try {
                dbManager.clearConnections();
                System.out.println("Cleared connections from database.");
            } catch (Exception e) {
                System.err.println("Error clearing database: " + e.getMessage());
            }
        }
    }
}