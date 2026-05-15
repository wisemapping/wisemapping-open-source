CREATE TABLE IF NOT EXISTS collaborator (
  id            INTEGER      NOT NULL IDENTITY,
  email         VARCHAR(255) NOT NULL UNIQUE,
  creation_date DATETIME
);

CREATE TABLE IF NOT EXISTS account (
  collaborator_id      INTEGER      NOT NULL PRIMARY KEY,
  authentication_type CHAR(1)      NOT NULL,
  authenticator_uri   VARCHAR(255),
  firstname           VARCHAR(255) NOT NULL,
  lastname            VARCHAR(255) NOT NULL,
  password            VARCHAR(255) NOT NULL,
  activation_code     BIGINT       NOT NULL,
  activation_date     DATETIME,
  allow_send_email    BOOLEAN      DEFAULT FALSE NOT NULL,
  locale              VARCHAR(5),
  oauth_sync		  BOOLEAN,
  sync_code           VARCHAR(255),
  oauth_token         LONGVARCHAR,
  reset_password_token        VARCHAR(64),
  reset_password_token_expiry DATETIME,
  suspended           BOOLEAN       DEFAULT FALSE NOT NULL,
  suspended_date      DATETIME,
  suspension_reason   CHAR(1),
  FOREIGN KEY (collaborator_id) REFERENCES collaborator (id) ON DELETE CASCADE ON UPDATE NO ACTION
);

CREATE TABLE IF NOT EXISTS mindmap (
  id             INTEGER       NOT NULL IDENTITY,
  title          VARCHAR(255)  NOT NULL,
  description    LONGVARCHAR,
  public         BOOLEAN       DEFAULT FALSE NOT NULL,
  creation_date  DATETIME,
  edition_date   DATETIME,
  creator_id     INTEGER       NOT NULL,
  last_editor_id INTEGER       NOT NULL,
  FOREIGN KEY (creator_id) REFERENCES account (collaborator_id) ON DELETE CASCADE ON UPDATE NO ACTION
);

CREATE TABLE IF NOT EXISTS mindmap_xml (
  mindmap_id INTEGER       NOT NULL PRIMARY KEY,
  xml        LONGVARBINARY NOT NULL,
  FOREIGN KEY (mindmap_id) REFERENCES mindmap (id) ON DELETE CASCADE ON UPDATE NO ACTION
);

CREATE TABLE IF NOT EXISTS mindmap_spam_info (
  mindmap_id            INTEGER       NOT NULL PRIMARY KEY,
  spam_detected         BOOLEAN       DEFAULT FALSE NOT NULL,
  spam_description      LONGVARCHAR,
  spam_detection_version INTEGER      DEFAULT 0 NOT NULL,
  spam_type_code        CHAR(1),
  created_at            DATETIME,
  updated_at            DATETIME,
  FOREIGN KEY (mindmap_id) REFERENCES mindmap (id) ON DELETE CASCADE ON UPDATE NO ACTION
);

CREATE TABLE IF NOT EXISTS mindmap_label (
  id              INTEGER            NOT NULL PRIMARY KEY IDENTITY,
  title           VARCHAR(255)       NOT NULL,
  creator_id      INTEGER            NOT NULL,
  parent_label_id INTEGER,
  color           VARCHAR(7)         NOT NULL,
  FOREIGN KEY (creator_id) REFERENCES account (collaborator_id),
  FOREIGN KEY (parent_label_id) REFERENCES mindmap_label (id) ON DELETE CASCADE ON UPDATE NO ACTION
);

CREATE TABLE IF NOT EXISTS r_label_mindmap (
  mindmap_id       INTEGER            NOT NULL,
  label_id         INTEGER            NOT NULL,
  PRIMARY KEY (mindmap_id, label_id),
  FOREIGN KEY (mindmap_id) REFERENCES mindmap (id) ON DELETE CASCADE ON UPDATE NO ACTION,
  FOREIGN KEY (label_id) REFERENCES mindmap_label (id) ON DELETE CASCADE ON UPDATE NO ACTION
);

CREATE TABLE IF NOT EXISTS mindmap_history (
 id            INTEGER       NOT NULL IDENTITY,
 xml           LONGVARBINARY NOT NULL,
 mindmap_id    INTEGER       NOT NULL,
 creation_date DATETIME,
 editor_id     INTEGER       NOT NULL,
 FOREIGN KEY (mindmap_id) REFERENCES mindmap (id) ON DELETE CASCADE ON UPDATE NO ACTION
);

CREATE TABLE IF NOT EXISTS collaboration_properties (
 id                 INTEGER NOT NULL IDENTITY,
 starred            BOOLEAN DEFAULT FALSE NOT NULL,
 mindmap_properties LONGVARCHAR
);

CREATE TABLE IF NOT EXISTS collaboration (
 id             INTEGER NOT NULL IDENTITY,
 collaborator_id INTEGER NOT NULL,
 properties_id  INTEGER,
 mindmap_id     INTEGER NOT NULL,
 role_id        SMALLINT NOT NULL,
  FOREIGN KEY (collaborator_id) REFERENCES collaborator (id) ON DELETE CASCADE ON UPDATE NO ACTION,
  FOREIGN KEY (mindmap_id) REFERENCES mindmap (id) ON DELETE CASCADE ON UPDATE NO ACTION,
  FOREIGN KEY (properties_id) REFERENCES collaboration_properties (id) ON DELETE CASCADE ON UPDATE NO ACTION
);

CREATE TABLE IF NOT EXISTS access_auditory (
  id         INTEGER NOT NULL IDENTITY,
  user_id    INTEGER NOT NULL,
  login_date DATETIME,
  FOREIGN KEY (user_id) REFERENCES account (collaborator_id) ON DELETE CASCADE ON UPDATE NO ACTION
);

CREATE TABLE IF NOT EXISTS mindmap_inactive_user (
  id                 INTEGER       NOT NULL IDENTITY,
  original_mindmap_id INTEGER      NOT NULL,
  creation_date      DATETIME,
  edition_date       DATETIME,
  creator_id         INTEGER,
  last_editor_id     INTEGER,
  description        LONGVARCHAR,
  public             BOOLEAN      DEFAULT FALSE NOT NULL,
  title              VARCHAR(255),
  xml                LONGVARBINARY NOT NULL,
  migration_date     DATETIME,
  migration_reason   VARCHAR(255)
);
