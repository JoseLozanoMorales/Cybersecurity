//TOMAR USUARIO PERO NO POR TOKEN
function currentUsername(){
  try {
    const raw = sessionStorage.getItem('user') || localStorage.getItem('user');
    if (!raw) return null;
    const u = JSON.parse(raw);
    // Ajusta estas claves a tu objeto real
    return u?.usuario || u?.username || u?.user || null;
  } catch { return null; }
}
const GAL_API = {
    list:   (pid)=> `/api/productos/${pid}/galeria/lista`,    // lista de ítems con flags/posiciones
    upload: (pid)=> `/api/productos/${pid}/galeria`,          // POST (JSON) batch
    flags:  `/api/galeria/flags`,                             // PATCH (JSON) batch
    reorder:(pid)=> `/api/productos/${pid}/galeria/reordenar`,// POST vista + items
    portada:(pid,gid)=> `/api/productos/${pid}/galeria/${gid}/portada`, // POST
    media:  (gid)=> `/api/galeria/${gid}/media`,              // GET binario
    del:    (gid)=> `/api/galeria/${gid}`                     // DELETE ?hard=
  };

  const GAL_STATE = {
    productoId: null,
    nombre: '',
    items: [],          // [{galeria_id, es_portada, para_galeria, para_menu, posicion_galeria, posicion_menu, habilitado, descripcion, mime_type, ancho, alto, peso_bytes}]
    vista: 'galeria',   // 'galeria' | 'menu'
    selectedFiles: [],  // [{file, para_menu, descripcion}]
    dragging: null      // galeria_id
  };

  const $g = (s) => document.querySelector(s);
  const $gi = (id) => document.getElementById(id);

  function openGaleriaModal(productoId, nombre){
    GAL_STATE.productoId = Number(productoId);
    GAL_STATE.nombre = nombre || '';
    GAL_STATE.vista = 'galeria';
    GAL_STATE.selectedFiles = [];
    $gi('galTitle').textContent = `Galería — #${productoId} ${nombre?('· '+nombre):''}`;
    $gi('modalGaleria').hidden = false;

    // radios vista
    document.querySelectorAll('input[name="gal_vista"]').forEach(r=>{
      r.checked = (r.value === 'galeria');
      r.onchange = () => { GAL_STATE.vista = r.value; renderGalGrid(); };
    });

    // botones & selects
    $gi('gal_btnCerrar').onclick = closeGaleriaModal;
    $gi('gal_btnSel').onclick = ()=> $gi('gal_files').click();
    $gi('gal_files').onchange = onGalFilesChange;
    $gi('gal_btnUpload').onclick = subirSeleccionadas;
    $gi('gal_btnFlags').onclick = guardarFlags;
    $gi('gal_btnOrden').onclick = guardarOrdenVista;
    preventWindowDrop(true); // activar mientras esté abierto
    loadGaleria();
  }
  // Drag & drop en el área punteada del modal de galería
  (function wireDropZona(){
    const dz = document.querySelector('#modalGaleria .dashed');
    const input = document.getElementById('gal_files');
    if (!dz || !input) return;

    const addCls = () => dz.classList.add('is-over');
    const rmCls  = () => dz.classList.remove('is-over');

    ['dragenter','dragover'].forEach(ev => dz.addEventListener(ev, e => {
      e.preventDefault(); e.stopPropagation(); addCls();
    }));
    ['dragleave','dragend'].forEach(ev => dz.addEventListener(ev, e => {
      e.preventDefault(); e.stopPropagation(); rmCls();
    }));
    dz.addEventListener('drop', e => {
      e.preventDefault(); e.stopPropagation(); rmCls();
      const files = Array.from(e.dataTransfer?.files || []);
      if (!files.length) return;

      // Colocar los archivos en el <input type="file">
      const dt = new DataTransfer();
      files.forEach(f => dt.items.add(f));
      input.files = dt.files;

      // Reutiliza tu lógica existente
      if (typeof onGalFilesChange === 'function') {
        onGalFilesChange({ target: input });
      }
    });
  })();


  // (Opcional) Sólo mientras el modal está abierto, evita drops sobre toda la ventana:
  function preventWindowDrop(enable){
    const stop = (e)=>{ e.preventDefault(); e.stopPropagation(); };
    const fnOver = enable ? (e)=>stop(e) : null;
    const fnDrop = enable ? (e)=>stop(e) : null;

    if (enable){
      window.addEventListener('dragover', fnOver, { passive:false });
      window.addEventListener('drop', fnDrop, { passive:false });
    } else {
      window.removeEventListener('dragover', fnOver);
      window.removeEventListener('drop', fnDrop);
    }
  }

  function closeGaleriaModal(){
    $gi('modalGaleria').hidden = true;
    $gi('gal_files').value = '';
    $gi('gal_sel_info').textContent = 'Ningún archivo seleccionado';
    GAL_STATE.selectedFiles = [];
    preventWindowDrop(false);  // opcional
  }

  /* ==== Carga y render ==== */

  async function loadGaleria(){
    $gi('gal_grid').innerHTML = '<div class="tt-muted-row">Cargando…</div>';
    try{
      const res = await fetch(GAL_API.list(GAL_STATE.productoId));
      if(!res.ok){ throw new Error('HTTP '+res.status); }
      const data = await res.json();
      GAL_STATE.items = Array.isArray(data) ? data : [];
    }catch(e){
      GAL_STATE.items = [];
    }
    renderCounters();
    renderGalGrid();
  }

  function renderCounters(){
    const total = GAL_STATE.items.length;
    const enGal = GAL_STATE.items.filter(x=> !!x.para_galeria && x.habilitado !== false).length;
    const enMenu= GAL_STATE.items.filter(x=> !!x.para_menu    && x.habilitado !== false).length;
    const portada = GAL_STATE.items.find(x=> x.es_portada && x.habilitado !== false);
    $gi('gal_counters').innerHTML =
      `Total: <b>${total}</b> · En galería: <b>${enGal}</b> (máx 15) · En menú: <b>${enMenu}</b> (máx 5) · Portada: <b>${portada?('#'+portada.galeria_id):'—'}</b>`;
  }

  function renderGalGrid(){
    const vista = GAL_STATE.vista; // 'galeria' | 'menu'
    // filtro de la vista (solo las que participan en esa vista) para ordenar
    const subset = GAL_STATE.items
      .filter(x => x.habilitado !== false && (vista === 'galeria' ? x.para_galeria : x.para_menu))
      .sort((a,b) => {
        const pa = (vista==='galeria') ? (a.posicion_galeria ?? 1e9) : (a.posicion_menu ?? 1e9);
        const pb = (vista==='galeria') ? (b.posicion_galeria ?? 1e9) : (b.posicion_menu ?? 1e9);
        if (pa !== pb) return pa - pb;
        return a.galeria_id - b.galeria_id;
      });

    const all = GAL_STATE.items.slice().sort((a,b)=> a.galeria_id - b.galeria_id);

    // grid
    const html = `
    <style>
      .gal-grid{display:grid;grid-template-columns:repeat(auto-fill,minmax(160px,1fr));gap:.75rem;}
      .gal-card{border:1px solid #ddd;border-radius:10px;overflow:hidden;background:#fff; position:relative}
      .gal-card.dragging{opacity:.6; outline:2px dashed #888}
      .gal-thumb{aspect-ratio:1/1; display:block; width:100%; object-fit:cover; background:#f3f3f3}
      .gal-body{padding:.5rem; display:grid; gap:.35rem; font-size:.9rem}
      .gal-row{display:flex; align-items:center; gap:.4rem; flex-wrap:wrap}
      .gal-pill{font-size:.75rem; background:#f3f3f3; padding:.15rem .4rem; border-radius:.5rem}
      .gal-actions{display:flex; gap:.35rem; align-items:center}
      .gal-star{cursor:pointer; font-size:1.1rem}
      .gal-del{cursor:pointer;}
      .gal-chk{display:flex; gap:.35rem; align-items:center}
    </style>

    <div class="gal-grid" id="gal_grid_inner">
      ${all.map(x=>{
        const inVista = subset.find(s=> s.galeria_id === x.galeria_id);
        const pos = (vista==='galeria') ? x.posicion_galeria : x.posicion_menu;
        const star = x.es_portada ? '⭐' : '☆';
        const mediaUrl = GAL_API.media(x.galeria_id);
        return `
          <div class="gal-card" draggable="${inVista? 'true':'false'}" data-gid="${x.galeria_id}">
            <img class="gal-thumb" loading="lazy" src="${mediaUrl}" alt="${(x.descripcion||'')}" />
            <div class="gal-body">
              <div class="gal-row">
                <span class="gal-pill">#${x.galeria_id}</span>
                ${inVista ? `<span class="gal-pill">pos: ${pos ?? '—'}</span>`: '<span class="gal-pill muted">fuera de vista</span>'}
                <span class="gal-actions" style="margin-left:auto;">
                  <span class="gal-star" title="Marcar portada" data-star="${x.galeria_id}">${star}</span>
                  <button class="btn btn--sm btn-outline gal-del" title="Eliminar" data-del="${x.galeria_id}">🗑</button>
                </span>
              </div>
              <div class="gal-row">
                <label class="gal-chk"><input type="checkbox" data-flag="gal" ${x.para_galeria?'checked':''} data-gid="${x.galeria_id}"> galería</label>
                <label class="gal-chk"><input type="checkbox" data-flag="menu" ${x.para_menu?'checked':''} data-gid="${x.galeria_id}"> menú</label>
                <label class="gal-chk"><input type="checkbox" data-flag="hab"  ${x.habilitado!==false?'checked':''} data-gid="${x.galeria_id}"> habilitado</label>
              </div>
              <input class="control" data-alt="${x.galeria_id}" value="${x.descripcion ? (''+x.descripcion).replace(/"/g,'&quot;') : ''}" placeholder="ALT / descripción" />
            </div>
          </div>
        `;
      }).join('')}
    </div>`;

    $gi('gal_grid').innerHTML = html;
    // drag & drop en la vista (solo cards con draggable=true)
    wireDragDrop();
    // star / del / flags
    const modal = document.getElementById('modalGaleria');
    modal.querySelectorAll('[data-star]').forEach(el => el.onclick = () => marcarPortada(Number(el.dataset.star)));
    modal.querySelectorAll('[data-del]').forEach(el => el.onclick = () => eliminarImagen(Number(el.dataset.del)));
    modal.querySelectorAll('input[data-flag]').forEach(el => el.onchange = () => onFlagChange(el));
  }

  function wireDragDrop(){
    const cards = Array.from(document.querySelectorAll('.gal-card[draggable="true"]'));
    cards.forEach(card=>{
      card.addEventListener('dragstart', (e)=>{
        GAL_STATE.dragging = Number(card.dataset.gid);
        card.classList.add('dragging');
        e.dataTransfer.effectAllowed = 'move';
      });
      card.addEventListener('dragend', ()=>{
        card.classList.remove('dragging');
        GAL_STATE.dragging = null;
      });
      card.addEventListener('dragover', (e)=>{
        e.preventDefault();
        const grid = $gi('gal_grid_inner');
        const draggingEl = document.querySelector('.gal-card.dragging');
        if(!draggingEl) return;
        const afterElement = getDragAfterElement(grid, e.clientY);
        if(afterElement == null){
          grid.appendChild(draggingEl);
        }else{
          grid.insertBefore(draggingEl, afterElement);
        }
      });
    });

    function getDragAfterElement(container, y){
      const els = [...container.querySelectorAll('.gal-card:not(.dragging)[draggable="true"]')];
      return els.reduce((closest, child)=>{
        const box = child.getBoundingClientRect();
        const offset = y - box.top - box.height / 2;
        if(offset < 0 && offset > closest.offset){
          return { offset, element: child };
        }else{
          return closest;
        }
      }, { offset: Number.NEGATIVE_INFINITY }).element;
    }
  }

  /* ==== Flags / portada / eliminar ==== */

  function onFlagChange(input){
    // cambia en memoria (se persiste con "Guardar flags")
    const gid = Number(input.dataset.gid);
    const item = GAL_STATE.items.find(x=> x.galeria_id === gid);
    if(!item) return;
    if(input.dataset.flag === 'gal') item.para_galeria = input.checked;
    if(input.dataset.flag === 'menu') item.para_menu = input.checked;
    if(input.dataset.flag === 'hab')  item.habilitado = input.checked;
  }

  async function guardarFlags(){
    const payload = GAL_STATE.items.map(x => ({
      galeria_id: x.galeria_id,
      para_galeria: !!x.para_galeria,
      para_menu:    !!x.para_menu,
      habilitado:   (x.habilitado !== false),
      descripcion:  $(`input[data-alt="${x.galeria_id}"]`)?.value || null
    }));
    const res = await fetch(GAL_API.flags, {
      method:'PATCH', headers:{'Content-Type':'application/json'},
      body: JSON.stringify(payload)
    });
    if(!res.ok){ alert('❌ No se pudieron guardar flags'); return; }
    await loadGaleria();
    alert('✅ Flags/ALT guardados');
  }

  async function marcarPortada(galeriaId){
    const res = await fetch(GAL_API.portada(GAL_STATE.productoId, galeriaId), { method:'POST' });
    if(!res.ok){ alert('❌ No se pudo marcar portada'); return; }
    await loadGaleria();
  }

  async function eliminarImagen(galeriaId){
    if(!confirm('¿Eliminar esta imagen?')) return;
    const res = await fetch(GAL_API.del(galeriaId), { method:'DELETE' });
    if(!res.ok){ alert('❌ No se pudo eliminar'); return; }
    await loadGaleria();
  }

  /* ==== Orden ==== */
  async function guardarOrdenVista(){
    // lee el orden actual de tarjetas visibles (draggable=true)
    const ids = [...document.querySelectorAll('.gal-card[draggable="true"]')].map(el => Number(el.dataset.gid));
    const items = ids.map((id, i) => ({ galeriaId: id, posicion: i+1 }));
    const payload = { vista: GAL_STATE.vista, items };

    const res = await fetch(GAL_API.reorder(GAL_STATE.productoId), {
      method:'POST', headers:{'Content-Type':'application/json'},
      body: JSON.stringify(payload)
    });
    if(!res.ok){ alert('❌ No se pudo guardar el orden'); return; }
    await loadGaleria();
    alert('✅ Orden guardado');
  }

  /* ==== Subida de nuevas imágenes ==== */

  function onGalFilesChange(e){
    const files = Array.from(e.target.files||[]);
    GAL_STATE.selectedFiles = files.map(f => ({ file: f, para_menu: $gi('gal_new_menu').checked, descripcion: '' }));
    $gi('gal_sel_info').textContent = `${files.length} archivo(s) seleccionado(s)`;
    $gi('gal_btnUpload').disabled = files.length === 0;
  }

  async function subirSeleccionadas(){
    if(GAL_STATE.selectedFiles.length === 0) return;
    // validaciones: tipo/tamaño
    const allowed = ['image/jpeg','image/png','image/webp'];
    for(const it of GAL_STATE.selectedFiles){
      const f = it.file;
      if(!allowed.includes(f.type)){ alert(`Tipo no permitido: ${f.name} (${f.type})`); return; }
      if(f.size > 10*1024*1024){ alert(`Archivo supera 10MB: ${f.name}`); return; }
    }

    // límite 20 total: consulta conteo actual
    const totalActual = GAL_STATE.items.length;
    if(totalActual + GAL_STATE.selectedFiles.length > 20){
      alert(`Límite 20 imágenes por producto. Ya tienes ${totalActual}.`);
      return;
    }

    // lee como base64 (con prefijo data:)
  const toB64 = (file)=> new Promise((resolve,reject)=>{
    const r = new FileReader();
    r.onload = ()=> resolve(String(r.result));  // data:mime;base64,XXXX
    r.onerror = reject;
    r.readAsDataURL(file);
  });

  const payload = [];
  for (const it of GAL_STATE.selectedFiles) {
    const dataURL = await toB64(it.file);
    const clean   = dataURL.includes(',') ? dataURL.split(',')[1] : dataURL; // <-- limpio

    payload.push({
      descripcion: it.descripcion || null,
      mime_type: it.file.type || 'application/octet-stream',
      // contenido_base64: clean,             // <-- enviar limpio
      bytes_b64: clean,
      para_galeria: true,
      // para_menu: !!it.para_menu,
      para_menu: !!it.para_menu,
      habilitado: true
    });
  }


    const res = await fetch(GAL_API.upload(GAL_STATE.productoId), {
      method:'POST', headers:{'Content-Type':'application/json'},
      body: JSON.stringify(payload)
    });
    if(!res.ok){
      const txt = await res.text().catch(()=> '');
      alert('❌ Error al subir: ' + (txt || ('HTTP '+res.status)));
      return;
    }
    // limpiar selección y recargar
    $gi('gal_files').value = '';
    $gi('gal_sel_info').textContent = 'Ningún archivo seleccionado';
    GAL_STATE.selectedFiles = [];
    $gi('gal_btnUpload').disabled = true;
    await loadGaleria();
    alert('✅ Imágenes subidas');
  }
