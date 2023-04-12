#!/bin/bash
########################################
#
# Test for deploy-app.sh
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
    echo "aws $@"
}

function curl {
    debug $@
    echo "curl $@"
}

function git {
    debug $@

    local PARAMS="$1"

    local GET_VERSION="describe"

    [[ "$PARAMS" = "$GET_VERSION" ]] && echo "1970-01-01_0200-0-g1111" && return 0
    return 1
}

# Stop exporting
set +a

# Do it with mandatory variables

RESULT=$(
    AWS_ACCESS_KEY_ID=id \
    AWS_SECRET_ACCESS_KEY=key \
    AWS_REGION=region \
    CI_COMMIT_SHA=sha \
    ENV_NAME=env \
    GITLAB_USER_LOGIN=user \
    $(dirname $0)/deploy-app.sh
)

RESULT_STATUS=$?

EXPECTED="aws elasticbeanstalk update-environment --environment-name env --version-label app-1970-01-01_0200-0-g1111 --region region"

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
