// /js/login.js
const USE_MOCK = false;

(function () {
  // Lee usuario guardado en sesion/local
  function getSessionUser() {
    const raw = sessionStorage.getItem('user') || localStorage.getItem('user');
    try { return raw ? JSON.parse(raw) : null; } catch { return null; }
  }
  function isLoggedIn() { return !!getSessionUser(); }

  // Guarda usuario y token
  function setLoggedUser(u, token) {
    sessionStorage.setItem('user', JSON.stringify(u));
    if (token) localStorage.setItem('token', token);
  }

  function parseRole(u) {
    if (!u) return { isAdmin: false, isWorker: false };
    const id = parseInt(u.id_rol ?? u.idRol ?? u.rol_id ?? 0, 10);
    const name = String(u.rol ?? u.role ?? '').toLowerCase();
    return {
      isAdmin: name === 'admin' || id === 1,
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
    const mfaForm = document.getElementById('mfaForm');
    const loginSection = document.getElementById('loginForm') || form;
    const mfaSection = document.getElementById('mfaSection');
    const mfaEmailMask = document.getElementById('mfaEmailMask');
    const mfaCodeInput = document.getElementById('mfaCode');

    if (!form) { console.warn('login.js: no hay #loginForm ni [data-login]'); return; }

    const $u = form.querySelector('#usuario, [name="usuario"]');
    const nTilde = String.fromCharCode(241);
    const $p = form.querySelector(`#contrase${nTilde}a, #contrasena, [name="contrase${nTilde}a"], [name="contrasena"], input[type="password"]`);
    const btn = form.querySelector('.login-button');
    const uiBusy = (on) => {
      if (!btn) return;
      btn.disabled = !!on;
      btn.textContent = on ? 'Validando...' : 'Entrar';
    };

    // 1. Envio del formulario de credenciales basicas
    form.addEventListener('submit', async (e) => {
      e.preventDefault();
      console.log('Login form submitted.');
      const usuario = ($u?.value || '').trim(); // Changed back to 'usuario'
      const contrasena = $p?.value || '';
      
      console.log('Usuario:', usuario); // Changed log
      console.log('Contraseña:', contrasena);

      if (!usuario || !contrasena) { // Changed condition
        notify('Completa usuario y contrasena'); // Changed message
        console.log('Usuario or Contraseña is empty.'); // Changed log
        return; 
      }

      // ===== MOCK (para probar UI sin backend) =====
      if (USE_MOCK) {
        const mockId = usuario.toLowerCase() === 'admin' ? 1 : (usuario.toLowerCase() === 'trabajador' ? 3 : 2);
        const mockUser = { usuarioId: 1, usuario, nombre: usuario, id_rol: mockId, rol: mockId === 1 ? 'admin' : mockId === 3 ? 'trabajador' : 'cliente' };
        setLoggedUser(mockUser, 'mock-token');
        redirectByRole(mockUser);
        return;
      }

      // ===== BACKEND REAL =====
      uiBusy(true);
      try {
        console.log('Attempting to fetch /api/login...');
        const res = await fetch('/api/login', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          credentials: 'include',
          body: JSON.stringify({ usuario, contrasena }) // Changed back to 'usuario'
        });

        const data = await res.json().catch(() => ({}));

        if (!res.ok) {
          notify(data.message || `Error ${res.status} en el servidor`);
          console.error('Login failed:', data);
          return;
        }

        // Si el backend indica que requiere MFA
        if (data.mfaRequired) {
          if (loginSection) loginSection.style.display = 'none';
          if (mfaSection) mfaSection.style.display = 'block';
          if (mfaEmailMask) mfaEmailMask.textContent = data.correo || '';

          sessionStorage.setItem('mfaTempData', JSON.stringify({
            txId: data.txId,
            correo: data.correo,
            usuarioId: data.usuarioId
          }));
          console.log('MFA required. Storing temp data:', data);
          return;
        }

        // Si no requiere MFA, se mantiene el flujo directo
        const user = data.user || data.usuario || null;
        const token = data.token || data.access || data.accessToken || null;

        if (!user) { 
          notify(data.message || 'Credenciales invalidas'); 
          console.log('User data not found in response.');
          return; 
        }

        setLoggedUser(user, token);
        window.SessionAuth?.sessionStart?.({ access: token, user });
        redirectByRole(user);
        console.log('Login successful. Redirecting...');
      } catch (err) {
        console.error('Error de conexion con el servidor:', err);
        notify('Error de conexion con el servidor');
      } finally {
        uiBusy(false);
      }
    });

    // 2. Envio del formulario para verificacion de codigo OTP (MFA)
    if (mfaForm) {
      mfaForm.addEventListener('submit', async (e) => {
        e.preventDefault();
        console.log('MFA form submitted.');
        const codigo = (mfaCodeInput?.value || '').trim();
        const tempDataRaw = sessionStorage.getItem('mfaTempData');

        if (!tempDataRaw) {
          notify('Sesion de verificacion expirada. Intenta iniciar sesion de nuevo.');
          location.reload();
          console.log('MFA temp data expired.');
          return;
        }

        const tempData = JSON.parse(tempDataRaw);
        console.log('MFA code:', codigo, 'Temp data:', tempData);

        try {
          const res = await fetch('/api/login/mfa', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
              codigo,
              correo: tempData.correo,
              txId: tempData.txId,
              usuarioId: tempData.usuarioId
            })
          });

          const data = await res.json().catch(() => ({}));

          if (res.ok && data.success) {
            sessionStorage.removeItem('mfaTempData');

            const user = data.user || null;
            const token = data.token || null;

            setLoggedUser(user, token);
            window.SessionAuth?.sessionStart?.({ access: token, user });
            redirectByRole(user);
            console.log('MFA successful. Redirecting...');
          } else {
            notify(data.message || 'Codigo incorrecto o expirado');
            console.error('MFA verification failed:', data);
          }
        } catch (err) {
          console.error('Error al verificar codigo:', err);
          notify('Error al verificar codigo');
        }
      });
    }
  });
})();