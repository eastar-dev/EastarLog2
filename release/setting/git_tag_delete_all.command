cd $(dirname $0)   # 현재 스크립트가 위치한 디렉토리로 이동

git tag -d $(git tag --list '[a-z]*')   # 이전에 만들어진 태그 중에서 소문자 알파벳으로 시작하는 태그를 모두 삭제

git ls-remote --tags origin --list 'refs/tags/[a-z]*' | awk '!/(})/ { print ":"$2 }' | xargs git push origin
# 원격 저장소의 태그 리스트 중에서 소문자 알파벳으로 시작하는 태그 리스트를 가져와서 삭제
# awk 명령어를 사용하여 중괄호를 포함하지 않는 태그 리스트 항목 출력
# xargs를 사용하여 출력된 결과를 git push origin 명령어와 함께 실행하여 원격 저장소에서 태그 삭제

# read -s -n1 -p "Press any key to continue...." keypress
# 사용자로부터 입력을 받을 수 있도록 대기하는 코드, 주석 처리되어 있음

exit 0
