(function(){
      const raw = sessionStorage.getItem('user') || localStorage.getItem('user');
      if (!raw) { location.replace('Login.html?next=' + encodeURIComponent('cuenta.html')); return; }
      let u; try { u = JSON.parse(raw); } catch { u = null; }
      if (!u) { location.replace('Login.html?next=' + encodeURIComponent('cuenta.html')); return; }

      // Si además quieres redirigir a admins a su vista:
      const id = parseInt(u.id_rol ?? u.idRol ?? u.rol_id ?? 0, 10);
      const nm = String(u.rol ?? u.role ?? u.roleName ?? '').toLowerCase();
      //if (id === 1 || nm === 'admin') location.replace('cuenta%20-%20admin.html');
      if (id === 1 || nm === 'admin') location.replace('Login.html');
    })();
  
