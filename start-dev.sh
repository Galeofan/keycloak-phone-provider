#!/bin/bash
# 1) Собирает проект внешним Maven
# 2) Собирает docker image
# 3) Останавливает контейнеры если такие уже запущены
# 4) Запускает контейнеры с postgres и keycloak

set -e

mvn clean package \
  && ./target/build-template.sh \
  && docker-compose -p keycloak -f ./target/docker-compose.yml down \
  && docker-compose -p keycloak -f ./target/docker-compose.yml up -d