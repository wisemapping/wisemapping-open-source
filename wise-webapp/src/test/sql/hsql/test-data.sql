INSERT INTO COLLABORATOR(id,email,creation_date) values (1,'test@wisemapping.org',CURDATE());
INSERT INTO USER (colaborator_id,firstname, lastname, password, activation_code,activation_date,allow_send_email)
values(1,'Test','User', 'ENC:a94a8fe5ccb19ba61c4c0873d391e987982fbbd3',1237,CURDATE(),1);

INSERT INTO COLLABORATOR(id,email,creation_date) values (2,'admin@wisemapping.org',CURDATE());
INSERT INTO USER (colaborator_id,firstname, lastname, password, activation_code,activation_date,allow_send_email)
values(2,'Admin','User', 'admin',1237,CURDATE(),1);


COMMIT;
SHUTDOWN;