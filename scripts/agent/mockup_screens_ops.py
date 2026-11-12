"""«Операции» (interactive, 19В): four tabs on one track — swipe or tap the bar; hold a row → selection, tap → sheet.

Rows come from JS (they are deleted / duplicated live), the markup around them is bricks with {{holes}}.
"""
import json

from mockup_parts import *  # noqa: F401,F403
from mockup_logic import tap, when
import mockup_screens_main as m
import mockup_screens_tabs as tabs

ROWS = [  # id, day, title, sub, amount, cur, kind, icon, dot colour, second line
    ('a', 0, 'Продукты', 'Galicia · Coto', '−32 000', 'ARS', 'e', 'cart', LIME, ''),
    ('b', 0, 'Кафе', 'USDT-карта · Аня', '−13,10', 'USDT', 'e', 'cafe', WARN, 'чек 13 300 ARS'),
    ('c', 1, 'Такси', 'Наличные', '−6 500', 'ARS', 'e', 'taxi', YELLOW, ''),
    ('d', 1, 'Зарплата', 'Ameria', '+1 200', 'USD', 'i', 'salary', CYAN, ''),
    ('e', 1, 'Ameria → Galicia', 'Перевод', '500', 'USD', 't', 'transfer', '#C79BFF', '512 000 ARS'),
]
KIND = {'e': PINK, 'i': CYAN, 't': INK}


def rows_js():
    return json.dumps([{'id': i, 'day': d, 'title': t, 'sub': s, 'amount': a, 'cur': c, 'amountColor': KIND[k], 'icon': ICON[ic], 'dot': col, 'second': sec}
                       for i, d, t, s, a, c, k, ic, col, sec in ROWS], ensure_ascii=False).replace('"amountColor":', 'amountColor:')  # unquoted: theme reads it as ink


def ops_panel():
    row_ = ('<div onPointerDown="{{r.down}}" style="background: {{r.bg}};">%s%s</div>'
            % (when('r.plain', m.tx('{{r.title}}', '{{r.sub}}', '{{r.amount}}', '{{r.cur}}', '{{r.amountColor}}', '{{r.icon}}', '{{r.dot}}', '{{r.second}}')),
               when('r.selected', m.tx('{{r.title}}', '{{r.sub}}', '{{r.amount}}', '{{r.cur}}', '{{r.amountColor}}', 'check', LIME, '{{r.second}}'))))
    days = ('<sc-for list="{{days}}" as="d" hint-placeholder-count="2">%s<sc-for list="{{d.rows}}" as="r" hint-placeholder-count="3">%s</sc-for></sc-for>'
            % (m.day('{{d.title}}', '{{d.total}}', '{{d.cur}}', '{{d.color}}'), row_))
    head = when('notSelecting', '<div style="display: flex; align-items: center; height: 64px; padding: 0 16px;"><h1 style="margin: 0; font-size: 28px; font-weight: 700;">Операции</h1></div>')
    sel = when('selecting', tap('<div style="display: flex; align-items: center; gap: 8px; height: 64px; padding: 0 8px;">%s<span style="font-size: 22px; font-weight: 700;">Выбрано {{selCount}}</span></div>'
                                % icon_button('close', 'Отменить выбор'), 'clearSel'))
    return head + sel + '<div style="padding: 0 16px;">%s<div style="margin: 14px -16px 0;">%s</div></div>' % (m.ops_head([chip('Расходы ✕', True), chip('Счёт ▾'), chip('Ноябрь ▾')]), days)


def accounts_panel():
    body = (big_total('Всего в общем балансе · 12 счетов', '9 640', 'USD') + '<div style="margin-top: 14px;">%s</div>' % segmented(['По типу', 'По валюте', 'Чьи'], 'По типу')
            + ''.join(group_head(t, len(in_group(g)), total) + group([acc_row(a) for a in in_group(g)]) for g, t, total in GROUPS[:2]))
    return tab_screen('Счета', body, 'acc', bar=False)


def panel(html):
    return '<div style="flex: none; width: 390px; height: 844px; position: relative; overflow: hidden;">%s</div>' % html


def bar():
    def tab(key, icon, lbl):
        return tap('<span aria-label="%s" style="position: relative; display: flex; justify-content: center; align-items: center; height: 64px;">%s</span>'
                   % (lbl, svg(icon, '{{tabs.%s.ink}}' % key, 24, 1.7)), 'tabs.%s.go' % key)
    plus = ('<span style="display: flex; justify-content: center;"><span style="width: 52px; height: 52px; border-radius: 50%%; background: %s; display: flex; '
            'align-items: center; justify-content: center;">%s</span></span>' % (LIME, svg('plus', ONLIME, 26, 2.2)))
    return ('<nav aria-label="Навигация" style="position: absolute; left: 16px; right: 16px; bottom: 18px; height: 64px; border-radius: 999px; background: %s; '
            'display: grid; grid-template-columns: repeat(5, minmax(0, 1fr)); align-items: center; padding: 0 6px; box-sizing: border-box;">'
            '<span style="{{pillStyle}}"></span>%s%s%s%s%s</nav>'
            % (BAR, tab('home', 'home', 'Обзор'), tab('ops', 'list', 'Операции'), plus, tab('acc', 'wallet', 'Счета'), tab('more', 'more', 'Ещё')))


