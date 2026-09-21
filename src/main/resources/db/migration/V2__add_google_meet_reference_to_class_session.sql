-- =========================================================
-- V2 - Google Meet reference in Class Session
-- Meet-Chamada
-- =========================================================

ALTER TABLE meet_class_session
    ADD COLUMN google_meet_space_name VARCHAR(255);

ALTER TABLE meet_class_session
    ADD COLUMN google_meet_conference_record_name VARCHAR(255);

CREATE INDEX idx_class_session_google_meet_space
    ON meet_class_session (google_meet_space_name);

CREATE UNIQUE INDEX uk_class_session_google_meet_conference_record
    ON meet_class_session (google_meet_conference_record_name);