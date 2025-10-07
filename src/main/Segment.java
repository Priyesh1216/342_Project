import java.time.Duration;

public class Segment {
    
    private Duration connectionDuration;

    public Segment(Duration connectionDuration) {
        this.connectionDuration = connectionDuration;
    }

    public Duration getConnectionDuration() {
        return connectionDuration;
    }

}
