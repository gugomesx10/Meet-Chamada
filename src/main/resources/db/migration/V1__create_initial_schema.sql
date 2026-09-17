-- =========================================================
-- V1 - Initial Schema
-- Meet-Chamada
-- =========================================================


-- =========================================================
-- USER
-- =========================================================

CREATE TABLE meet_user (
                           id UUID NOT NULL,
                           name VARCHAR(150) NOT NULL,
                           email VARCHAR(255) NOT NULL,
                           external_id VARCHAR(255),
                           created_at TIMESTAMP NOT NULL,
                           updated_at TIMESTAMP NOT NULL,

                           CONSTRAINT pk_meet_user PRIMARY KEY (id),
                           CONSTRAINT uk_meet_user_email UNIQUE (email),
                           CONSTRAINT uk_meet_user_external_id UNIQUE (external_id)
);


-- =========================================================
-- INSTITUTION
-- =========================================================

CREATE TABLE meet_institution (
                                  id UUID NOT NULL,
                                  name VARCHAR(150) NOT NULL,
                                  created_at TIMESTAMP NOT NULL,
                                  updated_at TIMESTAMP NOT NULL,

                                  CONSTRAINT pk_meet_institution PRIMARY KEY (id)
);


-- =========================================================
-- INSTITUTION MEMBERSHIP
-- =========================================================

CREATE TABLE meet_institution_membership (
                                             id UUID NOT NULL,
                                             user_id UUID NOT NULL,
                                             institution_id UUID NOT NULL,
                                             role VARCHAR(30) NOT NULL,
                                             created_at TIMESTAMP NOT NULL,
                                             updated_at TIMESTAMP NOT NULL,

                                             CONSTRAINT pk_meet_institution_membership
                                                 PRIMARY KEY (id),

                                             CONSTRAINT fk_institution_membership_user
                                                 FOREIGN KEY (user_id)
                                                     REFERENCES meet_user (id),

                                             CONSTRAINT fk_institution_membership_institution
                                                 FOREIGN KEY (institution_id)
                                                     REFERENCES meet_institution (id),

                                             CONSTRAINT uk_membership_user_institution
                                                 UNIQUE (user_id, institution_id),

                                             CONSTRAINT ck_institution_membership_role
                                                 CHECK (role IN ('STUDENT', 'TEACHER', 'ADMIN'))
);


-- =========================================================
-- COURSE
-- =========================================================

CREATE TABLE meet_course (
                             id UUID NOT NULL,
                             institution_id UUID NOT NULL,
                             name VARCHAR(150) NOT NULL,
                             description VARCHAR(1000),
                             start_date DATE NOT NULL,
                             end_date DATE NOT NULL,
                             status VARCHAR(30) NOT NULL,
                             created_at TIMESTAMP NOT NULL,
                             updated_at TIMESTAMP NOT NULL,

                             CONSTRAINT pk_meet_course PRIMARY KEY (id),

                             CONSTRAINT fk_course_institution
                                 FOREIGN KEY (institution_id)
                                     REFERENCES meet_institution (id),

                             CONSTRAINT ck_course_status
                                 CHECK (
                                     status IN (
                                                'PLANNED',
                                                'ACTIVE',
                                                'COMPLETED',
                                                'CANCELLED'
                                         )
                                     ),

                             CONSTRAINT ck_course_dates
                                 CHECK (end_date >= start_date)
);


-- =========================================================
-- COURSE MEMBERSHIP
-- =========================================================

CREATE TABLE meet_course_membership (
                                        id UUID NOT NULL,
                                        course_id UUID NOT NULL,
                                        user_id UUID NOT NULL,
                                        role VARCHAR(30) NOT NULL,
                                        created_at TIMESTAMP NOT NULL,
                                        updated_at TIMESTAMP NOT NULL,

                                        CONSTRAINT pk_meet_course_membership
                                            PRIMARY KEY (id),

                                        CONSTRAINT fk_course_membership_course
                                            FOREIGN KEY (course_id)
                                                REFERENCES meet_course (id),

                                        CONSTRAINT fk_course_membership_user
                                            FOREIGN KEY (user_id)
                                                REFERENCES meet_user (id),

                                        CONSTRAINT uk_course_membership_user_course
                                            UNIQUE (user_id, course_id),

                                        CONSTRAINT ck_course_membership_role
                                            CHECK (role IN ('STUDENT', 'INSTRUCTOR'))
);


