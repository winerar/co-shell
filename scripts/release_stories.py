#!/usr/bin/env python3
"""Release stories gate and brief (#693): every release brings its own set of stories (`release/ReleaseStories`, new `KEY`).

    python3 scripts/release_stories.py check [--base origin/main] [--head HEAD]   exit 3: a release is pending but the set is the old one
    python3 scripts/release_stories.py brief [--head HEAD]                        the top whats-new section as numbered items + how to build the set
    python3 scripts/release_stories.py set-key <release.2026-12-01>               write the new KEY into ReleaseStories.kt

`check` is used by .github/workflows/release.yml (a warning on the weekly run, an error on the button unless «без историй») and by
`agent.sh stories`. A release is pending when the top section of docs/whats-new.md differs from the base's.
"""
import argparse
import os
import re
import subprocess
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from release_notes import top_section  # noqa: E402

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
WHATS_NEW = "docs/whats-new.md"
STORIES = "app/src/main/kotlin/app/coshell/release/ReleaseStories.kt"
ART = "scripts/agent/story_art_693.py"
KEY = re.compile(r'const val KEY = "([^"]+)"')
MAX = 5


def story_key(kotlin):
    match = KEY.search(kotlin)
    return match.group(1) if match else None


def release_pending(base_whats_new, head_whats_new):
    """True when dev has a whats-new section that main has not shipped yet."""
    head = top_section(head_whats_new)[0]
    return head is not None and head != top_section(base_whats_new)[0]


def check(base_whats_new, head_whats_new, base_kotlin, head_kotlin):
    """(ok, message): the set must change together with the release."""
    if not release_pending(base_whats_new, head_whats_new):
        return True, "no new release: stories unchanged"
    old, new = story_key(base_kotlin), story_key(head_kotlin)
    if old is None or new is None:
        return False, "ReleaseStories.KEY not found"
    if old == new:
        return False, ("release «%s» has no stories yet: KEY is still %s. Build the set — `agent.sh stories` (brief), "
                       "then `story_art_693.py --write` and a new KEY" % (top_section(head_whats_new)[0], new))
    return True, "stories ready: %s → %s" % (old, new)


def items(section_body):
    """The bullets («• …») of a whats-new section."""
    return [line[1:].strip() for line in section_body.splitlines() if line.startswith("•")]


def art_names(art_source):
    """Names of the schemes already drawn in story_art_693.py (reuse before drawing new ones)."""
    start = art_source.find("STORIES = {")
    return re.findall(r"^    '(\w+)': \[", art_source[start:], re.M) if start >= 0 else []


def git_show(ref, path):
    return subprocess.run(["git", "show", "%s:%s" % (ref, path)], cwd=ROOT, capture_output=True, text=True, check=True).stdout


def main(argv):
    parser = argparse.ArgumentParser()
    parser.add_argument("mode", choices=["check", "brief", "set-key"])
    parser.add_argument("key", nargs="?")
    parser.add_argument("--base", default="origin/main")
    parser.add_argument("--head", default="HEAD")
    args = parser.parse_args(argv)
    if args.mode == "set-key":
        if not args.key or not re.fullmatch(r"release\.[\w-]+", args.key):
            sys.exit("usage: set-key release.YYYY-MM-DD")
        path = os.path.join(ROOT, STORIES)
        text = open(path, encoding="utf-8").read()
        open(path, "w", encoding="utf-8").write(KEY.sub('const val KEY = "%s"' % args.key, text, count=1))
        print("KEY = %s" % args.key)
        return 0
    head_whats, head_kt = git_show(args.head, WHATS_NEW), git_show(args.head, STORIES)
    if args.mode == "check":
        ok, message = check(git_show(args.base, WHATS_NEW), head_whats, git_show(args.base, STORIES), head_kt)
        print(("OK: " if ok else "✗ ") + message)
        return 0 if ok else 3
    heading, body = top_section(head_whats)
    print("Top section: %s · KEY now %s · pending release: %s" % (heading, story_key(head_kt),
                                                                 release_pending(git_show(args.base, WHATS_NEW), head_whats)))
    for number, item in enumerate(items(body), 1):
        print("%2d. %s" % (number, item[:150]))
    print("\nSchemes already drawn (reuse): %s" % ", ".join(art_names(git_show(args.head, ART))))
    print("Pick up to %d main changes. Per story: strings release_<name>_title/_text (strings_release.xml, Russian only),"
          " a scheme in story_art_693.py STORIES, a row in ReleaseStories.current + board Rel693S<N>." % MAX)
    print("Then: python3 scripts/agent/story_art_693.py --write; python3 scripts/release_stories.py set-key release.<date>; agent.sh l10n --write")
    return 0


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