def select_bar():
    return ('<div style="position: absolute; left: 16px; right: 16px; bottom: 18px; height: 64px; border-radius: 999px; background: %s; display: flex; align-items: center; gap: 8px; padding: 0 8px; box-sizing: border-box;">'
            '%s%s</div>' % (BAR, tap('<span style="{{dupSelStyle}}">%sДублировать</span>' % svg('copy', INK, 20), 'dupSel'),
                            tap('<span style="flex: 1; height: 48px; border-radius: 999px; background: %s; color: %s; display: flex; align-items: center; justify-content: center; gap: 8px; font-size: 15px; font-weight: 600;">%sУдалить · {{selCount}}</span>'
                                % (PINKBG, PINK, svg('trash', PINK, 20)), 'delSel')))


def edit_sheet():
    head = ('<div style="display: flex; align-items: center; gap: 8px; padding: 0 4px;">%s<span style="flex-grow: 1;"></span>%s%s</div>'
            % (m.acc_chip(m.ACC[0]), tap(icon_button('copy', 'Дублировать', INK, True), 'sheet.dup'), tap(icon_button('trash', 'Удалить', PINK, True), 'sheet.del')))
    inner = ('<div style="position: absolute; left: 0; right: 0; bottom: 0; padding: 0 12px 14px; display: flex; flex-direction: column; gap: 12px;">%s</div>'
             % (head + m.big_amount('{{sheet.amount}}', '{{sheet.cur}}', '{{sheet.amountColor}}') + when('sheet.copy', note('Копия — поменяйте дату или сумму и сохраните', 0))
                + m.cat_grid('Продукты') + m.chips_row(chip('Сегодня ▾'), chip('＋ Заметка')) + tap(m.keypad('{{sheet.saveLabel}}'), 'sheet.save')))
    return tap(m.scrim(), 'closeSheet') + m.sheet(inner, 230)


