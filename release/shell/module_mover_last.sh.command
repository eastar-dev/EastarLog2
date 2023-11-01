#!/bin/bash

clear
source "$(cd "$(dirname "$0")" && pwd -P)"/module_mover_function.sh

check_source_path

last_target=$(head -n 1 "${this_folder}/module_mover_last_target.txt")
if [ -z "$last_target" ]; then
  all_module
else
  modules=("$last_target")
fi

select_target_module

move_files

exit 0
