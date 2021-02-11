#!/bin/bash

CI_PROJECT_NAME=${CI_PROJECT_NAME:-test-project}

REQUIRED_TOOLS="curl git"

REQUIRED_VARIABLES='SLACK_HOOK_FOR_CANDIDATES'

#
# Do we have all we need
#
declare -a MISSING_TOOLS
for TOOL in $REQUIRED_TOOLS; do
    command -v $TOOL > /dev/null || MISSING_TOOLS+=($TOOL)
done
[[ ${#MISSING_TOOLS[@]} -ne 0 ]] && 
    echo "ERROR: Please install following tools: ${MISSING_TOOLS[@]}" && 
    echo "Stopping..." && 
    exit 11

declare -a MISSING_VARIABLES
for VAR in $REQUIRED_VARIABLES; do
    [ -z ${!VAR+x} ] && MISSING_VARIABLES+=($VAR)
done
[[ ${#MISSING_VARIABLES[@]} -ne 0 ]] && 
    echo "ERROR: Please set following variables: ${MISSING_VARIABLES[@]}" && 
    echo "Stopping..." && 
    exit 12

LAST_TAG_HASH=$(git log --no-walk --tags --format='%H' | head -1)
LAST_TAG_NAME=$(git describe --tags)

COMMITS=$(git log --format='%s' ${LAST_TAG_HASH}..HEAD)

MESSAGE="*New release of ${CI_PROJECT_NAME} available*\n\nContent compared to the last version (${LAST_TAG_NAME}):\n$COMMITS\n"

# Just remove additional spaces
MESSAGE=${MESSAGE//  / }

#
# Send message about content to Slack
#
echo "Sending following message to Slack:"
echo "---8<---"
echo -e "$MESSAGE"
echo "--->8---"

curl -s -X POST -H 'Content-type: application/json' \
    --data "{\"text\":\"${MESSAGE}\"}" \
    https://hooks.slack.com/services/$SLACK_HOOK_FOR_CANDIDATES