JS = r'''
    const s = this.state, W = 390, inkOn = '#0E1F00', muted = '#9AA594';
    const selecting = s.sel.length > 0;
    const rows = this.rows();
    const mk = (r) => {
      const selected = s.sel.indexOf(r.id) >= 0;
      return Object.assign({}, r, { selected, plain: !selected, bg: selected ? '#16200F' : 'transparent', down: () => { this.g.row = r.id; } });
    };
    const days = [{ title: 'СЕГОДНЯ', total: '−45 300', cur: 'ARS', color: '#FF5C7F' }, { title: 'ВЧЕРА', total: '+1 200', cur: 'USD', color: '#00F0FF' }]
      .map((d, i) => Object.assign({}, d, { rows: rows.filter((r) => r.day === i).map(mk) })).filter((d) => d.rows.length);
    const pos = Math.max(0, Math.min(3, s.idx - s.dx / W));
    const slot = pos <= 1 ? pos : pos >= 2 ? pos + 1 : 1 + (pos - 1) * 2;
    const ease = s.dragging ? 'none' : '380ms cubic-bezier(.2,.9,.25,1.05)';
    const col = 'calc((100% - 12px) / 5)';
    const tab = (k) => ({ go: () => this.setState({ idx: k }), ink: Math.round(pos) === k ? inkOn : muted });
    const sr = s.sheet && rows.concat(this.rows()).find((r) => r.id === s.sheet.id);
    return {
      days, selecting, notSelecting: !selecting, selCount: String(s.sel.length),
      trackStyle: 'display: flex; width: ' + (W * 4) + 'px; height: 844px; transform: translateX(' + (-pos * W) + 'px); transition: ' + (s.dragging ? 'none' : 'transform ' + ease) + '; touch-action: pan-y;',
      pillStyle: 'position: absolute; top: 10px; height: 44px; left: calc(6px + ' + col + ' * ' + slot + ' + 2px); width: calc(' + col + ' - 4px); border-radius: 999px; background: #B8FF2E; transition: ' + (s.dragging ? 'none' : 'left ' + ease) + ';',
      tabs: { home: tab(0), ops: tab(1), acc: tab(2), more: tab(3) },
      down: (e) => {
        Object.assign(this.g, { x: e.clientX, y: e.clientY, drag: false, fired: false, active: true });
        clearTimeout(this.g.lp);
        if (this.g.row && !selecting && !s.sheet) this.g.lp = setTimeout(() => { this.g.fired = true; this.setState({ sel: [this.g.row] }); }, 450);
      },
      move: (e) => {
        if (!this.g.active) return;
        const dx = e.clientX - this.g.x, dy = e.clientY - this.g.y;
        if (Math.abs(dx) > 10 || Math.abs(dy) > 10) clearTimeout(this.g.lp);
        if (!this.g.drag && Math.abs(dx) > 12 && Math.abs(dx) > Math.abs(dy) && !selecting && !s.sheet) this.g.drag = true;
        if (this.g.drag) this.setState({ dx: (dx > 0 && s.idx === 0) || (dx < 0 && s.idx === 3) ? dx / 3 : dx, dragging: true });
      },
      up: () => {
        if (!this.g.active) return;
        this.g.active = false; clearTimeout(this.g.lp);
        const row = this.g.row; this.g.row = null;
        if (this.g.drag) {
          let idx = s.idx;
          if (s.dx < -80 && idx < 3) idx++;
          if (s.dx > 80 && idx > 0) idx--;
          this.setState({ idx, dx: 0, dragging: false });
          return;
        }
        if (!row || this.g.fired) return;
        if (selecting) this.setState({ sel: s.sel.indexOf(row) >= 0 ? s.sel.filter((x) => x !== row) : s.sel.concat([row]) });
        else if (!s.sheet) this.setState({ sheet: { id: row, copy: false } });
      },
      clearSel: () => this.setState({ sel: [] }),
      dupSel: () => { if (s.sel.length === 1) this.duplicate(s.sel[0]); },
      dupSelStyle: 'flex: 1; display: flex; align-items: center; justify-content: center; gap: 8px; height: 48px; border-radius: 999px; font-size: 15px; opacity: ' + (s.sel.length === 1 ? 1 : 0.38) + ';',
      delSel: () => this.remove(s.sel.slice()),
      snack: { show: !!s.snack, text: s.snack ? s.snack.text : '' },
      undo: () => { clearTimeout(this.g.sn); this.setState({ gone: s.snack ? s.snack.restore : s.gone, snack: null }); },
      closeSheet: () => this.setState({ sheet: null }),
      sheet: {
        show: !!sr, copy: !!(s.sheet && s.sheet.copy), amount: sr ? sr.amount : '', cur: sr ? sr.cur : '', amountColor: sr ? sr.amountColor : '#EEF5E6',
        saveLabel: s.sheet && s.sheet.copy ? 'Сохранить' : 'Готово',
        dup: () => s.sheet && this.duplicate(s.sheet.id),
        del: () => s.sheet && this.remove([s.sheet.id]),
        save: () => this.setState(s.sheet && s.sheet.copy && sr ? { sheet: null, copies: [Object.assign({}, sr, { id: 'c' + s.copies.length, day: 0 })].concat(s.copies) } : { sheet: null })
      }
    };
'''

METHODS = r'''  constructor(props) { super(props); this.state = { idx: 1, dx: 0, dragging: false, gone: [], copies: [], sheet: null, sel: [], snack: null }; this.g = {}; }
  rows() { const s = this.state; return s.copies.concat(%s).filter((r) => s.gone.indexOf(r.id) < 0); }
  duplicate(id) { this.setState({ sel: [], sheet: { id, copy: true } }); }
  remove(ids) {
    const s = this.state; clearTimeout(this.g.sn);
    this.setState({ gone: s.gone.concat(ids), sel: [], sheet: null, snack: { text: ids.length > 1 ? 'Удалено операций: ' + ids.length : 'Операция удалена', restore: s.gone } });
    this.g.sn = setTimeout(() => this.setState({ snack: null }), 5000);
  }
'''


def operations():
    track = '<div style="{{trackStyle}}">%s</div>' % ''.join(panel(p) for p in [
        tab_screen('Обзор', m.balance_hero() + m.tiles(m.tile('Траты за ноябрь', '1 240,50', 'USD'), m.tile('Без категории', '14', 'операций')) + m.recent_card(m.RECENT[:3]), 'home', m.SEARCH_BTN, False),
        ops_panel(), accounts_panel(), tab_screen('Ещё', tabs.more_body(), 'more', bar=False)])
    snack = when('snack.show', tap(snackbar('{{snack.text}}'), 'undo'))
    body = ('<div onPointerDown="{{down}}" onPointerMove="{{move}}" onPointerUp="{{up}}" onPointerCancel="{{up}}" onPointerLeave="{{up}}" '
            'style="position: absolute; inset: 0; overflow: hidden;">%s%s%s%s%s</div>'
            % (track, when('notSelecting', bar()), when('selecting', select_bar()), snack, when('sheet.show', edit_sheet())))
    html = board('Операции', body, '__OPS__')
    return html.replace('  renderVals() { return {__OPS__}; }', METHODS % rows_js() + '  renderVals() {' + JS + '  }')


BOARDS = [('Operations', operations)]