//cerrar sesion
  async function logout() {
    try {
      sessionStorage.removeItem('user');
      localStorage.removeItem('user');
      sessionStorage.removeItem('token');
      localStorage.removeItem('token');

      await fetch('/api/auth/logout', { method: 'POST' }).catch(() => {});
    } finally {
      location.replace('Login.html');
    }
  }

  // ===== Guard de ADMIN + nombre en el header =====
  document.addEventListener('DOMContentLoaded', () => {
    const raw = sessionStorage.getItem('user') || localStorage.getItem('user');
    if (!raw) { location.replace('Login.html?next=' + encodeURIComponent('cuenta%20-%20admin.html')); return; }
    let u; try { u = JSON.parse(raw); } catch { u = null; }
    const id = parseInt(u?.id_rol ?? u?.idRol ?? u?.rol_id ?? 0, 10);
    const nm = String(u?.rol ?? u?.role ?? u?.roleName ?? '').toLowerCase();
    const isAdmin = id === 1 || nm === 'admin';
    if (!isAdmin) { location.replace('cuenta.html'); return; }
    const tag = document.querySelector('.user-info');
    if (tag) tag.textContent = '👤 Administrador: ' + (u?.nombre || u?.usuario || 'Admin');
  });

  // ===== Navegación lateral =====
  // function showSection(sectionId, el){
  //   document.querySelectorAll('.admin-section').forEach(s => s.classList.remove('active'));
  //   const sec = document.getElementById(sectionId); if (sec) sec.classList.add('active');
  //   document.querySelectorAll('.nav-item').forEach(i => i.classList.remove('active'));
  //   if (el) el.classList.add('active');
  // }

  // ===== UX botones demo =====
  document.addEventListener('DOMContentLoaded', function(){
    document.querySelectorAll('.btn').forEach(btn=>{
      btn.addEventListener('click', function(){
        const action = this.textContent.trim();
        if(action.match(/Agregar|Actualizar|Guardar/)){
          this.style.backgroundColor = '#4caf50'; this.textContent = '✓ Completado';
          setTimeout(()=>{ this.style.backgroundColor=''; this.textContent = action; }, 2000);
        } else if(action.match(/Eliminar|Bloquear/)){
          if(confirm('¿Estás seguro de realizar esta acción?')){
            this.style.backgroundColor = '#ff5722'; this.textContent = '✓ Realizado';
            setTimeout(()=>{ this.style.backgroundColor=''; this.textContent = action; }, 2000);
          }
        }
      });
    });
  });

  // ===== Crear usuario (Cliente usa SP crear_cliente; otros roles requieren endpoint propio) =====
  async function crearUsuario(){
    const payload = {
      nombre:     document.getElementById('v_nombre').value.trim(),
      cedula:     document.getElementById('v_cedula').value.trim(),
      correo:     document.getElementById('v_correo').value.trim(),
      telefono:   document.getElementById('v_telefono').value.trim(),
      usuario:    document.getElementById('v_usuario').value.trim()
    };
    const idRol = parseInt(document.getElementById('v_id_rol').value, 10);

    try {
      let url, body;
      if (idRol === 2){
        // Cliente: usa el endpoint existente que invoca SP crear_cliente
        url  = '/api/usuarios/crear';
        body = { ...payload, idMetodoPago: null };
      } else {
        // Admin/Trabajador: requiere un endpoint del backend para roles ≠ cliente
        url  = '/api/usuarios/crear-usuario';
        body = { ...payload, idRol };
      }

      const res = await fetch(url, { method:'POST', headers:{'Content-Type':'application/json'}, body: JSON.stringify(body) });
      const text = await res.text();
      if (!res.ok) throw new Error(text || ('HTTP ' + res.status));
      alert('✅ ' + (text || 'Operación realizada'));
    } catch (err) {
      alert('❌ Error: ' + (err.message || err));
    }
  }

  // ===== Productos: listar desde API y pintar tabla =====
  const API_PRODUCTS = '/api/productos';
  const API_PROD_DELETE = (id) => `/api/sp/productos/${id}`;
  const $p = (s) => document.querySelector(s);
  function coalesce(...vals){ for(const v of vals){ if(v!==undefined && v!==null) return v; } return undefined; }
  function fmtUSD(n){ const x = Number(n||0); return new Intl.NumberFormat('es-EC',{style:'currency',currency:'USD'}).format(x); }
  function fmtDate(d){ if(!d) return ''; const dt = new Date(d); return isNaN(dt) ? String(d) : dt.toLocaleDateString(); }
  function renderEmpty(msg){ $p('#tbProductos').innerHTML = `<tr><td colspan="12" class="tt-muted-row">${msg}</td></tr>`; }
  function mapProduct(raw){
    return {
      id:          coalesce(raw.producto_id, raw.productoId, raw.id, raw.id_producto),
      nombre:      coalesce(raw.nombre, raw.name, ''),
      precio:      coalesce(raw.preciounitario, raw.precioUnitario, raw.precio, raw.price, 0),
      enlace:      coalesce(raw.enlace, raw.url, ''),
      fecha:       coalesce(raw.fecha, raw.createdAt, raw.fecha_creacion),
      stock:       coalesce(raw.stock, raw.cantidad, 0),
      marca_id:    coalesce(raw.marca_id, raw.marcaId),
      gama_id:     coalesce(raw.gama_id, raw.gamaId),
      iva_id:      coalesce(raw.iva_id, raw.ivaId),
      costo:       coalesce(raw.costo, 0),
      habilitado:  coalesce(raw.habilitado, raw.activo, true)
    };
  }
  function renderProductos(items){
    if(!Array.isArray(items) || !items.length){ renderEmpty('Sin productos'); return; }
    const rows = items.map(p => {
      const enlaceHtml = p.enlace ? `<a href="${p.enlace}" target="_blank" rel="noopener" class="link--external">link</a>` : '';
      return `<tr>
        <td>${p.id ?? ''}</td>
        <td>${p.nombre ?? ''}</td>
        <td>${fmtUSD(p.precio)}</td>
        <td>${enlaceHtml}</td>
        <td>${fmtDate(p.fecha)}</td>
        <td>${p.stock ?? 0}</td>
        <td>${p.marca_id ?? ''}</td>
        <td>${p.gama_id ?? ''}</td>
        <td>${p.iva_id ?? ''}</td>
        <td>${fmtUSD(p.costo)}</td>
        <td>${p.habilitado ? '<span class="status active">Sí</span>' : '<span class="status blocked">No</span>'}</td>
          <td>
            <button class="btn btn-outline btn--sm" data-gal="${p.id}" data-nombre="${p.nombre ?? ''}">🖼 Galería</button>
            <button class="btn btn-warning btn--sm" data-edit="${p.id}">Editar</button>
            <button class="btn btn-danger btn--sm" data-del="${p.id}" data-nombre="${p.nombre ?? ''}">Eliminar</button>
          </td>
      </tr>`;
    }).join('');
    $p('#tbProductos').innerHTML = rows;

    // wire: abrir modal galería
    document.querySelectorAll('[data-gal]').forEach(btn=>{
      btn.addEventListener('click', ()=> openGaleriaModal(
        Number(btn.dataset.gal),
        btn.dataset.nombre || ''
      ));
    });
  }

  async function loadProductos(){
    try{
      const res = await fetch(API_PRODUCTS);
      if(!res.ok){ renderEmpty('Error cargando productos'); return; }
      const data = await res.json();
      const list = Array.isArray(data) ? data : (data.content || data.items || []);
      renderProductos(list.map(mapProduct));
    }catch(err){ renderEmpty('No se pudo conectar con /api/productos'); }
  }
  document.addEventListener('DOMContentLoaded', ()=>{ try{ loadProductos(); } catch(_){} });


 /* ===== Eliminar PRODUCTO ===== */
    document.addEventListener('click', async (ev) => {
      // Solo clicks en el botón dentro de la tabla de productos
      const btn = ev.target.closest('#tbProductos button[data-del]');
      if (!btn) return;

      ev.preventDefault(); ev.stopPropagation();

      const id = Number(btn.dataset.del);
      const nombre = btn.dataset.nombre || `#${id}`;
      if (!id) return;

      if (!confirm(`¿Eliminar el producto “${nombre}” (ID ${id})?`)) return;

      const usuario = (typeof getLoggedUsername === 'function') ? getLoggedUsername() : null;

      try {
        const res = await fetch(API_PROD_DELETE(id), {
          method: 'DELETE',
          headers: {
            'Content-Type': 'application/json',
            'X-Usuario': usuario || '',
            ...(typeof authHeaders === 'function' ? authHeaders() : {})
          }
        });
        const txt = await res.text();
        if (!res.ok) throw new Error(txt || ('HTTP ' + res.status));

        await loadProductos?.();
        alert('✅ Producto eliminado o deshabilitado (según restricciones del SP).');
      } catch (e) {
        alert('❌ No se pudo eliminar: ' + (e.message || e));
      }
    });


    document.addEventListener('DOMContentLoaded', ()=>{ try{ loadProductos(); } catch(_){} });
      // wire: eliminar producto
          document.querySelectorAll('[data-del]').forEach(btn => {
            btn.addEventListener('click', async () => {
              const id = Number(btn.dataset.del);
              const nombre = btn.dataset.nombre || `#${id}`;
              if (!id) return;
              if (!confirm(`¿Eliminar el producto “${nombre}” (ID ${id})?`)) return;

              const usuario = (typeof getLoggedUsername === 'function') ? getLoggedUsername() : null;

              try {
                const res = await fetch(API_PROD_DELETE(id), {
                  method: 'DELETE',
                  headers: {
                    'Content-Type': 'application/json',
                    'X-Usuario': usuario || '',
                    ...(typeof authHeaders === 'function' ? authHeaders() : {}) // Authorization si usas JWT
                  }
                });
                const txt = await res.text();
                if (!res.ok) throw new Error(txt || ('HTTP ' + res.status));

                await loadProductos(); // refresca la tabla
                alert('✅ Producto eliminado o deshabilitado (según restricciones).');
              } catch (e) {
                alert('❌ No se pudo eliminar: ' + (e.message || e));
              }
            });
          });

