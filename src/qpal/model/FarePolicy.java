package qpal.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/** Whole-peso fares; one 20% discount per eligible passenger, never stacked. */
public final class FarePolicy {
    private FarePolicy() {}
    public static BigDecimal passengerFare(BigDecimal fare, String type) {
        if (fare == null || fare.signum() <= 0) throw new IllegalArgumentException("Fare must be positive.");
        BigDecimal rate = switch (type) {
            case "Regular" -> BigDecimal.ONE;
            case "Student", "PWD", "Senior" -> new BigDecimal("0.80");
            default -> throw new IllegalArgumentException("Invalid passenger type.");
        };
        return fare.multiply(rate).setScale(0, RoundingMode.HALF_UP);
    }
    public static BigDecimal total(BigDecimal fare, List<String> types) {
        return types.stream().map(type -> passengerFare(fare, type)).reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    public static String format(BigDecimal fare) {
        return fare.setScale(0, RoundingMode.HALF_UP).toPlainString();
    }
    public static boolean validRouteFare(BigDecimal fare) {
        return fare != null && fare.signum() > 0 && fare.compareTo(new BigDecimal("99999999")) <= 0
                && fare.stripTrailingZeros().scale() <= 0;
    }
}
