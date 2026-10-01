package qpal.components;

import java.awt.*;
import javax.swing.*;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.plaf.basic.BasicComboPopup;
import javax.swing.plaf.basic.ComboPopup;
import com.toedter.calendar.JDateChooser;

/** Shared rectangular inputs and calendar popups for admin and kiosk forms. */
public final class FormInputStyle {
    private static final Color OUTLINE = new Color(232, 236, 242);
    private static final Color INK = new Color(20, 23, 28);
    private static final Color ROSE = new Color(255, 235, 240);

    private FormInputStyle() {}

    public static <T> void styleCombo(JComboBox<T> combo) {
        if (Boolean.TRUE.equals(combo.getClientProperty("adminStyled"))) return;
        combo.putClientProperty("adminStyled", true);
        ListCellRenderer<? super T> original = combo.getRenderer();
        combo.setUI(new BasicComboBoxUI() {
            @Override public void paintCurrentValueBackground(Graphics g, Rectangle bounds, boolean focused) {
                g.setColor(Color.WHITE);
                g.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
            }
            @Override protected JButton createArrowButton() {
                JButton arrow = new JButton(new ChevronIcon());
                arrow.setContentAreaFilled(false);
                arrow.setBackground(Color.WHITE);
                arrow.setForeground(INK);
                arrow.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
                arrow.setFocusable(false);
                return arrow;
            }
            @Override protected ComboPopup createPopup() {
                return new BasicComboPopup(comboBox) {
                    { setBorder(BorderFactory.createLineBorder(OUTLINE)); }
                    @Override protected void configureScroller() {
                        super.configureScroller();
                        scroller.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
                        scroller.setWheelScrollingEnabled(true);
                        styleScrollBar(scroller);
                    }
                };
            }
        });
        combo.setMaximumRowCount(8);
        combo.setBackground(Color.WHITE);
        combo.setForeground(INK);
        combo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        combo.setBorder(new InputBorder());
        combo.setRenderer((list, value, index, selected, focused) -> {
            Component cell = original.getListCellRendererComponent(list, value, index, selected, focused);
            cell.setBackground(selected && index >= 0 ? ROSE : Color.WHITE);
            cell.setForeground(INK);
            if (cell instanceof JComponent input)
                input.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));
            if (cell instanceof JComponent input) input.setOpaque(true);
            return cell;
        });
    }

    public static void styleCalendar(JDateChooser chooser) {
        chooser.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        chooser.setBackground(Color.WHITE);
        chooser.setBorder(new InputBorder());
        JComponent editor = chooser.getDateEditor().getUiComponent();
        editor.setBackground(Color.WHITE);
        editor.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));
        JButton button = chooser.getCalendarButton();
        button.setIcon(new ChevronIcon());
        button.setContentAreaFilled(false);
        button.setBackground(Color.WHITE);
        button.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));
        button.setPreferredSize(new Dimension(38, 36));
        button.setFocusPainted(false);
        var calendar = chooser.getJCalendar();
        calendar.setPreferredSize(new Dimension(392, 344));
        calendar.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        calendar.setBackground(Color.WHITE);
        calendar.setForeground(INK);
        calendar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(OUTLINE), BorderFactory.createEmptyBorder(16, 16, 16, 16)));
        calendar.setWeekOfYearVisible(false);
        calendar.setDecorationBackgroundColor(ROSE);
        calendar.setSundayForeground(new Color(225, 29, 72));
        calendar.setWeekdayForeground(INK);
        calendar.setDecorationBordersVisible(false);
        calendar.getDayChooser().setDayBordersVisible(false);
        calendar.setTodayButtonVisible(true);
        flattenCalendar(calendar);
        calendar.setDecorationBackgroundColor(ROSE);
        calendar.setSundayForeground(new Color(225, 29, 72));
        calendar.getMonthChooser().setPreferredSize(new Dimension(205, 36));
        calendar.getYearChooser().setPreferredSize(new Dimension(85, 36));
        if (calendar.getLayout() instanceof BorderLayout layout) layout.setVgap(12);
        if (calendar.getDayChooser().getDayPanel().getLayout() instanceof GridLayout layout) {
            layout.setHgap(4);
            layout.setVgap(4);
        }
        Runnable tintDays = () -> {
            for (Component child : calendar.getDayChooser().getDayPanel().getComponents()) {
                if (child instanceof JButton day) {
                    try {
                        boolean selected = Integer.parseInt(day.getText()) == calendar.getDayChooser().getDay();
                        boolean hovered = day.isEnabled() && day.getModel().isRollover();
                        day.setBackground(hovered ? new Color(255, 215, 226)
                                : selected ? ROSE : Color.WHITE);
                    } catch (NumberFormatException ignored) { }
                }
            }
        };
        for (Component child : calendar.getDayChooser().getDayPanel().getComponents()) {
            if (child instanceof JButton day) {
                day.setRolloverEnabled(true);
                day.setOpaque(true);
                if (!Boolean.TRUE.equals(day.getClientProperty("calendarHoverStyled"))) {
                    day.putClientProperty("calendarHoverStyled", true);
                    day.getModel().addChangeListener(e -> tintDays.run());
                }
            }
        }
        calendar.getDayChooser().addPropertyChangeListener(e -> { tintDays.run(); SwingUtilities.invokeLater(tintDays); });
        tintDays.run();
    }

    private static void flattenCalendar(Component component) {
        component.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        if (component instanceof JSpinner spinner) {
            spinner.setUI(new javax.swing.plaf.basic.BasicSpinnerUI());
            spinner.setBorder(BorderFactory.createLineBorder(OUTLINE));
        }
        if (component instanceof JComboBox<?> combo) { styleCombo(combo); return; }
        if (component instanceof JButton button) {
            button.setUI(new javax.swing.plaf.basic.BasicButtonUI());
            button.setBackground(Color.WHITE);
            button.setForeground(INK);
            button.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
        } else if (component instanceof JPanel panel) panel.setBackground(Color.WHITE);
        if (component instanceof Container container)
            for (Component child : container.getComponents()) flattenCalendar(child);
    }

    private static void styleScrollBar(JScrollPane scroll) {
        scroll.setBorder(BorderFactory.createEmptyBorder());
        JScrollBar bar = scroll.getVerticalScrollBar();
        bar.setPreferredSize(new Dimension(12, 0));
        bar.setUI(new javax.swing.plaf.basic.BasicScrollBarUI() {
            @Override protected void configureScrollBarColors() {
                trackColor = Color.WHITE;
                thumbColor = new Color(203, 210, 221);
            }
            private JButton empty() {
                JButton button = new JButton();
                button.setPreferredSize(new Dimension(0, 0));
                return button;
            }
            @Override protected JButton createDecreaseButton(int direction) { return empty(); }
            @Override protected JButton createIncreaseButton(int direction) { return empty(); }
            @Override protected void paintThumb(Graphics graphics, JComponent c, Rectangle r) {
                if (r.isEmpty()) return;
                Graphics2D g = (Graphics2D) graphics.create();
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g.setColor(isDragging ? new Color(225, 29, 72) : thumbColor);
                g.fillRoundRect(r.x + 2, r.y + 2, r.width - 4, r.height - 4, 8, 8);
                g.dispose();
            }
        });
        bar.setUnitIncrement(32);
    }

    private static final class InputBorder extends javax.swing.border.AbstractBorder {
        @Override public Insets getBorderInsets(Component c) { return new Insets(1, 1, 1, 1); }
        @Override public Insets getBorderInsets(Component c, Insets i) { i.set(1, 1, 1, 1); return i; }
        @Override public void paintBorder(Component c, Graphics graphics, int x, int y, int w, int h) {
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(c.isFocusOwner() ? new Color(225, 29, 72) : OUTLINE);
            g.drawRect(x, y, w - 1, h - 1);
            g.dispose();
        }
    }

    private static final class ChevronIcon implements Icon {
        public int getIconWidth() { return 14; }
        public int getIconHeight() { return 10; }
        public void paintIcon(Component c, Graphics graphics, int x, int y) {
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(INK);
            g.setStroke(new BasicStroke(1.6f));
            g.drawLine(x + 2, y + 2, x + 7, y + 7);
            g.drawLine(x + 7, y + 7, x + 12, y + 2);
            g.dispose();
        }
    }
}
