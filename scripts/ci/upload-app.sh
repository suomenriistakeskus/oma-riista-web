#!/bin/bash

# Predefined by CI
CI_REPOSITORY_URL=${CI_REPOSITORY_URL:-https://usr:pwd@host/repo}
CI_COMMIT_TITLE=${CI_COMMIT_TITLE:-test commit}
GITLAB_USER_LOGIN=${GITLAB_USER_LOGIN:-gitlab.user}

# Script requirements
REQUIRED_TOOLS="aws git"

REQUIRED_VARIABLES='AWS_ACCESS_KEY_ID
                    AWS_SECRET_ACCESS_KEY
                    AWS_REGION
                    APP_NAME 
                    ENV_NAME
                    GIT_USER
                    GIT_TOKEN
                    S3_BUCKET_NAME'


# Error codes
OK=0
TOOLS_MISSING=21
REQUIRED_VARIABLES_NOT_DEFINED=22
APP_FILE_NOT_FOUND=23
APP_VERSION_MISSING=24
TAG_CREATION_FAILED=25


function check_tools {

    declare -a MISSING_TOOLS
    for TOOL in $REQUIRED_TOOLS; do
        command -v $TOOL > /dev/null || MISSING_TOOLS+=($TOOL)
    done

    [[ ${#MISSING_TOOLS[@]} -ne 0 ]] &&
        echo "ERROR: Please install following tools: ${MISSING_TOOLS[@]}" &&
        echo "Stopping..." &&
        return $TOOLS_MISSING

    return $OK
}


function check_variables {

    declare -a MISSING_VARIABLES
    for VAR in $REQUIRED_VARIABLES; do
        [ -z ${!VAR+x} ] && MISSING_VARIABLES+=($VAR)
    done
    [[ ${#MISSING_VARIABLES[@]} -ne 0 ]] &&
        echo "ERROR: Please set following variables: ${MISSING_VARIABLES[@]}" &&
        echo "Stopping..." &&
        return $REQUIRED_VARIABLES_NOT_DEFINED

    return $OK
}


function create_git_tag {

    local TAG_NAME="$1"

    git tag "$TAG_NAME" &&
        git push "https://${GIT_USER}:${GIT_TOKEN}@${CI_REPOSITORY_URL#*@}" "$TAG_NAME" &&
        return $OK

    return $TAG_CREATION_FAILED
}


function latest_git_tag {
    git describe --long --tags --first-parent
}


function upload_app_to_s3 {

    local APP_VERSION="$1"
    local APP_FILE=$(ls -1t target/riistakeskus-*.war | head -1)

    [[ -z "$APP_VERSION" ]] && return $APP_VERSION_MISSING
    [[ -z "$APP_FILE" ]] && return $APP_FILE_NOT_FOUND

    echo "Copying '${APP_FILE}' to 's3://${S3_BUCKET_NAME}/${APP_NAME}/${APP_VERSION}'"

    aws s3 cp "${APP_FILE}" "s3://${S3_BUCKET_NAME}/${APP_NAME}/${APP_VERSION}" &&
    return $OK

}


function add_version_to_eb {

    local APP_VERSION="$1"
    local S3_BUCKET_KEY="${APP_NAME}/${APP_VERSION}"
    local VERSION_LABEL="app-${APP_VERSION}"
    local DESCRIPTION="${CI_COMMIT_TITLE:0:100} - ${GITLAB_USER_LOGIN}"

    [[ -z "$APP_VERSION" ]] && return $APP_VERSION_MISSING

    echo "Create application '${APP_NAME}' version '${VERSION_LABEL}', description '${DESCRIPTION}'"

    aws elasticbeanstalk create-application-version \
        --no-auto-create-application \
        --application-name "${APP_NAME}" \
        --version-label "${VERSION_LABEL}" \
        --description "${DESCRIPTION}" \
        --region "${AWS_REGION}" \
        --source-bundle S3Bucket="${S3_BUCKET_NAME}",S3Key="${S3_BUCKET_KEY}" &&
    return $OK

}

##   ##  ####  #### ##  ##
### ### ##  ##  ##  ### ##
## # ## ######  ##  ## ###
##   ## ##  ## #### ##  ##

NEW_TAG="$(date "+%Y-%m-%d_%H%M")"

check_tools                             &&
check_variables                         &&
create_git_tag      "$NEW_TAG"          &&
upload_app_to_s3    "$(latest_git_tag)" &&
add_version_to_eb   "$(latest_git_tag)"