-- =========================================================
-- CLASS SESSION
-- =========================================================

CREATE TABLE meet_class_session (
                                    id UUID NOT NULL,
                                    course_id UUID NOT NULL,
                                    title VARCHAR(150) NOT NULL,
                                    session_date DATE NOT NULL,
                                    start_time TIME NOT NULL,
                                    end_time TIME NOT NULL,
                                    status VARCHAR(30) NOT NULL,
                                    created_at TIMESTAMP NOT NULL,
                                    updated_at TIMESTAMP NOT NULL,

                                    CONSTRAINT pk_meet_class_session
                                        PRIMARY KEY (id),

                                    CONSTRAINT fk_class_session_course
                                        FOREIGN KEY (course_id)
                                            REFERENCES meet_course (id),

                                    CONSTRAINT ck_class_session_status
                                        CHECK (
                                            status IN (
                                                       'SCHEDULED',
                                                       'IN_PROGRESS',
                                                       'COMPLETED',
                                                       'CANCELLED'
                                                )
                                            ),

                                    CONSTRAINT ck_class_session_times
                                        CHECK (end_time > start_time)
);


-- =========================================================
-- SESSION BLOCK
-- =========================================================

CREATE TABLE meet_session_block (
                                    id UUID NOT NULL,
                                    class_session_id UUID NOT NULL,
                                    instructor_id UUID NOT NULL,
                                    title VARCHAR(150) NOT NULL,
                                    description VARCHAR(1000),
                                    type VARCHAR(30) NOT NULL,
                                    start_time TIME NOT NULL,
                                    end_time TIME NOT NULL,
                                    created_at TIMESTAMP NOT NULL,
                                    updated_at TIMESTAMP NOT NULL,

                                    CONSTRAINT pk_meet_session_block
                                        PRIMARY KEY (id),

                                    CONSTRAINT fk_session_block_class_session
                                        FOREIGN KEY (class_session_id)
                                            REFERENCES meet_class_session (id),

                                    CONSTRAINT fk_session_block_instructor
                                        FOREIGN KEY (instructor_id)
                                            REFERENCES meet_user (id),

                                    CONSTRAINT ck_session_block_type
                                        CHECK (
                                            type IN (
                                                     'THEORETICAL',
                                                     'TECHNICAL',
                                                     'LAB',
                                                     'WORKSHOP',
                                                     'MENTORING',
                                                     'ASSESSMENT',
                                                     'OTHER'
                                                )
                                            ),

                                    CONSTRAINT ck_session_block_times
                                        CHECK (end_time > start_time)
);


-- =========================================================
-- PRESENCE EVIDENCE
-- =========================================================

CREATE TABLE meet_presence_evidence (
                                        id UUID NOT NULL,
                                        student_id UUID NOT NULL,
                                        class_session_id UUID NOT NULL,
                                        session_block_id UUID,
                                        type VARCHAR(30) NOT NULL,
                                        source VARCHAR(30) NOT NULL,
                                        occurred_at TIMESTAMP NOT NULL,
                                        details VARCHAR(1000),
                                        created_at TIMESTAMP NOT NULL,

                                        CONSTRAINT pk_meet_presence_evidence
                                            PRIMARY KEY (id),

                                        CONSTRAINT fk_presence_evidence_student
                                            FOREIGN KEY (student_id)
                                                REFERENCES meet_user (id),

                                        CONSTRAINT fk_presence_evidence_class_session
                                            FOREIGN KEY (class_session_id)
                                                REFERENCES meet_class_session (id),

                                        CONSTRAINT fk_presence_evidence_session_block
                                            FOREIGN KEY (session_block_id)
                                                REFERENCES meet_session_block (id),

                                        CONSTRAINT ck_presence_evidence_type
                                            CHECK (
                                                type IN (
                                                         'MEETING_SESSION',
                                                         'CHECK_IN',
                                                         'CHECK_OUT',
                                                         'POLL_RESPONSE',
                                                         'ACTIVITY_RESPONSE',
                                                         'CHAT_INTERACTION',
                                                         'REACTION',
                                                         'ORAL_INTERACTION',
                                                         'TEACHER_CONFIRMATION'
                                                    )
                                                ),

                                        CONSTRAINT ck_presence_evidence_source
                                            CHECK (
                                                source IN (
                                                           'INTERNAL',
                                                           'GOOGLE_MEET',
                                                           'CANVAS',
                                                           'TEACHER',
                                                           'IMPORT'
                                                    )
                                                )
);


