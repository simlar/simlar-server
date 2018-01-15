#!/bin/bash
set -eu -o pipefail

declare -r  SERVER=${1:?"USAGE: $0 your.server.org"}

scp build/libs/simlar-server-*.war root@"${SERVER}":/var/lib/tomcat8/webapps/simlar-server.war
