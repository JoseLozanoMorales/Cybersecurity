// auth-menu.js  — común para todas las páginas
(function () {
  const origFetch = window.fetch;

  function readUserRaw(){
    return sessionStorage.getItem('user') || localStorage.getItem('user') || null;
  }
  function getLoggedUser(){
    try { return JSON.parse(readUserRaw() || 'null'); } catch { return null; }
  }
  function getLoggedUsername(){
    const u = getLoggedUser();
    const s = (u?.usuario || u?.username || u?.user || u?.correo || '').trim();
    return s || null;
  }
  function getLoggedUserId(){
    const u = getLoggedUser();
    return u?.usuarioId ?? u?.id ?? u?.userId ?? u?.id_usuario ?? u?.usuario_id ?? null;
  }

  // expón helpers por si los quieres usar en otros scripts
  window.getLoggedUsername = getLoggedUsername;
  window.getLoggedUserId   = getLoggedUserId;

  // parche de fetch
  window.fetch = function(input, init = {}) {
    // Normaliza a un Request para conservar método, body, etc.
    const req1 = (input instanceof Request) ? input : new Request(input, init);

    // Clona headers y agrega nuestros custom si no existen
    const h = new Headers(req1.headers || {});
    const uname = getLoggedUsername();
    const uid   = getLoggedUserId();

    if (uname && !h.has('X-Usuario'))  h.set('X-Usuario', uname);
    if (uid   != null && !h.has('X-User-Id')) h.set('X-User-Id', uid); // opcional, ya lo usas en pagos

    // Crea un nuevo Request con los headers finales (preserva body, método, credenciales, etc.)
    const req2 = new Request(req1, { headers: h });
    return origFetch(req2);
  };
})();


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





// === Idle/refresh por rol ===
const KEEPALIVE_ENABLED = false; // <- apaga/enciende el keepalive y el refresh inicial

let ACCESS = null;
let ROLE = null;
let LAST_INTERACTION = Date.now();
let keepaliveTimer = null;

const KA_INTERVAL_MS = 70_000;
const KA_TOUCH_WINDOW_MS = 30_000;
const ROLES_CON_IDLE = new Set(['ADMIN','TRABAJADOR']);

['mousemove','keydown','click','scroll','touchstart'].forEach(ev =>
  window.addEventListener(ev, ()=> { LAST_INTERACTION = Date.now(); }, {passive:true})
);
// === Idle watcher (client-side, sin backend) ================================

// Normaliza el rol que llega dentro del objeto user (string, número, etc.)
function __normalizeRole(r){
  if (r == null) return '';
  if (typeof r === 'string') return r.trim().toUpperCase();
  if (typeof r === 'number') {
    // Si tus IDs son distintos, ajusta este mapa:
    const map = {1:'ADMIN', 2:'TRABAJADOR', 3:'CLIENTE'};
    return (map[r] || '').toUpperCase();
  }
  return String(r).toUpperCase();
}

// IMPORTANT: si tu SessionAuth.sessionStart hace ROLE = user?.rol,
// asegúrate de guardarlo normalizado:
(function hardenRoleSetter(){
  const _sessionStart = window.SessionAuth?.sessionStart;
  if (_sessionStart && !_sessionStart.__wrapped){
    window.SessionAuth.sessionStart = function({ access, user }){
      if (user) user.rol = user.rol ?? user.role ?? user.rol_nombre ?? user.nombre_rol;
      if (user) user.rol = __normalizeRole(user.rol);
      return _sessionStart({ access, user });
    };
    window.SessionAuth.sessionStart.__wrapped = true;
  }
})();

// Roles que deben tener inactividad controlada

// Configura tiempos (en minutos) por rol:
const IDLE_LIMIT_MIN_BY_ROLE = {
  ADMIN: 1,        // cierra sesión a los 15 min sin actividad
  TRABAJADOR: 1
};
// Segundos antes del cierre en los que mostramos el aviso:
const WARNING_SECONDS = 60;

// Timers y estado
let __idleWarnTimer = null, __idleLogoutTimer = null;

// Timestamp compartido entre pestañas:
const LAST_TS_KEY = 'tt_lastActivityTs';
// Señal para forzar logout en todas las pestañas:
const FORCE_LOGOUT_KEY = 'tt_forceLogout';

// Lee el rol actual desde sessionStorage (lo completa SessionAuth.sessionStart)
function __getCurrentRole(){
  try {
    const u = JSON.parse(sessionStorage.getItem('user') || 'null');
    return __normalizeRole(u?.rol || u?.role || u?.rol_nombre);
  } catch { return ''; }
}

