#!/usr/bin/env python3
"""Prints the version name of a release build, e.g. "0.37" (#268).

Usage: version_name.py [repo-dir]  (default: current directory; HEAD is the release commit on main)

Every release is a merge into main. The major comes from version.properties; the minor counts the releases since
the merge that brought this major to main (that merge is X.0, the next one X.1, ...). Raising `major` is the only
manual step: the release that carries it becomes (major).0 by itself. Legacy: if no earlier merge has another
major (0.N history), minor = merges on main's first-parent line minus `minorOffset`.
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


def git(repo: pathlib.Path, *args: str) -> str:
    return subprocess.run(["git", *args], cwd=repo, check=True, capture_output=True, text=True).stdout


def merges_of_head(repo: pathlib.Path) -> list:
    """First-parent merges of HEAD, newest first."""
    return git(repo, "rev-list", "--first-parent", "--merges", "HEAD").split()


def major_at(repo: pathlib.Path, commit: str) -> int:
    try:
        text = git(repo, "show", f"{commit}:version.properties")
    except subprocess.CalledProcessError:
        return -1
    props = {}
    for line in text.splitlines():
        if "=" in line and not line.strip().startswith("#"):
            key, value = line.split("=", 1)
            props[key.strip()] = value.strip()
    return int(props.get("major", "0"))


def releases_with_major(repo: pathlib.Path, major: int, merges: list):
    """How many of the newest merges carry `major`, or None if all of them do (history before the scheme)."""
    run = 0
    for commit in merges:
        if major_at(repo, commit) != major:
            return run
        run += 1
    return None


def main() -> None:
    repo = pathlib.Path(sys.argv[1] if len(sys.argv) > 1 else ".")
    props = read_properties(repo / "version.properties")
    major = int(props.get("major", "0"))
    merges = merges_of_head(repo)
    run = releases_with_major(repo, major, merges)
    if run is None:  # legacy: the whole history is one major
        print(version_name(major, int(props.get("minorOffset", "0")), len(merges)))
    else:
        print(f"{major}.{max(run - 1, 0)}")


if __name__ == "__main__":
    main()
