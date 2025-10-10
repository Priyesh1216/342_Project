import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class RailwaySystem {
    private Connections connections;

    public RailwaySystem() {
        this.connections = new Connections();
    }

    public int loadConnectionData(String filepath) {
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
            String trainType, String daysOp, boolean firstClass, int maxStops) {
        List<Trip> allTrips = new ArrayList<>();

        // Parse the time into minutes
        Integer depTimeMinutes = parseTime(depTime);
        Integer arrTimeMinutes = parseTime(arrTime);

        // Find direct connections with ALL filters
        List<Connection> directConnections = connections.findMatching(depCity, arrCity,
                depTimeMinutes, arrTimeMinutes, trainType, daysOp);

        // Create a trip for each direct connection
        for (Connection conn : directConnections) {
            Trip trip = new Trip();
            trip.addSegment(new Segment(conn));
            trip.computeTotals(firstClass, 0); // No transfer time because direct
            allTrips.add(trip);
        }

        // Make sure that both the arrival and departure cities are filled in because
        // otherwise there will be too many combinations
        if (depCity != null && !depCity.trim().isEmpty() && arrCity != null && !arrCity.trim().isEmpty()) {
            if (maxStops >= 1) {
            List<Trip> oneStopTrips = findOneStopTrips(depCity, arrCity, depTimeMinutes, arrTimeMinutes,
                    trainType, daysOp, firstClass);
            allTrips.addAll(oneStopTrips);
        }

        if (maxStops >= 2) {
            List<Trip> twoStopTrips = findTwoStopTrips(depCity, arrCity, depTimeMinutes, arrTimeMinutes,
                    trainType, daysOp, firstClass, directConnections);
            allTrips.addAll(twoStopTrips);
        }
    }

        return allTrips;
    }

    private List<Trip> findOneStopTrips(String depCity, String arrCity, Integer depTime, Integer arrTime,
            String trainType, String daysOp, boolean firstClass) {
        List<Trip> trips = new ArrayList<>();

        // Find all first legs departing from origin
        List<Connection> firstSegments = connections.findMatching(depCity, null, depTime, null, trainType, daysOp);
            
        for(Connection firstSegment : firstSegments){
            List<Connection> secondSegments = connections.findMatching(firstSegment.getArrivalCity().getName(), arrCity, null, arrTime, trainType, daysOp);

            for (Connection secondSegment : secondSegments){
                int transferTime = calculateTransferTime(firstSegment, secondSegment);
                Trip trip = new Trip();
                trip.addSegment(new Segment(firstSegment));
                trip.addSegment(new Segment(secondSegment));

                
                trip.computeTotals(firstClass, transferTime);
                trips.add(trip);
            }
        }
        return trips;
    }

    private List<Trip> findTwoStopTrips(String depCity, String arrCity, Integer depTime, Integer arrTime,
            String trainType, String daysOp, boolean firstClass, List<Connection> directConnections) {
        List<Trip> trips = new ArrayList<>();

        // Find all first legs departing from origin
        List<Connection> firstSegments = connections.findMatching(depCity, null, depTime, null, trainType, daysOp);
        for(Connection firstSegment : firstSegments){

            if(isDirectConnection(firstSegment, directConnections)){
                continue;
            }
            List<Connection> secondSegments = connections.findMatching(firstSegment.getArrivalCity().getName(), null, null, null, trainType, daysOp);

            for (Connection secondSegment : secondSegments){

                if(isDirectConnection(secondSegment, directConnections)){
                    continue;
                }
                List<Connection> thirdSegments = connections.findMatching(secondSegment.getArrivalCity().getName(), arrCity, null, arrTime, trainType, daysOp);
                for(Connection thirdSegment:thirdSegments){
                    if(isDirectConnection(thirdSegment, directConnections)){
                        continue;
                    }
                    int transferTime1 = calculateTransferTime(firstSegment, secondSegment);
                    int transferTime2 = calculateTransferTime(secondSegment, thirdSegment);

                    Trip trip = new Trip();
                    trip.addSegment(new Segment(firstSegment));
                    trip.addSegment(new Segment(secondSegment));
                    trip.addSegment(new Segment(thirdSegment));

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

    private int calculateTransferTime(Connection firstSegment, Connection secondSegment){
        int arrivalMinutes = firstSegment.getArrivalTime().getHour() * 60 + firstSegment.getArrivalTime().getMinute();
        int departureMinutes = secondSegment.getArrivalTime().getHour() * 60 + secondSegment.getArrivalTime().getMinute();

        if(departureMinutes < arrivalMinutes) {
            departureMinutes += 24*60;
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
}