#!/usr/bin/env bash
set -e
cd "$(dirname "$0")" # Ensuring correct working directory

DATABASE="$1"
SOURCE_FILE=$(find ./shp/rhy/ -not -path "*/\.*" -type f -name "*.shp")

if [ -z "${DATABASE}" ]; then
    echo "Usage: $0 <target-db>"
    exit 1
fi

if [ ! -f "${SOURCE_FILE}" ]; then
    echo "Source shape-file not found at ${SOURCE_FILE}"
    exit 2
else
    echo "Using source file: ${SOURCE_FILE}"
fi

PSQL="psql -q1X -v ON_ERROR_STOP=1 -d ${DATABASE}"

${PSQL} -c "DROP TABLE IF EXISTS import_rhy;"

shp2pgsql -c -s 3047 -W UTF-8 -I -D "${SOURCE_FILE}" public.import_rhy | ${PSQL}

${PSQL} -f ./sql/rhy.sql
