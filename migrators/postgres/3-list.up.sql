-- DROP TABLE IF EXISTS lists;


CREATE TABLE lists (
id bigserial NOT NULL PRIMARY KEY,
name text,
mobile_clients_id BIGINT NOT NULL
);

ALTER TABLE lists OWNER TO shoplibackend;


