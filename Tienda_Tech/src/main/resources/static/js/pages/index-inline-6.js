let lastBasePayload = null;
    let lastOriginalData = null;
    let nivelUpActive = false;
    let nivelBoost = 1.20;

    async function postSugerencia(payload) {
        const resp = await fetch('/api/sugerencias/generar', {
            method: 'POST',
            headers: {'Content-Type': 'application/json', 'X-Username': payload.usuario || ''},
            credentials: 'include',
            body: JSON.stringify(payload)
        });
        const text = await resp.text().catch(() => '');
        if (!resp.ok) throw new Error(text || `HTTP ${resp.status}`);
        const data = JSON.parse(text || '{}');

        if (Array.isArray(data.warnings) && data.warnings.some(w => w.code === 'LOW_BUDGET')) {
            window.showToast && showToast('Presupuesto muy bajo');
        }
        return data;
    }

    async function generarSugerencia(respuestas, resultEl) {
        const getUserFromStorage = () => {
            try {
                const keys = ['user', 'usuario', 'usuarioLogueado', 'currentUser'];
                for (const k of keys) {
                    const raw = sessionStorage.getItem(k) || localStorage.getItem(k);
                    if (raw) return JSON.parse(raw);
                }
            } catch (_) {
            }
            return null;
        };
        const user = getUserFromStorage();
        if (!user) {
            const next = encodeURIComponent(location.pathname);
            location.href = `/Login.html?next=${next}`;
            return;
        }
        const userId = user.usuario_id ?? user.id ?? user.userId ?? null;
        const username = user.usuario ?? user.username ?? user.nombreUsuario ?? null;

        const payload = {
            usuario: username,
            usuario_id: userId,
            presupuesto: (respuestas.presupuesto === '' || respuestas.presupuesto == null)
                ? null : Number(respuestas.presupuesto),
            preferencias: respuestas,
            minimos: {}
        };

        lastBasePayload = JSON.parse(JSON.stringify(payload));
        nivelUpActive = false;

        const data = await postSugerencia(payload);
        lastOriginalData = data;
        renderConBotonNivel(data, resultEl);
    }

    async function onToggleNivel() {
        const resultEl = document.getElementById('ttSurveyResult') || document.body;
        try {
            if (!nivelUpActive) {
                const boosted = JSON.parse(JSON.stringify(lastBasePayload || {}));
                const basePres = lastBasePayload?.presupuesto ?? null;
                const fallback = lastOriginalData?.totalPrecio ?? 0;
                const base = (typeof basePres === 'number' && basePres > 0) ? basePres : fallback;

                boosted.presupuesto = Math.max(1, Math.round(base * nivelBoost));
                const data = await postSugerencia(boosted);

                nivelUpActive = true;
                renderConBotonNivel(data, resultEl);
                window.showToast && showToast(`Subiendo nivel (+${Math.round((nivelBoost - 1) * 100)}% presupuesto)`);
            } else {
                nivelUpActive = false;
                renderConBotonNivel(lastOriginalData, resultEl);
            }
        } catch (e) {
            console.error(e);
            resultEl.innerHTML = `<p class="csp-s-9876f9207bd5">No se pudo generar la sugerencia: ${e.message || e}</p>`;
        }
    }

    function renderConBotonNivel(data, resultEl) {
        renderResultPcCompleta(data, resultEl);
        const actions = document.getElementById('nivel-actions');
        if (!actions) return;

        actions.innerHTML = '';
        const btn = document.createElement('button');
        btn.type = 'button';
        btn.textContent = nivelUpActive ? 'Volver' : 'Subir nivel';
        btn.onclick = onToggleNivel;
        actions.appendChild(btn);
    }

