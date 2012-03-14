CREATE TABLE COLABORATOR (
id INTEGER NOT NULL IDENTITY,
email varchar(255) NOT NULL,
creation_date date);

CREATE TABLE USER (
id INTEGER NOT NULL IDENTITY,
colaborator_id INTEGER NOT NULL,
username varchar(255)   NOT NULL,
firstname varchar(255)  NOT NULL,
lastname varchar(255)  NOT NULL,
password varchar(255)  NOT NULL,
activationCode BIGINT NOT NULL,
activation_date DATE,
allowSendEmail CHAR(1) NOT NULL,
FOREIGN KEY(colaborator_id) REFERENCES colaborator(id)
);

CREATE TABLE MINDMAP (
id INTEGER NOT NULL IDENTITY,
title VARCHAR(255)  NOT NULL,
description VARCHAR(255)  NOT NULL,
xml LONGVARBINARY NOT NULL,
public BOOLEAN not null,
creation_date date,
edition_date date,
owner_id INTEGER not null,
tags varchar(1014)  ,
last_editor varchar(255)  ,
creator_user varchar(255)  ,
editor_properties varchar(512)
--FOREIGN KEY(owner_id) REFERENCES USER(colaborator_id)
);

CREATE TABLE MINDMAP_HISTORY
(id INTEGER NOT NULL IDENTITY,
xml LONGVARBINARY NOT NULL,
mindmap_id INTEGER NOT NULL,
creation_date datetime,
creator_user varchar(255));

CREATE TABLE MINDMAP_COLABORATOR
(id INTEGER NOT NULL IDENTITY,
colaborator_id INTEGER NOT NULL,
mindmap_id INTEGER NOT NULL,
role_id INTEGER NOT NULL,
FOREIGN KEY(colaborator_id) REFERENCES colaborator(id),
FOREIGN KEY(mindmap_id) REFERENCES mindmap(id)
);

CREATE TABLE TAG
(id INTEGER NOT NULL IDENTITY,
name varchar(255)  NOT NULL,
user_id INTEGER NOT NULL,
--FOREIGN KEY(user_id) REFERENCES user(colaborator_id)
);

CREATE TABLE USER_LOGIN
(id INTEGER NOT NULL IDENTITY,
email varchar(255),
login_date date);
COMMIT;
SHUTDOWN;