CREATE TABLE sign (
  id              SERIAL PRIMARY KEY,
  name           VARCHAR(100) NOT NULL,
  since          VARCHAR(100) NULL,
  until          VARCHAR(100) NULL,
  characteristics VARCHAR(100) NULL
);


CREATE SEQUENCE signs_sequence
  start 2
  increment 2;