// ===== MODAL: lógica mínima =====
const CATEGORIES = [
  'Fuente de poder','GPU','Motherboard','CPU','RAM','CPU COOLER',
  'Almacenamiento','Cubierta','Periféricos','Accesorios'
];

const BRANDS = [
  'Intel','AMD','HP','Samsung','Apple','Sony','Corsair','EVGA',
  'ASUS','MSI','Kingston','Seagate','Western Digital','Thermalright'
];

// Campos específicos por categoría (nombres alineados al SP v2)
const CATEGORY_FIELDS = {
  'Fuente de poder': [
    { name:'consumo_energia', label:'Consumo de energía (W)', type:'number' }
  ],
  'GPU': [
    { name:'tamanio', label:'Tamaño (mm)', type:'number' },
    { name:'consumo_energia', label:'Consumo de energía (W)', type:'number' }
  ],
  'Motherboard': [
    { name:'socket', label:'Socket', type:'text' },
    { name:'velocidad_ram', label:'Velocidad RAM (MHz)', type:'number' },
    { name:'chipset', label:'Chipset', type:'text' }
  ],
  'CPU': [
    { name:'sockets', label:'Sockets', type:'text' },
    { name:'generacion', label:'Generación', type:'number' }
  ],
  'RAM': [
    { name:'velocidades', label:'Velocidades (MHz)', type:'number' }
  ],
  'CPU COOLER': [
    { name:'tamanio', label:'Tamaño (mm)', type:'number' },
    { name:'socket',  label:'Socket', type:'text' }
  ],
  'Almacenamiento': [
    // Se renderiza aparte (capacidad + unidad + tipo)
  ],
  'Cubierta': [
    { name:'tamanio_gpu', label:'Tamaño de GPU (mm)', type:'number' },
    { name:'tamanio_refrigeracion', label:'Tamaño de refrigeración (mm)', type:'number' }
  ],
  'Periféricos': [
    { name:'tipo', label:'Tipo', type:'text' }
  ],
  'Accesorios': [
    // sin campos extra
  ]
};

/* Endpoints sugeridos (ajústalos a los que tengas en backend).
   Ya tienes: /api/sp/almacenamientos, /api/sp/cpu
   Agregamos el resto siguiendo el mismo patrón. */
const CATEGORY_ENDPOINT = {
  'Almacenamiento':  '/api/sp/almacenamientos',
  'CPU':             '/api/sp/cpu',
  'CPU COOLER':      '/api/sp/cpu-cooler',
  'Fuente de poder': '/api/sp/fuentes',
  'GPU':             '/api/sp/gpu',
  'Motherboard':     '/api/sp/motherboards',
  'RAM':             '/api/sp/ram',
  'Cubierta':        '/api/sp/cubiertas',
  'Periféricos':     '/api/sp/perifericos',
  'Accesorios':      '/api/sp/accesorios'
};

// IDs de ejemplo para demo (idealmente, poblar desde API)
const BRAND_ID = {
  'Intel': 1, 'AMD': 2, 'HP': 3, 'Samsung': 4, 'Apple': 5, 'Sony': 6,
  'Corsair': 7, 'EVGA': 8, 'ASUS': 9, 'MSI': 10, 'Kingston': 11,
  'Seagate': 12, 'Western Digital': 13, 'Thermalright': 14
};
const DEFAULT_GAMA_ID = 1;
const DEFAULT_IVA_ID  = 1;

// Helpers
const $ = (s) => document.querySelector(s);
function fillSelectOptions(sel, arr){
  sel.innerHTML = '<option value="">Seleccionar</option>' +
    arr.map(v=>`<option value="${v}">${v}</option>`).join('');
}
function getExtra(name){
  return document.querySelector(`#np_fields [data-extra="${name}"]`)?.value?.trim() ?? '';
}
function collectExtras(cat){
  const defs = CATEGORY_FIELDS[cat] || [];
  const out = {};
  defs.forEach(d=>{
    const el = document.querySelector(`#np_fields [data-extra="${d.name}"]`);
    if(!el) return;
    let val = el.value.trim();
    if (val === '') return;
    out[d.name] = d.type === 'number' ? Number(val) : val;
  });
  return out;
}
function renderDynamicFields(cat){
  const box = $('#np_fields');
  const title = $('#np_fields_title');
  const empty = $('#np_fields_empty');
  box.innerHTML = ''; title.innerHTML = ''; empty.style.display = 'flex';

  if(!cat){ return; }
  title.innerHTML = `<h4>${cat}</h4>`;
  empty.style.display = 'none';

  // Caso especial: Almacenamiento (capacidad + unidad + tipo)
  if (cat === 'Almacenamiento') {
    const g1 = document.createElement('div');
    g1.className = 'group';
    g1.innerHTML = `
      <label>Capacidad</label>
      <div class="input-unit">
        <input class="control" type="number" min="0" step="1"
               data-extra="capacidad" placeholder="Ingrese capacidad" />
        <select class="control control--unit" id="np_unidad_capacidad"
                data-extra="capacidad_unidad" aria-label="Unidad">
          <option value="GB" selected>GB</option>
          <option value="TB">TB</option>
        </select>
      </div>`;
    box.appendChild(g1);

    const g2 = document.createElement('div');
    g2.className = 'group';
    g2.innerHTML = `<label>Tipo</label>
                    <input class="control" data-extra="tipo" placeholder="Ingrese tipo (p.ej. NVMe, SATA, HDD)"/>`;
    box.appendChild(g2);
    return;
  }

  // Render genérico
  const defs = CATEGORY_FIELDS[cat] || [];
  defs.forEach(f=>{
    const wrap = document.createElement('div');
    wrap.className = 'group';
    const inputType = f.type === 'number' ? 'number' : 'text';
    const stepAttr  = f.type === 'number' ? ' step="1" ' : '';
    wrap.innerHTML = `<label>${f.label}</label>
                      <input class="control" ${stepAttr} type="${inputType}"
                             data-extra="${f.name}" placeholder="Ingrese ${f.label.toLowerCase()}"/>`;
    box.appendChild(wrap);
  });
}
function clearDynamicFields(){ $('#np_fields').innerHTML=''; $('#np_fields_title').innerHTML=''; $('#np_fields_empty').style.display='flex'; }
function updateImagesInfo(){
  const files = $('#np_imgs').files;
  $('#np_imgs_info').textContent = (files && files.length)? `${files.length} archivo(s) seleccionado(s)` : 'Ningún archivo seleccionado';
}

// Poblado inicial del modal y eventos
document.addEventListener('DOMContentLoaded', () => {
  fillSelectOptions($('#np_categoria'), CATEGORIES);
  fillSelectOptions($('#np_marca'), BRANDS);
  $('#btnAgregarProducto').addEventListener('click', ()=>{ $('#modalNuevoProducto').hidden = false; $('#np_nombre').focus(); });
  $('#btnCerrarModal').addEventListener('click', ()=>{ $('#modalNuevoProducto').hidden = true; clearDynamicFields(); $('#formNuevoProducto').reset(); updateImagesInfo(); });
  $('#btnCancelarModal').addEventListener('click', ()=>{ $('#modalNuevoProducto').hidden = true; clearDynamicFields(); $('#formNuevoProducto').reset(); updateImagesInfo(); });
  const backdrop = document.getElementById('modalNuevoProducto');
  backdrop.addEventListener('click', (e)=>{ if(e.target === backdrop){ $('#modalNuevoProducto').hidden = true; clearDynamicFields(); $('#formNuevoProducto').reset(); updateImagesInfo(); } });
  document.addEventListener('keydown', (e)=>{ if(e.key==='Escape' && !backdrop.hidden){ $('#modalNuevoProducto').hidden = true; clearDynamicFields(); $('#formNuevoProducto').reset(); updateImagesInfo(); } });
  $('#np_categoria').addEventListener('change', (e)=> renderDynamicFields(e.target.value));
  $('#btnSelImgs').addEventListener('click', ()=> $('#np_imgs').click());
  $('#np_imgs').addEventListener('change', updateImagesInfo);

  // Submit: ahora soporta TODAS las categorías
  $('#formNuevoProducto').addEventListener('submit', async (e)=>{
    e.preventDefault();

    const categoria = $('#np_categoria').value;
    const nombre    = $('#np_nombre').value.trim();
    const enlace    = $('#np_enlace').value.trim() || null;
    const precio    = Number(document.querySelector('#np_precio')?.value ?? 0);
    const costo     = Number(document.querySelector('#np_costo')?.value ?? 0);
    const stock     = parseInt(document.querySelector('#np_stock')?.value ?? '0', 10) || 0;
    const marcaTxt  = $('#np_marca').value;
    const marca_id  = BRAND_ID[marcaTxt] ?? 1;

    if (!nombre || !categoria || !marcaTxt) {
      alert('Completa los campos obligatorios');
      return;
    }

    const base = {
      nombre,
      preciounitario: precio,
      enlace,
      stock,
      marca_id,
      gama_id: DEFAULT_GAMA_ID,
      iva_id:  DEFAULT_IVA_ID,
      costo
    };

    // Construir payload específico
    let payload = { ...base };
    if (categoria === 'Almacenamiento') {
      const valor = Number(getExtra('capacidad')) || 0;
      const unidad = document.getElementById('np_unidad_capacidad')?.value || 'GB';
      // El backend normaliza 1000GB -> 1TB; aquí solo enviamos en GB para mantener tu flujo actual
      const capacidadGB = unidad === 'TB' ? (valor * 1000) : valor;
      payload = { ...payload, capacidad: capacidadGB, tipo: (getExtra('tipo') || 'SSD') };
    } else {
      payload = { ...payload, ...collectExtras(categoria) };
    }

    const url = CATEGORY_ENDPOINT[categoria];
    if (!url) {
      alert('No hay endpoint configurado para esta categoría.');
      return;
    }
     console.log('[NP] POST', url, 'payload →', payload);

  try {
    // 1) Crear producto
    const res = await fetch(url, {
    method: 'POST',
    headers: {
    'Content-Type': 'application/json',
    'X-Usuario': getLoggedUsername() || ''   // <<<<<< AÑADIDO
     },
      body: JSON.stringify(payload)
    });
    const raw = await res.text();
    if (!res.ok) throw new Error(raw || ('HTTP ' + res.status));

    // 1.1) obtener productoId de la respuesta (más robusto)
    let productoId = null;

    // intenta por content-type JSON
    const ct = (res.headers.get('content-type') || '').toLowerCase();
    if (ct.includes('application/json')) {
      try {
        const j = JSON.parse(raw);
        productoId =
          j.productoId ?? j.producto_id ?? j.id ?? j.id_producto ?? j.productID ?? null;
      } catch (_) {}
    }

    // intenta por Location header (REST 201 Created)
    if (!productoId) {
      const loc = res.headers.get('Location') || res.headers.get('location');
      if (loc) {
        const m = String(loc).match(/\/(\d+)(?:\?.*)?$/);
        if (m) productoId = Number(m[1]);
      }
    }

    // intenta por texto plano con dígitos
    if (!productoId) {
      const m = String(raw).match(/(\d{1,})/);
      if (m) productoId = Number(m[1]);
    }

    if (!productoId) {
      console.debug('Respuesta creación producto:', { status: res.status, headers: Object.fromEntries(res.headers.entries()), raw });
      alert('✅ Producto creado, pero no pude leer el ID (no vino en JSON/Location). Revisa Network → Response/Headers.');
      return;
    }

    // 2) Subir imágenes seleccionadas en el campo #np_imgs (opcional)
    const files = Array.from($('#np_imgs').files || []);
    if (files.length) {
      if (files.length > 15) { alert('Máximo 15 imágenes'); return; }
      const big = files.find(f => f.size > 10 * 1024 * 1024);
      if (big) { alert(`"${big.name}" supera 10MB`); return; }

      const toDataUrl = f => new Promise((ok, ko) => {
        const r = new FileReader();
        r.onload = () => ok(String(r.result)); // data:mime;base64,XXXXX
        r.onerror = ko;
        r.readAsDataURL(f);
      });
      const cleanB64 = s => s.includes(',') ? s.split(',')[1] : s; // quita prefijo data:

      const items = [];
      for (const f of files) {
        const dataUrl = await toDataUrl(f);
        items.push({
          descripcion: f.name,
          mime_type: f.type || 'application/octet-stream',
          bytes_b64: cleanB64(dataUrl),
          para_galeria: true,
          para_menu: false,
          habilitado: true
        });
      }

      const up = await fetch(`/api/productos/${productoId}/galeria`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(items)
      });
      const upTxt = await up.text();
      if (!up.ok) throw new Error(upTxt || ('HTTP ' + up.status));
    }

    alert('✅ Producto y galería guardados');
    $('#modalNuevoProducto').hidden = true;
    clearDynamicFields(); $('#formNuevoProducto').reset(); updateImagesInfo();
    loadProductos();
  } catch (err) {
    alert('❌ Error: ' + (err.message || err));
  }

  });
});

