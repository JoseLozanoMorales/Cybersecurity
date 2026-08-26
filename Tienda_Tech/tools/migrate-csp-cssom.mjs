import fs from 'node:fs';
import path from 'node:path';

const dir = path.resolve('src/main/resources/static/js/pages');

function findStatementEnd(source, start) {
  let quote = null, escaped = false, round = 0, square = 0, curly = 0;
  for (let i = start; i < source.length; i++) {
    const ch = source[i];
    if (quote) {
      if (escaped) escaped = false;
      else if (ch === '\\') escaped = true;
      else if (ch === quote) quote = null;
      continue;
    }
    if (ch === '"' || ch === "'" || ch === '`') { quote = ch; continue; }
    if (ch === '(') round++; else if (ch === ')') round--;
    else if (ch === '[') square++; else if (ch === ']') square--;
    else if (ch === '{') curly++; else if (ch === '}') curly--;
    else if (ch === ';' && round === 0 && square === 0 && curly === 0) return i;
  }
  return -1;
}

function migrateAssignments(source) {
  const pattern = /((?:document\.getElementById\([^;\n]*?\)|[A-Za-z_$][\w$]*)(?:\??\.[A-Za-z_$][\w$]*|\[[^\]\n]+\])*)\.style\.([A-Za-z_$][\w$]*)\s*=/g;
  const changes = [];
  let match;
  while ((match = pattern.exec(source))) {
    const end = findStatementEnd(source, pattern.lastIndex);
    if (end < 0) continue;
    const object = match[1], property = match[2];
    const rhs = source.slice(pattern.lastIndex, end).trim();
    const call = property === 'cssText'
      ? `ttSetCssText(${object}, ${rhs});`
      : `ttSetStyle(${object}, '${property}', ${rhs});`;
    changes.push({ start: match.index, end: end + 1, call });
    pattern.lastIndex = end + 1;
  }
  for (const change of changes.reverse()) {
    source = source.slice(0, change.start) + change.call + source.slice(change.end);
  }
  return source;
}

for (const name of fs.readdirSync(dir).filter(n => n.endsWith('.js'))) {
  const file = path.join(dir, name);
  let source = fs.readFileSync(file, 'utf8');
  source = source.replace(/Object\.assign\(([^,;]+)\.style\s*,\s*(\{[\s\S]*?\})\s*\);/g,
    (_, element, values) => `ttSetStyles(${element.trim()}, ${values});`);
  source = source.replace(
    /const\s+([A-Za-z_$][\w$]*)\s*=\s*document\.createElement\(['"]style['"]\);\s*\1\.textContent\s*=\s*(`[^`]*`|'[^']*'|"[^"]*");\s*document\.head\.appendChild\(\1\);/g,
    (_, name, css) => `ttAddCss(${css});`);
  source = migrateAssignments(source);
  fs.writeFileSync(file, source, 'utf8');
}
