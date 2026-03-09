#!/usr/bin/env bash
set -euo pipefail

PROJECT_ROOT="${EE_BENCH_PROJECT_ROOT:-/repo}"
SELECTED_TESTS="${EE_BENCH_SELECTED_TESTS:-}"

cd "${PROJECT_ROOT}"

run_tests() {
  # $1 = newline-separated list of "module:submodule:ClassName[#method]"
  local tests="$1"

  # Group by module:submodule
  declare -A module_tests
  while IFS= read -r entry; do
    [ -z "$entry" ] && continue
    # Split on last colon to separate module_path from class[#method]
    local module_path class_spec
    module_path="$(echo "$entry" | rev | cut -d: -f2- | rev)"
    class_spec="$(echo "$entry"  | rev | cut -d: -f1  | rev)"
    # Convert #method to .method for Gradle --tests syntax
    class_spec="${class_spec//#/.}"
    module_tests["$module_path"]+="${class_spec} "
  done <<< "$tests"

  local exit_code=0
  for mp in "${!module_tests[@]}"; do
    local gradle_task=":${mp}:test"
    local test_args=""
    for cls in ${module_tests[$mp]}; do
      test_args+=" --tests \"${cls}\""
    done
    echo "Running: ./gradlew ${gradle_task}${test_args} --no-daemon"
    eval "./gradlew ${gradle_task}${test_args} --no-daemon --continue" || exit_code=$?
  done
  return $exit_code
}

if [ -n "${SELECTED_TESTS}" ]; then
  run_tests "${SELECTED_TESTS}"
else
  # Run all tests
  ./gradlew test --no-daemon --continue || true
fi
