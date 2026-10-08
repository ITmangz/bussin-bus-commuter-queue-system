package qpal.model;
import java.util.*;
public final class RouteDropPoints {
    private RouteDropPoints() {}
    private static final Map<String,List<String>> ROUTES=new LinkedHashMap<>();
    static {
        String[] rows={
            "Bacoor|Imus|Dasmariñas", "Molino|Salawag|General Mariano Alvarez (GMA)",
            "General Trias|Manggahan|Trece Martires", "Tagaytay|Mendez|Alfonso",
            "Silang|Tagaytay|Amadeo", "Tagaytay|Alfonso|Mendez", "Dasmariñas|Silang|Silang",
            "Silang|Mendez|Tagaytay", "Tanza|Naic|Maragondon", "Tanza|Naic|Naic",
            "Naic|Maragondon|Ternate", "Kawit|Noveleta|Cavite City", "Imus|General Trias|Lancaster City",
            "Bacoor|Molino Boulevard|Molino", "Dasmariñas|Salawag|Paliparan", "Kawit|Tanza Bayan|Tanza",
            "Santa Rosa|Balibago|Balibago", "Calamba|Los Baños|Sta. Cruz", "Lipa|Tanauan|Batangas City",
            "Tanauan|Malvar|Lipa City", "Lipa|Rosario|San Juan", "Tagaytay|Alfonso|Nasugbu via Aguinaldo Highway",
            "Ternate|Maragondon|Nasugbu via Kaybiang", "Cainta|Masinag|Antipolo City", "Sariaya|Tayabas|Lucena City",
            "Lucena|Gumaca|Calauag", "Calauag|Lopez|Guinayangan", "Lucena|Atimonan|San Andres", "Gumaca|Calauag|Tagkawayan"};
        for(String row:rows) { String[] p=row.split("\\|"); ROUTES.put(key(p[2]),List.of("PITX",p[0],p[1],p[2])); }
    }
    private static String key(String s) {
        String k=java.text.Normalizer.normalize(s.trim(),java.text.Normalizer.Form.NFD).replaceAll("\\p{M}","").toLowerCase(Locale.ROOT);
        if(k.equals("gma")) return "general mariano alvarez (gma)";
        if(k.equals("santa cruz")) return "sta. cruz";
        return k;
    }
    public static List<String> route(String destination) { return ROUTES.getOrDefault(key(destination),List.of("PITX",destination)); }
    public static List<String> options(String destination) {
        var route=route(destination);
        return List.copyOf(new LinkedHashSet<>(route.subList(1,route.size())));
    }
    public static boolean valid(String origin,String destination,String stop) {
        return stop!=null && (stop.equals(destination) || (origin.equalsIgnoreCase("PITX") && options(destination).contains(stop)));
    }

    /** Approved fare policy: first stop 60%, second stop 80%, final destination 100%. */
    public static java.math.BigDecimal fare(String destination, String stop, java.math.BigDecimal fullFare) {
        List<String> stops = options(destination);
        if (stop == null || !stops.contains(stop) || fullFare == null || fullFare.signum() <= 0)
            throw new IllegalArgumentException("A valid drop-off and route fare are required.");
        int percent = key(stop).equals(key(destination)) ? 100 : stops.indexOf(stop) == 0 ? 60 : 80;
        return fullFare.multiply(java.math.BigDecimal.valueOf(percent))
                .divide(java.math.BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP);
    }
}
