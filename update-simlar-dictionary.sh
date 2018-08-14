#!/bin/bash

## exit on errors or unset variables
set -eu -o pipefail

declare -r GSED=$(which gsed)
declare -r SED=${GSED:-"$(which sed)"}

declare -r GREADLINK=$(which greadlink)
declare -r READLINK=${GREADLINK:-"$(which readlink)"}


declare -r PROJECT_DIR="$(dirname $("${READLINK}" -f $0))"
declare -r INTELLIJ_SOURCE=${1:-"${PROJECT_DIR}/.idea/dictionaries/$(whoami).xml"}
declare -r DESTINATION="${PROJECT_DIR}/ides/intellij/dictionaries/simlar.dic"


"${SED}" -n "s/^[[:space:]]*<w>//p" "${INTELLIJ_SOURCE}" | "${SED}" "s/<\/w>//" >> "${DESTINATION}"
sort --unique "${DESTINATION}" -o "${DESTINATION}"
"${SED}" -i '/^\s*$/d' "${DESTINATION}"
