#!/usr/bin/env bash

set -e

DATABASE="$1"

if [ -z "${DATABASE}" ]; then
    echo "Usage: $0 <target-db>"
    exit 1
fi

CSV_DUMP="psql ${DATABASE} -F ; --no-align --tuples-only"

for csv in ./export/*.sql; do
    NAME="$(basename ${csv} .sql)"
    ${CSV_DUMP} -f "${csv}" -o "./csv/${NAME}.csv"
done
