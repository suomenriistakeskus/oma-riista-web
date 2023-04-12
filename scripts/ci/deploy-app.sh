 #!/bin/bash

# Script requirements
REQUIRED_TOOLS="aws curl java"

REQUIRED_VARIABLES='AWS_ACCESS_KEY_ID
                    AWS_SECRET_ACCESS_KEY
                    AWS_REGION
                    CI_COMMIT_SHA
                    ENV_NAME
                    GITLAB_USER_LOGIN'

# Error codes
OK=0
TOOLS_MISSING=31
REQUIRED_VARIABLES_NOT_DEFINED=32


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

function latest_git_tag {
    git describe --long --tags --first-parent
}

function update_environment {

    local VERSION_LABEL="$1"

    aws elasticbeanstalk update-environment \
        --environment-name "${ENV_NAME}" \
        --version-label "${VERSION_LABEL}" \
        --region "${AWS_REGION}" &&
    return $OK

}

##   ##  ####  #### ##  ##
### ### ##  ##  ##  ### ##
## # ## ######  ##  ## ###
##   ## ##  ## #### ##  ##

check_tools                                                     &&
check_variables                                                 &&
update_environment  "app-$(latest_git_tag)"
