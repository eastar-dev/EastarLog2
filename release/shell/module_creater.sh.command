#!/bin/bash

clear

find_file_in_parent_dir() {
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

find_folders_with_file() {
    local _root_dir="$1"
    local _module_root_file="$2"

    local folder_list
    folder_list=($(find "$_root_dir" -type f -name "$_module_root_file" -exec dirname {} \; | sort -u))
    echo "${folder_list[@]}"
}

module_root_file="build.gradle.kts"
project_root_file="gradlew"
this_folder="$(cd "$(dirname "$0")" && pwd -P)"
project_root=$(find_file_in_parent_dir "$this_folder" "$project_root_file")

#모듈입력받기
echo "Enter the name for new module: "
read -p "?:" module_name
echo
#module_name="base/test"
#rm -r "$project_root/$module_name"

source_path="$this_folder/module_template"
target_path="$project_root/$module_name"
#echo "s:$source_path"
#echo "t:$target_path"

#source 유효성 체크 -------------------------------------------------------------
if [ ! -d "$source_path" ]; then
    echo "error:디렉토리가 아닌 유효한 경로가 아닙니다."
    echo
    exit 1
fi

#target 유효성 체크 -------------------------------------------------------------
modules=($(find_folders_with_file "$target_path" "$module_root_file"))
#echo "${modules[@]}"
for element in "${modules[@]}"; do
#    echo "${element#*${project_root}/}"
#    echo "$module_name"
    if [ "$module_name" == "${element#*${project_root}/}" ]; then
        echo "error:$module_name 이미 존재하는 모듈입니다."
        echo
        exit 1
    fi
done


if [ -d "$target_path" ]; then
    echo "해당 경로에 폴더가 존재합니다. 덮어쓸까요? (Y/n)"
    read -n 1 -p "?: " input
    echo
    if [[ "$input" == "Y" || "$input" == "y" || "$input" == "" ]]; then
        echo "$target_path 폴더에 덮어씁니다."
    else
        echo "작업을 종료합니다."
        echo
        exit 1
    fi
fi

#모듈복사 -------------------------------------------------------------
#echo "s:$source_path"
#echo "t:$target_path"
#echo "mkdir -p $target_path && cp -r -v $source_path/. $target_path"
mkdir -p "$target_path" && cp -r -v "$source_path/." "$target_path"

#후처리 -------------------------------------------------------------
mudule_package=$(basename "$target_path" /)
#echo $mudule_package

sed -i '' "s/__package__/$mudule_package/g" "$target_path/build.gradle.kts" "$target_path/src/androidTest/java/com/naver/cafe/__package__/ExampleInstrumentedTest.kt" "$target_path/src/test/java/com/naver/cafe/__package__/ExampleUnitTest.kt"
mv "$target_path/src/androidTest/java/com/naver/cafe/__package__" "$target_path/src/androidTest/java/com/naver/cafe/$mudule_package"
mv "$target_path/src/test/java/com/naver/cafe/__package__" "$target_path/src/test/java/com/naver/cafe/$mudule_package"
exit 0
