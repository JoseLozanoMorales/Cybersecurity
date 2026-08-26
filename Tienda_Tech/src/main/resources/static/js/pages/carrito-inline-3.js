document.addEventListener('DOMContentLoaded', () => {
    try {
        const raw = sessionStorage.getItem('user') || localStorage.getItem('user');
        const u = raw ? JSON.parse(raw) : null;
        if (!u) {
        const next = encodeURIComponent(location.pathname + location.search);
        location.href = '/Login.html?next=' + next;
        }
    } catch {
        const next = encodeURIComponent(location.pathname + location.search);
        location.href = '/Login.html?next=' + next;
    }
    });

