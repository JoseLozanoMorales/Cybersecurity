/* ==========================================================
       MODAL "Agregar Dirección"
       - Abre el modal, carga ciudades y crea la dirección
       - Usa window.PAGO del script grande para refrescar el <select>
       ========================================================== */
    (function(){
        // --- Elementos del modal ---
        const modalEl  = document.getElementById('addressModal');
        const formEl   = document.getElementById('addressForm');
        const citySel  = document.getElementById('addressCity');
        const inCalle  = document.getElementById('addressStreet');
        const inRef    = document.getElementById('addressReference');
        const selDir   = document.getElementById('selDireccion');

        // Instancia Bootstrap Modal (reutilizable)
        const modal    = () => (bootstrap.Modal.getInstance(modalEl) || new bootstrap.Modal(modalEl));

        // --- Helpers mínimos (consistentes con tu script grande) ---
        function getUsuarioId(){
            try{
                const raw = sessionStorage.getItem('user') || localStorage.getItem('user');
                const u = raw ? JSON.parse(raw) : null;
                return u?.UsuarioID ?? u?.usuarioId ?? u?.usuario_id ?? u?.id ?? null;
            }catch{ return null; }
        }
        async function fetchJSON(url, opt){
            const uid = getUsuarioId();
            const headers = Object.assign({}, (opt && opt.headers) || {});
            if (uid) headers['X-User-Id'] = uid;                // 👈 consistente con tu backend
            const r = await fetch(url, Object.assign({ credentials:'include', headers }, opt||{}));
            if (!r.ok) throw new Error(await r.text());
            const ct = r.headers.get('content-type') || '';
            return ct.includes('application/json') ? r.json() : r.text();
        }

        // --- Cargar ciudades cada vez que se abre el modal ---
        async function loadCities(){
            citySel.innerHTML = `<option value="">Cargando...</option>`;
            try{
                const arr = await fetchJSON('/api/ciudades');
                const list = (Array.isArray(arr)?arr:[])
                    .map(c => ({ id: c.ciudadId ?? c.id ?? c.ciudad_id, nombre: c.nombre||'' }))
                    .filter(c => c.id != null)
                    .sort((a,b)=> a.nombre.localeCompare(b.nombre, 'es'));
                citySel.innerHTML = `<option value="">Seleccionar ciudad</option>` +
                    list.map(c => `<option value="${c.id}">${c.nombre}</option>`).join('');
            }catch(e){
                console.error(e);
                citySel.innerHTML = `<option value="">(error)</option>`;
            }
        }

        // Limpiar y cargar ciudades al abrir
        modalEl?.addEventListener('show.bs.modal', async ()=>{
            formEl?.reset();
            await loadCities();
        });

        // (Opcional) Autofocus al mostrarse
        modalEl?.addEventListener('shown.bs.modal', ()=>{
            document.getElementById('addressStreet')?.focus();
        });

        // --- Crear la dirección desde el modal ---
        formEl?.addEventListener('submit', async (ev)=>{
            ev.preventDefault();

            const uid = getUsuarioId();
            if (!uid){ alert('Sesión no resuelta.'); return; }

            // Validación mínima
            const payload = {
                calle: inCalle.value.trim(),
                referencia: (inRef.value.trim() || null),
                ciudadId: citySel.value ? Number(citySel.value) : null
            };
            if (!payload.calle){ alert('Calle es obligatoria'); return; }
            if (!payload.ciudadId){ alert('Seleccione una ciudad'); return; }

            // Bloquea doble submit
            const submitBtn = formEl.querySelector('button[type="submit"]');
            if (submitBtn?.disabled) return;
            submitBtn && (submitBtn.disabled = true);

            try{
                // 1) Crea en backend
                const created = await fetchJSON(`/api/usuarios/${uid}/direcciones`, {
                    method:'POST',
                    headers:{ 'Content-Type':'application/json' },
                    body: JSON.stringify(payload)
                });

                // 2) Recarga el combo con tu función original (expuesta en window.PAGO)
                await window.PAGO?.reloadDirecciones?.();

                // 3) Preselecciona la recién creada y pinta en la UI (fija DIRECCION_ID)
                const newId = (created?.direccionId ?? created?.id ?? null);
                if (newId && selDir){
                    selDir.value = String(newId);
                    const list = window.PAGO?.getDirecciones?.() || [];
                    const d = list.find(x => Number(x.direccionId) === Number(newId));
                    if (d) window.PAGO?.setDireccion?.(d);
                }

                // 4) Cierra modal y (opcional) muestra feedback
                modal().hide();
                if (typeof setMsg === 'function') setMsg('Dirección agregada.', 'text-success');
            }catch(err){
                console.error(err);
                alert('No se pudo guardar la dirección.');
            }finally{
                submitBtn && (submitBtn.disabled = false);
            }
        });
    })();

