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

    document.getElementById('togglePassword').addEventListener('click', function() {
        const passwordField = document.getElementById('contrasena');
        const icon = this.querySelector('i');
        if (passwordField.type === 'password') {
            passwordField.type = 'text';
            icon.className = 'bi bi-eye-slash';
        } else {
            passwordField.type = 'password';
            icon.className = 'bi bi-eye';
        }
    });

    function changeProfileImage() {
        const input = document.createElement('input');
        input.type = 'file';
        input.accept = 'image/*';
        input.onchange = function(e) {
            const file = e.target.files[0];
            if (file) {
                const reader = new FileReader();
                reader.onload = function(e) {
                    const profileImage = document.querySelector('.profile-image');
                    ttSetStyle(profileImage, 'backgroundImage', `url(${e.target.result})`);
                    ttSetStyle(profileImage, 'backgroundSize', 'cover');
                    ttSetStyle(profileImage, 'backgroundPosition', 'center');
                    profileImage.innerHTML = '';
                };
                reader.readAsDataURL(file);
            }
        };
        input.click();
    }

    function goBack() {
        if (confirm('¿Estás seguro de que deseas salir? Los cambios no guardados se perderán.')) {
            window.location.href = '/';
        }
    }

    function showToast(message, type = 'info') {
        const toastHtml = `
        <div class="toast align-items-center text-bg-${type} border-0 position-fixed top-0 end-0 m-3 csp-s-847e550d62bb" role="alert">
          <div class="d-flex">
            <div class="toast-body">${message}</div>
            <button type="button" class="btn-close btn-close-white me-2 m-auto" data-bs-dismiss="toast"></button>
          </div>
        </div>`;
        document.body.insertAdjacentHTML('beforeend', toastHtml);
        const toastElement = document.querySelector('.toast:last-child');
        const toast = new bootstrap.Toast(toastElement);
        toast.show();
        toastElement.addEventListener('hidden.bs.toast', () => toastElement.remove());
    }

    window.addEventListener('load', function() {});

