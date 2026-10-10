package qpal.view.Admin;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import qpal.dao.AccountDao;
import qpal.model.Account;

public class EditProfilePanel extends JPanel {

    private final java.util.function.Supplier<Account> accountSupplier;
    private final Runnable profileSaved;
    private JDialog dialog;
    private AccountDao accountDao = new AccountDao();

    private JTextField txtName;
    private JTextField txtEmail;
    private JTextField txtAssignment;
    private JTextField txtSessionDetails;
    private JPasswordField txtPassword;

    private JLabel lblPreviewName;
    private JLabel lblPreviewRole;
    private JLabel lblPreviewDetails;
    private JLabel lblPhoto;

    private JButton btnSave;
    private JButton btnCancel;
    private JButton btnPhoto;
    private JButton btnPassword;

    private String photoPath = "";
    private String newPassword = "";
    private String currentPassword = "";

    public EditProfilePanel(AdminDashboard parent) {
        this(
                () -> parent == null ? null : parent.getCurrentAccount(),
                () -> {
                    if (parent != null) parent.showPage("dashboard");
                });
    }

    public EditProfilePanel(
            java.util.function.Supplier<Account> accountSupplier, Runnable profileSaved) {

        this.accountSupplier = accountSupplier;
        this.profileSaved = profileSaved;

        setLayout(new GridLayout(1, 2));
        setBackground(Color.WHITE);
        setBorder(new EmptyBorder(24, 24, 24, 24));

        add(createForm());
        add(createPreview());
        addPreviewListeners();
    }

