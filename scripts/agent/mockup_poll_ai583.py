#!/usr/bin/env python3
"""Poll boards for the «ИИ на телефоне» story (#583): standalone PNGs, dark + `@light`.

Usage: python3 scripts/agent/mockup_poll_ai583.py <out-dir>  → <out-dir>/img/*.png   then   agent.sh poll docs/design/poll-ai-583.json --img <out-dir>/img
Boards live in `mockup_poll_ai_entry.py` (category captions, voice, receipt) and `mockup_poll_ai_more.py` (package, notes, drafts, notifications, hints).
"""
import os
import subprocess
import sys

HERE = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, HERE)
import mockup_nav_poll as N  # noqa: E402
import mockup_poll_ai_entry as E  # noqa: E402
import mockup_poll_ai_more as M  # noqa: E402
import mockup_poll_update as U  # noqa: E402

BOARDS = E.BOARDS + M.BOARDS


def main():
    out = sys.argv[1]
    only = set(sys.argv[2:])
    os.makedirs(os.path.join(out, 'html'), exist_ok=True)
    for name, fn in BOARDS:
        if only and name not in only:
            continue
        for suffix, dark in (('', True), ('@light', False)):
            t = U.theme(dark)
            open(os.path.join(out, 'html', name + suffix + '.html'), 'w', encoding='utf-8').write(N.page(fn(t, name), 390, 844, dict(page=t['page'])))
    js = os.path.join(out, 'render.js')
    src = open(os.path.join(HERE, 'mockup_poll_update.py'), encoding='utf-8').read()
    open(js, 'w').write(src[src.index("'''const") + 3:src.index("''')")].replace('\\\\d', '\\d'))
    subprocess.run(['node', js, os.path.abspath(out)], check=True, cwd=out)
    print('boards → %s/img' % out)


if __name__ == '__main__':
    main()
