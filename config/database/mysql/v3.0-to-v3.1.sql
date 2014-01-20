ALTER TABLE `USER` ADD COLUMN `authentication_type` CHAR(1) CHARACTER SET utf8 NOT NULL DEFAULT 'D'
AFTER `colaborator_id`;

ALTER TABLE `USER` ADD COLUMN `authenticator_uri` VARCHAR(255) CHARACTER SET utf8
AFTER `authentication_type`;

CREATE TABLE DIRECTORY (
  id             INTEGER           NOT NULL PRIMARY KEY AUTO_INCREMENT,
  name           VARCHAR(255)
                 CHARACTER SET utf8 NOT NULL,
  description    VARCHAR(255)
                 CHARACTER SET utf8 NOT NULL,
  creator_id     INTEGER            NOT NULL,
  FOREIGN KEY (creator_id) REFERENCES USER (colaborator_id)
    ON DELETE CASCADE
    ON UPDATE NO ACTION
)
  CHARACTER SET utf8;

ALTER TABLE `MINDMAP` ADD COLUMN `directory_id` INTEGER NOT NULL DEFAULT '/'
AFTER `last_editor_id`