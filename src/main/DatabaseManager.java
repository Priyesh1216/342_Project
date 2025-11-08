import java.sql.*;
import java.time.LocalTime;
import java.time.LocalDate;
import java.time.LocalDateTime;
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

        stmt.execute("CREATE TABLE IF NOT EXISTS clients (" +
                "id TEXT PRIMARY KEY, " +
                "first_name TEXT NOT NULL, " +
                "last_name TEXT NOT NULL, " +
                "age INTEGER NOT NULL)");

        stmt.execute("CREATE TABLE IF NOT EXISTS booked_trips (" +
                "trip_id TEXT PRIMARY KEY, " +
                "departure_city TEXT NOT NULL, " +
                "arrival_city TEXT NOT NULL, " +
                "departure_time TEXT NOT NULL, " +
                "arrival_time TEXT NOT NULL, " +
                "booking_date TEXT NOT NULL, " +
                "is_first_class INTEGER NOT NULL, " +
                "departure_date TEXT, " +
                "arrival_date TEXT)");

        stmt.execute("CREATE TABLE IF NOT EXISTS reservations (" +
                "reservation_id INTEGER PRIMARY KEY, " +
                "trip_id TEXT NOT NULL, " +
                "client_id TEXT NOT NULL, " +
                "connection_route_id TEXT NOT NULL, " +
                "is_first_class INTEGER NOT NULL, " +
                "FOREIGN KEY(trip_id) REFERENCES booked_trips(trip_id), " +
                "FOREIGN KEY(client_id) REFERENCES clients(id))");

        stmt.close();

    }

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

    public void saveBookedTrip(BookedTrip trip) throws SQLException {
        for (Reservation res : trip.getReservations()) {
            saveClient(res.getClient());
        }

        PreparedStatement tripStmt = connection.prepareStatement(
                "INSERT OR REPLACE INTO booked_trips (trip_id, departure_city, arrival_city, " +
                        "departure_time, arrival_time, booking_date, is_first_class, " +
                        "departure_date, arrival_date) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)");

        Trip selectedTrip = trip.getSelectedTrip();
        tripStmt.setString(1, trip.getTripId());
        tripStmt.setString(2, selectedTrip.getDepartureCity());
        tripStmt.setString(3, selectedTrip.getArrivalCity());
        tripStmt.setString(4, selectedTrip.getDepartureTime());
        tripStmt.setString(5, selectedTrip.getArrivalTime());
        tripStmt.setString(6, trip.getBookingDate().toString());
        tripStmt.setInt(7, trip.isFirstClass() ? 1 : 0);
        tripStmt.setString(8, trip.getDepartureDate() != null ? trip.getDepartureDate().toString() : null);
        tripStmt.setString(9, trip.getArrivalDate() != null ? trip.getArrivalDate().toString() : null);

        tripStmt.executeUpdate();
        tripStmt.close();

        for (Reservation res : trip.getReservations()) {
            PreparedStatement resStmt = connection.prepareStatement(
                    "INSERT OR REPLACE INTO reservations (reservation_id, trip_id, client_id, " +
                            "connection_route_id, is_first_class) VALUES (?, ?, ?, ?, ?)");

            resStmt.setInt(1, res.getReservationId());
            resStmt.setString(2, trip.getTripId());
            resStmt.setString(3, res.getClient().getId());
            resStmt.setString(4, res.getConnection().getRouteID());
            resStmt.setInt(5, res.isFirstClass() ? 1 : 0);

            resStmt.executeUpdate();
            resStmt.close();
        }
    }

    public List<BookedTrip> loadAllBookedTrips(Connections connections) throws SQLException {
        List<BookedTrip> trips = new ArrayList<>();

        Statement stmt = connection.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM booked_trips");

        while (rs.next()) {
            String tripId = rs.getString("trip_id");
            boolean isFirstClass = rs.getInt("is_first_class") == 1;
            String departureDateStr = rs.getString("departure_date");
            LocalDate departureDate = departureDateStr != null ? LocalDate.parse(departureDateStr) : LocalDate.now();

            PreparedStatement resStmt = connection.prepareStatement(
                    "SELECT r.*, c.first_name, c.last_name, c.age " +
                            "FROM reservations r " +
                            "JOIN clients c ON r.client_id = c.id " +
                            "WHERE r.trip_id = ?");
            resStmt.setString(1, tripId);
            ResultSet resRs = resStmt.executeQuery();

            List<Reservation> reservations = new ArrayList<>();
            Trip reconstructedTrip = null;

            while (resRs.next()) {
                String clientId = resRs.getString("client_id");
                String firstName = resRs.getString("first_name");
                String lastName = resRs.getString("last_name");
                int age = resRs.getInt("age");
                Client client = new Client(firstName, lastName, age, clientId);

                String routeId = resRs.getString("connection_route_id");
                Connection conn = findConnectionByRouteId(routeId, connections);

                if (conn != null && reconstructedTrip == null) {
                    reconstructedTrip = new Trip();
                    reconstructedTrip.addConnection(conn);
                    reconstructedTrip.computeTotals(isFirstClass, 0);
                }

                if (conn != null) {
                    Reservation reservation = new Reservation(client, conn, isFirstClass);
                    reservations.add(reservation);
                }
            }

            resRs.close();
            resStmt.close();

            if (reconstructedTrip != null && !reservations.isEmpty()) {
                BookedTrip bookedTrip = new BookedTrip(reconstructedTrip, isFirstClass, departureDate);
                for (Reservation res : reservations) {
                    bookedTrip.addReservation(res);
                }
                trips.add(bookedTrip);
            }
        }

        rs.close();
        stmt.close();
        return trips;
    }

    private Connection findConnectionByRouteId(String routeId, Connections connections) {
        for (Connection conn : connections.getAll()) {
            if (conn.getRouteID().equals(routeId)) {
                return conn;
            }
        }
        return null;
    }

    public void clearBookedTrips() throws SQLException {
        Statement stmt = connection.createStatement();
        stmt.execute("DELETE FROM reservations");
        stmt.execute("DELETE FROM booked_trips");
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
