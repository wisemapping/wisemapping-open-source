
INSERT INTO COLLABORATOR (email, creation_date)
VALUES ('test@wisemapping.org', CURRENT_DATE)
ON CONFLICT DO NOTHING;

INSERT INTO ACCOUNT (collaborator_id, firstname, lastname, password, activation_code, activation_date, allow_send_email, authentication_type)
SELECT id, 'Test', 'User', 'ENC:5baa61e4c9b93f3f0682250b6cf8331b7ee68fd8', 1237, CURRENT_DATE, true, 'D'
FROM COLLABORATOR WHERE email = 'test@wisemapping.org'
ON CONFLICT DO NOTHING;

INSERT INTO COLLABORATOR (email, creation_date)
VALUES ('admin@wisemapping.org', CURRENT_DATE)
ON CONFLICT DO NOTHING;

INSERT INTO ACCOUNT (collaborator_id, firstname, lastname, password, activation_code, activation_date, allow_send_email, authentication_type)
SELECT id, 'Admin', 'User', 'ENC:6c69d1e41a95462be1ff01decc9c4d4022c6a082', 1237, CURRENT_DATE, true, 'D'
FROM COLLABORATOR WHERE email = 'admin@wisemapping.org'
ON CONFLICT DO NOTHING;

