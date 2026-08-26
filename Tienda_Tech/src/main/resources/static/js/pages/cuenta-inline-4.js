document.addEventListener('DOMContentLoaded', () => {
      // --- USER_ID desde sessionStorage (fallback 1)
      const USER_ID = (() => {
        try {
          const raw = sessionStorage.getItem('user') || localStorage.getItem('user');
          if (!raw) return 1;
          const u = JSON.parse(raw);
          return u.usuarioId ?? u.id ?? u.userId ?? u.id_usuario ?? 1;
        } catch { return 1; }
      })();

      // --- Elementos
      const listEl  = document.getElementById('direcciones-container');
      const modalEl = document.getElementById('addressModal');
      const formEl  = document.getElementById('addressForm');
      const citySel = document.getElementById('addressCity');
      const inCalle = document.getElementById('addressStreet');
      const inRef   = document.getElementById('addressReference');

      // Oculta y des-activa el selector de provincia si existe en el markup
      (function hideProvinceBlock(){
        const provSel  = document.getElementById('addressProvince');
        const provWrap = provSel ? provSel.closest('.col-md-6') : null;
        if (provSel)  provSel.removeAttribute('required');
        if (provWrap) ttSetStyle(provWrap, 'display', 'none');
      })();

      // --- Estado
      let CIUDADES = [];     // [{ ciudadId, nombre }]
      let editingId = null;  // null = crear, number = editar

      // --- Helpers
      const esc = s => s ? s.replace(/[&<>"']/g, m => ({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#039;'}[m])) : '';
      const option = (v,t) => `<option value="${v}">${t}</option>`;
      const setLoading = (sel,msg='Cargando...') => { if (sel) sel.innerHTML = option('', msg); };

      function cardHTML(d, n) {
        const ciudad = d.ciudadNombre || '';
        const provincia = d.provinciaNombre || '';
        const tituloUbic = (ciudad && provincia) ? `${ciudad}, ${provincia}` : (ciudad || provincia || 'Dirección');

        const dataAttrs = [
          `data-id="${d.direccionId}"`,
          `data-calle="${esc(d.calle || '')}"`,
          `data-referencia="${esc(d.referencia || '')}"`,
          `data-ciudadid="${d.ciudadId ?? ''}"`
        ].join(' ');

        return `
        <div class="col-12 col-md-6">
          <div class="address-card p-3 border rounded-3 h-100">
            <div class="d-flex justify-content-between align-items-center mb-1">
              <strong>${esc(tituloUbic)}</strong>
              <span class="text-muted">#${n}</span>
            </div>
            <div class="mb-1">${esc(d.calle || '')}</div>
            ${d.referencia ? `<div class="text-muted small">${esc(d.referencia)}</div>` : ''}
            <div class="mt-2 d-flex gap-2">
              <button class="btn btn-sm btn-outline-primary" data-action="edit" ${dataAttrs}>
                <i class="bi bi-pencil-square"></i> Editar
              </button>
              <button class="btn btn-sm btn-outline-danger" data-action="delete" data-id="${d.direccionId}">
                <i class="bi bi-trash"></i> Eliminar
              </button>
            </div>
          </div>
        </div>`;
      }

      // --- Listar direcciones (habilitadas según backend)
      async function loadUserAddresses() {
        if (!listEl) return;
        listEl.innerHTML = `<div class="col-12 text-center py-4 text-muted">Cargando direcciones…</div>`;
        try {
          const r = await fetch(`/api/usuarios/${USER_ID}/direcciones`);
          if (!r.ok) throw new Error(await r.text());
          const data = await r.json();

          // Si quieres asegurar “más nuevas primero”, descomenta:
          // data.sort((a,b) => (b.direccionId ?? 0) - (a.direccionId ?? 0));

          listEl.innerHTML = (!Array.isArray(data) || data.length === 0)
                  ? `<div class="col-12 text-center text-muted py-4">Aún no tienes direcciones guardadas.</div>`
                  : data.map((item, idx) => cardHTML(item, idx + 1)).join('');
        } catch (e) {
          console.error(e);
          listEl.innerHTML = `<div class="col-12 text-danger text-center py-4">No se pudieron cargar las direcciones.</div>`;
          notify('No se pudieron cargar las direcciones.', 'error');
        }
      }

      // --- Cargar todas las ciudades
      async function loadCities() {
        setLoading(citySel);
        try {
          const r = await fetch('/api/ciudades');
          if (!r.ok) throw new Error('No se pudo cargar ciudades');
          const arr = await r.json();
          CIUDADES = (Array.isArray(arr) ? arr : [])
                  .map(c => ({ ciudadId: c.ciudadId ?? c.id ?? c.ciudad_id, nombre: c.nombre || '' }))
                  .filter(c => c.ciudadId != null)
                  .sort((a,b) => a.nombre.localeCompare(b.nombre, 'es'));
          citySel.innerHTML = option('', 'Seleccionar ciudad') +
                  CIUDADES.map(c => option(c.ciudadId, c.nombre)).join('');
        } catch (e) {
          console.error(e);
          citySel.innerHTML = option('', 'Error cargando ciudades');
        }
      }

      // --- Clicks en tarjetas (editar / eliminar)
      listEl?.addEventListener('click', async (ev) => {
        const btn = ev.target.closest('[data-action]');
        if (!btn) return;
        const action = btn.getAttribute('data-action');

        if (action === 'delete') {
          const id = btn.getAttribute('data-id');
          if (!id || !confirm('¿Eliminar esta dirección?')) return;
          try {
            const resp = await fetch(`/api/usuarios/${USER_ID}/direcciones/${id}`, { method: 'DELETE' });
            if (resp.status === 204) await loadUserAddresses();
            else throw new Error(`Error ${resp.status}: ${await resp.text()}`);
          } catch (err) {
            console.error(err);
            alert('No se pudo eliminar la dirección.');
          }
          return;
        }

        if (action === 'edit') {
          editingId     = Number(btn.getAttribute('data-id'));
          inCalle.value = btn.getAttribute('data-calle') || '';
          inRef.value   = btn.getAttribute('data-referencia') || '';
          const cid     = btn.getAttribute('data-ciudadid') || '';
          if (!CIUDADES.length) await loadCities();
          citySel.value = cid;
          (bootstrap.Modal.getInstance(modalEl) || new bootstrap.Modal(modalEl)).show();
        }
      });

      // --- Modal eventos
      modalEl?.addEventListener('show.bs.modal', async () => {
        if (!CIUDADES.length) await loadCities();
        if (editingId == null) { formEl.reset(); citySel.value=''; }
      });
      modalEl?.addEventListener('hidden.bs.modal', () => { editingId = null; formEl.reset(); });

      // --- Guardar (crear/editar)
      formEl?.addEventListener('submit', async (ev) => {
        ev.preventDefault();
        const payload = {
          calle:      inCalle.value.trim(),
          referencia: (inRef.value.trim() || null),
          ciudadId:   citySel.value ? Number(citySel.value) : null // backend lo mapea a Short
        };
        if (!payload.calle)    { notify('Calle es obligatoria.', 'error'); return; }
        if (!payload.ciudadId) { notify('Seleccione una ciudad.', 'error'); return; }

        /*try {
          if (editingId == null) {
            // CREAR
            const r = await fetch(`/api/usuarios/${USER_ID}/direcciones`, {
              method: 'POST', headers: { 'Content-Type': 'application/json' },
              body: JSON.stringify(payload)
            });
            if (!r.ok) throw new Error(`Error ${r.status}: ${await r.text()}`);
          } else {
            // EDITAR
            const r = await fetch(`/api/usuarios/${USER_ID}/direcciones/${editingId}`, {
              method: 'PUT', headers: { 'Content-Type': 'application/json' },
              body: JSON.stringify(payload)
            });
            if (!r.ok) throw new Error(`Error ${r.status}: ${await r.text()}`);
          }
          (bootstrap.Modal.getInstance(modalEl) || new bootstrap.Modal(modalEl)).hide();
          await loadUserAddresses();
        } catch (e) {
          console.error('No se pudo guardar la dirección:', e);
          alert('No se pudo guardar la dirección.');
        }*/

        try {
          if (editingId == null) {
            // CREAR
            await fetchOrThrow(`/api/usuarios/${USER_ID}/direcciones`, {
              method: 'POST', headers: { 'Content-Type': 'application/json' },
              body: JSON.stringify(payload)
            });
            notify('Dirección agregada.', 'success');
          } else {
            // EDITAR
            await fetchOrThrow(`/api/usuarios/${USER_ID}/direcciones/${editingId}`, {
              method: 'PUT', headers: { 'Content-Type': 'application/json' },
              body: JSON.stringify(payload)
            });
            notify('Dirección actualizada.', 'success');
          }
          (bootstrap.Modal.getInstance(modalEl) || new bootstrap.Modal(modalEl)).hide();
          await loadUserAddresses();
        } catch (e) {
          console.error('No se pudo guardar la dirección:', e);
          // Mensajes más claros por estado (si usas fetchOrThrow tendrás e.status)
          if (e.status === 409) notify('Datos ya guardados: ya existe una dirección con esos datos.', 'error');
          else if (e.status === 422) notify('Datos inválidos. Verifique la información.', 'error');
          else notify(e.message || 'No se pudo guardar la dirección.', 'error');
        }
      });

      // --- Inicio
      loadCities().catch(()=>{});
      loadUserAddresses().catch(()=>{});
    });
  
