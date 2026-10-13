import java.util.*;
import java.util.regex.*;

/** Patterns inferred only from the user's examples, not official school validation rules. */
public final class StudentIdFormats {
    public record Match(String school, String number) {
        @Override
        public String toString() {
            return school + " format: " + number;
        }
    }

    private record Rule(String school, Pattern pattern) {}

    private static final List<Rule> RULES =
            List.of(
                    rule("PUP", "20\\d{2}-\\d{5}-[A-Z]{2}-\\d"),
                    rule("NU MOA", "20\\d{2}-\\d{7}"),
                    rule("NU APC", "20\\d{2}-\\d{6}"),
                    rule("FEU TECH", "20\\d{7}"));

    private static Rule rule(String school, String regex) {
        return new Rule(school, Pattern.compile("(?<![A-Z0-9-])(" + regex + ")(?![A-Z0-9-])"));
    }

    public static List<Match> find(String text) {
        String normalized =
                text.toUpperCase(Locale.ROOT)
                        .replaceAll("[\u2010-\u2015\u2212]", "-")
                        .replaceAll("[ \\t]*-[ \\t]*", "-")
                        .replaceAll("(?<=\\d)[ \\t]+(?=\\d)", "");
        Set<Match> matches = new LinkedHashSet<>();
        for (Rule rule : RULES) {
            Matcher matcher = rule.pattern.matcher(normalized);
            while (matcher.find()) matches.add(new Match(rule.school, matcher.group(1)));
        }
        return List.copyOf(matches);
    }
}
