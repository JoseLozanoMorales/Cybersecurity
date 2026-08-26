// === Carga del producto + Agregar al carrito + Slider ===
    (function(){
      const params = new URLSearchParams(location.search);
      const id = params.get('id');

      const infoBox  = document.querySelector('.product-info');
      const mainImg  = document.getElementById('mainImg');
      const thumbsEl = document.getElementById('thumbs');

      const btnPrev  = document.querySelector('.tt-gallery .prev');
      const btnNext  = document.querySelector('.tt-gallery .next');

      const btnAdd   = document.getElementById('btnAddCart');
      const qtyInput = document.getElementById('qtyInput');
      const okMsg    = document.getElementById('addCartMsg');
      const errMsg   = document.getElementById('addCartErr');

      let gal = [];     // [{id, es_portada, ...}]
      let idx = 0;

      function money(n){
        const x = Number(n);
        return isFinite(x) ? `$${x.toFixed(2)}` : '$0.00';
      }

      function imgUrl(gid){
        return gid ? `/api/galeria_v2/img/${gid}` : '/img/placeholder.png';
      }

      function show(i){
        if (!gal.length){
          mainImg.src = '/img/placeholder.png';
          return;
        }
        idx = (i + gal.length) % gal.length;
        mainImg.src = imgUrl(gal[idx].id);
        // marcar miniatura activa
        [...thumbsEl.querySelectorAll('img')].forEach((im, k)=>{
          im.classList.toggle('active', k === idx);
        });
      }

      function buildThumbs(){
        thumbsEl.innerHTML = gal.map(g => `
          <img src="${imgUrl(g.id)}" alt="miniatura">
        `).join('');
        [...thumbsEl.querySelectorAll('img')].forEach((im, k)=>{
          im.addEventListener('click', ()=> show(k));
        });
      }

      // Swipe en móvil
      (function attachSwipe(el){
        let sx = 0;
        el.addEventListener('touchstart', e => sx = e.touches[0].clientX, {passive:true});
        el.addEventListener('touchend', e => {
          const dx = (e.changedTouches[0].clientX - sx);
          if (Math.abs(dx) > 40) show(idx + (dx < 0 ? 1 : -1));
        }, {passive:true});
      })(document.querySelector('.tt-gallery'));

      // Teclas ← →
      document.addEventListener('keydown', e=>{
        if(e.key === 'ArrowLeft')  show(idx - 1);
        if(e.key === 'ArrowRight') show(idx + 1);
      });

      // Botones
      btnPrev?.addEventListener('click', ()=> show(idx - 1));
      btnNext?.addEventListener('click', ()=> show(idx + 1));

      async function tryPost(url, body){
        return fetch(url, {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          credentials: 'include',
          body: JSON.stringify(body)
        });
      }

      function hasActiveSessionCart(){
        const raw = sessionStorage.getItem('user') || localStorage.getItem('user');
        let ok = false;
        try { ok = !!(raw && JSON.parse(raw)); } catch { ok = !!raw; }
        return ok || !!localStorage.getItem('token') || !!localStorage.getItem('refreshToken');
      }

      async function addToCart(){
        if(!id){ alert('Falta parámetro id'); return; }
        const cantidad = Math.max(1, parseInt(qtyInput.value || '1', 10));
        btnAdd.disabled = true;
        okMsg.classList.add('d-none');
        errMsg.classList.add('d-none');
        try {
          if (hasActiveSessionCart()){
            const res = await tryPost('/api/carrito/items', { productoId: Number(id), cantidad });
            if (!res.ok) throw new Error('backend_error');
          } else {
            const key = 'cart';
            const cart = JSON.parse(localStorage.getItem(key) || '[]');
            const found = cart.find(x => String(x.productoId) === String(id));
            if (found) found.cantidad += cantidad; else cart.push({ productoId: Number(id), cantidad });
            localStorage.setItem(key, JSON.stringify(cart));
          }
          okMsg.classList.remove('d-none');
          setTimeout(()=> okMsg.classList.add('d-none'), 2500);
        } catch (e){
          console.error(e);
          errMsg.classList.remove('d-none');
          setTimeout(()=> errMsg.classList.add('d-none'), 3000);
        } finally {
          btnAdd.disabled = false;
        }
      }

      async function load(){
        if(!id){ alert('Falta parámetro id'); return; }
        try{
          const res = await fetch(`/api/productos/${encodeURIComponent(id)}`, { credentials:'include' });
          if(!res.ok){ throw new Error('No se pudo cargar el producto'); }
          const p = await res.json();

          // Info principal
          const items = infoBox.querySelectorAll('.product-info-item');
          if(items[0]) items[0].textContent = p.nombre || '';
          if(items[1]) items[1].textContent = money(p.precio ?? p.preciounitario);
          if(items[2]) items[2].textContent = p.marca  || '';

          // --- Enlace a "Características"
      const specLink = document.getElementById('specLink');

      async function resolveSpecsLink(prod) {
        // 1) intenta con campos que pueda traer /api/productos/{id}
        let raw = (
          prod.enlace ??
          prod.link ??
          prod.enlace_producto ??
          prod.enlaceProducto ??
          prod.url_especificaciones ??
          prod.url ??
          prod.href ??
          prod.caracteristicas_url ??
          prod.enlace_especificaciones ??
          prod.enlaceCaracteristicas
        );

        // 2) si sigue vacío, consulta el endpoint que usa el admin
        if (!raw) {
          try {
            const r2 = await fetch(`/api/sp/productos/${encodeURIComponent(id)}/editar`, { credentials:'include' });
            if (r2.ok) {
              const d2 = await r2.json();
              raw = d2.enlace ?? d2.url ?? null;
            }
          } catch {}
        }

        if (typeof raw === 'string') raw = raw.trim();
        if (raw && !/^https?:\/\//i.test(raw)) raw = 'https://' + raw.replace(/^\/\//, '');
        return raw || null;
      }

      if (specLink) {
        const url = await resolveSpecsLink(p);
        if (url) {
          specLink.href = url;
          specLink.target = '_blank';
          specLink.rel = 'noopener';
          ttSetStyle(specLink, 'cursor', 'pointer');
          specLink.classList.remove('disabled');
        } else {
          // Sin enlace: no navegamos
          specLink.removeAttribute('href');
          specLink.addEventListener('click', (e)=>{
            e.preventDefault();
            alert('Este producto no tiene enlace de características.');
          }, { once:true });
          ttSetStyle(specLink, 'opacity', '.7');
          ttSetStyle(specLink, 'cursor', 'not-allowed');
          specLink.classList.add('disabled');
        }
      }


          // Galería (orden: portada primero; luego por posicion_galeria, luego id)
          gal = Array.isArray(p.galeria) ? [...p.galeria] : [];
          gal.sort((a,b)=>
            (b.es_portada?1:0)-(a.es_portada?1:0) ||
            (a.posicion_galeria??9999)-(b.posicion_galeria??9999) ||
            (b.id - a.id)
          );
          if (!gal.length && p.imagenId){ gal = [{id:p.imagenId}]; }

          buildThumbs();
          show(0);

          // Fallback de imagen principal
          mainImg.onerror = ()=> { mainImg.onerror=null; mainImg.src='/img/placeholder.png'; };

        }catch(err){
          console.error(err);
          alert('No se pudo cargar la información del producto');
        }
      }

      if (btnAdd) btnAdd.addEventListener('click', addToCart);
      document.addEventListener('DOMContentLoaded', load);
    })();

