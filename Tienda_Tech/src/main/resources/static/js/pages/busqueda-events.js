
document.querySelector('[data-csp-handler-click="csp-busqueda-1"]')?.addEventListener('click', function(event) {
  const result = (function(event) {
toggleMenu()
}).call(this, event);
  if (result === false) { event.preventDefault(); event.stopPropagation(); }
});

document.querySelector('[data-csp-handler-click="csp-busqueda-2"]')?.addEventListener('click', function(event) {
  const result = (function(event) {
closeMenu()
}).call(this, event);
  if (result === false) { event.preventDefault(); event.stopPropagation(); }
});

document.querySelector('[data-csp-handler-click="csp-busqueda-3"]')?.addEventListener('click', function(event) {
  const result = (function(event) {
closeMenu()
}).call(this, event);
  if (result === false) { event.preventDefault(); event.stopPropagation(); }
});
