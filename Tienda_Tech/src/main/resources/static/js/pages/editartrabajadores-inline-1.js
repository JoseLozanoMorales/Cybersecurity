// Componente de mensajes
    function showMessage(type, text) {
        const box = document.getElementById('feedback');
        if (!box) return;
        const html = `
      <div class="alert alert-${type} alert-dismissible fade show" role="alert">
        ${text}
        <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Cerrar"></button>
      </div>`;
        box.innerHTML = html;
        box.scrollIntoView({ behavior: 'smooth', block: 'center' });
    }

    function toggleMenu() {
        document.getElementById('hamburgerMenu')?.classList.toggle('active');
        document.getElementById('overlay')?.classList.toggle('active');
    }
    function closeMenu() {
        document.getElementById('hamburgerMenu')?.classList.remove('active');
        document.getElementById('overlay')?.classList.remove('active');
    }

    // Toggle password visibility
    document.addEventListener('DOMContentLoaded', () => {
        const btn = document.getElementById('togglePassword');
        if (btn) {
            btn.addEventListener('click', function () {
                const input = document.getElementById('contrasena');
                const icon = this.querySelector('i');
                if (!input) return;
                const toType = input.type === 'password' ? 'text' : 'password';
                input.type = toType;
                icon.className = toType === 'password' ? 'bi bi-eye' : 'bi bi-eye-slash';
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
                el.textContent = '';
            };
            reader.readAsDataURL(file);
        };
        input.click();
    }

    function goBack() {
        if (document.referrer && document.referrer !== location.href) {
            history.back();
        } else {
            location.href = '/cuenta.html';
        }
    }

