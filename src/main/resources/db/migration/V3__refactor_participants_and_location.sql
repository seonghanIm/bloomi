-- V3: Refactor participants and location
-- 1. Remove companion column (replaced by meal_participants table)
-- 2. Change location to free text (VARCHAR(100))
-- 3. Create meal_participants table (1:N relationship)

-- Remove companion column
ALTER TABLE meal_records DROP COLUMN IF EXISTS companion;

-- Change location column to support free text (expand size)
ALTER TABLE meal_records ALTER COLUMN location TYPE VARCHAR(100);

-- Update location comment
COMMENT ON COLUMN meal_records.location IS 'Meal location as free text (e.g., 화곡동 서연이네, Jidong place)';

-- Create meal_participants table
CREATE TABLE IF NOT EXISTS meal_participants (
    id VARCHAR(36) PRIMARY KEY,
    meal_record_id VARCHAR(36) NOT NULL,
    name VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_meal_participants_meal_record
        FOREIGN KEY (meal_record_id)
        REFERENCES meal_records(id)
        ON DELETE CASCADE
);

-- Index for faster lookups
CREATE INDEX IF NOT EXISTS idx_meal_participants_meal_record_id
    ON meal_participants(meal_record_id);

-- Add comments
COMMENT ON TABLE meal_participants IS 'Meal participants - people who shared the meal';
COMMENT ON COLUMN meal_participants.name IS 'Participant name (e.g., 나, 천지동, 김정윤)';