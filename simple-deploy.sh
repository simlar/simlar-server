#!/bin/bash
set -eu -o pipefail

declare -r  SERVER=${1:?"USAGE: $0 your.server.org"}

scp build/libs/simlar-server*.war root@"${SERVER}":/tmp/
ssh root@"${SERVER}" "rm /var/lib/tomcat8/webapps/simlar-server*.war ; mv /tmp/simlar-server*.war /var/lib/tomcat8/webapps/"
sleep 15s
ssh root@"${SERVER}" "curl http://127.0.0.1:8080/simlar-server/version"
