package qpal.model;

/** A workstation, distinct from the trip or queue currently using it. */
public record EmployeeStation(String kind, int number) {
    public EmployeeStation {
        if (!("Payment".equals(kind) || "Boarding".equals(kind)) || number < 1 || number > 2)
            throw new IllegalArgumentException("Choose one of the four stations.");
    }

    public boolean boarding() {
        return "Boarding".equals(kind);
    }

    public String title() {
        return (boarding() ? "Boarding Gate " : "Counter ") + number;
    }

    public String description() {
        return boarding() ? "Commuter Boarding" : "Payment Counter";
    }
}
