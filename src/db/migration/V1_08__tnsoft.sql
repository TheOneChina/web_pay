CREATE TABLE nda_log (
  id SERIAL,
  domain_id int NOT NULL,
  user_name varchar(20),
  operation varchar(500),
  operation_time timestamp WITH TIME ZONE NOT NULL,
  PRIMARY KEY (id)
);