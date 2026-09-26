package qpal.model;

public class Trip {

    private int tripID;
    private int busID;
    private int routeID;
    private String busName;
    private String origin;
    private String destination;
    private String departureDate;
    private String departureTime;
    private double fare;
    private int availableSeats;
    private int seatCapacity;
    private String status;

    public Trip() {

    }

    public Trip(int tripID, int busID, int routeID, String busName, String origin, String destination, String departureDate, String departureTime, double fare, int availableSeats, String status) {

        this.tripID = tripID;
        this.busID = busID;
        this.routeID = routeID;
        this.busName = busName;
        this.origin = origin;
        this.destination = destination;
        this.departureDate = departureDate;
        this.departureTime = departureTime;
        this.fare = fare;
        this.availableSeats = availableSeats;
        this.status = status;

    }

    public int getTripID() {
        return tripID;
    }

    public void setTripID(int scheduleID) {
        this.tripID = scheduleID;
    }

    public int getBusID() {
        return busID;
    }

    public void setBusID(int busID) {
        this.busID = busID;
    }

    public int getRouteID() {
    return routeID;
    }

    public void setRouteID(int routeID) {
        this.routeID = routeID;
    }

    public String getBusName() {
        return busName;
    }

    public void setBusName(String busName) {
        this.busName = busName;
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

    public String getDepartureDate() {
        return departureDate;
    }

    public void setDepartureDate(String departureDate) {
        this.departureDate = departureDate;
    }

    public String getDepartureTime() {
        return departureTime;
    }

    public void setDepartureTime(String departureTime) {
        this.departureTime = departureTime;
    }

    public double getFare() {
        return fare;
    }

    public void setFare(double fare) {
        this.fare = fare;
    }

    public int getAvailableSeats() {
        return availableSeats;
    }

    public int getSeatCapacity() {
        return seatCapacity;
    }

    public void setSeatCapacity(int seatCapacity) {
        this.seatCapacity = seatCapacity;
    }

    public void setAvailableSeats(int availableSeats) {
        this.availableSeats = availableSeats;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

}