// ===== Ciudades (usa /api/ciudades y necesita provincias para selects) =====
    const API_CIUDADES = '/api/ciudades';
    const $C = (id) => document.getElementById(id);

    let CIUDADES = [];            // [{ id, nombre, provincia_id }]
    let PROVINCIAS_CACHE = [];    // [{ id, nombre }], la llenamos desde provincias.js o con GET

    // Helpers
    async function httpJsonC(url, opts = {}) {
      const res = await fetch(url, { headers: { 'Content-Type': 'application/json' }, ...opts });
      const txt = await res.text();
      const data = txt ? JSON.parse(txt) : null;
      if (!res.ok) throw new Error(data?.message || txt || ('HTTP ' + res.status));
      return data;
    }
    function showBoxC(el){ if (el) el.style.removeProperty('display'); }
    function hideBoxC(el){ if (el) el.style.display = 'none'; }
    function toIntC(v){ const n = parseInt(v,10); return isNaN(n) ? null : n; }

    // Map
    function mapCiudad(dto){
      return {
        id:           dto.ciudadId ?? dto.id ?? dto.ciudad_id,
        nombre:       dto.nombre ?? dto.name,
        provincia_id: dto.provinciaId ?? dto.provincia_id ?? dto.id_provincia
      };
    }
    function provinciaNombre(id){
      const p = PROVINCIAS_CACHE.find(x => String(x.id) === String(id));
      return p?.nombre || '';
    }

    // Provincias para selects de ciudad
    function renderProvinciasEnCiudad(){
      const opts = ['<option value="">Seleccionar provincia</option>']
              .concat(PROVINCIAS_CACHE.map(p => `<option value="${p.id}">${p.nombre}</option>`))
              .join('');
      const selAlta = $C('ciudad_provincia');
      const selNueva = $C('ciudad_nueva_provincia');
      if (selAlta)  selAlta.innerHTML = opts;
      if (selNueva) selNueva.innerHTML = ['<option value="">Nueva provincia</option>'].concat(
              PROVINCIAS_CACHE.map(p => `<option value="${p.id}">${p.nombre}</option>`)).join('');
    }

    // Render ciudades (select de edición)
    function renderSelectCiudades(){
      const sel = $C('select_ciudad');
      if (!sel) return;
      const opts = ['<option value="">Seleccionar ciudad</option>']
              .concat(CIUDADES.map(c => {
                const tagProv = provinciaNombre(c.provincia_id);
                return `<option value="${c.id}" data-prov="${c.provincia_id}">${c.nombre}${tagProv ? ' — '+tagProv : ''}</option>`;
              })).join('');
      sel.innerHTML = opts;
      sel.onchange = manejarSeleccionCiudad;
    }

    // Carga ciudades
    async function loadCiudades(){
      try{
        const data = await httpJsonC(API_CIUDADES, { method: 'GET' });
        CIUDADES = (Array.isArray(data) ? data : []).map(mapCiudad).filter(x => x.id != null);
      }catch(e){
        console.error('loadCiudades:', e);
        CIUDADES = [];
      }finally{
        renderSelectCiudades();
      }
    }

    // Panel de edición ciudad
    function manejarSeleccionCiudad(){
      const sel = $C('select_ciudad');
      const box = $C('ciudad_acciones');
      const id  = sel?.value || '';
      if (!id) { hideBoxC(box); return; }
      const c = CIUDADES.find(x => String(x.id) === String(id));
      if ($C('ciudad_nuevo_nombre'))     $C('ciudad_nuevo_nombre').value = c?.nombre || '';
      if ($C('ciudad_nueva_provincia'))  $C('ciudad_nueva_provincia').value = c?.provincia_id || '';
      showBoxC(box);
    }

    // === CRUD ===

    // Crear ciudad -> POST /api/ciudades
    async function agregarCiudad(){
      const provinciaId = toIntC($C('ciudad_provincia')?.value);   // <-- nombre camelCase
      const nombre = ($C('ciudad_nombre')?.value || '').trim();

      if (!provinciaId){ alert('Selecciona una provincia'); return; }
      if (!nombre){ alert('Ingresa el nombre de la ciudad'); return; }

      const payload = { nombre, provinciaId };                    // <-- clave correcta

      await httpJsonC('/api/ciudades', {
        method: 'POST',
        headers: { 'Content-Type':'application/json' },
        body: JSON.stringify(payload)
      });

      $C('ciudad_nombre').value = '';
      $C('ciudad_provincia').value = '';
      await loadCiudades();
      alert('✅ Ciudad agregada');
    }

    // Editar ciudad -> PUT /api/ciudades/{id}
    async function editarCiudad(){
      const id = toIntC($C('select_ciudad')?.value);
      if(!id){ alert('Selecciona una ciudad'); return; }

      const nombre = ($C('ciudad_nuevo_nombre')?.value || '').trim();
      const provincia_id = toIntC($C('ciudad_nueva_provincia')?.value);
      if (!provincia_id){ alert('Selecciona la nueva provincia'); return; } // <-- obligatorio

      await httpJsonC(`${API_CIUDADES}/${id}`, {
        method:'PUT',
        body: JSON.stringify({ nombre: nombre || undefined, provinciaId: provincia_id })
      });

      await loadCiudades();
      hideBoxC($C('ciudad_acciones'));
      if ($C('select_ciudad')) $C('select_ciudad').value = '';
      alert('✅ Ciudad actualizada');
    }

    // Eliminar ciudad -> DELETE /api/ciudades/{id}
    async function eliminarCiudad(){
      const id = toIntC($C('select_ciudad')?.value);
      if(!id){ alert('Selecciona una ciudad'); return; }
      if(!confirm('¿Eliminar la ciudad seleccionada?')) return;
      try{
        await httpJsonC(`${API_CIUDADES}/${id}`, { method:'DELETE' });
        await loadCiudades();
        hideBoxC($C('ciudad_acciones'));
        if ($C('select_ciudad')) $C('select_ciudad').value = '';
        alert('🗑️ Ciudad eliminada');
      }catch(e){ alert('❌ ' + e.message); }
    }

    // Escucha la actualización de provincias desde provincias.js
    window.addEventListener('tt:provincias-updated', (ev) => {
      PROVINCIAS_CACHE = Array.isArray(ev.detail) ? ev.detail : [];
      renderProvinciasEnCiudad();
    });

    // Si por alguna razón provincias no cargó aún, intenta traerlas una vez
    document.addEventListener('DOMContentLoaded', async () => {
      if (PROVINCIAS_CACHE.length === 0) {
        try{
          const res = await fetch('/api/provincias');
          const data = await res.json();
          PROVINCIAS_CACHE = (Array.isArray(data) ? data : [])
                  .map(d => ({ id: d.provinciaId ?? d.id ?? d.provincia_id, nombre: d.nombre }))
                  .filter(x => x.id != null);
          renderProvinciasEnCiudad();
        }catch(e){ console.warn('No se pudieron precargar provincias desde ciudades:', e); }
      }
      await loadCiudades();
    });

    // Exponer globales para onclick="..."
    window.agregarCiudad   = agregarCiudad;
    window.editarCiudad    = editarCiudad;
    window.eliminarCiudad  = eliminarCiudad;
    window.manejarSeleccionCiudad = manejarSeleccionCiudad;

