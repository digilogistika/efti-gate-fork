-- create schema
CREATE SCHEMA eftiEE;

-- Give permission to schema and table created
grant all privileges on schema eftiEE to root;

GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA eftiEE to root;
