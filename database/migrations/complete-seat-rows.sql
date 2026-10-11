-- Complete four-seat rows for existing buses. Existing seat numbers are unchanged.
-- Only active trips receive the additional seats; historical trip availability is preserved.
START TRANSACTION;
UPDATE trips t JOIN buses b ON b.bus_id=t.bus_id
SET t.available_seats=t.available_seats+(4-MOD(b.seat_capacity,4))
WHERE b.seat_capacity>0 AND MOD(b.seat_capacity,4)<>0
AND t.status IN ('Scheduled','Boarding');
UPDATE buses
SET available_seats=available_seats+(4-MOD(seat_capacity,4)),
seat_capacity=seat_capacity+(4-MOD(seat_capacity,4))
WHERE seat_capacity>0 AND MOD(seat_capacity,4)<>0;
COMMIT;
