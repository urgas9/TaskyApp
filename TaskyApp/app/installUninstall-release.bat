SET packageName=si.uni_lj.fri.taskyapp
SET apkFile=app-release.apk

adb uninstall %packageName%

adb install %apkFile%

adb shell monkey -p %packageName% -c android.intent.category.LAUNCHER 1

timeout /t -1