// Logic check of design-canvas boards (agent.sh mockups): every {{hole}} has a value, every tap (go_/tg_) runs and
// switches its state. Usage: node scripts/agent/check-mockups.js docs/design/mockups/*.dc.html
const fs = require('fs');
global.window = { localStorage: { getItem: () => null }, addEventListener() {}, removeEventListener() {} };
let bad = 0;
for (const f of process.argv.slice(2)) {
  const html = fs.readFileSync(f, 'utf8');
  const js = /data-dc-script data-props='.*?'>([\s\S]*?)<\/script>/.exec(html)[1];
  const C = new Function('DCLogic', js + '\nreturn Component;')(class { constructor(p) { this.props = p || {}; this.state = this.state || {}; } setState(s) { Object.assign(this.state, typeof s === 'function' ? s(this.state) : s); } forceUpdate() {} });
  const c = new C({}); if (!c.state) c.state = {};
  let v = c.renderVals();
  const gos = Object.keys(v).filter((k) => /^(go|tg)_/.test(k));
  const used = new Set((html.match(/\{\{\s*([\w.]+)\s*\}\}/g) || []).map((h) => h.replace(/[{}\s]/g, '')));
  const missing = [...used].filter((h) => !h.includes('.') && !(h in v) && !/^(co|true|false)$/.test(h));
  let fails = [];
  for (const g of gos) {
    try {
      v[g]();
      v = c.renderVals();
      if (g.startsWith('go_')) { const is = 'is_' + g.slice(3); if (is in v && v[is] !== true) fails.push(g); }
    } catch (e) { fails.push(g + ':' + e.message); }
  }
  const name = f.split('/').pop();
  if (gos.length || missing.length) console.log((fails.length || missing.length ? 'FAIL ' : 'ok   ') + name, gos.length + ' taps', fails.join(' '), missing.length ? 'missing ' + missing.join(',') : '');
  if (fails.length || missing.length) bad++;
}
process.exit(bad ? 1 : 0);
