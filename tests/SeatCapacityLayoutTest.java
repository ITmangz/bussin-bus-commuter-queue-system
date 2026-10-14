package qpal.view.Admin;

import javax.swing.*;
import qpal.model.SeatLayout;

public class SeatCapacityLayoutTest {
    static void check(boolean ok, String message) { if (!ok) throw new AssertionError(message); }
    public static void main(String[] args) throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            JComboBox<Integer> combo = ManagementComboBoxes.seatCapacity(24);
            for (SeatLayout layout : SeatLayout.values()) {
                ManagementComboBoxes.updateSeatCapacity(combo, layout, 40);
                check(Integer.valueOf(40).equals(combo.getSelectedItem()), "Compatible selection retained");
                for (int i = 0; i < combo.getItemCount(); i++)
                    check(layout.completeRows(combo.getItemAt(i)), "Every choice makes complete rows");
                check(combo.getItemCount() == (layout == SeatLayout.TWO_TWO ? 6 : 5), "Expected capacity choices");
            }
            ManagementComboBoxes.updateSeatCapacity(combo, SeatLayout.TWO_THREE, 24);
            check(combo.getSelectedIndex() == -1, "Invalid capacity requires reselection");
            ManagementComboBoxes.updateSeatCapacity(combo, SeatLayout.TWO_TWO, 48);
            check(Integer.valueOf(48).equals(combo.getSelectedItem()), "Legacy complete capacity retained");
            ManagementComboBoxes.updateSeatCapacity(combo, SeatLayout.THREE_TWO, 48);
            check(combo.getSelectedIndex() == -1, "Invalid legacy capacity removed");
        });
        System.out.println("PASS: capacity choices fit all layouts, valid selection retention, invalid selection clearing, legacy capacities.");
    }
}
