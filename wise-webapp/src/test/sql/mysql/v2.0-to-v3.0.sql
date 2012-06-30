CREATE TABLE COLLABORATION_PROPERTIES(
id INTEGER NOT NULL PRIMARY KEY AUTO_INCREMENT,
starred BOOL NOT NULL default 0,
mindmap_properties varchar(512) CHARACTER SET utf8
) CHARACTER SET utf8;

drop table `wisemapping`.`MINDMAP_NATIVE`
ALTER TABLE `wisemapping`.`mindmap_colaborator` RENAME TO  `wisemapping`.`COLLABORATION` ;
ALTER TABLE `wisemapping`.`colaborator` RENAME TO  `wisemapping`.`collaborator` ;

ALTER TABLE `wisemapping`.`mindmap` DROP COLUMN `editor_properties` , DROP COLUMN `mindMapNative_id` ;

ALTER TABLE `wisemapping`.`mindmap` CHANGE COLUMN `owner_id` `creator_id` INT(11) NOT NULL
, DROP INDEX `owner_id`
, ADD INDEX `owner_id` (`creator_id` ASC) ;
ALTER TABLE `wisemapping`.`collaboration` ADD COLUMN `properties_id` INT(11) NULL DEFAULT NULL  AFTER `role_id` ;
DROP TABLE USER_LOGIN;

CREATE TABLE ACCESS_AUDITORY (
id INTEGER NOT NULL PRIMARY KEY AUTO_INCREMENT,
user_id INTEGER NOT NULL,
login_date date
) CHARACTER SET utf8 ;

ALTER TABLE ACCESS_AUDITORY
  ADD CONSTRAINT `user_id`
  FOREIGN KEY ()
  REFERENCES `wisemapping`.`USER` ()
  ON DELETE CASCADE
  ON UPDATE NO ACTION
, ADD INDEX `user_id` () ;


ALTER TABLE `wisemapping`.`user` ADD COLUMN `locale` VARCHAR(5) NULL  AFTER `allowSendEmail` ;

# INSERT INTO `wisemapping`.`collaborator` (`id`, `email`, `creation_date`) VALUES (8081, 'fake@wisemapping.com', '2007-10-09');
# DELETE FROM `wisemapping`.`USER` where activation_date is null;
# DROP TABLE FEEDBACK;

