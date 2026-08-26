(function(){
        const RECENTES_API = '/api/productos/recientes-menu';
        const IMG_URL      = id => `/api/galeria/${id}/contenido`;
        const PRODUCT_URL  = pid => `/informacion_producto.html?id=${pid}`;

        const _fetch = (window.authFetch || window.fetch).bind(window);

        const $section = document.getElementById('slider-nuevos');
        const $track   = document.getElementById('ttSliderTrack');
        const $dots    = document.getElementById('ttSliderDots');
        const $prev    = $section?.querySelector('.tt-slider__nav--prev');
        const $next    = $section?.querySelector('.tt-slider__nav--next');

        if (!$section || !$track || !$dots || !$prev || !$next) return;

        const fmtPrecio = n => {
            try { return new Intl.NumberFormat('es-EC',{style:'currency',currency:'USD',maximumFractionDigits:2}).format(n); }
            catch { return `${Number(n||0).toFixed(2)}`; }
        };
        const gidOf = it => it?.galeriaId ?? it?.galeria_id ?? it?.galeria?.id ?? null;

        function buildSlide(item){
            const gid = gidOf(item);
            const li  = document.createElement('li');
            li.className = 'tt-slider__slide';
            li.innerHTML = `
          <article class="tt-card">
            <a class="tt-card__imgwrap" href="${PRODUCT_URL(item.productoId)}" aria-label="${item.nombre ?? 'Producto'}">
              <img loading="lazy" decoding="async"
                   src="${IMG_URL(gid)}"
                   alt="${item.nombre ?? 'Producto'}">
            </a>
            <div class="tt-card__info">
              <div class="tt-card__title" title="${item.nombre ?? ''}">${item.nombre ?? ''}</div>
              <div class="tt-card__price">${fmtPrecio(item.precio)}</div>
            </div>
          </article>`;
            return li;
        }

        let page = 0, pages = 1;

        function computeLayout(){
            const total = $track.children.length;
            pages = Math.max(1, total);
            page  = Math.min(page, pages - 1);
            update();
        }

        function update(){
            const viewport = $section.querySelector('.tt-slider__viewport');
            const width = viewport.clientWidth || 0;
            ttSetStyle($track, 'transform', `translateX(${-page * width}px)`);

            $prev.disabled = (page === 0);
            $next.disabled = (page >= pages - 1);

            $dots.innerHTML = '';
            for (let i=0;i<pages;i++){
                const d = document.createElement('span');
                d.className = 'tt-slider__dot' + (i===page ? ' is-active' : '');
                d.addEventListener('click', ()=>{ page = i; update(); });
                $dots.appendChild(d);
            }
        }

        $prev.addEventListener('click', ()=>{ if (page>0)       { page--; update(); } });
        $next.addEventListener('click', ()=>{ if (page<pages-1) { page++; update(); } });
        window.addEventListener('resize', computeLayout);

        async function init(){
            try{
                const res = await _fetch(RECENTES_API, { headers:{ 'Accept':'application/json' } });
                if (!res.ok) throw new Error(`HTTP ${res.status}`);
                const items = await res.json();

                const candidates = (items || [])
                    .map(it => ({ it, gid: gidOf(it) }))
                    .filter(x => !!x.gid);

                const checks = await Promise.all(candidates.map(async x => {
                    try {
                        let r = await _fetch(IMG_URL(x.gid), { method:'HEAD', credentials:'include' });
                        if (r.status === 405) {
                            r = await _fetch(IMG_URL(x.gid), { method:'GET', credentials:'include' });
                        }
                        return r.ok ? x.it : null;
                    } catch { return null; }
                }));

                const okItems = checks.filter(Boolean);

                $track.innerHTML = '';
                okItems.forEach(it => $track.appendChild(buildSlide(it)));

                if ($track.children.length === 0){
                    $track.innerHTML =
                        '<li class="tt-slider__slide"><div class="tt-card"><div class="tt-card__imgwrap csp-s-2c6b46a2eef7"><span>Sin productos recientes</span></div></div></li>';
                }

                computeLayout();
            }catch(err){
                console.error('Slider recientes error:', err);
                $track.innerHTML =
                    '<li class="tt-slider__slide"><div class="tt-card"><div class="tt-card__imgwrap csp-s-2c6b46a2eef7"><span>Error cargando recientes</span></div></div></li>';
                computeLayout();
            }
        }

        if (document.readyState === 'loading') {
            document.addEventListener('DOMContentLoaded', init);
        } else {
            init();
        }
    })();

