#!/bin/bash

# 스크립트 실행 시 전달된 첫 번째 인자를 파일명으로 하여 해당 파일의 내용을 읽어온다.
commit_message=$(cat $1)

# 현재 브랜치의 이름을 가져온다.
branch_name=$(git branch --show-current)

# 브랜치 이름에서 'feature/'를 제거하여 실제 이슈 번호를 추출한다.
branch_message=$(echo "$branch_name" | sed -r "s/^feature\/(.*)/\1/")

# 추출한 이슈 번호에서 'ISSUE-' 키워드를 제거하여 실제 이슈 번호만 추출한다.
issue_no=$(echo "$branch_message" | sed -r "s/(.*)(ISSUE-[0-9]+).*/\2/")

# 커밋 메시지에서 이슈 번호를 제외한 브랜치 이름의 나머지 부분을 추출한다.
commit_message_branch=$(echo "$branch_message" | sed -r "s/^ISSUE-[0-9]+(.*)/\1/")

# 만약 커밋 메시지가 'x' 또는 'ㅌ'로 시작하거나 빈 문자열인 경우, 브랜치 이름에서 추출한 나머지 부분을 커밋 메시지로 사용한다.
if [ "${commit_message:0:1}" = "x" ] || [ "${commit_message:0:1}" = "ㅌ" ] || [ "${commit_message:0:1}" = "" ] ; then
    commit_message="$commit_message_branch"
fi

# 만약 커밋 메시지가 대괄호('[')로 시작하지 않는 경우, 이슈 번호를 추가하여 커밋 메시지를 변경한다.
if [ "${commit_message:0:1}" != "[" ] ; then
    if [ "${branch_message}" = "${issue_no}" ] ; then
        commit_message="[NO-ISSUE] ${commit_message}"
    else
        commit_message="[${issue_no}] ${commit_message}"
    fi
fi

# 변경된 커밋 메시지를 첫 번째 인자로 전달된 파일에 덮어씌운다.
echo "$commit_message" > $1

# 프로그램을 정상적으로 종료한다.
exit 0
