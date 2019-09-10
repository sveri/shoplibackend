-- DROP TABLE IF EXISTS share_hashes;


CREATE TABLE share_hashes (
mobile_clients_id uuid NOT NULL,
hash TEXT NOT NULL,
list_id uuid NOT NULL
);

ALTER TABLE share_hashes OWNER TO shoplibackend;


-- v 1.1.0
ALTER TABLE share_hashes ADD COLUMN from_string TEXT default "";
ALTER TABLE share_hashes ADD COLUMN to_string TEXT default "";

ALTER TABLE share_hashes RENAME COLUMN from_string TO shared_by;
ALTER TABLE share_hashes RENAME COLUMN to_string TO shared_with;


