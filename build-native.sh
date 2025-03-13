#!/bin/bash

mkdir -p obj out

find src -name "*.java" -print0 | xargs -0 javac -d obj

$GRAALVM_HOME/bin/native-image -cp ./obj Harness -o out/harness