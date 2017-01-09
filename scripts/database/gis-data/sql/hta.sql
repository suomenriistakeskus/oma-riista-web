UPDATE import_hta
SET geom = ST_Multi(geom);

UPDATE import_hta
SET geom = ST_Multi(ST_Buffer(geom, 0))
WHERE NOT ST_IsValid(geom);

DELETE FROM hta;

INSERT INTO hta (
  gid,
  numero,
  nimi,
  nimi_ly,
  nimi_se,
  geom
) SELECT
    gid,
    numero,
    nimi,
    nimi_ly,
    nimi_se,
    geom
  FROM import_hta;

DROP TABLE import_hta;
