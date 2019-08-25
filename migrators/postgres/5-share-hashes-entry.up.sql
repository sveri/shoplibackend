-- DROP TABLE IF EXISTS share_hashes;


CREATE TABLE share_hashes (
mobile_clients_id uuid NOT NULL,
hash TEXT NOT NULL,
list_id uuid NOT NULL
);

ALTER TABLE share_hashes OWNER TO shoplibackend;