const API_PROVINCIAS = '/api/provincias';
    const $P = (id) => document.getElementById(id);

    let PROVINCIAS = []; // [{ id, nombre }]

    // Helpers
    async function httpJsonP(url, opts = {}) {
      const res = await fetch(url, { headers: { 'Content-Type': 'application/json' }, ...opts });
      const txt = await res.text();
      const data = txt ? JSON.parse(txt) : null;
      if (!res.ok) throw new Error(data?.message || txt || ('HTTP ' + res.status));
      return data;
    }
    function showBoxP(el){ if (el) el.style.removeProperty('display'); }
    function hideBoxP(el){ if (el) el.style.display = 'none'; }

    // Mapeo DTO -> modelo de UI
    function mapProvincia(dto){ return { id: dto.provinciaId ?? dto.id ?? dto.provincia_id, nombre: dto.nombre }; }

    // Render de selects donde aparecen provincias
    function renderSelectProvincias(){
      const opts = ['<option value="">Seleccionar provincia</option>']
              .concat(PROVINCIAS.map(p => `<option value="${p.id}">${p.nombre}</option>`))
              .join('');

      const selEditar    = $P('select_provincia');
      const selAltaCiu   = $P('ciudad_provincia');
      const selNuevaProv = $P('ciudad_nueva_provincia');

      if (selEditar)    { selEditar.innerHTML = opts; selEditar.onchange = manejarSeleccionProvincia; }
      if (selAltaCiu)   { selAltaCiu.innerHTML = opts; }
      if (selNuevaProv) {
        selNuevaProv.innerHTML = ['<option value="">Nueva provincia</option>']
                .concat(PROVINCIAS.map(p => `<option value="${p.id}">${p.nombre}</option>`)).join('');
      }

      // Notifica a otros scripts (ciudades.js) que hay provincias listas
      window.dispatchEvent(new CustomEvent('tt:provincias-updated', { detail: PROVINCIAS }));
    }

    // Cargar lista
    async function loadProvincias(){
      try{
        const data = await httpJsonP(API_PROVINCIAS, { method: 'GET' });
        PROVINCIAS = (Array.isArray(data) ? data : []).map(mapProvincia).filter(x => x.id != null);
      }catch(e){
        console.error('loadProvincias:', e);
        PROVINCIAS = [];
      }finally{
        renderSelectProvincias();
      }
    }

    // Mostrar/ocultar panel de edición
    function manejarSeleccionProvincia(){
      const sel = $P('select_provincia');
      const box = $P('provincia_acciones');
      const id  = sel?.value || '';
      if (!id) { hideBoxP(box); return; }
      const p = PROVINCIAS.find(x => String(x.id) === String(id));
      if ($P('provincia_nuevo_nombre')) $P('provincia_nuevo_nombre').value = p?.nombre || '';
      showBoxP(box);
    }

    // Utilidad para obtener ID actual
    function getProvinciaId(){
      const raw = ($P('select_provincia')?.value || $P('provincia_id')?.value || '').trim();
      const n = parseInt(raw, 10);
      return isNaN(n) ? null : n;
    }

    // === CRUD ===

    // Crear -> POST /api/provincias
    async function agregarProvincia(){
      const nombre = ($P('provincia_nombre')?.value || '').trim();
      if(!nombre){ alert('Ingresa el nombre'); return; }
      try{
        await httpJsonP(API_PROVINCIAS, { method:'POST', body: JSON.stringify({ nombre }) });
        if ($P('provincia_nombre')) $P('provincia_nombre').value = '';
        await loadProvincias();
        alert('✅ Provincia agregada');
      }catch(e){ alert('❌ ' + e.message); }
    }

    // Editar -> PUT /api/provincias/{id}
    async function editarProvincia(){
      const id = getProvinciaId();
      if(!id){ alert('Selecciona la provincia'); return; }
      const nombre = ($P('provincia_nuevo_nombre')?.value || '').trim(); // puede ir vacío
      try{
        await httpJsonP(`${API_PROVINCIAS}/${id}`, { method:'PUT', body: JSON.stringify({ nombre }) });
        hideBoxP($P('provincia_acciones'));
        if ($P('select_provincia')) $P('select_provincia').value = '';
        await loadProvincias();
        alert('✅ Provincia actualizada');
      }catch(e){ alert('❌ ' + e.message); }
    }

    // Eliminar -> DELETE /api/provincias/{id}
    async function eliminarProvincia(){
      const id = getProvinciaId();
      if(!id){ alert('Selecciona la provincia'); return; }
      if(!confirm('¿Eliminar/inhabilitar la provincia?')) return;
      try{
        await httpJsonP(`${API_PROVINCIAS}/${id}`, { method:'DELETE' });
        hideBoxP($P('provincia_acciones'));
        if ($P('select_provincia')) $P('select_provincia').value = '';
        await loadProvincias();
        alert('🗑️ Provincia eliminada');
      }catch(e){ alert('❌ ' + e.message); }
    }

    // init
    document.addEventListener('DOMContentLoaded', loadProvincias);

    // Exponer funciones globales (porque usas onclick="...")
    window.agregarProvincia   = agregarProvincia;
    window.editarProvincia    = editarProvincia;
    window.eliminarProvincia  = eliminarProvincia;
    window.manejarSeleccionProvincia = manejarSeleccionProvincia;


   //MOVIMIENTO <INVENTARIO>

  //  document.getElementById('btnAgregarMovimiento').addEventListener('click', () => {
  // document.getElementById('ventanaMovimiento').style.display = 'block';
  //   });

  // Abrir modal
  document.getElementById('btnAgregarMovimiento').addEventListener('click', () => {
    document.getElementById('movBackdrop').hidden = false;
    document.body.classList.add('tt-no-scroll');
    // foco inicial
    const first = document.getElementById('mov_producto_id');
    if (first) first.focus();
  });

  // Cerrar solo con la X
  document.getElementById('movCloseBtn').addEventListener('click', () => {
    document.getElementById('movBackdrop').hidden = true;
    document.body.classList.remove('tt-no-scroll');
  });

  // Importante: NO cerrar al hacer clic fuera.
  // El backdrop captura clics, pero no hay listener que cierre el modal.

  // (Opcional) Bloquear Escape para que tampoco cierre
  document.addEventListener('keydown', (ev) => {
    if (!document.getElementById('movBackdrop').hidden && ev.key === 'Escape') {
      ev.preventDefault();
    }
  });

 const f = document.getElementById('mov_fecha');
  if (f) {
    const d = new Date();
    const yyyy = d.getFullYear();
    const mm = String(d.getMonth() + 1).padStart(2, '0');
    const dd = String(d.getDate()).padStart(2, '0');
    f.value = `${yyyy}-${mm}-${dd}`;
    f.max = f.value; // opcional: no permitir fechas futuras
  }
(() => {
    'use strict';

    // Helpers
    const $ = (id) => document.getElementById(id);
    const digits = (s) => (s || '').replace(/\D+/g, '');
    const isEmail = (s) => /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(s);

    // Endpoints del backend
    const API = {
      buscarMin: (q, rolId, limit = 50) => {
        const p = new URLSearchParams();
        p.set('q', q ?? '');                  // <-- siempre manda q
        if (rolId) p.set('rolId', rolId);
        p.set('limit', String(limit));
        return '/api/usuarios/buscar-min?' + p.toString();
      },
      crearAdmin: '/api/usuarios/crear-usuarioAdmin', // como en Arreglado
    };


    async function httpJson(url, opts = {}) {
      const res = await fetch(url, { headers:{'Content-Type':'application/json'}, ...opts });
      const txt = await res.text();
      let data = null; try { data = txt ? JSON.parse(txt) : null; } catch {}
      if (!res.ok) throw new Error((data && (data.message||data.error)) || txt || `HTTP ${res.status}`);
      return data ?? {};
    }

    const mapUser = (dto) => ({
      id: dto.usuarioId ?? dto.id ?? dto.user_id ?? dto.userId,
      usuario: dto.usuario ?? dto.username ?? dto.user ?? '',
      correo: dto.correo ?? dto.email ?? '',
      rolId: dto.rolId ?? dto.idRol ?? dto.id_rol ?? null,
      nombre: dto.nombre ?? '',
      cedula: dto.cedula ?? '',
      telefono: dto.telefono ?? '',
      estado: (dto.habilitado ?? dto.activo ?? dto.estado ?? true) ? 'Activo' : 'Inactivo'
    });

    // Estado
    let BASE = [];      // último lote recibido
    let SELECTED = null;
    let debounceId = null;

    function setForm(u){
      SELECTED = u || null;
      $('ua_nombre').value   = u?.nombre   ?? '';
      $('ua_cedula').value   = u?.cedula   ?? '';
      $('ua_correo').value   = u?.correo   ?? '';
      $('ua_telefono').value = u?.telefono ?? '';
      $('ua_usuario').value  = u?.usuario  ?? '';
    }

    function renderSelect(list){
      const sel = $('ua_select_list');
      const ordered = [...list].sort((a,b) =>
              (a.nombre || '').localeCompare(b.nombre || '', 'es', { sensitivity:'base' })
      );
      sel.innerHTML = '<option value="">— Selecciona —</option>' + ordered.map(u => {
        const label = (u.nombre && u.nombre.trim()) || u.usuario || '(sin nombre)';
        return `<option value="${u.id}">${label}</option>`;
      }).join('');
    }

    async function load(q){
      const raw = await httpJson(API.buscarMin(q || '', null, 50), { method:'GET' });
      BASE = (Array.isArray(raw) ? raw : (raw.items || raw.content || []))
              .map(mapUser).filter(x => x.id != null);
      renderSelect(BASE);
    }

    function scheduleLoad(){
      clearTimeout(debounceId);
      debounceId = setTimeout(() => load($('ua_query').value).catch(()=>{}), 220);
    }

    // Eventos UI
    $('ua_select_list')?.addEventListener('change', () => {
      const id = $('ua_select_list').value;
      const u = BASE.find(x => String(x.id) === String(id));
      setForm(u || null);
    });

    document.addEventListener('DOMContentLoaded', () => {
      $('ua_query')?.addEventListener('input', scheduleLoad);
      load('').catch(()=>{}); // carga inicial
    });

    // Acciones
    async function uaActualizar(){
      if (!SELECTED) { alert('Selecciona un usuario.'); return; }
      const nombre   = ($('ua_nombre')?.value || '').trim();
      const cedula   = digits($('ua_cedula')?.value || '');
      const correo   = ($('ua_correo')?.value || '').trim();
      const telefono = digits($('ua_telefono')?.value || '');
      const usuario  = ($('ua_usuario')?.value || '').trim();

      if (correo && !isEmail(correo))      { alert('Correo no válido'); return; }
      if (cedula && cedula.length   !==10) { alert('La cédula debe tener 10 dígitos'); return; }
      if (telefono && telefono.length!==10){ alert('El teléfono debe tener 10 dígitos'); return; }

      const body = {};
      if (nombre   && nombre   !== (SELECTED.nombre   || '')) body.nombre   = nombre;
      if (cedula   && cedula   !== (SELECTED.cedula   || '')) body.cedula   = cedula;
      if (correo   && correo   !== (SELECTED.correo   || '')) body.correo   = correo;
      if (telefono && telefono !== (SELECTED.telefono || '')) body.telefono = telefono;
      if (usuario  && usuario  !== (SELECTED.usuario  || '')) body.usuario  = usuario;

      if (!Object.keys(body).length) { alert('No hay cambios.'); return; }

      try{
        await httpJson(API.actualizar(SELECTED.id, SELECTED.rolId), { method:'PUT', body: JSON.stringify(body) });
        alert('✅ Usuario actualizado');
        Object.assign(SELECTED, body);
        scheduleLoad();
      }catch(e){ alert('❌ ' + e.message); }
    }

    async function uaEliminar(){
      if (!SELECTED) { alert('Selecciona un usuario.'); return; }
      if (!confirm('¿Deshabilitar este usuario?')) return;
      try{
        await httpJson(API.deshabilitar(SELECTED.id, SELECTED.rolId), { method:'DELETE' });
        alert('🗑️ Usuario deshabilitado');
        setForm(null);
        $('ua_select_list').value = '';
        scheduleLoad();
      }catch(e){ alert('❌ ' + e.message); }
    }

    // ✅ FIX principal: función para crear admin/trabajador
    async function crearUsuario(){
      const nombre   = (document.getElementById('v_nombre')?.value || '').trim();
      const cedula   = digits(document.getElementById('v_cedula')?.value || '');
      const correo   = (document.getElementById('v_correo')?.value || '').trim();
      const telefono = digits(document.getElementById('v_telefono')?.value || '');
      const usuario  = (document.getElementById('v_usuario')?.value || '').trim();
      const idRol    = parseInt(document.getElementById('v_id_rol')?.value, 10);

      if (!nombre){ alert('Ingresa el nombre'); return; }
      if (!cedula || cedula.length!==10){ alert('La cédula debe tener 10 dígitos'); return; }
      if (!correo || !isEmail(correo)){ alert('Correo no válido'); return; }
      if (!telefono || telefono.length!==10){ alert('El teléfono debe tener 10 dígitos'); return; }
      if (!idRol){ alert('Selecciona un rol'); return; }

      const payload = {
        nombre, cedula, correo, telefono,
        usuario: usuario || undefined, // opcional
        idRol   // requerido por tu flujo admin/trabajador
      };

      try{
        await httpJson(API.crearAdmin, { method:'POST', body: JSON.stringify(payload) });
        alert('✅ Usuario (admin/trabajador) creado. Se enviará la contraseña por correo.');
        // Limpia el formulario
        ['v_nombre','v_cedula','v_correo','v_telefono','v_usuario'].forEach(id=>{
          const el = document.getElementById(id); if(el) el.value='';
        });
        const selRol = document.getElementById('v_id_rol'); if (selRol) selRol.value='3';
        // refresca la lista mínima para que aparezca el nuevo
        scheduleLoad();
      }catch(e){
        alert('❌ ' + e.message);
      }
    }

    // Exponer a la página
    window.uaActualizar = uaActualizar;
    window.uaEliminar   = uaEliminar;
    window.crearUsuario = crearUsuario; // <-- importante para el botón

  })();

  //Subtipos de Movimiento
