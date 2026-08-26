(() => {
          // ===== utilitarios =====
          const $ = (s,p=document)=>p.querySelector(s);
          const esc = s => String(s ?? '').replace(/[<>&"]/g, m=>({ '<':'&lt;','>':'&gt;','&':'&amp;'}[m]));
          const fmtDate = s => s ? String(s).replace('T',' ').replace('Z','') : '';

          // refs comunes del modal
          const B  = $('#au_backdrop');
          const E = {
              tabla:   $('#au_tabla'),
              usuario: $('#au_usuario'),
              fini:    $('#au_fini'),
              ffin:    $('#au_ffin'),
              buscar:  $('#au_buscar'),
              info:    $('#au_info'),
              // los existentes:
              prodWrap: $('#au_tbl_prod_wrap'),
              movWrap:  $('#au_tbl_mov_wrap'),
          };

          // === crea contenedor/tabla para USUARIOS si no existe ===
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
      </table>
    `;
              // Insertar justo donde están los otros
              E.movWrap.after(wrap);
          }

          function userWrap()  { return $('#au_tbl_user_wrap'); }
          function userTBody() { return $('#au_tb_user'); }

          async function api(path, params = {}) {
              const url = new URL(path, location.origin);
              Object.entries(params).forEach(([k,v]) => { if (v!=null && v!=='') url.searchParams.set(k,v); });
              const r = await fetch(url, { credentials: 'include' });
              if (!r.ok) throw new Error(`HTTP ${r.status}`);
              return r.json();
          }

          function setDefaultDates(){
              if (!E.fini || !E.ffin) return;
              const hoy = new Date();
              const end = hoy.toISOString().slice(0,10);
              const start = new Date(hoy.getTime() - 6*86400000).toISOString().slice(0,10);
              if (!E.fini.value) E.fini.value = start;
              if (!E.ffin.value) E.ffin.value = end;
          }

          async function loadUsuarios(){
              try {
                  const list = await api('/api/auditoria/usuariosL');
                  const opts = ['<option value="">Todos los usuarios</option>']
                      .concat((list||[]).map(u => `<option>${esc(u)}</option>`));
                  E.usuario.innerHTML = opts.join('');
              } catch {}
          }

          function switchTable(){
              if (!E.tabla) return;
              const t = E.tabla.value;
              const UW = userWrap();

              // visibilidad
              if (t === 'usuarios') {
                  E.prodWrap.hidden = true;
                  E.movWrap.hidden  = true;
                  UW.hidden = false;
                  E.info.textContent = '—';
              } else {
                  // deja que tu script original controle productos/movimientos
                  UW.hidden = true;
              }
          }

          async function buscarUsuarios(){
              const tb = userTBody();
              if (!tb) return;

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
      </tr>
    `).join('');
          }

          // ====== boot ======
          document.addEventListener('DOMContentLoaded', () => {
              ensureUserTable();

              // cuando cambie "Tabla" => mostrar usuarios si corresponde
              E.tabla?.addEventListener('change', () => {
                  switchTable();
                  if (E.tabla.value === 'usuarios') buscarUsuarios();
              });

              // botón Buscar: si está en "usuarios", buscamos
              E.buscar?.addEventListener('click', () => {
                  if (E.tabla?.value === 'usuarios') buscarUsuarios();
              });

              // al abrir el modal: cargamos usuarios (una vez) y auto-buscamos si la pestaña es usuarios
              $('#au_open')?.addEventListener('click', async () => {
                  setDefaultDates();
                  // si aún no tiene opciones (solo la de "Todos…"), carga
                  if (E.usuario && E.usuario.options.length <= 1) await loadUsuarios();
                  switchTable();
                  if (E.tabla?.value === 'usuarios') buscarUsuarios();
              });
          });
      })();
  
