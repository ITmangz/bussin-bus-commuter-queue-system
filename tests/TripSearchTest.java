import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import qpal.model.BookingData.TripOption;
import qpal.model.TripSearch;

public class TripSearchTest {
    private static final LocalDate DATE = LocalDate.of(2026, 10, 1);
    private static TripOption trip(int id, String origin, String destination, LocalDate date, String time) {
        return new TripOption(id, "Bus", origin, destination, date, LocalTime.parse(time), BigDecimal.TEN, 40, 40);
    }
    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
    public static void main(String[] args) {
        TripSearch search = new TripSearch("Dasmariñas", DATE, LocalTime.of(9, 0));
        List<TripOption> alternatives = List.of(
                trip(1,"PITX","Dasmarinas",DATE,"08:30"),
                trip(2,"PITX","Dasmariñas",DATE,"09:30"),
                trip(3,"PITX","Dasmariñas",DATE,"11:00"),
                trip(4,"PITX","Tagaytay",DATE,"09:00"),
                trip(5,"PITX","Dasmariñas",DATE.plusDays(1),"09:00"),
                trip(6,"Alabang","Dasmariñas",DATE,"09:00"));
        check(search.find(alternatives).stream().map(TripOption::id).toList().equals(List.of(2,1,3)),
                "Nearby schedules sorted by distance; ties prefer later; same route/date only");
        List<TripOption> withExact = new ArrayList<>(alternatives);
        withExact.add(trip(7,"pitx","Dasmariñas",DATE,"09:00"));
        withExact.add(trip(8,"PITX","Dasmariñas",DATE,"09:00"));
        check(search.find(withExact).stream().map(TripOption::id).toList().equals(List.of(7,8)), "All exact-time buses preferred");
        check(new TripSearch("Lancaster City", DATE, LocalTime.NOON).find(alternatives).isEmpty(), "No unrelated destination fallback");
        check(search.find(List.of()).isEmpty(), "No schedules");
        TripSearch midnight = new TripSearch("Dasmariñas",DATE,LocalTime.MIDNIGHT);
        check(midnight.find(List.of(trip(9,"PITX","Dasmariñas",DATE,"23:30"),
                trip(10,"PITX","Dasmariñas",DATE,"00:30"))).get(0).id()==10, "Time distance must not wrap to another day");
        check(new TripSearch("General Mariano Alvarez (GMA)",DATE,LocalTime.NOON)
                .find(List.of(trip(11,"PITX","GMA",DATE,"12:00"))).size()==1, "GMA alias matches");
        System.out.println("PASS: exact matching, nearest ordering, date/route boundaries, midnight and empty results");
    }
}
