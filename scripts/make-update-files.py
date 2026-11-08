#!/usr/bin/env python3
"""Prepares the self-update files that CI attaches to a release of the public releases repo.

Usage: make-update-files.py <apk> <versionCode> <versionName> <whats-new.md> <apk-base-url> <out-dir>

Copies the APK as co-shell-<versionCode>.apk and writes version.json next to it
(format: app/.../update/data/ReleaseManifest.kt). "apk" in version.json is
<apk-base-url>/co-shell-<versionCode>.apk — a fixed release URL, so a newer release published
in between cannot break a download. Notes are the newest "## " section of whats-new.md.
"""
import hashlib
import json
import pathlib
import shutil
import sys


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


def main() -> None:
    apk, code, name, notes_file, base_url, out = sys.argv[1:7]
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
    print(json.dumps({k: v for k, v in manifest.items() if k != "notes"}))


if __name__ == "__main__":
    main()
