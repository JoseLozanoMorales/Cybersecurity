(() => {
  const USER_KEY  = 'user';
  const TOKEN_KEY = 'token';

  function readRaw() {
    const raw = sessionStorage.getItem(USER_KEY) || localStorage.getItem(USER_KEY);
    if (!raw) return null;
    try { return JSON.parse(raw); } catch { return null; }
  }

  function getName(u = readRaw()) {
    return u?.usuario || u?.username || u?.user || null;
  }

  function getToken() {
    return sessionStorage.getItem(TOKEN_KEY) || localStorage.getItem(TOKEN_KEY) || null;
  }

  function authHeaders(extra = {}) {
    const h = new Headers(extra.headers || {});
    const t = getToken();
    if (t && !h.has('Authorization')) h.set('Authorization', `Bearer ${t}`);
    if (!h.has('Content-Type')) h.set('Content-Type', 'application/json');
    return { ...extra, headers: h };
  }

  function addXUsuario(init = {}, headerName = 'X-Usuario') {
    const h = new Headers(init.headers || {});
    const name = getName();
    if (name && !h.has(headerName)) h.set(headerName, name);
    return { ...init, headers: h };
  }

  function withAuth(init = {}) {
    return authHeaders(init);
  }

  function set(userObj, token, { persist = 'session' } = {}) {
    const S = persist === 'local' ? localStorage : sessionStorage;
    S.setItem(USER_KEY, JSON.stringify(userObj || {}));
    if (token) S.setItem(TOKEN_KEY, token);
    dispatchEvent(new CustomEvent('tt:user-changed', { detail: { user: userObj } }));
  }

  function clear({ redirectTo } = {}) {
    try {
      sessionStorage.removeItem(USER_KEY); sessionStorage.removeItem(TOKEN_KEY);
      localStorage.removeItem(USER_KEY);  localStorage.removeItem(TOKEN_KEY);
    } finally {
      dispatchEvent(new Event('tt:user-changed'));
      if (redirectTo) location.href = redirectTo;
    }
  }

  function requireRole(roles) {
    // roles: string o array de strings p.ej. 'admin' | ['admin','trabajador']
    const want = Array.isArray(roles) ? roles.map(r=>String(r).toLowerCase()) : [String(roles).toLowerCase()];
    const u = readRaw();
    const have =
      (u?.rol_nombre ?? u?.rolNombre ?? u?.rol ?? u?.role ?? u?.role_name ?? '').toString().toLowerCase();
    if (!want.includes(have)) {
      throw Object.assign(new Error('Acceso denegado'), { status: 403, have, want });
    }
    return true;
  }

  // Namespace público
  const User = {
    read:      readRaw,
    getName,
    getToken,
    withAuth,          // añade Authorization + Content-Type
    authHeaders,       // alias explícito
    addXUsuario,       // añade X-Usuario con el nombre actual
    set,               // guardar usuario/token tras login
    clear,             // logout
    requireRole        // guard simple por rol
  };

  // Compatibilidad con código existente
  window.User = User;
  window.currentUsername   = () => User.getName();
  window.getLoggedUsername = window.getLoggedUsername || (() => User.getName());
  window.authHeaders       = window.authHeaders || User.authHeaders;
})();
