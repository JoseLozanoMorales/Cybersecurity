// auth-menu.js  — común para todas las páginas
(function () {
  // === Ajusta si tu pantalla de login tiene otro nombre/ruta
  const REDIRECT_TO = 'login.html';

  // -------- SESIÓN ----------
  function getSessionUser(){
    const raw = sessionStorage.getItem('user') || localStorage.getItem('user');
    if (!raw) return null;
    try { return JSON.parse(raw); } catch { return null; }
  }
  function hasActiveSession(){
    return !!getSessionUser() || !!localStorage.getItem('token') || !!localStorage.getItem('refreshToken');
  }

  // -------- MENÚ ----------
  const menu = document.getElementById('hamburgerMenu'); // tolera páginas sin menú
  function getLogoutLink(){
    if (!menu) return null;
    return Array.from(menu.querySelectorAll('a.menu-item'))
      .find(a => (a.textContent || '').trim().toLowerCase() === 'cerrar sesión');
  }
  function updateLogoutVisibility(){
    const a = getLogoutLink(); if (!a) return;
    if (hasActiveSession()){
      a.classList.remove('logout-disabled');
      a.removeAttribute('aria-disabled'); a.tabIndex = 0;
    } else {
      a.classList.add('logout-disabled');
      a.setAttribute('aria-disabled','true'); a.tabIndex = -1;
    }
  }

  async function serverLogout(){
    // Soporta varios endpoints típicos; ignora errores si no existen
    const xsrf = (document.cookie.match(/XSRF-TOKEN=([^;]+)/)||[])[1];
    const headers = xsrf ? { 'X-XSRF-TOKEN': decodeURIComponent(xsrf) } : {};
    try { await fetch('/auth/logout', { method:'POST', headers, credentials:'include' }); } catch {}
    try { await fetch('/api/auth/logout', { method:'POST', headers, credentials:'include' }); } catch {}
    try { await fetch('/logout',       { method:'POST', headers, credentials:'include' }); } catch {}
  }

  async function hardLogout(){
    await serverLogout();
    sessionStorage.clear();
    localStorage.clear();
    try { // limpia caches SW si existieran
      if ('caches' in window) { const ks = await caches.keys(); await Promise.all(ks.map(k => caches.delete(k))); }
      if (navigator.serviceWorker) { const regs = await navigator.serviceWorker.getRegistrations(); await Promise.all(regs.map(r => r.unregister())); }
    } catch {}
    try { if (typeof closeMenu === 'function') closeMenu(); } catch {}
    location.href = `${REDIRECT_TO}?_=${Date.now()}`; // rompe caché
  }

  if (menu){
    // delegación: solo actúa en la 5ta opción "Cerrar sesión"
    menu.addEventListener('click', async (ev) => {
      const a = ev.target.closest('a.menu-item'); if (!a) return;
      const label = (a.textContent || '').trim().toLowerCase();
      if (label !== 'cerrar sesión') return;
      if (a.classList.contains('logout-disabled') || a.getAttribute('aria-disabled')==='true') { ev.preventDefault(); return; }
      ev.preventDefault();
      await hardLogout();
    });
  }

  document.addEventListener('DOMContentLoaded', updateLogoutVisibility);
  window.addEventListener('storage', updateLogoutVisibility);
  document.addEventListener('visibilitychange', updateLogoutVisibility);

  // ---------- DEBUG HUD ----------
  (function(){
    const q = new URLSearchParams(location.search);
    let DEBUG = q.has('debug') || localStorage.getItem('auth_debug') === '1';

    document.addEventListener('keydown', (e)=>{
      if(e.ctrlKey && e.altKey && e.key.toLowerCase()==='d'){
        DEBUG = !DEBUG; localStorage.setItem('auth_debug', DEBUG ? '1':'0');
        updateDebugHUD();
      }
    });

    window.__printAuthState = async function(){
      const userRaw = sessionStorage.getItem('user') || localStorage.getItem('user');
      let user = null; try { user = userRaw && JSON.parse(userRaw); } catch { user = userRaw; }
      const swRegs = (navigator.serviceWorker && await navigator.serviceWorker.getRegistrations()) || [];
      const cacheKeys = ('caches' in window) ? await caches.keys() : [];
      const state = {
        url: location.href,
        hasActiveSession: hasActiveSession(),
        user,
        localStorage: {...localStorage},
        sessionStorage: {...sessionStorage},
        cookies: document.cookie,
        serviceWorkers: swRegs.map(r=>r.scope),
        caches: cacheKeys
      };
      console.log('=== AUTH STATE ==='); console.table(state);
      return state;
    };
    window.__forceLogout = (dest=REDIRECT_TO) => hardLogout(dest);

    function ensureBadge(){
      let el = document.getElementById('auth-debug-badge');
      if (el) return el;
      el = document.createElement('div');
      el.id = 'auth-debug-badge';
      Object.assign(el.style, {
        position:'fixed', right:'10px', bottom:'10px', zIndex: 10000,
        padding:'6px 10px', borderRadius:'12px',
        font:'12px/1.2 system-ui, sans-serif',
        boxShadow:'0 2px 8px rgba(0,0,0,.15)', cursor:'pointer', userSelect:'none'
      });
      el.title = 'Click = imprimir estado en consola';
      el.addEventListener('click', ()=>window.__printAuthState());
      document.body.appendChild(el);
      return el;
    }
    function badgeText(){ return hasActiveSession() ? 'Sesión: ACTIVA' : 'Sesión: INACTIVA'; }
    function badgeColor(){ return hasActiveSession() ? '#e6ffed' : '#fff'; }
    function badgeBorder(){ return hasActiveSession() ? '1px solid #2ea043' : '1px solid #ddd'; }

    function updateDebugHUD(){
      if (!DEBUG){ const ex = document.getElementById('auth-debug-badge'); if (ex) ex.remove(); return; }
      const b = ensureBadge();
      b.textContent = badgeText();
      b.style.background = badgeColor();
      b.style.border = badgeBorder();
    }

    ['DOMContentLoaded','visibilitychange','storage'].forEach(ev =>
      document.addEventListener(ev, updateDebugHUD)
    );
    if (document.readyState === 'loading') {
      document.addEventListener('DOMContentLoaded', updateDebugHUD);
    } else { updateDebugHUD(); }
  })();

  // traza de carga para que veas en consola
  console.log('[auth-menu] loaded');
})();
