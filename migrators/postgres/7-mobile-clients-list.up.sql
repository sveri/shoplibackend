-- v 1.1.0

-- DROP TABLE IF EXISTS mobile_clients_list;


CREATE TABLE mobile_clients_list (
mobile_clients_id uuid NOT NULL,
list_id uuid NOT NULL,
shared boolean not null default false,
shared_by text not null default "",
shared_with text not null default ""
);

ALTER TABLE mobile_clients_list OWNER TO shoplibackend;

CREATE INDEX mobile_clients_id ON mobile_clients_list (mobile_clients_id);
CREATE UNIQUE INDEX mobile_clients_id_list_id_idx ON mobile_clients_list (mobile_clients_id, list_id);
