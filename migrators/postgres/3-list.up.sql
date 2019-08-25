-- DROP TABLE IF EXISTS lists;


CREATE TABLE lists (
id uuid primary key default gen_random_uuid(),
name text NOT NULL,
mobile_clients_id uuid NOT NULL,
list_id uuid NOT NULL
);

ALTER TABLE lists OWNER TO shoplibackend;


