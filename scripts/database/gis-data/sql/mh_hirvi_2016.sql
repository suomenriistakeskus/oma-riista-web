ALTER TABLE import_mh_hirvi_2016 OWNER TO riistakeskus;

-- Split numeric code from name field
ALTER TABLE import_mh_hirvi_2016 ADD COLUMN kohde_koodi INT4;
ALTER TABLE import_mh_hirvi_2016 ADD COLUMN kohde_nimi VARCHAR(255);

UPDATE import_mh_hirvi_2016 SET kohde_koodi = substring(nimi, 0, 5)::integer;
UPDATE import_mh_hirvi_2016 SET kohde_nimi = TRIM(substring(nimi, 6));
