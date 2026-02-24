#!/bin/sh
JAVA_EXE="${JAVA_HOME}/bin/java"
if ! command -v "$JAVA_EXE" > /dev/null 2>&1; then
    JAVA_EXE=java
fi
exec "$JAVA_EXE" -classpath "$(dirname "$0")/gradle/wrapper/gradle-wrapper.jar" org.gradle.wrapper.GradleWrapperMain "$@"
