package qpal.model;

import java.util.*;

public final class RouteDropPoints {
    private RouteDropPoints() {}

    private static final Map<String, List<String>> ROUTES = new LinkedHashMap<>();

    static {
        // Destination | drop-off point 1 | drop-off point 2.
        String[] rows = {
            "Alfonso, Cavite|Silang Town Proper|Tagaytay Rotonda",
            "Amadeo, Cavite|Robinsons Place Dasmariñas|Silang Bypass Road",
            "Antipolo City, Rizal|Ortigas Avenue Extension|Cainta Junction",
            "Balibago, Laguna|Southwoods Exit|Santa Rosa Exit",
            "Batangas City, Batangas|Santo Tomas Junction|Lipa City Exit",
            "Calauag, Quezon|Gumaca Town Proper|Lopez Public Market",
            "Cavite City, Cavite|Zapote Junction|Kawit Centennial Road",
            "Dasmariñas, Cavite|SM City Bacoor|Imus BDO Junction",
            "General Mariano Alvarez (GMA), Cavite|SM City Molino|Paliparan Road",
            "Guinayangan, Quezon|Atimonan Town Proper|Plaridel Junction",
            "Lancaster City, Cavite|St. Dominic Junction|Advincula Avenue",
            "Lipa City, Batangas|Turbina Junction|Malvar Exit",
            "Lucena City, Quezon|Candelaria Town Proper|Sariaya Public Market",
            "Maragondon, Cavite|Tanza Town Proper|Naic Crossing",
            "Mendez, Cavite|Olivarez Plaza|Tagaytay-Mendez Junction",
            "Molino, Cavite|Bacoor Boulevard|Molino Boulevard",
            "Naic, Cavite|Tejero Junction|Tanza Public Market",
            "Nasugbu via Aguinaldo Highway, Batangas|Tagaytay City Market|Alfonso Crossing",
            "Nasugbu via Kaybiang, Batangas|Ternate Town Proper|Kaybiang Tunnel Area",
            "Paliparan, Cavite|Molino III|Salawag Junction",
            "San Andres, Quezon|Catanauan Town Proper|San Narciso Junction",
            "San Juan, Batangas|Tiaong Town Proper|Rosario Public Market",
            "Silang, Cavite|District Imus|Dasmariñas Pala-Pala",
            "Santa Cruz, Laguna|Calamba Crossing|Los Baños Junction",
            "Tagaytay City, Cavite|SM City Dasmariñas|Silang Premier Plaza",
            "Tagkawayan, Quezon|Lopez Junction|Calauag Town Proper",
            "Tanza, Cavite|Kawit Junction|General Trias Tejero",
            "Ternate, Cavite|Naic Town Proper|Maragondon Junction",
            "Trece Martires City, Cavite|Imus Anabu|General Trias Manggahan"
        };
        for (String row : rows) {
            String[] p = row.split("\\|");
            ROUTES.put(key(p[0]), List.of("PITX", p[1], p[2], p[0]));
        }
    }

    private static String key(String s) {
        String k =
                java.text.Normalizer.normalize(s.trim(), java.text.Normalizer.Form.NFD)
                        .replaceAll("\\p{M}", "")
                        .toLowerCase(Locale.ROOT);
        k = k.split(",", 2)[0].trim();
        if (k.equals("tagaytay")) k = "tagaytay city";
        if (k.equals("trece martires")) k = "trece martires city";
        if (k.equals("tanauan")) k = "tanauan city";
        if (k.equals("gma")) return "general mariano alvarez (gma)";
        if (k.equals("santa cruz")) return "sta. cruz";
        return k;
    }

    public static List<String> route(String destination) {
        List<String> mapped = ROUTES.get(key(destination));
        return mapped == null
                ? List.of("PITX", destination)
                : List.of("PITX", mapped.get(1), mapped.get(2), destination);
    }

    public static List<String> options(String destination) {
        var route = route(destination);
        return List.copyOf(new LinkedHashSet<>(route.subList(1, route.size())));
    }

    public static boolean valid(String origin, String destination, String stop) {
        return stop != null
                && (stop.equals(destination)
                        || (origin.equalsIgnoreCase("PITX")
                                && options(destination).contains(stop)));
    }

    /** Approved fare policy: first stop 60%, second stop 80%, final destination 100%. */
    public static java.math.BigDecimal fare(
            String destination, String stop, java.math.BigDecimal fullFare) {
        List<String> stops = options(destination);
        if (stop == null || !stops.contains(stop) || fullFare == null || fullFare.signum() <= 0)
            throw new IllegalArgumentException("A valid drop-off and route fare are required.");
        int percent = key(stop).equals(key(destination)) ? 100 : stops.indexOf(stop) == 0 ? 60 : 80;
        return fullFare.multiply(java.math.BigDecimal.valueOf(percent))
                .divide(java.math.BigDecimal.valueOf(100), 0, java.math.RoundingMode.HALF_UP);
    }
}
