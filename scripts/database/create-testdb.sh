#!/usr/bin/env bash
cd "$(dirname "$0")" # Ensuring correct working directory

DATABASE_NAME="$1"
TEMPLATE_NAME="$2"

if [ -z "${DATABASE_NAME}" ] || [ -z "${TEMPLATE_NAME}" ] ; then
    echo "Usage: $0 <database-name> <template-name>"
    exit 1
fi

# If a template database does not exist, it is created automatically.
TEMPLATE_EXISTS_ALREADY=$(psql -lqt | cut -d \| -f 1 | grep -w "${TEMPLATE_NAME}")
set -e

if ! [ ${TEMPLATE_EXISTS_ALREADY} ] ; then
    echo "Template database \"${TEMPLATE_NAME}\" does not exist and is being created.";
    sh create-database.sh "${TEMPLATE_NAME}"
fi

psql -d postgres -c "DROP DATABASE IF EXISTS \"${DATABASE_NAME}\";"
psql -d postgres -c "CREATE DATABASE \"${DATABASE_NAME}\" TEMPLATE \"${TEMPLATE_NAME}\";"
