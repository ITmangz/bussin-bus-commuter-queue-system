package qpal.model;

public class Bus {

    private int busID;
    private String busNumber;
    private int seatCapacity;
    private int availableSeats;
    private String busStatus;
    private String statusReason;
    private SeatLayout seatLayout = SeatLayout.TWO_TWO;

    public SeatLayout getSeatLayout() { return seatLayout; }
    public void setSeatLayout(SeatLayout layout) { seatLayout = java.util.Objects.requireNonNull(layout); }

    public String getStatusReason() { return statusReason; }

    public void setStatusReason(String statusReason) { this.statusReason = statusReason; }

    public Bus() {}

    public int getBusID() {
        return busID;
    }

    public void setBusID(int busID) {
        this.busID = busID;
    }

    public String getBusNumber() {
        return busNumber;
    }

    public void setBusNumber(String busNumber) {
        this.busNumber = busNumber;
    }

    public int getSeatCapacity() {
        return seatCapacity;
    }

    public void setSeatCapacity(int seatCapacity) {
        this.seatCapacity = seatCapacity;
    }

    public int getAvailableSeats() {
        return availableSeats;
    }

    public void setAvailableSeats(int availableSeats) {
        this.availableSeats = availableSeats;
    }

    public String getBusStatus() {
        return busStatus;
    }

    public void setBusStatus(String busStatus) {
        this.busStatus = busStatus;
    }

    @Override
    public String toString() {
        return busNumber;
    }
}
