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
        MimeBodyPart plain = new MimeBodyPart();
        plain.setText(
                "Your Bussin password reset code is: "
                        + code
                        + "\n\nThis code expires in 5 minutes. Do not share it."
                        + "\nIf you did not request this, you can ignore this email.",
                "UTF-8");
        MimeBodyPart html = new MimeBodyPart();
        html.setText(resetEmailHtml(code), "UTF-8", "html");
        MimeMultipart content = new MimeMultipart("alternative");
        content.addBodyPart(plain);
        content.addBodyPart(html);
        message.setContent(content);
        Transport.send(message);
    }

    public static String resetEmailHtml(String code) {
        if (code == null || !code.matches("[0-9]{6}")) {
            throw new IllegalArgumentException("A six-digit reset code is required.");
        }
        return """
                <!doctype html>
                <html lang="en">
                    <head>
                        <meta charset="UTF-8">
                        <meta name="viewport" content="width=device-width, initial-scale=1">
                    </head>

                    <body style="
                        margin: 0;
                        padding: 0;
                        background: #ffffff;
                        color: #24292f;
                        font-family: Arial, Helvetica, sans-serif;
                    ">
                        <table
                            role="presentation"
                            width="100%%"
                            cellspacing="0"
                            cellpadding="0"
                            style="background: #ffffff;"
                        >
                            <tr>
                                <td align="center" style="padding: 40px 20px;">
                                    <table
                                        role="presentation"
                                        width="100%%"
                                        cellspacing="0"
                                        cellpadding="0"
                                        style="max-width: 600px;"
                                    >
                                        <!-- Bussin heading -->
                                        <tr>
                                            <td align="center" style="
                                                padding: 0 0 18px;
                                                font-size: 34px;
                                                font-weight: bold;
                                                color: #e1002d;
                                            ">
                                                Bussin
                                            </td>
                                        </tr>

                                        <tr>
                                            <td align="center" style="
                                                padding: 0 0 24px;
                                                font-size: 24px;
                                                line-height: 32px;
                                            ">
                                                Please verify your identity
                                            </td>
                                        </tr>

                                        <!-- Verification code card -->
                                        <tr>
                                            <td style="
                                                border: 1px solid #dfe3e8;
                                                border-radius: 6px;
                                                padding: 24px;
                                                font-size: 16px;
                                                line-height: 25px;
                                            ">
                                                <p style="margin: 0 0 18px;">
                                                    Here is your Bussin password reset code:
                                                </p>

                                                <p style="
                                                    margin: 0 0 24px;
                                                    text-align: center;
                                                    font-size: 32px;
                                                    line-height: 44px;
                                                    letter-spacing: 6px;
                                                    font-family: Consolas, monospace;
                                                ">%s</p>

                                                <p style="margin: 0 0 14px;">
                                                    This code is valid for <strong>5 minutes</strong>
                                                    and can only be used once.
                                                </p>

                                                <p style="margin: 0 0 14px;">
                                                    <strong>Please don't share this code with anyone.</strong>
                                                    Enter it only in the Bussin password reset screen.
                                                </p>

                                                <p style="margin: 0;">
                                                    Thanks,<br>The Bussin Team
                                                </p>
                                            </td>
                                        </tr>

                                        <!-- Email footer -->
                                        <tr>
                                            <td style="
                                                padding: 22px 0;
                                                font-size: 14px;
                                                line-height: 22px;
                                                color: #6b7280;
                                            ">
                                                You're receiving this email because a password reset
                                                was requested for your Bussin account. If this wasn't
                                                you, please ignore this email. Your password will
                                                remain unchanged.
                                            </td>
                                        </tr>

                                        <tr>
                                            <td style="border-top: 1px solid #dfe3e8; height: 1px;"></td>
                                        </tr>
                                    </table>
                                </td>
                            </tr>
                        </table>
                    </body>
                </html>
                """.formatted(code);
    }
}
