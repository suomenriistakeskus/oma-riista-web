#!/usr/bin/env bash
set -e
cd "$(dirname "$0")" # Ensuring correct working directory

DATABASE="$1"

if [ -z "${DATABASE}" ]; then
    echo "Usage: $0 <target-db>"
    exit 1
fi

PSQL="psql -q1X -v ON_ERROR_STOP=1 -d ${DATABASE}"

${PSQL} -f ./import/game_species.sql
${PSQL} -f ./import/municipality.sql
${PSQL} -f ./import/organisation.sql
${PSQL} -f ./import/harvest_area.sql
${PSQL} -f ./import/observation_base_fields.sql
${PSQL} -f ./import/observation_context_sensitive_fields.sql
${PSQL} -f ./import/harvest_season.sql
${PSQL} -f ./import/harvest_quota.sql
