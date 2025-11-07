import java.util.ArrayList;
import java.util.List;

public class Trip {
    private List<Connection> connections;
    private int totalDurationMinutes;
    private double totalFirstClassPrice;
    private double totalSecondClassPrice;
    private int transferTimeMinutes;

    public Trip() {
        this.connections = new ArrayList<>();
        this.totalDurationMinutes = 0;
        this.totalFirstClassPrice = 0.0;
        this.totalSecondClassPrice = 0.0;
        this.transferTimeMinutes = 0;
    }

    public void addConnection(Connection connection) {
        connections.add(connection);
    }

    public List<Connection> getConnections() {
        return connections;
    }

    public int getStopCount() {
        if (connections.size() > 0) {
            return connections.size() - 1;
        }
        return 0;
    }

    // Compute total durations + prices
    public void computeTotals(boolean firstClass, int transferMinutes) {
        totalDurationMinutes = 0;
        totalFirstClassPrice = 0.0;
        totalSecondClassPrice = 0.0;
        transferTimeMinutes = transferMinutes * getStopCount();

        for (Connection connection: connections) {
            totalDurationMinutes = totalDurationMinutes + connection.getDurationMinutes();
            totalFirstClassPrice = totalFirstClassPrice + connection.getFirstClassPrice();
            totalSecondClassPrice = totalSecondClassPrice + connection.getSecondClassPrice();
        }

        totalDurationMinutes = totalDurationMinutes + transferTimeMinutes;
    }

    public int getTotalDurationMinutes() {
        return totalDurationMinutes;
    }

    public double getTotalFirstClassPrice() {
        return totalFirstClassPrice;
    }

    public double getTotalSecondClassPrice() {
        return totalSecondClassPrice;
    }

    public int getTransferTimeMinutes() {
        return transferTimeMinutes;
    }

    // Get formatted duration (later used in Trip)
    public String getFormattedDuration() {
        int hours = totalDurationMinutes / 60;
        int minutes = totalDurationMinutes % 60;

        if (hours > 0) {
            return hours + "h " + minutes + "m";
        }

        else {
            return minutes + "m";
        }
    }

    public String getDepartureCity() {
        if (connections.size() > 0) {
            return connections.get(0).getDepartureCity().getName();
        }
        return "";
    }

    // The LAST connection's arrival city is the trip arrival city
    public String getArrivalCity() {
        if (connections.size() > 0) {
            return connections.get(connections.size() - 1).getArrivalCity().getName();
        }
        return "";
    }

    // The FIRST connection's departure time is the trip departure time
    public String getDepartureTime() {
        if (connections.size() > 0) {
            return connections.get(0).getDepartureTime().toString();
        }
        return "";
    }

    // Used in Ticket.java
    public String getArrivalTime() {
        if (connections.size() > 0) {
            Connection lastConnection = connections.get(connections.size() - 1);
            String time = lastConnection.getArrivalTime().toString();

            if (lastConnection.isNextDay()) {
                time = time + " (+1d)";
            }

            return time;
        }
        return "";
    }
}