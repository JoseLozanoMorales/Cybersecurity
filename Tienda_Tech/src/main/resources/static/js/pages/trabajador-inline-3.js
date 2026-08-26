//cerrar sesion
          async function logout() {
            try {
              sessionStorage.removeItem('user');
              localStorage.removeItem('user');
              sessionStorage.removeItem('token');
              localStorage.removeItem('token');

              await fetch('/api/auth/logout', { method: 'POST' }).catch(() => {});
            } finally {
              location.replace('Login.html');
            }
          }
        /* Si entras por el sidebar, también carga el perfil */
        document.addEventListener('DOMContentLoaded', () => {
            const item = Array.from(document.querySelectorAll('.sidebar .nav-item'))
                .find(i => i.textContent.trim().toLowerCase()==='cuenta');
            item?.addEventListener('click', () => loadAccountProfile());
        });

        /* Helpers de sesión/API */
        function readUser(){
            try{ return JSON.parse(sessionStorage.getItem('user') || localStorage.getItem('user') || 'null'); }
            catch{ return null; }
        }
        function getAvatarUrl(u){
            const v = (u?.avatar_path || u?.avatarPath || u?.avatarUrl || '').trim();
            return v || null;
        }

        /* Rellenar datos personales */
        async function loadAccountProfile(){
            const u0 = readUser();
            const set = (id, v) => { const el = document.getElementById(id); if (el) el.value = v ?? ''; };
            const img = document.getElementById('acc_avatar');

            // Prefill inmediato desde storage
            if (u0){
                set('acc_nombre',   u0.nombre);
                set('acc_usuario',  u0.usuario);
                set('acc_telefono', u0.telefono);
                set('acc_cedula',   u0.cedula);
                set('acc_correo',   u0.correo);
                const nameTag = document.getElementById('adminName');
                if (nameTag) nameTag.textContent = u0.nombre || u0.usuario || 'Admin';
                const a0 = getAvatarUrl(u0); if (a0 && img) img.src = a0 + '?t=' + Date.now();
            }
            if (img) img.onerror = () => { img.onerror=null; img.src='/assets/avatars/defaults/user.png'; };

            // Sincroniza con backend (/me)
            try{
                const r = await fetch('/api/usuarios/me', { credentials:'include' });
                if (r.ok){
                    const me = await r.json();
                    set('acc_nombre',   me.nombre);
                    set('acc_usuario',  me.usuario);
                    set('acc_telefono', me.telefono);
                    set('acc_cedula',   me.cedula);
                    set('acc_correo',   me.correo);
                    const nameTag = document.getElementById('adminName');
                    if (nameTag) nameTag.textContent = me.nombre || me.usuario || 'Admin';
                    const a1 = getAvatarUrl(me); if (a1 && img) img.src = a1 + '?t=' + Date.now();
                }
            }catch{}
        }
    
