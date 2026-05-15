CREATE TABLE IF NOT EXISTS collaborator (
  id            INTEGER  NOT NULL PRIMARY KEY AUTO_INCREMENT,
  email         VARCHAR(255) CHARACTER SET UTF8MB4 NOT NULL UNIQUE,
  creation_date DATETIME
)
CHARACTER SET UTF8MB4;

CREATE TABLE IF NOT EXISTS account (
  collaborator_id   INTEGER            NOT NULL PRIMARY KEY,
  authentication_type CHAR(1)
                      CHARACTER SET UTF8MB4 NOT NULL,
  authenticator_uri   VARCHAR(255)
                      CHARACTER SET UTF8MB4,
  firstname        VARCHAR(255) CHARACTER SET UTF8MB4 NOT NULL,
  lastname         VARCHAR(255) CHARACTER SET UTF8MB4 NOT NULL,
  password         VARCHAR(255) CHARACTER SET UTF8MB4 NOT NULL,
  activation_code  BIGINT(20)         NOT NULL,
  activation_date  DATETIME,
  allow_send_email BOOL NOT NULL DEFAULT 0,
  locale           VARCHAR(5) CHARACTER SET UTF8MB4,
  oauth_sync	   BOOL,
  sync_code        VARCHAR(255) CHARACTER SET UTF8MB4,
  oauth_token      MEDIUMTEXT CHARACTER SET UTF8MB4,
  reset_password_token        VARCHAR(64) CHARACTER SET UTF8MB4,
  reset_password_token_expiry DATETIME,
  suspended BOOL NOT NULL DEFAULT 0,
  suspended_date DATETIME,
  suspension_reason CHAR(1) CHARACTER SET UTF8MB4,
  FOREIGN KEY (collaborator_id) REFERENCES collaborator (id)
    ON DELETE CASCADE
    ON UPDATE NO ACTION
) CHARACTER SET UTF8MB4;

CREATE TABLE IF NOT EXISTS mindmap (
  id             INTEGER            NOT NULL PRIMARY KEY AUTO_INCREMENT,
  title          VARCHAR(255)
                 CHARACTER SET UTF8MB4 NOT NULL,
  description    MEDIUMTEXT
                 CHARACTER SET UTF8MB4,
  public         BOOL               NOT NULL DEFAULT 0,
  creation_date  DATETIME,
  edition_date   DATETIME,
  creator_id     INTEGER            NOT NULL,
  last_editor_id INTEGER            NOT NULL,
  FOREIGN KEY (creator_id) REFERENCES account (collaborator_id)
    ON DELETE CASCADE
    ON UPDATE NO ACTION
)
  CHARACTER SET UTF8MB4;

CREATE TABLE IF NOT EXISTS mindmap_xml (
  mindmap_id INTEGER    NOT NULL PRIMARY KEY,
  xml        MEDIUMBLOB NOT NULL,
  FOREIGN KEY (mindmap_id) REFERENCES mindmap (id)
    ON DELETE CASCADE
    ON UPDATE NO ACTION
)
  CHARACTER SET UTF8MB4;

CREATE TABLE IF NOT EXISTS mindmap_spam_info (
  mindmap_id            INTEGER            NOT NULL PRIMARY KEY,
  spam_detected         BOOL               NOT NULL DEFAULT 0,
  spam_description      MEDIUMTEXT         CHARACTER SET UTF8MB4,
  spam_detection_version INTEGER           NOT NULL DEFAULT 0,
  spam_type_code        CHAR(1)            CHARACTER SET UTF8MB4,
  created_at            DATETIME           NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at            DATETIME           NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (mindmap_id) REFERENCES mindmap (id)
    ON DELETE CASCADE
    ON UPDATE NO ACTION
)
  CHARACTER SET UTF8MB4;

