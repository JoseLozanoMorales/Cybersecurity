(async function loadCategories(){
        const grid = document.getElementById('categoriesGrid');
        if (!grid) return;

        try {
            const res  = await fetch('/api/categorias', { credentials:'include' });
            const cats = await res.json();

            grid.innerHTML = cats.map(c => `
          <button class="category-card"
                  data-id="${c.id}"
                  data-nombre="${c.nombre}">
            ${c.nombre}
          </button>
        `).join('');

            grid.addEventListener('click', (e) => {
                const card = e.target.closest('.category-card');
                if (!card) return;
                const id = card.dataset.id;
                const nombre = encodeURIComponent(card.dataset.nombre);
                location.href = `Busqueda.html?cat=${id}&nombre=${nombre}`;
            });
        } catch (err){
            console.error('Error cargando categorías', err);
            grid.innerHTML = `<p class="text-danger">No se pudieron cargar las categorías.</p>`;
        }
    })();

