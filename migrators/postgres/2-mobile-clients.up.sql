-- DROP TABLE IF EXISTS mobile_clients;


CREATE TABLE mobile_clients (
id uuid primary key default gen_random_uuid(),
device_id text NOT NULL,
app_id text NOT NULL,
user_id BIGINT
);

ALTER TABLE mobile_clients OWNER TO shoplibackend;

CREATE UNIQUE INDEX device_id_app_id_idx ON mobile_clients (device_id, app_id);

