INSERT INTO COLABORATOR(id,email,creation_date) values (1,'test@wisemapping.com',CURRENT_DATE());
INSERT INTO USER (colaborator_id,username,firstname, lastname, password, activationCode,activation_date,allowSendEmail)
values(1,'Wise Mapping Test User','Wise','Test', 'test',1237,CURRENT_DATE(),1);
COMMIT;
