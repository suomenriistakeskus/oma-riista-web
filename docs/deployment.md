## Deployment to AWS

#### filter-values.properties
Properties to configure production on build time. File is needed when building using prod profile. File is not provided, create it to project root.

File should contain following properties:
- aws.config.s3location
  - contains s3 url to properties file which overrides properties in production env
- cgpsmapper_uri
  - location of the cGPSMapper software bundle
- dd_conf.api.key
  - Datadog API key, if used


#### WAR
Use shell script build_prod.sh to create deployable war.

#### Deploy
Use shell script deploy-amazon.sh to deploy.
