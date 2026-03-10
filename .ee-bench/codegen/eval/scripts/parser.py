#!/usr/bin/env python3
"""Parse Gradle JUnit XML results into EE-bench format.

Extracts method-level test details including duration, failure messages,
stack traces, and skipped reasons from JUnit XML reports.
"""

import glob
import json
import os
import sys
import xml.etree.ElementTree as ET

MAX_STACKTRACE = 4096


def _truncate(text: str, limit: int) -> str:
    if not text or len(text) <= limit:
        return text or ""
    return text[:limit] + "\n... truncated ..."


def _safe_float(value: str | None, default: float = 0.0) -> float:
    if value is None:
        return default
    try:
        return float(value)
    except (ValueError, TypeError):
        return default


def find_xml_reports(project_root: str):
    """Yield (module_path, xml_file) for all test result XMLs."""
    pattern = os.path.join(project_root, "**/build/test-results/test/TEST-*.xml")
    for xml_file in glob.glob(pattern, recursive=True):
        rel = os.path.relpath(xml_file, project_root)
        parts = rel.replace(os.sep, "/").split("/")
        module_path = ":".join(parts[:-4])
        yield module_path, xml_file


def parse_results(project_root: str) -> dict:
    class_results: dict[str, dict] = {}
    methods: list[dict] = []
    total_duration = 0.0
    total_errors = 0
    total_skipped = 0

    for module_path, xml_file in find_xml_reports(project_root):
        try:
            tree = ET.parse(xml_file)
        except ET.ParseError:
            continue
        root = tree.getroot()
        suite_classname = root.get("name", "")
        suite_time = _safe_float(root.get("time"))
        total_duration += suite_time

        for tc in root.findall("testcase"):
            classname = tc.get("classname") or suite_classname
            method = tc.get("name", "")
            class_key = f"{module_path}:{classname}"
            method_key = f"{class_key}#{method}"
            duration = _safe_float(tc.get("time"))

            if class_key not in class_results:
                class_results[class_key] = {
                    "passed": True,
                    "methods_passed": [],
                    "methods_failed": [],
                }

            skipped_el = tc.find("skipped")
            if skipped_el is not None:
                total_skipped += 1
                entry: dict = {"name": method_key, "status": "skipped"}
                msg = skipped_el.get("message", "")
                if msg:
                    entry["message"] = msg
                methods.append(entry)
                continue

            failure_el = tc.find("failure")
            error_el = tc.find("error")

            if failure_el is not None or error_el is not None:
                el = failure_el if failure_el is not None else error_el
                fail_type = "assertion" if failure_el is not None else "error"
                if error_el is not None:
                    total_errors += 1

                class_results[class_key]["passed"] = False
                class_results[class_key]["methods_failed"].append(method_key)

                method_entry: dict = {
                    "name": method_key,
                    "status": "failed",
                    "duration_seconds": duration,
                    "type": fail_type,
                }
                msg = el.get("message", "")
                if msg:
                    method_entry["message"] = msg
                stacktrace = (el.text or "").strip()
                if stacktrace:
                    method_entry["stacktrace"] = _truncate(stacktrace, MAX_STACKTRACE)
                methods.append(method_entry)
            else:
                class_results[class_key]["methods_passed"].append(method_key)
                methods.append({
                    "name": method_key,
                    "status": "passed",
                    "duration_seconds": duration,
                })

    passed_tests = [{"name": k} for k, v in class_results.items() if v["passed"]]
    failed_tests = [{"name": k} for k, v in class_results.items() if not v["passed"]]

    skipped_class_keys = {m["name"].rsplit("#", 1)[0] for m in methods if m["status"] == "skipped"}
    active_class_keys = set(class_results)
    skipped_only_keys = skipped_class_keys - active_class_keys
    skipped_tests = [{"name": k} for k in sorted(skipped_only_keys)]

    return {
        "passed": len(passed_tests),
        "failed": len(failed_tests),
        "total": len(class_results),
        "passed_tests": passed_tests,
        "failed_tests": failed_tests,
        "skipped_tests": skipped_tests,
        "summary": {
            "total": len(class_results),
            "passed": len(passed_tests),
            "failed": len(failed_tests),
            "errors": total_errors,
            "skipped": total_skipped,
            "duration_seconds": round(total_duration, 3),
        },
        "methods": methods,
    }


if __name__ == "__main__":
    root = sys.argv[1] if len(sys.argv) > 1 else "/repo"
    print(json.dumps(parse_results(root)))
