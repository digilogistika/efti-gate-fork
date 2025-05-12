-- create schema
CREATE SCHEMA efti_schema;

-- Give permission to schema and table created
grant all privileges on schema efti_schema to efti;

GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA efti_schema TO efti;
