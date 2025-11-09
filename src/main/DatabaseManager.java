import java.sql.*;
import java.time.LocalTime;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class DatabaseManager {
    private static final String DB_URL = "jdbc:sqlite:railway_system.db";
    private java.sql.Connection connection;

    public DatabaseManager() {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection(DB_URL);
            initializeDatabase();
            System.out.println("Database connected successfully.");
        } catch (Exception e) {
            System.err.println("Database connection error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void initializeDatabase() throws SQLException {
        Statement stmt = connection.createStatement();

        stmt.execute("CREATE TABLE IF NOT EXISTS cities (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "name TEXT UNIQUE NOT NULL)");

        stmt.execute("CREATE TABLE IF NOT EXISTS trains (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "type TEXT UNIQUE NOT NULL)");

        stmt.execute("CREATE TABLE IF NOT EXISTS connections (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "route_id TEXT NOT NULL, " +
                "departure_city_id INTEGER NOT NULL, " +
                "arrival_city_id INTEGER NOT NULL, " +
                "departure_time TEXT NOT NULL, " +
                "arrival_time TEXT NOT NULL, " +
                "train_id INTEGER NOT NULL, " +
                "days_of_operation TEXT NOT NULL, " +
                "first_class_price REAL NOT NULL, " +
                "second_class_price REAL NOT NULL, " +
                "is_next_day INTEGER NOT NULL, " +
                "FOREIGN KEY(departure_city_id) REFERENCES cities(id), " +
                "FOREIGN KEY(arrival_city_id) REFERENCES cities(id), " +
                "FOREIGN KEY(train_id) REFERENCES trains(id))");

        stmt.execute("CREATE TABLE IF NOT EXISTS trips (" +
                "trip_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "total_duration_minutes INTEGER NOT NULL, " +
                "total_transfer_time_minutes INTEGER NOT NULL, " +
                "first_class_price REAL NOT NULL, " +
                "second_class_price REAL NOT NULL)");

        stmt.execute("CREATE TABLE IF NOT EXISTS trip_connections (" +
                "trip_id INTEGER NOT NULL, " +
                "connection_id INTEGER NOT NULL, " +
                "sequence_order INTEGER NOT NULL, " +
                "PRIMARY KEY (trip_id, connection_id), " +
                "FOREIGN KEY(trip_id) REFERENCES trips(trip_id), " +
                "FOREIGN KEY(connection_id) REFERENCES connections(id))");

        stmt.execute("CREATE TABLE IF NOT EXISTS clients (" +
                "id TEXT PRIMARY KEY, " +
                "first_name TEXT NOT NULL, " +
                "last_name TEXT NOT NULL, " +
                "age INTEGER NOT NULL)");

        stmt.execute("CREATE TABLE IF NOT EXISTS booked_trips (" +
                "booked_trip_id TEXT PRIMARY KEY, " +
                "trip_id INTEGER NOT NULL, " +
                "booking_date TEXT NOT NULL, " +
                "departure_date TEXT, " +
                "arrival_date TEXT, " +
                "is_first_class INTEGER NOT NULL, " +
                "FOREIGN KEY(trip_id) REFERENCES trips(trip_id))");

        stmt.execute("CREATE TABLE IF NOT EXISTS reservations (" +
                "reservation_id INTEGER PRIMARY KEY, " +
                "booked_trip_id TEXT NOT NULL, " +
                "client_id TEXT NOT NULL, " +
                "connection_id INTEGER NOT NULL, " +
                "is_first_class INTEGER NOT NULL, " +
                "FOREIGN KEY(booked_trip_id) REFERENCES booked_trips(booked_trip_id), " +
                "FOREIGN KEY(client_id) REFERENCES clients(id), " +
                "FOREIGN KEY(connection_id) REFERENCES connections(id))");

        stmt.close();
        System.out.println("Database tables initialized.");
    }

    // ==================== CITY OPERATIONS ====================

    public int saveCityIfNotExists(String name) throws SQLException {
        PreparedStatement checkStmt = connection.prepareStatement(
                "SELECT id FROM cities WHERE LOWER(name) = LOWER(?)");
        checkStmt.setString(1, name.trim());
        ResultSet rs = checkStmt.executeQuery();

        if (rs.next()) {
            int id = rs.getInt("id");
            rs.close();
            checkStmt.close();
            return id;
        }
        rs.close();
        checkStmt.close();

        PreparedStatement insertStmt = connection.prepareStatement(
                "INSERT INTO cities (name) VALUES (?)", Statement.RETURN_GENERATED_KEYS);
        insertStmt.setString(1, name.trim());
        insertStmt.executeUpdate();

        ResultSet generatedKeys = insertStmt.getGeneratedKeys();
        int id = generatedKeys.next() ? generatedKeys.getInt(1) : -1;
        generatedKeys.close();
        insertStmt.close();

        return id;
    }

    // TRAIN OPERATIONS
    public int saveTrainIfNotExists(String type) throws SQLException {
        PreparedStatement checkStmt = connection.prepareStatement(
                "SELECT id FROM trains WHERE LOWER(type) = LOWER(?)");
        checkStmt.setString(1, type.trim());
        ResultSet rs = checkStmt.executeQuery();

        if (rs.next()) {
            int id = rs.getInt("id");
            rs.close();
            checkStmt.close();
            return id;
        }
        rs.close();
        checkStmt.close();

        PreparedStatement insertStmt = connection.prepareStatement(
                "INSERT INTO trains (type) VALUES (?)", Statement.RETURN_GENERATED_KEYS);
        insertStmt.setString(1, type.trim());
        insertStmt.executeUpdate();

        ResultSet generatedKeys = insertStmt.getGeneratedKeys();
        int id = generatedKeys.next() ? generatedKeys.getInt(1) : -1;
        generatedKeys.close();
        insertStmt.close();

        return id;
    }

    // CONNECTION OPERATIONS

    public void saveConnection(Connection conn, int depCityId, int arrCityId, int trainId) throws SQLException {
        PreparedStatement stmt = connection.prepareStatement(
                "INSERT INTO connections (route_id, departure_city_id, arrival_city_id, " +
                        "departure_time, arrival_time, train_id, days_of_operation, " +
                        "first_class_price, second_class_price, is_next_day) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");

        stmt.setString(1, conn.getRouteID());
        stmt.setInt(2, depCityId);
        stmt.setInt(3, arrCityId);
        stmt.setString(4, conn.getDepartureTime().toString());
        stmt.setString(5, conn.getArrivalTime().toString());
        stmt.setInt(6, trainId);
        stmt.setString(7, conn.getDaysOfOperation());
        stmt.setDouble(8, conn.getFirstClassPrice());
        stmt.setDouble(9, conn.getSecondClassPrice());
        stmt.setInt(10, conn.isNextDay() ? 1 : 0);

        stmt.executeUpdate();
        stmt.close();
    }

    public List<Connection> loadAllConnections(Connections connections) throws SQLException {
        List<Connection> loadedConnections = new ArrayList<>();

        String query = "SELECT c.*, " +
                "dc.name as dep_city_name, " +
                "ac.name as arr_city_name, " +
                "t.type as train_type " +
                "FROM connections c " +
                "JOIN cities dc ON c.departure_city_id = dc.id " +
                "JOIN cities ac ON c.arrival_city_id = ac.id " +
                "JOIN trains t ON c.train_id = t.id";

        Statement stmt = connection.createStatement();
        ResultSet rs = stmt.executeQuery(query);

        while (rs.next()) {
            String routeId = rs.getString("route_id");
            String depCityName = rs.getString("dep_city_name");
            String arrCityName = rs.getString("arr_city_name");
            LocalTime depTime = LocalTime.parse(rs.getString("departure_time"));
            LocalTime arrTime = LocalTime.parse(rs.getString("arrival_time"));
            String trainType = rs.getString("train_type");
            String daysOfOp = rs.getString("days_of_operation");
            double firstClassPrice = rs.getDouble("first_class_price");
            double secondClassPrice = rs.getDouble("second_class_price");
            boolean isNextDay = rs.getInt("is_next_day") == 1;

            City depCity = connections.findOrCreateCity(depCityName);
            City arrCity = connections.findOrCreateCity(arrCityName);
            Train train = connections.findOrCreateTrain(trainType);

            Connection connection = new Connection(routeId, depCity, arrCity, depTime, arrTime,
                    train, daysOfOp, firstClassPrice, secondClassPrice, isNextDay);

            loadedConnections.add(connection);
        }

        rs.close();
        stmt.close();
        return loadedConnections;
    }

    public int getConnectionCount() throws SQLException {
        Statement stmt = connection.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT COUNT(*) as count FROM connections");
        int count = rs.next() ? rs.getInt("count") : 0;
        rs.close();
        stmt.close();
        return count;
    }

    public void clearConnections() throws SQLException {
        Statement stmt = connection.createStatement();
        stmt.execute("DELETE FROM connections");
        stmt.close();
    }

    // FIND CONNECTION DB ID
    private int findConnectionDbId(Connection conn) throws SQLException {
        PreparedStatement stmt = connection.prepareStatement(
                "SELECT id FROM connections WHERE route_id = ? " +
                        "AND departure_time = ? AND arrival_time = ?");

        stmt.setString(1, conn.getRouteID());
        stmt.setString(2, conn.getDepartureTime().toString());
        stmt.setString(3, conn.getArrivalTime().toString());

        ResultSet rs = stmt.executeQuery();
        int id = -1;
        if (rs.next()) {
            id = rs.getInt("id");
        }

        rs.close();
        stmt.close();
        return id;
    }

    // FIND CONNECTION BY DB ID
    private Connection findConnectionById(int connectionId, Connections connections) {
        try {
            PreparedStatement stmt = connection.prepareStatement(
                    "SELECT c.*, dc.name as dep_city_name, ac.name as arr_city_name, " +
                            "t.type as train_type " +
                            "FROM connections c " +
                            "JOIN cities dc ON c.departure_city_id = dc.id " +
                            "JOIN cities ac ON c.arrival_city_id = ac.id " +
                            "JOIN trains t ON c.train_id = t.id " +
                            "WHERE c.id = ?");

            stmt.setInt(1, connectionId);
            ResultSet rs = stmt.executeQuery();

            Connection conn = null;
            if (rs.next()) {
                String routeId = rs.getString("route_id");
                String depCityName = rs.getString("dep_city_name");
                String arrCityName = rs.getString("arr_city_name");
                LocalTime depTime = LocalTime.parse(rs.getString("departure_time"));
                LocalTime arrTime = LocalTime.parse(rs.getString("arrival_time"));
                String trainType = rs.getString("train_type");
                String daysOfOp = rs.getString("days_of_operation");
                double firstClassPrice = rs.getDouble("first_class_price");
                double secondClassPrice = rs.getDouble("second_class_price");
                boolean isNextDay = rs.getInt("is_next_day") == 1;

                City depCity = connections.findOrCreateCity(depCityName);
                City arrCity = connections.findOrCreateCity(arrCityName);
                Train train = connections.findOrCreateTrain(trainType);

                conn = new Connection(routeId, depCity, arrCity, depTime, arrTime,
                        train, daysOfOp, firstClassPrice, secondClassPrice, isNextDay);
            }

            rs.close();
            stmt.close();
            return conn;

        } catch (SQLException e) {
            System.err.println("Error finding connection by ID: " + e.getMessage());
            return null;
        }
    }

    // TRIP OPERATIONS

    private int saveTrip(Trip trip) throws SQLException {
        PreparedStatement stmt = connection.prepareStatement(
                "INSERT INTO trips (total_duration_minutes, total_transfer_time_minutes, " +
                        "first_class_price, second_class_price) VALUES (?, ?, ?, ?)",
                Statement.RETURN_GENERATED_KEYS);

        stmt.setInt(1, trip.getTotalDurationMinutes());
        stmt.setInt(2, trip.getTransferTimeMinutes());
        stmt.setDouble(3, trip.getTotalFirstClassPrice());
        stmt.setDouble(4, trip.getTotalSecondClassPrice());

        stmt.executeUpdate();

        ResultSet generatedKeys = stmt.getGeneratedKeys();
        int tripId = generatedKeys.next() ? generatedKeys.getInt(1) : -1;
        generatedKeys.close();
        stmt.close();

        return tripId;
    }

    private void saveTripConnections(int tripDbId, Trip trip) throws SQLException {
        int sequence = 1;
        for (Connection conn : trip.getConnections()) {
            int connectionDbId = findConnectionDbId(conn);

            if (connectionDbId != -1) {
                PreparedStatement stmt = connection.prepareStatement(
                        "INSERT INTO trip_connections (trip_id, connection_id, sequence_order) " +
                                "VALUES (?, ?, ?)");

                stmt.setInt(1, tripDbId);
                stmt.setInt(2, connectionDbId);
                stmt.setInt(3, sequence++);

                stmt.executeUpdate();
                stmt.close();
            }
        }
    }

    private Trip loadTrip(int tripDbId, Connections connections) throws SQLException {
        PreparedStatement tripStmt = connection.prepareStatement(
                "SELECT * FROM trips WHERE trip_id = ?");
        tripStmt.setInt(1, tripDbId);
        ResultSet tripRs = tripStmt.executeQuery();

        if (!tripRs.next()) {
            tripRs.close();
            tripStmt.close();
            return null;
        }

        int transferTime = tripRs.getInt("total_transfer_time_minutes");

        tripRs.close();
        tripStmt.close();

        PreparedStatement connStmt = connection.prepareStatement(
                "SELECT connection_id FROM trip_connections " +
                        "WHERE trip_id = ? ORDER BY sequence_order");
        connStmt.setInt(1, tripDbId);
        ResultSet connRs = connStmt.executeQuery();

        Trip trip = new Trip();
        while (connRs.next()) {
            int connectionDbId = connRs.getInt("connection_id");
            Connection conn = findConnectionById(connectionDbId, connections);
            if (conn != null) {
                trip.addConnection(conn);
            }
        }

        connRs.close();
        connStmt.close();

        if (!trip.getConnections().isEmpty()) {
            int transferTimePerStop = trip.getStopCount() > 0 ? transferTime / trip.getStopCount() : 0;
            trip.computeTotals(false, transferTimePerStop);
        }

        return trip;
    }

    // CLIENT OPERATIONS

    public void saveClient(Client client) throws SQLException {
        PreparedStatement stmt = connection.prepareStatement(
                "INSERT OR REPLACE INTO clients (id, first_name, last_name, age) VALUES (?, ?, ?, ?)");

        stmt.setString(1, client.getId());
        stmt.setString(2, client.getFirstName());
        stmt.setString(3, client.getLastName());
        stmt.setInt(4, client.getAge());

        stmt.executeUpdate();
        stmt.close();
    }

    // BOOKED TRIP OPERATIONS

    public void saveBookedTrip(BookedTrip bookedTrip, Connections connectionsObj) throws SQLException {
        connection.setAutoCommit(false);

        try {
            // 1. Save clients
            for (Reservation res : bookedTrip.getReservations()) {
                saveClient(res.getClient());
            }

            // 2. Save Trip entity
            Trip selectedTrip = bookedTrip.getSelectedTrip();
            int tripDbId = saveTrip(selectedTrip);

            if (tripDbId == -1) {
                throw new SQLException("Failed to save trip");
            }

            // 3. Save trip-connection relationships
            saveTripConnections(tripDbId, selectedTrip);

            // 4. Save BookedTrip
            PreparedStatement bookedTripStmt = connection.prepareStatement(
                    "INSERT OR REPLACE INTO booked_trips (booked_trip_id, trip_id, booking_date, " +
                            "departure_date, arrival_date, is_first_class) VALUES (?, ?, ?, ?, ?, ?)");

            bookedTripStmt.setString(1, bookedTrip.getTripId());
            bookedTripStmt.setInt(2, tripDbId);
            bookedTripStmt.setString(3, bookedTrip.getBookingDate().toString());
            bookedTripStmt.setString(4,
                    bookedTrip.getDepartureDate() != null ? bookedTrip.getDepartureDate().toString() : null);
            bookedTripStmt.setString(5,
                    bookedTrip.getArrivalDate() != null ? bookedTrip.getArrivalDate().toString() : null);
            bookedTripStmt.setInt(6, bookedTrip.isFirstClass() ? 1 : 0);

            bookedTripStmt.executeUpdate();
            bookedTripStmt.close();

            // 5. Save reservations
            for (Reservation res : bookedTrip.getReservations()) {
                int connectionDbId = findConnectionDbId(res.getConnection());

                if (connectionDbId != -1) {
                    PreparedStatement resStmt = connection.prepareStatement(
                            "INSERT OR REPLACE INTO reservations (reservation_id, booked_trip_id, " +
                                    "client_id, connection_id, is_first_class) VALUES (?, ?, ?, ?, ?)");

                    resStmt.setInt(1, res.getReservationId());
                    resStmt.setString(2, bookedTrip.getTripId());
                    resStmt.setString(3, res.getClient().getId());
                    resStmt.setInt(4, connectionDbId);
                    resStmt.setInt(5, res.isFirstClass() ? 1 : 0);

                    resStmt.executeUpdate();
                    resStmt.close();
                }
            }

            connection.commit();
            connection.setAutoCommit(true);
            System.out.println("✓ Saved booked trip: " + bookedTrip.getTripId());

        } catch (SQLException e) {
            connection.rollback();
            connection.setAutoCommit(true);
            System.err.println("Error saving booked trip: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    public List<BookedTrip> loadAllBookedTrips(Connections connections) throws SQLException {
        List<BookedTrip> bookedTrips = new ArrayList<>();

        Statement stmt = connection.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM booked_trips");

        while (rs.next()) {
            String bookedTripId = rs.getString("booked_trip_id");
            int tripDbId = rs.getInt("trip_id");
            boolean isFirstClass = rs.getInt("is_first_class") == 1;
            String departureDateStr = rs.getString("departure_date");
            LocalDate departureDate = departureDateStr != null ? LocalDate.parse(departureDateStr) : LocalDate.now();

            Trip reconstructedTrip = loadTrip(tripDbId, connections);

            if (reconstructedTrip == null) {
                continue;
            }

            PreparedStatement resStmt = connection.prepareStatement(
                    "SELECT r.*, c.first_name, c.last_name, c.age " +
                            "FROM reservations r " +
                            "JOIN clients c ON r.client_id = c.id " +
                            "WHERE r.booked_trip_id = ?");
            resStmt.setString(1, bookedTripId);
            ResultSet resRs = resStmt.executeQuery();

            List<Reservation> reservations = new ArrayList<>();

            while (resRs.next()) {
                String clientId = resRs.getString("client_id");
                String firstName = resRs.getString("first_name");
                String lastName = resRs.getString("last_name");
                int age = resRs.getInt("age");
                Client client = new Client(firstName, lastName, age, clientId);

                int connectionDbId = resRs.getInt("connection_id");
                Connection conn = findConnectionById(connectionDbId, connections);

                if (conn != null) {
                    Reservation reservation = new Reservation(client, conn, isFirstClass);
                    reservations.add(reservation);
                }
            }

            resRs.close();
            resStmt.close();

            if (!reservations.isEmpty()) {
                BookedTrip bookedTrip = new BookedTrip(reconstructedTrip, isFirstClass, departureDate);
                for (Reservation res : reservations) {
                    bookedTrip.addReservation(res);
                }
                bookedTrips.add(bookedTrip);
            }
        }

        rs.close();
        stmt.close();
        return bookedTrips;
    }

    public void clearBookedTrips() throws SQLException {
        Statement stmt = connection.createStatement();
        stmt.execute("DELETE FROM reservations");
        stmt.execute("DELETE FROM booked_trips");
        stmt.execute("DELETE FROM trip_connections");
        stmt.execute("DELETE FROM trips");
        stmt.close();
    }

    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("Database connection closed.");
            }
        } catch (SQLException e) {
            System.err.println("Error closing database: " + e.getMessage());
        }
    }
}