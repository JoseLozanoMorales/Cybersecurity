
document.querySelector('[data-csp-handler-click="csp-cuenta-1"]')?.addEventListener('click', function(event) {
  const result = (function(event) {
toggleMenu()
}).call(this, event);
  if (result === false) { event.preventDefault(); event.stopPropagation(); }
});

document.querySelector('[data-csp-handler-click="csp-cuenta-2"]')?.addEventListener('click', function(event) {
  const result = (function(event) {
closeMenu()
}).call(this, event);
  if (result === false) { event.preventDefault(); event.stopPropagation(); }
});

document.querySelector('[data-csp-handler-click="csp-cuenta-3"]')?.addEventListener('click', function(event) {
  const result = (function(event) {
closeMenu()
}).call(this, event);
  if (result === false) { event.preventDefault(); event.stopPropagation(); }
});

document.querySelector('[data-csp-handler-click="csp-cuenta-4"]')?.addEventListener('click', function(event) {
  const result = (function(event) {
window.location.href='editar_usuario.html'
}).call(this, event);
  if (result === false) { event.preventDefault(); event.stopPropagation(); }
});
