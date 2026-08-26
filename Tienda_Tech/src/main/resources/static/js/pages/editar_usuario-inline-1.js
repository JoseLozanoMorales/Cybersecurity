function toggleMenu() {
      const menu = document.getElementById('hamburgerMenu');
      const overlay = document.getElementById('overlay');
      menu.classList.toggle('active');
      overlay.classList.toggle('active');
    }
    function closeMenu() {
      const menu = document.getElementById('hamburgerMenu');
      const overlay = document.getElementById('overlay');
      menu.classList.remove('active');
      overlay.classList.remove('active');
    }
    // Toggle password visibility
    document.addEventListener('DOMContentLoaded', () => {
      const btn = document.getElementById('togglePassword');
      if (btn) {
        btn.addEventListener('click', function () {
          const input = document.getElementById('contrasena');
          const icon = this.querySelector('i');
          if (!input) return;
          if (input.type === 'password') { input.type = 'text'; icon.className = 'bi bi-eye-slash'; }
          else { input.type = 'password'; icon.className = 'bi bi-eye'; }
        });
      }
    });
    // Cambiar imagen de perfil (demo local)
    function changeProfileImage() {
      const input = document.createElement('input');
      input.type = 'file';
      input.accept = 'image/*';
      input.onchange = function (e) {
        const file = e.target.files[0];
        if (!file) return;
        const reader = new FileReader();
        reader.onload = function (ev) {
          const el = document.querySelector('.profile-image');
          ttSetStyle(el, 'backgroundImage', `url(${ev.target.result})`);
          ttSetStyle(el, 'backgroundSize', 'cover');
          ttSetStyle(el, 'backgroundPosition', 'center');
          el.innerHTML = '';
        };
        reader.readAsDataURL(file);
      };
      input.click();
    }
    function goBack() {
      if (confirm('¿Salir sin guardar?')) window.history.back();
    }
  
