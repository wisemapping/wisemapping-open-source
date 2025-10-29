SET DATABASE SQL SYNTAX MYS TRUE;
-- Configure HSQLDB to be case sensitive for unquoted identifiers
SET DATABASE SQL NAMES FALSE;
SET DATABASE SQL REGULAR NAMES FALSE;

-- Initialize test users - INSERT IGNORE ensures idempotency
INSERT IGNORE INTO COLLABORATOR (id, email, creation_date) VALUES (1, 'test@wisemapping.org', CURDATE());
INSERT IGNORE INTO ACCOUNT (collaborator_id, firstname, lastname, password, activation_code, activation_date, allow_send_email, authentication_type, suspended)  
VALUES (1, 'Test', 'User', 'ENC:5baa61e4c9b93f3f0682250b6cf8331b7ee68fd8', 1237, CURDATE(), TRUE, 'D', FALSE);

INSERT IGNORE INTO COLLABORATOR (id, email, creation_date) VALUES (2, 'admin@wisemapping.org', CURDATE());
INSERT IGNORE INTO ACCOUNT (collaborator_id, firstname, lastname, password, activation_code, activation_date, allow_send_email, authentication_type, suspended)  
VALUES (2, 'Admin', 'User', 'ENC:6c69d1e41a95462be1ff01decc9c4d4022c6a082', 1237, CURDATE(), TRUE, 'D', FALSE);