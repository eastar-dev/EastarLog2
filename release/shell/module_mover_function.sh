#!/bin/bash

function find_file_in_parent_dir() {
    local _current="$1"
    local _target="$2"

    while true; do
        if [ -f "$_current/$_target" ]; then
            echo "$_current"
            return 0
        fi
        if [ "$_current" = "/" ]; then
            echo "파일을 찾을 수 없습니다."
            return 1
        fi
        _current=$(dirname "$_current")
    done
}

function find_folders_with_file() {
    local _root_dir="$1"
    local _module_root_file="$2"
    local _max_depth="${3:-3}"

    local folder_list
    folder_list=($(find "$_root_dir" -maxdepth "$_max_depth" -type f -name "$_module_root_file" -exec dirname {} \; | sort -u))
    echo "${folder_list[@]}"
}

function check_path_type() {
    local _path="$1"
    echo "$_path"

    if [ -d "$_path" ]; then
        return 1 # Folder
    elif [ -f "$_path" ]; then
        return 2 # File
    else
        return 0 # Invalid path
    fi
}

# 호출방법
#_param1=$(IFS=","; echo "${array1[*]}")
#_param2=$(IFS=","; echo "${array2[*]}")
#filter_list "$_param1" "$_param2"

function filter_list() {
    local _list
    local _filter_list
    local _result=()
    IFS="," read -ra _list <<<"$1"
    IFS="," read -ra _filter_list <<<"$2"

    for _element in "${_list[@]}"; do
        for _exclude_string in "${_filter_list[@]}"; do
            if [[ "$_element" == *"${_exclude_string}"* ]]; then
                _result+=("$_element")
                break
            fi
        done
    done

    echo "${_result[@]}"
}

function exitProcess() {
    echo "$1"
    echo "$2"
    read -n 1
    echo
    exit 1
}

function check_source_path() {
    # 클립보드에서 파일 경로 읽어오기 - 시작 ------------------------------------------------------------------
    source_pathfile=$(pbpaste)

    #다중파일분리
    source_pathfile_array=()
    while IFS= read -r line; do
        source_pathfile_array+=("$line")
    done <<<"$source_pathfile"

    #echo "$source_pathfile_array"
    #source_file_count=${#source_pathfile_array[@]}
    #echo "배열의 요소 개수: $source_file_count"

    for file in "${source_pathfile_array[@]}"; do
        source_pathfile="$file"
        #echo "$source_pathfile"

        #소스파일유효성체크
        if [ -d "$source_pathfile" ]; then
            echo "폴더:${source_pathfile#*${project_root}}"
        elif [ -f "$source_pathfile" ]; then
            echo "파일:${source_pathfile#*${project_root}}"
        else
#            echo "$source_pathfile" "유효한 경로가 아닙니다."
            exitProcess "$source_pathfile" "유효한 경로가 아닙니다."
        fi
    done
    # 클립보드에서 파일 경로 읽어오기 - 끝 ------------------------------------------------------------------
}

function select_target_module() {
    #target모듈 select start -----------------------------------------------------------------------------------------------
    modules_count=${#modules[@]}
    #echo "$modules_count"
    if [ $modules_count -lt 1 ]; then
        exitProcess "오류: 모듈 개수가 1보다 작습니다."
    elif [ $modules_count -eq 1 ]; then
        selected_folder="${modules[0]}"
    else
        alphabet=("1" "2" "3" "4" "5" "6" "7" "8" "9" "0" "a" "b" "c" "d" "e" "f" "g" "h" "i" "j" "k" "l" "m" "n" "o" "p" "q" "r" "s" "t" "u" "v" "w" "x" "y" "z" "A" "B" "C" "D" "E" "F" "G" "H" "I" "J" "K" "L" "M" "N" "O" "P" "Q" "R" "S" "T" "U" "V" "W" "X" "Y" "Z")
        for i in "${!modules[@]}"; do
            #    alphabet_index=$((i % ${#alphabet[@]}))
            alphabet_index=$i
            alphabet_letter="${alphabet[alphabet_index]}"
            echo "$alphabet_letter. ${modules[$i]#*${project_root}}"
        done

        echo "Enter the number of the target module: "
        read -n 1 input_letter
        echo

        index=-1
        for i in "${!alphabet[@]}"; do
            if [ "${alphabet[$i]}" = "$input_letter" ]; then
                index=$i
                break
            fi
        done

        if [ $index -eq -1 ]; then
            exitProcess "오류: 유효하지 않은 선택입니다."
        fi

        if [ $index -ge ${#modules[@]} ]; then
            exitProcess "해당 위치에 폴더가 없습니다."
        fi
        selected_folder="${modules[$index]}"
    fi

    target_module_root=$selected_folder

    echo "${target_module_root}" >"${this_folder}/module_mover_last_target.txt"
    #target모듈 select end -----------------------------------------------------------------------------------------------
}

function move_files() {
    # Print the array elements
    for file in "${source_pathfile_array[@]}"; do
        source_pathfile="$file"

        source_module_root=$(find_file_in_parent_dir "$source_pathfile" "$module_root_file")
        project_root=$(find_file_in_parent_dir "$source_pathfile" "$project_root_file")
        source_file="${source_pathfile#*${source_module_root}}"

        target_pathfile="$target_module_root$source_file"
        target_path=$(dirname "$target_pathfile")

        #echo "dump ============================="
        #echo $project_root
        #echo $source_pathfile
        #echo $source_module_root
        #echo $source_file
        #echo $target_module_root
        #echo $target_pathfile
        #echo "${source_module_root#*${project_root}} -> ${target_module_root#*${project_root}}"
        #echo "dump ============================="

        #소스파일유효성체크
        prefix=""
        if [ -d "$source_pathfile" ]; then
            prefix="폴더:"
        elif [ -f "$source_pathfile" ]; then
            if [ -f "$target_pathfile" ]; then
                prefix="*파일:"
            else
                prefix="파일:"
            fi
        else
            echo prefix="에러:"
        fi

        # 폴더가 있는 경우는 mv 명령으로 이동을 하지 못해 복사후 지우는 것으로 변경
        echo "$prefix ${source_module_root#*${project_root}} -> ${target_module_root#*${project_root}} : $source_file"
        mkdir -p "$target_path" && cp -R "$source_pathfile" "$target_path/" && rm -r "$source_pathfile"
    done

#    exitProcess "작업이 완료되었습니다."
}

function all_module() {
    modules=($(find_folders_with_file "$project_root" "$module_root_file"))
#    echo ${modules[*]}
    filter_strings=("$project_root/") #전체
    param1=$(IFS=","; echo "${modules[*]}")
    param2=$(IFS=","; echo "${filter_strings[*]}")
    filtered_array=$(filter_list "$param1" "$param2")
    modules=($filtered_array)
#    echo ${modules[*]}
}

module_root_file="build.gradle.kts"
project_root_file="gradlew"
this_folder="$(cd "$(dirname "$0")" && pwd -P)"
project_root=$(find_file_in_parent_dir "$this_folder" "$project_root_file")

#echo "-----------------------------------------------------------------------------"
#echo "this_folder       = $this_folder"
#echo "project_root_file = $project_root_file"
#echo "module_root_file  = $module_root_file"
#echo "project_root      = $project_root"
#echo "-----------------------------------------------------------------------------"
