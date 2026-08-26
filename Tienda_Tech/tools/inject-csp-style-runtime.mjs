import fs from 'node:fs';
import path from 'node:path';

const root = path.resolve('src/main/resources/static');
for (const name of fs.readdirSync(root).filter(n => n.endsWith('.html'))) {
  const file = path.join(root, name);
  let html = fs.readFileSync(file, 'utf8');
  if (!/<head\b/i.test(html) || html.includes('/js/csp-style-runtime.js')) continue;
  html = html.replace(/<\/head>/i,
    '  <link rel="stylesheet" href="/css/csp-runtime.css">\n' +
    '  <script src="/js/csp-style-runtime.js"></script>\n</head>');
  fs.writeFileSync(file, html, 'utf8');
}