const API_SUBTIPOS = '/api/subtipos-movimiento';
  let SUBTIPOS_CACHE = null;

  function authHeaders() {
    const token = sessionStorage.getItem('token') || localStorage.getItem('token');
    return token ? { 'Authorization': `Bearer ${token}` } : {};
  }

  function mapSubtipo(d){
    return {
      id:      d.subtipo_id ?? d.subtipoId ?? d.id,
      nombre:  d.nombre ?? '',
      tipoId:  d.tipo_id ?? d.tipoId ?? null   // 1=Entrada, 2=Salida
    };
  }

  async function fetchSubtipos({ tipo = null } = {}){
    const u = new URL(API_SUBTIPOS, location.origin);
    if (tipo != null) u.searchParams.set('tipo', tipo);
    const res = await fetch(u.toString(), {
      headers: { 'Content-Type':'application/json', ...authHeaders() }
    });
    const txt = await res.text();
    const raw = txt ? JSON.parse(txt) : [];
    const list = (Array.isArray(raw) ? raw : (raw.content || raw.items || []))
      .map(mapSubtipo)
      .filter(x => x.id != null);
    list.sort((a,b)=>(a.tipoId??0)-(b.tipoId??0) || a.nombre.localeCompare(b.nombre));
    return list;
  }

  async function loadSubtiposOnce(){
    if (SUBTIPOS_CACHE) return SUBTIPOS_CACHE;
    try { SUBTIPOS_CACHE = await fetchSubtipos(); }
    catch(e){ console.error('Error cargando subtipos:', e); SUBTIPOS_CACHE = []; }
    return SUBTIPOS_CACHE;
  }

  function toggleSubtipoPlaceholderClass(sel){
    if (!sel) return;
    if (!sel.value) sel.classList.add('is-empty');
    else sel.classList.remove('is-empty');
  }

 function renderSubtipoSelect(list, selectedId){
  const sel = document.getElementById('mov_subtipo_id');
  if (!sel) return;

  // Placeholder no seleccionable
  let html = `<option value="" disabled ${selectedId ? '' : 'selected'} hidden>
                Seleccionar subtipo
              </option>`;

  // Agrupar por tipo
  const grupos = { '1': { label:'Entradas', items:[] },
                   '2': { label:'Salidas',  items:[] },
                   'null': { label:'Otros', items:[] } };
  for (const s of list){
    (grupos[String(s.tipoId ?? 'null')] ?? grupos['null']).items.push(s);
  }
  for (const key of ['1','2','null']){
    const g = grupos[key]; if (!g.items.length) continue;
    html += `<optgroup label="${g.label}">` +
            g.items.map(s => `<option value="${s.id}">${s.nombre}</option>`).join('') +
            `</optgroup>`;
  }

  // Pintar y seleccionar si aplica
  sel.innerHTML = html;
  if (selectedId != null) sel.value = String(selectedId);

  // Apariencia placeholder + estado de costo
  toggleSubtipoPlaceholderClass(sel);
  updateCostoUnitarioState();

  // Listener único para ambos efectos
  if (!sel.dataset.bound){
    sel.addEventListener('change', () => {
      toggleSubtipoPlaceholderClass(sel);
      updateCostoUnitarioState();
      updateCantidadMode();
    });
    sel.dataset.bound = '1';
  }
  
}
    // Cargar al abrir el modal de Movimiento
    (function initSubtiposOnModalOpen(){
      const btn = document.getElementById('btnAgregarMovimiento');
      if (!btn) return;
      btn.addEventListener('click', async () => {
        const sel = document.getElementById('mov_subtipo_id');
        const keep = sel?.value || null;
        const data = await loadSubtiposOnce();
        renderSubtipoSelect(data, keep);
        updateCantidadMode();
      });
    })();



    // ¿El subtipo seleccionado es COMPRA?
    // ¿El subtipo seleccionado es COMPRA?
    function isCompraSelected() {
      const sel = document.getElementById('mov_subtipo_id');
      const txt = sel?.options?.[sel.selectedIndex]?.text || '';
      return txt.trim().toUpperCase() === 'COMPRA';
    }
    
    function isAjusteSelected() {
      const sel = document.getElementById('mov_subtipo_id');
      const txt = sel?.options?.[sel.selectedIndex]?.text || '';
      return txt.trim().toUpperCase() === 'AJUSTE';
    }
    // Cambia el modo del input Cantidad según el subtipo
    function updateCantidadMode() {
      const qty = document.getElementById('mov_cantidad');
      if (!qty) return;

      if (isAjusteSelected()) {
        // permitir negativos y positivos, sin 0 (se valida en initCantidad)
        // usar text o number con min negativo:
        qty.type = 'text';            // <- más compatible con el '-'
        qty.removeAttribute('min');
        qty.placeholder = '±1';
      } else {
        qty.type = 'number';
        qty.setAttribute('min', '1'); // solo positivos
        qty.step = '1';
        qty.placeholder = '1';
        // normaliza si quedó un valor inválido
        const n = parseInt(qty.value, 10);
        if (!Number.isInteger(n) || n < 1) qty.value = '1';
      }
    }


    // Habilita/inhabilita el campo de costo según el subtipo
    function updateCostoUnitarioState() {
      const input = document.getElementById('mov_costo_unitario');
      if (!input) return;
      if (isCompraSelected()) {
        input.disabled = false;
        input.title = '';
      } else {
        input.disabled = true;
        input.value = ''; // limpiamos para no enviar costo cuando no procede
        input.title = 'Disponible solo cuando el subtipo es COMPRA';
      }
    }



// CANTIDAD: solo enteros y > 0
(function initCantidad(){
  const qty  = document.getElementById('mov_cantidad');
  const form = document.getElementById('formNuevoMovimiento');
  if (!qty) return;

  // Limpia según el modo actual
  function sanitize() {
    if (isAjusteSelected()) {
      // permitir un '-' al inicio y dígitos; quitar repetidos o internos
      let v = qty.value.replace(/[^\d-]/g, '');
      // si hay más de un '-', deja solo el primero y al inicio
      v = v.replace(/-/g, (m, i) => (i === 0 ? '-' : ''));
      if (v.length > 1 && v[0] !== '-') v = v.replace(/-/g, ''); // '-' solo al inicio
      // quitar ceros a la izquierda (pero deja "-0" temporalmente para corregir en blur)
      v = v.replace(/^(-?)0+(\d)/, '$1$2');
      qty.value = v;
    } else {
      // solo enteros positivos
      let v = qty.value.replace(/[^\d]/g, '');
      v = v.replace(/^0+/, '');
      qty.value = v;
    }
  }

  // Teclas permitidas según el modo
  qty.addEventListener('keydown', (e) => {
    const nav = ['Backspace','Delete','ArrowLeft','ArrowRight','Home','End','Tab'];
    if (e.ctrlKey || e.metaKey || nav.includes(e.key)) return;

    if (isAjusteSelected()) {
      // permitir un '-' al inicio
      if (e.key === '-') {
        const { selectionStart, selectionEnd, value } = qty;
        const alreadyHasMinus = value.startsWith('-');
        const caretAtStart = selectionStart === 0 && selectionEnd === 0;
        if (alreadyHasMinus || !caretAtStart) { e.preventDefault(); }
        return;
      }
      if (!/^\d$/.test(e.key)) e.preventDefault();
      return;
    }

    // modo normal (no AJUSTE): bloquear -, +, e/E y no dígitos
    if (['-','+','e','E'].includes(e.key)) { e.preventDefault(); return; }
    if (!/^\d$/.test(e.key)) e.preventDefault();
  });

  // Limpieza en vivo
  qty.addEventListener('input', sanitize);

  // Normaliza al salir
  qty.addEventListener('blur', () => {
    sanitize();
    const raw = qty.value.trim();
    if (isAjusteSelected()) {
      // entero ≠ 0
      const n = parseInt(raw, 10);
      if (!Number.isInteger(n) || n === 0) qty.value = '1'; // valor por defecto válido
    } else {
      // entero ≥ 1
      let n = parseInt(raw, 10);
      if (!Number.isInteger(n) || n < 1) n = 1;
      qty.value = String(n);
    }
  });

  // Validación final al enviar el formulario
  if (form && !form.dataset.qtyBound){
    form.addEventListener('submit', (e) => {
      const n = parseInt(qty.value, 10);
      const ok = isAjusteSelected() ? (Number.isInteger(n) && n !== 0)
                                    : (Number.isInteger(n) && n >= 1);
      if (!ok) {
        e.preventDefault();
        alert(isAjusteSelected()
          ? 'La cantidad debe ser un entero distinto de 0 (se permiten negativos) para AJUSTE.'
          : 'La cantidad debe ser un entero mayor a 0.');
        qty.focus();
      }
    });
    form.dataset.qtyBound = '1';
  }

  // Revalidar al cambiar el subtipo (ya tienes un listener; aprovechamos)
  const sel = document.getElementById('mov_subtipo_id');
  if (sel && !sel.dataset.rebindQty){
    sel.addEventListener('change', () => {
      // al cambiar a/desde AJUSTE, re-sanitiza y aplica reglas
      qty.dispatchEvent(new Event('blur'));
    });
    sel.dataset.rebindQty = '1';
  }
})();


    //Costo Unitario
(function initCostoUnitarioMask(){
      const el = document.getElementById('mov_costo_unitario');
      const form = document.getElementById('formNuevoMovimiento');
      if (!el) return;

      // Teclas permitidas
      el.addEventListener('keydown', (e) => {
        const k = e.key;
        if (e.ctrlKey || e.metaKey) return;
        const nav = ['Backspace','Delete','ArrowLeft','ArrowRight','Home','End','Tab'];
        if (nav.includes(k)) return;
        if (k === '-' || k === '+' || k === 'e' || k === 'E') { e.preventDefault(); return; }
        if (k === '.' || k === ',') {
          const v = el.value;
          const hasSep = v.includes('.') || v.includes(',');
          const selection = el.selectionStart !== el.selectionEnd;
          if (hasSep && !selection) e.preventDefault();
          return;
        }
        if (/^\d$/.test(k)) return;
        e.preventDefault();
      });

      // Validación en vivo sin mover el caret
      let last = '', lastPos = 0;
      el.addEventListener('input', () => {
        const v = el.value;
        const ok = /^\d{0,9}([.,]\d{0,2})?$/.test(v);
        if (ok || v === '') { last = v; lastPos = el.selectionStart || 0; }
        else { el.value = last; el.setSelectionRange(lastPos, lastPos); }
      });

      // Normalizar al salir (mostrar con coma)
      el.addEventListener('blur', () => {
        const raw = el.value.trim();
        if (raw === '') return;
        const n = Number(raw.replace(',', '.'));
        if (!isFinite(n) || n <= 0) { el.value = ''; return; }
        el.value = n.toFixed(2).replace('.', ',');
      });

      // >>> Permitir vacío al enviar (solo valida si hay valor)
      if (form && !form.dataset.costMaskBound){
        form.addEventListener('submit', (e) => {
          if (el.disabled) return;            // si no es COMPRA, ignorar
          const raw = el.value.trim();
          if (raw === '') return;             // permitir enviar sin costo
          const n = Number(raw.replace(',', '.'));
          if (!isFinite(n) || n <= 0) {
            e.preventDefault();
            alert('El costo unitario debe ser un número decimal positivo (o déjalo vacío).');
            el.focus();
            return;
          }
          // normaliza con punto para backend
          el.value = n.toFixed(2);            // "123.45"
        });
        form.dataset.costMaskBound = '1';
      }
    })();



    //Feedback de movimiento de inventario
    (function validarMovimientoConFeedback(){
      const form    = document.getElementById('formNuevoMovimiento');
      if (!form) return;

      const inpNom  = document.getElementById('mov_producto_nombre');
      const inpId   = document.getElementById('mov_producto_id');
      const msg     = document.getElementById('mov_producto_msg');

      // Refs para habilitar/deshabilitar "Guardar"
      const btnSave = document.getElementById('btnGuardarMov');
      const qty     = document.getElementById('mov_cantidad');
      const subtipo = document.getElementById('mov_subtipo_id');
      const fecha   = document.getElementById('mov_fecha'); // <input type="date" id="mov_fecha">

      const hasProducto = () => {
        const n = parseInt(inpId?.value || '0', 10);
        return Number.isInteger(n) && n > 0;
      };

      function setProductoError(on, text='Debes seleccionar un producto.') {
        inpNom.classList.toggle('is-invalid', on);
        if (msg) { msg.textContent = text; msg.hidden = !on; }
        inpNom.setCustomValidity(on ? text : '');
      }

      function updateGuardarState(){
        const n          = parseInt(qty?.value || '0', 10);
        const okQty      = Number.isInteger(n) && n >= 1;
        const okSubtipo  = !!subtipo?.value;
        const okFecha    = fecha ? !!fecha.value : true;  // true si aún no agregaste el campo
        const okProducto = hasProducto();
        if (btnSave) btnSave.disabled = false;
      }
      // Expuesta para que el picker la use al elegir producto
      window.updateGuardarState = updateGuardarState;

      qty?.addEventListener('input',  updateGuardarState);
      subtipo?.addEventListener('change', updateGuardarState);
      fecha?.addEventListener('change', updateGuardarState);

      // Submit (CAPTURE): valida producto y bloquea el "✓ Completado" del handler de botones
      form.addEventListener('submit', (e) => {
        if (!hasProducto()) {
          setProductoError(true);
          e.preventDefault();
          e.stopImmediatePropagation();
          form.dataset.blockDemo = '1';   // <- evita "Completado" en el click handler
          form.reportValidity();
          return;
        }
        setProductoError(false);
      }, { capture: true });

      // Al abrir el modal, limpiar y recalcular
      document.getElementById('btnAgregarMovimiento')?.addEventListener('click', () => {
        setProductoError(false);
        updateGuardarState();
      });

      // Estado inicial
      updateGuardarState();
    })();


