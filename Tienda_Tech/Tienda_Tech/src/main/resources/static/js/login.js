// /js/login.js
// === Configura aquí ===
const USE_MOCK = false; // true = sin backend; false = usa /api/login

(function () {
  // --- Helpers de sesión (fallback si auth-menu no los define) ---
  function getSessionUser() {
    if (typeof window.getSessionUser === 'function') return window.getSessionUser();
    const raw = sessionStorage.getItem('user') || localStorage.getItem('user');
    try { return raw ? JSON.parse(raw) : null; } catch { return null; }
  }
  function isLoggedIn() {
    if (typeof window.isLoggedIn === 'function') return window.isLoggedIn();
    return !!getSessionUser();
  }
  function setLoggedUser(u, token) {
    if (typeof window.setLoggedUser === 'function') return window.setLoggedUser(u, token);
    sessionStorage.setItem('user', JSON.stringify(u));
    if (token) localStorage.setItem('token', token);
  }

  // --- Normaliza rol del objeto user ---
  function parseRole(u) {
    if (!u) return { id: 0, name: '', isAdmin: false, isWorker: false, isClient: false };
    const id  = parseInt(u.id_rol ?? u.idRol ?? u.rol_id ?? u.role_id ?? u.idrol ?? 0, 10);
    const name = String(u.rol ?? u.role ?? u.roleName ?? u.rolNombre ?? u.nombreRol ?? '').toLowerCase();
    const isAdmin  = name === 'admin'      || id === 1;
    const isClient = name === 'cliente'    || name === 'client' || id === 2;
    const isWorker = name === 'trabajador' || name === 'worker' || id === 3;
    return { id, name, isAdmin, isWorker, isClient };
  }

  // --- Decide adónde ir según rol ---
  function redirectByRole(u) {
    const { isAdmin, isWorker } = parseRole(u);
    let home = '/index.html';
    if (isAdmin)  home = '/admin.html';
    else if (isWorker) home = '/trabajador.html';
    const next = new URLSearchParams(location.search).get('next') || home;
    location.href = next;
  }

  // --- Si YA hay sesión, salta directo a la home por rol ---
  if (isLoggedIn()) {
    redirectByRole(getSessionUser());
    return;
  }

  // --- UI helpers opcionales ---
  function notify(msg, type) {
    if (typeof window.showMessage === 'function') return window.showMessage(msg, type);
    alert(msg);
  }

  document.addEventListener('DOMContentLoaded', () => {
    const form = document.getElementById('loginForm') || document.querySelector('form[data-login]');
    if (!form) { console.warn('login.js: No se encontró el formulario (#loginForm o [data-login]).'); return; }

    // Inputs (con y sin ñ)
    const $u = form.querySelector('#usuario, [name="usuario"]');
    const $p = form.querySelector('#contraseña, #contrasena, [name="contraseña"], [name="contrasena"], input[type="password"]');
    const btn = form.querySelector('.login-button');

    const uiBusy = (on) => { if (!btn) return; btn.disabled = !!on; btn.textContent = on ? 'Validando...' : 'Entrar'; btn.style.background = on ? '#666' : ''; };

    form.addEventListener('submit', async (e) => {
      e.preventDefault();
      const usuario    = ($u?.value || '').trim();
      const contrasena = $p?.value || '';
      if (!usuario || !contrasena) { notify('Por favor completa todos los campos', 'error'); return; }

      // ====== MODO MOCK ======
      if (USE_MOCK) {
        // escribe "admin", "trabajador" o cualquier otro para simular
        let mockId = 2;
        if (usuario.toLowerCase() === 'admin') mockId = 1;
        if (usuario.toLowerCase() === 'trabajador') mockId = 3;
        const mockUser = { usuarioId: 999, usuario, nombre: usuario, id_rol: mockId, rol: mockId===1?'admin':mockId===3?'trabajador':'cliente' };
        setLoggedUser(mockUser, 'mock-token');
        redirectByRole(mockUser);
        return;
      }

      // ====== BACKEND REAL ======
      uiBusy(true);
      try {
        // Acepta "contrasenia", "contrasena" o "contraseña" del lado del server
        const res = await fetch('/api/login', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ usuario, contrasena }) // sin ñ
        });
        let data; try { data = await res.json(); } catch { data = {}; }

        if (!res.ok) {
          notify(data.message || `Error ${res.status} en el servidor`, 'error');
          uiBusy(false); return;
        }
        if (!(data.success && data.user)) {
          notify(data.message || 'Credenciales inválidas', 'error');
          uiBusy(false); return;
        }

        setLoggedUser(data.user, data.token);
        redirectByRole(data.user);
      } catch (err) {
        console.error(err);
        notify('Error de conexión con el servidor', 'error');
      } finally {
        uiBusy(false);
      }
    });
  });
})();
