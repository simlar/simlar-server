#!/bin/bash
set -eu -o pipefail

declare -r SERVER=${1:?"USAGE: $0 your.server.org"}

declare -r REMOTE="root@${SERVER}"

declare -r REMOTE_DIR=$(ssh root@"${SERVER}" "mktemp --directory --tmpdir simlar-server-XXXXXXXXXX")
echo "created temporary directory: ${REMOTE}:${REMOTE_DIR}"

function cleanup {
    echo "removing temporary directory: ${REMOTE}:${REMOTE_DIR}"
    ssh ${REMOTE} "rm -rf \"${REMOTE_DIR}\""
}
trap cleanup EXIT


declare -r VERSION_OLD=$(ssh ${REMOTE} "curl --silent http://127.0.0.1:8080/simlar-server/version")
echo "current version on server: ${VERSION_OLD}"


echo "build war file with version: $(git describe --tags --always)"
./gradlew clean bootWar


echo -e "\n\n"
echo "copy war file"
scp build/libs/simlar-server*.war ${REMOTE}:"${REMOTE_DIR}/"


echo "install war file"
ssh ${REMOTE} "rm /var/lib/tomcat8/webapps/simlar-server*.war ; mv "${REMOTE_DIR}"/simlar-server*.war /var/lib/tomcat8/webapps/"


echo "waiting"
for I in $(seq 1 15) ; do
    echo -n "."
    sleep 1s
done
echo


echo "check version"
declare -r VERSION_NEW=$(ssh ${REMOTE} "curl --silent http://127.0.0.1:8080/simlar-server/version")
echo "update success: ${VERSION_OLD} -> ${VERSION_NEW}"
