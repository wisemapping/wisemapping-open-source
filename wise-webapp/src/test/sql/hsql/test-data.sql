INSERT INTO COLABORATOR(id,email,creation_date) values (1,'test@wisemapping.org',CURDATE());
INSERT INTO USER (colaborator_id,username,firstname, lastname, password, activationCode,activation_date,allowSendEmail)
values(1,'WiseMapping Test User','Wise','test', 'ENC:a94a8fe5ccb19ba61c4c0873d391e987982fbbd3',1237,CURDATE(),1);
COMMIT;
SHUTDOWN;