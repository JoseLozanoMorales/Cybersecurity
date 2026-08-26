(function () {
      const REDIRECT_TO = 'login.html'; // ajusta si tu login se llama distinto

      const menu = document.getElementById('hamburgerMenu');
      if (!menu) return;

      // ---- Detecta si hay sesión activa ----
      function hasActiveSession(){
        const raw = sessionStorage.getItem('user') || localStorage.getItem('user');
        let ok = false;
        try { ok = !!(raw && JSON.parse(raw)); } catch { ok = !!raw; }
        return ok || !!localStorage.getItem('token') || !!localStorage.getItem('refreshToken');
      }

      // ---- Busca el enlace "Cerrar sesión" y aplica estilo/estado ----
      function getLogoutLink(){
        return Array.from(menu.querySelectorAll('a.menu-item'))
          .find(a => (a.textContent || '').trim().toLowerCase() === 'cerrar sesión');
      }

      function updateLogoutVisibility(){
        const a = getLogoutLink();
        if (!a) return;
        if (hasActiveSession()){
          a.classList.remove('logout-disabled');
          a.removeAttribute('aria-disabled');
          a.tabIndex = 0;
        } else {
          a.classList.add('logout-disabled');
          a.setAttribute('aria-disabled','true');
          a.tabIndex = -1;
        }
      }

      // ---- Delegación de clicks dentro del menú ----
      menu.addEventListener('click', async function (ev) {
        const a = ev.target.closest('a.menu-item');
        if (!a) return;

        const label = (a.textContent || '').trim().toLowerCase();
        if (label !== 'cerrar sesión') return;

        // si está inactivo, no hace nada
        if (a.getAttribute('aria-disabled') === 'true' || a.classList.contains('logout-disabled')){
          ev.preventDefault();
          return;
        }

        ev.preventDefault();

        // (Opcional) notificar al backend
        try { await fetch('/auth/logout', { method: 'POST', credentials: 'include' }); } catch {}
        try { await fetch('/api/auth/logout', { method: 'POST', credentials: 'include' }); } catch {}

        // limpiar sesión local
        sessionStorage.removeItem('user');
        localStorage.removeItem('user');
        localStorage.removeItem('token');
        localStorage.removeItem('refreshToken');

        // limpiar cookies no-HttpOnly (si las hubiera)
        try {
          document.cookie.split(';').forEach(c=>{
            document.cookie = c.replace(/^ +/,'')
              .replace(/=.*/,'=;expires=' + new Date(0).toUTCString() + ';path=/');
          });
        } catch {}

        try { closeMenu(); } catch {}
        window.location.href = REDIRECT_TO;
      });

      // Inicializa y mantén sincronizado si cambia el storage (otra pestaña)
      document.addEventListener('DOMContentLoaded', updateLogoutVisibility);
      window.addEventListener('storage', updateLogoutVisibility);
      // por si el panel se abre/cierra y quieres refrescar
      document.addEventListener('visibilitychange', updateLogoutVisibility);
    })();

