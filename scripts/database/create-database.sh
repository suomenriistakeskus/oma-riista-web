#!/usr/bin/env bash
cd "$(dirname "$0")" # Ensuring correct working directory
set -e

DATABASE_NAME="$1"

if [ -z "${DATABASE_NAME}" ]; then
    echo "Usage: $0 <database_name>"
    exit 1
fi

echo "Creating a blank database '${DATABASE_NAME}'"
sh create-blank-database.sh "${DATABASE_NAME}" true

echo "Loading basic schema into database using mvn liquibase"
sh load-schema.sh "${DATABASE_NAME}"

echo "Downloading and populating initial data sets"
sh populate-data.sh ${DATABASE_NAME}

echo "Test database template \"${DATABASE_NAME}\" successfully created."