///Enviar Movimiento
(function wireGuardarMovimiento() {
    const API_MOV_SP = '/api/sp/movimiento-inventario'; // ajusta si tu ruta es otra
    const form = document.getElementById('formNuevoMovimiento');
    const btn  = document.getElementById('btnGuardarMov');
    if (!form || !btn) return;

    const $ = id => document.getElementById(id);

    const f = {
      id:    () => parseInt($('mov_producto_id')?.value || '0', 10),
      sub:   () => parseInt($('mov_subtipo_id')?.value || '0', 10),
      qty:   () => parseInt($('mov_cantidad')?.value || '0', 10),
      fecha: () => ($('mov_fecha')?.value || '').trim(),
      ref:   () => ($('mov_referencia')?.value || '').trim(),
      obs:   () => ($('mov_observacion')?.value || '').trim(),
      costo: () => {
        const el = $('mov_costo_unitario');
        if (!el || el.disabled) return null;
        const raw = el.value.trim();
        if (!raw) return null;
        const n = Number(raw.replace(',', '.'));
        return Number.isFinite(n) ? Number(n.toFixed(2)) : null;
      }
    };

    form.addEventListener('submit', async (e) => {
      if (form.dataset.blockDemo === '1') { delete form.dataset.blockDemo; return; }
      e.preventDefault();

      if (!form.reportValidity()) return;
      if (f.id() <= 0) return;

      try {
        btn.disabled = true;

        const fecha = f.fecha();
        const item = {
          producto_id:    f.id(),
          subtipo_id:     f.sub(),
          cantidad:       f.qty(),
          fecha:          fecha ? `${fecha} 00:00:00` : null,
          referencia:     f.ref() || null,
          observacion:    f.obs() || null,
          costo_unitario: f.costo()
        };

        // Opción A: usuario por query param (recomendado)
        const userName = currentUsername();
        const url = userName
          ? `${API_MOV_SP}?usuario=${encodeURIComponent(userName)}`
          : API_MOV_SP;

        // (Opcional) Blindaje adicional: también mete el usuario en el item
        if (userName) item.usuario = userName;

        const res = await fetch(url, {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json'
            // ...(typeof authHeaders === 'function' ? authHeaders() : {})
          },
          body: JSON.stringify(item)
        });

        const txt = await res.text();
        if (!res.ok) throw new Error(txt || ('HTTP ' + res.status));

        alert('✅ Movimiento registrado');

        // Cerrar modal y limpiar
        document.getElementById('movBackdrop').hidden = true;
        document.body.classList.remove('tt-no-scroll');
        form.reset();
        if (typeof updateCostoUnitarioState === 'function') updateCostoUnitarioState();
        if (typeof window.updateGuardarState === 'function') window.updateGuardarState();

        // Refrescar tabla (si existe)
        if (typeof loadMovimientos === 'function') await loadMovimientos();

      } catch (err) {
        alert('❌ ' + (err?.message || err));
      } finally {
        btn.disabled = false;
      }
    });
  })();

  ////Listar Movimientos en la tabla de movimiento inventario
const API_MOVS = '/api/movimientos'; // <-- ajusta a tu ruta real

    function renderEmptyMovs(msg){
      const tb = document.getElementById('tbMovimientos');
      if (tb) tb.innerHTML = `<tr><td colspan="9" class="tt-muted-row">${msg}</td></tr>`;
    }
    function fmtUSD(n){
      const x = Number(n ?? 0);
      return new Intl.NumberFormat('es-EC',{style:'currency',currency:'USD'}).format(x);
    }
    function fmtFecha(s){
      if (!s) return '';
      const d = new Date(s);
      if (isNaN(d)) return s; // por si ya viene formateado
      // fecha + hora corta
      return d.toLocaleDateString() + ' ' + d.toLocaleTimeString([], {hour:'2-digit', minute:'2-digit'});
    }
    function mapMov(raw){
      return {
        id:            raw.movimiento_id ?? raw.id ?? raw.movId,
        fecha:         raw.fecha ?? raw.createdAt,
        producto:      raw.producto?.nombre ?? raw.producto_nombre ?? raw.nombreProducto ?? raw.producto ?? '',
        subtipo:       raw.subtipo?.nombre  ?? raw.subtipo_nombre  ?? raw.subtipo  ?? '',
        cantidad:      raw.cantidad ?? raw.qty ?? 0,
        costo_unit:    raw.costo_unitario ?? raw.costoUnitario ?? raw.costo ?? null,
        costo_total:   raw.costo_total ?? raw.costoTotal ?? null,
        referencia:    raw.referencia ?? raw.ref ?? '',
        observacion:   raw.observacion ?? raw.obs ?? ''
      };
    }

    async function loadMovimientos(){
      const tb = document.getElementById('tbMovimientos');
      if (!tb) return;
      renderEmptyMovs('Cargando…');
      try {
        const res = await fetch(API_MOVS, { headers: { 'Content-Type':'application/json', ...(typeof authHeaders==='function'? authHeaders():{}) }});
        const txt = await res.text();
        const data = txt ? JSON.parse(txt) : [];
        const list = (Array.isArray(data) ? data : (data.content || data.items || [])).map(mapMov);

        if (!list.length){ renderEmptyMovs('Sin movimientos'); return; }

        tb.innerHTML = list.map(m => `
          <tr>
            <td>${m.id ?? ''}</td>
            <td>${fmtFecha(m.fecha)}</td>
            <td>${m.producto}</td>
            <td>${m.subtipo}</td>
            <td>${m.cantidad}</td>
            <td>${m.costo_unit != null ? fmtUSD(m.costo_unit) : ''}</td>
            <td>${m.costo_total != null ? fmtUSD(m.costo_total) : ''}</td>
            <td>${m.referencia}</td>
            <td>${m.observacion}</td>
          </tr>
        `).join('');
      } catch (e){
        console.error('loadMovimientos:', e);
        renderEmptyMovs('No se pudo cargar /api/movimientos');
      }
    }




//ShwowSection 
function showSection(sectionId, el){
      // cambiar de sección
      document.querySelectorAll('.admin-section').forEach(s => s.classList.remove('active'));
      const sec = document.getElementById(sectionId); if (sec) sec.classList.add('active');
      document.querySelectorAll('.nav-item').forEach(i => i.classList.remove('active'));
      if (el) el.classList.add('active');

      // refrescar tabla al abrir "Movimiento de Inventario"
      if (sectionId === 'inventory') { loadMovimientos?.(); }
    }

    // Si "inventory" ya está visible al cargar, pedir datos también
    document.addEventListener('DOMContentLoaded', () => {
      if (document.getElementById('inventory')?.classList.contains('active')) {
        loadMovimientos?.();
      }
    });

//Modal de movimeinto inventario
function resetMovimientoModal() {
      const form = document.getElementById('formNuevoMovimiento');
      if (!form) return;

      form.reset();                                // limpia cantidad, fecha, etc.
      const idEl   = document.getElementById('mov_producto_id');
      const nomEl  = document.getElementById('mov_producto_nombre');
      const msgEl  = document.getElementById('mov_producto_msg');

      if (idEl)  idEl.value  = '';
      if (nomEl) {
        nomEl.value = '';
        nomEl.setCustomValidity('');               // quita error pendiente
      }
      if (msgEl) msgEl.hidden = true;

      updateCostoUnitarioState?.();
      window.updateGuardarState?.();
    }

    // Abrir modal
    document.getElementById('btnAgregarMovimiento').addEventListener('click', () => {
      resetMovimientoModal();                      // <-- limpiar SIEMPRE al abrir
      document.getElementById('movBackdrop').hidden = false;
      document.body.classList.add('tt-no-scroll');
    });

    // Cerrar con la X
    document.getElementById('movCloseBtn').addEventListener('click', () => {
      resetMovimientoModal();                      // <-- limpiar también al cancelar
      document.getElementById('movBackdrop').hidden = true;
      document.body.classList.remove('tt-no-scroll');
    });




