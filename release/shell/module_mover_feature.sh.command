#!/bin/bash

clear
source "$(cd "$(dirname "$0")" && pwd -P)"/module_mover_function.sh

check_source_path

modules=($(find_folders_with_file "$project_root" "$module_root_file"))
filter_strings=("/feature/" "/app")

param1=$(IFS=","; echo "${modules[*]}")
param2=$(IFS=","; echo "${filter_strings[*]}")
filtered_array=$(filter_list "$param1" "$param2")
modules=($filtered_array)

select_target_module

move_files
