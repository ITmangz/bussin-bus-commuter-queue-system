import java.math.BigDecimal;
import java.util.List;
import qpal.model.FarePolicy;
import qpal.model.RouteDropPoints;

public class FarePolicyTest {
    static void equal(String expected, BigDecimal actual) {
        if (!new BigDecimal(expected).equals(actual))
            throw new AssertionError(expected + " != " + actual);
    }

    public static void main(String[] args) throws Exception {
        for (String type : List.of("Student", "PWD", "Senior")) {
            equal("40", FarePolicy.passengerFare(new BigDecimal("50"), type));
            equal("41", FarePolicy.passengerFare(new BigDecimal("51"), type));
            equal("42", FarePolicy.passengerFare(new BigDecimal("53"), type));
        }
        equal("51", FarePolicy.passengerFare(new BigDecimal("51"), "Regular"));
        equal(
                "174",
                FarePolicy.total(
                        new BigDecimal("51"), List.of("Regular", "Student", "PWD", "Senior")));
        BigDecimal stop = RouteDropPoints.fare("Amadeo", "Silang", new BigDecimal("51"));
        equal("31", stop);
        equal("25", FarePolicy.passengerFare(stop, "Student"));
        if (FarePolicy.validRouteFare(new BigDecimal("50.01"))
                || FarePolicy.validRouteFare(BigDecimal.ZERO))
            throw new AssertionError("Invalid route fare accepted");
        if (!FarePolicy.validRouteFare(new BigDecimal("50.00")))
            throw new AssertionError("Whole amount rejected");
        if (!FarePolicy.format(new BigDecimal("40.00")).equals("40"))
            throw new AssertionError("Decimal display");
        var method =
                Class.forName("qpal.view.Admin.ManagementComboBoxes")
                        .getDeclaredMethod("seatCapacity", int.class);
        method.setAccessible(true);
        javax.swing.SwingUtilities.invokeAndWait(
                () -> {
                    try {
                        var combo = (javax.swing.JComboBox<?>) method.invoke(null, 36);
                        if (combo.getItemCount() != 6 || !combo.getSelectedItem().equals(36))
                            throw new AssertionError("Seat selection");
                        for (int i = 0; i < 6; i++)
                            if (!combo.getItemAt(i).equals(24 + 4 * i))
                                throw new AssertionError("Seat options");
                        if (((javax.swing.JComboBox<?>) method.invoke(null, 30)).getSelectedIndex()
                                != -1) throw new AssertionError("Legacy capacity");
                    } catch (ReflectiveOperationException ex) {
                        throw new RuntimeException(ex);
                    }
                });
        System.out.println(
                "PASS: eligible discounts, mixed totals, rounding, route validation, whole-peso"
                    + " display and seat choices");
    }
}
