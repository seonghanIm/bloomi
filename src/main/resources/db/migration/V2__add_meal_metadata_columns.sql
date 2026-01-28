-- Add meal metadata columns to meal_records table
-- V2: Add mealType, emotion, location, companion columns

ALTER TABLE meal_records ADD COLUMN IF NOT EXISTS meal_type VARCHAR(20);
ALTER TABLE meal_records ADD COLUMN IF NOT EXISTS emotion VARCHAR(20);
ALTER TABLE meal_records ADD COLUMN IF NOT EXISTS location VARCHAR(20);
ALTER TABLE meal_records ADD COLUMN IF NOT EXISTS companion VARCHAR(20);

-- Add comments for documentation
COMMENT ON COLUMN meal_records.meal_type IS 'Meal type: BREAKFAST, LUNCH, DINNER, SNACK';
COMMENT ON COLUMN meal_records.emotion IS 'Emotion during meal: HAPPY, SATISFIED, NORMAL, SAD, STRESSED, TIRED';
COMMENT ON COLUMN meal_records.location IS 'Meal location: HOME, OFFICE, RESTAURANT, CAFE, SCHOOL, OTHER';
COMMENT ON COLUMN meal_records.companion IS 'Dining companion: ALONE, FAMILY, FRIENDS, COLLEAGUES, PARTNER, OTHER';