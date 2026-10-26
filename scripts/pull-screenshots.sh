#!/bin/sh
# 在模拟器存活期间把 ScreenshotTest 生成到应用内部目录的 PNG 拉到宿主机。
# 用 run-as 而不是 adb pull /sdcard/...：Android 11+ 对外部目录有访问限制。
set -u
PKG="com.liferemaining.app.debug"
mkdir -p screenshots

NAMES=$(adb shell run-as "$PKG" ls files/screenshots 2>/dev/null | tr -d '\r' || true)
if [ -z "$NAMES" ]; then
  echo "no screenshots found under files/screenshots"
  exit 0
fi

for name in $NAMES; do
  adb exec-out run-as "$PKG" cat "files/screenshots/$name" > "screenshots/$name" 2>/dev/null || true
done

ls -la screenshots || true
