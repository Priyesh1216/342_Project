import java.util.ArrayList;
import java.util.List;

// Store booked trips
// Followed similar logic to Connections
public class TripCollection {
    private List<BookedTrip> trips;

    public TripCollection() {
        this.trips = new ArrayList<>();
    }

    // Save a booked trip
    public void saveTrip(BookedTrip trip) {
        if (trip != null && trip.isValid()) {
            trips.add(trip);
            System.out.println("Trip saved: " + trip.getTripId());
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