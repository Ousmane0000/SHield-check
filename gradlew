#!/bin/sh

# Copyright 2015 the original author or authors.
# Licensed under the Apache License, Version 2.0

set -e

DIRNAME="$(cd "$(dirname "$0")" || exit 1; pwd)"

cd "$DIRNAME" || exit 1

JARFILE="$DIRNAME/gradle/wrapper/gradle-wrapper.jar"

if [ ! -r "$JARFILE" ]; then
    mkdir -p "$DIRNAME/gradle/wrapper"
    echo "Downloading gradle-wrapper.jar ..."
    # Download from official Gradle distribution
    curl -s -o "$JARFILE" "https://services.gradle.org/distributions/gradle-8.0-wrapper.jar" || \
    wget -q -O "$JARFILE" "https://services.gradle.org/distributions/gradle-8.0-wrapper.jar"
fi

JAVA_CMD="java"
if [ -n "$JAVA_HOME" ]; then
  JAVA_CMD="$JAVA_HOME/bin/java"
fi

exec "$JAVA_CMD" -cp "$JARFILE" org.gradle.wrapper.GradleWrapperMain "$@"
