# Fare policy and proposed cashless FIFO flow

## Implemented fare policy

New bookings receive one 20% discount for each Student, PWD or Senior passenger. Regular passengers pay the full drop-off fare. Discounts do not stack. Drop-off fares are rounded to the nearest whole peso first; each passenger's discounted fare is then rounded to the nearest whole peso (half up). The booking total is the sum of those individual fares. For example, a PHP 51 fare costs PHP 41 for each eligible passenger.

Route fare entry accepts positive whole-peso amounts. Existing saved bookings are not repriced. Passenger categories on saved bookings cannot be changed without cancelling and rebooking, to prevent category and charged-fare mismatches. Staff should check eligibility documents before collecting payment. This is the application's configured discount and rounding policy.

Bus capacity choices: 24, 28, 32, 36, 40, 44. Legacy capacities can be retained using Keep current when editing. Capacity changes are blocked for scheduled or boarding trips with bookings; historical trips do not block edits. Unbooked active trips receive the new seat capacity.

## Proposed cashless FIFO flow (not implemented)

1. Save the reservation and allocate its immutable queue date/number before starting cashless payment. Seat reservation, booking creation and queue allocation must commit together. Define arrival as this successful reservation, rather than when somebody opens a kiosk screen.
2. Use the same booking queue identity for Cash, GCash and Card. Payment confirmation updates payment status only; it must never create another booking or queue number. A provider-verified payment event must be idempotent. A typed reference or screenshot alone is not proof of payment.
3. Keep counter service in booking queue order. Cashless customers can pay while waiting and keep their original place; verification or eligibility checks still use that booking.
4. Within each departing trip, board by original queue date/number, never by payment timestamp. Strict FIFO means an earlier unresolved booking blocks later boarding until it pays, cancels, or expires. The existing payment cutoff (30 minutes before departure) provides a deadline; only paid bookings may board.
5. Record cancellations and no-shows without renumbering remaining bookings. An explicit staff skip must be logged and is an exception to strict FIFO; moving skipped people to the end is not strict original-order FIFO.
6. Handle late cashless confirmation after reservation expiry through reconciliation/refund, without restoring a released seat or moving anyone ahead. Separate departure queues by trip so a later trip does not block an earlier departure.

Current boarding order is payment time, with skipped passengers moved behind eligible passengers. Both the boarding list and gate call-next selection would need to change together to adopt this proposal. Cashless provider integration is not part of the fare/seat update.
