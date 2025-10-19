// Single reservation for one passenger on a specific connection.
// Multiple reservations can be grouped together in a BookedTrip.
// Each reservation has a ticket with a unique ID.

public class Reservation {
    // Counter for reservation IDs
    private static int nextReservationId = 1000; // Starting ID for reservations

    private int reservationId;
    private Client client; // The passenger for the reservation
    private Connection connection; // The connection being reserved
    private BookedTrip trip; // Reference to the parent trip
    private boolean isFirstClass;

    // Constructor
    public Reservation(Client client, Connection connection, boolean isFirstClass) {
        this.reservationId = nextReservationId++;
        this.client = client;
        this.connection = connection;
        this.isFirstClass = isFirstClass;
    }

    // Getters
    public int getReservationId() {
        return reservationId;
    }

    public Client getClient() {
        return client;
    }

    public Connection getConnection() {
        return connection;
    }

    public BookedTrip getTrip() {
        return trip;
    }

    public boolean isFirstClass() {
        return isFirstClass;
    }

    // Setters
    public void setTrip(BookedTrip trip) {
        this.trip = trip;
    }

    public void setFirstClass(boolean firstClass) {
        this.isFirstClass = firstClass;
    }

    public boolean isValid() {
        // Check client exists and is valid
        if (client == null || !client.isValid()) {
            return false;
        }
        // Check connection exists
        if (connection == null) {
            return false;
        }
        return true;
    }

    // Prevents duplicate reservations for the same connection
    public boolean isForConnection(Connection conn) {
        if (connection == null || conn == null) {
            return false;
        }

        // Compare route ID, cities, and departure time to find same connection
        return connection.getRouteID().equals(conn.getRouteID()) &&
                connection.getDepartureCity().getName().equals(conn.getDepartureCity().getName()) &&
                connection.getArrivalCity().getName().equals(conn.getArrivalCity().getName()) &&
                connection.getDepartureTime().equals(conn.getDepartureTime());
    }

    public double getPrice() {
        if (connection == null) {
            return 0.0;
        }
        return isFirstClass ? connection.getFirstClassPrice() : connection.getSecondClassPrice();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Reservation #").append(reservationId).append("\n");
        sb.append("  Client: ").append(client.getFullName()).append("\n");
        sb.append("  Route: ").append(connection.getDepartureCity().getName())
                .append(" → ").append(connection.getArrivalCity().getName()).append("\n");
        sb.append("  Departure: ").append(connection.getDepartureTime()).append("\n");
        sb.append("  Class: ").append(isFirstClass ? "First Class" : "Second Class").append("\n");
        sb.append("  Price: €").append(String.format("%.2f", getPrice())).append("\n");
        return sb.toString();
    }
}