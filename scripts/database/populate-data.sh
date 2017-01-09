#!/usr/bin/env bash
set -e
cd "$(dirname "$0")" # Ensuring correct working directory

DATABASE_NAME="$1"
DATA_SOURCE_RHY="https://s3-eu-west-1.amazonaws.com/omariista-public/rk-rhy-v1-5.zip"
DATA_SOURCE_HTA="https://s3-eu-west-1.amazonaws.com/omariista-public/hta-v1.zip"

if [ -z "${DATABASE_NAME}" ]; then
    echo "Usage: $0 <test-database-name>"
    exit 1
fi

# RHY data download and extraction
echo ">>> Extracting RHY geometries"
mkdir -p gis-data/shp/rhy
echo Downloading RHY data from ${DATA_SOURCE_RHY}
wget ${DATA_SOURCE_RHY} -O /tmp/rhy_data.zip
unzip -o /tmp/rhy_data.zip -d gis-data/shp/rhy

echo ">>> Extracting HTA geometries"
mkdir -p gis-data/shp/hta
echo Downloading HTA data from ${DATA_SOURCE_HTA}
wget ${DATA_SOURCE_HTA} -O /tmp/hta_data.zip
unzip -o /tmp/hta_data.zip -d gis-data/shp/hta

# Executing the population scripts
echo ">>> Loading RHY geometries"
sh gis-data/load-hta-geometry.sh ${DATABASE_NAME}
echo ">>> Loading HTA geometries"
sh gis-data/load-rhy-geometry.sh ${DATABASE_NAME}
echo ">>> Loading reference data"
sh reference-data/load-reference-data-from-csv.sh ${DATABASE_NAME}
echo ">>> Loading sample data"
sh sample-data/load-sample-data.sh ${DATABASE_NAME}
