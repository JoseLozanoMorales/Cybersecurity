document.addEventListener('DOMContentLoaded', () => {
      const $ = (s) => document.querySelector(s);

      // 1) Resolver userId de forma robusta
      const qsId = Number(new URLSearchParams(location.search).get('id')) || 0;

      function resolveUserIdFromCache() {
        try {
          const raw = sessionStorage.getItem('user') || localStorage.getItem('user');
          if (!raw) return 0;
          const u = JSON.parse(raw);
          const keys = [
            'usuario_id', 'usuarioId', 'id_usuario', 'idUsuario',
            'id', 'userId', 'idUser'
          ];
          for (const k of keys) {
            const v = Number(u?.[k]);
            if (Number.isFinite(v) && v > 0) return v;
          }
          return 0;
        } catch { return 0; }
      }

      function resolveUserIdFromPath() {
        const segs = location.pathname.split('/').filter(Boolean);
        const last = Number(segs[segs.length - 1]);
        return Number.isFinite(last) && last > 0 ? last : 0;
      }

      let userId = qsId || resolveUserIdFromCache() || resolveUserIdFromPath();

      if (!userId) {
        const manual = Number(prompt('No se pudo resolver el ID. Ingresa el ID de usuario a editar:') || 0);
        if (Number.isFinite(manual) && manual > 0) userId = manual;
      }

      document.getElementById('usuarioId').value = userId || '';
      if (!userId) {
        alert('No se pudo resolver el ID de usuario. Abre la página como: editar_usuario.html?id=16');
      }

      // Cache del usuario (si existe)
      let cached = {};
      try {
        const raw = sessionStorage.getItem('user') || localStorage.getItem('user') || '{}';
        cached = JSON.parse(raw) || {};
      } catch { cached = {}; }

      // 2) Inputs
      const form = $('#editUserForm');
      const iNombre   = $('#nombre');
      const iUsuario  = $('#usuario');
      const iCorreo   = $('#correo');
      const iTelefono = $('#telefono');
      const iPass     = $('#contrasena');
      const iPass2    = $('#repetirContrasena');

      // 3) Prellenar desde cache (opcional)
      if (cached.nombre)   iNombre.value   = cached.nombre;
      if (cached.usuario)  iUsuario.value  = cached.usuario;
      if (cached.correo)   iCorreo.value   = cached.correo;
      if (cached.telefono) iTelefono.value = cached.telefono;

    // --- Avatar: pintar actual y manejar subida ---

    const imgAvatar   = document.getElementById('avatar-edit');
    const fileAvatar  = document.getElementById('avatarFile');
    const btnAvatar   = document.getElementById('avatarEdit');

    function resolveAvatarUrlForView() {
      // intenta desde cache; si no, la ruta estándar del servidor
      const cachedUrl = cached.avatar_path || cached.avatarPath || null;
      if (cachedUrl) return `${cachedUrl}?t=${Date.now()}`;
      if (userId)    return `/uploads/avatars/${userId}/avatar.png?t=${Date.now()}`;
      return `/assets/avatars/defaults/user.png`;
    }
    imgAvatar.addEventListener('error', () => {
      imgAvatar.src = '/assets/avatars/defaults/user.png?t=' + Date.now();
    });
    // pintar actual
    imgAvatar.src = resolveAvatarUrlForView();

    // abrir selector de archivo al hacer clic en el avatar
    btnAvatar.addEventListener('click', () => fileAvatar.click());

    // subir archivo y refrescar imagen
    fileAvatar.addEventListener('change', async (e) => {
      const file = e.target.files?.[0];
      if (!file || !userId) return;

      const fd = new FormData();
      fd.append('file', file);

      try {
        const resp = await fetch(`/api/usuarios/${userId}/avatar`, { method: 'POST', body: fd });
        const data = await resp.json().catch(() => ({}));

        if (!resp.ok || data.ok === false) {
          throw new Error(data.error || `HTTP ${resp.status}`);
        }

        // url “limpia” que guarda el backend; cache-buster para refrescar la vista
        const baseUrl = data.url || `/uploads/avatars/${userId}/avatar.png`;
        imgAvatar.src = `${baseUrl}?t=${Date.now()}`;

        // persiste en cache local para que otras páginas lo usen
        try {
          const raw = sessionStorage.getItem('user') || localStorage.getItem('user') || '{}';
          const u   = JSON.parse(raw);
          u.avatar_path = baseUrl;
          sessionStorage.setItem('user', JSON.stringify(u));
          localStorage.setItem('user', JSON.stringify(u));
          cached = u; // sincroniza la variable local
        } catch (_) {}
        const bust = Date.now();
        document.querySelectorAll('img[data-avatar="user"]').forEach(el => {
          el.src = `${baseUrl}?t=${bust}`;
        });
        alert('✅ Avatar actualizado');
      } catch (err) {
        alert('❌ No se pudo actualizar el avatar: ' + (err.message || err));
      } finally {
        // permitir volver a elegir el mismo archivo
        fileAvatar.value = '';
      }
    });


      // 4) Envío
      form.addEventListener('submit', async (e) => {
        e.preventDefault();

        // Validación simple: contraseñas iguales si se ingresaron
        if (iPass.value || iPass2.value) {
          if (iPass.value !== iPass2.value) { alert('Las contraseñas no coinciden'); return; }
        }

        // Construye payload; null => no cambiar (COALESCE en SP)
        const payload = {
          nombre:      (iNombre.value   || '').trim() || null,
          usuario:     (iUsuario.value  || '').trim() || null,
          correo:      (iCorreo.value   || '').trim() || null,
          telefono:    (iTelefono.value || '').trim() || null,
          contrasenia: (iPass.value     || '').trim() || null
          // cedula:   null
        };

        try {
          const res  = await fetch(`/api/usuarios/cliente/${userId}`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
          });
          const text = await res.text();

          if (!res.ok) {
            if (res.status === 409) { alert(text || 'Correo/usuario ya usados'); return; }
            throw new Error(text || `HTTP ${res.status}`);
          }

          alert('✅ Cambios guardados');

          // Actualiza cache local (sin usar optional chaining a la izquierda)
          const updated = {
            ...cached,
            nombre:   payload.nombre   ?? cached.nombre,
            usuario:  payload.usuario  ?? cached.usuario,
            correo:   payload.correo   ?? cached.correo,
            telefono: payload.telefono ?? cached.telefono
          };
          sessionStorage.setItem('user', JSON.stringify(updated));
        } catch (err) {
          alert('❌ Error: ' + (err.message || err));
        }
      });
    });
  
