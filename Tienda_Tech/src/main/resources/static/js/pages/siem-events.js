
document.querySelector('[data-csp-handler-click="csp-siem-1"]')?.addEventListener('click', function(event) {
  const result = (function(event) {
loadSiemEvents()
}).call(this, event);
  if (result === false) { event.preventDefault(); event.stopPropagation(); }
});

document.querySelector('[data-csp-handler-click="csp-siem-2"]')?.addEventListener('click', function(event) {
  const result = (function(event) {
clearSiemEvents()
}).call(this, event);
  if (result === false) { event.preventDefault(); event.stopPropagation(); }
});

document.querySelector('[data-csp-handler-change="csp-siem-3"]')?.addEventListener('change', function(event) {
  const result = (function(event) {
renderSiemEvents()
}).call(this, event);
  if (result === false) { event.preventDefault(); event.stopPropagation(); }
});
