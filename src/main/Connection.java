import java.time.LocalTime;

public class Connection {
    private String routeID;
    private City departureCity;
    private City arrivalCity;
    private LocalTime departureTime;
    private LocalTime arrivalTime;
    private Train train;
    private String daysOfOperation;
    private double firstClassPrice;
    private double secondClassPrice;
    private boolean isNextDay; // From CSV (+1d) flag

    public Connection(String routeID, City departureCity, City arrivalCity,
            LocalTime departureTime, LocalTime arrivalTime, Train train,
            String daysOfOperation, double firstClassPrice, double secondClassPrice,
            boolean isNextDay) {
        this.routeID = routeID;
        this.departureCity = departureCity;
        this.arrivalCity = arrivalCity;
        this.departureTime = departureTime;
        this.arrivalTime = arrivalTime;
        this.train = train;
        this.daysOfOperation = daysOfOperation;
        this.firstClassPrice = firstClassPrice;
        this.secondClassPrice = secondClassPrice;
        this.isNextDay = isNextDay;
    }

    // Getters
    public String getRouteID() {
        return routeID;
    }

    public City getDepartureCity() {
        return departureCity;
    }

    public City getArrivalCity() {
        return arrivalCity;
    }

    public LocalTime getDepartureTime() {
        return departureTime;
    }

    public LocalTime getArrivalTime() {
        return arrivalTime;
    }

    public Train getTrain() {
        return train;
    }

    public String getDaysOfOperation() {
        return daysOfOperation;
    }

    public double getFirstClassPrice() {
        return firstClassPrice;
    }

    public double getSecondClassPrice() {
        return secondClassPrice;
    }

    public boolean isNextDay() {
        return isNextDay;
    }

    // If isNextDay is true, add 24 hours to the arrival time
    public int getDurationMinutes() {
        int depMinutes = departureTime.getHour() * 60 + departureTime.getMinute();
        int arrMinutes = arrivalTime.getHour() * 60 + arrivalTime.getMinute();

        // If marked as next day then add 24 hours
        if (isNextDay) {
            arrMinutes = arrMinutes + (24 * 60);
        }

        return arrMinutes - depMinutes;
    }

    // Formatted string (easier to read)
    public String getFormattedDuration() {
        int totalMinutes = getDurationMinutes();
        int hours = totalMinutes / 60;
        int minutes = totalMinutes % 60;

        if (hours > 0) {
            return hours + "h " + minutes + "m";
        }

        else {
            return minutes + "m";
        }
    }
}