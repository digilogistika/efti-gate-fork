-- create schema
CREATE SCHEMA eftiEE;

-- Give permission to schema and table created
grant all privileges on schema eftiEE to efti;

GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA eftiEE TO efti;