//Endpoitns
(() => {
      // ===== Endpoints =====
      const API_PROD_SEARCH = '/api/productos/buscar'; // POST JSON -> array
      const API_CATS        = '/api/categorias';       // GET -> [{id_categoria,nombre}]

      // ===== Refs (coinciden con TU HTML) =====
      const PP = {
        backdrop:  document.getElementById('movProdBackdrop'),
        closeBtn:  document.getElementById('movProdClose'),
        // filtros
        cat:       document.getElementById('pp_cat'),
        name:      document.getElementById('pp_name'),     // nombre o ID
        stockMin:  document.getElementById('pp_stock_min'),
        stockMax:  document.getElementById('pp_stock_max'),
        fmin:      document.getElementById('pp_fini'),     // fecha inicio
        fmax:      document.getElementById('pp_ffin'),     // fecha fin
        search:    document.getElementById('pp_search'),
        // tabla
        tbody:     document.getElementById('pp_tbody'),
        // pie (sin paginación)
        prev:      document.getElementById('pp_prev'),
        next:      document.getElementById('pp_next'),
        info:      document.getElementById('pp_info')
      };

      // ===== Utils =====
      const asInt = (v) => {
        const t = String(v ?? '').trim();
        if (t === '') return undefined;
        const n = parseInt(t, 10);
        return Number.isFinite(n) ? n : undefined;
      };
      const fmtUSDV2 = (n) =>
        new Intl.NumberFormat('es-EC', { style:'currency', currency:'USD' })
          .format(Number(n || 0));

      const mapProduct = (raw) => ({
        id:     raw.producto_id ?? raw.productoId ?? raw.id ?? raw.id_producto,
        nombre: raw.nombre ?? raw.name ?? '',
        precio: raw.preciounitario ?? raw.precioUnitario ?? raw.precio ?? 0,
        stock:  raw.stock ?? raw.cantidad ?? 0,
        costo:  Number(raw.costo ?? raw.cost ?? raw.costo_unitario ?? 0)
      });

      // ===== Cargar categorías cada vez que abres el modal =====
      async function loadPickerCategorias() {
        const sel = PP.cat;
        if (!sel) return;
        sel.innerHTML = '<option value="">Todas las categorías</option>';
        try {
          const res  = await fetch(API_CATS);
          if (!res.ok) throw new Error('HTTP ' + res.status);
          const data = await res.json();
          for (const c of (Array.isArray(data) ? data : [])) {
            const opt = document.createElement('option');
            opt.value = c.id_categoria ?? c.id ?? '';
            opt.textContent = c.nombre ?? '';
            sel.appendChild(opt);
          }
        } catch (err) {
          console.warn('No se pudieron cargar categorías:', err);
          const opt = document.createElement('option');
          opt.disabled = true;
          opt.textContent = '(No se pudo cargar categorías)';
          sel.appendChild(opt);
        }
      }

      function resetPickerFilters(){
        if (PP.cat)      PP.cat.value = '';
        if (PP.name)     PP.name.value = '';
        if (PP.stockMin) PP.stockMin.value = '';
        if (PP.stockMax) PP.stockMax.value = '';
        if (PP.fmin)     PP.fmin.value = '';
        if (PP.fmax)     PP.fmax.value = '';
      }

      // ===== HTTP =====
      async function buscarProductos(filtros){
        const res = await fetch(API_PROD_SEARCH, {
          method: 'POST',
          headers: { 'Content-Type':'application/json' },
          body: JSON.stringify(filtros)
        });
        const txt  = await res.text();
        const data = txt ? JSON.parse(txt) : [];
        return Array.isArray(data) ? data : (data.content || data.items || []);
      }

      // ===== Render tabla =====
      function renderPickerTable(items){
        if (!PP.tbody) return;
        if (!items || !items.length){
          PP.tbody.innerHTML = `<tr><td colspan="6" class="tt-muted-row">Sin resultados</td></tr>`;
        } else {
          PP.tbody.innerHTML = items.map(p => `
            <tr>
              <td>${p.nombre ?? ''}</td>
              <td>${fmtUSDV2(p.precio)}</td>
              <td>${fmtUSDV2(p.costo)}</td>
              <td>${p.stock ?? 0}</td>
              <td>
                <button class="btn btn-primary btn--sm"
                        data-pick="${p.id}"
                        data-name="${p.nombre}">Elegir</button>
              </td>
            </tr>
          `).join('');
        }

        // sin paginación
        if (PP.prev) PP.prev.disabled = true;
        if (PP.next) PP.next.disabled = true;
        if (PP.info) PP.info.textContent = `${items?.length ?? 0} ítems`;

        // elegir
        PP.tbody.querySelectorAll('button[data-pick]').forEach(btn=>{
          btn.addEventListener('click', () => {
            const id   = btn.getAttribute('data-pick');
            const name = btn.getAttribute('data-name') || `#${id}`;
            document.getElementById('mov_producto_id').value = id;
            const nameEl = document.getElementById('mov_producto_nombre');
            if (nameEl) {
              nameEl.value = name;
              nameEl.classList.remove('is-invalid');
              nameEl.setCustomValidity('');
            }
            const msg = document.getElementById('mov_producto_msg');
            if (msg) msg.hidden = true;
            if (window.updateGuardarState) window.updateGuardarState();
            closeProductPicker();
          });
        });
      }

      // ===== Ejecutar búsqueda =====
      async function loadPicker(){
        if (!PP.tbody) return;

        // Sanitizar y validar
        const sMin = asInt(PP.stockMin?.value);
        const sMax = asInt(PP.stockMax?.value);
        const fmin = (PP.fmin?.value || '').trim() || undefined;
        const fmax = (PP.fmax?.value || '').trim() || undefined;
        if (fmin && fmax && fmin > fmax){
          alert('La fecha inicial no puede ser mayor que la final');
          return;
        }

        const filtros = {};
        const catId = asInt(PP.cat?.value);
        if (catId !== undefined) filtros.categoria_id = catId;
        const q = (PP.name?.value || '').trim();
        if (q) filtros.q = q;
        if (sMin !== undefined) filtros.stock_min = sMin;  // <-- permite 0
        if (sMax !== undefined) filtros.stock_max = sMax;  // <-- permite 0
        if (fmin) filtros.fecha_min = fmin;
        if (fmax) filtros.fecha_max = fmax;

        PP.tbody.innerHTML = `<tr><td colspan="6" class="tt-muted-row">Cargando…</td></tr>`;
        try{
          const data  = await buscarProductos(filtros);
          const items = (Array.isArray(data) ? data : []).map(mapProduct);
          renderPickerTable(items);
        }catch(err){
          console.error('loadPicker:', err);
          PP.tbody.innerHTML = `<tr><td colspan="6" class="tt-muted-row">Error cargando resultados</td></tr>`;
        }
      }

      // ===== Abrir / cerrar modal =====
      async function openProductPicker(){
        if (!PP.backdrop) return;
        PP.backdrop.hidden = false;
        document.body.classList.add('tt-no-scroll');
        resetPickerFilters();
        await loadPickerCategorias();
        await loadPicker();              // lista inicial (sin filtros)
        setTimeout(()=> PP.name?.focus(), 0);
      }
      function closeProductPicker(){
        if (!PP.backdrop) return;
        PP.backdrop.hidden = true;
        document.body.classList.remove('tt-no-scroll');
      }

      // ===== Enlaces =====
      document.getElementById('btnAddProducto')?.addEventListener('click', openProductPicker);
      PP.closeBtn?.addEventListener('click', closeProductPicker);

      // Buscar SOLO con botón o Enter (no al cambiar categoría)
      PP.search?.addEventListener('click', () => loadPicker());
      [PP.name, PP.stockMin, PP.stockMax, PP.fmin, PP.fmax].forEach(inp => {
        if (!inp) return;
        inp.addEventListener('keydown', e => {
          if (e.key === 'Enter'){ e.preventDefault(); loadPicker(); }
        });
      });

      // Inputs de stock: permitir 0, sin letras ni decimales
      [PP.stockMin, PP.stockMax].forEach(inp => {
        if (!inp) return;
        // bloquear símbolos no numéricos
        inp.addEventListener('keydown', e => {
          if (['e','E','+','-','.',','].includes(e.key)) e.preventDefault();
        });
        // limpiar y conservar "0" válido
        inp.addEventListener('input', () => {
          let v = inp.value.replace(/[^\d]/g, '');
          if (v.length > 1) {
            v = v.replace(/^0+/, '');
            if (v === '') v = '0'; // 000 -> 0
          }
          inp.value = v;
        });
      });

      // Deshabilitar paginadores (no usados)
      if (PP.prev) PP.prev.disabled = true;
      if (PP.next) PP.next.disabled = true;

      // Exponer si lo necesitas
      window.openProductPicker = openProductPicker;
    })();

    (() => {
      // === Endpoints ===
      // === Endpoints ===
      const API_DETALLE = (id) => `/api/sp/productos/${encodeURIComponent(id)}/editar`;
      const API_IVAS    = '/api/sp/ivas';
      const API_UPDATE  = (id) => `/api/sp/productos/${encodeURIComponent(id)}/basico`;


      // Refs del modal Editar
      const $ = (id) => document.getElementById(id);
      const EP = {
        backdrop: $('ep_backdrop'),
        close:    $('ep_btn_close'),
        form:     $('ep_form'),
        id:       $('ep_id'),
        nombre:   $('ep_nombre'),
        enlace:   $('ep_enlace'),
        iva:      $('ep_iva'),
        hab:      $('ep_hab'),
        precio:   $('ep_precio'),
      };

      // === Cache IVAs ===
      let IVAS_CACHE = null;
      async function loadIvas() {
        if (IVAS_CACHE) return IVAS_CACHE;
        const res = await fetch(API_IVAS, { headers: { ...(typeof authHeaders==='function'? authHeaders():{}) } });
        const data = await res.json();
        IVAS_CACHE = Array.isArray(data) ? data : [];
        return IVAS_CACHE;
      }
      async function fillIvaSelect(selectedId) {
        const list = await loadIvas();
        EP.iva.innerHTML = list
          .filter(x => x.habilitado !== false)
          .map(x => {
            const id  = x.ivaId ?? x.iva_id ?? x.id;
            const tag = x.etiqueta ?? (x.porcentaje != null ? (x.porcentaje + '%') : id);
            // ⬇⬇⬇ aquí estaba el error: faltaban las comillas invertidas
            return `<option value="${id}">${tag}</option>`;
          })
          .join('');
        if (selectedId != null) EP.iva.value = String(selectedId);
      }


      // === Cargar detalle ===
      async function fetchDetalle(id) {
        const res = await fetch(API_DETALLE(id), { headers: { ...(typeof authHeaders==='function'? authHeaders():{}) }});
        if (!res.ok) throw new Error('HTTP ' + res.status);
        const d = await res.json();
        return {
          productoId: d.productoId ?? d.producto_id ?? id,
          nombre:     d.nombre ?? '',
          enlace:     d.enlace ?? '',
          ivaId:      d.ivaId ?? d.iva_id ?? null,
          habilitado: (d.habilitado ?? true),
          precio:     d.precioUnitario ?? d.preciounitario ?? d.precio_unitario ?? null
          // costoActual: (eliminado)
        };
        
      }

      // === Abrir/cerrar modal ===
      async function openEditModal(id){
        if (!EP.backdrop) return;
        EP.form?.reset();
        EP.backdrop.hidden = false;
        document.body.classList.add('tt-no-scroll');

        try {
          // Carga en paralelo
          const [detalle] = await Promise.all([fetchDetalle(id), fillIvaSelect(null)]);
          EP.id.value     = detalle.productoId;
          EP.nombre.value = detalle.nombre;
          EP.enlace.value = detalle.enlace || '';
          EP.hab.value    = String(!!detalle.habilitado);
          EP.precio.value = (detalle.precio != null ? Number(detalle.precio).toFixed(2) : '');
          
          await fillIvaSelect(detalle.ivaId);
        } catch(e){
          alert('❌ No se pudo cargar el detalle: ' + (e.message || e));
          closeEditModal();
        }
      }
      function closeEditModal(){
        if (!EP.backdrop) return;
        EP.backdrop.hidden = true;
        document.body.classList.remove('tt-no-scroll');
      }

      // Cerrar con la ❌
      EP.close?.addEventListener('click', closeEditModal);
      // No cerrar por Escape/backdrop (coincide con tu UX)

      // Delegación: click en "Editar" dentro de la tabla
      document.addEventListener('click', (ev) => {
        const btn = ev.target.closest('#tbProductos button[data-edit]');
        if (!btn) return;
        ev.preventDefault();
        openEditModal(Number(btn.dataset.edit));
      });
          // Bloquear negativos / notación científica y limitar a 2 decimales en "Precio unitario"
        (function initPrecioUnitarioMask(){
          const el = document.getElementById('ep_precio');
          const form = document.getElementById('ep_form');
          if (!el || el.dataset.maskBound) return;

          // No permitir -, +, e/E y sólo un separador decimal
          el.addEventListener('keydown', (e) => {
            const nav = ['Backspace','Delete','ArrowLeft','ArrowRight','Home','End','Tab'];
            if (e.ctrlKey || e.metaKey || nav.includes(e.key)) return;
            if (['-','+','e','E'].includes(e.key)) { e.preventDefault(); return; }
            if (e.key === '.' || e.key === ',') {
              const v = el.value;
              const hasSep = v.includes('.') || v.includes(',');
              const selection = el.selectionStart !== el.selectionEnd;
              if (hasSep && !selection) e.preventDefault();
              return;
            }
            if (!/^\d$/.test(e.key)) e.preventDefault();
          });

          // Mantener formato 0–2 decimales mientras escribe
          let last='', pos=0;
          el.addEventListener('input', () => {
            const v = el.value;
            const ok = /^\d{0,9}([.,]\d{0,2})?$/.test(v);
            if (ok || v === '') { last = v; pos = el.selectionStart || 0; }
            else { el.value = last; el.setSelectionRange(pos, pos); }
          });

          // Normaliza al salir (≥ 0 y 2 decimales)
          el.addEventListener('blur', () => {
            const raw = el.value.trim();
            if (raw === '') return;
            const n = Number(raw.replace(',', '.'));
            if (!isFinite(n) || n < 0) { el.value = ''; el.reportValidity?.(); return; }
            el.value = n.toFixed(2); // deja punto para el backend
          });

          // Validación final al guardar
          form?.addEventListener('submit', (e) => {
            const raw = el.value.trim();
            const n = Number(raw.replace(',', '.'));
            if (!isFinite(n) || n < 0) {
              e.preventDefault();
              alert('El precio unitario debe ser un número ≥ 0');
              el.focus();
            }
          });

          el.dataset.maskBound = '1';
        })();
      // === Guardar cambios ===
      EP.form?.addEventListener('submit', async (e) => {
        e.preventDefault();
        const id = parseInt(EP.id.value, 10);
        if (!id) { alert('ID inválido'); return; }

        const rawPrecio = (EP.precio.value || '').trim();
        const precio = rawPrecio !== '' ? Number(rawPrecio.replace(',', '.')) : undefined;
        const payload = {
        nombre: (EP.nombre.value || '').trim() || undefined,
        enlace: (EP.enlace.value || '').trim() || null,
        iva_id: EP.iva.value ? parseInt(EP.iva.value, 10) : undefined,   // <-- snake_case
        habilitado: EP.hab.value === 'true',
        preciounitario: (precio != null && isFinite(precio)) ? precio : undefined
         };

        try {
          const res = await fetch(API_UPDATE(id), {
            method: 'PUT',
            headers: {
              'Content-Type': 'application/json',
              'X-Usuario': (typeof getLoggedUsername==='function' ? (getLoggedUsername() || '') : ''),
              ...(typeof authHeaders==='function' ? authHeaders() : {})
            },
            body: JSON.stringify(payload)
          });
          const txt = await res.text();
          if (!res.ok) throw new Error(txt || ('HTTP ' + res.status));

          closeEditModal();
          await (typeof loadProductos==='function' ? loadProductos() : Promise.resolve());
          alert('✅ Cambios guardados');
        } catch (e) {
          alert('❌ No se pudo guardar: ' + (e.message || e));
        }
      });
    })();
