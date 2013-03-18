INSERT INTO COLLABORATOR (id, email, creation_date) VALUES (1, 'test@wisemapping.org', CURDATE());
INSERT INTO USER (colaborator_id, firstname, lastname, password, activation_code, activation_date, allow_send_email)
  VALUES (1, 'Test', 'User', 'ENC:a94a8fe5ccb19ba61c4c0873d391e987982fbbd3', 1237, CURDATE(), 1);

INSERT INTO COLLABORATOR (id, email, creation_date) VALUES (2, 'admin@wisemapping.org', CURDATE());
INSERT INTO USER (colaborator_id, firstname, lastname, password, activation_code, activation_date, allow_send_email)
  VALUES (2, 'Admin', 'User', 'admin', 1237, CURDATE(), 1);


COMMIT;
SHUTDOWN;