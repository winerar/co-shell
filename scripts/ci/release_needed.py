"""Is a weekly release worth making? (used by .github/workflows/release.yml on the schedule)

    git diff --name-only origin/main...origin/dev | python3 scripts/ci/release_needed.py

Reads changed paths on stdin; prints `true` / `false`. Same «not code» rule as the «Detect code changes» step
of ci.yml: only docs/, *.md and scripts/agent/ changed → nothing to ship to the phones.
"""
import re
import sys

NOT_CODE = re.compile(r"^docs/|\.md$|^scripts/agent/")


def release_needed(paths):
    return any(p.strip() and not NOT_CODE.search(p.strip()) for p in paths)


if __name__ == "__main__":
    print("true" if release_needed(sys.stdin.read().splitlines()) else "false")
