-- DROP TABLE IF EXISTS lists;


CREATE TABLE lists (
id uuid primary key default gen_random_uuid(),
name text,
mobile_clients_id uuid
);

ALTER TABLE lists OWNER TO shoplibackend;


