import java.util.ArrayList;
import java.util.List;

public class Trip {
    private List<Segment> segments;
    private int totalDurationMinutes;
    private double totalFirstClassPrice;
    private double totalSecondClassPrice;
    private int transferTimeMinutes;

    public Trip() {
        this.segments = new ArrayList<>();
        this.totalDurationMinutes = 0;
        this.totalFirstClassPrice = 0.0;
        this.totalSecondClassPrice = 0.0;
        this.transferTimeMinutes = 0;
    }

    public void addSegment(Segment segment) {
        segments.add(segment);
    }

    public List<Segment> getSegments() {
        return segments;
    }

    public int getStopCount() {
        if (segments.size() > 0) {
            return segments.size() - 1;
        }
        return 0;
    }

    // Compute total durations + prices
    public void computeTotals(boolean firstClass, int transferMinutes) {
        totalDurationMinutes = 0;
        totalFirstClassPrice = 0.0;
        totalSecondClassPrice = 0.0;
        transferTimeMinutes = transferMinutes * getStopCount();

        for (Segment segment : segments) {
            totalDurationMinutes = totalDurationMinutes + segment.getConnection().getDurationMinutes();
            totalFirstClassPrice = totalFirstClassPrice + segment.getConnection().getFirstClassPrice();
            totalSecondClassPrice = totalSecondClassPrice + segment.getConnection().getSecondClassPrice();
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
        if (segments.size() > 0) {
            return segments.get(0).getConnection().getDepartureCity().getName();
        }
        return "";
    }

    // The LAST segment's arrival city is the trip arrival city
    public String getArrivalCity() {
        if (segments.size() > 0) {
            return segments.get(segments.size() - 1).getConnection().getArrivalCity().getName();
        }
        return "";
    }

    // The FIRST segment's departure time is the trip departure time
    public String getDepartureTime() {
        if (segments.size() > 0) {
            return segments.get(0).getConnection().getDepartureTime().toString();
        }
        return "";
    }

    // Used in Ticket.java
    public String getArrivalTime() {
        if (segments.size() > 0) {
            Segment lastSegment = segments.get(segments.size() - 1);
            String time = lastSegment.getConnection().getArrivalTime().toString();

            if (lastSegment.getConnection().isNextDay()) {
                time = time + " (+1d)";
            }

            return time;
        }
        return "";
    }
}