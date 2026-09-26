# Kiosk and admin database connection

Both applications use `DbConnection` and the existing `qpal` database. No migration
is required for the schema inspected for this change. No live bookings or trips
were added during development.

## Try the complete flow

1. Start MySQL and launch `qpal.MainEmpAndAdmin` and `qpal.MainKiosk`.
2. In Admin, create an active route and an available bus, then add a future
   Scheduled trip with available seats. The database had no trips when checked.
3. In the kiosk, manually select PITX as the origin, one of the 29 configured
   destinations, a date, and a preferred time (30-minute choices). All four inputs
   start blank and reset to blank. Continue shows a JOptionPane warning and stays
   on Trip Details if any input is missing. Exact schedules are shown when available;
   otherwise the same route/date's available trips are shown nearest-time first.
   Equally close times prefer the later departure. No other date is substituted.
4. Select a trip, passenger count/type, and seats. No names are requested;
   booking records use Passenger 1, Passenger 2, and so on.
5. Review the total and select payment. Submitting saves a booking, passengers,
   seats, Pending payment and today's queue entry in one transaction.
6. The original ticket preview shows the real booking reference and queue number.
   Click Print Ticket to open the ticket-machine popup. A loading spinner and
   "Ticket is printing..." appear while the ticket animates out of the slot.
   After the animation, it displays "Please grab your payment ticket." Click the
   ticket (or press Space when focused) to close the popup, clear kiosk inputs,
   and return Home. This is an on-screen animation with no system printer dialog
   or physical print job. Detailed receipt printing remains at the admin counter.
7. Open Admin Queue Management. It refreshes every five seconds while visible.
   Call Next Queue, confirm payment collection with Mark as Paid, then Complete.
   Select a row for details, printing, or recalling a skipped queue.
8. Revenue Management refreshes every five seconds while visible. Only Paid
   payments contribute to today's revenue. Dashboard counts refresh every five seconds while visible.

## Behavior

- All passenger types currently use the admin's route fare. Passenger type is
  recorded, but no discount policy is assumed or added.
- E-Wallet maps to the existing GCash database value. Cash, GCash and Card are
  payment-method selections for counter collection, not electronic processing.
- Selecting a seat does not hold it. Final submission checks it atomically;
  a competing booking produces an error and the passenger can go back to choose
  another seat. No partial booking is saved.
- Queue numbers are allocated under a database lock, unique per database date.
  A booking reference is reused when retrying a submission to prevent duplicates.
- Booked trips retain their seat availability during admin edits. Their bus,
  route and departure cannot change through the editor, and cancellation through
  that editor is blocked. A dedicated cancellation/refund workflow is not included.
- A bus with bookings cannot change its seat capacity.
- No database constraints were changed. The Java flow validates the passenger
  seats and keeps booking/payment trip IDs consistent.

For separate computers, configure both Java processes with the same database URL,
for example `-Dqpal.db.url=jdbc:mysql://SERVER:3306/qpal`. Optional system properties
are `qpal.db.user` and `qpal.db.password`. The default remains local `qpal`.
The MySQL server must allow the application's account to connect from each device.

## Admin deletion and revenue controls

- Bus Delete permanently removes the bus and all its trips, bookings, passengers,
  seats, queue entries and payments. The confirmation lists these effects.
- Route & Schedule has a separate Delete Route dialog. Deleting a route removes
  its related trips and booking/payment records but keeps buses. Trip Delete
  removes only that trip and its dependent records. All deletions are transactional;
  daily queue counters are kept so deleted numbers are not reused.
- Revenue has Add, Edit, Print and Delete buttons. Add/Edit use modal forms;
  Print opens a preview of the current page and then the system print dialog.
  Delete opens a confirmation dialog. Refresh pauses while these dialogs are open.
- Add can create a standalone payment for a trip or replace a missing payment
  for an existing booking. A standalone payment does not reserve seats or create
  a queue. It represents one commuter in the paid count.
- Booking-linked payments retain their trip/booking and must match the booking
  total. Deleting or unpaying one preserves the booking/seats, marks the booking
  Pending and returns a Completed queue to Waiting. Use Add and choose the booking
  to replace a deleted payment before collecting/completing it again.
- Edits check whether a payment changed after the dialog opened. A stale save
  is rejected instead of overwriting a newer payment update.

## Verification

The tests in `tests` require a disposable empty copy of the live schema named
`qpal_integration_test_<suffix>`. They deliberately refuse a URL pointing to `qpal`.
Compile the project and both tests with Java 21 and `lib/*` on the classpath.
Run `BookingIntegrationTest` with `-Djava.awt.headless=true` and
`-Dqpal.db.url=jdbc:mysql://localhost:3306/qpal_integration_test_<suffix>`.

Checks cover rollback, duplicate submission, competing kiosks, queue numbering,
payment collection, queue transitions, revenue, trip editing, and Swing data
loading. The temporary test schema is removed after verification. Physical
printing and electronic payment processing were not tested.

## Automatic departures and boarding

- Scheduled and Boarding trips become Departed when their departure date/time is
  reached, using the database clock, regardless of occupancy or passenger attendance.
- The application checks every five seconds while running and catches up overdue
  trips when reopened. Keep an admin or kiosk process running for continuous updates.
- Bus Management refreshes automatically and shows Trip Status separately from the
  fleet availability status. Boarding takes priority, followed by today's next
  Scheduled trip, then the latest Departed trip.
- Queue Management has a Boarding Queue tab for paid, non-cancelled bookings on
  Boarding trips, including bookings made before today. Departed trips leave this tab.

### Boarding actions

Selecting Boarding Queue switches the details panel to Currently Boarding and
shows boarding Queue Actions: Call Next Queue, Recall, Complete Boarding, and Print Ticket. Select a boarding row to view its ticket, recall it
with an on-screen boarding announcement, or Complete Boarding. Completion is
stored separately from counter payment/queue completion and removes that row
from the boarding list. Returning to Payment Queue restores its payment actions.
The app creates a queue_boarding table on first use; records cascade when their
queue entry is deleted. Completion rejects unpaid, cancelled, already boarded,
and departed trips, including when the displayed row is stale.
