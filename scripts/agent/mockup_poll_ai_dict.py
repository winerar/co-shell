#!/usr/bin/env python3
"""Poll boards (#584): wording of the grey caption when the category comes from the brand dictionary (the grey line itself is decided — poll 2026-11-20, «А»).

Usage: python3 scripts/agent/mockup_poll_ai_dict.py <out-dir>  → <out-dir>/img/*.png   then   agent.sh poll docs/design/poll-ai-584-dict.json --img <out-dir>/img
"""
import os
import subprocess
import sys

HERE = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, HERE)
import mockup_nav_poll as N  # noqa: E402
import mockup_poll_ai_entry as E  # noqa: E402
import mockup_poll_ai_parts as X  # noqa: E402
import mockup_poll_update as U  # noqa: E402


def board(text, icon):
    def fn(t, v):
        return E.sheet(t, E.head(t), E.amount(t), X.cat_card(t, 1, X.caption(t, text, icon)), chips=False)
    return fn


BOARDS = [('DictA', board('по названию', 'hist')), ('DictB', board('по названию места', 'hist')), ('DictC', board('подставлено само', 'hist')),
          ('DictHist', board('по прошлым операциям', 'hist'))]


def main():
    out = sys.argv[1]
    os.makedirs(os.path.join(out, 'html'), exist_ok=True)
    for name, fn in BOARDS:
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
