-- DROP TABLE IF EXISTS list_entry;


CREATE TABLE list_entry (
id uuid primary key,
list_id uuid NOT NULL,
name text NOT NULL,
done boolean default false not null,
created_at timestamp without time zone default (now())
);

ALTER TABLE list_entry OWNER TO shoplibackend;


