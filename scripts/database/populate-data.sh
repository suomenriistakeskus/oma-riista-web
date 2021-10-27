#!/usr/bin/env bash
set -e
cd "$(dirname "$0")" # Ensuring correct working directory

DATABASE_NAME="$1"
BUCKET_PREFIX="https://s3-eu-west-1.amazonaws.com/omariista-public"

if [ -z "${DATABASE_NAME}" ]; then
    echo "Usage: $0 <test-database-name>"
    exit 1
fi

# Use command shasum if sha1sum is not found (for macs)
SHASUM="$(command -v sha1sum > /dev/null 2>&1 && echo sha1sum || echo shasum)"

echo ">>> Loading GIS dump"
echo "a3b4ccdbbc4b67b9456bda3ed1f513bc0aa73b9d  /tmp/palstaalue.pgdump" > /tmp/palstaalue.sha
[ -f "/tmp/palstaalue.pgdump" ] && ${SHASUM} --status -c /tmp/palstaalue.sha || wget "${BUCKET_PREFIX}/palstaalue.pgdump" -O /tmp/palstaalue.pgdump
echo "67248c71dde315890d0fa6a03376c9b976a43a0c  /tmp/vesialue.pgdump" > /tmp/vesialue.sha
[ -f "/tmp/vesialue.pgdump" ] && ${SHASUM} --status -c /tmp/vesialue.sha || wget "${BUCKET_PREFIX}/vesialue.pgdump" -O /tmp/vesialue.pgdump
./gis-data/load-snapshot.sh "${DATABASE_NAME}"

echo ">>> Extracting RHY geometries"
mkdir -p gis-data/shp/rhy
echo "31de1be966ac87d096b4b9def684ed2f070ba946  /tmp/rhy_data.zip" > /tmp/rhy_data.sha
[ -f "/tmp/rhy_data.zip" ] && ${SHASUM} --status -c /tmp/rhy_data.sha || wget "${BUCKET_PREFIX}/rk-rhy-v1-9.zip" -O /tmp/rhy_data.zip
unzip -qo /tmp/rhy_data.zip -d gis-data/shp/rhy

echo ">>> Extracting HTA geometries"
mkdir -p gis-data/shp/hta
echo "696885dfd322337220f67278715bbe35ebed6af0  /tmp/hta_data.zip" > /tmp/hta_data.sha
[ -f /tmp/hta_data.zip ] && ${SHASUM} --status -c /tmp/hta_data.sha || wget "${BUCKET_PREFIX}/hta-v1.zip" -O /tmp/hta_data.zip
unzip -qo /tmp/hta_data.zip -d gis-data/shp/hta

echo ">>> Extracting poronhoito geometries"
mkdir -p gis-data/shp/ph
echo "3b8e0073633d1981c99751ab05bf97eb0b8e7357  /tmp/ph_data.zip" > /tmp/ph_data.sha
[ -f /tmp/ph_data.zip ] && ${SHASUM} --status -c /tmp/ph_data.sha || wget "${BUCKET_PREFIX}/ph-v1.zip" -O /tmp/ph_data.zip
unzip -qo /tmp/ph_data.zip -d gis-data/shp/ph

echo ">>> Loading RHY geometries"
./gis-data/load-rhy-geometry.sh "${DATABASE_NAME}"

echo ">>> Loading HTA geometries"
./gis-data/load-hta-geometry.sh "${DATABASE_NAME}"

echo ">>> Loading reference data"
./reference-data/load-reference-data-from-csv.sh "${DATABASE_NAME}"

echo ">>> Loading harvest area geometries"
./gis-data/load-harvestarea-geometry.sh "${DATABASE_NAME}"

echo ">>> Loading sample data"
./sample-data/load-sample-data.sh "${DATABASE_NAME}"

echo ">>> Dropping temporary tables"
PSQL="psql -v ON_ERROR_STOP=1 -d ${DATABASE_NAME}"
${PSQL} -AtX -c "select 'DROP TABLE IF EXISTS ' || tablename || '; ' from pg_tables where schemaname = 'public' and tablename like 'import\\_%'" | ${PSQL} -1
