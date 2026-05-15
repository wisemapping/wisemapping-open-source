CREATE TABLE IF NOT EXISTS collaborator (
  id            SERIAL       NOT NULL PRIMARY KEY,
  email         VARCHAR(255) NOT NULL UNIQUE,
  creation_date TIMESTAMP
);

CREATE TABLE IF NOT EXISTS account (
  collaborator_id      INTEGER      NOT NULL PRIMARY KEY,
  authentication_type CHAR(1)      NOT NULL,
  authenticator_uri   VARCHAR(255),
  firstname           VARCHAR(255) NOT NULL,
  lastname            VARCHAR(255) NOT NULL,
  password            VARCHAR(255) NOT NULL,
  activation_code     BIGINT       NOT NULL,
  activation_date     TIMESTAMP,
  allow_send_email    BOOL         NOT NULL DEFAULT FALSE,
  locale              VARCHAR(5),
  oauth_sync          BOOL,
  sync_code           VARCHAR(255),
  oauth_token         TEXT,
  reset_password_token        VARCHAR(64),
  reset_password_token_expiry TIMESTAMP,
  suspended           BOOL         NOT NULL DEFAULT FALSE,
  suspended_date      TIMESTAMP,
  suspension_reason   CHAR(1),
  FOREIGN KEY (collaborator_id) REFERENCES collaborator (id) ON DELETE CASCADE ON UPDATE NO ACTION
);

CREATE TABLE IF NOT EXISTS mindmap_label (
  id              SERIAL             NOT NULL PRIMARY KEY,
  title           VARCHAR(255)       NOT NULL,
  creator_id      INTEGER            NOT NULL,
  parent_label_id INTEGER,
  color           VARCHAR(7)         NOT NULL,
  FOREIGN KEY (creator_id) REFERENCES account (collaborator_id),
  FOREIGN KEY (parent_label_id) REFERENCES mindmap_label (id) ON DELETE CASCADE ON UPDATE NO ACTION
);

CREATE TABLE IF NOT EXISTS mindmap (
  id             SERIAL       NOT NULL PRIMARY KEY,
  title          VARCHAR(255) NOT NULL,
  description    TEXT,
  public         BOOL         NOT NULL DEFAULT FALSE,
  creation_date  TIMESTAMP,
  edition_date   TIMESTAMP,
  creator_id     INTEGER      NOT NULL,
  last_editor_id INTEGER      NOT NULL,
  FOREIGN KEY (creator_id) REFERENCES account (collaborator_id) ON DELETE CASCADE ON UPDATE NO ACTION
);

CREATE TABLE IF NOT EXISTS mindmap_xml (
  mindmap_id INTEGER NOT NULL PRIMARY KEY,
  xml        BYTEA   NOT NULL,
  FOREIGN KEY (mindmap_id) REFERENCES mindmap (id) ON DELETE CASCADE ON UPDATE NO ACTION
);

CREATE TABLE IF NOT EXISTS mindmap_spam_info (
  mindmap_id            INTEGER      NOT NULL PRIMARY KEY,
  spam_detected         BOOL         NOT NULL DEFAULT FALSE,
  spam_description      TEXT,
  spam_detection_version INTEGER     NOT NULL DEFAULT 0,
  spam_type_code        CHAR(1),
  created_at            TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at            TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (mindmap_id) REFERENCES mindmap (id) ON DELETE CASCADE ON UPDATE NO ACTION
);

CREATE TABLE IF NOT EXISTS r_label_mindmap (
  mindmap_id       INTEGER            NOT NULL,
  label_id         INTEGER            NOT NULL,
  PRIMARY KEY (mindmap_id, label_id),
  FOREIGN KEY (mindmap_id) REFERENCES mindmap (id) ON DELETE CASCADE ON UPDATE NO ACTION,
  FOREIGN KEY (label_id) REFERENCES mindmap_label (id) ON DELETE CASCADE ON UPDATE NO ACTION
);

CREATE TABLE IF NOT EXISTS mindmap_history
(id            SERIAL  NOT NULL PRIMARY KEY,
 xml           BYTEA   NOT NULL,
 mindmap_id    INTEGER NOT NULL,
 creation_date TIMESTAMP,
 editor_id     INTEGER NOT NULL,
  FOREIGN KEY (mindmap_id) REFERENCES mindmap (id) ON DELETE CASCADE ON UPDATE NO ACTION
);

CREATE TABLE IF NOT EXISTS collaboration_properties (
  id                 SERIAL NOT NULL PRIMARY KEY,
  starred            BOOL   NOT NULL DEFAULT FALSE,
  mindmap_properties TEXT
);

CREATE TABLE IF NOT EXISTS collaboration (
  id             SERIAL  NOT NULL PRIMARY KEY,
  collaborator_id INTEGER NOT NULL,
  properties_id  INTEGER,
  mindmap_id     INTEGER NOT NULL,
  role_id        SMALLINT NOT NULL,
  FOREIGN KEY (collaborator_id) REFERENCES collaborator (id) ON DELETE CASCADE ON UPDATE NO ACTION,
  FOREIGN KEY (mindmap_id) REFERENCES mindmap (id) ON DELETE CASCADE ON UPDATE NO ACTION,
  FOREIGN KEY (properties_id) REFERENCES collaboration_properties (id) ON DELETE CASCADE ON UPDATE NO ACTION
);

CREATE TABLE IF NOT EXISTS access_auditory (
  id         SERIAL  NOT NULL PRIMARY KEY,
  login_date TIMESTAMP,
  user_id    INTEGER NOT NULL,
  FOREIGN KEY (user_id) REFERENCES account (collaborator_id) ON DELETE CASCADE ON UPDATE NO ACTION
);

CREATE TABLE IF NOT EXISTS mindmap_inactive_user (
  id                 SERIAL        PRIMARY KEY,
  original_mindmap_id INTEGER      NOT NULL,
  creation_date      TIMESTAMP,
  edition_date       TIMESTAMP,
  creator_id         INTEGER,
  last_editor_id     INTEGER,
  description        TEXT,
  public             BOOL          NOT NULL DEFAULT FALSE,
  title              VARCHAR(255),
  xml                BYTEA NOT NULL,
  migration_date     TIMESTAMP,
  migration_reason   VARCHAR(255)
)
