# Bus layouts and priority service

Add/Edit Bus offers 3 left + 2 right, 2 left + 3 right, and 2 left + 2 right.
Rows are numbered from the front; columns run left to right across the aisle.
Capacity choices follow the layout: 24/28/32/36/40/44 for 2+2 and
25/30/35/40/45 for 3+2 or 2+3. Changing layout retains a compatible capacity;
otherwise a new capacity must be selected. Existing complete-row capacities
outside this range can be retained. The DAO rejects incomplete rows on save.
The kiosk shows four rows per page. The first row has five priority seats in a five-seat
layout and four in 2+2, marked P and colored amber.

New front-row reservations require a Senior or PWD passenger. Regular and Student
passengers, including companions, use other seats. Senior/PWD passengers can also
use standard seats. In mixed bookings the kiosk assigns selected priority seats
to eligible passengers and displays each assignment before continuing. The DAO
enforces the same rule for staff-created bookings and rejects stale layouts.
Existing reservations are preserved, including regular front-row reservations.

Payment queue calls use Senior/PWD priority, then FIFO by queue number within
each tier. One qualifying passenger gives the whole booking priority. Current
service is not interrupted; the two counters remain independent. Existing skip
behavior is retained: waiting bookings precede skipped retries, and staff can
explicitly recall a skipped booking. Priority applies within each waiting/retry
group. The staff list, call-next and ordinary recall use the same ordering.

Boarding is scoped to the gate's assigned trip. Senior/PWD bookings go first,
then FIFO within each tier by payment completion time (entry into boarding).
Skipped boarding bookings move to the end of their tier. Specific boarding
calls cannot bypass the next eligible booking. Monitors use the DAO order and
mark priority bookings; a group already being called is not interrupted.

PWD Other reveals a required text field. Blank/whitespace answers cannot
continue. Back navigation retains the answer; choosing a named category hides
it, and Start Over clears it. As before, ID/disability details remain in the
kiosk session and are not persisted to the database.

Apply database/migrations/seat-layout-priority.sql once for existing installations.
The local development database has been migrated. Old buses default to 2+2;
booking layout snapshots protect historical receipt/revenue labels. Changes to
bus layout or capacity require no bookings on its Scheduled/Boarding trips.

Validation: compile src and tests with javac (UTF-8, lib/*); run
PriorityBookingTest, BusEditTest, SeatLayoutUiTest and QueueMonitorLayoutTest.
Database tests create and drop uniquely named test schemas, leaving production
bookings untouched. SeatLayoutUiTest produces build/seats-3+2.png,
build/seats-2+3.png, build/seats-2+2.png and build/pwd-other.png.
