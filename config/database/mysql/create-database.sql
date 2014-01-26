#
# Command: mysql -u root -p < create_database.sql
#
DROP DATABASE IF EXISTS wisemapping;

CREATE DATABASE IF NOT EXISTS wisemapping
  CHARACTER SET = 'utf8'
  COLLATE = 'utf8_unicode_ci';
GRANT ALL ON wisemapping.* TO 'wisemapping'@'localhost';
SET PASSWORD FOR 'wisemapping'@'localhost' = PASSWORD('password');