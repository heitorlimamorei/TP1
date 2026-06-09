#!/bin/sh
set -eu

project_dir=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
build_dir=$(mktemp -d)
runtime_dir=$(mktemp -d)

cleanup() {
    rm -rf "$build_dir" "$runtime_dir"
}
trap cleanup EXIT

javac --release 11 -d "$build_dir" \
    $(find "$project_dir/src" "$project_dir/tests" -name '*.java' | sort)

(
    cd "$runtime_dir"
    java -ea -cp "$build_dir" entrepairs.TestRunner
)
