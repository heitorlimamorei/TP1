#!/bin/sh
set -eu

rm -rf out
mkdir -p out
javac -d out $(find src -name '*.java' | sort)
java -cp out entrepairs.app.Application
