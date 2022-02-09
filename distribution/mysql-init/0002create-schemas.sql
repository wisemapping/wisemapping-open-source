#
# Command: mysql -u root -p < create_schemas.sql
#

USE wisemapping;

CREATE TABLE COLLABORATOR (
  id            INTEGER            NOT NULL PRIMARY KEY AUTO_INCREMENT,
  email         VARCHAR(255)
                CHARACTER SET utf8 NOT NULL UNIQUE,
  creation_date DATE
)
  CHARACTER SET utf8;

CREATE TABLE USER (
  colaborator_id   INTEGER            NOT NULL PRIMARY KEY,
  authentication_type CHAR(1)
                      CHARACTER SET utf8 NOT NULL,
  authenticator_uri   VARCHAR(255)
                      CHARACTER SET utf8,
  firstname        VARCHAR(255) CHARACTER SET utf8 NOT NULL,
  lastname         VARCHAR(255) CHARACTER SET utf8 NOT NULL,
  password         VARCHAR(255) CHARACTER SET utf8 NOT NULL,
  activation_code  BIGINT(20)         NOT NULL,
  activation_date  DATE,
  allow_send_email CHAR(1) CHARACTER SET utf8 NOT NULL DEFAULT 0,
  locale           VARCHAR(5),
  FOREIGN KEY (colaborator_id) REFERENCES COLLABORATOR (id)
    ON DELETE CASCADE
    ON UPDATE NO ACTION
)
  CHARACTER SET utf8;

CREATE TABLE MINDMAP (
  id             INTEGER            NOT NULL PRIMARY KEY AUTO_INCREMENT,
  title          VARCHAR(255)
                 CHARACTER SET utf8 NOT NULL,
  description    VARCHAR(255)
                 CHARACTER SET utf8 NOT NULL,
  xml            MEDIUMBLOB         NOT NULL,
  public         BOOL               NOT NULL DEFAULT 0,
  creation_date  DATETIME,
  edition_date   DATETIME,
  creator_id     INTEGER            NOT NULL,
  tags           VARCHAR(1014)
                 CHARACTER SET utf8,
  last_editor_id INTEGER            NOT NULL,
  FOREIGN KEY (creator_id) REFERENCES USER (colaborator_id)
    ON DELETE CASCADE
    ON UPDATE NO ACTION
)
  CHARACTER SET utf8;

CREATE TABLE LABEL (
  id              INTEGER            NOT NULL PRIMARY KEY AUTO_INCREMENT,
  title           VARCHAR(30)
                  CHARACTER SET utf8 NOT NULL,
  creator_id      INTEGER            NOT NULL,
  parent_label_id INTEGER,
  color           VARCHAR(7)         NOT NULL,
  iconName        VARCHAR(50)       NOT NULL,
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

CREATE TABLE MINDMAP_HISTORY
(id            INTEGER    NOT NULL PRIMARY KEY AUTO_INCREMENT,
 xml           MEDIUMBLOB NOT NULL,
 mindmap_id    INTEGER    NOT NULL,
 creation_date DATETIME,
 editor_id     INTEGER    NOT NULL,
  FOREIGN KEY (mindmap_id) REFERENCES MINDMAP (id)
    ON DELETE CASCADE
    ON UPDATE NO ACTION
)
  CHARACTER SET utf8;

CREATE TABLE COLLABORATION_PROPERTIES (
  id                 INTEGER NOT NULL PRIMARY KEY AUTO_INCREMENT,
  starred            BOOL    NOT NULL DEFAULT 0,
  mindmap_properties VARCHAR(512)
                     CHARACTER SET utf8
)
  CHARACTER SET utf8;

CREATE TABLE COLLABORATION (
  id             INTEGER NOT NULL PRIMARY KEY AUTO_INCREMENT,
  colaborator_id INTEGER NOT NULL,
  properties_id  INTEGER NOT NULL,
  mindmap_id     INTEGER NOT NULL,
  role_id        INTEGER NOT NULL,
  FOREIGN KEY (colaborator_id) REFERENCES COLLABORATOR (id),
  FOREIGN KEY (mindmap_id) REFERENCES MINDMAP (id)
    ON DELETE CASCADE
    ON UPDATE NO ACTION,
  FOREIGN KEY (properties_id) REFERENCES COLLABORATION_PROPERTIES (id)
    ON DELETE CASCADE
    ON UPDATE NO ACTION
)
  CHARACTER SET utf8;

CREATE TABLE TAG (
  id      INTEGER            NOT NULL PRIMARY KEY AUTO_INCREMENT,
  name    VARCHAR(255)
          CHARACTER SET utf8 NOT NULL,
  user_id INTEGER            NOT NULL,
  FOREIGN KEY (user_id) REFERENCES USER (colaborator_id)
    ON DELETE CASCADE
    ON UPDATE NO ACTION
)
  CHARACTER SET utf8;

CREATE TABLE ACCESS_AUDITORY (
  id         INTEGER NOT NULL PRIMARY KEY AUTO_INCREMENT,
  login_date DATE,
  user_id    INTEGER NOT NULL,
  FOREIGN KEY (user_id) REFERENCES USER (colaborator_id)
    ON DELETE CASCADE
    ON UPDATE NO ACTION
)
  CHARACTER SET utf8;

COMMIT;