package qpal.view.Commuter;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class KioskStepsPanel {

    public static JPanel create(int activeStep) {

        String[] steps = {"Trip Details","Available Trips","Passengers","Select Seats","Review Trip","Payment"};
        JPanel panel = new JPanel(new GridLayout(1,6,1,0));
        panel.setPreferredSize(new Dimension(1000,75));
        panel.setBackground(new Color(220,220,220));

        for(int i = 0; i < steps.length; i++) {

            JPanel step = new JPanel(new GridLayout(2,1));
            step.setBackground(new Color(240,243,245));
            step.setBorder(new EmptyBorder(12,14,12,8));
            JLabel lblNumber = new JLabel("STEP 0" + (i + 1));
            lblNumber.setFont(new Font("Segoe UI",Font.PLAIN,11));
            lblNumber.setForeground(Color.GRAY);
            JLabel lblTitle = new JLabel(steps[i]);
            lblTitle.setFont(new Font("Segoe UI",Font.BOLD,13));

            if(i == activeStep) {

                step.setBackground(new Color(255,235,240));
                step.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0,0,3,0,new Color(225,0,45)),
                        new EmptyBorder(12,14,9,8)));
                lblTitle.setForeground(new Color(225,0,45));
            }

            step.add(lblNumber);
            step.add(lblTitle);
            panel.add(step);
        }

        return panel;
    }
}
