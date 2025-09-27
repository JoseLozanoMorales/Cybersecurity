// /js/role-utils.js
(function (global) {
  function normalize(s) { return (s ?? '').toString().trim().toLowerCase(); }

  // deduce el nombre del rol
  function resolveRoleName(u) {
    // nombres comunes de campos para el rol (cubrimos varios casos del proyecto)
    const byName = normalize(
      u?.rolNombre ?? u?.rol ?? u?.role ?? u?.roleName ?? u?.nombreRol ??
      u?.role_name ?? u?.tipo ?? u?.tipoUsuario ?? u?.tipo_usuario
    );
    if (byName) return byName;

    // por id si viene numérico
    const id = Number(u?.idRol ?? u?.rolId ?? u?.rol_id ?? u?.role_id ?? u?.id_rol ?? u?.idrol ?? u?.roleId);
    if (id === 1) return 'admin';
    if (id === 2) return 'cliente';
    if (id === 3) return 'trabajador';
    return '';
  }

  function resolveUserName(u) {
    return u?.nombre ?? u?.name ?? u?.username ?? u?.correo ?? 'Usuario';
  }

  //recuperar el usuario logueado desde sessionStorage
  function ensureLoggedIn() {
    try {
      const raw = sessionStorage.getItem('user');
      if (!raw) return null;
      return JSON.parse(raw);
    } catch { return null; }
  }

  // Exponer en window (global)
  global.RoleUtils = { resolveRoleName, resolveUserName, ensureLoggedIn };
})(window);
