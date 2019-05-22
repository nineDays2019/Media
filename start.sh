#!/usr/bin/env bash
#echo $1 支持 从命令行传入命令，然后执行
./gradlew installDebug -q -Pcmd="$1"