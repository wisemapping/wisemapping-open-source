CREATE DATABASE wisemapping CHARACTER SET='utf8' COLLATE='utf8_unicode_ci'; 
CREATE USER 'wisemapping'@'localhost' IDENTIFIED BY 'password'; 
GRANT ALL ON wisemapping.* TO 'wisemapping'@'localhost';
