#!/bin/bash
set -eu -o pipefail

declare -r SERVER=${1:?"USAGE: $0 your.server.org"}


shopt -s nullglob
declare -r WAR_FILES=(build/libs/simlar-server*.war)

if [ ${#WAR_FILES[@]} == 0 ] ; then
    echo "no war file found"
    exit 1
fi

if [ ${#WAR_FILES[@]} != 1 ] ; then
    echo "more than one war file found:"
    for WAR_FILE in ${WAR_FILES[@]} ; do
        echo "  ${WAR_FILE}"
    done
    exit 1
fi

declare -r WAR_FILE=${WAR_FILES[0]}
echo "using war file: ${WAR_FILE}"


declare -r REMOTE="root@${SERVER}"

declare -r REMOTE_DIR=$(ssh ${REMOTE} "mktemp --directory --tmpdir simlar-server-XXXXXXXXXX")
echo "created temporary directory: ${SERVER}:${REMOTE_DIR}"

function cleanup {
    echo "removing temporary directory: ${SERVER}:${REMOTE_DIR}"
    ssh ${REMOTE} "rm -rf \"${REMOTE_DIR}\""
}
trap cleanup EXIT


declare -r VERSION_OLD=$(ssh ${REMOTE} "curl --silent http://127.0.0.1:8080/simlar-server/version")
echo "current version on server: ${VERSION_OLD}"


echo "copy war file"
scp "${WAR_FILE}" ${REMOTE}:"${REMOTE_DIR}/"


echo "install war file"
ssh ${REMOTE} "systemctl stop tomcat10.service ; rm -rf /var/lib/tomcat10/webapps/simlar-server* ; mv \"${REMOTE_DIR}\"/simlar-server*.war /var/lib/tomcat10/webapps/ ; systemctl start tomcat10.service"


echo "waiting"
for I in $(seq 1 15) ; do
    echo -n "."
    sleep 1s
done
echo


echo "check version"
declare -r VERSION_NEW=$(ssh ${REMOTE} "curl --silent http://127.0.0.1:8080/simlar-server/version")
echo "update success: ${VERSION_OLD} -> ${VERSION_NEW}"
