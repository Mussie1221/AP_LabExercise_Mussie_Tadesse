-- Add image handling columns to messages table
ALTER TABLE messages
    ADD COLUMN image_data LONGBLOB NULL AFTER message_text,
    ADD COLUMN image_mime VARCHAR(255) NULL AFTER image_data,
    ADD COLUMN image_name VARCHAR(255) NULL AFTER image_mime;

-- Optional: If you need to adjust existing data types, you can modify as needed.
-- Ensure the columns are placed after message_text for clarity.
