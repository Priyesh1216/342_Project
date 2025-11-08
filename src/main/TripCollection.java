import java.util.ArrayList;
import java.util.List;

import javafx.scene.chart.PieChart.Data;

// Store booked trips
// Followed similar logic to Connections
public class TripCollection {
    private List<BookedTrip> trips;
    private DatabaseManager dbManager;
    private Connections connections;

    public TripCollection() {
        this.trips = new ArrayList<>();
        this.dbManager = null;
        this.connections = null;
    }

    public void setDatabaseManager(DatabaseManager dbManager, Connections connections) {
        this.dbManager = dbManager;
        this.connections = connections;
        loadFromDatabase();
    }

    private void loadFromDatabase() {
        if (dbManager == null || connections == null)
            return;

        try {
            System.out.println("Loading booked trips from database...");
            List<BookedTrip> loaded = dbManager.loadAllBookedTrips(connections);
            trips.addAll(loaded);
            System.out.println("Loaded " + loaded.size() + " booked trips from database.");
        } catch (Exception e) {
            System.err.println("Error loading trips from database: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Save a booked trip
    public void saveTrip(BookedTrip trip) {
        if (trip != null && trip.isValid()) {
            trips.add(trip);
            System.out.println("Trip saved: " + trip.getTripId());

            // Save to database if available
            if (dbManager != null) {
                try {
                    dbManager.saveBookedTrip(trip);
                } catch (Exception e) {
                    System.err.println("Error saving trip to database: " + e.getMessage());
                }
            }
        } else {
            System.err.println("Cannot save trip");
        }
    }

    public List<BookedTrip> findTripsByClientId(String clientId) {
        List<BookedTrip> clientTrips = new ArrayList<>();

        for (BookedTrip trip : trips) {
            if (trip.hasReservationForClient(clientId)) {
                clientTrips.add(trip);
            }
        }

        return clientTrips;
    }

    // Find all trips for a client based on the given last name and ID
    // For the view trips task
    public List<BookedTrip> findTripsByCredentials(String lastName, String id) {
        List<BookedTrip> matchingTrips = new ArrayList<>();

        for (BookedTrip trip : trips) {
            for (Reservation reservation : trip.getReservations()) {
                Client client = reservation.getClient();
                System.out.println("Client: " + client.getLastName() + ", ID=" + client.getId());
                // Check if this client matches the criteria
                if (client.matchesCredentials(lastName, id)) {
                    matchingTrips.add(trip);
                    break; // Found a match in this trip
                }
            }
        }

        return matchingTrips;
    }

    public List<BookedTrip> getAllTrips() {
        return new ArrayList<>(trips); // Return copy to prevent external modification
    }

    public int getTripCount() {
        return trips.size();
    }

    public BookedTrip findTripById(String tripId) {
        for (BookedTrip trip : trips) {
            if (trip.getTripId().equals(tripId)) {
                return trip;
            }
        }
        return null;
    }

    // Reset the application
    public void clear() {
        trips.clear();
        System.out.println("All trips cleared from memory.");

        // Clear from database if available
        if (dbManager != null) {
            try {
                dbManager.clearBookedTrips();
                System.out.println("Cleared booked trips from database.");
            } catch (Exception e) {
                System.err.println("Error clearing trips from database: " + e.getMessage());
            }
        }
    }

    // One reservation per client per connection
    public boolean hasReservationForConnection(String clientId, Connection connection) {
        for (BookedTrip trip : trips) {
            for (Reservation reservation : trip.getReservations()) {
                // Check if this reservation is for the same client and same connection
                if (reservation.getClient().getId().equals(clientId) &&
                        reservation.isForConnection(connection)) {
                    return true;
                }
            }
        }
        return false;
    }
}