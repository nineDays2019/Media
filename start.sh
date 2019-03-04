#!/usr/bin/env bash
#echo $1
./gradlew installDebug -q -Pcmd="$1"