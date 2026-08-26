(function(){
        const LINK_ID='themeStylesheet', STORAGE_KEY='tt-theme', BTN_ID='themeToggle';
        function setTheme(theme){
            const link=document.getElementById(LINK_ID); if(!link) return;
            const href = theme==='dark'
                ? (link.dataset.dark  || 'css/styleOscuroMenu.css')   // <-- aquí
                : (link.dataset.light || 'css/styleClaroMenu.css');
            if(link.getAttribute('href')!==href) link.setAttribute('href', href);
            localStorage.setItem(STORAGE_KEY, theme);
            const btn=document.getElementById(BTN_ID);
            if(btn){
                const dark=theme==='dark';
                btn.textContent = dark ? '☀️ Claro' : '🌙 Oscuro';
                btn.title       = dark ? 'Cambiar a modo claro' : 'Cambiar a modo oscuro';
                btn.setAttribute('aria-pressed', String(dark));
                btn.classList.toggle('btn-warning', dark);
                btn.classList.toggle('btn-secondary', !dark);
            }
        }
        function toggleTheme(){
            const current=localStorage.getItem(STORAGE_KEY)||'light';
            setTheme(current==='light' ? 'dark' : 'light');
        }
        document.addEventListener('DOMContentLoaded',()=>{
            document.getElementById(BTN_ID)?.addEventListener('click', toggleTheme);
            setTheme(localStorage.getItem(STORAGE_KEY)||'light');
        });
    })();

