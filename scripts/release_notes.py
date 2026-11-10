"""Title and body of the release PR dev → main (used by .github/workflows/release.yml).

    python3 scripts/release_notes.py [--base origin/main] [--head origin/dev] [--allow-no-whats-new]

Prints the title on the first line and the body after it. The body has the family section from
docs/whats-new.md, the PRs merged into dev since the last release and `Fixes #N` collected from
their descriptions (so the issues close when the release PR is merged into main).
Exit code 2: the top whats-new section is the same as in the base (already released).
"""

import argparse
import json
import re
import subprocess
import sys

WHATS_NEW = "docs/whats-new.md"
FIXES = re.compile(r"\b(?:fix(?:es|ed)?|close[sd]?|resolve[sd]?)\s+#(\d+)", re.IGNORECASE)
MERGED_PR = re.compile(r"^Merge pull request #(\d+) from \S+")


def top_section(text):
    """(heading, body) of the first `## ` section of whats-new, or (None, "")."""
    heading, lines = None, []
    for line in text.splitlines():
        if line.startswith("## "):
            if heading is not None:
                break
            heading = line[3:].strip()
        elif heading is not None:
            lines.append(line)
    return heading, "\n".join(lines).strip()


def merged_prs(merge_subjects):
    """PR numbers from `git log --merges --format=%s`, oldest first, without duplicates."""
    numbers = []
    for subject in reversed(merge_subjects):
        match = MERGED_PR.match(subject.strip())
        if match and int(match.group(1)) not in numbers:
            numbers.append(int(match.group(1)))
    return numbers


def fixed_issues(bodies):
    """Issue numbers referenced as `Fixes #N` (and similar keywords) in PR descriptions."""
    found = []
    for body in bodies:
        for number in FIXES.findall(body or ""):
            if int(number) not in found:
                found.append(int(number))
    return found


def compose(heading, section, prs, issues):
    title = f"Release: {heading}" if heading else "Release"
    parts = ["## Что нового (для семьи)", section or "_раздел whats-new не обновлён_"]
    if prs:
        parts += ["", "## Вошедшие PR", "\n".join(f"- #{n} {t}".rstrip() for n, t in prs)]
    if issues:
        parts += ["", "\n".join(f"Fixes #{n}" for n in issues)]
    return title, "\n".join(parts)


def _run(*args):
    return subprocess.run(args, check=True, capture_output=True, text=True).stdout


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--base", default="origin/main")
    parser.add_argument("--head", default="origin/dev")
    parser.add_argument("--allow-no-whats-new", action="store_true")
    args = parser.parse_args()

    heading, section = top_section(_run("git", "show", f"{args.head}:{WHATS_NEW}"))
    try:
        released = top_section(_run("git", "show", f"{args.base}:{WHATS_NEW}"))[0]
    except subprocess.CalledProcessError:
        released = None
    stale = heading is None or heading == released
    if stale and not args.allow_no_whats_new:
        print(f"Top section of {WHATS_NEW} is already released: {heading!r}. Add a new section "
              "or run with allow_no_whats_new.", file=sys.stderr)
        return 2
    if stale:
        heading, section = None, ""

    subjects = _run("git", "log", "--merges", "--format=%s", f"{args.base}..{args.head}").splitlines()
    prs, bodies = [], []
    for number in merged_prs(subjects):
        try:
            info = json.loads(_run("gh", "pr", "view", str(number), "--json", "title,body"))
        except (subprocess.CalledProcessError, FileNotFoundError):
            info = {"title": "", "body": ""}  # no gh access: list the PR without title and Fixes
        prs.append((number, info["title"]))
        bodies.append(info["body"])
    title, body = compose(heading, section, prs, fixed_issues(bodies))
    print(title)
    print(body)
    return 0


if __name__ == "__main__":
    sys.exit(main())