-- =========================================================
-- CHECK-IN WINDOW
-- =========================================================

CREATE TABLE meet_check_in_window (
                                      id UUID NOT NULL,
                                      class_session_id UUID NOT NULL,
                                      session_block_id UUID,
                                      opened_by UUID NOT NULL,
                                      opened_at TIMESTAMP NOT NULL,
                                      closes_at TIMESTAMP NOT NULL,
                                      status VARCHAR(30) NOT NULL,
                                      created_at TIMESTAMP NOT NULL,
                                      updated_at TIMESTAMP NOT NULL,

                                      CONSTRAINT pk_meet_check_in_window
                                          PRIMARY KEY (id),

                                      CONSTRAINT fk_check_in_window_class_session
                                          FOREIGN KEY (class_session_id)
                                              REFERENCES meet_class_session (id),

                                      CONSTRAINT fk_check_in_window_session_block
                                          FOREIGN KEY (session_block_id)
                                              REFERENCES meet_session_block (id),

                                      CONSTRAINT fk_check_in_window_opened_by
                                          FOREIGN KEY (opened_by)
                                              REFERENCES meet_user (id),

                                      CONSTRAINT ck_check_in_window_status
                                          CHECK (
                                              status IN (
                                                         'OPEN',
                                                         'CLOSED',
                                                         'CANCELLED'
                                                  )
                                              ),

                                      CONSTRAINT ck_check_in_window_period
                                          CHECK (closes_at > opened_at)
);


-- =========================================================
-- CHECK-IN RESPONSE
-- =========================================================

CREATE TABLE meet_check_in_response (
                                        id UUID NOT NULL,
                                        check_in_window_id UUID NOT NULL,
                                        student_id UUID NOT NULL,
                                        responded_at TIMESTAMP NOT NULL,
                                        valid BOOLEAN NOT NULL,
                                        created_at TIMESTAMP NOT NULL,

                                        CONSTRAINT pk_meet_check_in_response
                                            PRIMARY KEY (id),

                                        CONSTRAINT fk_check_in_response_window
                                            FOREIGN KEY (check_in_window_id)
                                                REFERENCES meet_check_in_window (id),

                                        CONSTRAINT fk_check_in_response_student
                                            FOREIGN KEY (student_id)
                                                REFERENCES meet_user (id),

                                        CONSTRAINT uk_check_in_response_window_student
                                            UNIQUE (check_in_window_id, student_id)
);


-- =========================================================
-- ATTENDANCE DECISION
-- =========================================================

CREATE TABLE meet_attendance_decision (
                                          id UUID NOT NULL,
                                          student_id UUID NOT NULL,
                                          class_session_id UUID NOT NULL,
                                          status VARCHAR(30) NOT NULL,
                                          decision_source VARCHAR(30) NOT NULL,
                                          decided_by UUID,
                                          reason VARCHAR(2000),
                                          decided_at TIMESTAMP,
                                          created_at TIMESTAMP NOT NULL,
                                          updated_at TIMESTAMP NOT NULL,

                                          CONSTRAINT pk_meet_attendance_decision
                                              PRIMARY KEY (id),

                                          CONSTRAINT fk_attendance_decision_student
                                              FOREIGN KEY (student_id)
                                                  REFERENCES meet_user (id),

                                          CONSTRAINT fk_attendance_decision_class_session
                                              FOREIGN KEY (class_session_id)
                                                  REFERENCES meet_class_session (id),

                                          CONSTRAINT fk_attendance_decision_decided_by
                                              FOREIGN KEY (decided_by)
                                                  REFERENCES meet_user (id),

                                          CONSTRAINT uk_attendance_student_session
                                              UNIQUE (student_id, class_session_id),

                                          CONSTRAINT ck_attendance_decision_status
                                              CHECK (
                                                  status IN (
                                                             'PENDING',
                                                             'CONFIRMED',
                                                             'REVIEW_REQUIRED',
                                                             'ABSENT',
                                                             'JUSTIFIED'
                                                      )
                                                  ),

                                          CONSTRAINT ck_attendance_decision_source
                                              CHECK (
                                                  decision_source IN (
                                                                      'SYSTEM',
                                                                      'TEACHER',
                                                                      'ADMIN'
                                                      )
                                                  )
);


