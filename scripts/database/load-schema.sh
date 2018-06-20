#!/usr/bin/env bash
set -e
cd "$(dirname "$0")" # Ensuring correct working directory

DATABASE_NAME="$1"
CACHE_FILE="/tmp/riistakeskus.sql"

if [ -z "${DATABASE_NAME}" ]; then
    echo "Usage: $0 <database-name>"
    exit 1
fi

PSQL="psql -q1X -v ON_ERROR_STOP=1 -d ${DATABASE_NAME}"

if [ -f "${CACHE_FILE}" ]; then
    cat "${CACHE_FILE}" | ${PSQL}
fi

cd ../.. && mvn --quiet -Pdev,jsSkip -Ddb.url="jdbc:postgresql:$DATABASE_NAME" resources:resources liquibase:update

pg_dump -d ${DATABASE_NAME} > "${CACHE_FILE}"
