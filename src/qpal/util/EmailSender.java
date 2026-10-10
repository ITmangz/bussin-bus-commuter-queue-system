package qpal.util;

import jakarta.mail.*;
import jakarta.mail.internet.*;
import java.util.Properties;

public class EmailSender {
    public static boolean validEmail(String email) {
        if (email == null || email.length() > 100 || !email.matches("[^\\s@]+@[^\\s@]+\\.[^\\s@]+"))
            return false;
        try {
            InternetAddress address = new InternetAddress(email, true);
            address.validate();
            return address.getAddress().equals(email) && address.getPersonal() == null;
        } catch (AddressException ex) {
            return false;
        }
    }

    public static void sendResetCode(String recipient, String code) throws Exception {
        String sender = System.getenv("BUSSIN_MAIL_ADDRESS");
        String password = System.getenv("BUSSIN_MAIL_APP_PASSWORD");
        if (sender == null || password == null || !validEmail(sender) || password.isBlank()) {
            throw new IllegalStateException(
                    "Configure BUSSIN_MAIL_ADDRESS and BUSSIN_MAIL_APP_PASSWORD, then restart the"
                        + " app.");
        }
        Properties properties = new Properties();
        properties.put("mail.smtp.host", "smtp.gmail.com");
        properties.put("mail.smtp.port", "587");
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.starttls.required", "true");
        properties.put("mail.smtp.ssl.checkserveridentity", "true");
        properties.put("mail.smtp.connectiontimeout", "10000");
        properties.put("mail.smtp.timeout", "15000");
        properties.put("mail.smtp.writetimeout", "15000");
        Session session =
                Session.getInstance(
                        properties,
                        new Authenticator() {
                            @Override
                            protected PasswordAuthentication getPasswordAuthentication() {
                                return new PasswordAuthentication(
                                        sender, password.replace(" ", ""));
                            }
                        });
        MimeMessage message = new MimeMessage(session);
        message.setFrom(new InternetAddress(sender, "Bussin"));
        message.setRecipient(Message.RecipientType.TO, new InternetAddress(recipient));
        message.setSubject("Bussin password reset code", "UTF-8");
        message.setText(
                "Your Bussin password reset code is: "
                        + code
                        + "\n\nThis code expires in 5 minutes. Do not share it."
                        + "\nIf you did not request this, you can ignore this email.",
                "UTF-8");
        Transport.send(message);
    }
}
