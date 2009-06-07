CREATE TABLE COLABORATOR (
id INTEGER NOT NULL PRIMARY KEY AUTO_INCREMENT,
email varchar(255) CHARACTER SET utf8  NOT NULL UNIQUE,
creation_date date
) CHARACTER SET utf8;

CREATE TABLE USER (
id INTEGER NOT NULL PRIMARY KEY AUTO_INCREMENT,
colaborator_id INTEGER NOT NULL,
username varchar(255) CHARACTER SET utf8  NOT NULL,
firstname varchar(255) CHARACTER SET utf8 NOT NULL,
lastname varchar(255) CHARACTER SET utf8 NOT NULL,
password varchar(255) CHARACTER SET utf8 NOT NULL,
activationCode BIGINT(20) NOT NULL,
activation_date date,
allowSendEmail char(1) CHARACTER SET utf8 NOT NULL default 0,
FOREIGN KEY(colaborator_id) REFERENCES colaborator(id)
) CHARACTER SET utf8 ;

CREATE TABLE MINDMAP (
id INTEGER NOT NULL PRIMARY KEY AUTO_INCREMENT,
title varchar(255) CHARACTER SET utf8 NOT NULL,
description varchar(255) CHARACTER SET utf8 NOT NULL,
xml blob NOT NULL,
public BOOL not null default 0,
mindMapNative_id INTEGER NOT NULL default 0,
creation_date date,
edition_date date,
owner_id INTEGER not null,
tags varchar(1014) CHARACTER SET utf8 ,
last_editor varchar(255) CHARACTER SET utf8 ,
creator_user varchar(255) CHARACTER SET utf8 ,
editor_properties varchar(512) CHARACTER SET utf8 ,
FOREIGN KEY(owner_id) REFERENCES user(colaborator_id)
) CHARACTER SET utf8 ;

CREATE TABLE MINDMAP_NATIVE
(
id INTEGER NOT NULL PRIMARY KEY AUTO_INCREMENT,
svg_xml blob,
vml_xml blob
) CHARACTER SET utf8 ;

CREATE TABLE MINDMAP_HISTORY
(
id INTEGER NOT NULL PRIMARY KEY AUTO_INCREMENT,
xml blob NOT NULL,
mindmap_id INTEGER NOT NULL,
creation_date datetime,
creator_user varchar(255) CHARACTER SET utf8
) CHARACTER SET utf8 ;

CREATE TABLE MINDMAP_COLABORATOR (
id INTEGER NOT NULL PRIMARY KEY AUTO_INCREMENT,
colaborator_id INTEGER NOT NULL,
mindmap_id INTEGER NOT NULL,
role_id INTEGER NOT NULL,
FOREIGN KEY(colaborator_id) REFERENCES colaborator(id),
FOREIGN KEY(mindmap_id) REFERENCES mindmap(id)
) CHARACTER SET utf8 ;

CREATE TABLE TAG(
id INTEGER NOT NULL PRIMARY KEY AUTO_INCREMENT,
name varchar(255) CHARACTER SET utf8 NOT NULL,
user_id INTEGER NOT NULL,
FOREIGN KEY(user_id) REFERENCES user(colaborator_id)
) CHARACTER SET utf8 ;

CREATE TABLE USER_LOGIN (
id INTEGER NOT NULL PRIMARY KEY AUTO_INCREMENT,
email varchar(255) CHARACTER SET utf8 ,
login_date date
) CHARACTER SET utf8 ;
COMMIT;