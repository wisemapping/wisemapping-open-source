SET DATABASE SQL SYNTAX MYS TRUE;
-- Configure HSQLDB to be case sensitive for unquoted identifiers
SET DATABASE SQL NAMES FALSE;
SET DATABASE SQL REGULAR NAMES FALSE;

INSERT IGNORE INTO COLLABORATOR (id, email, creation_date) VALUES (1, 'test@wisemapping.org', CURDATE());
INSERT IGNORE INTO ACCOUNT (collaborator_id, firstname, lastname, password, activation_code, activation_date, allow_send_email, authentication_type, suspended)  
VALUES (1, 'Test', 'User', 'ENC:a94a8fe5ccb19ba61c4c0873d391e987982fbbd3', 1237, CURDATE(), 1, 'D', FALSE);

INSERT IGNORE INTO COLLABORATOR (id, email, creation_date) VALUES (2, 'admin@wisemapping.org', CURDATE());
INSERT IGNORE INTO ACCOUNT (collaborator_id, firstname, lastname, password, activation_code, activation_date, allow_send_email, authentication_type, suspended)  
VALUES (2, 'Admin', 'User', 'ENC:a94a8fe5ccb19ba61c4c0873d391e987982fbbd3', 1237, CURDATE(), 1, 'D', FALSE);