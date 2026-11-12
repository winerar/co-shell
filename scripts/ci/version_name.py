#!/usr/bin/env python3
"""Prints the version name of a release build, e.g. "0.37" (#268).

Usage: version_name.py [repo-dir]  (default: current directory; HEAD is the release commit on main)

Every release is a merge into main, so the minor version is the number of merges on main's
first-parent line, minus `minorOffset` from version.properties; the major comes from there too.
CI runs it only on main and passes the result to Gradle as VERSION_NAME.
"""
import pathlib
import subprocess
import sys


def read_properties(path: pathlib.Path) -> dict:
    props = {}
    for line in path.read_text(encoding="utf-8").splitlines():
        line = line.strip()
        if line and not line.startswith("#") and "=" in line:
            key, value = line.split("=", 1)
            props[key.strip()] = value.strip()
    return props


def version_name(major: int, minor_offset: int, merges: int) -> str:
    return f"{major}.{max(merges - minor_offset, 0)}"


def count_merges(repo: pathlib.Path) -> int:
    out = subprocess.run(["git", "rev-list", "--count", "--first-parent", "--merges", "HEAD"],
                         cwd=repo, check=True, capture_output=True, text=True).stdout
    return int(out.strip())


def main() -> None:
    repo = pathlib.Path(sys.argv[1] if len(sys.argv) > 1 else ".")
    props = read_properties(repo / "version.properties")
    print(version_name(int(props.get("major", "0")), int(props.get("minorOffset", "0")), count_merges(repo)))


if __name__ == "__main__":
    main()
