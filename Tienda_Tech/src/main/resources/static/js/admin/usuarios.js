//Leer usuario
function currentUsername(){
    try {
        const raw = sessionStorage.getItem('user') || localStorage.getItem('user');
    if (!raw) return null;
        const u = JSON.parse(raw);
        // Ajusta estas claves a tu objeto real
        return u?.usuario || u?.username || u?.user || null;
    } catch { return null; }
}
//Cerrar sesión
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