    public void showDialog(Component owner) {

        loadProfile();
        setPreferredSize(new Dimension(740, 460));

        dialog =
                new JDialog(
                        SwingUtilities.getWindowAncestor(owner),
                        "Edit Profile",
                        Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setUndecorated(true);
        dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        dialog.setContentPane(AdminFormStyle.frame(this));
        dialog.pack();
        dialog.setResizable(false);
        dialog.setLocationRelativeTo(null);
        dialog.setVisible(true);
    }

    private JPanel createForm() {

        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(new EmptyBorder(0, 0, 0, 24));

        JLabel lblTitle = new JLabel("Edit your Profile");
        lblTitle.setFont(new Font("SansSerif", Font.BOLD, 22));
        lblTitle.setForeground(new Color(240, 0, 55));
        lblTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(lblTitle);
        panel.add(Box.createVerticalStrut(7));

        JLabel lblSubtitle = new JLabel("Update your photo and personal details.");
        lblSubtitle.setFont(new Font("SansSerif", Font.PLAIN, 12));
        lblSubtitle.setForeground(new Color(100, 100, 100));
        lblSubtitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(lblSubtitle);
        panel.add(Box.createVerticalStrut(20));

        txtName = createTextField();

        txtName.setDocument(
                new javax.swing.text.PlainDocument() {

                    @Override
                    public void insertString(int offs, String str, javax.swing.text.AttributeSet a)
                            throws javax.swing.text.BadLocationException {

                        if (str == null) {
                            return;
                        }

                        if (getLength() + str.length() <= 100) {
                            super.insertString(offs, str, a);

                        } else {
                            txtName.setText("");
                            Toolkit.getDefaultToolkit().beep();
                            qpal.components.AppDialogs.showMessageDialog(
                                    null,
                                    "Name must not exceed 100 characters.",
                                    "Warning!",
                                    JOptionPane.WARNING_MESSAGE);
                        }
                    }
                });
        addField(panel, "Name", txtName);
        panel.add(Box.createVerticalStrut(18));

        txtEmail = createTextField();

        txtEmail.setDocument(
                new javax.swing.text.PlainDocument() {

                    @Override
                    public void insertString(int offs, String str, javax.swing.text.AttributeSet a)
                            throws javax.swing.text.BadLocationException {

                        if (str == null) {
                            return;
                        }

                        if (getLength() + str.length() <= 100) {
                            super.insertString(offs, str, a);

                        } else {
                            txtEmail.setText("");
                            Toolkit.getDefaultToolkit().beep();
                            qpal.components.AppDialogs.showMessageDialog(
                                    null,
                                    "Email must not exceed 100 characters.",
                                    "Warning!",
                                    JOptionPane.WARNING_MESSAGE);
                        }
                    }
                });
        addField(panel, "Email", txtEmail);
        panel.add(Box.createVerticalStrut(22));

        JPanel details = new JPanel(new GridLayout(1, 2, 24, 0));
        details.setOpaque(false);
        details.setAlignmentX(Component.LEFT_ALIGNMENT);
        details.setMaximumSize(new Dimension(Integer.MAX_VALUE, 66));

        txtAssignment = createTextField();
        txtSessionDetails = createTextField();
        for (JTextField field : new JTextField[] {txtAssignment, txtSessionDetails}) {
            field.setEditable(false);
            field.setFont(new Font("SansSerif", Font.PLAIN, 12));
            field.setBackground(new Color(245, 246, 248));
        }
        boolean employee = isEmployee();
        JPanel assignment = new JPanel();
        assignment.setOpaque(false);
        assignment.setLayout(new BoxLayout(assignment, BoxLayout.Y_AXIS));
        addField(assignment, employee ? "Assigned Station" : "Role", txtAssignment);
        JPanel sessionDetails = new JPanel();
        sessionDetails.setOpaque(false);
        sessionDetails.setLayout(new BoxLayout(sessionDetails, BoxLayout.Y_AXIS));
        addField(
                sessionDetails, employee ? "Session Started" : "Account Status", txtSessionDetails);
        details.add(assignment);
        details.add(sessionDetails);
        panel.add(details);
        panel.add(Box.createVerticalStrut(14));

        JSeparator separator = new JSeparator();
        separator.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        separator.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(separator);
        panel.add(Box.createVerticalStrut(14));
        panel.add(Box.createVerticalGlue());

        JLabel lblPassword = createLabel("Password");
        panel.add(lblPassword);
        panel.add(Box.createVerticalStrut(8));

        JPanel password = new JPanel(new GridLayout(1, 2, 16, 0));
        password.setOpaque(false);
        password.setAlignmentX(Component.LEFT_ALIGNMENT);
        password.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        password.setPreferredSize(new Dimension(0, 36));
        password.setMinimumSize(new Dimension(0, 36));
        txtPassword = new JPasswordField("unchanged");
        txtPassword.setEditable(false);
        txtPassword.setBackground(new Color(220, 220, 220));
        txtPassword.setBorder(new EmptyBorder(8, 10, 8, 10));
        btnPassword = createButton("Change Password", new Color(240, 0, 55));
        btnPassword.addActionListener(e -> changePassword());
        password.add(txtPassword);
        password.add(btnPassword);
        panel.add(password);

        return panel;
    }

    private JPanel createPreview() {

        JPanel panel = new JPanel(new BorderLayout(0, 16));
        panel.setOpaque(false);
        panel.setBorder(
                BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 1, 0, 0, new Color(230, 230, 230)),
                        new EmptyBorder(16, 23, 0, 0)));

        JPanel preview = new JPanel();
        preview.setOpaque(false);
        preview.setLayout(new BoxLayout(preview, BoxLayout.Y_AXIS));

        JLabel lblPreview = new JLabel("Preview");
        lblPreview.setForeground(Color.GRAY);
        lblPreview.setFont(new Font("SansSerif", Font.PLAIN, 15));
        lblPreview.setAlignmentX(Component.CENTER_ALIGNMENT);
        preview.add(lblPreview);
        preview.add(Box.createVerticalStrut(16));

        lblPhoto = new JLabel();
        lblPhoto.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblPhoto.setHorizontalAlignment(SwingConstants.CENTER);
        lblPhoto.setPreferredSize(new Dimension(150, 150));
        lblPhoto.setMaximumSize(new Dimension(150, 150));
        preview.add(lblPhoto);
        preview.add(Box.createVerticalStrut(8));

