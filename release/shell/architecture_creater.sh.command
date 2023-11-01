#!/bin/bash

clear
source "$(cd "$(dirname "$0")" && pwd -P)"/module_mover_function.sh

check_source_path

template_path="${this_folder}/architecture_template/"

rsync -av --progress --recursive --include '*/' --exclude '*' "${template_path}" "${source_pathfile}"
