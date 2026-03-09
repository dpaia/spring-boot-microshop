#!/usr/bin/env python3
"""Parse Gradle JUnit XML results into EE-bench format."""

import glob
import json
import os
import sys
import xml.etree.ElementTree as ET


def find_xml_reports(project_root: str):
    """Yield (module_path, xml_file) for all test result XMLs."""
    pattern = os.path.join(project_root, "**/build/test-results/test/TEST-*.xml")
    for xml_file in glob.glob(pattern, recursive=True):
        # Derive module path relative to project_root
        rel = os.path.relpath(xml_file, project_root)
        # rel = "microservices/product-service/build/test-results/test/TEST-*.xml"
        parts = rel.replace(os.sep, "/").split("/")
        # Drop "build/test-results/test/TEST-*.xml" — keep module path
        module_path = ":".join(parts[:-4])  # e.g. "microservices:product-service"
        yield module_path, xml_file


def parse_results(project_root: str) -> dict:
    # key = "module_path:ClassName" -> {passed: bool, methods_passed: [...], methods_failed: [...]}
    class_results: dict[str, dict] = {}

    for module_path, xml_file in find_xml_reports(project_root):
        try:
            tree = ET.parse(xml_file)
        except ET.ParseError:
            continue
        root = tree.getroot()
        suite_classname = root.get("name", "")

        for tc in root.findall("testcase"):
            classname = tc.get("classname") or suite_classname
            method = tc.get("name", "")
            key = f"{module_path}:{classname}"
            if key not in class_results:
                class_results[key] = {"passed": True, "methods_passed": [], "methods_failed": []}

            failed = tc.find("failure") is not None or tc.find("error") is not None
            skipped = tc.find("skipped") is not None
            if skipped:
                continue
            if failed:
                class_results[key]["passed"] = False
                class_results[key]["methods_failed"].append(f"{key}#{method}")
            else:
                class_results[key]["methods_passed"].append(f"{key}#{method}")

    passed_tests = [{"name": k} for k, v in class_results.items() if v["passed"]]
    failed_tests = [{"name": k} for k, v in class_results.items() if not v["passed"]]

    return {
        "passed": len(passed_tests),
        "failed": len(failed_tests),
        "total": len(class_results),
        "passed_tests": passed_tests,
        "failed_tests": failed_tests,
    }


if __name__ == "__main__":
    root = sys.argv[1] if len(sys.argv) > 1 else "/repo"
    print(json.dumps(parse_results(root)))
