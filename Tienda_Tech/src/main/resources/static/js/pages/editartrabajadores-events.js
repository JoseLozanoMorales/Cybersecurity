
document.querySelector('[data-csp-handler-click="csp-editartrabajadores-1"]')?.addEventListener('click', function(event) {
  const result = (function(event) {
goBack()
}).call(this, event);
  if (result === false) { event.preventDefault(); event.stopPropagation(); }
});

document.querySelector('[data-csp-handler-click="csp-editartrabajadores-2"]')?.addEventListener('click', function(event) {
  const result = (function(event) {
changeProfileImage()
}).call(this, event);
  if (result === false) { event.preventDefault(); event.stopPropagation(); }
});
