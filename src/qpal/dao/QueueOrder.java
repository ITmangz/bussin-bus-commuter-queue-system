package qpal.dao;

/** Shared SQL ordering for the staff list, monitor and transactional call/recall. */
final class QueueOrder {
    private QueueOrder() {}

    static String priority(String queue) {
        return "CASE WHEN EXISTS (SELECT 1 FROM booking_passengers priority_person WHERE "
                + "priority_person.booking_id=" + queue + ".booking_id AND "
                + "priority_person.passenger_type IN ('Senior','PWD')) THEN 0 ELSE 1 END";
    }

    static String payment(String queue) {
        // Preserve skip handling: waiting passengers are called before skipped retries.
        return "CASE WHEN " + queue + ".status='Waiting' THEN 0 ELSE 1 END,"
                + priority(queue) + "," + queue + ".queue_number," + queue + ".queue_entry_id";
    }
}
