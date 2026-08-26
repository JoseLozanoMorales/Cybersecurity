(function (global) {
  const state = new WeakMap();
  let sequence = 0;

  function sheet() {
    const link = document.querySelector('link[href="/css/csp-runtime.css"]');
    if (!link?.sheet) throw new Error('CSP runtime stylesheet is not available');
    return link.sheet;
  }
  function cssName(property) {
    return property.replace(/[A-Z]/g, value => '-' + value.toLowerCase());
  }
  function safeValue(value) {
    const text = String(value ?? '').trim();
    if (/[{}]/.test(text)) throw new Error('Unsafe CSS value rejected');
    return text;
  }
  function ttSetStyle(element, property, value) {
    if (!element) return;
    const prop = cssName(property);
    if (!/^--[\w-]+$|^[a-z][a-z0-9-]*$/.test(prop)) throw new Error('Invalid CSS property');
    const previous = state.get(element) || new Map();
    const oldClass = previous.get(prop);
    if (oldClass) element.classList.remove(oldClass);
    const normalized = safeValue(value);
    if (!normalized) {
      previous.delete(prop); state.set(element, previous); return;
    }
    const className = `tt-csp-runtime-${++sequence}`;
    sheet().insertRule(`.${className}{${prop}:${normalized}!important}`, sheet().cssRules.length);
    element.classList.add(className);
    previous.set(prop, className);
    state.set(element, previous);
  }
  function ttSetStyles(element, values) {
    Object.entries(values || {}).forEach(([property, value]) => ttSetStyle(element, property, value));
  }
  function ttSetCssText(element, cssText) {
    String(cssText || '').split(';').forEach(declaration => {
      const colon = declaration.indexOf(':');
      if (colon > 0) ttSetStyle(element, declaration.slice(0, colon).trim(), declaration.slice(colon + 1).trim());
    });
  }
  function ttAddCss(cssText) {
    const chunks = String(cssText || '').match(/(?:@keyframes\s+[\w-]+\s*\{(?:[^{}]|\{[^{}]*\})*\}|[^{}]+\{[^{}]*\})/g) || [];
    chunks.forEach(rule => sheet().insertRule(rule, sheet().cssRules.length));
  }
  global.ttSetStyle = ttSetStyle;
  global.ttSetStyles = ttSetStyles;
  global.ttSetCssText = ttSetCssText;
  global.ttAddCss = ttAddCss;
})(window);
