package qpal.view.Admin;

import java.awt.*;
import javax.swing.*;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.plaf.basic.BasicComboPopup;
import javax.swing.plaf.basic.ComboPopup;
import com.toedter.calendar.JDateChooser;

/** Consistent card outlines and input popups for admin forms. */
final class AdminFormStyle {
    private static final Color OUTLINE = new Color(232, 236, 242);
    private static final Color INK = new Color(20, 23, 28);
    private static final Color ROSE = new Color(255, 235, 240);

    private AdminFormStyle() {}

    static JPanel frame(JPanel form) {
        styleInputs(form);
        AdminCard card = new AdminCard(2);
        card.setLayout(new BorderLayout());
        card.add(form);
        JPanel surround = new JPanel(new BorderLayout());
        surround.setBackground(new Color(245, 245, 245));
        surround.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        surround.add(card);
        return surround;
    }

    static void styleInputs(Component component) {
        if (component instanceof JDateChooser chooser) {
            styleCalendar(chooser);
            return;
        }
        if (component instanceof JComboBox<?> combo) {
            styleCombo(combo);
            return;
        }
        if (component instanceof Container container)
            for (Component child : container.getComponents()) styleInputs(child);
    }

    private static <T> void styleCombo(JComboBox<T> combo) {
        if (Boolean.TRUE.equals(combo.getClientProperty("adminStyled"))) return;
        combo.putClientProperty("adminStyled", true);
        ListCellRenderer<? super T> original = combo.getRenderer();
        combo.setUI(new BasicComboBoxUI() {
            @Override protected JButton createArrowButton() {
                JButton arrow = new JButton("\u25be");
                arrow.setBackground(Color.WHITE);
                arrow.setForeground(INK);
                arrow.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
                arrow.setFocusable(false);
                return arrow;
            }
            @Override protected ComboPopup createPopup() {
                return new BasicComboPopup(comboBox) {
                    @Override protected void configureScroller() {
                        super.configureScroller();
                        scroller.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
                        scroller.setWheelScrollingEnabled(true);
                        AdminCard.styleScrollBar(scroller, Color.WHITE);
                    }
                };
            }
        });
        combo.setMaximumRowCount(8);
        combo.setBackground(Color.WHITE);
        combo.setForeground(INK);
        combo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        combo.setBorder(BorderFactory.createLineBorder(OUTLINE));
        combo.setRenderer((list, value, index, selected, focused) -> {
            Component cell = original.getListCellRendererComponent(list, value, index, selected, focused);
            cell.setBackground(selected && index >= 0 ? ROSE : Color.WHITE);
            cell.setForeground(INK);
            if (cell instanceof JComponent input)
                input.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));
            return cell;
        });
    }

    private static void styleCalendar(JDateChooser chooser) {
        chooser.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        chooser.setBackground(Color.WHITE);
        chooser.setBorder(BorderFactory.createLineBorder(OUTLINE));
        JComponent editor = chooser.getDateEditor().getUiComponent();
        editor.setBackground(Color.WHITE);
        editor.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));
        JButton button = chooser.getCalendarButton();
        button.setBackground(ROSE);
        button.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));
        button.setPreferredSize(new Dimension(38, 36));
        button.setFocusPainted(false);
        var calendar = chooser.getJCalendar();
        calendar.setPreferredSize(new Dimension(310, 270));
        calendar.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        calendar.setBackground(Color.WHITE);
        calendar.setForeground(INK);
        calendar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(OUTLINE), BorderFactory.createEmptyBorder(10, 10, 10, 10)));
        calendar.setWeekOfYearVisible(false);
        calendar.setDecorationBackgroundColor(ROSE);
        calendar.setSundayForeground(new Color(225, 29, 72));
        calendar.setWeekdayForeground(INK);
        calendar.setDecorationBordersVisible(false);
        calendar.getDayChooser().setDayBordersVisible(false);
        calendar.setTodayButtonVisible(true);
        styleInputs(calendar);
    }
}
