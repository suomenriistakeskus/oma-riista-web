DROP TABLE IF EXISTS import_user;

CREATE TABLE import_user (
  username VARCHAR(255) PRIMARY KEY,
  role     VARCHAR(255) NOT NULL,
  ssn      CHAR(11),
  password VARCHAR(255)
);

\COPY import_user FROM './csv/system_user.csv' WITH CSV DELIMITER ';' NULL '' ENCODING 'UTF-8';

INSERT INTO system_user (
  username,
  role,
  first_name,
  last_name,
  password,
  person_id,
  is_active
) SELECT
    a.username,
    'ROLE_' || a.role,
    COALESCE(p.first_name, a.role),
    COALESCE(p.last_name, a.role),
    a.password,
    p.person_id,
    TRUE
  FROM import_user a
    LEFT JOIN person p ON (a.ssn = p.ssn);
