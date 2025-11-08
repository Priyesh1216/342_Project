import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

// Trip booking, which may contain one or more eservations.
// Note: Each trip has a unique ID.
public class BookedTrip {
    private String tripId; // Unique ID
    private List<Reservation> reservations; // All reservations for this trip
    private Trip selectedTrip; // The trip/connection selected from search
    private LocalDateTime bookingDate; // Time of when trip was booked
    private boolean isFirstClass;
    private java.time.LocalDate departureDate;
    private java.time.LocalDate arrivalDate;

    public BookedTrip(Trip selectedTrip, boolean isFirstClass) {
        this(selectedTrip, isFirstClass, java.time.LocalDate.now());
    }

    public BookedTrip(Trip selectedTrip, boolean isFirstClass, java.time.LocalDate departureDate) {
        this.tripId = generateTripId();
        this.reservations = new ArrayList<>();
        this.selectedTrip = selectedTrip;
        this.bookingDate = LocalDateTime.now();
        this.departureDate = departureDate;
        this.isFirstClass = isFirstClass;
        this.arrivalDate = calculateArrivalDate(selectedTrip, departureDate);
    }

    private String generateTripId() {
        // Use UUID and take first 6 characters
        String uuid = UUID.randomUUID().toString().replace("-", "").substring(0, 6).toUpperCase();
        return "TR-" + uuid;
    }

    public void addReservation(Reservation reservation) {
        if (reservation != null) {
            reservations.add(reservation);
            reservation.setTrip(this); // Link back to this trip
        }
    }

    // Getters
    public String getTripId() {
        return tripId;
    }

    public List<Reservation> getReservations() {
        return new ArrayList<>(reservations);
    }

    public Trip getSelectedTrip() {
        return selectedTrip;
    }

    public LocalDateTime getBookingDate() {
        return bookingDate;
    }

    public boolean isFirstClass() {
        return isFirstClass;
    }

    public int getReservationCount() {
        return reservations.size();
    }

    public java.time.LocalDate getDepartureDate() {
        return departureDate;
    }

    public java.time.LocalDate getArrivalDate() {
        return arrivalDate;
    }

    public void setDepartureDate(java.time.LocalDate departureDate) {
        this.departureDate = departureDate;
    }

    public boolean hasReservationForClient(String clientId) {
        for (Reservation reservation : reservations) {
            if (reservation.getClient().getId().equals(clientId)) {
                return true;
            }
        }
        return false;
    }

    public void setTripId(String tripId) {
        this.tripId = tripId;
    }

    public Reservation getReservationForClient(String clientId) {
        for (Reservation reservation : reservations) {
            if (reservation.getClient().getId().equals(clientId)) {
                return reservation;
            }
        }
        return null;
    }

    public double getTotalPrice() {
        double total = 0.0;
        for (Reservation reservation : reservations) {
            total += reservation.getPrice();
        }
        return total;
    }

    // Get the first reservation's client
    public Client getPrimaryClient() {
        if (reservations.isEmpty()) {
            return null;
        }
        return reservations.get(0).getClient();
    }

    public boolean isFuture() {

        if (departureDate == null) {
            System.out.println("Trip " + tripId + ": departure date is null");
            return false;
        }
        boolean future = !departureDate.isBefore(LocalDate.now());
        System.out.println("Trip " + tripId + ", departure: " + departureDate + ", future=" + future);
        return future;
    }

    // Check if trip has all the info necessary
    public boolean isValid() {
        // Check trip ID exists
        if (tripId == null || tripId.isEmpty()) {
            return false;
        }
        // Check at least one reservation exists
        if (reservations.isEmpty()) {
            return false;
        }
        // Check selected trip exists
        if (selectedTrip == null) {
            return false;
        }

        // All reservations must be valid
        for (Reservation reservation : reservations) {
            if (!reservation.isValid()) {
                return false;
            }
        }

        return true;
    }

    private java.time.LocalDate calculateArrivalDate(Trip trip, java.time.LocalDate departureDate) {
        LocalDate currentDay = departureDate;

        for(int i = 0; i < trip.getConnections().size(); i++){
            Connection conn = trip.getConnections().get(i);
            if(conn.isNextDay()){
                currentDay = currentDay.plusDays(1);
            }

            if(i < trip.getConnections().size() - 1){
                Connection nextConn = trip.getConnections().get(i+1);
                int transferMinutes = (nextConn.getDepartureTime().getHour() * 60 + conn.getArrivalTime().getMinute()) - (conn.getArrivalTime().getHour() * 60 + conn.getArrivalTime().getMinute());

                if (transferMinutes < 0) {
                    currentDay = currentDay.plusDays(1);
                }
            }
        }
        return currentDay;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("═══════════════════════════════════════\n");
        sb.append("         BOOKED TRIP: ").append(tripId).append("\n");
        sb.append("═══════════════════════════════════════\n");
        sb.append("Route: ").append(selectedTrip.getDepartureCity())
                .append(" → ").append(selectedTrip.getArrivalCity()).append("\n");
        sb.append("Departure: ").append(selectedTrip.getDepartureTime()).append("\n");
        sb.append("Arrival: ").append(selectedTrip.getArrivalTime()).append("\n");
        sb.append("Duration: ").append(selectedTrip.getFormattedDuration()).append("\n");
        sb.append("Planned Departure Date: ")
                .append(departureDate != null ? departureDate : "Not set")
                .append("\n");
        sb.append("Planned Arrival Date: ").append(arrivalDate != null ? arrivalDate : "Not set").append("\n");
        sb.append("Class: ").append(isFirstClass ? "First Class" : "Second Class").append("\n");
        sb.append("Booking Date: ").append(bookingDate).append("\n");
        sb.append("───────────────────────────────────────\n");
        sb.append("Reservations (").append(reservations.size()).append("):\n");

        for (int i = 0; i < reservations.size(); i++) {
            Reservation res = reservations.get(i);
            sb.append("  ").append(i + 1).append(". ")
                    .append(res.getClient().getFullName())
                    .append(" (Reservation ID: ").append(res.getReservationId()).append(")\n");
        }

        sb.append("───────────────────────────────────────\n");
        sb.append("Total Price: €").append(String.format("%.2f", getTotalPrice())).append("\n");
        sb.append("═══════════════════════════════════════\n");

        return sb.toString();
    }
}