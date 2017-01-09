DELETE FROM mh_hirvi;

INSERT INTO mh_hirvi (vuosi, koodi, nimi, pinta_ala, geom)
  SELECT
    2015,
    m2.kohde_koodi,
    m2.kohde_nimi,
    m2.kartta_ala,
    m1.geom
  FROM (
         SELECT
           MIN(gid)                                            AS gid,
           ST_Multi(ST_CollectionHomogenize(ST_Collect(geom))) AS geom
         FROM import_mh_hirvi_2015
         WHERE kohde_nimi IS NOT NULL
         GROUP BY kohde_koodi
       ) m1
    JOIN import_mh_hirvi_2015 m2 USING (gid);

INSERT INTO mh_hirvi (vuosi, koodi, nimi, pinta_ala, geom)
  SELECT
    2016,
    kohde_koodi,
    nimi,
    pinta_ala,
    geom
  FROM import_mh_hirvi_2016;
