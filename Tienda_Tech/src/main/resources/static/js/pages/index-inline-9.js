(function pcBuilderSlideshow(){
        const IMAGES = [
            '/assets/menu/img1.jpg',
            '/assets/menu/img2.jpg',
            '/assets/menu/img3.jpg'
        ];

        const INTERVAL_MS = 5000;
        const FADE_MS     = 1000;

        const box = document.querySelector('.pc-builder-container');
        if (!box || !IMAGES.length) return;

        const a = box.querySelector('.pcb-bg.layer-a');
        let   b = box.querySelector('.pcb-bg.layer-b');
        let   c = box.querySelector('.pcb-bg.layer-c');
        if (!a) return;
        if (!b){ b = document.createElement('div'); b.className='pcb-bg layer-b'; a.after(b); }

        IMAGES.forEach(src => { const i = new Image(); i.src = src; });

        let idx = 0, frontIsA = true;

        const setBg = (el, src) => ttSetStyle(el, 'backgroundImage', `url("${src}")`);
        const show  = el => el.classList.add('show');
        const hide  = el => el.classList.remove('show');

        setBg(a, IMAGES[idx]); show(a);
        setBg(b, IMAGES[(idx+1) % IMAGES.length]);

        function next(){
            idx = (idx + 1) % IMAGES.length;
            const front = frontIsA ? a : b;
            const back  = frontIsA ? b : a;
            setBg(back, IMAGES[idx]);
            show(back); hide(front);
            setTimeout(() => { frontIsA = !frontIsA; }, FADE_MS);
        }

        if (IMAGES.length > 1) setInterval(next, INTERVAL_MS);
    })();

