package qpal.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public final class BookingData {
    private BookingData() {}

    public record TripOption(
            int id,
            String bus,
            String origin,
            String destination,
            LocalDate date,
            LocalTime time,
            BigDecimal fare,
            int capacity,
            int available) {
        public String route() {
            return origin + " - " + destination;
        }

        public String schedule() {
            return date + " | " + time;
        }
    }

    public record Passenger(String name, String type, int seat) {}

    public record FareLine(String type, BigDecimal base, BigDecimal charged) {}

    public record Receipt(
            int bookingId,
            String reference,
            LocalDate queueDate,
            int queueNumber,
            String bus,
            String route,
            String schedule,
            String seats,
            BigDecimal total,
            String method,
            String paymentStatus,
            List<FareLine> fares) {
        public Receipt {
            fares = List.copyOf(fares);
        }

        public Receipt(
                int bookingId,
                String reference,
                LocalDate queueDate,
                int queueNumber,
                String bus,
                String route,
                String schedule,
                String seats,
                BigDecimal total,
                String method,
                String paymentStatus) {
            this(
                    bookingId,
                    reference,
                    queueDate,
                    queueNumber,
                    bus,
                    route,
                    schedule,
                    seats,
                    total,
                    method,
                    paymentStatus,
                    List.of());
        }

        public String fareBreakdown() {
            if (fares.isEmpty() || fares.stream().anyMatch(f -> f.base() == null))
                return "Fare charged: PHP "
                        + FarePolicy.format(total)
                        + "\nDiscount details: Not recorded";
            BigDecimal subtotal =
                    fares.stream().map(FareLine::base).reduce(BigDecimal.ZERO, BigDecimal::add);
            StringBuilder text =
                    new StringBuilder("Fare subtotal: PHP " + FarePolicy.format(subtotal));
            for (String type : List.of("Student", "PWD", "Senior")) {
                var eligible = fares.stream().filter(f -> type.equals(f.type())).toList();
                if (!eligible.isEmpty()) {
                    BigDecimal discount =
                            eligible.stream()
                                    .map(f -> f.base().subtract(f.charged()))
                                    .reduce(BigDecimal.ZERO, BigDecimal::add);
                    text.append("\n")
                            .append(type)
                            .append(" 20% (x")
                            .append(eligible.size())
                            .append("): -PHP ")
                            .append(FarePolicy.format(discount));
                }
            }
            text.append("\nTotal discount: PHP ")
                    .append(FarePolicy.format(subtotal.subtract(total)));
            return text.toString();
        }

        public String text() {
            return text("P");
        }

        public String text(String prefix) {
            return "BUSSIN TICKET\n\n"
                    + detailsText(prefix)
                    + ("Paid".equals(paymentStatus)
                            ? ""
                            : "\n"
                                  + "Wait for your queue number at the payment counter.\n"
                                  + "Payment pending staff verification.");
        }

        public String detailsText(String prefix) {
            return "Queue: "
                    + queueDate
                    + " / "
                    + String.format(java.util.Locale.ROOT, "%s%03d", prefix, queueNumber)
                    + "\nBooking: "
                    + reference
                    + "\nBus: "
                    + bus
                    + "\nRoute: "
                    + route
                    + "\nDeparture: "
                    + schedule
                    + "\nSeats: "
                    + seats
                    + "\nTotal: PHP "
                    + FarePolicy.format(total)
                    + "\nPayment: "
                    + method
                    + " - "
                    + paymentStatus;
        }
    }

    public record QueueRow(
            int id,
            int bookingId,
            int number,
            String route,
            String bus,
            String schedule,
            String passenger,
            String payment,
            String status,
            int passengers) {}

    public record RevenueData(
            List<Object[]> rows,
            int paidPassengers,
            int pendingPayments,
            BigDecimal todayRevenue,
            BigDecimal totalRevenue) {}
}