        btnPhoto = new JButton("Edit Photo");
        btnPhoto.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnPhoto.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnPhoto.addActionListener(e -> choosePhoto());
        preview.add(btnPhoto);
        preview.add(Box.createVerticalStrut(14));

        lblPreviewName = new JLabel("Your name");
        lblPreviewName.setFont(new Font("SansSerif", Font.BOLD, 22));
        lblPreviewName.setAlignmentX(Component.CENTER_ALIGNMENT);
        preview.add(lblPreviewName);
        preview.add(Box.createVerticalStrut(8));

        lblPreviewRole = new JLabel("Admin");
        lblPreviewRole.setFont(new Font("SansSerif", Font.PLAIN, 14));
        lblPreviewRole.setForeground(Color.GRAY);
        lblPreviewRole.setAlignmentX(Component.CENTER_ALIGNMENT);
        preview.add(lblPreviewRole);
        preview.add(Box.createVerticalStrut(12));

        lblPreviewDetails = new JLabel(" ");
        lblPreviewDetails.setFont(new Font("SansSerif", Font.PLAIN, 14));
        lblPreviewDetails.setForeground(Color.GRAY);
        lblPreviewDetails.setAlignmentX(Component.CENTER_ALIGNMENT);
        preview.add(lblPreviewDetails);
        panel.add(preview, BorderLayout.CENTER);

        JPanel buttons = new JPanel();
        buttons.setLayout(new BoxLayout(buttons, BoxLayout.X_AXIS));
        buttons.setOpaque(false);
        buttons.setPreferredSize(new Dimension(276, 36));
        btnCancel = createButton("Cancel", new Color(220, 220, 220));
        btnCancel.setForeground(Color.DARK_GRAY);
        btnSave = createButton("Save Changes", new Color(240, 0, 55));
        for (JButton button : new JButton[] {btnCancel, btnSave}) {
            Dimension size = new Dimension(132, 36);
            button.setPreferredSize(size);
            button.setMinimumSize(size);
            button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        }
        btnCancel.addActionListener(
                e -> {
                    loadProfile();
                    if (dialog != null) {

                        dialog.dispose();
                    }
                });
        btnSave.addActionListener(e -> saveProfile());
        buttons.add(btnCancel);
        buttons.add(Box.createHorizontalStrut(12));
        buttons.add(btnSave);
        JPanel buttonRow = new JPanel(new GridBagLayout());
        buttonRow.setOpaque(false);
        buttonRow.setPreferredSize(new Dimension(0, 36));
        GridBagConstraints row = new GridBagConstraints();
        row.weightx = 1;
        row.fill = GridBagConstraints.HORIZONTAL;
        buttonRow.add(buttons, row);
        panel.add(buttonRow, BorderLayout.SOUTH);

