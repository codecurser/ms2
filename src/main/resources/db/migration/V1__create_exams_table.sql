-- V1__create_exams_table.sql
CREATE TABLE exams (
    id UUID PRIMARY KEY,
    creator_id UUID NOT NULL,
    title VARCHAR(200) NOT NULL,
    description TEXT,
    duration_minutes INTEGER NOT NULL,
    start_time TIMESTAMP WITHOUT TIME ZONE,
    end_time TIMESTAMP WITHOUT TIME ZONE,
    status VARCHAR(30) NOT NULL,
    share_code VARCHAR(20) UNIQUE,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    published_at TIMESTAMP WITHOUT TIME ZONE
);

CREATE INDEX idx_exams_creator_id ON exams(creator_id);
CREATE INDEX idx_exams_status ON exams(status);
CREATE INDEX idx_exams_share_code ON exams(share_code);
