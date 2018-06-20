-- Convert RHY official_code data-format from numeric to zero-padded 3 character string
ALTER TABLE import_rhy
  ALTER COLUMN numero TYPE CHAR(3) USING LPAD(numero, 3, '0');

UPDATE import_rhy
SET geom = ST_Multi(geom);

UPDATE import_rhy
SET geom = ST_Multi(ST_Buffer(geom, 0))
WHERE NOT ST_IsValid(geom);

INSERT INTO rhy (gid, nimi_fi, nimi_sv, id, geom)
  SELECT
    gid,
    nimi,
    nimi_se,
    numero,
    geom
  FROM import_rhy;
