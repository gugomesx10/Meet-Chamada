-- =========================================================
-- V4 - Google Meet Participant Link
-- Meet-Chamada
-- =========================================================

CREATE TABLE meet_google_meet_participant_link (
                                                   id UUID NOT NULL,
                                                   user_id UUID NOT NULL,
                                                   google_meet_user_name VARCHAR(255) NOT NULL,
                                                   created_at TIMESTAMP NOT NULL,
                                                   updated_at TIMESTAMP NOT NULL,

                                                   CONSTRAINT pk_google_meet_participant_link
                                                       PRIMARY KEY (id),

                                                   CONSTRAINT fk_google_meet_participant_link_user
                                                       FOREIGN KEY (user_id)
                                                           REFERENCES meet_user (id),

                                                   CONSTRAINT uk_google_meet_participant_link_user_name
                                                       UNIQUE (google_meet_user_name)
);

CREATE INDEX idx_google_meet_participant_link_user
    ON meet_google_meet_participant_link (user_id);