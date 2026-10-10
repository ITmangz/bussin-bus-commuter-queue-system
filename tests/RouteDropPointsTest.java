import qpal.model.RouteDropPoints;

public class RouteDropPointsTest {
    public static void main(String[] args) throws Exception {
        if (!RouteDropPoints.route("Dasmariñas")
                .equals(
                        java.util.List.of(
                                "PITX", "SM City Bacoor", "Imus BDO Junction", "Dasmariñas")))
            throw new AssertionError();
        if (RouteDropPoints.options("Naic").size() != 3)
            throw new AssertionError("Duplicate options");
        if (!RouteDropPoints.options("GMA").contains("SM City Molino"))
            throw new AssertionError("Alias");
        if (RouteDropPoints.valid("PITX", "Naic", "Imus")) throw new AssertionError("Invalid stop");
        if (RouteDropPoints.valid("Elsewhere", "Naic", "Tanza")) throw new AssertionError("Origin");
        if (!RouteDropPoints.options("Unknown").equals(java.util.List.of("Unknown")))
            throw new AssertionError();
        if (!RouteDropPoints.options("Santa Cruz, Laguna").contains("Los Baños Junction"))
            throw new AssertionError("Province alias");
        if (!RouteDropPoints.options("Nasugbu via Kaybiang").contains("Kaybiang Tunnel Area"))
            throw new AssertionError("Nasugbu alias");
        System.out.println(
                "PASS: mappings, duplicate stops, aliases, invalid selections and fallback");
        if (!RouteDropPoints.options("Nasugbu via Aguinaldo Highway").contains("Alfonso Crossing"))
            throw new AssertionError("Aguinaldo route");
        if (RouteDropPoints.options("Nasugbu via Aguinaldo Highway")
                .contains("Kaybiang Tunnel Area"))
            throw new AssertionError("Nasugbu routes must remain distinct");
        String kiosk =
                java.nio.file.Files.readString(
                        java.nio.file.Path.of("src/qpal/view/Commuter/TripDetailsPanel.java"));
        String list =
                kiosk.substring(
                        kiosk.indexOf("destinationbox = new JComboBox<>"),
                        kiosk.indexOf("timebox = new JComboBox<>"));
        var names = java.util.regex.Pattern.compile("\"([^\"]*)\"").matcher(list);
        int count = 0;
        while (names.find()) {
            String destination = names.group(1);
            if (destination.isEmpty()) continue;
            if (RouteDropPoints.route(destination).size() != 4)
                throw new AssertionError("Missing mapping: " + destination);
            count++;
        }
        if (count != 29) throw new AssertionError("Expected 29 kiosk destinations");
        var full = new java.math.BigDecimal("50");
        if (!RouteDropPoints.fare("Tagaytay", "SM City Dasmariñas", full)
                .equals(new java.math.BigDecimal("30"))) throw new AssertionError("First stop");
        if (!RouteDropPoints.fare("Tagaytay", "Silang Premier Plaza", full)
                .equals(new java.math.BigDecimal("40"))) throw new AssertionError("Second stop");
        if (!RouteDropPoints.fare("Tagaytay", "Tagaytay", full)
                .equals(new java.math.BigDecimal("50"))) throw new AssertionError("Final stop");
        if (!RouteDropPoints.fare("Naic", "Naic", full).equals(new java.math.BigDecimal("50")))
            throw new AssertionError("Repeated final stop");
        if (!RouteDropPoints.fare(
                        "Tagaytay", "SM City Dasmariñas", new java.math.BigDecimal("50.01"))
                .equals(new java.math.BigDecimal("30"))) throw new AssertionError("Rounding");
        try {
            RouteDropPoints.fare("Tagaytay", "PITX", full);
            throw new AssertionError("Origin selectable");
        } catch (IllegalArgumentException expected) {
        }
        System.out.println("PASS: fare percentages, final stop, rounding and origin rejection");
    }
}
