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
    echo "Importing ${table_name}"
    ${PSQL} -c "DELETE FROM ${table_name}"
    cat "/tmp/${table_name}.pgdump" | ${PSQL} -c "COPY ${table_name} FROM STDIN WITH BINARY"
done

${PSQL} -c "CREATE INDEX palstaalue_geom ON palstaalue USING GIST (geom)"
${PSQL} -c "CREATE INDEX vesialue_geom ON vesialue USING GIST (geom)"
