#!/usr/bin/env bash
set -e
cd "$(dirname "$0")" # Ensuring correct working directory

DATABASE="$1"

if [ -z "${DATABASE}" ]; then
    echo "Usage: $0 <target-db>"
    exit 1
fi

PSQL="psql -q1X -v ON_ERROR_STOP=1 -d ${DATABASE}"
GIS_TABLES=(palstaalue vesialue)

for table_name in "${GIS_TABLES[@]}"
do
    echo "Exporting ${table_name}"
    SQL="COPY (SELECT * FROM ${table_name} WHERE ST_DWithin(geom, ST_SetSrid(ST_MakePoint(549799, 7464490), 3067), 1000*50)) TO STDOUT WITH BINARY"
    ${PSQL} -c "${SQL}"  > "/tmp/${table_name}.pgdump"
done
