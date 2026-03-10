#!/usr/bin/env bash
set -euo pipefail

PROJECT_ROOT="${EE_BENCH_PROJECT_ROOT:-/repo}"
PATCH_FILE="${EE_BENCH_SUBMISSION_PATCH:-/ee-bench/submission/patch.diff}"
TEST_PATCH_FILE="${EE_BENCH_TEST_PATCH:-/ee-bench/eval/test_patch.diff}"

TIMESTAMP="$(date -u +%Y-%m-%dT%H:%M:%SZ)"

cd "${PROJECT_ROOT}"
git reset --hard HEAD
git clean -fd

# ── Patch applied criterion ──────────────────────────────────────────────
PATCH_STATUS="fail"
if [ -f "${PATCH_FILE}" ] && [ -s "${PATCH_FILE}" ]; then
  if git apply --ignore-whitespace "${PATCH_FILE}" 2>/dev/null; then
    PATCH_STATUS="pass"
  fi
fi

# Apply test patch (informational only — not counted in patch_applied)
if [ -f "${TEST_PATCH_FILE}" ] && [ -s "${TEST_PATCH_FILE}" ]; then
  git apply --ignore-whitespace "${TEST_PATCH_FILE}" 2>/dev/null || true
fi

# ── Compilation criterion ────────────────────────────────────────────────
COMPILE_STATUS="fail"
COMPILE_OUTPUT=""
COMPILE_OUTPUT=$(./gradlew compileJava compileTestJava --no-daemon 2>&1) && COMPILE_STATUS="pass" || true

# ── Test criterion ───────────────────────────────────────────────────────
TEST_STATUS="fail"
TEST_OUTPUT=""
TEST_OUTPUT=$(bash /ee-bench/eval/scripts/run_script.sh 2>&1) || true

# Parse results with parser.py
RESULTS_JSON=$(python3 /ee-bench/eval/scripts/parser.py "${PROJECT_ROOT}" 2>/dev/null || echo '{}')

PASSED=$(echo "${RESULTS_JSON}" | python3 -c "import sys,json; d=json.load(sys.stdin); print(d.get('passed',0))" 2>/dev/null || echo 0)
FAILED=$(echo "${RESULTS_JSON}" | python3 -c "import sys,json; d=json.load(sys.stdin); print(d.get('failed',0))" 2>/dev/null || echo 0)
TOTAL=$(echo "${RESULTS_JSON}" | python3 -c "import sys,json; d=json.load(sys.stdin); print(d.get('total',0))" 2>/dev/null || echo 0)
PASSED_TESTS=$(echo "${RESULTS_JSON}" | python3 -c "import sys,json; d=json.load(sys.stdin); print(json.dumps(d.get('passed_tests',[])))" 2>/dev/null || echo '[]')
FAILED_TESTS=$(echo "${RESULTS_JSON}" | python3 -c "import sys,json; d=json.load(sys.stdin); print(json.dumps(d.get('failed_tests',[])))" 2>/dev/null || echo '[]')

[ "${FAILED}" -eq 0 ] && [ "${TOTAL}" -gt 0 ] && TEST_STATUS="pass" || true

# ── Emit result JSON ─────────────────────────────────────────────────────
python3 - <<PYEOF
import json, sys
result = {
  "schema_version": "2.0",
  "command": "run",
  "status": "success",
  "timestamp": "${TIMESTAMP}",
  "criteria": [
    {"criterion": "patch_applied", "status": "${PATCH_STATUS}"},
    {"criterion": "compilation",   "status": "${COMPILE_STATUS}"},
    {
      "criterion": "tests",
      "status": "${TEST_STATUS}",
      "summary": {"total": ${TOTAL}, "passed": ${PASSED}, "failed": ${FAILED}, "errors": 0, "skipped": 0},
      "passed_tests": ${PASSED_TESTS},
      "failed_tests": ${FAILED_TESTS}
    }
  ]
}
print(json.dumps(result))
PYEOF
