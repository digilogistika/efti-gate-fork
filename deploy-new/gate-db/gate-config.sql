DELETE
FROM gate
where 1 = 1; -- to explicitly state all rows are to be deleted

INSERT INTO gate (country, gateid, createddate, lastmodifieddate)
VALUES ('DEF', 'gate', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
