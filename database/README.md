BUSSIN Database Setup

1. Install kayo XAMPP and start MySQL.
2. Open phpMyAdmin.
3. Create/import the qpal.sql database.
4. Make sure niyo name ng database is qpal.
5. Open the project in VS Code.
6. Check DbConnection.java.
7. Run the system.

Seat layouts and priority seats
------------------------------
For an existing database, run migrations/seat-layout-priority.sql once before
starting this version. Fresh qpal.sql imports already include these columns.
The migration preserves existing buses and booking labels as 2+2. Add/Edit Bus
supports 3+2, 2+3 and 2+2. Bookings save the chosen layout so receipts and revenue
seat labels remain correct after later bus edits. Layout/capacity changes are
blocked while the bus has bookings on a Scheduled or Boarding trip.

New bookings reserve the front row for Senior/PWD passengers. Existing seat
reservations are retained. See ../docs/priority-seating.md for queue rules and tests.

Status Change Reasons
---------------------
For an existing database, run migrations/status-reasons.sql once before starting
this version. Fresh imports of qpal.sql already include the two new columns.
The migration preserves existing records and leaves their reasons empty. The UI
shows "Not recorded" for these records; a reason is required when saving a bus
with Maintenance or Inactive status, or an Inactive account. Reasons are limited
to 500 characters. Returning to Available or Active clears the current reason.

Activity Log
------------
For a new database, import activity_logs.sql into qpal (also included in qpal.sql).
The existing local activity_logs table from the earlier setup can be reused.
ActivityLogDao uses DbConnection, PreparedStatement and a model, like AccountDao.
The DAO does not create or change database tables at runtime.

The admin Activity List uses the Revenue List layout: Search, All Actions, Print,
and ten rows per page. Print uses a snapshot of the current page. No images or
module icons appear in the table; only action labels have colored backgrounds.

Logs record successful signed-in actions from this version onward. Email and role
are stored as historical copies; account_id identifies the user even after an email
change. Employee queries are restricted by account_id. Logs are read-only in the UI.
Unauthenticated kiosk actions and automatic departures are not attributed to users.
If an insert fails, the business action is preserved and the Activity Log page shows
a warning. This is best-effort logging, not a transactional audit system.
