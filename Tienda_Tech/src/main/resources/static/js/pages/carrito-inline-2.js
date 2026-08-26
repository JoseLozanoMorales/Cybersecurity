(function(){
      const listEl   = document.getElementById('cart-items');
      const emptyEl  = document.getElementById('empty-cart');
      const fmt      = n => `$${Number(n||0).toFixed(2)}`;
      const ENVIO    = 15.00;
      let ITEMS = [];

      async function fetchJSON(url, opt){
        const r = await fetch(url, Object.assign({credentials:'include'}, opt||{}));
        if (!r.ok) throw new Error(r.status + ' ' + r.statusText);
        return r.json();
      }

      async function loadCart(){
        try{
          ITEMS = await fetchJSON('/api/carrito/items');
          render(ITEMS);
          await updateSummary();
        }catch(err){
          console.error('No se pudo cargar el carrito', err);
          listEl.innerHTML = '<p>Error al cargar el carrito.</p>';
        }
      }

        // ---------- helpers NUEVOS ----------
        function esc(s){
            return String(s??'').replace(/[&<>"']/g, m=>({
                '&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'
            }[m]));
        }
        function bestImgId(it){
            // planos
            const flat = it || {};
            const ids = [
                flat.imagenId, flat.imagen_id,
                flat.portadaId, flat.portada_id,
                flat.galeriaId, flat.galeria_id,
                flat.imgId, flat.id_imagen, flat.producto_galeria_id
            ].filter(Boolean);
            if (ids.length) return ids[0];

            // anidados comunes
            const nested = [
                flat.imagen?.id, flat.portada?.id, flat.galeria?.id,
                flat.producto?.imagenId, flat.producto?.imagen_id,
                flat.producto?.portadaId, flat.producto?.portada_id
            ].filter(Boolean);
            return nested[0];
        }
        function buildImgSrcFromId(id){
            return id ? `/api/galeria_v2/img/${id}` : '/img/placeholder.png';
        }
        async function fetchProductAny(id){
            const tries = [
                `/api/productos/${id}`,
                `/api/productos/por-id?id=${id}`,
                `/api/busqueda/detalle/${id}`
            ];
            for (const url of tries){
                try{
                    const r = await fetch(url, { credentials:'include' });
                    if (r.ok) return await r.json();
                }catch{}
            }
            return null;
        }
        async function hydrateItemImage(pid, it, imgEl){
            // 1) si el item ya trae imagen
            const direct = bestImgId(it);
            if (direct){
                imgEl.src = buildImgSrcFromId(direct);
                return;
            }
            // 2) buscar detalle del producto y tomar su imagen
            const d = await fetchProductAny(pid);
            const id = bestImgId(d);
            imgEl.src = buildImgSrcFromId(id);
        }

        // ---------- REEMPLAZA SOLO ESTA FUNCIÓN render(items) ----------
        function render(items){
            listEl.innerHTML = '';
            if (!items || items.length === 0){
                ttSetStyle(listEl, 'display', 'none');
                ttSetStyle(emptyEl, 'display', 'block');
                return;
            }
            ttSetStyle(listEl, 'display', '');
            ttSetStyle(emptyEl, 'display', 'none');

            items.forEach(it=>{
                const row = document.createElement('div');
                row.className = 'cart-item';
                const pid = it.producto_id ?? it.productoId ?? it.id;
                row.dataset.pid = pid;

                row.innerHTML = `
      <div class="row align-items-center">
        <div class="col-3 col-md-2">
          <div class="product-image-small">
            <img alt="${esc(it.nombre||'Producto')}" data-pid="${pid}">
          </div>
        </div>
        <div class="col-6 col-md-5">
            <h6 class="mb-1">${esc(it.nombre)}</h6>
            <small class="text-muted d-block">IVA: ${Number(it.iva_porcentaje||0).toFixed(2)}%</small>
            <small class="text-muted">Precio sin/IVA: $${Number(it.precio||0).toFixed(2)}</small>
        </div>
        <div class="col-3 col-md-2">
          <div class="quantity-controls">
            <div class="quantity-btn btn-minus"><i class="bi bi-dash"></i></div>
            <input type="number" class="quantity-input" min="1" value="${it.cantidad}">
            <div class="quantity-btn btn-plus"><i class="bi bi-plus"></i></div>
          </div>
        </div>
        <div class="col-6 col-md-2 mt-2 mt-md-0">
          <div class="fw-bold">
            $${(it.precio_con_iva ?? (Number(it.precio||0)*(1+(Number(it.iva_porcentaje||0)/100)))).toFixed(2)}
          </div>
        </div>
        <div class="col-6 col-md-1 mt-2 mt-md-0 text-end">
          <i class="bi bi-trash remove-btn"></i>
        </div>
      </div>
    `;

                // wire qty / remove
                const qtyInput = row.querySelector('.quantity-input');
                row.querySelector('.btn-minus').onclick = () =>
                    setQty(pid, Math.max(1, Number(qtyInput.value)-1));
                row.querySelector('.btn-plus').onclick  = () =>
                    setQty(pid, Number(qtyInput.value)+1);
                qtyInput.onchange = () =>
                    setQty(pid, Math.max(1, Number(qtyInput.value)||1));
                row.querySelector('.remove-btn').onclick = () => removeItem(pid);

                // imagen: placeholder inmediato + carga real
                const imgEl = row.querySelector('img[data-pid]');
                imgEl.src = '/img/placeholder.png';
                imgEl.addEventListener('error', ()=> { imgEl.src = '/img/placeholder.png'; });
                hydrateItemImage(pid, it, imgEl);

                listEl.appendChild(row);
            });
        }
      /*function render(items){
        listEl.innerHTML = '';
        if (!items || items.length === 0){
          ttSetStyle(listEl, 'display', 'none');
          ttSetStyle(emptyEl, 'display', 'block');
          return;
        }
        ttSetStyle(listEl, 'display', '');
        ttSetStyle(emptyEl, 'display', 'none');

        items.forEach(it=>{
          const row = document.createElement('div');
          row.className = 'cart-item';
          row.dataset.pid = it.producto_id;

          row.innerHTML = `
            <div class="row align-items-center">
              <div class="col-3 col-md-2">
                <div class="product-image-small">Imagen</div>
              </div>
              <div class="col-6 col-md-5">
                  <h6 class="mb-1">${it.nombre}</h6>
                  <small class="text-muted d-block">IVA: ${Number(it.iva_porcentaje||0).toFixed(2)}%</small>
                  <small class="text-muted">Precio sin/IVA: ${fmt(it.precio)}</small>
              </div>
              <div class="col-3 col-md-2">
                <div class="quantity-controls">
                  <div class="quantity-btn btn-minus"><i class="bi bi-dash"></i></div>
                  <input type="number" class="quantity-input" min="1" value="${it.cantidad}">
                  <div class="quantity-btn btn-plus"><i class="bi bi-plus"></i></div>
                </div>
              </div>
              <div class="col-6 col-md-2 mt-2 mt-md-0">
                <div class="fw-bold">${fmt(it.precio_con_iva ?? (it.precio*(1+(it.iva_porcentaje||0)/100)))}</div>
              </div>
              <div class="col-6 col-md-1 mt-2 mt-md-0 text-end">
                <i class="bi bi-trash remove-btn"></i>
              </div>
            </div>
          `;

          const qtyInput = row.querySelector('.quantity-input');
          row.querySelector('.btn-minus').onclick = () =>
            setQty(it.producto_id, Math.max(1, Number(qtyInput.value)-1));
          row.querySelector('.btn-plus').onclick  = () =>
            setQty(it.producto_id, Number(qtyInput.value)+1);
          qtyInput.onchange = () =>
            setQty(it.producto_id, Math.max(1, Number(qtyInput.value)||1));
          row.querySelector('.remove-btn').onclick = () => removeItem(it.producto_id);

          listEl.appendChild(row);
        });
      }*/

      async function setQty(productoId, cantidad){
        await fetchJSON(`/api/carrito/items/${encodeURIComponent(productoId)}`, {
          method:'PATCH',
          headers:{'Content-Type':'application/json'},
          body: JSON.stringify({cantidad})
        });
        await loadCart();
      }

      async function removeItem(productoId){
        if (!confirm('¿Eliminar este producto del carrito?')) return;
        await fetchJSON(`/api/carrito/items/${encodeURIComponent(productoId)}`, { method:'DELETE' });
        await loadCart();
      }

      async function updateSummary(){
        const s = await fetchJSON('/api/carrito/resumen');
        const sub = Number(s.subtotal||0);
        const imp = Number(s.impuestos||0);
        const tot = Number(s.total||0);

        const count = (ITEMS||[]).reduce((acc,it)=>acc + Number(it.cantidad||0), 0);
        const subtotalLabel = document.getElementById('subtotal').parentElement.querySelector('span:first-child');
        subtotalLabel.textContent = `Subtotal (${count} producto${count===1?'':'s'}):`;

        document.getElementById('subtotal').textContent  = fmt(sub);
        document.getElementById('taxes').textContent     = fmt(imp);
<!--        document.getElementById('shipping').textContent  = fmt(ENVIO);-->
        document.getElementById('total').textContent     = fmt(tot);
      }

      document.addEventListener('DOMContentLoaded', loadCart);
    })();

