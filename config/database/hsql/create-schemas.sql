CREATE TABLE COLLABORATOR (
  id            INTEGER      NOT NULL IDENTITY,
  email         VARCHAR(255) NOT NULL,
  creation_date DATE
);

CREATE TABLE USER (
  colaborator_id      INTEGER      NOT NULL IDENTITY,
  authentication_type CHAR(1)      NOT NULL,
  authenticator_uri   VARCHAR(255) NULL,
  firstname           VARCHAR(255) NOT NULL,
  lastname            VARCHAR(255) NOT NULL,
  password            VARCHAR(255) NOT NULL,
  activation_code     BIGINT       NOT NULL,
  activation_date     DATE,
  allow_send_email    CHAR(1)      NOT NULL,
  locale              VARCHAR(5),
  FOREIGN KEY (colaborator_id) REFERENCES COLLABORATOR (id)
);

CREATE TABLE MINDMAP (
  id             INTEGER       NOT NULL IDENTITY,
  title          VARCHAR(255)  NOT NULL,
  description    VARCHAR(255)  NOT NULL,
  xml            LONGVARBINARY NOT NULL,
  public         BOOLEAN       NOT NULL,
  creation_date  DATETIME,
  edition_date   DATETIME,
  creator_id     INTEGER       NOT NULL,
  tags           VARCHAR(1014),
  last_editor_id INTEGER       NOT NULL
--FOREIGN KEY(creator_id) REFERENCES USER(colaborator_id)
);

CREATE TABLE LABEL (
  id              INTEGER            NOT NULL PRIMARY KEY IDENTITY,
  title           VARCHAR(30),
  creator_id      INTEGER            NOT NULL,
  parent_label_id INTEGER,
  color           VARCHAR(7)         NOT NULL,
  iconName        VARCHAR(50)       NOT NULL
  --FOREIGN KEY (creator_id) REFERENCES USER (colaborator_id)
);

CREATE TABLE R_LABEL_MINDMAP (
  mindmap_id       INTEGER            NOT NULL,
  label_id         INTEGER            NOT NULL,
  PRIMARY KEY (mindmap_id, label_id),
  FOREIGN KEY (mindmap_id) REFERENCES MINDMAP (id),
  FOREIGN KEY (label_id) REFERENCES LABEL (id) ON DELETE CASCADE ON UPDATE NO ACTION
);

CREATE TABLE MINDMAP_HISTORY (
 id            INTEGER       NOT NULL IDENTITY,
 xml           LONGVARBINARY NOT NULL,
 mindmap_id    INTEGER       NOT NULL,
 creation_date DATETIME,
 editor_id     INTEGER       NOT NULL,
 FOREIGN KEY (mindmap_id) REFERENCES MINDMAP (id)
);

CREATE TABLE COLLABORATION_PROPERTIES (
 id                 INTEGER NOT NULL IDENTITY,
 starred            BOOLEAN NOT NULL,
 mindmap_properties VARCHAR(512)
);

CREATE TABLE COLLABORATION (
 id             INTEGER NOT NULL IDENTITY,
 colaborator_id INTEGER NOT NULL,
 properties_id  INTEGER NOT NULL,
 mindmap_id     INTEGER NOT NULL,
 role_id        INTEGER NOT NULL,
  FOREIGN KEY (colaborator_id) REFERENCES COLLABORATOR (id),
  FOREIGN KEY (mindmap_id) REFERENCES MINDMAP (id),
  FOREIGN KEY (properties_id) REFERENCES COLLABORATION_PROPERTIES (id)
);


CREATE TABLE TAG (
 id      INTEGER      NOT NULL IDENTITY,
 name    VARCHAR(255) NOT NULL,
 user_id INTEGER      NOT NULL,
--FOREIGN KEY(user_id) REFERENCES USER(colaborator_id)
);

CREATE TABLE ACCESS_AUDITORY (
  id         INTEGER NOT NULL IDENTITY,
  user_id    INTEGER NOT NULL,
  login_date DATE,
  FOREIGN KEY (user_id) REFERENCES USER (colaborator_id)
    ON DELETE CASCADE
    ON UPDATE NO ACTION
);

COMMIT;
