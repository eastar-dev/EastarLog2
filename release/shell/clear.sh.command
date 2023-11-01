#!/bin/bash

# adb devices 명령어를 실행하여 현재 연결된 안드로이드 디바이스 목록을 변수에 저장합니다.
DEVICE_LIST=$(adb devices | grep -w device | cut -f 1)

# 디바이스 목록의 개수를 구합니다.
DEVICE_COUNT=$(echo "$DEVICE_LIST" | wc -l)

if [ "$DEVICE_COUNT" -eq 1 ]; then
  # 디바이스 목록이 1개인 경우, 해당 디바이스를 자동으로 선택합니다.
  DEVICE="$DEVICE_LIST"
  echo "Device found: $DEVICE"
else
  # 디바이스 ID와 모델명을 함께 출력합니다.
  echo "Connected devices:"
  for DEVICE in $DEVICE_LIST; do
    INDEX=$((INDEX + 1))
    MODEL=$(adb -s $DEVICE shell getprop ro.product.model | tr -d '[:space:]')
    echo "$INDEX) $DEVICE $MODEL"
  done

  # 사용자로부터 디바이스 선택을 입력받습니다.
  echo "Enter the number of the device you want to use: "
  read -n 1 DEVICE_NUMBER

  # 선택한 디바이스 ID를 변수에 저장합니다.
  DEVICE=$(echo "$DEVICE_LIST" | sed -n "$DEVICE_NUMBER p")
fi

# 선택한 디바이스 ID를 출력합니다.
echo "Selected device: $DEVICE"

# line separator
echo "----------------------------------------"

# shell pm clear 명령어를 실행하여 선택한 package clear
adb -s "$DEVICE" shell pm clear com.nhn.android.navercafe.stage

exit 0