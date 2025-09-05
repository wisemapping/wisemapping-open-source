CREATE TABLE IF NOT EXISTS COLLABORATOR (
  id            INTEGER      NOT NULL IDENTITY,
  email         VARCHAR(255) NOT NULL UNIQUE,
  creation_date DATE
);

CREATE TABLE IF NOT EXISTS ACCOUNT (
  collaborator_id      INTEGER      NOT NULL IDENTITY,
  authentication_type CHAR(1)      NOT NULL,
  authenticator_uri   VARCHAR(255) NULL,
  firstname           VARCHAR(255) NOT NULL,
  lastname            VARCHAR(255) NOT NULL,
  password            VARCHAR(255) NOT NULL,
  activation_code     BIGINT       NOT NULL,
  activation_date     DATE,
  allow_send_email    CHAR(1)      NOT NULL,
  locale              VARCHAR(5),
  google_sync		  BOOLEAN,
  sync_code           VARCHAR(255),
  google_token        VARCHAR(255),
  suspended           BOOLEAN       NOT NULL DEFAULT FALSE,
  suspended_date      DATETIME,
  suspension_reason   CHAR(1),
  suspension_end_date DATETIME,
  FOREIGN KEY (collaborator_id) REFERENCES COLLABORATOR (id)
);

CREATE TABLE IF NOT EXISTS MINDMAP (
  id             INTEGER       NOT NULL IDENTITY,
  title          VARCHAR(255)  NOT NULL,
  description    VARCHAR(255),
  xml            LONGVARBINARY NOT NULL,
  public         BOOLEAN       NOT NULL,
  spam_detected  BOOLEAN       NOT NULL,
  spam_description LONGVARCHAR,
  creation_date  DATETIME,
  edition_date   DATETIME,
  creator_id     INTEGER       NOT NULL,
  last_editor_id INTEGER       NOT NULL
--FOREIGN KEY(creator_id) REFERENCES ACCOUNT(collaborator_id)
);

CREATE TABLE IF NOT EXISTS MINDMAP_LABEL (
  id              INTEGER            NOT NULL PRIMARY KEY IDENTITY,
  title           VARCHAR(30),
  creator_id      INTEGER            NOT NULL,
  parent_label_id INTEGER,
  color           VARCHAR(7)         NOT NULL,
  --FOREIGN KEY (creator_id) REFERENCES ACCOUNT (collaborator_id)
);

CREATE TABLE IF NOT EXISTS R_LABEL_MINDMAP (
  mindmap_id       INTEGER            NOT NULL,
  label_id         INTEGER            NOT NULL,
  PRIMARY KEY (mindmap_id, label_id),
  FOREIGN KEY (mindmap_id) REFERENCES MINDMAP (id),
  FOREIGN KEY (label_id) REFERENCES MINDMAP_LABEL (id) ON DELETE CASCADE ON UPDATE NO ACTION
);

CREATE TABLE IF NOT EXISTS MINDMAP_HISTORY (
 id            INTEGER       NOT NULL IDENTITY,
 xml           LONGVARBINARY NOT NULL,
 mindmap_id    INTEGER       NOT NULL,
 creation_date DATETIME,
 editor_id     INTEGER       NOT NULL,
 FOREIGN KEY (mindmap_id) REFERENCES MINDMAP (id)
);

CREATE TABLE IF NOT EXISTS COLLABORATION_PROPERTIES (
 id                 INTEGER NOT NULL IDENTITY,
 starred            BOOLEAN NOT NULL,
 mindmap_properties VARCHAR(512)
);

CREATE TABLE IF NOT EXISTS COLLABORATION (
 id             INTEGER NOT NULL IDENTITY,
 collaborator_id INTEGER NOT NULL,
 properties_id  INTEGER NOT NULL,
 mindmap_id     INTEGER NOT NULL,
 role_id        INTEGER NOT NULL,
  FOREIGN KEY (collaborator_id) REFERENCES COLLABORATOR (id),
  FOREIGN KEY (mindmap_id) REFERENCES MINDMAP (id),
  FOREIGN KEY (properties_id) REFERENCES COLLABORATION_PROPERTIES (id)
);

CREATE TABLE IF NOT EXISTS ACCESS_AUDITORY (
  id         INTEGER NOT NULL IDENTITY,
  user_id    INTEGER NOT NULL,
  login_date DATE,
  FOREIGN KEY (user_id) REFERENCES ACCOUNT (collaborator_id)
    ON DELETE CASCADE
    ON UPDATE NO ACTION
);

