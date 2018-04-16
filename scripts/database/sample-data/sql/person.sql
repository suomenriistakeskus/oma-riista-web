DROP TABLE IF EXISTS import_person;

CREATE TABLE import_person (
  ssn           CHAR(11) PRIMARY KEY,
  hunter_number CHAR(8) NOT NULL,
  first_name    VARCHAR(255),
  last_name     VARCHAR(255)
);

\COPY import_person FROM './csv/person.csv' WITH CSV DELIMITER ';' NULL '' ENCODING 'UTF-8';

INSERT INTO person (
  ssn,
  hunter_number,
  first_name,
  by_name,
  last_name
) SELECT
    ssn,
    hunter_number,
    first_name,
    first_name,
    last_name
  FROM import_person;

UPDATE person
SET home_municipality_code    = '536',
  language_code               = 'fi',
  magazine_language_code      = 'fi',
  phone_number                = '+358501234' || person_id,
  email                       = 'person' || person_id || '@invalid',
  hunting_card_start          = '2015-08-01',
  hunting_card_end            = '2028-07-31',
  hunter_exam_date            = '2015-03-01',
  hunter_exam_expiration_date = '2028-07-31',
  hunting_payment_one_day     = '2015-06-01',
  hunting_payment_one_year    = 2015;

UPDATE person
SET rhy_membership_id = (SELECT organisation_id
                         FROM organisation
                         WHERE organisation_type = 'RHY' AND official_code = '368');

INSERT INTO address (consistency_version, street_address, postal_code, city, country_name, country_code)
  SELECT
    person_id,
    'Katu ' || person_id,
    '00001',
    'Nokia',
    'Suomi',
    'FI'
  FROM person;

UPDATE person
SET mr_address_id = (SELECT address_id
                     FROM address
                     WHERE consistency_version = person_id);
