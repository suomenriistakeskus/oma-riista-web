# Continuous Integration and Deployment

The project has CI configuration for GitLab CI.

## Stage: Build

Building stage consists of preparing the build environment and the actual Maven build.

Required CI / CD variables:

- **TARGET_ENV**: Environment which is build: `dev` or `prod`
- **CONFIG_PROPERTIES_TARGET**: Used config properties file in the build. `filter-values.properties` for `prod`, `profiles/config.root.properties` for `dev`.
- **CONFIG_PROPERTIES_FILE**: Content of filename defined by **CONFIG_PROPERTIES_TARGET**

## Stage: Deploy

Deploy stage copies application (.war) file to AWS S3, deploys it to Elastic Beanstalk and informs about new available version in Slack.

The content information of new version is fetched from Jira by using issue IDs in Git commit messages.

Uses:

- AWS S3
- AWS Beanstalk
- GitLab Git
- Jira
- Slack

Required CI / CD variables:

- **AWS_ACCESS_KEY_ID**: ID of AWS API account
- **AWS_SECRET_ACCESS_KEY**: Key for AWS API account
- **APP_NAME**: Elastic Beanstalk application name
- **ENV_NAME**: Elastic Beanstalk environment name
- **S3_BUCKET_NAME**: S3 bucket for storing new versions
- **SLACK_HOOK**: Slack application hook for sending info messages
- **JIRA_HOST**: Hostname for Jira, e.g. company.atlassian.net
- **JIRA_CREDENTIALS**: Jira credentials as `jira_user:jira_api_token`
