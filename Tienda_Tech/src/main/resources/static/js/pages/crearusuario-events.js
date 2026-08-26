
document.querySelector('[data-csp-handler-click="csp-crearusuario-1"]')?.addEventListener('click', function(event) {
  const result = (function(event) {
toggleMenu()
}).call(this, event);
  if (result === false) { event.preventDefault(); event.stopPropagation(); }
});

document.querySelector('[data-csp-handler-click="csp-crearusuario-2"]')?.addEventListener('click', function(event) {
  const result = (function(event) {
closeMenu()
}).call(this, event);
  if (result === false) { event.preventDefault(); event.stopPropagation(); }
});

document.querySelector('[data-csp-handler-click="csp-crearusuario-3"]')?.addEventListener('click', function(event) {
  const result = (function(event) {
closeMenu()
}).call(this, event);
  if (result === false) { event.preventDefault(); event.stopPropagation(); }
});

document.querySelector('[data-csp-handler-click="csp-crearusuario-4"]')?.addEventListener('click', function(event) {
  const result = (function(event) {
goBack()
}).call(this, event);
  if (result === false) { event.preventDefault(); event.stopPropagation(); }
});

document.querySelector('[data-csp-handler-click="csp-crearusuario-5"]')?.addEventListener('click', function(event) {
  const result = (function(event) {
changeProfileImage()
}).call(this, event);
  if (result === false) { event.preventDefault(); event.stopPropagation(); }
});
