# Gmail password-reset setup

The existing Forgot Password, Code Verification, and Reset Password screens now use Jakarta Mail. Verification emails are only sent for password resets.

1. Enable Google 2-Step Verification on the sender account and create an App Password named Bussin (if your account permits app passwords).
2. Open Windows **Edit environment variables for your account**. Add these user variables:
   - `BUSSIN_MAIL_ADDRESS`: your complete sender Gmail address.
   - `BUSSIN_MAIL_APP_PASSWORD`: the generated Google App Password, not your normal password.
3. Restart VS Code and the app so Java receives the variables. Do not place credentials in source, Git, screenshots, or chat.
4. In Manage Accounts, set the account's email to a real address you control. Gmail, Outlook, Yahoo, and school addresses are accepted. Old username-only records are not modified automatically.
5. Choose Forgot Password, enter that registered address, and check its inbox/spam folder. Enter the code and set a new password, then test login.

The app uses Gmail SMTP on port 587 with required STARTTLS and certificate hostname checking. The Jakarta Mail and Activation JARs must remain in `lib`.

Codes expire in five minutes, allow five incorrect attempts, and have a 60-second resend cooldown. Successful resend replaces the previous code. Verification creates a five-minute, single-use reset authorization. Challenges persist in the `password_reset_codes` table, created automatically using the existing database connection. Database credentials need permission to create that table on first use.

Sending runs in the background. A successful SMTP handoff does not prove delivery; later bounce notices may arrive in the sender inbox. Unknown/inactive accounts get the same general confirmation, without sending mail.

The existing account password storage format is preserved for login compatibility. This change does not migrate the app's existing plaintext passwords to password hashes. A deployed multi-user application should move reset processing and mail credentials into a trusted server, rather than distributing Gmail/database credentials with desktop clients.

Automated checks use an isolated temporary database and send no emails. Real delivery requires the sender environment variables and a manual reset test.
