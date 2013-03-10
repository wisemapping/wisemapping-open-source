CREATE TABLE COLLABORATOR (
id SERIAL NOT NULL PRIMARY KEY,
email varchar(255)  NOT NULL UNIQUE,
creation_date date
);

CREATE TABLE "user" (
id SERIAL NOT NULL PRIMARY KEY,
colaborator_id INTEGER NOT NULL,
firstname varchar(255) NOT NULL,
lastname varchar(255) NOT NULL,
password varchar(255) NOT NULL,
activation_code BIGINT NOT NULL,
activation_date date,
allow_send_email text NOT NULL default 0,
locale varchar(5),
FOREIGN KEY(colaborator_id) REFERENCES COLLABORATOR(id) ON DELETE CASCADE ON UPDATE NO ACTION
) ;


CREATE TABLE MINDMAP (
id SERIAL NOT NULL PRIMARY KEY,
title varchar(255) NOT NULL,
description varchar(255) NOT NULL,
xml bytea NOT NULL,
public BOOL not null default FALSE,
creation_date timestamp,
edition_date timestamp,
creator_id INTEGER not null,
tags varchar(1014) ,
last_editor_id INTEGER NOT NULL --,
--FOREIGN KEY(creator_id) REFERENCES "USER"(colaborator_id) ON DELETE CASCADE ON UPDATE NO ACTION
) ;


CREATE TABLE MINDMAP_HISTORY
(id SERIAL NOT NULL PRIMARY KEY,
xml bytea NOT NULL,
mindmap_id INTEGER NOT NULL,
creation_date timestamp,
editor_id INTEGER NOT NULL,
FOREIGN KEY(mindmap_id) REFERENCES MINDMAP(id) ON DELETE CASCADE ON UPDATE NO ACTION
) ;


CREATE TABLE COLLABORATION_PROPERTIES(
id SERIAL NOT NULL PRIMARY KEY,
starred BOOL NOT NULL default FALSE,
mindmap_properties varchar(512)
);

CREATE TABLE COLLABORATION (
id SERIAL NOT NULL PRIMARY KEY,
colaborator_id INTEGER NOT NULL,
properties_id INTEGER NOT NULL,
mindmap_id INTEGER NOT NULL,
role_id INTEGER NOT NULL,
FOREIGN KEY(colaborator_id) REFERENCES COLLABORATOR(id),
FOREIGN KEY(mindmap_id) REFERENCES MINDMAP(id) ON DELETE CASCADE ON UPDATE NO ACTION,
FOREIGN KEY(properties_id) REFERENCES COLLABORATION_PROPERTIES(id) ON DELETE CASCADE ON UPDATE NO ACTION
) ;

CREATE TABLE TAG(
id SERIAL NOT NULL PRIMARY KEY,
name varchar(255) NOT NULL,
user_id INTEGER NOT NULL --,
--FOREIGN KEY(user_id) REFERENCES "USER"(colaborator_id) ON DELETE CASCADE ON UPDATE NO ACTION
) ;


CREATE TABLE ACCESS_AUDITORY (
id SERIAL NOT NULL PRIMARY KEY,
login_date date,
user_id INTEGER NOT NULL
) ;


COMMIT;
