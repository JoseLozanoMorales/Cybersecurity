// ====== Búsqueda por categoría + botón de carrito (icono-only) ======
      (function(){
        const params     = new URLSearchParams(location.search);
        const catId      = params.get('cat');
        const catNombre  = params.get('nombre') ? decodeURIComponent(params.get('nombre')) : null;

        const titleEl    = document.getElementById('resultsTitle');
        const container  = document.getElementById('resultsContainer');
        const noResults  = document.getElementById('noResults');
        const searchBox  = document.getElementById('searchInput');
        const snack      = document.getElementById('snack');

        let current = [];

        if (catNombre) titleEl.textContent = `Categoría: ${catNombre}`;

        function money(x){
          const n = Number(x);
          return isFinite(n) ? `$${n.toFixed(2)}` : '';
        }

        function showSnack(){
          snack.classList.add('show');
          setTimeout(()=> snack.classList.remove('show'), 1600);
        }

        function hasActiveSessionCart(){
          const raw = sessionStorage.getItem('user') || localStorage.getItem('user');
          let ok = false;
          try { ok = !!(raw && JSON.parse(raw)); } catch { ok = !!raw; }
          return ok || !!localStorage.getItem('token') || !!localStorage.getItem('refreshToken');
        }

        async function tryPost(url, body){
          return fetch(url, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            credentials: 'include',
            body: JSON.stringify(body)
          });
        }

        // === Agregar al carrito ===
        window.addToCart = async function(ev, productoId){
          ev.stopPropagation();
          try{
            if (hasActiveSessionCart()){
              let res = await tryPost('/api/carrito/items', { productoId: Number(productoId), cantidad: 1 });
              if(!res.ok){
                res = await tryPost('/api/carrito/agregar', { productoId: Number(productoId), cantidad: 1 });
              }
              if(!res.ok) throw new Error('backend_error');
            } else {
              const key = 'cart';
              const cart = JSON.parse(localStorage.getItem(key) || '[]');
              const f = cart.find(x => String(x.productoId) === String(productoId));
              if (f) f.cantidad += 1; else cart.push({ productoId: Number(productoId), cantidad: 1 });
              localStorage.setItem(key, JSON.stringify(cart));
            }
            showSnack();
          } catch(e){
            console.error('No se pudo agregar al carrito', e);
            alert('No se pudo agregar al carrito. Inténtalo de nuevo.');
          }
        };

        // === Render cards con botón icono ===
        function render(list){
          if (!Array.isArray(list)) list = [];

          // 🔒 además de la BD, filtramos en front por seguridad
          list = list
            .map(p => ({
              id: p.id ?? p.productoId,
              nombre: p.nombre,
              precio: Number(p.precio ?? p.preciounitario ?? 0),
              marca: p.marca,
              imagenId: p.imagenId ?? p.imagen_id
            }))
            .filter(p => p.precio > 0);

          if (list.length === 0){
            container.classList.add('d-none');
            noResults.classList.remove('d-none');
            return;
          }
          container.classList.remove('d-none');
          noResults.classList.add('d-none');

          window.viewProduct = (id) => {
            location.href = `informacion_producto.html?id=${encodeURIComponent(id)}`;
          };

          container.innerHTML = list.map(p => {
            const imgSrc = p.imagenId
              ? `/api/galeria_v2/img/${p.imagenId}`           // 👈 endpoint correcto
              : '/img/placeholder.png';

            return `
              <div class="product-card" data-product-id="${p.id}">
                <img class="product-image"
                     src="${imgSrc}"
                     alt="${p.nombre}">
                <div class="product-details">
                  <div class="product-name">${p.nombre}</div>
                  <div class="product-price">${money(p.precio)}</div>
                  <div class="product-brand">${p.marca || ''}</div>
                </div>
                <button class="btn btn-primary add-cart-btn"
                        title="Agregar al carrito"
                        aria-label="Agregar al carrito"
                        data-action="add-cart" data-product-id="${p.id}">
                  <i class="bi bi-cart-plus"></i>
                </button>
              </div>
            `;
          }).join('');
        }

        container.addEventListener('click', event => {
          const addButton = event.target.closest('[data-action="add-cart"]');
          if (addButton) {
            event.stopPropagation();
            addToCart(event, Number(addButton.dataset.productId));
            return;
          }
          const card = event.target.closest('.product-card[data-product-id]');
          if (card) viewProduct(Number(card.dataset.productId));
        });
        container.addEventListener('error', event => {
          if (event.target instanceof HTMLImageElement) event.target.src = '/img/placeholder.png';
        }, true);

        async function load(){
          try {
            // Puedes usar el nuevo endpoint o el que ya tenías:
            // const url = `/api/busqueda/categoria/${encodeURIComponent(catId || '')}`;
            const url = `/api/productos/por-categoria?categoriaId=${encodeURIComponent(catId || '')}`;
            const res = await fetch(url, { credentials:'include' });
            const data = await res.json();
            current = Array.isArray(data) ? data : (Array.isArray(data?.content) ? data.content : []);
            render(current);
          } catch (e){
            console.error(e);
            container.innerHTML = `<p class="text-danger">Error al cargar productos.</p>`;
          }
        }

        if (searchBox){
          searchBox.addEventListener('input', () => {
            const term = searchBox.value.trim().toLowerCase();
            const filtered = term === '' ? current :
              current.filter(p =>
                (p.nombre || '').toLowerCase().includes(term) ||
                (p.marca  || '').toLowerCase().includes(term)
              );
            render(filtered);
          });
        }

        document.addEventListener('DOMContentLoaded', load);
      })();
  
