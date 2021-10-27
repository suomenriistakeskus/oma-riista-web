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

function curl {
    debug $@

    local PARAMS="$1 $2"
    local SLACK_POST="-s -X"

    [[ "$PARAMS" = "$SLACK_POST" ]] && echo 'MOCKed curl'
}

function git {
    debug $@

    local PARAMS="$1 $2"

    local GET_TAG_HASHES="log --no-walk"
    local GET_LATEST_TAG_NAME="describe --tags"
    local GET_SUMMARIES_FROM_PROD_VERSION="log --format=%s"

    [[ "$PARAMS" = "$GET_TAG_HASHES" ]] && echo -e "1111\n2222"
    [[ "$PARAMS" = "$GET_LATEST_TAG_NAME" ]] && echo -e "TAG-1"
    [[ "$PARAMS" = "$GET_SUMMARIES_FROM_PROD_VERSION" ]] && echo -e "OR-1 Foo\nOR-2: Bar\nOR-2: Oops\nSomething \"else\"\n OR-3 Baz"
}

# Stop exporting
set +a

# Do it with mandatory variables

RESULT=$(SLACK_HOOK_FOR_CANDIDATES=slack \
    $(dirname $0)/rel-candidate-info.sh)

# Verify results

EXPECTED='Sending following message to Slack:
---8<---
*New release of test-project available*

Content compared to the last version (TAG-1):
OR-1 Foo
OR-2: Bar
OR-2: Oops
Something else
 OR-3 Baz

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
