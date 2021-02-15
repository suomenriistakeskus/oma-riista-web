#!/bin/bash
########################################
#
# Test for deploy-info.sh
#
########################################

# Export automatically
set -a

function debug {
    [ -z ${DEBUG+x} ] || echo "DEBUG: ${FUNCNAME[1]} $@" >&2
}

# MOCKed APIs

function aws {
    debug $@

    echo '{"Environments": [{"VersionLabel": "app-1970-01-01_0200-0-g1111"}]}'
}

function curl {
    debug $@

    local PARAMS="$1 $2"
    
    local JIRA_REQUEST="-s -u"
    local SLACK_POST="-s -X"

    [[ "$PARAMS" = "$JIRA_REQUEST" ]] && echo '{"fields": {"summary":"MOCKed"}}'
    [[ "$PARAMS" = "$SLACK_POST" ]] && echo 'MOCKed curl'
}

function git {
    debug $@

    local PARAMS="$1 $2 $3"

    local GET_TAG_HASHES="log --no-walk --tags"
    local GET_SUMMARIES_FROM_PROD_VERSION="log --format=%s 1111..3333"
    local GET_SUMMARIES_FROM_PREV_TAG="log --format=%s 2222..3333"
    local GET_LAST_TAG_NAME="describe --long --tags"

    [[ "$PARAMS" = "$GET_TAG_HASHES" ]] && echo -e "3333\n2222\n1111"
    [[ "$PARAMS" = "$GET_SUMMARIES_FROM_PROD_VERSION" ]] && echo -e "OR-1 Foo\nOR-2: Bar\nOR-2: Oops\nSomething else\n OR-3 Baz"
    [[ "$PARAMS" = "$GET_SUMMARIES_FROM_PREV_TAG" ]] && echo -e "OR-2: Bar\nSomething else\n OR-3 Baz"
    [[ "$PARAMS" = "$GET_LAST_TAG_NAME" ]] && echo -e "1970-01-02_0200-0-g2222"
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
    JIRA_CREDENTIALS=foo \
    JIRA_HOST=bar \
    SLACK_HOOK=baz \
    $(dirname $0)/deploy-info.sh)

# Verify results

EXPECTED='All commits (for debugging purposes):
OR-1 Foo
OR-2: Bar
OR-2: Oops
Something else
 OR-3 Baz

--------

Sending following message to Slack:
---8<---
*New release of test-project available*

Content of *1970-01-02_0200-0-g2222*:
- <https://bar/browse/OR-1|OR-1>: MOCKed
- <https://bar/browse/OR-2|OR-2>: MOCKed
- <https://bar/browse/OR-3|OR-3>: MOCKed
--->8---
MOCKed curl'

if [[ "$RESULT" = "$EXPECTED" ]]; then
    echo "==== Output ===="
    echo "$RESULT"
    echo "==== Test PASSED ===="
else
    echo "==== RESULT vs EXPECTED ===="
    diff -y <(echo "$RESULT") <(echo "$EXPECTED")
    echo "==== Test FAILED ===="
fi
