# Drop-off selection and fares

The complete page is `src/qpal/view/Commuter/DropPointPanel.java`. It keeps the existing 1000x650 BUSSIN header, steps and footer, shows a disabled PITX origin row and large destination rows inside a scroll pane using the shared ScrollBarStyle, with fares on the right in two-decimal format. PITX stays red on the route strip. Rows highlight on hover; clicking a selected destination clears both the stop and fare. There is no bottom selection/fare summary. Continue requires a selected stop and a loaded fare; messages use the shared AppDialogs design.

## Fare rule

As requested, use the admin's current full route fare from `routes.fare`:

- First intermediate stop: 60%.
- Second intermediate stop: 80%.
- Final destination: 100%, including routes where the final stop appears twice in the route mapping.

Amounts use BigDecimal and HALF_UP rounding to two decimal places. A full fare of PHP 50 gives PHP 30 / PHP 40 / PHP 50. No example prices are hard-coded. Change the full fare through the existing admin Add/Edit Trip form. The percentage rule is in `RouteDropPoints.fare`.

Fares load in a SwingWorker via RouteDao.getActiveFare. Missing, zero, or failed fare loads prevent proceeding. Existing bookings keep their saved amounts.

## Connections already implemented

- `TripDetailsPanel.getDropPoint()` and `getSelectedFare()` hold the selection and per-passenger fare. Changing routes or Start Over invalidates the selection.
- `AvailableTripPanel` uses the selected stop's fare on trip cards, while retaining the original full fare in TripOption for the database freshness check. Selecting a trip updates the per-passenger fare from that trip.
- `ConfirmTripDetailsPanel` displays the selected fare multiplied by the passenger count.
- `PaymentPanel` already passes the drop-off to BookingDao. BookingDao now calculates the stop fare from the live route fare, saves it for each passenger, and uses it for the booking/payment total. It rejects changed route fares before saving. Receipts use the saved total and drop-off.

These changes require the accompanying RouteDropPoints, RouteDao, TripDetailsPanel, TripCardPanel, AvailableTripPanel, ConfirmTripDetailsPanel and BookingDao files, not just the page file. No additional database schema changes are required by this redesign.

## Your bus image

The route strip contains an empty JLabel at PITX. Set your scaled image in the RouteStrip constructor's marked line, or use:

```java
dropPointPanel.getBusLabel().setIcon(new ImageIcon("resources/icons/your-bus.png"));
```

The label is 52x32 pixels. The bus remains at the origin; route markers and the red line highlight the selected stop. No bus is painted in code.

## Checks

DropPointUiTest checks the three stops, exclusive selection, selected amounts, refreshed admin fares, missing fare handling and route changes. It renders `build/drop-point.png`. RouteDropPointsTest checks percentages, rounding, duplicate final stops and origin rejection. PaymentDetailsTest verifies the existing payment field validation.

Database booking/payment persistence still needs an integration run against a disposable MySQL schema; the headless tests do not exercise live database writes.