-- =========================================================
-- ATTENDANCE REVIEW
-- =========================================================

CREATE TABLE meet_attendance_review (
                                        id UUID NOT NULL,
                                        attendance_decision_id UUID NOT NULL,
                                        reviewer_id UUID NOT NULL,
                                        previous_status VARCHAR(30) NOT NULL,
                                        new_status VARCHAR(30) NOT NULL,
                                        reason VARCHAR(2000) NOT NULL,
                                        reviewed_at TIMESTAMP NOT NULL,
                                        created_at TIMESTAMP NOT NULL,

                                        CONSTRAINT pk_meet_attendance_review
                                            PRIMARY KEY (id),

                                        CONSTRAINT fk_attendance_review_decision
                                            FOREIGN KEY (attendance_decision_id)
                                                REFERENCES meet_attendance_decision (id),

                                        CONSTRAINT fk_attendance_review_reviewer
                                            FOREIGN KEY (reviewer_id)
                                                REFERENCES meet_user (id),

                                        CONSTRAINT ck_attendance_review_previous_status
                                            CHECK (
                                                previous_status IN (
                                                                    'PENDING',
                                                                    'CONFIRMED',
                                                                    'REVIEW_REQUIRED',
                                                                    'ABSENT',
                                                                    'JUSTIFIED'
                                                    )
                                                ),

                                        CONSTRAINT ck_attendance_review_new_status
                                            CHECK (
                                                new_status IN (
                                                               'PENDING',
                                                               'CONFIRMED',
                                                               'REVIEW_REQUIRED',
                                                               'ABSENT',
                                                               'JUSTIFIED'
                                                    )
                                                )
);


-- =========================================================
-- AUDIT EVENT
-- =========================================================

CREATE TABLE meet_audit_event (
                                  id UUID NOT NULL,
                                  actor_id UUID,
                                  action VARCHAR(100) NOT NULL,
                                  entity_type VARCHAR(100) NOT NULL,
                                  entity_id UUID,
                                  details VARCHAR(4000),
                                  occurred_at TIMESTAMP NOT NULL,
                                  created_at TIMESTAMP NOT NULL,

                                  CONSTRAINT pk_meet_audit_event
                                      PRIMARY KEY (id),

                                  CONSTRAINT fk_audit_event_actor
                                      FOREIGN KEY (actor_id)
                                          REFERENCES meet_user (id)
);


-- =========================================================
-- INDEXES
-- PostgreSQL does not automatically create indexes for FK
-- columns, so we add the ones useful for our main queries.
-- =========================================================

CREATE INDEX idx_institution_membership_institution
    ON meet_institution_membership (institution_id);

CREATE INDEX idx_course_institution
    ON meet_course (institution_id);

CREATE INDEX idx_course_membership_course
    ON meet_course_membership (course_id);

CREATE INDEX idx_class_session_course_date
    ON meet_class_session (course_id, session_date);

CREATE INDEX idx_session_block_class_session
    ON meet_session_block (class_session_id);

CREATE INDEX idx_session_block_instructor
    ON meet_session_block (instructor_id);

CREATE INDEX idx_presence_evidence_session_student
    ON meet_presence_evidence (class_session_id, student_id);

CREATE INDEX idx_presence_evidence_occurred_at
    ON meet_presence_evidence (occurred_at);

CREATE INDEX idx_presence_evidence_block
    ON meet_presence_evidence (session_block_id);

CREATE INDEX idx_check_in_window_session
    ON meet_check_in_window (class_session_id);

CREATE INDEX idx_check_in_window_block
    ON meet_check_in_window (session_block_id);

CREATE INDEX idx_check_in_response_student
    ON meet_check_in_response (student_id);

CREATE INDEX idx_attendance_decision_session_status
    ON meet_attendance_decision (class_session_id, status);

CREATE INDEX idx_attendance_review_decision
    ON meet_attendance_review (attendance_decision_id);

CREATE INDEX idx_audit_event_actor
    ON meet_audit_event (actor_id);

CREATE INDEX idx_audit_event_entity
    ON meet_audit_event (entity_type, entity_id);

CREATE INDEX idx_audit_event_occurred_at
    ON meet_audit_event (occurred_at);