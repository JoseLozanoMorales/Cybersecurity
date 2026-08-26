document.addEventListener('DOMContentLoaded', () => {
        const $ = (s) => document.querySelector(s);

        // -------- Resolver userId --------
        const qsId = Number(new URLSearchParams(location.search).get('id')) || 0;
        function resolveUserIdFromCache(){
            try{
                const raw = sessionStorage.getItem('user') || localStorage.getItem('user');
                if(!raw) return 0;
                const u = JSON.parse(raw);
                const keys = ['usuario_id','usuarioId','id_usuario','idUsuario','id','userId','idUser'];
                for (const k of keys) {
                    const v = Number(u?.[k]); if (Number.isFinite(v) && v>0) return v;
                }
                return 0;
            } catch { return 0; }
        }
        function resolveUserIdFromPath(){
            const segs = location.pathname.split('/').filter(Boolean);
            const last = Number(segs.at(-1));
            return Number.isFinite(last) && last>0 ? last : 0;
        }
        let userId = qsId || resolveUserIdFromCache() || resolveUserIdFromPath();
        $('#usuarioId').value = userId || '';

        if (!userId) {
            showMessage('warning', 'No se pudo resolver el ID de usuario. Abre esta página como <code>editar_usuario.html?id=16</code> o inicia sesión para precargar tu perfil.');
        }

        // -------- Inputs --------
        const form      = $('#editUserForm');
        const iNombre   = $('#nombre');
        const iUsuario  = $('#usuario');
        const iCorreo   = $('#correo');
        const iTelefono = $('#telefono');
        const iPass     = $('#contrasena');
        const iPass2    = $('#repetirContrasena');
        const sRol      = $('#rolId'); // puede no existir

        // -------- Prefill desde cache (si existe) --------
        try {
            const raw = sessionStorage.getItem('user') || localStorage.getItem('user');
            if (raw) {
                const u = JSON.parse(raw);
                if (u?.nombre)   iNombre.value   = u.nombre;
                if (u?.usuario)  iUsuario.value  = u.usuario;
                if (u?.correo)   iCorreo.value   = u.correo;
                if (u?.telefono) iTelefono.value = u.telefono;
                const cachedRol = Number(u?.id_rol ?? u?.rol_id ?? u?.idRol ?? 0);
                if (cachedRol && sRol) sRol.value = String(cachedRol);
            }
        } catch {}

        // -------- Helpers --------
        const isEmail = (s) => /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test((s||'').trim());
        const onlyDigits = (s) => (s||'').replace(/\D+/g,'');

        // -------- UI estado botón / form --------
        const btn = form.querySelector('button[type="submit"]');
        const btnText = document.getElementById('btnText');
        const btnSpinner = document.getElementById('btnSpinner');
        function setBusy(busy){
            if (busy){
                btnSpinner?.classList.remove('d-none');
                btn?.setAttribute('disabled','true');
                btnText && (btnText.textContent = 'Guardando…');
                Array.from(form.elements).forEach(el => el.disabled = true);
            } else {
                btnSpinner?.classList.add('d-none');
                btn?.removeAttribute('disabled');
                btnText && (btnText.textContent = 'Guardar cambios');
                Array.from(form.elements).forEach(el => el.disabled = false);
            }
        }

        // -------- Submit: PUT --------
        form.addEventListener('submit', async (e) => {
            e.preventDefault();

            if (!userId) { showMessage('danger', 'Falta el ID de usuario.'); return; }

            // Validaciones mínimas
            const correo = iCorreo.value.trim();
            if (correo && !isEmail(correo)) { showMessage('warning', 'Correo inválido.'); return; }

            const tel = onlyDigits(iTelefono.value);
            if (tel && tel.length !== 10) { showMessage('warning', 'El teléfono debe tener 10 dígitos.'); return; }

            if ((iPass.value || iPass2.value) && iPass.value !== iPass2.value) {
                showMessage('warning', 'Las contraseñas no coinciden.'); return;
            }

            // Construir payload; null => no cambiar
            const payload = {
                nombre:     (iNombre.value   || '').trim() || null,
                usuario:    (iUsuario.value  || '').trim() || null,
                correo:     correo || null,
                telefono:   (tel || '') || null,
                contrasena: (iPass.value     || '').trim() || null,
                cedula:     null // no se edita aquí
            };

            // === ELEGIR ENDPOINT SEGÚN ROL (usa select si existe, si no usa cache del login) ===
            const rolIdSel   = sRol ? Number(sRol.value || 0) : 0;
            const rolIdCache = rolActualDesdeCache();
            const rolId      = rolIdSel || rolIdCache;

            const isAdminOrTrab = (rolId === 1 || rolId === 3);

            // IMPORTANTE: si es admin/trabajador, pasa rolId en la query
            const url = isAdminOrTrab
                ? `/api/usuarios/admin/${userId}?rolId=${rolId}`
                : `/api/usuarios/cliente/${userId}`;

            // Limpia feedback anterior y envía
            showMessage('info', 'Enviando cambios…');
            setBusy(true);

            try {
                const res  = await fetch(url, {
                    method: 'PUT',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify(payload)
                });

                const raw = await res.text().catch(() => '');
                let serverMsg = '';
                try {
                    const j = raw ? JSON.parse(raw) : null;
                    if (j && typeof j === 'object') {
                        serverMsg = j.message || j.error || j.detail || JSON.stringify(j);
                    } else {
                        serverMsg = raw;
                    }
                } catch { serverMsg = raw; }

                if (!res.ok) {
                    if (res.status === 409) {
                        showMessage('danger', serverMsg || 'Correo o usuario ya están en uso.');
                    } else if (res.status === 400) {
                        showMessage('danger', serverMsg || 'Solicitud inválida. Revisa los datos ingresados.');
                    } else if (res.status === 404) {
                        showMessage('danger', serverMsg || 'Usuario no encontrado.');
                    } else {
                        showMessage('danger', serverMsg || `Error HTTP ${res.status}.`);
                    }
                    setBusy(false);
                    return;
                }

                showMessage('success', '✅ Cambios guardados correctamente. Redirigiendo…');

                // Refresca cache (sin contraseña)
                try {
                    const cacheRaw = sessionStorage.getItem('user') || localStorage.getItem('user');
                    const cached = cacheRaw ? JSON.parse(cacheRaw) : {};
                    const updated = {
                        ...cached,
                        nombre:   payload.nombre   ?? cached?.nombre,
                        usuario:  payload.usuario  ?? cached?.usuario,
                        correo:   payload.correo   ?? cached?.correo,
                        telefono: payload.telefono ?? cached?.telefono,
                        ...(sRol ? { id_rol: rolId } : {})  // si hay select, refleja cambio local
                    };
                    sessionStorage.setItem('user', JSON.stringify(updated));
                } catch {}

                setTimeout(() => { goBack(); }, 800);

            } catch (err) {
                showMessage('danger', '❌ Error de conexión. ' + (err?.message || 'Inténtalo más tarde.'));
                setBusy(false);
            }
        });
    });

