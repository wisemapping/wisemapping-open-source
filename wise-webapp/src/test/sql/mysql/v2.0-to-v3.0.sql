CREATE TABLE COLLABORATION_PROPERTIES(
id INTEGER NOT NULL PRIMARY KEY AUTO_INCREMENT,
starred BOOL NOT NULL default 0,
mindmap_properties varchar(512) CHARACTER SET utf8
) CHARACTER SET utf8;

drop table `wisemapping`.`MINDMAP_NATIVE`;
ALTER TABLE `wisemapping`.`MINDMAP_COLABORATOR` RENAME TO  `wisemapping`.`COLLABORATION`;
ALTER TABLE `wisemapping`.`COLABORATOR` RENAME TO  `wisemapping`.`COLLABORATOR`;

ALTER TABLE `wisemapping`.`MINDMAP` DROP COLUMN `editor_properties` , DROP COLUMN `mindMapNative_id` ;

ALTER TABLE `wisemapping`.`MINDMAP` CHANGE COLUMN `owner_id` `creator_id` INT(11) NOT NULL
, DROP INDEX `owner_id`
, ADD INDEX `owner_id` (`creator_id` ASC) ;

ALTER TABLE `wisemapping`.`COLLABORATION` ADD COLUMN `properties_id` INT(11) NULL DEFAULT NULL  AFTER `role_id` ;
DROP TABLE USER_LOGIN;

CREATE TABLE ACCESS_AUDITORY (
id INTEGER NOT NULL PRIMARY KEY AUTO_INCREMENT,
user_id INTEGER NOT NULL,
login_date date
) CHARACTER SET utf8 ;

#ALTER TABLE ACCESS_AUDITORY
#  ADD CONSTRAINT `user_id`
#  FOREIGN KEY ()
#  REFERENCES `wisemapping`.`USER` ()
#  ON DELETE CASCADE
#  ON UPDATE NO ACTION
#, ADD INDEX `user_id` () ;

ALTER TABLE `wisemapping`.`MINDMAP_HISTORY` DROP COLUMN `creator_user` , ADD COLUMN `editor_id` INT(11) NULL DEFAULT NULL  AFTER `creation_date`;

ALTER TABLE `wisemapping`.`USER` ADD COLUMN `locale` VARCHAR(5) NULL  AFTER `allowSendEmail` ;

ALTER TABLE `wisemapping`.`MINDMAP` DROP COLUMN `last_editor` , ADD COLUMN `last_editor_id` INT(11) NULL DEFAULT 9  AFTER `tags` ;

ALTER TABLE `wisemapping`.`USER` DROP COLUMN `username` , CHANGE COLUMN `activationCode` `activation_code` BIGINT(20) NOT NULL  , CHANGE COLUMN `allowSendEmail` `allow_send_email` CHAR(1) NOT NULL DEFAULT '0'  ;

INSERT INTO `wisemapping`.`MINDMAP` (`last_editor_id`) VALUES (1);

INSERT INTO `wisemapping`.`COLLABORATOR` (`id`, `email`, `creation_date`) VALUES (8081, 'migfake@wisemapping.com', '2007-10-09');
DELETE FROM `wisemapping`.`USER` where activation_date is null;
DROP TABLE FEEDBACK;

