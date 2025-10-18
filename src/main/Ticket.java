// Generated once trip is booked
public class Ticket {
    private static int nextTicketId = 100000; // Starting ticket ID

    private int ticketId;
    private Reservation reservation; // The reservation this ticket documents
    private double firstClassPrice;
    private double secondClassPrice;
    private Trip trip;
    private boolean isFirstClass;

    public Ticket(Reservation reservation) {
        this.ticketId = nextTicketId++; // Auto-increment ticket ID
        this.reservation = reservation;
        this.trip = reservation.getTrip().getSelectedTrip();
        this.isFirstClass = reservation.isFirstClass();

        // Calculate prices from the trip
        this.firstClassPrice = trip.getTotalFirstClassPrice();
        this.secondClassPrice = trip.getTotalSecondClassPrice();
    }

    public Ticket(Trip trip, boolean isFirstClass) {
        this.ticketId = nextTicketId++; // Auto-increment ticket ID
        this.trip = trip;
        this.isFirstClass = isFirstClass;

        // Calculate prices from the trip
        this.firstClassPrice = trip.getTotalFirstClassPrice();
        this.secondClassPrice = trip.getTotalSecondClassPrice();
    }

    // Getters
    public int getTicketId() {
        return ticketId;
    }

    public Reservation getReservation() {
        return reservation;
    }

    public double getFirstClassPrice() {
        return firstClassPrice;
    }

    public double getSecondClassPrice() {
        return secondClassPrice;
    }

    public Trip getTrip() {
        return trip;
    }

    public boolean isFirstClass() {
        return isFirstClass;
    }

    public double getSelectedPrice() {
        return isFirstClass ? firstClassPrice : secondClassPrice;
    }

    public String getTicketClass() {
        return isFirstClass ? "First Class" : "Second Class";
    }

    public String toString() {
        // If this ticket is from a reservation, show detailed passenger info
        if (reservation != null) {
            Client client = reservation.getClient();
            Connection conn = reservation.getConnection();
            BookedTrip bookedTrip = reservation.getTrip();

            StringBuilder sb = new StringBuilder();
            sb.append("╔════════════════════════════════════════════╗\n");
            sb.append("║           RAILWAY TICKET                   ║\n");
            sb.append("╠════════════════════════════════════════════╣\n");
            sb.append("║ Ticket ID: ").append(String.format("%-30s", ticketId)).append(" ║\n");

            if (bookedTrip != null) {
                sb.append("║ Trip ID:   ").append(String.format("%-30s", bookedTrip.getTripId())).append(" ║\n");
            }

            sb.append("╠════════════════════════════════════════════╣\n");
            sb.append("║ Passenger: ").append(String.format("%-30s", client.getFullName())).append(" ║\n");
            sb.append("║ Age:       ").append(String.format("%-30s", client.getAge())).append(" ║\n");
            sb.append("║ ID:        ").append(String.format("%-30s", client.getId())).append(" ║\n");
            sb.append("╠════════════════════════════════════════════╣\n");
            sb.append("║ From:      ").append(String.format("%-30s", conn.getDepartureCity().getName())).append(" ║\n");
            sb.append("║ To:        ").append(String.format("%-30s", conn.getArrivalCity().getName())).append(" ║\n");
            sb.append("║ Departure: ").append(String.format("%-30s", conn.getDepartureTime())).append(" ║\n");
            sb.append("║ Arrival:   ").append(String.format("%-30s",
                    conn.getArrivalTime() + (conn.isNextDay() ? " (+1d)" : ""))).append(" ║\n");
            sb.append("║ Train:     ").append(String.format("%-30s", conn.getTrain().getType())).append(" ║\n");
            sb.append("╠════════════════════════════════════════════╣\n");
            sb.append("║ Class:     ").append(String.format("%-30s",
                    reservation.isFirstClass() ? "First Class" : "Second Class")).append(" ║\n");
            sb.append("║ Price:     €").append(String.format("%-29.2f", reservation.getPrice())).append(" ║\n");
            sb.append("╚════════════════════════════════════════════╝\n");

            return sb.toString();
        }

        // Fallback to original format for backward compatibility
        StringBuilder sb = new StringBuilder();
        sb.append("========================================\n");
        sb.append("           RAILWAY TICKET\n");
        sb.append("========================================\n");
        sb.append("Ticket ID:    #").append(ticketId).append("\n");
        sb.append("Class:        " + getTicketClass() + "\n");
        sb.append("Route:        " + trip.getDepartureCity() + " → " + trip.getArrivalCity() + "\n");
        sb.append("Departure:    " + trip.getDepartureTime() + "\n");
        sb.append("Arrival:      " + trip.getArrivalTime() + "\n");
        sb.append("Duration:     " + trip.getFormattedDuration() + "\n");
        sb.append("Stops:        " + trip.getStopCount() + "\n");
        sb.append("----------------------------------------\n");
        sb.append("First Class:  €" + String.format("%.2f", firstClassPrice) + "\n");
        sb.append("Second Class: €" + String.format("%.2f", secondClassPrice) + "\n");
        sb.append("Selected:     €" + String.format("%.2f", getSelectedPrice()) + "\n");
        sb.append("========================================\n");
        return sb.toString();
    }
}