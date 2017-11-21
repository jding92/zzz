package ca.ubc.cs.cpsc210.translink.model;

/**
 * Represents an estimated arrival with time to arrival in minutes,
 * the route number, and the name of destination.
 */
public class Arrival implements Comparable<Arrival>{
    private int timeToStop;
    private String destination;
    private String routeNumber;
    private String status;

    /**
     * Constructs a new arrival with the given time to stop (in minutes),
     * destination and platform.
     * @param timeToStop     time until bus arrives at stop (in minutes)
     * @param destination    name of destination stop
     * @param routeNumber    route number of the bus to arrive
     */
    public Arrival(int timeToStop, String destination, String routeNumber) {
        this.timeToStop = timeToStop;
        this.destination = destination;
        this.routeNumber = routeNumber;
    }

    /**
     * Get time until bus arrives at stop in minutes.
     *
     * @return  time until bus arrives at stop in minutes
     */
    public int getTimeToStopInMins() {
        return timeToStop;
    }

    public String getDestination() {
        return destination;
    }

    public String getRouteNumber() {
        return routeNumber;
    }

    /**
     * Order bus arrivals by time until bus arrives at stop
     * (shorter times ordered before longer times)
     */
    @Override
    public int compareTo(Arrival arrival) {
        // Do not modify the implementation of this method!
        return this.timeToStop - arrival.timeToStop;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
