#!/usr/bin/env bash
set -e
cd "$(dirname "$0")" # Ensuring correct working directory

DATABASE_NAME="$1"
BUCKET_PREFIX="https://s3-eu-west-1.amazonaws.com/omariista-public"

if [ -z "${DATABASE_NAME}" ]; then
    echo "Usage: $0 <test-database-name>"
    exit 1
fi

echo ">>> Loading GIS dump"
echo "a3b4ccdbbc4b67b9456bda3ed1f513bc0aa73b9d /tmp/palstaalue.pgdump" > /tmp/palstaalue.sha
[ -f "/tmp/palstaalue.pgdump" ] && sha1sum --status -c /tmp/palstaalue.sha || wget "${BUCKET_PREFIX}/palstaalue.pgdump" -O /tmp/palstaalue.pgdump
echo "67248c71dde315890d0fa6a03376c9b976a43a0c  /tmp/vesialue.pgdump" > /tmp/vesialue.sha
[ -f "/tmp/vesialue.pgdump" ] && sha1sum --status -c /tmp/vesialue.sha || wget "${BUCKET_PREFIX}/vesialue.pgdump" -O /tmp/vesialue.pgdump
./gis-data/load-snapshot.sh "${DATABASE_NAME}"

echo ">>> Extracting RHY geometries"
mkdir -p gis-data/shp/rhy
echo "ad26ed0ead3ba3e9051857dba910f41970c19ae1  /tmp/rhy_data.zip" > /tmp/rhy_data.sha
[ -f "/tmp/rhy_data.zip" ] && sha1sum --status -c /tmp/rhy_data.sha || wget "${BUCKET_PREFIX}/rk-rhy-v1-5.zip" -O /tmp/rhy_data.zip
unzip -qo /tmp/rhy_data.zip -d gis-data/shp/rhy

echo ">>> Extracting HTA geometries"
mkdir -p gis-data/shp/hta
echo "696885dfd322337220f67278715bbe35ebed6af0  /tmp/hta_data.zip" > /tmp/hta_data.sha
[ -f /tmp/hta_data.zip ] && sha1sum --status -c /tmp/hta_data.sha || wget "${BUCKET_PREFIX}/hta-v1.zip" -O /tmp/hta_data.zip
unzip -qo /tmp/hta_data.zip -d gis-data/shp/hta

echo ">>> Loading RHY geometries"
./gis-data/load-rhy-geometry.sh "${DATABASE_NAME}"

echo ">>> Loading HTA geometries"
./gis-data/load-hta-geometry.sh "${DATABASE_NAME}"

echo ">>> Loading reference data"
./reference-data/load-reference-data-from-csv.sh "${DATABASE_NAME}"

echo ">>> Loading sample data"
./sample-data/load-sample-data.sh "${DATABASE_NAME}"

echo ">>> Dropping temporary tables"
PSQL="psql -v ON_ERROR_STOP=1 -d ${DATABASE_NAME}"
${PSQL} -AtX -c "select 'DROP TABLE IF EXISTS ' || tablename || '; ' from pg_tables where schemaname = 'public' and tablename like 'import_%'" | ${PSQL} -1
