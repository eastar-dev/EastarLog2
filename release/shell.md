## [주요 ADB 명령어 모음]

### adb shell pm list packages -f

```shell
adb shell pm revoke dev.eastar.log.demo android.permission.POST_NOTIFICATIONS
adb shell pm clear-permission-flags dev.eastar.log.demo android.permission.POST_NOTIFICATIONS user-set
adb shell pm clear-permission-flags dev.eastar.log.demo android.permission.POST_NOTIFICATIONS user-fixed
```

### 앱 초기화 지우고 새로 설치 한것처럼 만들어 줌

```shell
adb shell pm clear dev.eastar.log.demo
```
