package qpal.model;

/** Physical row geometry; stored seat numbers remain one-based. */
public enum SeatLayout {
    THREE_TWO("3+2", 3, 2), TWO_THREE("2+3", 2, 3), TWO_TWO("2+2", 2, 2);

    private final String code;
    private final int left, right;

    SeatLayout(String code, int left, int right) {
        this.code = code;
        this.left = left;
        this.right = right;
    }

    public String code() { return code; }
    public int left() { return left; }
    public int perRow() { return left + right; }
    public boolean completeRows(int capacity) { return capacity > 0 && capacity % perRow() == 0; }
    public boolean prioritySeat(int seat) { return seat > 0 && seat <= perRow(); }
    public static boolean eligible(String type) {
        return "Senior".equals(type) || "PWD".equals(type);
    }
    public String label(int seat) {
        if (seat < 1) throw new IllegalArgumentException("Invalid seat number");
        return ((seat - 1) / perRow() + 1) + "" + (char) ('A' + (seat - 1) % perRow());
    }
    public int number(String label) {
        if (label == null || !label.matches("[1-9][0-9]*[A-" + (char) ('A' + perRow() - 1) + "]"))
            throw new IllegalArgumentException("Invalid seat label");
        return Math.addExact(Math.multiplyExact(Integer.parseInt(label.substring(0, label.length() - 1)) - 1,
                perRow()), label.charAt(label.length() - 1) - 'A' + 1);
    }
    public static SeatLayout fromCode(String code) {
        for (SeatLayout layout : values()) if (layout.code.equals(code)) return layout;
        throw new IllegalArgumentException("Unknown seat layout: " + code);
    }
    @Override public String toString() { return left + " left + " + right + " right"; }
}
