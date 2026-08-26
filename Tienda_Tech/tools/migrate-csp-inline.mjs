import fs from 'node:fs';
import path from 'node:path';

const root = path.resolve('src/main/resources/static');
const jsRoot = path.join(root, 'js', 'pages');
const cssRoot = path.join(root, 'css', 'pages');
fs.mkdirSync(jsRoot, { recursive: true });
fs.mkdirSync(cssRoot, { recursive: true });

for (const name of fs.readdirSync(root).filter(n => n.endsWith('.html'))) {
  const file = path.join(root, name);
  const stem = path.basename(name, '.html').toLowerCase();
  let html = fs.readFileSync(file, 'utf8');
  let scriptIndex = 0;
  let styleIndex = 0;
  const handlers = [];

  html = html.replace(/<script\b([^>]*)>([\s\S]*?)<\/script>/gi, (all, attrs, body) => {
    if (/\bsrc\s*=/i.test(attrs) || !body.trim()) return all;
    const outName = `${stem}-inline-${++scriptIndex}.js`;
    fs.writeFileSync(path.join(jsRoot, outName), body.trimStart() + '\n', 'utf8');
    return `<script${attrs} src="/js/pages/${outName}"></script>`;
  });

  html = html.replace(/<style\b([^>]*)>([\s\S]*?)<\/style>/gi, (all, attrs, body) => {
    const outName = `${stem}-inline-${++styleIndex}.css`;
    fs.writeFileSync(path.join(cssRoot, outName), body.trimStart() + '\n', 'utf8');
    return `<link rel="stylesheet" href="/css/pages/${outName}">`;
  });

  html = html.replace(/<([a-z][\w:-]*)([^<>]*?)>/gi, (all, tag, attrs) => {
    let changed = false;
    const updated = attrs.replace(/\s(on[a-z]+)\s*=\s*("([\s\S]*?)"|'([\s\S]*?)')/gi,
      (attribute, eventAttr, quoted, doubleValue, singleValue) => {
        const eventName = eventAttr.slice(2).toLowerCase();
        const code = doubleValue ?? singleValue ?? '';
        const marker = `csp-${stem}-${handlers.length + 1}`;
        handlers.push({ marker, eventName, code });
        changed = true;
        return ` data-csp-handler-${eventName}="${marker}"`;
      });
    return changed ? `<${tag}${updated}>` : all;
  });

  if (handlers.length) {
    const outName = `${stem}-events.js`;
    const source = handlers.map(({ marker, eventName, code }) => `
document.querySelector('[data-csp-handler-${eventName}="${marker}"]')?.addEventListener('${eventName}', function(event) {
  const result = (function(event) {\n${code}\n}).call(this, event);
  if (result === false) { event.preventDefault(); event.stopPropagation(); }
});`).join('\n') + '\n';
    fs.writeFileSync(path.join(jsRoot, outName), source, 'utf8');
    const include = `<script src="/js/pages/${outName}"></script>`;
    html = /<\/body>/i.test(html) ? html.replace(/<\/body>/i, `${include}\n</body>`) : `${html}\n${include}\n`;
  }

  fs.writeFileSync(file, html, 'utf8');
  if (scriptIndex || styleIndex || handlers.length) {
    console.log(`${name}: scripts=${scriptIndex}, styles=${styleIndex}, handlers=${handlers.length}`);
  }
}
