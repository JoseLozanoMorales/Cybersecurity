(() => {
        const LOGIN_URL = '/Login.html';
        const ENCUESTA_ID = 1;
        const TOP_N = 3;

        const $ = (s, r = document) => r.querySelector(s);
        const el = (tag, attrs = {}) => Object.assign(document.createElement(tag), attrs);

        function isLogged() {
            try {
                const raw = sessionStorage.getItem('user') || localStorage.getItem('user');
                if (raw) {
                    JSON.parse(raw);
                    return true;
                }
            } catch {
            }
            return !!localStorage.getItem('token') || !!localStorage.getItem('refreshToken');
        }

        /*function getUserFromStorage() {
            try {
                const raw = sessionStorage.getItem('user') || localStorage.getItem('user');
                return raw ? JSON.parse(raw) : null;
            } catch {
                return null;
            }
        }*/

        function injectUI() {
            const btn = document.getElementById('openSurveyBtn');
            const overlay = document.getElementById('ttSurveyOverlay');
            const modal = document.getElementById('ttSurveyModal');
            const form = document.getElementById('ttSurveyForm');
            const result = document.getElementById('ttSurveyResult');
            const close = modal.querySelector('.tt-modal-close');

            const open = () => {
                if (!isLogged()) {
                    const next = encodeURIComponent(location.pathname);
                    location.href = `/Login.html?next=${next}`;
                    return;
                }
                overlay.removeAttribute('hidden');
                modal.removeAttribute('hidden');
                ttSetStyle(overlay, 'display', 'block');
                ttSetStyle(modal, 'display', 'flex');
                result.innerHTML = '';
                loadSurvey(form);
            };

            const closeAll = () => {
                ttSetStyle(overlay, 'display', 'none');
                ttSetStyle(modal, 'display', 'none');
                overlay.setAttribute('hidden', '');
                modal.setAttribute('hidden', '');
                form.innerHTML = '';
                result.innerHTML = '';
                ttSetStyle(result, 'display', 'none');
            };

            btn.addEventListener('click', open);
            overlay.addEventListener('click', closeAll);
            close.addEventListener('click', closeAll);

            form.addEventListener('submit', async (e) => {
                e.preventDefault();
                ttSetStyle(result, 'display', 'block');
                await submitSurvey(form, result, closeAll);
            });
        }

        async function loadSurvey(formEl) {
            formEl.innerHTML = '<div class="field"><em>Cargando encuesta…</em></div>';
            try {
                const enc = await fetch('/api/encuesta', {credentials: 'include'}).then(r => r.json());
                formEl.innerHTML = '';
                (enc.preguntas || []).sort((a, b) => (a.orden ?? 0) - (b.orden ?? 0)).forEach(p => {
                    if (p.habilitado === false) return;
                    const field = el('div', {className: 'field'});
                    const label = el('label', {htmlFor: `f_${p.key}`, textContent: p.texto || p.key});
                    field.appendChild(label);

                    if (Array.isArray(p.opciones) && p.opciones.length) {
                        const sel = el('select', {id: `f_${p.key}`, name: p.key});
                        sel.appendChild(el('option', {value: '', textContent: '-- Selecciona --'}));
                        p.opciones
                            .filter(o => o.habilitado !== false)
                            .sort((a, b) => (a.orden ?? 0) - (b.orden ?? 0))
                            .forEach(o => sel.appendChild(el('option', {
                                value: o.valor,
                                textContent: o.texto || o.valor
                            })));
                        field.appendChild(sel);
                    } else {
                        const k = String(p.key || '').toLowerCase();
                        if (k.includes('presupuesto') || k.includes('anio') || k.includes('años') || k === 'horizonte_anios') {
                            field.appendChild(el('input', {
                                type: 'number',
                                id: `f_${p.key}`,
                                name: p.key,
                                min: '0',
                                step: '1',
                                placeholder: 'Ingresa un número'
                            }));
                        } else if (k.includes('ray_tracing') || k.includes('rt') || k.startsWith('bool_')) {
                            const wrap = el('div', {className: 'field inline'});
                            const chk = el('input', {type: 'checkbox', id: `f_${p.key}`, name: p.key});
                            wrap.append(chk, el('label', {htmlFor: `f_${p.key}`, textContent: 'Sí'}));
                            field.appendChild(wrap);
                        } else {
                            field.appendChild(el('input', {
                                type: 'text',
                                id: `f_${p.key}`,
                                name: p.key,
                                placeholder: 'Tu respuesta…'
                            }));
                        }
                    }
                    formEl.appendChild(field);
                });

                if (!formEl.querySelector('#ttSurveySubmit')) {
                    const actions = el('div', {className: 'tt-modal-actions'});
                    const btn = el('button', {
                        id: 'ttSurveySubmit',
                        type: 'submit',
                        className: 'btn btn-primary',
                        textContent: 'Obtener Recomendación'
                    });
                    actions.appendChild(btn);
                    formEl.appendChild(actions);
                }

            } catch (e) {
                formEl.innerHTML = `<div class="field"><span class="csp-s-9876f9207bd5">No se pudo cargar la encuesta.</span></div>`;
                console.warn(e);
            }
        }

        function collectAnswers(formEl) {
            const data = {};
            formEl.querySelectorAll('input,select,textarea').forEach(inp => {
                const name = inp.name || inp.id;
                if (!name) return;
                if (inp.type === 'checkbox') {
                    data[name] = inp.checked ? 'true' : 'false';
                } else if (inp.type === 'number') {
                    data[name] = inp.value === '' ? null : Number(inp.value);
                } else {
                    data[name] = inp.value;
                }
            });
            return data;
        }

        async function submitSurvey(formEl, resultEl, closeModal) {
            const user = (() => {
                try {
                    const keys = ['user', 'usuario', 'usuarioLogueado', 'currentUser'];
                    for (const k of keys) {
                        const raw = sessionStorage.getItem(k) || localStorage.getItem(k);
                        if (raw) return JSON.parse(raw);
                    }
                } catch (_) {
                }
                return null;
            })();

            if (!user) {
                const next = encodeURIComponent(location.pathname);
                location.href = `/Login.html?next=${next}`;
                return;
            }

            const respuestas = collectAnswers(formEl);
            if ('presupuesto' in respuestas && (respuestas.presupuesto == null || respuestas.presupuesto === '')) {
                alert('Por favor ingresa tu presupuesto.');
                return;
            }

            // Ocultar el formulario y mostrar un mensaje de carga
            ttSetStyle(formEl, 'display', 'none');
            resultEl.innerHTML = '<div class="field csp-s-1874c1a8eb05"><em>Generando recomendación...</em></div>';
            ttSetStyle(resultEl, 'display', 'block');

            try {
                await generarSugerencia(respuestas, resultEl);
            } catch (error) {
                resultEl.innerHTML = `<p class="csp-s-e5ccf63a7303">Error al generar recomendación: ${error.message || error}</p>`;
                ttSetStyle(formEl, 'display', 'block');
            }
        }

        function capitalize(s = '') {
            return s ? s.charAt(0).toUpperCase() + s.slice(1) : '';
        }

        function categoriaDisplayName(it) {
            const cat = (it.categoria || '').toLowerCase();
            if (cat.startsWith('perif') && it.slot_key) {
                return `Periféricos (${capitalize(String(it.slot_key))})`;
            }
            return it.categoria || '';
        }


        // helpers para IDs/imagenes (se usan también por el botón “Armar”)
        function _bestImgId(it){
            const f = it || {};
            const flat = [
                f.imagenId, f.imagen_id, f.portadaId, f.portada_id,
                f.galeriaId, f.galeria_id, f.imgId, f.id_imagen
            ].filter(Boolean);
            if (flat.length) return flat[0];
            const nested = [
                f.imagen?.id, f.portada?.id, f.galeria?.id,
                f.producto?.imagenId, f.producto?.imagen_id,
                f.producto?.portadaId, f.producto?.portada_id
            ].filter(Boolean);
            return nested[0];
        }
        function _imgSrcFromId(id){
            return id ? `/api/galeria_v2/img/${id}` : '/img/placeholder.png';
        }
        async function _fetchProductAny(id){
            const tries = [
                `/api/productos/${id}`,
                `/api/productos/por-id?id=${id}`,
                `/api/busqueda/detalle/${id}`
            ];
            for (const url of tries){
                try{
                    const r = await fetch(url, {credentials:'include'});
                    if (r.ok) return await r.json();
                }catch{}
            }
            return null;
        }
        function _getPid(it){
            return it.producto_id ?? it.productoId ?? it.id_producto ?? it.idProducto ?? it.pid ?? it.id;
        }
        function _catKeyFromNombre(cat, slotKey){
            const c = String(cat||'').toLowerCase();
            if (c.startsWith('perif') || slotKey) return 'periferico';
            if (c.includes('cooler') || c.includes('refriger')) return 'cooling';
            if (c.includes('gpu') || c.includes('video') || c.includes('gráfica') || c.includes('grafica') || c.includes('tarjeta')) return 'gpu';
            if (c.includes('cpu')) return 'cpu';
            if (c.includes('ram') || c.includes('memoria')) return 'ram';
            if (c.includes('almacen') || c.includes('ssd') || c.includes('disco') || c.includes('hdd')) return 'storage';
            if (c.includes('mother') || c.includes('placa') || c.includes('mobo')) return 'mobo';
            if (c.includes('psu') || c.includes('fuente')) return 'psu';
            if (c.includes('case') || c.includes('cubierta') || c.includes('gabinete') || c.includes('chasis')) return 'case';
            return null;
        }

        async function _buildSelectionFromRecommendation(data){
            // Construye localStorage["pcbuilder.selection"] con {catKey:{id,nombre,img}}
            const sel = {};
            const items = Array.isArray(data?.items) ? data.items : [];
            for (const it of items){
                const key = _catKeyFromNombre(it.categoria, it.slot_key);
                if (!key || key === 'periferico') continue; // periféricos no ocupan slot
                const pid = _getPid(it);
                if (!pid) continue;
                const nombre = it.producto ?? it.nombre ?? '';

                // intenta sacar imagen del propio item; si no, pide detalle
                let imgId = _bestImgId(it);
                if (!imgId){
                    const d = await _fetchProductAny(pid);
                    imgId = _bestImgId(d);
                }
                const img = _imgSrcFromId(imgId);
                sel[key] = { id: pid, nombre, img };
            }
            return sel;
        }

        async function _armWithRecommendation(data){
            // 1) Construir selección para Armado (sin periféricos)
            const selection = await _buildSelectionFromRecommendation(data);
            localStorage.setItem('pcbuilder.selection', JSON.stringify(selection));

            // 2) Agregar TODOS los recomendados al carrito (incluye periféricos)
            const items = Array.isArray(data?.items) ? data.items : [];
            for (const it of items){
                const pid = _getPid(it);
                if (!pid) continue;
                // agrega 1 unidad de cada componente recomendado
                await addToCart(pid, 1);
            }

            // 3) Ir al Armador ya con los slots prellenados
            location.href = 'Armado.html';
        }

        // ===== REEMPLAZO de renderResultPcCompleta =====
        function renderResultPcCompleta(data, box) {
            if (!data || !Array.isArray(data.items) || data.items.length === 0) {
                box.innerHTML = `<p class="tt-muted-row">No se obtuvieron recomendaciones (ajusta tus respuestas o presupuesto).</p>`;
                return;
            }

            const rows = data.items.map(it => `
      <tr>
        <td>${(function catName(){
                const cat = (it.categoria || '').toLowerCase();
                if (cat.startsWith('perif') && it.slot_key){
                    const s = String(it.slot_key||''); return `Periféricos (${s.charAt(0).toUpperCase()+s.slice(1)})`;
                }
                return it.categoria || '';
            })()}</td>
        <td>${it.producto ?? ''}</td>
        <td>${Number((it.precio ?? it.precio_snap) ?? 0).toFixed(2)}</td>
      </tr>
    `).join('');

            const header = `
      <h4 class="csp-s-c45e284a36cd">PC sugerida #${data.sugerenciaId} — ${data.tipo}</h4>
      <p class="tt-muted-row">
        <b>Presupuesto:</b> ${data.presupuestoTotal ?? data.presupuesto ?? '—'}
        &nbsp;·&nbsp; <b>Total elegido:</b> ${data.totalPrecio ?? '—'}
        &nbsp;·&nbsp; <b>Componentes:</b> ${data.totalComponentes ?? 0}
      </p>
      <div id="nivel-actions" class="csp-s-2b74100d36f1"></div>
    `;

            box.innerHTML = `
      ${header}
      <div class="data-table">
        <table>
          <thead>
            <tr><th>Categoría</th><th>Producto</th><th>Precio</th></tr>
          </thead>
          <tbody>${rows}</tbody>
        </table>
      </div>

      <div class="csp-s-3d35fb5b82c4">
        <button id="btnArmarFromRec"
                type="button"
                class="ttq-btn csp-s-5e45bad8a0ae"
               >
          Armar
        </button>
      </div>
    `;

            // Botón “Subir nivel” se sigue gestionando fuera (renderConBotonNivel)
            const btnArmar = document.getElementById('btnArmarFromRec');
            if (btnArmar){
                btnArmar.onclick = () => _armWithRecommendation(data);
            }
        }

        // Exponer por si lo usas desde otro lado
        window.renderResultPcCompleta = renderResultPcCompleta;

        if (document.readyState === 'loading') {
            document.addEventListener('DOMContentLoaded', injectUI);
        } else {
            injectUI();
        }
    })();

