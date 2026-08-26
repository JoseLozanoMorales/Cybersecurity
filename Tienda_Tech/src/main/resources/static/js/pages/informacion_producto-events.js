
document.querySelector('[data-csp-handler-click="csp-informacion_producto-1"]')?.addEventListener('click', function(event) {
  const result = (function(event) {
toggleMenu()
}).call(this, event);
  if (result === false) { event.preventDefault(); event.stopPropagation(); }
});

document.querySelector('[data-csp-handler-click="csp-informacion_producto-2"]')?.addEventListener('click', function(event) {
  const result = (function(event) {
closeMenu()
}).call(this, event);
  if (result === false) { event.preventDefault(); event.stopPropagation(); }
});

document.querySelector('[data-csp-handler-click="csp-informacion_producto-3"]')?.addEventListener('click', function(event) {
  const result = (function(event) {
addReview()
}).call(this, event);
  if (result === false) { event.preventDefault(); event.stopPropagation(); }
});
