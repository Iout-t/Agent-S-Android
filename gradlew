#!/usr/bin/env bash
set -eu

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
WRAPPER_JAR="$SCRIPT_DIR/gradle/wrapper/gradle-wrapper.jar"

if [ -f "$WRAPPER_JAR" ]; then
  exec java -classpath "$WRAPPER_JAR" org.gradle.wrapper.GradleWrapperMain "$@"
fi

if ! command -v gradle >/dev/null 2>&1; then
  echo "Gradle is not installed and the wrapper JAR is missing." >&2
  echo "Install Gradle or restore the Gradle wrapper files." >&2
  exit 1
fi

exec gradle "$@"
