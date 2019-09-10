-- DROP TABLE IF EXISTS shared_list_to_user;


CREATE TABLE shared_list_to_user (
mobile_clients_id uuid NOT NULL,
list_id uuid NOT NULL
);

ALTER TABLE shared_list_to_user OWNER TO shoplibackend;

CREATE UNIQUE INDEX shared_list_to_user_mobile_clients_id ON shared_list_to_user (mobile_clients_id);


-- v 1.1.0
DROP TABLE IF EXISTS shared_list_to_user;