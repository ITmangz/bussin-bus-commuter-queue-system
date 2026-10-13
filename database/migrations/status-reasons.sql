-- Apply once to an existing qpal database before running this version.
-- Existing records have no recorded reason; staff supply one when editing them.
ALTER TABLE buses ADD COLUMN status_reason VARCHAR(500) NULL;
ALTER TABLE accounts ADD COLUMN status_reason VARCHAR(500) NULL;
