public class Ticket {
    private double firstClassPrice;
    private double secondClassPrice;
    private Trip trip;
    private boolean isFirstClass;

    public Ticket(Trip trip, boolean isFirstClass) {
        this.trip = trip;
        this.isFirstClass = isFirstClass;

        // Calculate prices from the trip
        this.firstClassPrice = trip.getTotalFirstClassPrice();
        this.secondClassPrice = trip.getTotalSecondClassPrice();
    }

    // Getters
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

    // toString for display
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("========================================\n");
        sb.append("           RAILWAY TICKET\n");
        sb.append("========================================\n");
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