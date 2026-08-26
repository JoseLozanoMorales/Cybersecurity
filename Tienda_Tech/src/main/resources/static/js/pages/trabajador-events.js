
document.querySelector('[data-csp-handler-click="csp-trabajador-1"]')?.addEventListener('click', function(event) {
  const result = (function(event) {
showSection('account', this)
}).call(this, event);
  if (result === false) { event.preventDefault(); event.stopPropagation(); }
});

document.querySelector('[data-csp-handler-click="csp-trabajador-2"]')?.addEventListener('click', function(event) {
  const result = (function(event) {
showSection('products', this)
}).call(this, event);
  if (result === false) { event.preventDefault(); event.stopPropagation(); }
});

document.querySelector('[data-csp-handler-click="csp-trabajador-3"]')?.addEventListener('click', function(event) {
  const result = (function(event) {
showSection('reports', this)
}).call(this, event);
  if (result === false) { event.preventDefault(); event.stopPropagation(); }
});

document.querySelector('[data-csp-handler-click="csp-trabajador-4"]')?.addEventListener('click', function(event) {
  const result = (function(event) {
logout()
}).call(this, event);
  if (result === false) { event.preventDefault(); event.stopPropagation(); }
});
