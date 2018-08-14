#!/bin/bash

## exit on errors or unset variables
set -eu -o pipefail

declare -r PROJECT_DIR="$(dirname $(readlink -f $0))"

declare -r INTELLIJ_SOURCE=${1:-"${PROJECT_DIR}/.idea/dictionaries/$(whoami).xml"}

declare -r DESTINATION="${PROJECT_DIR}/ides/intellij/dictionaries/simlar.dic"
declare -r TEMP_FILE="${DESTINATION}.tmp"

cp "${DESTINATION}" "${TEMP_FILE}"
sed -n "s/^[[:space:]]*<w>//p" "${INTELLIJ_SOURCE}" | sed "s/<\/w>//" >> "${TEMP_FILE}"
sort "${TEMP_FILE}" | uniq | grep -v "^$" > "${DESTINATION}"
rm "${TEMP_FILE}"
