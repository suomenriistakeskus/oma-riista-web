DROP TABLE IF EXISTS import_mooselike_price;

CREATE TABLE import_mooselike_price (
  hunting_year               INT           NOT NULL,
  game_species_official_code INT           NOT NULL,
  adult_price                NUMERIC(6, 2) NOT NULL,
  young_price                NUMERIC(6, 2),
  iban                       CHAR(18),
  bic                        VARCHAR(11),
  recipient_name             VARCHAR(70),
  PRIMARY KEY (hunting_year, game_species_official_code)
);

\COPY import_mooselike_price FROM './csv/mooselike_price.csv' WITH CSV DELIMITER ';' NULL '' ENCODING 'UTF-8';

INSERT INTO mooselike_price (hunting_year, game_species_id, adult_price, young_price, iban, bic, recipient_name)
  SELECT
    m.hunting_year,
    gs.game_species_id,
    m.adult_price,
    m.young_price,
    m.iban,
    m.bic,
    m.recipient_name
  FROM import_mooselike_price m
    JOIN game_species gs ON (m.game_species_official_code = gs.official_code);
