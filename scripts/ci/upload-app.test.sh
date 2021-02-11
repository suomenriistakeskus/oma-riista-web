#!/bin/bash
########################################
#
# Test for upload-app.sh
#
########################################

# Export by default
set -a

function debug {
    [ -z ${DEBUG+x} ] || echo "DEBUG: ${FUNCNAME[1]} $@" >&2
}

# MOCKed APIs

function aws {
    debug $@
}

function git {
    debug $@

    local PARAMS="$1"

    local CREATE_TAG="tag"
    local UPLOAD_TAG="push"
    local GET_VERSION="describe"

    [[ "$PARAMS" = "$CREATE_TAG" ]] && return 0
    [[ "$PARAMS" = "$UPLOAD_TAG" ]] && return 0
    [[ "$PARAMS" = "$GET_VERSION" ]] && echo "1970-01-01_0200-0-g1111" && return 0
    return 1
}

function ls {
    debug $@

    local PARAMS="$1"

    local FIND_APP_FILES="-1t"

    [[ "$PARAMS" = "$FIND_APP_FILES" ]] && echo -e "file1\nfile2\file3" && return 0
    return 1

}

# Stop exporting
set +a

# Do it with mandatory variables

RESULT=$(
    AWS_ACCESS_KEY_ID=id \
    AWS_SECRET_ACCESS_KEY=key \
    AWS_REGION=region \
    APP_NAME=app \
    ENV_NAME=env \
    GIT_USER=user \
    GIT_TOKEN=token \
    S3_BUCKET_NAME=bucket \
    $(dirname $0)/upload-app.sh
)

RESULT_STATUS=$?

EXPECTED="Copying 'file1' to 's3://bucket/app/1970-01-01_0200-0-g1111'"$'\n'\
"Create application 'app' version 'app-1970-01-01_0200-0-g1111', description 'test commit - gitlab.user'"

echo "==== Output ===="
echo "$RESULT"

if [[ $RESULT_STATUS -eq 0 ]] && [[ "$RESULT" = "$EXPECTED" ]]; then
    echo "==== Test PASSED ===="
else
    echo "==== RESULT vs EXPECTED ===="
    diff -y <(echo "$RESULT") <(echo "$EXPECTED")
    echo "==== ERROR CODE: $RESULT_STATUS ===="
    echo "==== Test FAILED ===="
fi
