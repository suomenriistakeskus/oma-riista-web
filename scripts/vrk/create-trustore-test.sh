#!/usr/bin/env bash

# certificates downloaded from https://eevertti.vrk.fi/Default.aspx?id=238

JAVA_HOME="$(/usr/libexec/java_home)"

rm -f truststore.jks
cp "${JAVA_HOME}/jre/lib/security/cacerts" truststore.jks

keytool -noprompt -importcert -alias vrkrootc -keystore truststore.jks -storepass changeit -file certs/vrktestc.crt
keytool -importcert -alias vrktp -keystore truststore.jks -storepass changeit -file certs/vrktp.crt
keytool -importcert -alias vrktp2 -keystore truststore.jks -storepass changeit -file certs/vrktp2.crt
keytool -importcert -alias vrktp3 -keystore truststore.jks -storepass changeit -file certs/vrktp3.crt

# Verify content
keytool -keystore truststore.jks -storepass changeit -list | grep vrk
