import java.time.LocalDateTime;

public class Connection {

    private String routeID;
    private LocalDateTime departureTime;
    private LocalDateTime arrivalTime;
    private String[] daysOfOperation;

    public Connection(String routeID, LocalDateTime departureTime, LocalDateTime arrivalTime, String[] daysOfOperation) {
        this.routeID = routeID;
        this.departureTime = departureTime;
        this.arrivalTime = arrivalTime;
        this.daysOfOperation = daysOfOperation;
    }

    public LocalDateTime getDepartureTime() {
        return departureTime;
    }

    public LocalDateTime getArrivalTime() {
        return arrivalTime;
    }

    public String[] getDaysOfOperation() {
        return daysOfOperation;
    }

}
