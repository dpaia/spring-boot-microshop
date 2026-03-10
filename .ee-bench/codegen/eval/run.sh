#!/usr/bin/env bash
set -euo pipefail

PROJECT_ROOT="${EE_BENCH_PROJECT_ROOT:-/repo}"
PATCH_FILE="${EE_BENCH_SUBMISSION_PATCH:-/ee-bench/submission/patch.diff}"
TEST_PATCH_FILE="${EE_BENCH_TEST_PATCH:-/ee-bench/eval/test_patch.diff}"

TIMESTAMP="$(date -u +%Y-%m-%dT%H:%M:%SZ)"
RUN_START=$(date +%s.%N 2>/dev/null || python3 -c "import time; print(f'{time.time():.3f}')")

cd "${PROJECT_ROOT}"
git reset --hard HEAD
git clean -fd --exclude=gradle/wrapper/gradle-wrapper.jar

_elapsed() {
  local start="$1"
  local now
  now=$(date +%s.%N 2>/dev/null || python3 -c "import time; print(f'{time.time():.3f}')")
  python3 -c "print(round(${now} - ${start}, 3))"
}

# ── Patch applied criterion ──────────────────────────────────────────────
PATCH_START=$(date +%s.%N 2>/dev/null || python3 -c "import time; print(f'{time.time():.3f}')")
PATCH_STATUS="fail"
PATCH_OUTPUT=""
if [ -f "${PATCH_FILE}" ] && [ -s "${PATCH_FILE}" ]; then
  PATCH_OUTPUT=$(git apply --ignore-whitespace "${PATCH_FILE}" 2>&1) && PATCH_STATUS="pass" || true
fi
PATCH_DURATION=$(_elapsed "${PATCH_START}")

# Apply test patch (informational only — not counted in patch_applied)
if [ -f "${TEST_PATCH_FILE}" ] && [ -s "${TEST_PATCH_FILE}" ]; then
  git apply --ignore-whitespace "${TEST_PATCH_FILE}" 2>/dev/null || true
fi

# ── Compilation criterion ────────────────────────────────────────────────
COMPILE_START=$(date +%s.%N 2>/dev/null || python3 -c "import time; print(f'{time.time():.3f}')")
COMPILE_STATUS="fail"
COMPILE_OUTPUT=""
COMPILE_OUTPUT=$(./gradlew compileJava compileTestJava --no-daemon 2>&1) && COMPILE_STATUS="pass" || true
COMPILE_DURATION=$(_elapsed "${COMPILE_START}")

# ── Test criterion ───────────────────────────────────────────────────────
TEST_START=$(date +%s.%N 2>/dev/null || python3 -c "import time; print(f'{time.time():.3f}')")
TEST_STATUS="fail"
TEST_OUTPUT=""
TEST_OUTPUT=$(bash /ee-bench/eval/scripts/run_script.sh 2>&1) || true

# Parse results with parser.py
RESULTS_JSON=$(python3 /ee-bench/eval/scripts/parser.py "${PROJECT_ROOT}" 2>/dev/null || echo '{}')
TEST_DURATION=$(_elapsed "${TEST_START}")

TOTAL_DURATION=$(_elapsed "${RUN_START}")

# Determine test pass/fail from parser output
FAILED=$(echo "${RESULTS_JSON}" | python3 -c "import sys,json; d=json.load(sys.stdin); print(d.get('failed',0))" 2>/dev/null || echo 0)
TOTAL=$(echo "${RESULTS_JSON}" | python3 -c "import sys,json; d=json.load(sys.stdin); print(d.get('total',0))" 2>/dev/null || echo 0)
[ "${FAILED}" -eq 0 ] && [ "${TOTAL}" -gt 0 ] && TEST_STATUS="pass" || true

# ── Write temp files for safe passing to Python emitter ─────────────────
printf '%s' "${PATCH_OUTPUT}" > /tmp/ee_bench_patch_output.txt
printf '%s' "${COMPILE_OUTPUT}" > /tmp/ee_bench_compile_output.txt
printf '%s' "${TEST_OUTPUT}" > /tmp/ee_bench_test_output.txt
printf '%s' "${RESULTS_JSON}" > /tmp/ee_bench_parser_results.json

# ── Emit result JSON ─────────────────────────────────────────────────────
EE_TIMESTAMP="${TIMESTAMP}" \
EE_TOTAL_DURATION="${TOTAL_DURATION}" \
EE_PATCH_STATUS="${PATCH_STATUS}" \
EE_PATCH_DURATION="${PATCH_DURATION}" \
EE_COMPILE_STATUS="${COMPILE_STATUS}" \
EE_COMPILE_DURATION="${COMPILE_DURATION}" \
EE_TEST_STATUS="${TEST_STATUS}" \
EE_TEST_DURATION="${TEST_DURATION}" \
python3 - <<'PYEOF'
import json, os, sys

MAX_OUTPUT = 50_000


def trunc(s, limit=MAX_OUTPUT):
    if not s or len(s) <= limit:
        return s or ""
    return s[:limit] + "\n... truncated ..."


def read_file(path):
    try:
        with open(path) as f:
            return f.read()
    except OSError:
        return ""


def read_json(path):
    try:
        with open(path) as f:
            return json.load(f)
    except (OSError, json.JSONDecodeError):
        return {}


patch_output = read_file("/tmp/ee_bench_patch_output.txt")
compile_output = read_file("/tmp/ee_bench_compile_output.txt")
test_output = read_file("/tmp/ee_bench_test_output.txt")
parser = read_json("/tmp/ee_bench_parser_results.json")

summary = parser.get("summary", {
    "total": parser.get("total", 0),
    "passed": parser.get("passed", 0),
    "failed": parser.get("failed", 0),
    "errors": 0,
    "skipped": 0,
})

result = {
    "schema_version": "2.0",
    "command": "run",
    "status": "success",
    "timestamp": os.environ["EE_TIMESTAMP"],
    "duration_seconds": float(os.environ["EE_TOTAL_DURATION"]),
    "criteria": [
        {
            "criterion": "patch_applied",
            "status": os.environ["EE_PATCH_STATUS"],
            "duration_seconds": float(os.environ["EE_PATCH_DURATION"]),
            "output": trunc(patch_output),
        },
        {
            "criterion": "compilation",
            "status": os.environ["EE_COMPILE_STATUS"],
            "duration_seconds": float(os.environ["EE_COMPILE_DURATION"]),
            "output": trunc(compile_output),
        },
        {
            "criterion": "tests",
            "status": os.environ["EE_TEST_STATUS"],
            "duration_seconds": float(os.environ["EE_TEST_DURATION"]),
            "summary": summary,
            "passed_tests": parser.get("passed_tests", []),
            "failed_tests": parser.get("failed_tests", []),
            "skipped_tests": parser.get("skipped_tests", []),
            "methods": parser.get("methods", []),
            "output": trunc(test_output),
        },
    ],
}

print(json.dumps(result))
PYEOF
