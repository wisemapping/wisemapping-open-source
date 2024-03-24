#
# Command: mysql -u root -p < create-database.sql
#
DROP DATABASE IF EXISTS wisemapping;

CREATE DATABASE IF NOT EXISTS wisemapping
  CHARACTER SET = 'utf8'
  COLLATE = 'utf8_unicode_ci';

CREATE USER 'wisemapping'@'%' IDENTIFIED BY 'password';
GRANT ALL PRIVILEGES ON wisemapping.* TO 'wisemapping'@'%' WITH GRANT OPTION;
FLUSH PRIVILEGES;