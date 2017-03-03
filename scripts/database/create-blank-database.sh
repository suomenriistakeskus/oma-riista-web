#!/usr/bin/env bash

set -e

DATABASE_NAME="$1"
DROP_PREVIOUS="$2"

if [ -z "${DATABASE_NAME}" ]; then
    echo "Usage: $0 <test-database-name> [<drop-previous> (true|false) default: false]"
    exit 1
fi

if [ "$DROP_PREVIOUS" = true ] ; then
    psql -d postgres -c "DROP DATABASE IF EXISTS \"${DATABASE_NAME}\";"
fi

psql -d postgres -c "CREATE DATABASE \"${DATABASE_NAME}\" WITH OWNER riistakeskus;"
psql -d "${DATABASE_NAME}" -c "CREATE EXTENSION postgis;"
psql -d "${DATABASE_NAME}" -c "CREATE EXTENSION pg_trgm;"
psql -d "${DATABASE_NAME}" -c "CREATE EXTENSION hstore;"
