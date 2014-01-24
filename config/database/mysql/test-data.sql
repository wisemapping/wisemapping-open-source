INSERT INTO COLLABORATOR (id, email, creation_date) VALUES (1, 'test@wisemapping.org', CURRENT_DATE());
INSERT INTO USER (colaborator_id, firstname, lastname, password, activation_code, activation_date, allow_send_email,authentication_type)
  VALUES (1, 'Test', 'User', 'ENC:a94a8fe5ccb19ba61c4c0873d391e987982fbbd3', 1237, CURRENT_DATE(), 1,'D');

INSERT INTO COLLABORATOR (id, email, creation_date) VALUES (2, 'admin@wisemapping.org', CURRENT_DATE());
INSERT INTO USER (colaborator_id, firstname, lastname, password, activation_code, activation_date, allow_send_email,authentication_type)
  VALUES (2, 'Admin', 'User', 'admin', 1237, CURRENT_DATE(), 1, 'D');

INSERT INTO COLLABORATOR (id, email, creation_date) VALUES (3, 'homer@wisemapping.org', CURRENT_DATE());
INSERT INTO USER (colaborator_id, firstname, lastname, password, activation_code, activation_date, allow_send_email,authentication_type)
  VALUES (3, 'Homer', 'Simpson', 'homer', 1237, CURRENT_DATE(), 1, 'D');

INSERT INTO COLLABORATOR (id, email, creation_date) VALUES (4, 'marge@wisemapping.org', CURRENT_DATE());
INSERT INTO USER (colaborator_id, firstname, lastname, password, activation_code, activation_date, allow_send_email,authentication_type)
  VALUES (4, 'Marge', 'Bouvier', 'marge', 1237, CURRENT_DATE(), 1, 'D');

INSERT INTO COLLABORATOR (id, email, creation_date) VALUES (5, 'bart@wisemapping.org', CURRENT_DATE());
INSERT INTO USER (colaborator_id, firstname, lastname, password, activation_code, activation_date, allow_send_email,authentication_type)
  VALUES (5, 'Bart', 'Simpson', 'bart', 1237, CURRENT_DATE(), 1, 'D');

INSERT INTO COLLABORATOR (id, email, creation_date) VALUES (6, 'lisa@wisemapping.org', CURRENT_DATE());
INSERT INTO USER (colaborator_id, firstname, lastname, password, activation_code, activation_date, allow_send_email,authentication_type)
  VALUES (6, 'Lisa', 'Simpson', 'lisa', 1237, CURRENT_DATE(), 1, 'D');

COMMIT;
