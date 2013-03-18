ALTER TABLE `USER` ADD COLUMN `authentication_type` CHAR(1) CHARACTER SET utf8 NOT NULL DEFAULT 'D'
AFTER `colaborator_id`;

ALTER TABLE `USER` ADD COLUMN `authenticator_uri` VARCHAR(255) CHARACTER SET utf8
AFTER `authentication_type`;