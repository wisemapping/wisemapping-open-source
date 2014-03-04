CREATE TABLE COLLABORATOR (
  id            SERIAL       NOT NULL PRIMARY KEY,
  email         VARCHAR(255) NOT NULL UNIQUE,
  creation_date DATE
);

CREATE TABLE "user" (
  authentication_type TEXT         NOT NULL,
  authenticator_uri   VARCHAR(255),
  colaborator_id      INTEGER      NOT NULL PRIMARY KEY,
  firstname           VARCHAR(255) NOT NULL,
  lastname            VARCHAR(255) NOT NULL,
  password            VARCHAR(255) NOT NULL,
  activation_code     BIGINT       NOT NULL,
  activation_date     DATE,
  allow_send_email    TEXT         NOT NULL DEFAULT 0,
  locale              VARCHAR(5),
  FOREIGN KEY (colaborator_id) REFERENCES COLLABORATOR (id) ON DELETE CASCADE ON UPDATE NO ACTION
);

CREATE TABLE LABEL (
  id              INTEGER            NOT NULL PRIMARY KEY,
  title           VARCHAR(255),
  creator_id      INTEGER            NOT NULL,
  parent_label_id INTEGER,
  color           VARCHAR(7)         NOT NULL
  --FOREIGN KEY (creator_id) REFERENCES USER (colaborator_id)
);

CREATE TABLE R_LABEL_MINDMAP (
  mindmap_id       INTEGER            NOT NULL,
  label_id         INTEGER            NOT NULL,
  PRIMARY KEY (mindmap_id, label_id),
  FOREIGN KEY (mindmap_id) REFERENCES MINDMAP (id),
  FOREIGN KEY (label_id) REFERENCES LABEL (id) ON DELETE CASCADE ON UPDATE NO ACTION
);

CREATE TABLE MINDMAP (
  id             SERIAL       NOT NULL PRIMARY KEY,
  title          VARCHAR(255) NOT NULL,
  description    VARCHAR(255) NOT NULL,
  xml            BYTEA        NOT NULL,
  public         BOOL         NOT NULL DEFAULT FALSE,
  creation_date  TIMESTAMP,
  edition_date   TIMESTAMP,
  creator_id     INTEGER      NOT NULL,
  tags           VARCHAR(1014),
  last_editor_id INTEGER      NOT NULL --,
--FOREIGN KEY(creator_id) REFERENCES "USER"(colaborator_id) ON DELETE CASCADE ON UPDATE NO ACTION
);


CREATE TABLE MINDMAP_HISTORY
(id            SERIAL  NOT NULL PRIMARY KEY,
 xml           BYTEA   NOT NULL,
 mindmap_id    INTEGER NOT NULL,
 creation_date TIMESTAMP,
 editor_id     INTEGER NOT NULL,
  FOREIGN KEY (mindmap_id) REFERENCES MINDMAP (id) ON DELETE CASCADE ON UPDATE NO ACTION
);


CREATE TABLE COLLABORATION_PROPERTIES (
  id                 SERIAL NOT NULL PRIMARY KEY,
  starred            BOOL   NOT NULL DEFAULT FALSE,
  mindmap_properties VARCHAR(512)
);

CREATE TABLE COLLABORATION (
  id             SERIAL  NOT NULL PRIMARY KEY,
  colaborator_id INTEGER NOT NULL,
  properties_id  INTEGER NOT NULL,
  mindmap_id     INTEGER NOT NULL,
  role_id        INTEGER NOT NULL,
  FOREIGN KEY (colaborator_id) REFERENCES COLLABORATOR (id),
  FOREIGN KEY (mindmap_id) REFERENCES MINDMAP (id) ON DELETE CASCADE ON UPDATE NO ACTION,
  FOREIGN KEY (properties_id) REFERENCES COLLABORATION_PROPERTIES (id) ON DELETE CASCADE ON UPDATE NO ACTION
);

CREATE TABLE TAG (
  id      SERIAL       NOT NULL PRIMARY KEY,
  name    VARCHAR(255) NOT NULL,
  user_id INTEGER      NOT NULL --,
--FOREIGN KEY(user_id) REFERENCES "USER"(colaborator_id) ON DELETE CASCADE ON UPDATE NO ACTION
);


CREATE TABLE ACCESS_AUDITORY (
  id         SERIAL  NOT NULL PRIMARY KEY,
  login_date DATE,
  user_id    INTEGER NOT NULL,
  FOREIGN KEY (user_id) REFERENCES "user" (colaborator_id) ON DELETE CASCADE ON UPDATE NO ACTION
);


COMMIT;
