#!/usr/bin/env python3
"""Prepares the self-update files that CI attaches to a release of the public releases repo.

Usage: make-update-files.py <apk> <versionCode> <versionName> <whats-new.md> <apk-base-url> <out-dir> [previous-releases.json]

Copies the APK as co-shell-<versionCode>.apk and writes version.json next to it
(format: app/.../update/data/ReleaseManifest.kt). "apk" in version.json is
<apk-base-url>/co-shell-<versionCode>.apk — a fixed release URL, so a newer release published
in between cannot break a download. Notes are the newest "## " section of whats-new.md.
releases.json — this release plus the previous history (the releases.json of the latest release, when CI got one),
newest first, at most HISTORY_SIZE: «Прошлые версии» on the update screen (#284, ReleaseHistory.kt).
"""
import datetime
import hashlib
import json
import pathlib
import shutil
import sys

HISTORY_SIZE = 20


def latest_notes(path: pathlib.Path) -> str:
    lines, inside = [], False
    for line in path.read_text(encoding="utf-8").splitlines():
        if line.startswith("## "):
            if inside:
                break
            inside = True
            continue
        if inside:
            lines.append(line)
    return "\n".join(lines).strip()


def read_history(path: str | None) -> list:
    """Previous releases.json; a missing or broken file (first release, network error) is an empty history."""
    if not path:
        return []
    try:
        data = json.loads(pathlib.Path(path).read_text(encoding="utf-8"))
    except (OSError, ValueError):
        return []
    return [r for r in data if isinstance(r, dict) and isinstance(r.get("versionCode"), int)] if isinstance(data, list) else []


def history(entry: dict, previous: list) -> list:
    """[entry] + previous without its own versionCode, newest first, at most HISTORY_SIZE."""
    rest = [r for r in previous if r["versionCode"] != entry["versionCode"]]
    return sorted([entry, *rest], key=lambda r: r["versionCode"], reverse=True)[:HISTORY_SIZE]


def main() -> None:
    apk, code, name, notes_file, base_url, out = sys.argv[1:7]
    previous = sys.argv[7] if len(sys.argv) > 7 else None
    out_dir = pathlib.Path(out)
    out_dir.mkdir(parents=True, exist_ok=True)
    apk_name = f"co-shell-{code}.apk"
    target = out_dir / apk_name
    shutil.copyfile(apk, target)
    data = target.read_bytes()
    manifest = {
        "versionCode": int(code),
        "versionName": name,
        "apk": f"{base_url.rstrip('/')}/{apk_name}",
        "sha256": hashlib.sha256(data).hexdigest(),
        "size": len(data),
        "notes": latest_notes(pathlib.Path(notes_file)),
    }
    (out_dir / "version.json").write_text(json.dumps(manifest, ensure_ascii=False, indent=2), encoding="utf-8")
    entry = {
        "versionCode": manifest["versionCode"],
        "versionName": name,
        "date": datetime.datetime.now(datetime.timezone.utc).date().isoformat(),
        "notes": manifest["notes"],
    }
    releases = history(entry, read_history(previous))
    (out_dir / "releases.json").write_text(json.dumps(releases, ensure_ascii=False, indent=2), encoding="utf-8")
    print(json.dumps({k: v for k, v in manifest.items() if k != "notes"}))


if __name__ == "__main__":
    main()
