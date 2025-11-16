-- Migration 6.7: Extract XML blob from MINDMAP to a separate table
-- Purpose: keep the main table slim and guarantee lazy loading of XML payloads

-- Create the new storage table
BEGIN;
  CREATE TABLE IF NOT EXISTS MINDMAP_XML (
    mindmap_id INTEGER       NOT NULL PRIMARY KEY,
    xml        LONGVARBINARY NOT NULL,
    FOREIGN KEY (mindmap_id) REFERENCES MINDMAP (id)
      ON DELETE CASCADE
      ON UPDATE NO ACTION
  );
COMMIT;

-- Copy existing XML payloads
BEGIN;
  INSERT INTO MINDMAP_XML (mindmap_id, xml)
  SELECT id, xml
  FROM MINDMAP;
COMMIT;

-- Remove the old column from MINDMAP
BEGIN;
  ALTER TABLE MINDMAP DROP COLUMN xml;
COMMIT;
