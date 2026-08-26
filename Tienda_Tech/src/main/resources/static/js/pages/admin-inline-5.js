(() => {
          // ===== Helpers =====
          const $  = (s, p=document) => p.querySelector(s);
          const esc = s => String(s ?? '').replace(/[<>&"]/g, m => ({'<':'&lt;','>':'&gt;','&':'&amp;','"':'&quot;'}[m]));
          const fmtDate = s => s ? String(s).replace('T',' ').replace('Z','') : '';
          async function api(path, params = {}) {
              const url = new URL(path, location.origin);
              Object.entries(params).forEach(([k,v]) => { if (v != null && v !== '') url.searchParams.set(k, v); });
              const r = await fetch(url, { credentials: 'include' });
              if (!r.ok) throw new Error(`HTTP ${r.status}`);
              return r.json();
          }

          // ===== Refs del modal Auditoría =====
          const MODAL = $('#au_backdrop');
          const E = {
              tabla:   $('#au_tabla'),
              usuario: $('#au_usuario'),
              fini:    $('#au_fini'),
              ffin:    $('#au_ffin'),
              buscar:  $('#au_buscar'),
              info:    $('#au_info'),
              prodId:  $('#au_producto_id'),
              prodWrap: $('#au_tbl_prod_wrap'),
              movWrap:  $('#au_tbl_mov_wrap')
          };

          const state = { usuariosCargados: false };

          // ===== Crear tabla de "Usuarios" si no existe =====
          function ensureUserTable() {
              if ($('#au_tbl_user_wrap')) return;
              const wrap = document.createElement('div');
              wrap.id = 'au_tbl_user_wrap';
              wrap.className = 'data-table';
              wrap.hidden = true;
              wrap.innerHTML = `
      <table>
        <thead>
          <tr>
            <th>Fecha/Hora</th>
            <th>Usuario</th>
            <th>IP</th>
            <th>Host</th>
            <th>Sesión</th>
          </tr>
        </thead>
        <tbody id="au_tb_user">
          <tr><td colspan="5" class="tt-muted-row">Sin datos…</td></tr>
        </tbody>
      </table>`;
              (E.movWrap || E.prodWrap).after(wrap);
          }
          const userWrap  = () => $('#au_tbl_user_wrap');
          const userTBody = () => $('#au_tb_user');

          // ===== Fechas por defecto =====
          function setDefaultDates(){
              if (!E.fini || !E.ffin) return;
              const hoy = new Date();
              const end = hoy.toISOString().slice(0,10);
              const start = new Date(hoy.getTime() - 6*86400000).toISOString().slice(0,10);
              if (!E.fini.value) E.fini.value = start;
              if (!E.ffin.value) E.ffin.value = end;
          }

          // ===== Carga de usuarios para la pestaña "Usuarios" =====
          async function loadUsuarios() {
              try {
                  const list = await api('/api/auditoria/usuariosL');
                  const opts = ['<option value="">Todos los usuarios</option>']
                      .concat((list || []).map(u => `<option>${esc(u)}</option>`));
                  E.usuario.innerHTML = opts.join('');
                  state.usuariosCargados = true;
              } catch (e) {
                  console.warn('No se pudieron cargar usuarios', e);
              }
          }

          // ===== Mostrar/ocultar secciones según pestaña =====
          function showUserView() {
              if (E.prodWrap) E.prodWrap.hidden = true;
              if (E.movWrap)  E.movWrap.hidden  = true;
              if (E.prodId)   ttSetStyle(E.prodId, 'display', 'none'); // ocultar Producto ID en Usuarios
              userWrap().hidden = false;
              E.info.textContent = '—';
          }
          function showProdView() {
              if (userWrap()) userWrap().hidden = true;
              if (E.prodWrap) E.prodWrap.hidden = false;
              if (E.movWrap)  E.movWrap.hidden  = true;
              if (E.prodId)   ttSetStyle(E.prodId, 'display', '');     // mostrar en Productos
          }
          function switchTable() {
              const t = E.tabla?.value;
              if (t === 'usuarios') {
                  showUserView();
                  if (!state.usuariosCargados) loadUsuarios();
              } else {
                  showProdView(); // deja que tu script de productos maneje la búsqueda
              }
          }

          // ===== Buscar y render de USUARIOS =====
          async function buscarUsuarios(){
              const tb = userTBody();
              if (!tb || E.tabla?.value !== 'usuarios') return;

              E.buscar.disabled = true;
              E.info.textContent = 'Buscando…';
              tb.innerHTML = `<tr><td colspan="5" class="tt-muted-row">Cargando…</td></tr>`;

              try {
                  const rows = await api('/api/auditoria/logins', {
                      usuario: E.usuario?.value || '',
                      desde:   E.fini?.value    || '',
                      hasta:   E.ffin?.value    || ''
                  });
                  renderUsuarios(rows || []);
                  E.info.textContent = `${rows?.length ?? 0} logins.`;
              } catch (e) {
                  tb.innerHTML = `<tr><td colspan="5" class="tt-muted-row">Error al cargar.</td></tr>`;
                  E.info.textContent = 'Error al cargar.';
                  console.error(e);
              } finally {
                  E.buscar.disabled = false;
              }
          }

          function renderUsuarios(rows){
              const tb = userTBody();
              if (!rows.length) {
                  tb.innerHTML = `<tr><td colspan="5" class="tt-muted-row">Sin datos…</td></tr>`;
                  return;
              }
              tb.innerHTML = rows.map(r => `
      <tr>
        <td>${esc(fmtDate(r.fechaLogin))}</td>
        <td>${esc(r.usuario)}</td>
        <td>${esc(r.ip)}</td>
        <td title="${esc(r.host)}">${esc(r.host)}</td>
        <td>${esc(r.idSesion)}</td>
      </tr>`).join('');
          }

          // ===== Boot =====
          document.addEventListener('DOMContentLoaded', () => {
              if (!MODAL) return;

              ensureUserTable();
              setDefaultDates();

              // Cambiar pestaña
              E.tabla?.addEventListener('change', () => {
                  switchTable();
                  if (E.tabla.value === 'usuarios') buscarUsuarios();
              });

              // Botón Buscar: solo actúa en pestaña Usuarios
              E.buscar?.addEventListener('click', () => {
                  if (E.tabla?.value === 'usuarios') buscarUsuarios();
              });

              // Abrir modal: prepara vista y precarga si corresponde
              $('#au_open')?.addEventListener('click', async () => {
                  setDefaultDates();
                  switchTable();
                  if (E.tabla?.value === 'usuarios') {
                      if (!state.usuariosCargados) await loadUsuarios();
                      buscarUsuarios();
                  }
              });

              // Enter en filtros => buscar (solo cuando pestaña = usuarios)
              [E.usuario, E.fini, E.ffin].forEach(ctrl => {
                  ctrl?.addEventListener('keydown', e => {
                      if (e.key === 'Enter' && E.tabla?.value === 'usuarios') buscarUsuarios();
                  });
              });
          });
      })();
  
