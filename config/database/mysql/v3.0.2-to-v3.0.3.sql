ALTER TABLE `USER` DROP COLUMN `id`;

ALTER TABLE `ACCESS_AUDITORY`
  ADD CONSTRAINT
  FOREIGN KEY (user_id) REFERENCES USER (colaborator_id)
    ON DELETE CASCADE
    ON UPDATE NO ACTION;

CREATE TABLE LABEL (
  id              INTEGER            NOT NULL PRIMARY KEY AUTO_INCREMENT,
  title           VARCHAR(30)
                  CHARACTER SET utf8 NOT NULL,
  creator_id      INTEGER            NOT NULL,
  parent_label_id INTEGER,
  color           VARCHAR(7)         NOT NULL,
  FOREIGN KEY (creator_id) REFERENCES USER (colaborator_id),
  FOREIGN KEY (parent_label_id) REFERENCES LABEL (id)
    ON DELETE CASCADE
    ON UPDATE NO ACTION
)
  CHARACTER SET utf8;

CREATE TABLE R_LABEL_MINDMAP (
  mindmap_id       INTEGER            NOT NULL,
  label_id         INTEGER            NOT NULL,
  PRIMARY KEY (mindmap_id, label_id),
  FOREIGN KEY (mindmap_id) REFERENCES MINDMAP (id),
  FOREIGN KEY (label_id) REFERENCES LABEL (id)
    ON DELETE CASCADE
    ON UPDATE NO ACTION
)
  CHARACTER SET utf8;

ALTER TABLE `LABEL`
    ADD COLUMN iconName VARCHAR(50) NOT NULL;

UPDATE LABEL SET iconName = 'glyphicon glyphicon-tag';
