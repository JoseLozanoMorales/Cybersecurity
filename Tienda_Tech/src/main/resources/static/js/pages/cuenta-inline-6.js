document.addEventListener('DOMContentLoaded', () => {
      if (window.isLoggedIn && !window.isLoggedIn()) {
        const next = location.pathname + location.search;
        location.replace(`/Login.html?next=${encodeURIComponent(next)}`);
      }
    });
    function handleLoginSuccess(user, token){
      sessionStorage.setItem('user', JSON.stringify(user));
      if (token) localStorage.setItem('token', token);
      const next = new URLSearchParams(location.search).get('next') || 'index.html';
      location.href = next;
    }
  
