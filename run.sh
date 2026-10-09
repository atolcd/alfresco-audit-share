#!/bin/sh

export COMPOSE_FILE_PATH="${PWD}/target/classes/docker/docker-compose.yml"

if [ -z "${M2_HOME}" ]; then
  export MVN_EXEC="mvn"
else
  export MVN_EXEC="${M2_HOME}/bin/mvn"
fi

start() {
    docker volume create audit-share-acs-volume
    docker volume create audit-share-db-volume
    docker volume create audit-share-es-volume
    docker volume create audit-share-amq_conf
    docker volume create audit-share-amq_data
    docker volume create audit-share-amq_logs
    docker compose -f "$COMPOSE_FILE_PATH" up --build -d
}

start_share() {
    docker compose -f "$COMPOSE_FILE_PATH" up --build -d audit-share-share
}

start_acs() {
    docker compose -f "$COMPOSE_FILE_PATH" up --build -d audit-share-acs
}

start_es() {
    docker volume create audit-share-es-volume
    docker compose -f "$COMPOSE_FILE_PATH" up --build -d audit-share-es
}

down() {
    if [ -f "$COMPOSE_FILE_PATH" ]; then
        docker compose -f "$COMPOSE_FILE_PATH" down
    fi
}

purge() {
    docker volume rm -f audit-share-acs-volume
    docker volume rm -f audit-share-db-volume
    docker volume rm -f audit-share-es-volume
    docker volume rm -f audit-share-amq_conf
    docker volume rm -f audit-share-amq_data
    docker volume rm -f audit-share-amq_logs
}

purge_es() {
    docker compose -file "$COMPOSE_FILE_PATH" kill elasticsearch
    yes | docker compose -f "$COMPOSE_FILE_PATH" rm -f elasticsearch
    docker volume rm -f form-extras-es-volume
}

build() {
    $MVN_EXEC clean package
}

build_share() {
    docker compose -f "$COMPOSE_FILE_PATH" kill audit-share-share
    yes | docker compose -f "$COMPOSE_FILE_PATH" rm -f audit-share-share
    $MVN_EXEC clean package -pl audit-share-share,audit-share-share-docker
}

build_acs() {
    docker compose -f "$COMPOSE_FILE_PATH" kill audit-share-acs
    yes | docker compose -f "$COMPOSE_FILE_PATH" rm -f audit-share-acs
    $MVN_EXEC clean package -pl audit-share-platform,audit-share-platform-docker
}

tail() {
    docker compose -f "$COMPOSE_FILE_PATH" logs -f
}

tail_acs() {
    docker compose -f "$COMPOSE_FILE_PATH" logs -f audit-share-acs
}

tail_share() {
    docker compose -f "$COMPOSE_FILE_PATH" logs -f audit-share-share
}

tail_ass() {
    docker compose -f "$COMPOSE_FILE_PATH" logs -f audit-share-ass
}

tail_all() {
    docker compose -f "$COMPOSE_FILE_PATH" logs --tail="all"
}

ssh_acs() {
    docker exec -it docker_audit-share-acs_1 /bin/bash
}

ssh_share() {
    docker exec -it docker_audit-share-share_1 /bin/bash
}

reindexing() {
    docker compose down -f "$COMPOSE_FILE_PATH" reindexing
    docker compose -f "$COMPOSE_FILE_PATH" up -d reindexing
    docker compose -f "$COMPOSE_FILE_PATH" logs -f reindexing
}

case "$1" in
  build_start)
    down
    build
    start
    tail
    ;;
  start)
    start
    tail
    ;;
  stop)
    down
    ;;
  restart)
    down
    start
    tail
    ;;
  reset)
    down
    purge
    build
    start
    tail
    ;;
  purge)
    down
    purge
    ;;
  tail)
    tail
    ;;
  tail_acs)
    tail_acs
    ;;
  tail_share)
    tail_share
    ;;
  reload_share)
    build_share
    start_share
    tail_share
    ;;
  reload_acs)
    build_acs
    start_acs
    tail_acs
    ;;
  ssh_acs)
    ssh_acs
    ;;
  ssh_ass)
    ssh_ass
    ;;
  ssh_share)
    ssh_share
    ;;
  reindex_es)
    reindexing
    ;;
  *)
    echo "Usage: $0 {build_start|start|stop|restart|reset|purge|tail|tail_acs|tail_share|reload_share|reload_acs|ssh_acs|ssh_share|reindex_es}"
esac