function __idleLimitMs(){
  const role = __getCurrentRole();
  const mins = IDLE_LIMIT_MIN_BY_ROLE[role] ?? 0;
  return mins * 60_000;
}

function __clearIdleTimers(){
  if (__idleWarnTimer){ clearTimeout(__idleWarnTimer); __idleWarnTimer = null; }
  if (__idleLogoutTimer){ clearTimeout(__idleLogoutTimer); __idleLogoutTimer = null; }
}

// Re-programa el aviso y el cierre según el último timestamp visto
function __scheduleIdleTimers(){
  __clearIdleTimers();
  const limit = __idleLimitMs();
  if (!limit) return; // rol no controlado

  const now = Date.now();
  const last = Number(localStorage.getItem(LAST_TS_KEY) || now);
  const elapsed = Math.max(0, now - last);
  const remainingMs = Math.max(0, limit - elapsed);

  // Aviso (WARNING_SECONDS antes del cierre)
  const warnAt = Math.max(0, remainingMs - WARNING_SECONDS*1000);
  __idleWarnTimer = setTimeout(() => {
    const now2 = Date.now();
    const last2 = Number(localStorage.getItem(LAST_TS_KEY) || now2);
    const remain = Math.ceil((limit - (now2 - last2))/1000);
    if (remain > 0) {
      window.hideIdleModal?.();
      window.showIdleModal?.(Math.max(remain,1));
    }
  }, warnAt);

  // Cierre definitivo
  __idleLogoutTimer = setTimeout(() => {
    // emite señal para cerrar en todas las pestañas
    localStorage.setItem(FORCE_LOGOUT_KEY, '1');
    setTimeout(() => localStorage.removeItem(FORCE_LOGOUT_KEY), 0);
    window.SessionAuth?.sessionLogout?.();
  }, remainingMs);
}

// Marca actividad (se invoca en eventos de usuario y al pulsar “Seguir aquí”)
function __pingActivity(){
  try {
    const ts = Date.now();
    localStorage.setItem(LAST_TS_KEY, String(ts));
    window.hideIdleModal?.();
    __scheduleIdleTimers();
  } catch {}
}

// Enganche de eventos de actividad (una vez, global)
(function wireActivityEventsOnce(){
  if (window.__idleEventsWired) return; window.__idleEventsWired = true;

  ['mousemove','keydown','click','scroll','touchstart'].forEach(ev =>
    window.addEventListener(ev, __pingActivity, {passive:true})
  );
  document.addEventListener('visibilitychange', () => {
    if (!document.hidden) __pingActivity();
  });
  window.addEventListener('storage', (ev) => {
    if (ev.key === LAST_TS_KEY) __scheduleIdleTimers();
    if (ev.key === FORCE_LOGOUT_KEY && ev.newValue === '1'){
      window.SessionAuth?.sessionLogout?.();
    }
  });

  // Si tu modal ya existe y tiene “Seguir aquí”, asegúrate de que dispare ping
  // (además de cualquier fetch/keepalive que ya tengas)
  const _ensure = window.ensureIdleModal;
  if (typeof _ensure === 'function' && !_ensure.__wrapped){
    window.ensureIdleModal = function(){
      const el = _ensure();
      const btnSeguir = el?.querySelector?.('.btn-seguir');
      if (btnSeguir && !btnSeguir.__wired){
        const old = btnSeguir.onclick;
        btnSeguir.onclick = async () => {
          try {
            // Si luego activas /auth/keepalive, se ejecutará aquí; si falla, igual reseteamos
            await (old?.() || Promise.resolve());
          } catch {}
          __pingActivity();
        };
        btnSeguir.__wired = true;
      }
      return el;
    };
    window.ensureIdleModal.__wrapped = true;
  }
})();

// Arranque del watcher al inicializar la UI de sesión
(function hookInitAuthUI(){
  const orig = window.SessionAuth?.initAuthUI;
  if (!orig || orig.__wrapped) return;
  window.SessionAuth.initAuthUI = async function(){
    await orig(); // conserva tu refresh/keepalive si lo activas más adelante
    const role = __getCurrentRole();
    if (ROLES_CON_IDLE.has(role)){
      if (!localStorage.getItem(LAST_TS_KEY)){
        localStorage.setItem(LAST_TS_KEY, String(Date.now()));
      }
      __scheduleIdleTimers();
    }
  };
  window.SessionAuth.initAuthUI.__wrapped = true;
})();

// Utilidades para probar desde la consola
window.__idleTest = {
  ping: __pingActivity,
  warn: ()=>window.showIdleModal?.(WARNING_SECONDS),
  forceLogoutAll: ()=>{
    localStorage.setItem(FORCE_LOGOUT_KEY,'1');
    setTimeout(()=>localStorage.removeItem(FORCE_LOGOUT_KEY),0);
  }
};

