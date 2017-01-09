#!/usr/bin/env bash

# certificate downloaded from https://eevertti.vrk.fi/Default.aspx?id=237

JAVA_HOME="$(/usr/libexec/java_home)"

rm -f truststore.jks
cp "${JAVA_HOME}/jre/lib/security/cacerts" truststore.jks

keytool -noprompt -importcert -alias vrkrootc -keystore truststore.jks -storepass changeit -file certs/vrkrootc.crt
keytool -importcert -alias vrksp -keystore truststore.jks -storepass changeit -file certs/vrksp.crt
keytool -importcert -alias vrksp2 -keystore truststore.jks -storepass changeit -file certs/vrksp2.crt
keytool -importcert -alias vrksp3 -keystore truststore.jks -storepass changeit -file certs/vrksp3.crt

# Verify content
keytool -keystore truststore.jks -storepass changeit -list | grep vrk
