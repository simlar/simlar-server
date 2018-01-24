#!/bin/bash
set -eu -o pipefail

declare -r SERVER=${1:?"USAGE: $0 your.server.org"}

declare -r REMOTE="root@${SERVER}"

declare -r REMOTE_DIR=$(ssh root@"${SERVER}" "mktemp -d -t simlar-server-XXXXXXXX")
echo "created temporary directory: ${REMOTE}:${REMOTE_DIR}"

function cleanup {
    ssh ${REMOTE} "rm -rf \"${REMOTE_DIR}\""
    echo "removed temporary directory: ${REMOTE}:${REMOTE_DIR}"
}
trap cleanup EXIT


echo "build war file with version: $(git describe --tags --always)"
./gradlew clean war

echo -e "\n\n"
echo "copy war file"
scp build/libs/simlar-server*.war ${REMOTE}:"${REMOTE_DIR}/"

echo "install war file"
ssh ${REMOTE} "rm /var/lib/tomcat8/webapps/simlar-server*.war ; mv "${REMOTE_DIR}"/simlar-server*.war /var/lib/tomcat8/webapps/"

echo "wait"
sleep 15s

echo "check version"
ssh ${REMOTE} "curl --silent http://127.0.0.1:8080/simlar-server/version"