        return panel;
    }

    public void loadProfile() {

        Account account = accountSupplier.get();
        if (account == null) {

            txtName.setText("");

        } else {

            txtName.setText(account.getName());
        }
        if (account == null) {

            txtEmail.setText("");

        } else {

            txtEmail.setText(account.getEmail());
        }
        if (account == null) {

            lblPreviewRole.setText("Sign in to edit your profile");

        } else {

            lblPreviewRole.setText(account.getRole());
        }
        newPassword = "";
        currentPassword = "";
        txtPassword.setText("unchanged");
        photoPath = "";
        txtAssignment.setText(account == null ? "—" : account.getRole());
        txtSessionDetails.setText(account == null ? "—" : account.getStatus());
        txtSessionDetails.setToolTipText(null);
        if (account != null)
            photoPath = account.getProfileImage() == null ? "" : account.getProfileImage();
        if (isEmployee()) {
            var session = qpal.dao.EmployeeStationDao.current();
            boolean assigned = session != null && session.accountId() == account.getID();
            txtAssignment.setText(assigned ? session.station().title() : "Not assigned");
            txtSessionDetails.setText(assigned ? "Loading…" : "Not started");
            if (assigned)
                qpal.util.UiTask.run(
                        () -> new qpal.dao.EmployeeSummaryDao().sessionStarted(session),
                        started -> {
                            if (qpal.dao.EmployeeStationDao.current() != session) return;
                            txtSessionDetails.setText(
                                    started == null
                                            ? "Unavailable"
                                            : started.format(
                                                    java.time.format.DateTimeFormatter.ofPattern(
                                                            "MMM d, h:mm a",
                                                            java.util.Locale.ENGLISH)));
                            txtSessionDetails.setToolTipText(
                                    started == null
                                            ? null
                                            : started.format(
                                                            java.time.format.DateTimeFormatter
                                                                    .ofPattern(
                                                                            "MMMM d, yyyy, h:mm a",
                                                                            java.util.Locale
                                                                                    .ENGLISH))
                                                    + " (Asia/Manila)");
                        },
                        ex -> {
                            if (qpal.dao.EmployeeStationDao.current() == session) {
                                txtSessionDetails.setText("Unavailable");
                                txtSessionDetails.setToolTipText(
                                        "Could not load the session start time. Reopen your profile"
                                            + " to retry.");
                            }
                        });
        }
        btnSave.setEnabled(account != null);
        updatePreview();
    }

    private boolean isEmployee() {
        Account account = accountSupplier.get();
        return account != null && "Employee".equalsIgnoreCase(account.getRole());
    }

    private void addPreviewListeners() {

        DocumentListener listener =
                new DocumentListener() {
                    public void insertUpdate(DocumentEvent e) {

                        updatePreview();
                    }

                    public void removeUpdate(DocumentEvent e) {

                        updatePreview();
                    }

                    public void changedUpdate(DocumentEvent e) {

                        updatePreview();
                    }
                };
        txtName.getDocument().addDocumentListener(listener);
    }

    private void updatePreview() {

        String name = txtName.getText().trim();
        if (name.isEmpty()) {

            lblPreviewName.setText("Your name");

        } else {

            lblPreviewName.setText(name);
        }
        lblPreviewDetails.setText(
                isEmployee() ? txtAssignment.getText() : txtSessionDetails.getText());

        BufferedImage image = new BufferedImage(150, 150, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setClip(new Ellipse2D.Double(1, 1, 148, 148));
        g.setColor(new Color(250, 235, 240));
        g.fillRect(0, 0, 150, 150);
        BufferedImage photo = null;
        if (!photoPath.isEmpty()) {

            try {

                photo = ImageIO.read(new File(photoPath));
            } catch (Exception e) {

                photo = null;
            }
        }
        if (photo != null) {

            double scale = Math.max(150.0 / photo.getWidth(), 150.0 / photo.getHeight());
            int width = (int) (photo.getWidth() * scale);
            int height = (int) (photo.getHeight() * scale);
            g.drawImage(photo, (150 - width) / 2, (150 - height) / 2, width, height, null);
        } else {
            g.setColor(new Color(225, 29, 72));
            g.setFont(new Font("SansSerif", Font.BOLD, 52));
            String initial = name.isEmpty() ? "?" : name.substring(0, 1).toUpperCase();
            g.drawString(initial, (150 - g.getFontMetrics().stringWidth(initial)) / 2, 94);
        }
        g.dispose();
        lblPhoto.setIcon(new ImageIcon(image));
    }

    private void choosePhoto() {

        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(
                new FileNameExtensionFilter("Profile images (PNG, JPG)", "png", "jpg", "jpeg"));
        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) {

            return;
        }

        try {
            if (ImageIO.read(chooser.getSelectedFile()) == null) {

                throw new Exception();
            }
            photoPath = chooser.getSelectedFile().getAbsolutePath();
            updatePreview();
        } catch (Exception e) {
            qpal.components.AppDialogs.showMessageDialog(
                    dialog,
                    "Please select a valid PNG or JPG image.",
                    "Warning",
                    JOptionPane.WARNING_MESSAGE);
        }
    }

    private void changePassword() {

        Account account = accountSupplier.get();
        if (account == null) {
            return;
        } else {
            new ChangePasswordPanel(
                    dialog,
                    account.getID(),
                    (current, password) -> {
                        currentPassword = current;
                        newPassword = password;
                        txtPassword.setText(password);
                        txtPassword.setToolTipText(
                                "New password will be applied when you save changes.");
                    });
        }
    }

    private void saveProfile() {

        Account account = accountSupplier.get();
        if (account == null) {

            return;
        }

        String name = txtName.getText().trim();
        String email = txtEmail.getText().trim();
        String password = newPassword;
        String verifiedPassword = currentPassword;
        String selectedPhoto = photoPath;
        if (selectedPhoto.length() > 255) {

            qpal.components.AppDialogs.showMessageDialog(
                    dialog,
                    "The image path is too long. Choose a file with a shorter path.",
                    "Warning",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (name.isEmpty()) {

            qpal.components.AppDialogs.showMessageDialog(
                    dialog, "Name needs an input.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!email.equals(account.getEmail()) && !email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {

            qpal.components.AppDialogs.showMessageDialog(
                    dialog, "Enter a valid email address.", "Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        setSaving(true);
        new SwingWorker<Boolean, Void>() {
            protected Boolean doInBackground() throws Exception {

                if (!email.equalsIgnoreCase(account.getEmail()) && accountDao.CheckEmail(email)) {

                    throw new IllegalArgumentException("Email already exists.");
                }
                return accountDao.updateProfile(
                        account.getID(), name, email, password, selectedPhoto, verifiedPassword);
            }

            protected void done() {

                setSaving(false);

                try {

                    boolean success = get();

                    if (success) {

                        account.setName(name);
                        account.setEmail(email);

                        if (!password.isEmpty()) {

                            account.setPassword(password);
                        }

                        account.setProfileImage(selectedPhoto);

                        newPassword = "";
                        currentPassword = "";
                        txtPassword.setText("unchanged");

                        qpal.components.AppDialogs.showMessageDialog(
                                dialog,
                                "Profile updated successfully.",
                                "Success",
                                JOptionPane.INFORMATION_MESSAGE);

                        if (dialog != null) {

                            dialog.dispose();
                        }

                        profileSaved.run();

                    } else {

                        qpal.components.AppDialogs.showMessageDialog(
                                dialog,
                                "Unable to save changes. Your current password may have changed;"
                                    + " enter it again and retry.",
                                "Error",
                                JOptionPane.ERROR_MESSAGE);
                    }

                } catch (Exception e) {

                    if (e.getCause() instanceof IllegalArgumentException) {

                        qpal.components.AppDialogs.showMessageDialog(
                                dialog,
                                e.getCause().getMessage(),
                                "Warning",
                                JOptionPane.WARNING_MESSAGE);

                    } else {

                        qpal.components.AppDialogs.showMessageDialog(
                                dialog,
                                "Failed to update profile.",
                                "Error",
                                JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        }.execute();
    }

    private void setSaving(boolean saving) {
        btnSave.setEnabled(!saving);
        btnCancel.setEnabled(!saving);
        btnPassword.setEnabled(!saving);
        btnPhoto.setEnabled(!saving);
        txtName.setEnabled(!saving);
        txtEmail.setEnabled(!saving);
    }

    private JTextField createTextField() {
        JTextField field = new JTextField();
        field.setBackground(new Color(220, 220, 220));
        field.setBorder(new EmptyBorder(8, 10, 8, 10));
        field.setPreferredSize(new Dimension(150, 36));
        field.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        field.setAlignmentX(Component.LEFT_ALIGNMENT);
        return field;
    }

    private JLabel createLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("SansSerif", Font.BOLD, 14));
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    private void addField(JPanel panel, String text, JComponent field) {
        panel.add(createLabel(text));
        panel.add(Box.createVerticalStrut(10));
        panel.add(field);
    }

    private JButton createButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("SansSerif", Font.BOLD, 12));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder());
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }
}
