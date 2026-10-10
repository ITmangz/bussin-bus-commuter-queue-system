package qpal.model;

import java.text.Normalizer;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import qpal.model.BookingData.TripOption;

public record TripSearch(String destination, LocalDate date, LocalTime time) {

    public List<TripOption> find(List<TripOption> available) {

        if (date == null || time == null || destination == null || destination.isBlank())
            return List.of();

        List<TripOption> matching =
                available.stream()
                        .filter(
                                t ->
                                        normalize(t.origin()).equals("pitx")
                                                && normalize(t.destination())
                                                        .equals(normalize(destination))
                                                && t.date().equals(date))
                        .sorted(
                                Comparator.comparingLong(
                                                (TripOption t) ->
                                                        Math.abs(
                                                                t.time().toSecondOfDay()
                                                                        - time.toSecondOfDay()))
                                        .thenComparing(t -> t.time().isBefore(time))
                                        .thenComparing(TripOption::time)
                                        .thenComparingInt(TripOption::id))
                        .toList();

        List<TripOption> exact = matching.stream().filter(t -> t.time().equals(time)).toList();

        return exact.isEmpty() ? matching : exact;
    }

    private static String normalize(String value) {

        String normalized =
                Normalizer.normalize(value.trim(), Normalizer.Form.NFD)
                        .replaceAll("\\p{M}", "")
                        .replaceAll("\\s+", " ")
                        .toLowerCase(Locale.ROOT);

        if (normalized.equals("gma")) return "general mariano alvarez (gma)";

        return normalized;
    }
}
