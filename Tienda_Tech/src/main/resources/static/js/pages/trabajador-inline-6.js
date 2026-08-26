/* Auto-logout por inactividad: 3 minutos */
        (function () {
            const IDLE_MS   = 3 * 60 * 1000;          // 3 min
            const REDIRECT  = "/index.html";          // cambia si tu login es otra ruta
            let timer;

            async function autoLogout() {
                try {
                    // Si ya tienes una función global logout(), úsala
                    if (typeof logout === "function") {
                        await logout();
                    } else {
                        // fallback: cerrar sesión en el backend
                        await fetch("/api/logout", { method: "POST", credentials: "include" });
                    }
                } catch (_) { /* ignora errores de red */ }
                // y redirigir al login/página pública
                location.href = REDIRECT;
            }

            function resetTimer() {
                clearTimeout(timer);
                timer = setTimeout(autoLogout, IDLE_MS);
            }

            // Eventos que cuentan como “actividad” del usuario
            ["click","mousemove","keydown","scroll","touchstart","focus"].forEach(ev =>
                window.addEventListener(ev, resetTimer, { passive: true })
            );

            resetTimer();
        })();
    
