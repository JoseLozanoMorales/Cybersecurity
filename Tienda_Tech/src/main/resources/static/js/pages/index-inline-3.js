(function(){
        const LOGIN_PAGE = '/Login.html';

        function isLogged() {
            try {
                const raw = sessionStorage.getItem('user') || localStorage.getItem('user');
                if (raw && JSON.parse(raw)) return true;
            } catch {}
            return !!localStorage.getItem('token') || !!localStorage.getItem('refreshToken');
        }

        function guardClick(ev) {
            const a = ev.currentTarget;
            if (isLogged()) return;
            ev.preventDefault();
            const target = a.getAttribute('href') || '/';
            const next = encodeURIComponent(target);
            location.href = `${LOGIN_PAGE}?next=${next}`;
        }

        function wireGuards(rootOrEvent) {
            const ctx = (rootOrEvent && typeof rootOrEvent.querySelectorAll === 'function')
                ? rootOrEvent
                : document;

            ctx.querySelectorAll('a.requires-auth').forEach(a => {
                a.removeEventListener('click', guardClick);
                a.addEventListener('click', guardClick);
            });
        }

        document.addEventListener('DOMContentLoaded', () => wireGuards(document));

        new MutationObserver(() => wireGuards(document))
            .observe(document.body, { subtree: true, childList: true });
    })();

