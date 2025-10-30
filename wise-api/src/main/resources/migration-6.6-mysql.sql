-- Migration 6.6: Add indexes to MINDMAP_SPAM_INFO for query optimization
-- This migration improves performance for spam detection and filtering queries
-- Run this manually on existing databases only (not included in schema-mysql.sql)

-- Index for spam detection status filtering (admin queries)
-- Used by: getAllMindmaps(), searchMindmaps(), countAllMindmaps()
-- Benefit: Speeds up queries filtering by spam_detected status
CREATE INDEX idx_spam_detected ON MINDMAP_SPAM_INFO(spam_detected);

-- Index for batch processing version checks (spam detection batch jobs)
-- Used by: findPublicMindmapsNeedingSpamDetection(), countPublicMindmapsNeedingSpamDetection()
-- Benefit: Critical for batch job performance - finds mindmaps needing reprocessing
CREATE INDEX idx_spam_detection_version ON MINDMAP_SPAM_INFO(spam_detection_version);

-- Analyze table to update statistics after index creation
ANALYZE TABLE MINDMAP_SPAM_INFO;

