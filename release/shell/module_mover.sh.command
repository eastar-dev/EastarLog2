#!/bin/bash

clear
source "$(cd "$(dirname "$0")" && pwd -P)"/module_mover_function.sh

check_source_path

all_module
#echo "modules= ${modules[*]}"
filter_strings=("/base/" "$project_root/app" "/domain/" "/data/" "/feature/" "/presentation/")

param1=$(IFS=","; echo "${modules[*]}")
param2=$(IFS=","; echo "${filter_strings[*]}")
filtered_array=$(filter_list "$param1" "$param2")
modules=($filtered_array)

select_target_module

move_files
