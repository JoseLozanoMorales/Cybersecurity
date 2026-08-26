
document.querySelector('[data-csp-handler-click="csp-admin-1"]')?.addEventListener('click', function(event) {
  const result = (function(event) {
showSection('account', this)
}).call(this, event);
  if (result === false) { event.preventDefault(); event.stopPropagation(); }
});

document.querySelector('[data-csp-handler-click="csp-admin-2"]')?.addEventListener('click', function(event) {
  const result = (function(event) {
showSection('products', this)
}).call(this, event);
  if (result === false) { event.preventDefault(); event.stopPropagation(); }
});

document.querySelector('[data-csp-handler-click="csp-admin-3"]')?.addEventListener('click', function(event) {
  const result = (function(event) {
showSection('suggestions', this)
}).call(this, event);
  if (result === false) { event.preventDefault(); event.stopPropagation(); }
});

document.querySelector('[data-csp-handler-click="csp-admin-4"]')?.addEventListener('click', function(event) {
  const result = (function(event) {
showSection('users', this)
}).call(this, event);
  if (result === false) { event.preventDefault(); event.stopPropagation(); }
});

document.querySelector('[data-csp-handler-click="csp-admin-5"]')?.addEventListener('click', function(event) {
  const result = (function(event) {
showSection('auditoria', this)
}).call(this, event);
  if (result === false) { event.preventDefault(); event.stopPropagation(); }
});

document.querySelector('[data-csp-handler-click="csp-admin-6"]')?.addEventListener('click', function(event) {
  const result = (function(event) {
showSection('inventory', this)
}).call(this, event);
  if (result === false) { event.preventDefault(); event.stopPropagation(); }
});

document.querySelector('[data-csp-handler-click="csp-admin-7"]')?.addEventListener('click', function(event) {
  const result = (function(event) {
showSection('delivery', this)
}).call(this, event);
  if (result === false) { event.preventDefault(); event.stopPropagation(); }
});

document.querySelector('[data-csp-handler-click="csp-admin-8"]')?.addEventListener('click', function(event) {
  const result = (function(event) {
showSection('reports', this)
}).call(this, event);
  if (result === false) { event.preventDefault(); event.stopPropagation(); }
});

document.querySelector('[data-csp-handler-click="csp-admin-9"]')?.addEventListener('click', function(event) {
  const result = (function(event) {
showSection('settings', this)
}).call(this, event);
  if (result === false) { event.preventDefault(); event.stopPropagation(); }
});

document.querySelector('[data-csp-handler-click="csp-admin-10"]')?.addEventListener('click', function(event) {
  const result = (function(event) {
showSection('siem-audit', this)
}).call(this, event);
  if (result === false) { event.preventDefault(); event.stopPropagation(); }
});

document.querySelector('[data-csp-handler-click="csp-admin-11"]')?.addEventListener('click', function(event) {
  const result = (function(event) {
logout()
}).call(this, event);
  if (result === false) { event.preventDefault(); event.stopPropagation(); }
});

document.querySelector('[data-csp-handler-click="csp-admin-12"]')?.addEventListener('click', function(event) {
  const result = (function(event) {
loadSiemEvents()
}).call(this, event);
  if (result === false) { event.preventDefault(); event.stopPropagation(); }
});

document.querySelector('[data-csp-handler-click="csp-admin-13"]')?.addEventListener('click', function(event) {
  const result = (function(event) {
clearSiemEvents()
}).call(this, event);
  if (result === false) { event.preventDefault(); event.stopPropagation(); }
});

document.querySelector('[data-csp-handler-change="csp-admin-14"]')?.addEventListener('change', function(event) {
  const result = (function(event) {
renderSiemEvents()
}).call(this, event);
  if (result === false) { event.preventDefault(); event.stopPropagation(); }
});

document.querySelector('[data-csp-handler-click="csp-admin-15"]')?.addEventListener('click', function(event) {
  const result = (function(event) {
uaActualizar()
}).call(this, event);
  if (result === false) { event.preventDefault(); event.stopPropagation(); }
});

document.querySelector('[data-csp-handler-click="csp-admin-16"]')?.addEventListener('click', function(event) {
  const result = (function(event) {
uaEliminar()
}).call(this, event);
  if (result === false) { event.preventDefault(); event.stopPropagation(); }
});

document.querySelector('[data-csp-handler-click="csp-admin-17"]')?.addEventListener('click', function(event) {
  const result = (function(event) {
crearUsuario()
}).call(this, event);
  if (result === false) { event.preventDefault(); event.stopPropagation(); }
});

document.querySelector('[data-csp-handler-click="csp-admin-18"]')?.addEventListener('click', function(event) {
  const result = (function(event) {
agregarProvincia()
}).call(this, event);
  if (result === false) { event.preventDefault(); event.stopPropagation(); }
});

document.querySelector('[data-csp-handler-change="csp-admin-19"]')?.addEventListener('change', function(event) {
  const result = (function(event) {
manejarSeleccionProvincia()
}).call(this, event);
  if (result === false) { event.preventDefault(); event.stopPropagation(); }
});

document.querySelector('[data-csp-handler-click="csp-admin-20"]')?.addEventListener('click', function(event) {
  const result = (function(event) {
editarProvincia()
}).call(this, event);
  if (result === false) { event.preventDefault(); event.stopPropagation(); }
});

document.querySelector('[data-csp-handler-click="csp-admin-21"]')?.addEventListener('click', function(event) {
  const result = (function(event) {
eliminarProvincia()
}).call(this, event);
  if (result === false) { event.preventDefault(); event.stopPropagation(); }
});

document.querySelector('[data-csp-handler-click="csp-admin-22"]')?.addEventListener('click', function(event) {
  const result = (function(event) {
agregarCiudad()
}).call(this, event);
  if (result === false) { event.preventDefault(); event.stopPropagation(); }
});

document.querySelector('[data-csp-handler-change="csp-admin-23"]')?.addEventListener('change', function(event) {
  const result = (function(event) {
manejarSeleccionCiudad()
}).call(this, event);
  if (result === false) { event.preventDefault(); event.stopPropagation(); }
});

document.querySelector('[data-csp-handler-click="csp-admin-24"]')?.addEventListener('click', function(event) {
  const result = (function(event) {
editarCiudad()
}).call(this, event);
  if (result === false) { event.preventDefault(); event.stopPropagation(); }
});

document.querySelector('[data-csp-handler-click="csp-admin-25"]')?.addEventListener('click', function(event) {
  const result = (function(event) {
eliminarCiudad()
}).call(this, event);
  if (result === false) { event.preventDefault(); event.stopPropagation(); }
});
