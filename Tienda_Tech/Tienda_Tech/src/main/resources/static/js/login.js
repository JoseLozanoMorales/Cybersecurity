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
    const mfaForm = document.getElementById('mfaForm');
    const loginSection = document.getElementById('loginForm') || form; // fallback
    const mfaSection = document.getElementById('mfaSection');
    const mfaEmailMask = document.getElementById('mfaEmailMask');
    const mfaCodeInput = document.getElementById('mfaCode');

    if (!form) { console.warn('login.js: no hay #loginForm ni [data-login]'); return; }

    const $u = form.querySelector('#usuario, [name="usuario"]');
    const $p = form.querySelector('#contraseña, #contrasena, [name="contraseña"], [name="contrasena"], input[type="password"]');
    const btn = form.querySelector('.login-button');
    const uiBusy = (on) => { if (!btn) return; btn.disabled = !!on; btn.textContent = on ? 'Validando...' : 'Entrar'; };

    // 1. Envío del Formulario de Credenciales Básicas
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

      // ===== BACKEND REAL =====
      uiBusy(true);
      try {
        const res = await fetch('/api/login', {
          method:'POST',
          headers:{'Content-Type':'application/json'},
          credentials:'include',
          body: JSON.stringify({ usuario, contrasena, contrasenia: contrasena, password: contrasena })
        });

        const data = await res.json().catch(()=> ({}));

        if (!res.ok) {
          notify(data.message || `Error ${res.status} en el servidor`);
          return;
        }

        // Si el backend indica que requiere MFA
        if (data.mfaRequired) {
          if (loginSection) loginSection.style.display = 'none';
          if (mfaSection) mfaSection.style.display = 'block';
          if (mfaEmailMask) mfaEmailMask.textContent = data.correo;

          // Guardamos datos temporales para el paso de verificación MFA
          sessionStorage.setItem('mfaTempData', JSON.stringify({
            txId: data.txId,
            correo: data.correo,
            usuarioId: data.usuarioId
          }));
          return;
        }

        // Si no requiere MFA (flujo directo fallback)
        const user  = data.user || data.usuario || null;
        const token = data.token || data.access || data.accessToken || null;

        if (!user) { notify(data.message || 'Credenciales inválidas'); return; }

        setLoggedUser(user, token);
        window.SessionAuth?.sessionStart?.({ access: token, user });
        redirectByRole(user);
      } catch (err) {
        console.error(err);
        notify('Error de conexión con el servidor');
      } finally {
        uiBusy(false);
      }
    });

    // 2. Envío de Formulario para Verificación de Código OTP (MFA)
    if (mfaForm) {
      mfaForm.addEventListener('submit', async (e) => {
        e.preventDefault();
        const codigo = (mfaCodeInput?.value || '').trim();
        const tempDataRaw = sessionStorage.getItem('mfaTempData');
        
        if (!tempDataRaw) {
          notify('Sesión de verificación expirada. Intenta iniciar sesión de nuevo.');
          location.reload();
          return;
        }

        const tempData = JSON.parse(tempDataRaw);

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
          } else {
            notify(data.message || 'Código incorrecto o expirado');
          }
        } catch (err) {
          console.error(err);
          notify('Error al verificar código');
        }
      });
    }
  });
})();
