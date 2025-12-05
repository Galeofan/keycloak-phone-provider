#!/bin/bash

docker buildx build ./target --platform linux/amd64 --load -t galeofan/keycloak:${version.keycloak}_phone-${project.version}