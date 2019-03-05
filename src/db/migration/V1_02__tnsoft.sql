CREATE TABLE nda_tag (
  tag_no varchar(40) NOT NULL,
  domain_id int,
  temperature_min numeric(8,2),
  temperature_max numeric(8,2),
  creation_time timestamp WITH TIME ZONE NOT NULL,
  last_modified timestamp WITH TIME ZONE NOT NULL,
  status smallint NOT NULL DEFAULT 1,
  PRIMARY KEY (tag_no),
  CONSTRAINT u_tag_tag_no UNIQUE (tag_no)
);

CREATE TABLE nda_express (
  id SERIAL,
  domain_id int NOT NULL,
  express_no varchar(50) NOT NULL,
  creation_time timestamp WITH TIME ZONE NOT NULL,
  last_modified timestamp WITH TIME ZONE NOT NULL,
  status smallint NOT NULL DEFAULT 0,  
  checkin_time timestamp WITH TIME ZONE,
  checkout_time timestamp WITH TIME ZONE,
  PRIMARY KEY (id)
);

CREATE TABLE nda_tag_express (
  id SERIAL,
  domain_id int NOT NULL,
  express_id int NOT NULL,
  tag_no varchar(40) NOT NULL,
  creation_time timestamp WITH TIME ZONE NOT NULL,
  last_modified timestamp WITH TIME ZONE NOT NULL,
  status smallint NOT NULL DEFAULT 1,
  PRIMARY KEY (id)
);

CREATE TABLE nda_user_express (
  id SERIAL,
  domain_id int NOT NULL,
  express_id int NOT NULL,
  user_id int NOT NULL,
  creation_time timestamp WITH TIME ZONE NOT NULL,
  last_modified timestamp WITH TIME ZONE NOT NULL,
  status smallint NOT NULL DEFAULT 1,
  PRIMARY KEY (id)
);

CREATE TABLE nda_location_express (
  id SERIAL,
  domain_id int NOT NULL,
  express_id int NOT NULL,
  lat numeric(9,6),
  lng numeric(9,6),
  creation_time timestamp WITH TIME ZONE NOT NULL,
  last_modified timestamp WITH TIME ZONE NOT NULL,
  PRIMARY KEY (id)
);

CREATE TABLE nda_temperature_express (
  id SERIAL,
  domain_id int NOT NULL,
  express_id int NOT NULL,
  temperature numeric(8,2),
  humidity numeric(8,2),
  creation_time timestamp WITH TIME ZONE NOT NULL,
  last_modified timestamp WITH TIME ZONE NOT NULL,
  PRIMARY KEY (id)
);

CREATE TABLE nda_alert_level (
  id SERIAL,
  domain_id int NOT NULL,
  name varchar(20),
  hours numeric(4,1),
  times smallint,
  creation_time timestamp WITH TIME ZONE NOT NULL,
  last_modified timestamp WITH TIME ZONE NOT NULL,
  PRIMARY KEY (id)
);

CREATE TABLE nda_alert (
  id SERIAL,
  domain_id int NOT NULL,
  tag_no varchar(40) NOT NULL,
  nda_alert_level_id int,
  creation_time timestamp WITH TIME ZONE NOT NULL,
  last_modified timestamp WITH TIME ZONE NOT NULL,
  status smallint NOT NULL DEFAULT 1,
  PRIMARY KEY (id)
);

CREATE TABLE nda_alert_express (
  id SERIAL,
  domain_id int NOT NULL,
  tag_no varchar(40) NOT NULL,
  nda_alert_id int,
  experss_id int,
  PRIMARY KEY (id)
);