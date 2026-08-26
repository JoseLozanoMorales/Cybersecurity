(() => {
          // ===== Helpers básicos =====
          const $  = (sel, p=document) => p.querySelector(sel);
          const $$ = (sel, p=document) => Array.from(p.querySelectorAll(sel));
          const B  = $('#au_backdrop');

          const E = {
              open:    $('#au_open'),
              close:   $('#au_close'),
              buscar:  $('#au_buscar'),
              tabla:   $('#au_tabla'),
              usuario: $('#au_usuario'),
              prodId:  $('#au_producto_id'),
              fini:    $('#au_fini'),
              ffin:    $('#au_ffin'),
              info:    $('#au_info'),
              btnHist: $('#au_hist_user'),
              prodWrap: $('#au_tbl_prod_wrap'),
              movWrap:  $('#au_tbl_mov_wrap'),
              tbProd:   $('#au_tb_prod'),
              tbMov:    $('#au_tb_mov'),
          };

          const state = { usuariosCargados: false };

          function show(el){ el.hidden = false; }
          function hide(el){ el.hidden = true; }

          // ===== Utilidades de formato =====
          function fmtMoney(v){
              if (v == null || v === '') return '';
              const n = Number(v);
              return Number.isFinite(n) ? n.toFixed(2) : String(v);
          }
          function fmtDate(s){
              if (!s) return '';
              return String(s).replace('T',' ').replace('Z','');
          }
          function boolTxt(v){ return v === true ? 'Sí' : (v === false ? 'No' : ''); }
          function safe(v){ return (v == null) ? '' : v; }
          function esc(s){ return String(s).replace(/[<>&"]/g, m=>({ '<':'&lt;','>':'&gt;','&':'&amp;','"':'&quot;'}[m])); }

          // ===== API helper =====
          async function api(path, params = {}) {
              const url = new URL(path, location.origin);
              for (const [k,v] of Object.entries(params)) {
                  if (v !== '' && v != null) url.searchParams.set(k, v);
              }
              const r = await fetch(url, { credentials: 'include' });
              if (!r.ok) throw new Error(`HTTP ${r.status}`);
              return await r.json();
          }

          // ===== Apertura / cierre =====
          function setDefaultDates(){
              const hoy = new Date();
              const end = hoy.toISOString().slice(0,10);
              const start = new Date(hoy.getTime() - 6*86400000).toISOString().slice(0,10);
              if (!E.fini.value) E.fini.value = start;
              if (!E.ffin.value) E.ffin.value = end;
          }

          async function openModalAud(){
              setDefaultDates();
              if (!state.usuariosCargados) await loadUsuarios();
              switchTable(); // asegura visibilidad + limpia tabla opuesta
              syncHistButton();
              E.info.textContent = '—';
              show(B);
              // Autobuscar al abrir
              buscarAuditoria().catch(()=>{});
          }
          function closeModalAud(){ hide(B); }

          // ===== UI: tabla visible y limpieza =====
          function switchTable(){
              const t = E.tabla?.value;

              if (t === 'productos') {
                  if (E.usuario) E.usuario.value = '';

                  // Mostrar tabla de productos y ocultar las demás
                  if (E.prodWrap) E.prodWrap.hidden = false;
                  if (E.movWrap)  E.movWrap.hidden  = true;   // no usamos "movimientos" si el select tiene "usuarios"
                  if (E.tbMov)    E.tbMov.innerHTML = `<tr><td colspan="10" class="tt-muted-row">Sin datos…</td></tr>`;
              } else {
                  // Cuando NO es "productos" (ej: "usuarios"), ocultamos productos
                  if (E.prodWrap) E.prodWrap.hidden = true;
                  if (E.movWrap)  E.movWrap.hidden  = true;   // la vista de usuarios la maneja su propio script
                  if (E.tbProd)   E.tbProd.innerHTML = `<tr><td colspan="14" class="tt-muted-row">Sin datos…</td></tr>`;
              }

              E.info.textContent = '—';
          }

          // ===== Cargar usuarios =====
          async function loadUsuarios(){
              try {
                  const data = await api('/api/auditorias/usuarios');
                  const opts = ['<option value="">Todos los usuarios</option>']
                      .concat((Array.isArray(data) ? data : []).map(u => {
                          const val = (typeof u === 'string') ? u : (u?.usuario ?? '');
                          return `<option>${esc(val)}</option>`;
                      }));
                  E.usuario.innerHTML = opts.join('');
                  state.usuariosCargados = true;
              } catch (e) {
                  console.warn('No se pudieron cargar usuarios', e);
              }
          }

          // ===== Buscar =====
          async function buscarAuditoria(){
              const params = {
                  usuario: E.usuario.value || '',
                  productoId: E.prodId.value || '',
                  desde: E.fini.value || '',
                  hasta: E.ffin.value || '',
              };

              E.buscar.disabled = true;
              E.info.textContent = 'Buscando…';
              const t = E.tabla.value;

              // Spinner / estado
              if (t === 'productos') {
                  E.tbProd.innerHTML = `<tr><td colspan="9" class="tt-muted-row">Cargando…</td></tr>`;
              } else {
                  E.tbMov.innerHTML = `<tr><td colspan="10" class="tt-muted-row">Cargando…</td></tr>`;
              }

              try {
                  if (t === 'productos') {
                      const rows = await api('/api/auditorias/productos', params);
                      renderProductos(rows || []);
                      E.info.textContent = `${rows?.length ?? 0} cambios de productos.`;
                  } else {
                      const rows = await api('/api/auditorias/movimientos', params);
                      renderMovimientos(rows || []);
                      E.info.textContent = `${rows?.length ?? 0} movimientos auditados.`;
                  }
              } catch (e) {
                  console.error(e);
                  if (t === 'productos') {
                      E.tbProd.innerHTML = `<tr><td colspan="9" class="tt-muted-row">Error al cargar.</td></tr>`;
                  } else {
                      E.tbMov.innerHTML = `<tr><td colspan="10" class="tt-muted-row">Error al cargar.</td></tr>`;
                  }
                  E.info.textContent = 'Error al cargar.';
              } finally {
                  E.buscar.disabled = false;
                  syncHistButton();
              }
          }

          // ===== Render: producto_auditoria (columnas separadas antes/después) =====
          function renderProductos(rows){
              if (!rows.length) {
                  E.tbProd.innerHTML = `<tr><td colspan="14" class="tt-muted-row">Sin datos…</td></tr>`;
                  return;
              }

              // Helper: decide qué va en antes/después según el tipo
              const split = (tipo, before, after) => {
                  const t = String(tipo ?? '').trim().toUpperCase();
                  if (t === 'I') return [null, after];   // Inserción: antes vacío
                  if (t === 'D') return [before, null];  // (Opcional) Eliminación: después vacío
                  return [before, after];                // Update u otros
              };

              const html = rows.map(r => {
                  const fh   = fmtDate(r.fechahorareg ?? r.fechaHoraReg ?? r.fh);
                  const usr  = r.usuario ?? '';
                  const tipo = (r.tipo ?? '').toString().toUpperCase();
                  const pid  = r.producto_id ?? r.productoId ?? '';

                  // Valores crudos
                  const nomA0 = r.nombre ?? '';
                  const nomD0 = r.nombre_despues ?? r.nombreDespues ?? '';

                  const prA0  = r.preciounitario ?? r.precioUnitario;
                  const prD0  = r.preciounitario_despues ?? r.precioUnitarioDespues;

                  const stA0  = r.stock;
                  const stD0  = r.stock_despues ?? r.stockDespues;

                  const ivaA0 = r.iva_id ?? r.ivaId;
                  const ivaD0 = r.iva_id_despues ?? r.ivaIdDespues;

                  const hbA0  = r.habilitado;
                  const hbD0  = r.habilitado_despues ?? r.habilitadoDespues;

                  // Aplicar split según tipo
                  const [nomA, nomD] = split(tipo, nomA0, nomD0);
                  const [prA,  prD ] = split(tipo, prA0,  prD0);
                  const [stA,  stD ] = split(tipo, stA0,  stD0);
                  const [ivaA, ivaD] = split(tipo, ivaA0, ivaD0);
                  const [hbA,  hbD ] = split(tipo, hbA0,  hbD0);

                  // Formatos finales
                  const prAfmt = (prA == null ? '' : fmtMoney(prA));
                  const prDfmt = (prD == null ? '' : fmtMoney(prD));
                  const stAfmt = (stA == null ? '' : stA);
                  const stDfmt = (stD == null ? '' : stD);
                  const ivaAfmt= (ivaA== null ? '' : ivaA);
                  const ivaDfmt= (ivaD== null ? '' : ivaD);
                  const hbAfmt = (hbA == null ? '' : boolTxt(hbA));
                  const hbDfmt = (hbD == null ? '' : boolTxt(hbD));

                  return `
      <tr>
        <td>${esc(fh)}</td>
        <td>${esc(usr)}</td>
        <td>${esc(tipo)}</td>
        <td>${esc(pid)}</td>

        <td title="${esc(nomA ?? '')}">${esc(nomA ?? '')}</td>
        <td title="${esc(nomD ?? '')}"><strong>${esc(nomD ?? '')}</strong></td>

        <td>${prAfmt}</td>
        <td><strong>${prDfmt}</strong></td>

        <td>${esc(stAfmt)}</td>
        <td><strong>${esc(stDfmt)}</strong></td>

        <td>${esc(ivaAfmt)}</td>
        <td><strong>${esc(ivaDfmt)}</strong></td>

        <td>${hbAfmt}</td>
        <td><strong>${hbDfmt}</strong></td>
      </tr>`;
              }).join('');

              E.tbProd.innerHTML = html;
          }

          // ===== Render: movimiento_inventario_auditoria =====
          function renderMovimientos(rows){
              if (!rows.length) {
                  E.tbMov.innerHTML = `<tr><td colspan="10" class="tt-muted-row">Sin datos…</td></tr>`;
                  return;
              }
              const html = rows.map(r => {
                  const f   = fmtDate(r.fecha);
                  const u   = r.usuario ?? '';
                  const t   = r.tipo ?? '';
                  const pid = r.producto_id ?? r.productoId ?? '';
                  const sub = r.subtipo_id ?? r.subtipoId ?? '';
                  const c   = safe(r.cantidad);
                  const cu  = fmtMoney(r.costo_unitario ?? r.costoUnitario);
                  const ct  = fmtMoney(r.costo_total ?? r.costoTotal);
                  const ref = r.referencia ?? '';
                  const obs = r.observacion ?? '';
                  return `
        <tr>
          <td>${esc(f)}</td>
          <td>${esc(u)}</td>
          <td>${esc(t)}</td>
          <td>${esc(pid)}</td>
          <td>${esc(sub)}</td>
          <td>${esc(c)}</td>
          <td>${cu}</td>
          <td>${ct}</td>
          <td title="${esc(ref)}">${esc(ref)}</td>
          <td title="${esc(obs)}">${esc(obs)}</td>
        </tr>`;
              }).join('');
              E.tbMov.innerHTML = html;
          }

          // ===== Historial por usuario (otro HTML) =====
          function syncHistButton(){ E.btnHist.disabled = (E.usuario.value === ''); }
          function verHistorialUsuario(){
              const u = E.usuario.value;
              if (!u) return;
              const q = new URLSearchParams({
                  u,
                  desde: E.fini.value || '',
                  hasta: E.ffin.value || ''
              });
              // guarda también en storage si necesitas leerlo allá
              sessionStorage.setItem('tt_audit_user', u);
              location.href = `/auditoria-usuario.html?${q.toString()}`;
          }

          // ===== Eventos =====
          E.open?.addEventListener('click', openModalAud);
          E.close?.addEventListener('click', closeModalAud);

          E.tabla?.addEventListener('change', () => {
              switchTable();
              if (E.tabla.value === 'productos') buscarAuditoria(); // limpia y recarga productos
          });

          E.usuario?.addEventListener('change', syncHistButton);
          E.buscar?.addEventListener('click', buscarAuditoria);
          E.btnHist?.addEventListener('click', verHistorialUsuario);

          // Enter en filtros => buscar
          [E.usuario, E.prodId, E.fini, E.ffin].forEach(ctrl => {
              ctrl?.addEventListener('keydown', e => { if (e.key === 'Enter') buscarAuditoria(); });
          });

          // Cerrar con click fuera o ESC
          B.addEventListener('click', (ev) => { if (ev.target === B) closeModalAud(); });
          window.addEventListener('keydown', (ev) => { if (!B.hidden && ev.key === 'Escape') closeModalAud(); });
      })();
  
