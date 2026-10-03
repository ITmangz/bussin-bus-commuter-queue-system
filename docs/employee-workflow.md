# Employee workflow

Employee login passes the authenticated account into `EmployeeDashboard`. The sidebar keeps the admin feature names: Dashboard, Queue Management, Bus Management, Route & Schedule Management, Activity Log, and Log Out. Employee classes follow the existing `Employee...Panel` naming convention and reuse the admin components for consistent styling.

## Stations

Employees select and confirm Counter 1, Counter 2, Boarding Gate 1, or Boarding Gate 2 before opening any feature page. In-use stations are unavailable. The database atomically prevents two sessions from claiming the same station or one employee from claiming multiple stations.

The selected station is fixed for the session. Log out and log in to select a different station. Reservations renew every 15 seconds and expire after 90 seconds without renewal, so a crashed client does not permanently occupy a station. Normal logout releases the reservation immediately. A renewal failure returns the employee to station selection once any in-flight queue action finishes. Expired sessions and inactive employee accounts cannot operate queues.

## Queues and access

- Payment employees see the shared waiting/skipped line and the serving queue at their own counter. Calling the next queue uses the existing FIFO rules. Payment, receipt/ticket printing, and completion stay available.
- Boarding employees see queues belonging to the trip at their gate. The existing assign-trip, release-gate, and confirm-departure controls operate only on that gate.
- Station restrictions are checked in the database transaction as well as in the UI. Admin station controls retain their existing behavior.
- Bus and route/schedule pages retain the admin tables, summaries, searches, filters, and pagination. Employee versions omit Add, Edit, Print, and Delete.
- Activity Log uses the existing account-filtered query and shows only the signed-in employee's records.

The employee dashboard shares the admin dashboard's statistic, boarding-bus, and queue-status card builders. Active Boarding shows all boarding buses to employees at both counters and gates, including departure information and booked-seat progress. Queue Status shows only the employee's assigned station. View schedules and Manage queues open the corresponding employee pages.

Station artwork is left blank in `EmployeeStationPanel`: `lblCounter1Image`, `lblCounter2Image`, `lblGate1Image`, and `lblGate2Image` are 90 × 76 image JLabels ready for ImageIcons. Station buttons have hover feedback; clicking a selected station again clears the selection and disables Confirm.

## Dashboard totals

Totals cover the current Philippine day (UTC+08:00) and the signed-in employee across their station sessions. Database connections explicitly use this timezone:

- **Total number of commuters served:** passenger counts from successful payment-queue completion or boarding completion performed by the employee. Failed/repeated completions are not counted. If one employee serves the same booking at both stages, both completed services count.
- **Total amount collected:** paid fare attributed to the employee, excluding change. Collection is credited when payment succeeds, independently of later ticket printing or queue completion.
- **Pending queues:** the unfinished queue assigned to the payment counter, or all unboarded queues for the trip at the assigned boarding gate.

Attribution is inserted in the same transaction as payment/completion. Historical actions before this feature have no reliable employee attribution and are not backfilled from free-text logs.

At midnight, the next automatic refresh shows the new day's totals starting at zero. No earnings records are deleted. Logging back in on the same day restores that day's cumulative total.

Logout saves an Activity Log entry with the station, session collection, today's total, and session commuters served. Session collection can span midnight, while today's total always refers to the current Philippine date. The log and station release commit together. If saving fails, logout stays open for retry; the app does not silently discard the summary.

A background check runs on startup and every minute. It writes one Daily Summary per employee and completed day, including zero-collection days with recorded station sessions. Missed days catch up after restarting the app. Summary markers and activity entries commit together, preventing duplicates and allowing failed saves to retry. The log timestamp is the time the summary is generated; the description identifies the day being summarized. Employees see their own entries; admins can see all entries.

## Database and verification

The app creates `employee_stations`, `employee_queue_work`, `employee_work_sessions`, and `employee_daily_summaries` automatically, following the existing queue-table initialization pattern. The database user needs CREATE TABLE permission on first use. Existing business records are not rewritten. Restart the app after updating so employees begin tracked sessions.

`EmployeeUiTest` covers the four-station selector, locked queue modes, read-only controls, navigation, and dashboard previews. `EmployeeIntegrationTest` creates its own temporary database, copies table definitions only, seeds fixtures, and removes that database afterward. It covers concurrent claims, account/session restrictions, payment rollback, ownership, attribution, private logs, expired leases, and inactive accounts. Run these with the existing queue, payment, and boarding regression tests.
