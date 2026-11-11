#!/usr/bin/env node
// Local preview of design-canvas boards (docs/design/mockups/*.dc.html) — a minimal stand-in for the canvas
// runtime: {{holes}}, <sc-if>, <sc-for>, the board's Component class. No events, no animation.
// Usage: node scripts/agent/render-mockups.js <out-dir> <theme: dark|light> <palette> <board.dc.html>...
// Example: node scripts/agent/render-mockups.js /tmp/shots light Манго docs/design/mockups/More.dc.html
const fs = require('fs');
const path = require('path');
const { chromium } = require('playwright');

const [outDir, theme, palette, ...files] = process.argv.slice(2);

function page(html) {
  const m = /<script type="text\/x-dc" data-dc-script data-props='(.*?)'>([\s\S]*?)<\/script>/.exec(html);
  const body = /<x-dc>([\s\S]*)<\/x-dc>/.exec(html)[1];
  const helmet = (/<helmet>([\s\S]*?)<\/helmet>/.exec(body) || ['', ''])[1];
  const tpl = body.replace(/<helmet>[\s\S]*?<\/helmet>/, '');
  const props = JSON.parse(m[1].replace(/&#39;/g, "'").replace(/&amp;/g, '&'));
  const size = props.$preview || { width: 390, height: 844 };
  return { size, doc: `<!doctype html><html><head><meta charset="utf-8">${helmet}</head><body>
<template id="tpl">${tpl}</template><div id="root"></div>
<script>
class DCLogic { constructor(p) { this.props = p; this.state = {}; } setState(s) { Object.assign(this.state, s); } forceUpdate() {} }
const React = { createRef: () => ({ current: null }) };
${m[2]}
const comp = new Component(${JSON.stringify({ theme: theme === 'light' ? 'светлая' : 'тёмная', palette })});
if (comp.componentDidMount) try { comp.componentDidMount(); } catch (e) {}
const vals = comp.renderVals();
function look(scope, p) { p = p.trim(); if (p === 'true') return true; if (p === 'false') return false; if (/^-?\\d+(\\.\\d+)?$/.test(p)) return +p;
  return p.split('.').reduce((o, k) => (o == null ? undefined : o[k]), scope); }
function interp(s, scope) {
  const whole = /^\\s*\\{\\{([^}]+)\\}\\}\\s*$/.exec(s); if (whole) return look(scope, whole[1]);
  return s.replace(/\\{\\{([^}]+)\\}\\}/g, (_, p) => { const v = look(scope, p); return v == null ? '' : String(v); }); }
function render(node, scope, out) {
  for (const n of [...node.childNodes]) {
    if (n.nodeType === 3) { out.appendChild(document.createTextNode(interp(n.textContent, scope))); continue; }
    if (n.nodeType !== 1) continue;
    const tag = n.tagName.toLowerCase();
    if (tag === 'sc-if') { if (interp(n.getAttribute('value'), scope)) render(n, scope, out); continue; }
    if (tag === 'sc-for') { const list = interp(n.getAttribute('list'), scope) || []; const as = n.getAttribute('as');
      list.forEach((it, i) => render(n, Object.assign(Object.create(scope), { [as]: it, $index: i }), out)); continue; }
    const svg = tag === 'svg' || out.namespaceURI === 'http://www.w3.org/2000/svg';
    const el = svg ? document.createElementNS('http://www.w3.org/2000/svg', tag) : document.createElement(tag === 'dc-import' ? 'div' : n.tagName);
    for (const a of n.attributes) { if (/^on[A-Z]/.test(a.name) || /^on[a-z]+$/.test(a.name)) continue;
      const v = interp(a.value, scope); if (v === false || v == null) continue; try { el.setAttribute(a.name, v === true ? '' : v); } catch (e) {} }
    if (n.tagName === 'INPUT' && n.getAttribute('value')) el.value = interp(n.getAttribute('value'), scope);
    render(tag === 'template' ? n.content : n, scope, el); out.appendChild(el);
  }
}
render(document.getElementById('tpl').content, vals, document.getElementById('root'));
</script></body></html>` };
}

(async () => {
  fs.mkdirSync(outDir, { recursive: true });
  const browser = await chromium.launch();
  for (const f of files) {
    const { size, doc } = page(fs.readFileSync(f, 'utf8'));
    const p = await browser.newPage({ viewport: { width: size.width, height: size.height }, deviceScaleFactor: 1 });
    const errors = [];
    p.on('pageerror', (e) => errors.push(e.message));
    await p.setContent(doc, { waitUntil: 'load' });
    await p.waitForTimeout(150);
    const out = path.join(outDir, path.basename(f).replace('.dc.html', `.${theme}.${palette}.png`));
    await p.screenshot({ path: out });
    console.log(errors.length ? 'ERR ' + errors[0] : 'ok', out);
    await p.close();
  }
  await browser.close();
})();
