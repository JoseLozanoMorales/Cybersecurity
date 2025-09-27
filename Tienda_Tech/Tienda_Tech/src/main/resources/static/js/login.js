// /js/login.js
const USE_MOCK = false;

(function () {
  // Lee usuario guardado en sesión/local
  function getSessionUser() {
    const raw = sessionStorage.getItem('user') || localStorage.getItem('user');
    try { return raw ? JSON.parse(raw) : null; } catch { return null; }
  }
  function isLoggedIn() { return !!getSessionUser(); }

  // guarda usuario y token
  function setLoggedUser(u, token) {
    sessionStorage.setItem('user', JSON.stringify(u));
    if (token) localStorage.setItem('token', token);
  }

  function parseRole(u) {
    if (!u) return { isAdmin:false, isWorker:false };
    const id  = parseInt(u.id_rol ?? u.idRol ?? u.rol_id ?? 0, 10);
    const name = String(u.rol ?? u.role ?? '').toLowerCase();
    return {
      isAdmin:  name === 'admin' || id === 1,
      isWorker: name === 'trabajador' || name === 'worker' || id === 3
    };
  }
  function redirectByRole(u) {
    const { isAdmin, isWorker } = parseRole(u);
    let home = '/index.html';
    if (isAdmin) home = '/admin.html';
    else if (isWorker) home = '/trabajador.html';
    const next = new URLSearchParams(location.search).get('next') || home;
    location.href = next;
  }
  if (isLoggedIn()) { redirectByRole(getSessionUser()); return; }

  function notify(msg) { alert(msg); }

  document.addEventListener('DOMContentLoaded', () => {
    const form = document.getElementById('loginForm') || document.querySelector('form[data-login]');
    if (!form) { console.warn('login.js: no hay #loginForm ni [data-login]'); return; }

    const $u = form.querySelector('#usuario, [name="usuario"]');
    const $p = form.querySelector('#contraseña, #contrasena, [name="contraseña"], [name="contrasena"], input[type="password"]');
    const btn = form.querySelector('.login-button');
    const uiBusy = (on) => { if (!btn) return; btn.disabled = !!on; btn.textContent = on ? 'Validando...' : 'Entrar'; };

    form.addEventListener('submit', async (e) => {
      e.preventDefault();
      const usuario    = ($u?.value || '').trim();
      const contrasena = $p?.value || '';
      if (!usuario || !contrasena) { notify('Completa usuario y contraseña'); return; }

      // ===== MOCK (para probar UI sin backend) =====
      if (USE_MOCK) {
        const mockId = usuario.toLowerCase()==='admin' ? 1 : (usuario.toLowerCase()==='trabajador' ? 3 : 2);
        const mockUser = { usuarioId: 1, usuario, nombre: usuario, id_rol: mockId, rol: mockId===1?'admin':mockId===3?'trabajador':'cliente' };
        setLoggedUser(mockUser, 'mock-token');
        redirectByRole(mockUser);
        return;
      }
//ay mi corazon
      // ===== BACKEND REAL =====
      uiBusy(true);
      try {
        // Usa UNO de tus endpoints (sé consistente). Aquí /api/login:
        const res = await fetch('/api/login', {
          method:'POST',
          headers:{'Content-Type':'application/json'},
          credentials:'include',
          // Enviamos las 3 variantes por si tu controller espera una en particular:
          body: JSON.stringify({ usuario, contrasena, contrasenia: contrasena, password: contrasena })
        });

        const data = await res.json().catch(()=> ({}));

        if (!res.ok) {
          notify(data.message || `Error ${res.status} en el servidor`);
          return;
        }
        // Ajusta a tu respuesta real:
        // ejemplos válidos: {success:true, user:{...}, token:'...'} o {user:{...}, access:'...'}
        const user  = data.user || data.usuario || null;
        const token = data.token || data.access || data.accessToken || null;

        if (!user) { notify(data.message || 'Credenciales inválidas'); return; }

        setLoggedUser(user, token);
        // si tienes un helper global
        window.SessionAuth?.sessionStart?.({ access: token, user });

        // redirigir según rol
        redirectByRole(user);
      } catch (err) {
        console.error(err);
        notify('Error de conexión con el servidor');
      } finally {
        uiBusy(false);
      }
    });
  });
})();
