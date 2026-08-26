window.addEventListener('DOMContentLoaded', () => window.SessionAuth?.initAuthUI?.());
        document.querySelectorAll('[data-logout]')
          .forEach(b => b.addEventListener('click', () => window.SessionAuth?.sessionLogout?.()));
    
