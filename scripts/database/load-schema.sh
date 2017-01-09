#!/usr/bin/env bash
set -e
cd "$(dirname "$0")" # Ensuring correct working directory

DATABASE_NAME="$1"

if [ -z "${DATABASE_NAME}" ]; then
    echo "Usage: $0 <database-name>"
    exit 1
fi

cd ../.. && mvn -Pe2e-test,jsSkip -Ddb.url="jdbc:postgresql:$DATABASE_NAME" process-resources liquibase:update