CREATE TABLE IF NOT EXISTS mindmap_label (
  id              INTEGER            NOT NULL PRIMARY KEY AUTO_INCREMENT,
  title           VARCHAR(255)
                  CHARACTER SET UTF8MB4 NOT NULL,
  creator_id      INTEGER            NOT NULL,
  parent_label_id INTEGER,
  color           VARCHAR(7)         CHARACTER SET UTF8MB4 NOT NULL,
  FOREIGN KEY (creator_id) REFERENCES account (collaborator_id),
  FOREIGN KEY (parent_label_id) REFERENCES mindmap_label (id)
    ON DELETE CASCADE
    ON UPDATE NO ACTION
)
  CHARACTER SET UTF8MB4;

CREATE TABLE IF NOT EXISTS r_label_mindmap (
  mindmap_id       INTEGER            NOT NULL,
  label_id         INTEGER            NOT NULL,
  PRIMARY KEY (mindmap_id, label_id),
  FOREIGN KEY (mindmap_id) REFERENCES mindmap (id),
  FOREIGN KEY (label_id) REFERENCES mindmap_label (id)
    ON DELETE CASCADE
    ON UPDATE NO ACTION
)
  CHARACTER SET UTF8MB4;

CREATE TABLE IF NOT EXISTS mindmap_history
(id            INTEGER    NOT NULL PRIMARY KEY AUTO_INCREMENT,
 xml           MEDIUMBLOB NOT NULL,
 mindmap_id    INTEGER    NOT NULL,
 creation_date DATETIME,
 editor_id     INTEGER    NOT NULL,
  FOREIGN KEY (mindmap_id) REFERENCES mindmap (id)
    ON DELETE CASCADE
    ON UPDATE NO ACTION
)
  CHARACTER SET UTF8MB4;

CREATE TABLE IF NOT EXISTS collaboration_properties (
  id                 INTEGER NOT NULL PRIMARY KEY AUTO_INCREMENT,
  starred            BOOL    NOT NULL DEFAULT 0,
  mindmap_properties MEDIUMTEXT CHARACTER SET UTF8MB4
)
  CHARACTER SET UTF8MB4;

CREATE TABLE IF NOT EXISTS collaboration (
  id             INTEGER NOT NULL PRIMARY KEY AUTO_INCREMENT,
  collaborator_id INTEGER NOT NULL,
  properties_id  INTEGER,
  mindmap_id     INTEGER NOT NULL,
  role_id        SMALLINT NOT NULL,
  FOREIGN KEY (collaborator_id) REFERENCES collaborator (id),
  FOREIGN KEY (mindmap_id) REFERENCES mindmap (id)
    ON DELETE CASCADE
    ON UPDATE NO ACTION,
  FOREIGN KEY (properties_id) REFERENCES collaboration_properties (id)
    ON DELETE CASCADE
    ON UPDATE NO ACTION
)
  CHARACTER SET UTF8MB4;

CREATE TABLE IF NOT EXISTS access_auditory (
  id         INTEGER NOT NULL PRIMARY KEY AUTO_INCREMENT,
  login_date DATETIME,
  user_id    INTEGER NOT NULL,
  FOREIGN KEY (user_id) REFERENCES account (collaborator_id)
    ON DELETE CASCADE
    ON UPDATE NO ACTION
)
CHARACTER SET UTF8MB4;

CREATE TABLE IF NOT EXISTS mindmap_inactive_user (
  id                 INTEGER        NOT NULL PRIMARY KEY AUTO_INCREMENT,
  original_mindmap_id INTEGER      NOT NULL,
  creation_date      DATETIME,
  edition_date       DATETIME,
  creator_id         INTEGER,
  last_editor_id     INTEGER,
  description        MEDIUMTEXT CHARACTER SET UTF8MB4,
  public             BOOL          NOT NULL DEFAULT 0,
  title              VARCHAR(255) CHARACTER SET UTF8MB4,
  xml                MEDIUMBLOB NOT NULL,
  migration_date     DATETIME,
  migration_reason   VARCHAR(255) CHARACTER SET UTF8MB4
) CHARACTER SET UTF8MB4;
