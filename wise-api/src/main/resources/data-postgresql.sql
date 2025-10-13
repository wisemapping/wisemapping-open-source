
INSERT INTO COLLABORATOR (id, email, creation_date) VALUES (1, 'test@wisemapping.org', CURRENT_DATE)
ON CONFLICT DO NOTHING;

INSERT INTO ACCOUNT (collaborator_id, firstname, lastname, password, activation_code, activation_date, allow_send_email,authentication_type)
  VALUES (1, 'Test', 'User', 'ENC:a94a8fe5ccb19ba61c4c0873d391e987982fbbd3', 1237, CURRENT_DATE, true,'D')
ON CONFLICT DO NOTHING;

INSERT INTO COLLABORATOR (id, email, creation_date) VALUES (2, 'admin@wisemapping.org', CURRENT_DATE)
ON CONFLICT DO NOTHING;

INSERT INTO ACCOUNT (collaborator_id, firstname, lastname, password, activation_code, activation_date, allow_send_email,authentication_type)
  VALUES (2, 'Admin', 'User', 'ENC:a94a8fe5ccb19ba61c4c0873d391e987982fbbd3', 1237, CURRENT_DATE, true,'D')
ON CONFLICT DO NOTHING;

