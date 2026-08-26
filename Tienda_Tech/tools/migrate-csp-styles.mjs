import fs from 'node:fs';
import path from 'node:path';
import crypto from 'node:crypto';

const root = path.resolve('src/main/resources/static');
const cssFile = path.join(root, 'css', 'csp-static.css');
const classes = new Map();

function classFor(css) {
  const normalized = css.trim().replace(/;+$/, '');
  const name = `csp-s-${crypto.createHash('sha256').update(normalized).digest('hex').slice(0, 12)}`;
  classes.set(name, normalized);
  return name;
}

function migrate(text) {
  return text.replace(/<([a-z][\w:-]*)([^<>]*?)\sstyle\s*=\s*("([\s\S]*?)"|'([\s\S]*?)')([^<>]*?)>/gi,
    (all, tag, before, quoted, doubleCss, singleCss, after) => {
      const cls = classFor(doubleCss ?? singleCss ?? '');
      let attrs = `${before}${after}`;
      if (/\sclass\s*=\s*"/i.test(attrs)) {
        attrs = attrs.replace(/(\sclass\s*=\s*")([^"]*)"/i, (_, start, value) => `${start}${value} ${cls}"`);
      } else if (/\sclass\s*=\s*'/i.test(attrs)) {
        attrs = attrs.replace(/(\sclass\s*=\s*')([^']*)'/i, (_, start, value) => `${start}${value} ${cls}'`);
      } else {
        attrs += ` class="${cls}"`;
      }
      return `<${tag}${attrs}>`;
    });
}

const targets = [
  ...fs.readdirSync(root).filter(n => n.endsWith('.html')).map(n => path.join(root, n)),
  ...fs.readdirSync(path.join(root, 'js', 'pages')).filter(n => n.endsWith('.js')).map(n => path.join(root, 'js', 'pages', n))
];

for (const file of targets) {
  const original = fs.readFileSync(file, 'utf8');
  let updated = migrate(original);
  if (file.endsWith('.html') && /<head\b/i.test(updated) && !updated.includes('/css/csp-static.css')) {
    updated = updated.replace(/<\/head>/i, '  <link rel="stylesheet" href="/css/csp-static.css">\n</head>');
  }
  if (updated !== original) fs.writeFileSync(file, updated, 'utf8');
}

const css = [...classes.entries()].sort(([a], [b]) => a.localeCompare(b))
  .map(([name, value]) => `.${name}{${value}}`).join('\n') + '\n';
fs.writeFileSync(cssFile, css, 'utf8');
console.log(`Generated ${classes.size} CSP-safe static style classes.`);
