function rolActualDesdeCache() {
        try {
            const raw = sessionStorage.getItem('user') || localStorage.getItem('user');
            if (!raw) return 0;
            const u = JSON.parse(raw);
            const idRol = Number(u?.id_rol ?? u?.rol_id ?? u?.idRol ?? 0);
            return Number.isFinite(idRol) ? idRol : 0;
        } catch {
            return 0;
        }
    }

