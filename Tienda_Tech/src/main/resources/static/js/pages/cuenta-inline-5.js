(function () {
      const DEFAULT = '/assets/avatars/defaults/user.png';
      const img = document.getElementById('avatar');
      if (!img) return;

      // fallback si la URL falla
      img.onerror = () => { img.onerror = null; img.src = DEFAULT; };

      // leer usuario de session/local storage
      function readUser() {
        const raw = sessionStorage.getItem('user') || localStorage.getItem('user');
        if (!raw) return null;
        try { return JSON.parse(raw); } catch { return null; }
      }
      function getId(u){
        return u?.usuarioId ?? u?.id ?? u?.userId ?? u?.id_usuario ?? u?.usuario_id ?? null;
      }
      function getAvatar(u){
        return (u?.avatar_path || u?.avatarPath || u?.avatarUrl || '').trim() || null;
      }
      async function fetchMe(){
        try {
          const r = await fetch('/api/usuarios/me');
          if (!r.ok) return null;
          return await r.json();
        } catch { return null; }
      }
      async function fetchById(id){
        if (!id) return null;
        try {
          const r = await fetch(`/api/usuarios/${id}`);
          if (!r.ok) return null;
          return await r.json();
        } catch { return null; }
      }

      (async function init(){
        // 1) desde storage
        const u = readUser();
        const id = getId(u);
        const url1 = getAvatar(u);
        if (url1) { img.src = url1 + '?t=' + Date.now(); return; }

        // 2) desde /me
        const me = await fetchMe();
        const url2 = getAvatar(me);
        if (url2) { img.src = url2 + '?t=' + Date.now(); return; }

        // 3) desde /usuarios/{id} (si tenemos id)
        if (id) {
          const byId = await fetchById(id);
          const url3 = getAvatar(byId);
          if (url3) { img.src = url3 + '?t=' + Date.now(); return; }

          // 4) último intento: convención de ruta directa (por si la BD no trae avatar_path)
          img.src = `/uploads/avatars/${id}/avatar.png?t=` + Date.now();
          return;
        }

        // 5) default
        img.src = DEFAULT;
      })();
    })();
  
