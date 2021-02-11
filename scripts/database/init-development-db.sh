#!/usr/bin/env bash
#
# Requires (at least):
# - postgis
# - wget
# - sha1sum or shasum
#
# Initialize database named rk1 on localhost port 5432:
# $ scripts/database/init-development-db.sh localhost 5432 rk1
#
# Start development environment that uses the database:
# $ mvn -Ddb.url=jdbc:postgresql://localhost:5432/rk1?sslmode=disable jetty:run
#

PSQL_HOST=${1:-postgres}
PSQL_PORT=${2:-5432}
PSQL_USER=postgres
PSQL_MAINT_DB=postgres
PSQL_USER_DB=${3:-riistakeskus}

BASEPATH="$(dirname "$0")"

DEBUG=
#DEBUG=echo   # Uncomment to just see commands

${DEBUG} psql -h ${PSQL_HOST} -p ${PSQL_PORT} -U ${PSQL_USER} -d ${PSQL_MAINT_DB} -c "DO \$\$ BEGIN CREATE ROLE riistakeskus WITH LOGIN PASSWORD 'riistakeskus'; EXCEPTION WHEN duplicate_object THEN RAISE NOTICE '%, skipping', SQLERRM USING ERRCODE = SQLSTATE; END \$\$;"
${DEBUG} psql -h ${PSQL_HOST} -p ${PSQL_PORT} -U ${PSQL_USER} -d ${PSQL_MAINT_DB} -c "CREATE DATABASE ${PSQL_USER_DB} WITH OWNER riistakeskus;"
${DEBUG} psql -h ${PSQL_HOST} -p ${PSQL_PORT} -U ${PSQL_USER} -d ${PSQL_USER_DB} -c "CREATE EXTENSION postgis;"
${DEBUG} psql -h ${PSQL_HOST} -p ${PSQL_PORT} -U ${PSQL_USER} -d ${PSQL_USER_DB} -c "CREATE EXTENSION pg_trgm;"
${DEBUG} psql -h ${PSQL_HOST} -p ${PSQL_PORT} -U ${PSQL_USER} -d ${PSQL_USER_DB} -c "CREATE EXTENSION hstore;"

${DEBUG} mvn -f "${BASEPATH}"/../../pom.xml --quiet -Pdev,jsSkip -Ddb.url=jdbc:postgresql://${PSQL_HOST}:${PSQL_PORT}/${PSQL_USER_DB} resources:resources liquibase:update
${DEBUG} "${BASEPATH}"/populate-data.sh "${PSQL_USER_DB} -h ${PSQL_HOST} -p ${PSQL_PORT} -U ${PSQL_USER}"
