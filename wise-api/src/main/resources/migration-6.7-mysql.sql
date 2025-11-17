-- Migration 6.7: Move MINDMAP XML payload to dedicated table
-- Goal: ensure XML blobs are stored separately and fetched lazily through JPA

START TRANSACTION;

CREATE TABLE IF NOT EXISTS MINDMAP_XML (
  mindmap_id INTEGER    NOT NULL PRIMARY KEY,
  xml        MEDIUMBLOB NOT NULL,
  CONSTRAINT fk_mindmap_xml FOREIGN KEY (mindmap_id) REFERENCES MINDMAP (id)
    ON DELETE CASCADE
    ON UPDATE NO ACTION
) CHARACTER SET UTF8MB4;

COMMIT;

-- Copy existing XML values into the new table (idempotent thanks to ON DUPLICATE KEY)
INSERT INTO MINDMAP_XML (mindmap_id, xml)
SELECT id, xml
FROM MINDMAP
ON DUPLICATE KEY UPDATE xml = VALUES(xml);

-- Drop XML column from MINDMAP now that the data was migrated
ALTER TABLE MINDMAP DROP COLUMN IF EXISTS xml;
