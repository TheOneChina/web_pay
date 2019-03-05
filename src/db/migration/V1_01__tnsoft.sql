-- ------------------------------------------------------
-- Utilities / dictionary tables
-- ------------------------------------------------------
CREATE TABLE nda_domain (
  id SERIAL,
  name varchar(50),
  password bytea NOT NULL,
  display_name varchar(150),
  description varchar(2000),
  email varchar(250),
  phone varchar(20),
  fax varchar(20),
  icon_id int,
  address varchar(300),
  preferences varchar(2000),
  creation_time timestamp WITH TIME ZONE NOT NULL,
  last_modified timestamp WITH TIME ZONE NOT NULL,
  status smallint NOT NULL DEFAULT 0,
  last_login timestamp WITH TIME ZONE,
  PRIMARY KEY (id),
  CONSTRAINT u_domain_name UNIQUE (name)
);

CREATE TABLE binary_file (
  id SERIAL,
  domain_id int NOT NULL,
  type int NOT NULL DEFAULT 0,
  mime_type int NOT NULL DEFAULT 0,
  content text,
  status smallint NOT NULL DEFAULT 0,
  csn bigint NOT NULL,
  PRIMARY KEY (id),
  CONSTRAINT fk_binary_file_domain FOREIGN KEY (domain_id) REFERENCES nda_domain(id) ON DELETE CASCADE
);

-- ------------------------------------------------------
-- Core entities
-- ------------------------------------------------------
CREATE TABLE nda_user (
  id SERIAL,
  name varchar(150) NOT NULL,
  type smallint NOT NULL,
  password bytea NOT NULL,
  staff_no varchar(50),
  ticket varchar(32),
  attempt int NOT NULL DEFAULT 0,
  nick_name varchar(50),
  gender char(1) NOT NULL,
  birth_date varchar(50),
  email varchar(150),
  mobile varchar(20),
  description varchar(2000),
  address varchar(200),
  icon_id int,
  creation_time timestamp WITH TIME ZONE NOT NULL,
  last_modified timestamp WITH TIME ZONE NOT NULL,
  last_login timestamp WITH TIME ZONE,
  status smallint NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  CONSTRAINT u_user_name UNIQUE (name),
  CONSTRAINT fk_user_icon FOREIGN KEY (icon_id) REFERENCES binary_file(id) ON DELETE SET NULL
);

CREATE TABLE nda_role (
  id SERIAL,
  name varchar(50) NOT NULL,
  description varchar(250),
  creation_time timestamp WITH TIME ZONE NOT NULL,
  last_modified timestamp WITH TIME ZONE NOT NULL,
  status smallint NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  CONSTRAINT u_role_name UNIQUE (name)
);

-- ------------------------------------------------------
-- Entity relationship
-- ------------------------------------------------------
CREATE TABLE user_role (
  id SERIAL,
  user_id int NOT NULL,
  role_id int NOT NULL,
  domain_id int NOT NULL,
  flag int NOT NULL,
  csn bigint NOT NULL,
  status smallint NOT NULL DEFAULT 0,
  PRIMARY KEY (id),
  CONSTRAINT u_user_role UNIQUE (user_id,role_id,domain_id),
  CONSTRAINT fk_user_role_user FOREIGN KEY (user_id) REFERENCES nda_user(id) ON DELETE CASCADE,
  CONSTRAINT fk_user_role_role FOREIGN KEY (role_id) REFERENCES nda_role(id) ON DELETE CASCADE,
  CONSTRAINT fk_user_role_domain FOREIGN KEY (domain_id) REFERENCES nda_domain(id) ON DELETE CASCADE
);

-- ------------------------------------------------------
-- Others
-- ------------------------------------------------------
CREATE TABLE mobile_device (
  device_id char(32) NOT NULL,
  user_id int NOT NULL,
  manufacturer varchar(50),
  model varchar(32),
  device_type int NOT NULL DEFAULT 0,
  os_version varchar(64),
  app_version varchar(20),
  status int NOT NULL DEFAULT 0,
  apns_token varchar(64),
  creation_date timestamp WITH TIME ZONE NOT NULL,
  last_modified timestamp WITH TIME ZONE NOT NULL,
  csn bigint NOT NULL,
  PRIMARY KEY (device_id)
);


CREATE TABLE crash (
  hash char(32) NOT NULL,
  app_version varchar(20),
  device_id char(32),
  stacktrace varchar(9000),
  crash_count int NOT NULL DEFAULT 0,
  state int NOT NULL DEFAULT 0,
  last_modified timestamp WITH TIME ZONE NOT NULL,
  csn bigint NOT NULL,
  PRIMARY KEY (hash)
);

INSERT INTO nda_role (name,description,creation_time,last_modified, status) VALUES ('超级管理员','',now(),now(), 1);
INSERT INTO nda_role (name,description,creation_time,last_modified, status) VALUES ('管理员','',now(),now(), 1);
INSERT INTO nda_role (name,description,creation_time,last_modified, status) VALUES ('维护员','',now(),now(), 1);
INSERT INTO nda_role (name,description,creation_time,last_modified, status) VALUES ('配送员','',now(),now(), 1);
INSERT INTO nda_role (name,description,creation_time,last_modified, status) VALUES ('中转员','',now(),now(), 1);


INSERT INTO nda_domain (name,password,display_name,creation_time,last_modified, status) VALUES ('TNSOFT',E'\\xC8B7E4E231306450BEC20F580F9BC234','TNSOFT domain',now(),now(), 1);
INSERT INTO nda_user (name,type,password,nick_name,gender,creation_time,last_modified, status) VALUES ('admin@tnsoft.com',0,E'\\xFE3929AF9D4805DCBF3BB06D9EDDE64E','demo','M',now(),now(), 1);
INSERT INTO user_role (user_id,role_id,domain_id,flag, csn) VALUES (1,1,1,1,0);
