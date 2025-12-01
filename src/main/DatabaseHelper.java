
import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper {

    private static final String DB_FILE = "railway.db";

    // Load the SQLite JDBC driver
    static {
        try {
            Class.forName("org.sqlite.JDBC");
            System.out.println("SQLite driver loaded");
        }

        catch (ClassNotFoundException e) {
            System.err.println("SQLite driver not found");
            e.printStackTrace();
        }
    }

    // Create tables (run once at startup)
    public static void setup() {
        try {
            java.sql.Connection conn = DriverManager.getConnection("jdbc:sqlite:" + DB_FILE);
            Statement stmt = conn.createStatement();

            // Create clients table
            stmt.execute(
                    "CREATE TABLE IF NOT EXISTS clients (id TEXT PRIMARY KEY, first_name TEXT, last_name TEXT, age INTEGER)");

            // Create trips table with all fields
            stmt.execute(
                    "CREATE TABLE IF NOT EXISTS trips (" +
                            "trip_id TEXT PRIMARY KEY, " +
                            "client_id TEXT, " +
                            "departure_city TEXT, " +
                            "arrival_city TEXT, " +
                            "departure_time TEXT, " +
                            "arrival_time TEXT, " +
                            "departure_date TEXT, " +
                            "arrival_date TEXT, " +
                            "booking_date TEXT, " +
                            "is_first_class INTEGER, " +
                            "is_next_day INTEGER, " +
                            "price REAL, " +
                            "duration_minutes INTEGER, " +
                            "stop_count INTEGER)");

            stmt.close();
            conn.close();

            System.out.println("Database ready");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Save client
    public static void saveClient(Client client) {
        try {
            java.sql.Connection conn = DriverManager.getConnection("jdbc:sqlite:" + DB_FILE);
            PreparedStatement ps = conn.prepareStatement("INSERT OR REPLACE INTO clients VALUES (?, ?, ?, ?)");

            ps.setString(1, client.getId());
            ps.setString(2, client.getFirstName());
            ps.setString(3, client.getLastName());
            ps.setInt(4, client.getAge());
            ps.executeUpdate();

            ps.close();
            conn.close();

            System.out.println("Client saved to database");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Save trip with all information
    public static void saveTrip(BookedTrip trip) {
        try {
            java.sql.Connection conn = DriverManager.getConnection("jdbc:sqlite:" + DB_FILE);
            PreparedStatement ps = conn.prepareStatement(
                    "INSERT OR REPLACE INTO trips VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");

            Trip t = trip.getSelectedTrip();
            Connection firstConn = t.getSegments().get(0).getConnection();

            ps.setString(1, trip.getTripId());
            ps.setString(2, trip.getPrimaryClient().getId());
            ps.setString(3, t.getDepartureCity());
            ps.setString(4, t.getArrivalCity());
            ps.setString(5, t.getDepartureTime().toString());
            ps.setString(6, t.getArrivalTime().toString());
            ps.setString(7, trip.getDepartureDate().toString());
            ps.setString(8, trip.getArrivalDate() != null ? trip.getArrivalDate().toString() : null);
            ps.setString(9, trip.getBookingDate().toString());
            ps.setInt(10, trip.isFirstClass() ? 1 : 0);
            ps.setInt(11, firstConn.isNextDay() ? 1 : 0);
            ps.setDouble(12, trip.getTotalPrice());
            ps.setInt(13, t.getTotalDurationMinutes());
            ps.setInt(14, t.getStopCount());

            ps.executeUpdate();

            ps.close();
            conn.close();

            System.out.println("Trip saved to database: " + trip.getTripId());

        } catch (Exception e) {
            System.err.println("Error saving trip to database");
            e.printStackTrace();
        }
    }

    // Load trips from database as BookedTrip objects with all information
    public static List<BookedTrip> loadTrips(String lastName, String clientId) {
        List<BookedTrip> trips = new ArrayList<>();

        String sql = "SELECT t.*, c.first_name, c.last_name, c.age FROM trips t " +
                "JOIN clients c ON t.client_id = c.id " +
                "WHERE c.last_name = ? AND c.id = ?";

        try {
            java.sql.Connection conn = DriverManager.getConnection("jdbc:sqlite:" + DB_FILE);
            PreparedStatement ps = conn.prepareStatement(sql);

            ps.setString(1, lastName);
            ps.setString(2, clientId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                // Create client
                Client client = new Client(
                        rs.getString("first_name"),
                        rs.getString("last_name"),
                        rs.getInt("age"),
                        rs.getString("client_id"));

                // Get all trip data from database
                String tripId = rs.getString("trip_id");
                String depCity = rs.getString("departure_city");
                String arrCity = rs.getString("arrival_city");
                String depTime = rs.getString("departure_time");
                String arrTime = rs.getString("arrival_time");
                String depDate = rs.getString("departure_date");
                String arrDateStr = rs.getString("arrival_date");
                boolean isFirstClass = rs.getInt("is_first_class") == 1;
                boolean isNextDay = rs.getInt("is_next_day") == 1;
                double price = rs.getDouble("price");

                // Create Cities
                City dep = new City(depCity);
                City arr = new City(arrCity);
                Train train = new Train("Saved Trip");

                // Use actual times from database
                Connection conn2 = new Connection(
                        "DB-" + tripId, dep, arr,
                        java.time.LocalTime.parse(depTime),
                        java.time.LocalTime.parse(arrTime),
                        train, "Daily",
                        price,
                        price,
                        isNextDay);

                // Create Trip
                Segment seg = new Segment(conn2);
                Trip trip = new Trip();
                trip.addSegment(seg);
                trip.computeTotals(isFirstClass, 0);

                // Create BookedTrip with all dates
                java.time.LocalDate departureDate = java.time.LocalDate.parse(depDate);
                BookedTrip bookedTrip = new BookedTrip(trip, isFirstClass, departureDate);

                // Set the original trip ID from database
                bookedTrip.setTripId(tripId);

                // Add reservation
                Reservation res = new Reservation(client, conn2, isFirstClass);
                bookedTrip.addReservation(res);

                trips.add(bookedTrip);

                System.out.println("Loaded trip from DB: " + tripId);
            }

            rs.close();
            ps.close();
            conn.close();

        } catch (Exception e) {
            System.err.println("Error loading trips from database");
            e.printStackTrace();
        }

        return trips;
    }
}