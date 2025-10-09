import java.util.ArrayList;
import java.util.List;

public class Connections {
    private List<Connection> connections;
    private List<City> cities;
    private List<Train> trains;

    public Connections() {
        this.connections = new ArrayList<>();
        this.cities = new ArrayList<>();
        this.trains = new ArrayList<>();
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
            return true; // No filter applied
        }

        String connDays = connectionDays.trim();
        String filter = filterDays.trim();

        // "Daily" connections match any filter
        if (connDays.equalsIgnoreCase("Daily")) {
            return true;
        }

        // Exact match
        if (connDays.equalsIgnoreCase(filter)) {
            return true;
        }

        // "Mon-Fri" connections match individual weekday filters
        if (connDays.equalsIgnoreCase("Mon-Fri")) {
            if (filter.equalsIgnoreCase("Mon") || filter.equalsIgnoreCase("Tue") ||
                    filter.equalsIgnoreCase("Wed") || filter.equalsIgnoreCase("Thu") ||
                    filter.equalsIgnoreCase("Fri")) {
                return true;
            }
        }

        return false; // No match
    }

    public void clear() {
        connections.clear();
        cities.clear();
        trains.clear();
    }
}