//
const _fetch = window.fetch.bind(window);
window.fetch = async (url, opts = {}) => {
  opts.headers = opts.headers || {};
  if (ACCESS) opts.headers['Authorization'] = `Bearer ${ACCESS}`;
  opts.credentials = 'include';       // importante para cookie refresh
  return _fetch(url, opts);
};
//Session
function sessionStart({ access, user }) {
  ACCESS = access;
  ROLE = user?.rol || user?.role;
  sessionStorage.setItem('user', JSON.stringify(user));
  if (KEEPALIVE_ENABLED && ROLES_CON_IDLE.has(ROLE)) startKeepalive();
}

async function sessionLogout(){
  try { await fetch('/auth/logout', {method:'POST'}); } catch {}
  ACCESS = null; ROLE = null;
  sessionStorage.removeItem('user');
  stopKeepalive(); hideIdleModal?.();
  location.href = '/login.html';
}

async function initAuthUI(){
  const u = JSON.parse(sessionStorage.getItem('user') || 'null');
  if (KEEPALIVE_ENABLED && u && ROLES_CON_IDLE.has(u.rol || u.role)) {
    startKeepalive();
    try {
      const r = await fetch('/auth/refresh', { method:'POST' });
      if (r.ok) {
        const d = await r.json();
        ACCESS = d.access;
        maybeWarnIdle(d.meta?.remainingIdleSeconds);
      }
    } catch {}
  }
}

//
function startKeepalive(){   if (!KEEPALIVE_ENABLED) return;
                             stopKeepalive();
                             keepaliveTimer = setInterval(doKeepalive, KA_INTERVAL_MS); }
function stopKeepalive(){ if (keepaliveTimer) { clearInterval(keepaliveTimer); keepaliveTimer = null; } }

async function doKeepalive(){
  if (!KEEPALIVE_ENABLED || !ROLES_CON_IDLE.has(ROLE)) return;
  if (Date.now() - LAST_INTERACTION > KA_TOUCH_WINDOW_MS) return;
  try {
    const r = await fetch('/auth/keepalive', {method:'POST'});
    if (!r.ok) throw 0;
    const d = await r.json();
    ACCESS = d.access;
    maybeWarnIdle(d.meta?.remainingIdleSeconds);
  } catch {}
}
function maybeWarnIdle(remaining){ if (typeof remaining === 'number' && remaining <= 120) showIdleModal(Math.max(remaining,1)); }

let idleModalShown=false, idleCountdownTimer=null;
function ensureIdleModal(){
  let m=document.getElementById('idle-modal'); if (m) return m;
  document.body.insertAdjacentHTML('beforeend', `
  <div id="idle-modal" style="position:fixed;inset:0;display:none;align-items:center;justify-content:center;background:rgba(0,0,0,.55);z-index:9999">
    <div style="background:#111;color:#fff;padding:22px 20px;border-radius:12px;max-width:420px;width:92%;text-align:center;box-shadow:0 10px 30px rgba(0,0,0,.5)">
      <h3 style="margin:0 0 10px">Sesión por inactividad</h3>
      <p>Tu sesión se cerrará en <b class="idle-secs">120</b> segundos.</p>
      <div style="display:flex;gap:12px;justify-content:center;margin-top:12px">
        <button class="btn-seguir">Seguir aquí</button>
        <button class="btn-salir">Cerrar sesión</button>
      </div>
    </div>
  </div>`);
  return document.getElementById('idle-modal');
}
function showIdleModal(seconds){
  if (idleModalShown) return; idleModalShown = true;
  const el=ensureIdleModal(); let remaining=seconds;
  el.querySelector('.idle-secs').textContent=remaining; el.style.display='flex';
  idleCountdownTimer=setInterval(()=>{ remaining--; el.querySelector('.idle-secs').textContent=remaining; if (remaining<=0){clearInterval(idleCountdownTimer); sessionLogout();}},1000);
  el.querySelector('.btn-seguir').onclick = async ()=>{ try{const r=await fetch('/auth/keepalive',{method:'POST'}); if(r.ok){const d=await r.json(); ACCESS=d.access;}}catch{} hideIdleModal();};
  el.querySelector('.btn-salir').onclick = sessionLogout;
}
function hideIdleModal(){ idleModalShown=false; if (idleCountdownTimer) clearInterval(idleCountdownTimer); const m=document.getElementById('idle-modal'); if (m) m.style.display='none'; }
window.SessionAuth = { sessionStart, sessionLogout, initAuthUI };
