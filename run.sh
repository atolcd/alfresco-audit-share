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
    docker volume create audit-share-ass-volume
    docker-compose -f "$COMPOSE_FILE_PATH" up --build -d
}

start_share() {
    docker-compose -f "$COMPOSE_FILE_PATH" up --build -d audit-share-share
}

start_acs() {
    docker-compose -f "$COMPOSE_FILE_PATH" up --build -d audit-share-acs
}

start_ass() {
    docker volume create audit-share-ass-volume
    docker-compose -f "$COMPOSE_FILE_PATH" up --build -d audit-share-ass
}

down() {
    if [ -f "$COMPOSE_FILE_PATH" ]; then
        docker-compose -f "$COMPOSE_FILE_PATH" down
    fi
}

purge() {
    docker volume rm -f audit-share-acs-volume
    docker volume rm -f audit-share-db-volume
    docker volume rm -f audit-share-ass-volume
}

purge_ass() {
    docker-compose -f "$COMPOSE_FILE_PATH" kill audit-share-ass
    yes | docker-compose -f "$COMPOSE_FILE_PATH" rm -f audit-share-ass
    docker volume rm -f audit-share-ass-volume
}

build() {
    $MVN_EXEC clean package
}

build_share() {
    docker-compose -f "$COMPOSE_FILE_PATH" kill audit-share-share
    yes | docker-compose -f "$COMPOSE_FILE_PATH" rm -f audit-share-share
    $MVN_EXEC clean package -pl audit-share-share,audit-share-share-docker
}

build_acs() {
    docker-compose -f "$COMPOSE_FILE_PATH" kill audit-share-acs
    yes | docker-compose -f "$COMPOSE_FILE_PATH" rm -f audit-share-acs
    $MVN_EXEC clean package -pl audit-share-platform,audit-share-platform-docker
}

tail() {
    docker-compose -f "$COMPOSE_FILE_PATH" logs -f
}

tail_acs() {
    docker-compose -f "$COMPOSE_FILE_PATH" logs -f audit-share-acs
}

tail_share() {
    docker-compose -f "$COMPOSE_FILE_PATH" logs -f audit-share-share
}

tail_ass() {
    docker-compose -f "$COMPOSE_FILE_PATH" logs -f audit-share-ass
}

tail_all() {
    docker-compose -f "$COMPOSE_FILE_PATH" logs --tail="all"
}

ssh_acs() {
    docker exec -it docker_audit-share-acs_1 /bin/bash
}

ssh_share() {
    docker exec -it docker_audit-share-share_1 /bin/bash
}

ssh_ass() {
    docker exec -it docker_audit-share-ass_1 /bin/bash
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
  tail_ass)
    tail_ass
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
  reindex_ass)
    purge_ass
    start_ass
    tail_ass
    ;;
  *)
    echo "Usage: $0 {build_start|start|stop|restart|reset|purge|tail|tail_acs|tail_share|tail_ass|reload_share|reload_acs|ssh_acs|ssh_ass|ssh_share|reindex_ass}"
esac