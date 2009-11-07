INSERT INTO COLABORATOR(id,email,creation_date) values (1,'test@wisemapping.org',CURDATE());
INSERT INTO USER (colaborator_id,username,firstname, lastname, password, activationCode,activation_date,allowSendEmail)
values(1,'Wise Mapping Test User','Wise','test', 'test',1237,CURDATE(),1);
COMMIT;
SHUTDOWN;