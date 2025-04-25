-- create user
create user root with encrypted password 'root';
grant all privileges on database efti to root;


-- create schema
CREATE SCHEMA efti_schema;

-- Give permission to schema and table created
grant all privileges on schema efti_schema to root;


GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA efti_schema to root;
