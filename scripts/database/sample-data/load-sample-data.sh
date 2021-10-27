#!/usr/bin/env bash
set -e
cd "$(dirname "$0")" # Ensuring correct working directory

DATABASE="$1"

if [ -z "${DATABASE}" ]; then
    echo "Usage: $0 <target-db>"
    exit 1
fi

PSQL="psql -q1X -v ON_ERROR_STOP=1 -d ${DATABASE}"

${PSQL} -f ./sql/person.sql && \
${PSQL} -f ./sql/system_user.sql && \
${PSQL} -f ./sql/club.sql && \
${PSQL} -f ./sql/occupation.sql && \
${PSQL} -f ./sql/harvest_permit.sql && \
${PSQL} -f ./sql/harvest_permit_partner.sql && \
${PSQL} -f ./sql/harvest_permit_species_amount.sql && \
${PSQL} -f ./sql/hunting_club_area.sql && \
${PSQL} -f ./sql/hunting_group.sql && \
${PSQL} -f ./sql/harvest_permit_area.sql && \
${PSQL} -f ./sql/harvest_permit_application.sql && \
${PSQL} -f ./sql/rhy_annual_statistics.sql
