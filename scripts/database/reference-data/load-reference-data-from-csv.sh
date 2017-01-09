#!/usr/bin/env bash
set -e
cd "$(dirname "$0")" # Ensuring correct working directory

DATABASE="$1"

if [ -z "${DATABASE}" ]; then
    echo "Usage: $0 <target-db>"
    exit 1
fi

PSQL="psql -v ON_ERROR_STOP=1 -d ${DATABASE}"

${PSQL} -c "DELETE FROM harvest_quota;"
${PSQL} -c "DELETE FROM harvest_season;"
${PSQL} -c "DELETE FROM observation_context_sensitive_fields;"
${PSQL} -c "DELETE FROM observation_base_fields;"
${PSQL} -c "DELETE FROM harvest_area_rhys;"
${PSQL} -c "DELETE FROM harvest_area;"
${PSQL} -c "DELETE FROM harvest_report_fields;"
${PSQL} -c "DELETE FROM organisation;"
${PSQL} -c "DELETE FROM municipality;"
${PSQL} -c "DELETE FROM game_species;"

${PSQL} -f ./import/game_species.sql
${PSQL} -f ./import/municipality.sql
${PSQL} -f ./import/organisation.sql
${PSQL} -f ./import/harvest_area.sql
${PSQL} -f ./import/harvest_report_fields.sql
${PSQL} -f ./import/observation_base_fields.sql
${PSQL} -f ./import/observation_context_sensitive_fields.sql
${PSQL} -f ./import/harvest_season.sql
${PSQL} -f ./import/harvest_quota.sql
