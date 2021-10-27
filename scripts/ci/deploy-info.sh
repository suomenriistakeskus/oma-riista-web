#!/bin/bash

CI_PROJECT_NAME=${CI_PROJECT_NAME:-test-project}

REQUIRED_TOOLS="awk aws curl git grep sort uniq sed jq"

REQUIRED_VARIABLES='AWS_ACCESS_KEY_ID 
                    AWS_SECRET_ACCESS_KEY
                    AWS_REGION
                    APP_NAME 
                    ENV_NAME 
                    JIRA_CREDENTIALS 
                    JIRA_HOST 
                    SLACK_HOOK'

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

#
# Parse commits between last two GIT tags or from
# GIT hash of app in production
#
APP_IN_PROD=$(aws elasticbeanstalk describe-environments \
    --environment-names ${ENV_NAME} \
    --application-name ${APP_NAME} \
    --region ${AWS_REGION} | jq -rM '.Environments[].VersionLabel')

GIT_HASH_REGEX="app-.+-g([0-9a-z]+)"

if [[ $APP_IN_PROD =~ $GIT_HASH_REGEX ]]; then
    FROM_TAG_HASH=${BASH_REMATCH[1]}
else
    echo "WARN: No version in production found. Comparing tags."
    FROM_TAG_HASH=$(git log --no-walk --tags --format='%H' | head -2 | tail -1)
fi

TO_TAG_HASH=$(git log --no-walk --tags --format='%H' | head -1)
COMMITS=$(git log --format='%s' ${FROM_TAG_HASH}..${TO_TAG_HASH})

echo "All commits (for debugging purposes):"
echo -e "$COMMITS\n\n--------\n"

# Figure out related JIRA issues
ISSUES=$(
    echo "$COMMITS" | 
    grep -o -e '\(OR-[0-9]\+\)' | # select only JIRA IDs (OR-nnn or OR-nnn:)
    sort |                        # remove duplicate IDs
    uniq
)

LATEST_GIT_TAG=$(git describe --long --tags --first-parent)

MESSAGE="*Deployment of $CI_PROJECT_NAME to PRODUCTION started*\n\nContent of *$LATEST_GIT_TAG*:"

#
# Fetch issue summary and issue link from JIRA
#
for ISSUE in $ISSUES; do
    JIRA_RSP=$(curl -s -u "${JIRA_CREDENTIALS}" "https://${JIRA_HOST}/rest/api/2/issue/${ISSUE}?fields=summary")
    SUMMARY=$(echo "$JIRA_RSP" | jq -rM '.fields.summary') 
    URL="https://${JIRA_HOST}/browse/${ISSUE}"
    MESSAGE="$MESSAGE\n- <$URL|$ISSUE>: ${SUMMARY//\"/\\\"}"
done

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
    https://hooks.slack.com/services/$SLACK_HOOK
