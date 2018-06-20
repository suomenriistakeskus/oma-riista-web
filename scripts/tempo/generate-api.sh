#!/usr/bin/env bash
set -e
cd "$(dirname "$0")" # Ensuring correct working directory

rm -rf ../../src/generated/java/com/nsftele/tempo

swagger-codegen generate \
-i tempo_net_api.json -l java -o tempo-api --library feign \
--model-package=com.nsftele.tempo.model \
--api-package=com.nsftele.tempo.api \
--invoker-package=com.nsftele.tempo \
-Dmodels,modelDocs=false,modelTests=false,apis,apiTests=false,apiDocs=false,hideGenerationTimestamp=true,dateLibrary=java8

mkdir -p ../../src/generated/java/com/nsftele
mv tempo-api/src/main/java/com/nsftele/tempo ../../src/generated/java/com/nsftele/

rm -rf tempo-api
