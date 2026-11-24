#!/usr/bin/env python3
"""Prints the version name of a release build in calendar form YY.M.N, e.g. "26.12.1".

Usage: version_name.py [repo-dir]  (default: current directory; HEAD is the release commit on main)

Every release (and every hotfix) is a merge into main. YY.M is the UTC year and month of HEAD's commit date, N counts the
first-parent merges of that same month up to and including HEAD (the first release of a month is N=1). No manual step:
there is no `major` to raise. Version codes (the number the updater compares) are unrelated — they stay the CI run number.
CI runs it only on main and passes the result to Gradle as VERSION_NAME.
"""
import datetime
import pathlib
import subprocess
import sys


def version_name(year: int, month: int, number: int) -> str:
    return f"{year % 100}.{month}.{max(number, 1)}"


def git(repo: pathlib.Path, *args: str) -> str:
    return subprocess.run(["git", *args], cwd=repo, check=True, capture_output=True, text=True).stdout


def month_of(repo: pathlib.Path, commit: str) -> tuple:
    stamp = int(git(repo, "show", "-s", "--format=%ct", commit).strip())
    moment = datetime.datetime.fromtimestamp(stamp, datetime.timezone.utc)
    return moment.year, moment.month


def releases_in_month(repo: pathlib.Path, month: tuple) -> int:
    """First-parent merges of HEAD (newest first) that fall in `month`, stopping at the first earlier one."""
    count = 0
    for commit in git(repo, "rev-list", "--first-parent", "--merges", "HEAD").split():
        if month_of(repo, commit) != month:
            break
        count += 1
    return count


def main() -> None:
    repo = pathlib.Path(sys.argv[1] if len(sys.argv) > 1 else ".")
    year, month = month_of(repo, "HEAD")
    print(version_name(year, month, releases_in_month(repo, (year, month))))


if __name__ == "__main__":
    main()
