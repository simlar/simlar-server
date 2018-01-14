#!/bin/bash
set -eu -o pipefail

declare -r  SERVER=${1:?"USAGE: $0 your.server.org"}

echo "copy war file"
scp build/libs/simlar-server*.war root@"${SERVER}":/tmp/

echo "install war file"
ssh root@"${SERVER}" "rm /var/lib/tomcat8/webapps/simlar-server*.war ; mv /tmp/simlar-server*.war /var/lib/tomcat8/webapps/"

echo "wait"
sleep 15s

echo "check version"
ssh root@"${SERVER}" "curl http://127.0.0.1:8080/simlar-server/version"
