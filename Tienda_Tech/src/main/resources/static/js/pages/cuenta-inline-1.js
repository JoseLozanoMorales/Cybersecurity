(function () {
      const REDIRECT_TO = '/index.html';
      const menu = document.getElementById('hamburgerMenu');
      if (!menu) return;

      function hasActiveSession(){
        const raw = sessionStorage.getItem('user') || localStorage.getItem('user');
        let ok = false;
        try { ok = !!(raw && JSON.parse(raw)); } catch { ok = !!raw; }
        return ok || !!localStorage.getItem('token') || !!localStorage.getItem('refreshToken');
      }
      function getLogoutLink(){
        return Array.from(menu.querySelectorAll('a.menu-item'))
                .find(a => (a.textContent || '').trim().toLowerCase() === 'cerrar sesión');
      }
      function updateLogoutVisibility(){
        const a = getLogoutLink();
        if (!a) return;
        if (hasActiveSession()){
          a.classList.remove('logout-disabled'); a.removeAttribute('aria-disabled'); a.tabIndex = 0;
        } else {
          a.classList.add('logout-disabled'); a.setAttribute('aria-disabled','true'); a.tabIndex = -1;
        }
      }
      menu.addEventListener('click', async function (ev) {
        const a = ev.target.closest('a.menu-item'); if (!a) return;
        const label = (a.textContent || '').trim().toLowerCase();
        if (label !== 'cerrar sesión') return;
        if (a.getAttribute('aria-disabled') === 'true' || a.classList.contains('logout-disabled')){
          ev.preventDefault(); return;
        }
        ev.preventDefault();
        try { await fetch('/auth/logout', { method: 'POST', credentials: 'include' }); } catch {}
        try { await fetch('/api/auth/logout', { method: 'POST', credentials: 'include' }); } catch {}
        sessionStorage.removeItem('user'); localStorage.removeItem('user');
        localStorage.removeItem('token'); localStorage.removeItem('refreshToken');
        try { document.cookie.split(';').forEach(c=>{
          document.cookie = c.replace(/^ +/,'').replace(/=.*/,'=;expires=' + new Date(0).toUTCString() + ';path=/');
        }); } catch {}
        try { closeMenu(); } catch {}
        window.location.href = REDIRECT_TO;
      });
      document.addEventListener('DOMContentLoaded', updateLogoutVisibility);
      window.addEventListener('storage', updateLogoutVisibility);
      document.addEventListener('visibilitychange', updateLogoutVisibility);
    })();
  
