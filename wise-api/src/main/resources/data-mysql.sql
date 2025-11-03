INSERT IGNORE INTO COLLABORATOR (id, email, creation_date) VALUES (1, 'test@wisemapping.org', CURRENT_DATE());
INSERT IGNORE INTO ACCOUNT (collaborator_id, firstname, lastname, password, activation_code, activation_date, allow_send_email,authentication_type)
  VALUES (1, 'Test', 'User', 'ENC:5baa61e4c9b93f3f0682250b6cf8331b7ee68fd8', 1237, CURRENT_DATE(), 1,'D');

INSERT IGNORE INTO COLLABORATOR (id, email, creation_date) VALUES (2, 'admin@wisemapping.org', CURRENT_DATE());
INSERT IGNORE INTO ACCOUNT (collaborator_id, firstname, lastname, password, activation_code, activation_date, allow_send_email,authentication_type)
  VALUES (2, 'Admin', 'User', 'ENC:6c69d1e41a95462be1ff01decc9c4d4022c6a082', 1237, CURRENT_DATE(), 1,'D');
