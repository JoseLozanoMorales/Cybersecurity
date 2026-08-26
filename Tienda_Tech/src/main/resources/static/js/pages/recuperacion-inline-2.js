(function () {
        'use strict';

        document.addEventListener('DOMContentLoaded', () => {
            const form = document.getElementById('recoverForm');
            if (!form) return;

            form.addEventListener('submit', async (e) => {
                e.preventDefault();

                const input = document.getElementById('correo');
                const correo = (input?.value || '').trim();
                const btn = document.getElementById('btnEnviar');
                const showMessage = window.ttShowMessage || ((msg) => alert(msg));

                if (!correo) {
                    showMessage('Por favor ingresa tu correo.', 'error');
                    return;
                }

                btn.disabled = true;
                const oldText = btn.textContent;
                btn.textContent = 'Enviando...';
                ttSetStyle(btn, 'background', '#665');

                // Timeout de 10s por si el server no responde
                const controller = new AbortController();
                const to = setTimeout(() => controller.abort(), 10000);

                try {
                    // Endpoint que genera + hashea + actualiza con tu SP + envía correo
                    const res = await fetch('/api/usuarios/recuperar-password', {
                        method: 'POST',
                        headers: { 'Content-Type': 'application/json' },
                        body: JSON.stringify({ correo }),
                        signal: controller.signal
                    });
                    clearTimeout(to);

                    const txt = await res.text();
                    let data = null; try { data = txt ? JSON.parse(txt) : null; } catch {}

                    if (res.ok) {
                        showMessage('¡Listo! Te enviamos una contraseña temporal a tu correo.', 'success');
                        ttSetStyle(btn, 'background', '#4CAF50');
                    } else if (res.status === 404 || /no existe/i.test(data?.message || '')) {
                        showMessage('No existe el correo registrado.', 'error');
                        ttSetStyle(btn, 'background', '#dc3545');
                    } else if (res.status === 429) {
                        showMessage('Demasiadas solicitudes. Inténtalo más tarde.', 'error');
                        ttSetStyle(btn, 'background', '#dc3545');
                    } else {
                        showMessage(data?.message || 'No se pudo enviar el correo. Intenta más tarde.', 'error');
                        ttSetStyle(btn, 'background', '#dc3545');
                    }
                } catch (err) {
                    const aborted = err?.name === 'AbortError';
                    showMessage(aborted ? 'Tiempo de espera agotado.' : 'Error de conexión con el servidor.', 'error');
                    ttSetStyle(btn, 'background', '#dc3545');
                } finally {
                    btn.disabled = false;
                    btn.textContent = oldText;
                    setTimeout(() => { ttSetStyle(btn, 'background', ''); }, 1500);
                }
            });
        });
    })();

