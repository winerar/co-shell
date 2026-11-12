"""Interactivity for generated boards: tap targets, states and flags built from plain bricks.

Markup names drive the logic — proto() scans the board for these holes and writes the JS itself:
  {{go_G_V}} — set group G to V (tap)     {{is_G_V}} — group G equals V (for <sc-if>)
  {{tg_F}}   — toggle flag F (tap)        {{on_F}} / {{off_F}} — flag F is set / not set
Group and flag names are lowercase letters only; values are letters/digits. Defaults: proto(..., st='acc', f=True).
Only bricks go inside: state = which bricks are shown, so every state stays on components and follows the theme.
"""
import re

from mockup_bricks import board


def tap(inner, action):
    """Make bricks clickable without changing their layout."""
    return '<div style="display: contents; cursor: pointer;" onClick="{{%s}}">%s</div>' % (action, inner)


def go(group, value, inner):
    return tap(inner, 'go_%s_%s' % (group, value))


def flip(flag, inner):
    return tap(inner, 'tg_' + flag)


def when(cond, inner):
    return '<sc-if value="{{%s}}" hint-placeholder-val="{{false}}">%s</sc-if>' % (cond, inner)


def per(group, values, render):
    """One copy of the bricks per value of the group; only the current one is shown."""
    return ''.join(when('is_%s_%s' % (group, v), render(v)) for v in values)


def toggled(flag, on_html, off_html):
    return when('on_' + flag, on_html) + when('off_' + flag, off_html)


HOLE = re.compile(r'\{\{\s*(go|is|tg|on|off)_([a-z]+)(?:_(\w+))?\s*\}\}')


def logic(html, defaults, extra=''):
    """JS object entries for renderVals(): one per hole used in the markup."""
    out = {}
    for kind, name, val in HOLE.findall(html):
        if kind in ('go', 'is') and not val:
            raise SystemExit('hole %s_%s needs a value' % (kind, name))
        d = defaults.get(name, False if kind in ('tg', 'on', 'off') else '')
        cur = "(s.%s === undefined ? %s : s.%s)" % (name, repr(d).replace('True', 'true').replace('False', 'false'), name)
        if kind == 'go':
            out['go_%s_%s' % (name, val)] = "() => this.setState({ %s: '%s' })" % (name, val)
        elif kind == 'is':
            out['is_%s_%s' % (name, val)] = "%s === '%s'" % (cur, val)
        elif kind == 'tg':
            out['tg_' + name] = '() => this.setState({ %s: !%s })' % (name, cur)
        elif kind == 'on':
            out['on_' + name] = '!!' + cur
        else:
            out['off_' + name] = '!' + cur
    entries = ', '.join('%s: %s' % kv for kv in sorted(out.items()))
    return entries + (', ' + extra if extra else '')


def proto(title, html, extra='', methods='', height=844, **defaults):
    """Interactive board: bricks + holes above. `extra` — more renderVals entries, `methods` — class members."""
    script = logic(html, defaults, extra)
    page = board(title, html, script, height)
    page = page.replace('  renderVals() { return {%s}; }' % script,
                        '%s  renderVals() { const s = this.state || {}; return {%s}; }' % (methods, script))
    return page
