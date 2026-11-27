"""Design-board rules that must hold on the canvas (`docs/design/mockups/*.dc.html`), checked in CI.

- The bottom bar on every board is the app's default bar: `BarTabs()` of `MainBottomBar.kt` in the same order (#140, #511, #154).
- No shadows on boards, as in the code (`NoShadowsTest`, #477): `box-shadow` only as a ring (offset and blur 0), no `drop-shadow`.
Run after `agent.sh mockups`: `python3 -m unittest discover -s scripts/agent -p 'test_mockup_rules.py'`.
"""
import glob
import os
import re
import unittest

ROOT = os.path.abspath(os.path.join(os.path.dirname(__file__), '..', '..'))
BOARDS = os.path.join(ROOT, 'docs', 'design', 'mockups')
BAR = os.path.join(ROOT, 'app', 'src', 'main', 'kotlin', 'app', 'coshell', 'navigation', 'MainBottomBar.kt')
STRINGS = os.path.join(ROOT, 'app', 'src', 'main', 'res', 'values')
ADD = 'Новая операция'


def app_tabs():
    """Russian labels of the default bar (`BarTabs()`: Обзор, the default second and third, Ещё; #154), from the string resources."""
    text = read(BAR)
    enum = text[text.index('enum class TopLevel'):text.index('/** The section of a chosen')]
    label = dict(re.findall(r'^\s+([A-Z_]+)\(.*?R\.string\.(\w+)', enum, re.S | re.M))
    second, third = re.search(r'BarTabs\(val second: TopLevel = TopLevel\.(\w+), val third: TopLevel = TopLevel\.(\w+)\)', text).groups()
    keys = [label[k] for k in ('HOME', second, third, 'MORE')]
    res = {}
    for f in glob.glob(os.path.join(STRINGS, 'strings*.xml')):
        for k, v in re.findall(r'<string name="(\w+)"[^>]*>([^<]*)</string>', read(f)):
            res[k] = v
    return [res[k] for k in keys]


def read(path):
    with open(path, encoding='utf-8') as f:
        return f.read()


def boards():
    return sorted(glob.glob(os.path.join(BOARDS, '*.dc.html')))


def navs(html):
    """aria-labels inside each `<nav aria-label="Навигация">…</nav>`."""
    return [re.findall(r'aria-label="([^"]+)"', body) for body in re.findall(r'<nav aria-label="Навигация"[^>]*>(.*?)</nav>', html, re.S)]


LENGTH = r'-?\d*\.?\d+(?:px)?'


def blurred_shadows(html):
    out = []
    for value in re.findall(r'box-shadow:\s*([^;"]+)', html):
        for shadow in re.split(r',(?![^(]*\))', value):
            nums = re.findall(LENGTH, re.sub(r'(rgba?|color-mix)\([^)]*\)|#[0-9a-fA-F]+|var\([^)]*\)|%\w', '', shadow))
            if len(nums) >= 3 and any(float(n.replace('px', '')) != 0 for n in nums[:3]):
                out.append(shadow.strip())
    if 'drop-shadow' in html:
        out.append('drop-shadow')
    return out


class MockupRulesTest(unittest.TestCase):
    def test_app_bar_has_four_tabs(self):
        self.assertEqual(4, len(app_tabs()))

    def test_every_board_bar_is_the_app_bar(self):
        tabs = app_tabs()
        bad, seen = [], 0
        for path in boards():
            for labels in navs(read(path)):
                seen += 1
                if [x for x in labels if x != ADD] != tabs:
                    bad.append('%s: %s' % (os.path.basename(path), labels))
        self.assertGreater(seen, 20, 'boards with a bottom bar not found — is the pattern stale?')
        self.assertEqual([], bad, 'bottom bar on boards != MainBottomBar %s' % tabs)

    def test_no_shadows_on_boards(self):
        bad = []
        for path in boards():
            found = blurred_shadows(read(path))
            if found:
                bad.append('%s: %s' % (os.path.basename(path), found[0]))
        self.assertEqual([], bad, 'shadows on boards (only rings: offset and blur 0)')

    def test_shadow_parser(self):
        self.assertEqual([], blurred_shadows('box-shadow: inset 0 0 0 1px #fff, 0 0 0 2px rgba(0,0,0,.5);'))
        self.assertEqual(1, len(blurred_shadows('box-shadow: 0 6px 18px rgba(0,0,0,0.35);')))


if __name__ == '__main__':
    unittest.main()
