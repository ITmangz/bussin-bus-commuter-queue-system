package qpal.model;

public class Route {

    private int routeID;
    private String origin;
    private String destination;
    private double fare;
    private String status;

    public Route() {}

    public Route(int routeID, String origin, String destination, double fare, String status) {
        this.routeID = routeID;
        this.origin = origin;
        this.destination = destination;
        this.fare = fare;
        this.status = status;
    }

    public int getRouteID() {
        return routeID;
    }

    public void setRouteID(int routeID) {
        this.routeID = routeID;
    }

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public double getFare() {
        return fare;
    }

    public void setFare(double fare) {
        this.fare = fare;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return origin + " → " + destination;
    }
}
