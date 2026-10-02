BUSSIN Database Setup

1. Install kayo XAMPP and start MySQL.
2. Open phpMyAdmin.
3. Create/import the qpal.sql database.
4. Make sure niyo name ng database is qpal.
5. Open the project in VS Code.
6. Check DbConnection.java.
7. Run the system.

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
