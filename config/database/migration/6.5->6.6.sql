-- Migration script to rename google_sync to oauth_sync and google_token to oauth_token
-- Version: 6.5 -> 6.6

-- MySQL
ALTER TABLE ACCOUNT CHANGE COLUMN google_sync oauth_sync BOOL;
ALTER TABLE ACCOUNT CHANGE COLUMN google_token oauth_token VARCHAR(255);

-- PostgreSQL
-- ALTER TABLE ACCOUNT RENAME COLUMN google_sync TO oauth_sync;
-- ALTER TABLE ACCOUNT RENAME COLUMN google_token TO oauth_token;

-- HSQLDB
-- ALTER TABLE ACCOUNT ALTER COLUMN google_sync RENAME TO oauth_sync;
-- ALTER TABLE ACCOUNT ALTER COLUMN google_token RENAME TO oauth_token;
