# Staff payment form and simulation

The kiosk only selects a payment method and creates a pending booking. Staff open
Payment to see the details form alongside the existing QR-scan or card-tap animation.
Wallet options are GCash, Maya, MariBank, and PayPal. PayPal uses an email field; the other wallets use mobile numbers. Card types are Visa, Mastercard, and Other.

Enter demo mobile details or the last four card digits, then click the QR/card or
the scan/tap button. When the animation succeeds, the form automatically receives
a read-only transaction reference and Mark as Paid becomes available. This is a
simulation and does not contact a provider or charge money.

Cancelling an attempt, editing payment details, or changing methods stops the
animation and clears approval. A new attempt is required. Final confirmation stores
the selected method and generated reference in the existing payment transaction;
it keeps the same booking and queue number. Mobile numbers and card details are
demo inputs and are not stored. Existing expiry, station, exact-amount, and duplicate
reference checks still apply. The payment-method enum is extended automatically before payment transactions to preserve the chosen provider on receipts. Existing records are preserved.

StaffPaymentSimulationTest runs the real Swing timers and verifies reference
population, cancellation, invalidation, method switching, and rendered previews.
QueuePaymentTest --db checks settlement in an isolated temporary database.
