-- DROP TABLE IF EXISTS lists;


CREATE TABLE lists (
id uuid primary key,
name text NOT NULL,
mobile_clients_id uuid NOT NULL
);

ALTER TABLE lists OWNER TO shoplibackend;

-- v 1.1.0
ALTER TABLE lists DROP COLUMN mobile_clients_id;
ALTER TABLE lists ADD COLUMN owner uuid NOT NULL;




