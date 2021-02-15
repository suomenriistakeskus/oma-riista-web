#!/usr/bin/env bash
set -e
cd "$(dirname "$0")" # Ensuring correct working directory

DATABASE="$1"

if [ -z "${DATABASE}" ]; then
    echo "Usage: $0 <target-db>"
    exit 1
fi

PSQL="psql -q1X -v ON_ERROR_STOP=1 -d ${DATABASE}"

echo ">>> Loading game_species reference data"
${PSQL} -f ./import/game_species.sql
echo ">>> Loading municipality reference data"
${PSQL} -f ./import/municipality.sql
echo ">>> Loading municipality_rhy reference data"
${PSQL} -f ./import/municipality_rhy.sql
echo ">>> Loading organisation reference data"
${PSQL} -f ./import/organisation.sql
echo ">>> Loading harvest_area reference data"
${PSQL} -f ./import/harvest_area.sql
echo ">>> Loading observation_base_fields reference data"
${PSQL} -f ./import/observation_base_fields.sql
echo ">>> Loading observation_context_sensitive_fields reference data"
${PSQL} -f ./import/observation_context_sensitive_fields.sql
echo ">>> Loading harvest_season reference data"
${PSQL} -f ./import/harvest_season.sql
echo ">>> Loading harvest_quota reference data"
${PSQL} -f ./import/harvest_quota.sql
