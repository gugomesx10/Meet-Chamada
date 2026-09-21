-- =========================================================
-- V3 - Google Meet fields in Presence Evidence
-- Meet-Chamada
-- =========================================================

ALTER TABLE meet_presence_evidence
    ADD COLUMN ended_at TIMESTAMP;

ALTER TABLE meet_presence_evidence
    ADD COLUMN external_reference VARCHAR(500);

CREATE UNIQUE INDEX uk_presence_evidence_source_external_reference
    ON meet_presence_evidence (source, external_reference);