CREATE TABLE IF NOT EXISTS COLLABORATOR (
  id            INTEGER  NOT NULL PRIMARY KEY AUTO_INCREMENT,
  email         VARCHAR(255) CHARACTER SET UTF8MB4 NOT NULL UNIQUE,
  creation_date DATE
)
CHARACTER SET UTF8MB4;

CREATE TABLE IF NOT EXISTS ACCOUNT (
  collaborator_id   INTEGER            NOT NULL PRIMARY KEY,
  authentication_type CHAR(1)
                      CHARACTER SET UTF8MB4 NOT NULL,
  authenticator_uri   VARCHAR(255)
                      CHARACTER SET utf8,
  firstname        VARCHAR(255) CHARACTER SET UTF8MB4 NOT NULL,
  lastname         VARCHAR(255) CHARACTER SET UTF8MB4 NOT NULL,
  password         VARCHAR(255) CHARACTER SET UTF8MB4 NOT NULL,
  activation_code  BIGINT(20)         NOT NULL,
  activation_date  DATE,
  allow_send_email CHAR(1) CHARACTER SET UTF8MB4 NOT NULL DEFAULT 0,
  locale           VARCHAR(5),
  google_sync	   BOOL,
  sync_code        VARCHAR(255),
  google_token     VARCHAR(255),
  suspended BOOL NOT NULL DEFAULT 0,
  suspended_date DATETIME,
  suspension_reason CHAR(1),
  suspension_end_date DATETIME,
  FOREIGN KEY (collaborator_id) REFERENCES COLLABORATOR (id)
    ON DELETE CASCADE
    ON UPDATE NO ACTION
) CHARACTER SET UTF8MB4;

CREATE TABLE IF NOT EXISTS MINDMAP (
  id             INTEGER            NOT NULL PRIMARY KEY AUTO_INCREMENT,
  title          VARCHAR(255)
                 CHARACTER SET UTF8MB4 NOT NULL,
  description    VARCHAR(255)
                 CHARACTER SET utf8,
  xml            MEDIUMBLOB         NOT NULL,
  public         BOOL               NOT NULL DEFAULT 0,
  spam_detected  BOOL               NOT NULL DEFAULT 0,
  spam_description TEXT CHARACTER SET UTF8MB4,
  creation_date  DATETIME,
  edition_date   DATETIME,
  creator_id     INTEGER            NOT NULL,
  last_editor_id INTEGER            NOT NULL,
  FOREIGN KEY (creator_id) REFERENCES ACCOUNT (collaborator_id)
    ON DELETE CASCADE
    ON UPDATE NO ACTION
)
  CHARACTER SET UTF8MB4;

CREATE TABLE IF NOT EXISTS  MINDMAP_LABEL (
  id              INTEGER            NOT NULL PRIMARY KEY AUTO_INCREMENT,
  title           VARCHAR(30)
                  CHARACTER SET UTF8MB4 NOT NULL,
  creator_id      INTEGER            NOT NULL,
  parent_label_id INTEGER,
  color           VARCHAR(7)         NOT NULL,
  FOREIGN KEY (creator_id) REFERENCES ACCOUNT (collaborator_id),
  FOREIGN KEY (parent_label_id) REFERENCES MINDMAP_LABEL (id)
    ON DELETE CASCADE
    ON UPDATE NO ACTION
)
  CHARACTER SET UTF8MB4;

CREATE TABLE IF NOT EXISTS R_LABEL_MINDMAP (
  mindmap_id       INTEGER            NOT NULL,
  label_id         INTEGER            NOT NULL,
  PRIMARY KEY (mindmap_id, label_id),
  FOREIGN KEY (mindmap_id) REFERENCES MINDMAP (id),
  FOREIGN KEY (label_id) REFERENCES MINDMAP_LABEL (id)
    ON DELETE CASCADE
    ON UPDATE NO ACTION
)
  CHARACTER SET UTF8MB4;

CREATE TABLE IF NOT EXISTS MINDMAP_HISTORY
(id            INTEGER    NOT NULL PRIMARY KEY AUTO_INCREMENT,
 xml           MEDIUMBLOB NOT NULL,
 mindmap_id    INTEGER    NOT NULL,
 creation_date DATETIME,
 editor_id     INTEGER    NOT NULL,
  FOREIGN KEY (mindmap_id) REFERENCES MINDMAP (id)
    ON DELETE CASCADE
    ON UPDATE NO ACTION
)
  CHARACTER SET UTF8MB4;

CREATE TABLE IF NOT EXISTS COLLABORATION_PROPERTIES (
  id                 INTEGER NOT NULL PRIMARY KEY AUTO_INCREMENT,
  starred            BOOL    NOT NULL DEFAULT 0,
  mindmap_properties VARCHAR(512)
                     CHARACTER SET utf8
)
  CHARACTER SET UTF8MB4;

CREATE TABLE IF NOT EXISTS COLLABORATION (
  id             INTEGER NOT NULL PRIMARY KEY AUTO_INCREMENT,
  collaborator_id INTEGER NOT NULL,
  properties_id  INTEGER NOT NULL,
  mindmap_id     INTEGER NOT NULL,
  role_id        INTEGER NOT NULL,
  UNIQUE KEY UC_ROLE (mindmap_id,collaborator_id),
  FOREIGN KEY (collaborator_id) REFERENCES COLLABORATOR (id),
  FOREIGN KEY (mindmap_id) REFERENCES MINDMAP (id)
    ON DELETE CASCADE
    ON UPDATE NO ACTION,
  FOREIGN KEY (properties_id) REFERENCES COLLABORATION_PROPERTIES (id)
    ON DELETE CASCADE
    ON UPDATE NO ACTION
)
  CHARACTER SET UTF8MB4;

CREATE TABLE IF NOT EXISTS ACCESS_AUDITORY (
  id         INTEGER NOT NULL PRIMARY KEY AUTO_INCREMENT,
  login_date DATE,
  user_id    INTEGER NOT NULL,
  FOREIGN KEY (user_id) REFERENCES ACCOUNT (collaborator_id)
    ON DELETE CASCADE
    ON UPDATE NO ACTION
)
CHARACTER SET UTF8MB4;