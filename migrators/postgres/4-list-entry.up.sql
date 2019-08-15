-- DROP TABLE IF EXISTS list_entry;


CREATE TABLE list_entry (
id bigserial NOT NULL PRIMARY KEY,
list_id BIGINT NOT NULL,
name text,
done boolean default false not null,
created_at timestamp without time zone default (now() at time zone 'utc')
);

ALTER TABLE list_entry OWNER TO shoplibackend;


