create TABLE IF NOT EXISTS COLLABORATOR (
  id            SERIAL       NOT NULL PRIMARY KEY,
  email         VARCHAR(255) NOT NULL UNIQUE,
  creation_date DATE
);

create TABLE IF NOT EXISTS ACCOUNT (
  authentication_type TEXT         NOT NULL,
  authenticator_uri   VARCHAR(255),
  collaborator_id      INTEGER      NOT NULL PRIMARY KEY,
  firstname           VARCHAR(255) NOT NULL,
  lastname            VARCHAR(255) NOT NULL,
  password            VARCHAR(255) NOT NULL,
  activation_code     BIGINT       NOT NULL,
  activation_date     DATE,
  allow_send_email    TEXT         NOT NULL DEFAULT 0,
  locale              VARCHAR(5),
  google_sync         BOOLEAN,
  sync_code           VARCHAR(255),
  google_token        VARCHAR(255),
  FOREIGN KEY (collaborator_id) REFERENCES COLLABORATOR (id) ON delete CASCADE ON update NO ACTION
);

create TABLE IF NOT EXISTS MINDMAP_LABEL (
  id              INTEGER            NOT NULL PRIMARY KEY,
  title           VARCHAR(255),
  creator_id      INTEGER            NOT NULL,
  parent_label_id INTEGER,
  color           VARCHAR(7)         NOT NULL,
  FOREIGN KEY (creator_id) REFERENCES ACCOUNT (collaborator_id)
);

create TABLE IF NOT EXISTS MINDMAP (
  id             SERIAL       NOT NULL PRIMARY KEY,
  title          VARCHAR(255) NOT NULL,
  description    VARCHAR(255),
  xml            BYTEA        NOT NULL,
  public         BOOL         NOT NULL DEFAULT FALSE,
  spam_detected  BOOL         NOT NULL DEFAULT FALSE,
  creation_date  TIMESTAMP,
  edition_date   TIMESTAMP,
  creator_id     INTEGER      NOT NULL,
  last_editor_id INTEGER      NOT NULL --,
--FOREIGN KEY(creator_id) REFERENCES "USER"(collaborator_id) ON DELETE CASCADE ON UPDATE NO ACTION
);

create TABLE IF NOT EXISTS R_LABEL_MINDMAP (
  mindmap_id       INTEGER            NOT NULL,
  label_id         INTEGER            NOT NULL,
  PRIMARY KEY (mindmap_id, label_id),
  FOREIGN KEY (mindmap_id) REFERENCES MINDMAP (id),
  FOREIGN KEY (label_id) REFERENCES MINDMAP_LABEL (id) ON delete CASCADE ON update NO ACTION
);

create TABLE IF NOT EXISTS MINDMAP_HISTORY
(id            SERIAL  NOT NULL PRIMARY KEY,
 xml           BYTEA   NOT NULL,
 mindmap_id    INTEGER NOT NULL,
 creation_date TIMESTAMP,
 editor_id     INTEGER NOT NULL,
  FOREIGN KEY (mindmap_id) REFERENCES MINDMAP (id) ON delete CASCADE ON update NO ACTION
);

create TABLE IF NOT EXISTS COLLABORATION_PROPERTIES (
  id                 SERIAL NOT NULL PRIMARY KEY,
  starred            BOOL   NOT NULL DEFAULT FALSE,
  mindmap_properties VARCHAR(512)
);

create TABLE IF NOT EXISTS COLLABORATION (
  id             SERIAL  NOT NULL PRIMARY KEY,
  collaborator_id INTEGER NOT NULL,
  properties_id  INTEGER NOT NULL,
  mindmap_id     INTEGER NOT NULL,
  role_id        INTEGER NOT NULL,
  FOREIGN KEY (collaborator_id) REFERENCES COLLABORATOR (id),
  FOREIGN KEY (mindmap_id) REFERENCES MINDMAP (id) ON delete CASCADE ON update NO ACTION,
  FOREIGN KEY (properties_id) REFERENCES COLLABORATION_PROPERTIES (id) ON delete CASCADE ON update NO ACTION
);

create TABLE IF NOT EXISTS ACCESS_AUDITORY (
  id         SERIAL  NOT NULL PRIMARY KEY,
  login_date DATE,
  user_id    INTEGER NOT NULL,
  FOREIGN KEY (user_id) REFERENCES ACCOUNT (collaborator_id) ON delete CASCADE ON update NO ACTION
);