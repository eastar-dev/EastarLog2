#!/bin/bash

# 클립보드에서 복사한 텍스트를 가져옵니다.
clipData=$(pbpaste)

# 'feature/'로 시작하는 텍스트를 추출하여 'branchText' 변수에 저장합니다.
branchText=$(echo "$clipData" | sed -r "s/^feature\/(.*)/\1/")

# '#'으로 시작하는 부분을 추출하여 해당 부분을 앞으로 이동시키고 뒤에 공백을 추가합니다.
branchText=$(echo "$branchText" | sed -r "s/(.*)(#[0-9]+)/\2 \1/")

# 일부 특수문자 및 단어를 제거하고 모든 줄바꿈 문자를 공백으로 대체합니다.
branchText=$(echo "$branchText" | sed -e 's/\n/ /g' -e 's/[[]/ /g' -e 's/[]]/ /g' -e 's/[\]/ /g' -e 's/[,]/ /g' -e 's/[&]/ /g' -e 's/["]/ /g' -e 's/[.]/ /g' -e 's/[/]/ /g' -e 's/[\]/ /g' -e 's/Android//g' -e 's/android//g' -e 's/[>]/ /g')

# 브랜치 이름의 끝에 공백이 있다면 이를 제거합니다.
branchText=$(echo "$branchText" | sed -r 's/ $//')

# 'feature/'를 추가하여 새로운 브랜치 이름을 생성합니다.
branchText="feature/"$branchText

# 새로운 브랜치 이름을 클립보드에 복사합니다.
echo -n "$branchText" | pbcopy

# 프로그램을 정상적으로 종료합니다.
exit 0
