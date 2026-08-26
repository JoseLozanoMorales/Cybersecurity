(() => {
        // ====== Config ======
        const CAT_TO_ID = { storage:1, cpu:2, cooling:3, case:4, psu:5, gpu:6, ram:7, mobo:8, periferico:9 };
        const SLOTABLE = new Set(Object.keys(CAT_TO_ID).filter(k => k !== 'periferico'));
        const SAVE_KEY = 'pcbuilder.selection';

        const API = {
            byCatV1:  id => `/api/productos/por-categoria?categoriaId=${encodeURIComponent(id)}`,
            byCatV0:  id => `/api/busqueda/categoria/${id}`,
            img:      galId => `/api/galeria_v2/img/${galId}`,
            detailUrl: id => `informacion_producto.html?id=${encodeURIComponent(id)}&from=builder`
        };

        // ====== Snack ======
        const snack = (() => {
            let el = document.getElementById('snack');
            if (!el) {
                el = document.createElement('div'); el.id = 'snack';
                el.textContent = 'Producto añadido al carrito';
                ttSetStyles(el, {position:'fixed',bottom:'18px',left:'50%',transform:'translateX(-50%)',
                    background:'#1b5e20',color:'#fff',padding:'10px 16px',borderRadius:'8px',
                    boxShadow:'0 6px 20px rgba(0,0,0,.25)',opacity:'0',pointerEvents:'none',
                    transition:'opacity .25s ease',zIndex:'9999'});
                document.body.appendChild(el);
            }
            return el;
        })();
        function showSnack(msg){
            if (msg) snack.textContent = msg;
            ttSetStyle(snack, 'opacity', '1'); setTimeout(()=> ttSetStyle(snack, 'opacity', '0'), 1600);
        }

        // ====== Helpers sesión/cart ======
        function hasActiveSession(){
            const raw = sessionStorage.getItem('user') || localStorage.getItem('user');
            let ok = false; try { ok = !!(raw && JSON.parse(raw)); } catch { ok = !!raw; }
            return ok || !!localStorage.getItem('token') || !!localStorage.getItem('refreshToken');
        }
        async function tryPost(url, body){
            return fetch(url, { method:'POST', headers:{'Content-Type':'application/json'}, credentials:'include', body: JSON.stringify(body) });
        }
        async function addToCart(productoId){
            try{
                if (hasActiveSession()){
                    let res = await tryPost('/api/carrito/items',  { productoId:Number(productoId), cantidad:1 });
                    if(!res.ok) res = await tryPost('/api/carrito/agregar', { productoId:Number(productoId), cantidad:1 });
                    if(!res.ok) throw new Error('backend_error');
                } else {
                    const key = 'cart';
                    const cart = JSON.parse(localStorage.getItem(key) || '[]');
                    const f = cart.find(x => String(x.productoId) === String(productoId));
                    if (f) f.cantidad += 1; else cart.push({ productoId:Number(productoId), cantidad:1 });
                    localStorage.setItem(key, JSON.stringify(cart));
                }
                showSnack('Producto añadido al carrito');
                return true;
            } catch(e){
                console.error('No se pudo agregar al carrito', e);
                alert('No se pudo agregar al carrito. Inténtalo de nuevo.');
                return false;
            }
        }
        async function removeFromCart(productoId){
            try{
                if (hasActiveSession()){
                    const r = await fetch(`/api/carrito/items/${encodeURIComponent(productoId)}`, { method:'DELETE', credentials:'include' });
                    if(!r.ok) throw new Error('backend_delete_fail');
                } else {
                    const key = 'cart';
                    const cart = JSON.parse(localStorage.getItem(key) || '[]');
                    const idx = cart.findIndex(x => String(x.productoId) === String(productoId));
                    if (idx >= 0) { cart.splice(idx,1); localStorage.setItem(key, JSON.stringify(cart)); }
                }
                showSnack('Producto eliminado');
                return true;
            } catch(e){
                console.error('No se pudo eliminar del carrito', e);
                alert('No se pudo eliminar del carrito. Inténtalo de nuevo.');
                return false;
            }
        }

        // ====== Estado ======
        const grid1 = document.querySelector('.selected-grid');
        const grid2 = document.querySelector('.selected-grid-second');
        const catItems = [...document.querySelectorAll('.components-section .component-item')];

        // guardamos los src originales para poder restaurar
        const defaultTopSrc = {};
        const topImgs = {};
        catItems.forEach(el => {
            const key = el.dataset.cat;
            const img = el.querySelector('img');
            topImgs[key] = img;
            defaultTopSrc[key] = img?.getAttribute('src') || '';
        });

        // selección persistida
        let selection = {};
        try { selection = JSON.parse(localStorage.getItem(SAVE_KEY) || '{}') || {}; } catch { selection = {}; }

        // ====== Botón "−" para quitar selección del slot ======
        function ensureRemoveButtonFor(el){
            const key = el.dataset.cat;
            if (!SLOTABLE.has(key)) return; // periféricos no tienen slot
            let btn = el.querySelector('.slot-remove');
            if (!btn) {
                btn = document.createElement('button');
                btn.className = 'slot-remove';
                btn.type = 'button';
                btn.title = 'Quitar selección';
                btn.setAttribute('aria-label','Quitar selección');
                btn.textContent = '−';
                // estilo mínimo inline para que funcione sin tocar tu CSS
                ttSetStyles(btn, {
                    marginTop:'6px', display:'none', background:'#e53935', color:'#fff',
                    border:'none', width:'28px', height:'28px', borderRadius:'8px',
                    fontSize:'18px', lineHeight:'1', cursor:'pointer'
                });
                btn.addEventListener('click', async (ev) => {
                    ev.stopPropagation();
                    await clearSlot(key);
                });
                el.appendChild(btn);
            }
            // mostrar solo si hay selección
            ttSetStyle(btn, 'display', selection[key]?.id ? 'inline-flex' : 'none');
            ttSetStyle(btn, 'alignItems', 'center'); ttSetStyle(btn, 'justifyContent', 'center');
        }

        function refreshAllRemoveButtons(){
            catItems.forEach(ensureRemoveButtonFor);
        }

        function setTopImage(catKey, src, alt, id) {
            const img = topImgs[catKey]; if (!img) return;
            img.src = src;
            if (alt) img.alt = alt;
            const item = img.closest('.component-item');
            if (item) { item.dataset.selectedId = id || ''; item.classList.add('selected'); }
            refreshAllRemoveButtons();
        }

        async function clearSlot(catKey){
            const sel = selection[catKey];
            if (!sel) return;
            const ok = await removeFromCart(sel.id);
            if (!ok) return;

            // Restaurar icono original
            const img = topImgs[catKey];
            if (img) img.src = defaultTopSrc[catKey] || img.src;

            // limpiar marcas y persistencia
            const item = img?.closest('.component-item');
            if (item) { item.dataset.selectedId=''; item.classList.remove('selected'); }
            delete selection[catKey];
            localStorage.setItem(SAVE_KEY, JSON.stringify(selection));
            refreshAllRemoveButtons();
        }

        // Restaurar selección al cargar
        Object.entries(selection).forEach(([k,v]) => {
            if (SLOTABLE.has(k) && v?.img) setTopImage(k, v.img, v.nombre, v.id);
        });
        refreshAllRemoveButtons();

        // ====== Carga por categoría ======
        async function fetchByCat(catId){
            try {
                let r = await fetch(API.byCatV1(catId), { credentials:'include' });
                if(!r.ok) throw new Error(r.status);
                return await r.json();
            } catch {
                const r2 = await fetch(API.byCatV0(catId), { credentials:'include' });
                if(!r2.ok) throw new Error(r2.status);
                return await r2.json();
            }
        }

        async function loadCat(catKey){
            const catId = CAT_TO_ID[catKey];
            if(!catId){ console.warn('Falta mapeo para', catKey); return; }
            try{
                const raw = await fetchByCat(catId);
                const list = (Array.isArray(raw) ? raw : (Array.isArray(raw?.content) ? raw.content : []))
                    .map(p => ({
                        id: p.id ?? p.productoId,
                        nombre: p.nombre ?? 'Producto',
                        precio: Number(p.precio ?? p.preciounitario ?? 0),
                        marca: p.marca ?? '',
                        imgId: p.imagenId ?? p.imagen_id ?? p.portadaId ?? p.portada_id
                    }))
                    .filter(p => p.id && p.precio > 0);
                render(list.slice(0,12), catKey);
            }catch(e){
                console.error('Error cargando productos:', e);
                render([], catKey);
            }
        }

        function render(list, catKey){
            const nodes = list.map(p => {
                const src = p.imgId ? API.img(p.imgId) : '/img/placeholder.png';
                return `
        <div class="selected-item" data-id="${p.id}" data-src="${esc(src)}" data-cat="${esc(catKey)}" title="${esc(p.nombre)}">
          <img src="${src}" class="component-icon" alt="${esc(p.nombre)}">
          <div class="buttons-container">
            <button class="info-btn" data-action="info"><span class="info-text">Info</span></button>
            <button class="cart-btn" data-action="cart" title="Agregar al carrito">🛒</button>
          </div>
          <div class="product-name">${esc(p.nombre)}</div>
          <div class="product-price">${money(p.precio)}</div>
          <div class="product-brand csp-s-28dc79fe4f3d">${esc(p.marca)}</div>
        </div>`;
            });
            while (nodes.length < 12) nodes.push('<div class="selected-item"></div>');
            grid1.innerHTML = nodes.slice(0,6).join('');
            grid2.innerHTML = nodes.slice(6).join('');

            const cards = [...grid1.querySelectorAll('.selected-item[data-id]'),
                ...grid2.querySelectorAll('.selected-item[data-id]')];

            cards.forEach(card => {
                const id   = card.dataset.id;
                const src  = card.dataset.src;
                const cat  = card.dataset.cat;
                const name = card.querySelector('.product-name')?.textContent?.trim() || 'Producto';

                const go = () => location.href = API.detailUrl(id);
                card.querySelector('img')?.addEventListener('click', go);
                card.querySelector('.product-name')?.addEventListener('click', go);
                card.querySelector('[data-action="info"]')?.addEventListener('click', ev => { ev.stopPropagation(); go(); });

                card.querySelector('[data-action="cart"]')?.addEventListener('click', async ev => {
                    ev.stopPropagation();
                    const ok = await addToCart(id);
                    if (!ok) return;

                    if (SLOTABLE.has(cat)) {
                        setTopImage(cat, src, name, id);
                        selection[cat] = { id, img: src, nombre: name };
                        localStorage.setItem(SAVE_KEY, JSON.stringify(selection));
                    }
                });
            });
        }

        // Accesibilidad + navegación por categorías
        catItems.forEach(el => {
            el.setAttribute('tabindex','0');
            el.setAttribute('role','button');
            el.addEventListener('click', () => loadCat(el.dataset.cat));
            el.addEventListener('keydown', ev => { if(ev.key==='Enter'||ev.key===' '){ ev.preventDefault(); el.click(); } });
        });

        // (Opcional) cargar una categoría por defecto
        // loadCat('cpu');

        // ====== Utils ======
        function money(n){ try{ return new Intl.NumberFormat('es-EC',{style:'currency',currency:'USD'}).format(+n||0); }catch{ return `$${(+n||0).toFixed(2)}`; } }
        function esc(s){ return String(s??'').replace(/[&<>"']/g, m=>({ '&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;' }[m])); }
    })();
