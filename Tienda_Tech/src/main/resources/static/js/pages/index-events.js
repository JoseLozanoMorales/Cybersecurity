
document.querySelector('[data-csp-handler-click="csp-index-1"]')?.addEventListener('click', function(event) {
  const result = (function(event) {
toggleMenu()
}).call(this, event);
  if (result === false) { event.preventDefault(); event.stopPropagation(); }
});

document.querySelector('[data-csp-handler-click="csp-index-2"]')?.addEventListener('click', function(event) {
  const result = (function(event) {
closeMenu()
}).call(this, event);
  if (result === false) { event.preventDefault(); event.stopPropagation(); }
});

document.querySelector('[data-csp-handler-click="csp-index-3"]')?.addEventListener('click', function(event) {
  const result = (function(event) {
closeMenu()
}).call(this, event);
  if (result === false) { event.preventDefault(); event.stopPropagation(); }
});

document.querySelector('[data-csp-handler-click="csp-index-4"]')?.addEventListener('click', function(event) {
  const result = (function(event) {
window.location.href='Armado.html'
}).call(this, event);
  if (result === false) { event.preventDefault(); event.stopPropagation(); }
});
