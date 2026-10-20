#!/usr/bin/env bash
# =====================================================================
#  Maven 包装脚本
#  为什么需要它：本机 Git Bash 里 MSYS 路径转换被禁用，直接跑 `mvn`
#  会把 POSIX 路径传给 Windows JVM，报「找不到或无法加载主类
#  org.codehaus.plexus.classworlds.launcher.Launcher」。
#  这里改成用 java 直接启动 classworlds，路径全部用 Windows 风格。
#  用法：bash deploy/_mvn.sh -B -DskipTests package
# =====================================================================
set -euo pipefail

JAVA_BIN="${JAVA_BIN:-/c/Program Files/Eclipse Adoptium/jdk-21.0.11.10-hotspot/bin/java.exe}"
MVN_HOME="${MVN_HOME:-D:\\BaiduNetdiskDownload\\maven\\apache-maven-3.9.16}"
MVN_PROJECT_DIR="${MVN_PROJECT_DIR:-D:\\cursor\\soul社交\\backend}"

if [ ! -f "$JAVA_BIN" ]; then
  echo "!! 找不到 java：$JAVA_BIN" >&2
  echo "   请用环境变量 JAVA_BIN 指定你的 java.exe 路径" >&2
  exit 1
fi

exec "$JAVA_BIN" \
  -classpath "$MVN_HOME\\boot\\plexus-classworlds-2.11.0.jar" \
  -Dclassworlds.conf="$MVN_HOME\\bin\\m2.conf" \
  -Dmaven.home="$MVN_HOME" \
  -Dmaven.multiModuleProjectDirectory="$MVN_PROJECT_DIR" \
  -Dfile.encoding=UTF-8 \
  org.codehaus.plexus.classworlds.launcher.Launcher "$@"
