-- Migration script to move spam columns from MINDMAP to MINDMAP_SPAM_INFO table
-- Version: 6.4 -> 6.5

-- MySQL
-- Create the new MINDMAP_SPAM_INFO table
CREATE TABLE IF NOT EXISTS MINDMAP_SPAM_INFO (
  mindmap_id            INTEGER            NOT NULL PRIMARY KEY,
  spam_detected         BOOL               NOT NULL DEFAULT 0,
  spam_description      TEXT               CHARACTER SET UTF8MB4,
  spam_detection_version INTEGER           NOT NULL DEFAULT 0,
  spam_type_code        VARCHAR(50),
  created_at            DATETIME           NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at            DATETIME           NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (mindmap_id) REFERENCES MINDMAP (id)
    ON DELETE CASCADE
    ON UPDATE NO ACTION
)
  CHARACTER SET UTF8MB4;

-- Move data from MINDMAP to MINDMAP_SPAM_INFO
INSERT INTO MINDMAP_SPAM_INFO (mindmap_id, spam_detected, spam_description, spam_detection_version, created_at, updated_at)
SELECT id, spam_detected, spam_description, spam_detection_version, NOW(), NOW()
FROM MINDMAP
WHERE spam_detected = 1 OR spam_description IS NOT NULL OR spam_detection_version > 0;

-- Create index for better performance
CREATE INDEX idx_mindmap_spam_info_detected ON MINDMAP_SPAM_INFO(spam_detected);
CREATE INDEX idx_mindmap_spam_info_version ON MINDMAP_SPAM_INFO(spam_detection_version);

-- PostgreSQL  
-- CREATE TABLE IF NOT EXISTS MINDMAP_SPAM_INFO (
--   mindmap_id            INTEGER      NOT NULL PRIMARY KEY,
--   spam_detected         BOOL         NOT NULL DEFAULT FALSE,
--   spam_description      TEXT,
--   spam_detection_version INTEGER     NOT NULL DEFAULT 0,
--   spam_type_code        VARCHAR(50),
--   created_at            TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
--   updated_at            TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
--   FOREIGN KEY (mindmap_id) REFERENCES MINDMAP (id)
--     ON DELETE CASCADE
--     ON UPDATE NO ACTION
-- );
-- 
-- INSERT INTO MINDMAP_SPAM_INFO (mindmap_id, spam_detected, spam_description, spam_detection_version, created_at, updated_at)
-- SELECT id, spam_detected, spam_description, spam_detection_version, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
-- FROM MINDMAP
-- WHERE spam_detected = TRUE OR spam_description IS NOT NULL OR spam_detection_version > 0;
-- 
-- CREATE INDEX idx_mindmap_spam_info_detected ON MINDMAP_SPAM_INFO(spam_detected);
-- CREATE INDEX idx_mindmap_spam_info_version ON MINDMAP_SPAM_INFO(spam_detection_version);

-- HSQLDB
-- CREATE TABLE IF NOT EXISTS MINDMAP_SPAM_INFO (
--   mindmap_id            INTEGER       NOT NULL PRIMARY KEY,
--   spam_detected         BOOLEAN       NOT NULL DEFAULT FALSE,
--   spam_description      LONGVARCHAR,
--   spam_detection_version INTEGER      NOT NULL DEFAULT 0,
--   spam_type_code        VARCHAR(50),
--   created_at            DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
--   updated_at            DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
--   FOREIGN KEY (mindmap_id) REFERENCES MINDMAP (id)
--     ON DELETE CASCADE
--     ON UPDATE NO ACTION
-- );
-- 
-- INSERT INTO MINDMAP_SPAM_INFO (mindmap_id, spam_detected, spam_description, spam_detection_version, created_at, updated_at)
-- SELECT id, spam_detected, spam_description, spam_detection_version, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
-- FROM MINDMAP
-- WHERE spam_detected = TRUE OR spam_description IS NOT NULL OR spam_detection_version > 0;
-- 
-- CREATE INDEX idx_mindmap_spam_info_detected ON MINDMAP_SPAM_INFO(spam_detected);
-- CREATE INDEX idx_mindmap_spam_info_version ON MINDMAP_SPAM_INFO(spam_detection_version);
