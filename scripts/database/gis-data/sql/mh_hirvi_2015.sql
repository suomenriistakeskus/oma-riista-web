ALTER TABLE import_mh_hirvi_2015 OWNER TO riistakeskus;

-- Remove invalid entries (5 kpl)
DELETE FROM import_mh_hirvi_2015 WHERE kohde_nimi IS NULL;

-- Split numeric code from name field
ALTER TABLE import_mh_hirvi_2015 ADD COLUMN kohde_koodi INT4;

UPDATE import_mh_hirvi_2015 SET kohde_koodi = substring(kohde_nimi, 0, 5) :: INTEGER;
UPDATE import_mh_hirvi_2015 SET kohde_nimi = TRIM(substring(kohde_nimi, 6));
