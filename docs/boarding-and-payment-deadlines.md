# Boarding and payment deadlines

- Payment must be saved more than 30 minutes before the scheduled departure. At exactly 30 minutes, payment is closed.
- New unpaid reservations use the same cutoff. Released seats are reflected in availability, but the system does not accept new unpaid reservations after the cutoff.
- Every five seconds while the application service is running, overdue unpaid bookings and their queue entries become No-show. Pending payment records are closed internally as Cancelled (shown as Unpaid in the payment queue), and active seat reservations are released exactly once. Paid bookings are retained.
- Expiry catches up after restarting or reconnecting. This desktop application does not run an independent database scheduler while all clients are closed.
- Completing a paid payment queue automatically places it in the boarding list as Awaiting Gate.
- In Boarding Queue, select Gate 1 or Gate 2, then Assign trip to gate. The earliest departure is suggested; staff may select another trip after checking bus readiness.
- Each gate holds one trip, and each trip holds at most one gate. Call Next Queue calls only passengers belonging to that gate's trip, ordered by payment time. Skipping a boarding call moves it behind other eligible passengers in that trip.
- Release gate returns the trip to Awaiting Gate without clearing completed boarding records.
- Confirm departure requires completed boarding, or explicit confirmation to mark remaining paid bookings No-show. No-show does not refund payment or release a paid seat for resale.
- Scheduled time alone never marks a trip Departed. Overdue active boarding trips remain visible as delayed.
- Missed payment calls can be skipped and recalled before the cutoff. Skipped unpaid bookings become No-show at the same deadline.

The application creates `boarding_gates` and `boarding_skips` using its existing create-if-missing convention. No existing booking records are altered merely by compilation or tests.

Validation: `PaymentDeadlineTest` uses connection-local temporary tables. `BoardingGateTest` copies schema only into a uniquely named test database and drops only that database afterward.
