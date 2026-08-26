async function TTREG_createUser() {
        const ns = window.TTREG;
        if (!ns || !ns.pendingUserData) {
            showToast('No hay datos de registro en espera. Intenta nuevamente.', 'danger');
            ns?.setFormDisabled(false);
            return;
        }

        // Sugerido: enviar otpTxId para validación en backend
        const body = { ...ns.pendingUserData, otpTxId: ns.txId };

        try {
            const r = await fetch(ns.API_USER_CREATE, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(body)
            });
            const txt = await r.text().catch(() => '');

            if (!r.ok) {
                showToast('Error al crear usuario' + (txt ? `: ${txt}` : ''), 'danger');
                ttSetStyle(document.getElementById('verifyBlock'), 'display', 'none');
                ns.setFormDisabled(false);
                return;
            }

            showToast('Cuenta creada. Redirigiendo al login…', 'success');
            // Limpieza
            ns.pendingUserData = null;
            ns.txId = null;
            try {
                document.getElementById('contrasena').value = '';
                document.getElementById('repetirContrasena').value = '';
            } catch {}
            setTimeout(() => location.href = '/login.html', 1200);
        } catch (e) {
            console.error('CREATE USER EXCEPTION', e);
            showToast('Error de conexión al crear la cuenta', 'danger');
            ttSetStyle(document.getElementById('verifyBlock'), 'display', 'none');
            ns.setFormDisabled(false);
        }
    }

    async function TTREG_verifyOtp() {
        const ns = window.TTREG;
        const input = document.getElementById('otpCode');
        const code = (input.value || '').trim();
        if (!/^[0-9]{6}$/.test(code)) {
            input.classList.add('is-invalid');
            return;
        }
        input.classList.remove('is-invalid');

        try {
            const r = await fetch(ns.API_OTP, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ accion: 'validar', correo: ns.pendingEmail, codigo: code, txId: ns.txId })
            });
            const txt = await r.text().catch(() => '');

            if (r.ok) {
                showToast('Código verificado. Creando la cuenta…', 'success');
                await TTREG_createUser();
            } else {
                showToast('Código inválido o expirado' + (txt ? `: ${txt}` : ''), 'danger');
            }
        } catch (e) {
            console.error('OTP/validar EXCEPTION', e);
            showToast('Error de conexión al verificar OTP', 'danger');
        }
    }

    document.addEventListener('DOMContentLoaded', () => {
        const btnVerify = document.getElementById('btnVerify');
        const btnResend = document.getElementById('btnResend');
        if (btnVerify) btnVerify.addEventListener('click', TTREG_verifyOtp);
        if (btnResend) btnResend.addEventListener('click', () => {
            if (!window.TTREG?.pendingEmail) {
                showToast('No hay un correo pendiente. Completa el formulario primero.', 'warning');
                return;
            }
            window.TTREG.sendOtp(window.TTREG.pendingEmail);
        });
    });

