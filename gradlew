#!/usr/bin/env bash
set -euo pipefail

APP_HOME="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
WRAPPER_JAR="$APP_HOME/gradle/wrapper/gradle-wrapper.jar"

if [[ -n "${JAVA_HOME:-}" && -x "$JAVA_HOME/bin/java" ]]; then
  JAVA_EXE="$JAVA_HOME/bin/java"
elif [[ -x "$HOME/opt/android-studio/jbr/bin/java" ]]; then
  JAVA_EXE="$HOME/opt/android-studio/jbr/bin/java"
else
  JAVA_EXE="$(command -v java)"
fi

exec "$JAVA_EXE" \
  -Dorg.gradle.appname=gradlew \
  -classpath "$WRAPPER_JAR" \
  org.gradle.wrapper.GradleWrapperMain \
  "$@"
