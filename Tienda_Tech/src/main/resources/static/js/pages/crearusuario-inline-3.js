// Namespace compartido
    window.TTREG = {
        API_OTP: '/api/otp',
        API_USER_CREATE: '/api/usuarios/crear',
        pendingUserData: null,
        pendingEmail: null,
        txId: null,
        resendTimerId: null,
        resendLeft: 60,
        setFormDisabled(disabled) {
            const formEl = document.getElementById('editUserForm');
            if (!formEl) return;
            Array.from(formEl.elements).forEach(el => {
                if (el.id === 'togglePassword') return; // deja el ojo activo
                el.disabled = disabled;
            });
        },
        startResendTimer() {
            const btn = document.getElementById('btnResend');
            const lbl = document.getElementById('resendTimer');
            if (!btn || !lbl) return;
            btn.disabled = true;
            this.resendLeft = 60;
            lbl.textContent = `(${this.resendLeft})`;
            clearInterval(this.resendTimerId);
            this.resendTimerId = setInterval(() => {
                this.resendLeft--;
                lbl.textContent = `(${this.resendLeft})`;
                if (this.resendLeft <= 0) {
                    clearInterval(this.resendTimerId);
                    btn.disabled = false;
                    lbl.textContent = '';
                }
            }, 1000);
        },
        async sendOtp(email) {
            try {
                const r = await fetch(this.API_OTP, {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ accion: 'enviar', correo: email, txId: this.txId })
                });
                if (!r.ok) {
                    const txt = await r.text().catch(() => '');
                    showToast(`Error al enviar código (HTTP ${r.status})` + (txt ? `: ${txt}` : ''), 'danger');
                    this.setFormDisabled(false);
                    return false;
                }
                const payload = await r.json();
                this.txId = payload.txId;
                this.pendingEmail = payload.correo;

                // Mostrar bloque OTP
                document.getElementById('verifyEmailLabel').textContent = this.pendingEmail;
                ttSetStyle(document.getElementById('verifyBlock'), 'display', 'block');
                this.startResendTimer();
                showToast('Código enviado al correo', 'info');
                return true;
            } catch (e) {
                console.error('OTP/enviar EXCEPTION', e);
                showToast('Error de conexión al enviar OTP', 'danger');
                this.setFormDisabled(false);
                return false;
            }
        }
    };

    // Submit: valida y SOLO envía OTP (no crea usuario aún)
    document.getElementById('editUserForm').addEventListener('submit', async function (e) {
        e.preventDefault();

        const nombre      = document.getElementById('nombre').value.trim();
        const correo      = document.getElementById('correo').value.trim();
        const contrasena  = document.getElementById('contrasena').value.trim();
        const repetir     = document.getElementById('repetirContrasena').value.trim();
        const cedula      = document.getElementById('cedula').value.trim();
        const telefono    = document.getElementById('telefono').value.trim();
        const usuario     = document.getElementById('usuario').value.trim();

        if (!nombre || !correo || !contrasena || !repetir || !cedula || !telefono) {
            showToast('Completa los campos obligatorios.', 'danger');
            return;
        }
        if (contrasena !== repetir) {
            showToast('Las contraseñas no coinciden.', 'danger');
            document.getElementById('repetirContrasena').classList.add('is-invalid');
            return;
        }
        if (!/^[0-9]{10}$/.test(cedula)) {
            showToast('Cédula inválida (10 dígitos).', 'danger');
            return;
        }
        if (!/^[0-9]{10}$/.test(telefono)) {
            showToast('Teléfono inválido (10 dígitos).', 'danger');
            return;
        }

        // Guarda los datos hasta que verifique OTP
        window.TTREG.pendingUserData = { nombre, correo, contrasena, cedula, telefono, usuario };

        // Deshabilita el form y envía OTP
        window.TTREG.setFormDisabled(true);
        const ok = await window.TTREG.sendOtp(correo);
        if (!ok) {
            // Si falla el envío de OTP, re-habilita para corregir
            window.TTREG.setFormDisabled(false);
        }
    });

