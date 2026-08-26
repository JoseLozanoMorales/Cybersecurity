
document.querySelector('[data-csp-handler-click="csp-carrito-1"]')?.addEventListener('click', function(event) {
  const result = (function(event) {
toggleMenu()
}).call(this, event);
  if (result === false) { event.preventDefault(); event.stopPropagation(); }
});

document.querySelector('[data-csp-handler-click="csp-carrito-2"]')?.addEventListener('click', function(event) {
  const result = (function(event) {
closeMenu()
}).call(this, event);
  if (result === false) { event.preventDefault(); event.stopPropagation(); }
});

document.querySelector('[data-csp-handler-click="csp-carrito-3"]')?.addEventListener('click', function(event) {
  const result = (function(event) {
closeMenu()
}).call(this, event);
  if (result === false) { event.preventDefault(); event.stopPropagation(); }
});

document.querySelector('[data-csp-handler-click="csp-carrito-4"]')?.addEventListener('click', function(event) {
  const result = (function(event) {
proceedToCheckout()
}).call(this, event);
  if (result === false) { event.preventDefault(); event.stopPropagation(); }
});

document.querySelector('[data-csp-handler-click="csp-carrito-5"]')?.addEventListener('click', function(event) {
  const result = (function(event) {
continueShopping()
}).call(this, event);
  if (result === false) { event.preventDefault(); event.stopPropagation(); }
});
