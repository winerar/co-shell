#!/usr/bin/env python3
"""Convert Material Symbols SVGs (npm @material-symbols/svg-300, outlined) to Android vector drawables.

Usage: material-symbol-to-vector.py <svg-dir> <res/drawable dir> name[:mirrored] ...
Each icon `name` is written as `ic_<name>.xml`. See docs/agents/code-style.md (Icons).
"""
import re
import sys
from pathlib import Path

TEMPLATE = """<?xml version="1.0" encoding="utf-8"?>
<!-- Material Symbols Outlined, weight 300: {name} (Apache 2.0) -->
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp"
    android:height="24dp"
    android:autoMirrored="{mirrored}"
    android:viewportWidth="960"
    android:viewportHeight="960">
    <group android:translateY="960">
        <path
            android:fillColor="@android:color/white"
            android:pathData="{path}" />
    </group>
</vector>
"""

src, dst = Path(sys.argv[1]), Path(sys.argv[2])
for spec in sys.argv[3:]:
    name, _, flag = spec.partition(":")
    svg = (src / f"{name}.svg").read_text()
    paths = re.findall(r'<path d="([^"]+)"', svg)
    assert len(paths) == 1 and 'viewBox="0 -960 960 960"' in svg, name
    out = TEMPLATE.format(name=name, mirrored=str(flag == "mirrored").lower(), path=paths[0])
    (dst / f"ic_{name}.xml").write_text(out)
    print("ic_" + name)
