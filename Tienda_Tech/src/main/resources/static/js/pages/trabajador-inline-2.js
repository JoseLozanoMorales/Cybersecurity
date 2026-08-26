// Espera que role-utils.js esté cargado con defer
        document.addEventListener('DOMContentLoaded', () => {
          const u = RoleUtils.ensureLoggedIn();
          if (!u) {
            const next = encodeURIComponent(location.pathname + location.search);
            location.href = '/Login.html?next=' + next; // respeta el nombre real del archivo
            return;
          }
          const role = RoleUtils.resolveRoleName(u);
          if (role !== 'trabajador') {
            location.href = '/index.html';
            return;
          }
          const name = RoleUtils.resolveUserName(u);
          const ui = document.querySelector('.user-info');
          if (ui) ui.textContent = `👤 ${role.charAt(0).toUpperCase() + role.slice(1)}: ${name}`;
          document.title = 'Panel Trabajador - TiendaTech';
        });
    
