-- Migration 6.7: Move MINDMAP XML payload to dedicated table
-- This keeps large blobs off the main table and makes lazy loading predictable

BEGIN;

CREATE TABLE IF NOT EXISTS MINDMAP_XML (
  mindmap_id INTEGER NOT NULL PRIMARY KEY,
  xml        BYTEA   NOT NULL,
  CONSTRAINT fk_mindmap_xml FOREIGN KEY (mindmap_id) REFERENCES MINDMAP (id)
    ON DELETE CASCADE
    ON UPDATE NO ACTION
);

COMMIT;

-- Copy existing XML payloads. ON CONFLICT makes the statement idempotent.
INSERT INTO MINDMAP_XML (mindmap_id, xml)
SELECT id, xml
FROM MINDMAP
ON CONFLICT (mindmap_id) DO UPDATE SET xml = EXCLUDED.xml;

-- Remove the legacy column
ALTER TABLE MINDMAP DROP COLUMN IF EXISTS xml;
