#!/usr/bin/env bash

set -e

DATABASE="$1"

if [ -z "${DATABASE}" ]; then
    echo "Usage: $0 <target-db>"
    exit 1
fi

PSQL="psql -v ON_ERROR_STOP=1 -d ${DATABASE}"

${PSQL} -c "DROP TABLE IF EXISTS import_mh_hirvi_2015;"
${PSQL} -c "DROP TABLE IF EXISTS import_mh_hirvi_2016;"

shp2pgsql -c -N abort -D -i -s 3067 -W LATIN1 -D ./shp/Metsahallituksen_alueet_2015/Hirvieläinten_metsästysalueet.shp import_mh_hirvi_2015 | ${PSQL} -q
shp2pgsql -c -N abort -D -i -s 3067 -W UTF-8 -D ./shp/Metsahallituksen_alueet_2016/Hirvenmetsästysalueet import_mh_hirvi_2016 | ${PSQL} -q

#shp2pgsql -c -N abort -D -i -s 3067 -W LATIN1 -D ./shp/Metsahallituksen_alueet_2015/Pienriistan_metsästysalueet.shp import_mh_pienriista_2015 | ${PSQL} -q
#shp2pgsql -c -N abort -D -i -s 3067 -W UTF-8 -D ./shp/Metsahallituksen_alueet_2016/Pienriistan\ metsästysalueet. import_mh_pienriista_2016 | ${PSQL} -q

${PSQL} -f ./sql/mh_hirvi_2015.sql
${PSQL} -f ./sql/mh_hirvi_2016.sql
${PSQL} -f ./sql/mh_hirvi.sql
