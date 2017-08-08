#!/bin/bash
docker run -it -d \
  -p 8180:8080 \
  -v `pwd`/realm:/tmp/realm \
  jboss/keycloak:3.2.0.Final \
  -b 0.0.0.0 \
  -Dkeycloak.migration.action=import \
  -Dkeycloak.migration.provider=singleFile \
  -Dkeycloak.migration.file=/tmp/realm/wildfly-swarm-keycloak-example-realm.json
