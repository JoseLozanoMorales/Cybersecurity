// ====== NOTIFICADOR GLOBAL (top-right) ======
    (function () {
      const css = `
  .tt-toast{position:fixed;right:20px;top:20px;z-index:3000;display:flex;flex-direction:column;gap:10px}
  .tt-toast .item{min-width:260px;max-width:380px;padding:12px 14px;border-radius:10px;color:#fff;
                  box-shadow:0 10px 25px rgba(0,0,0,.18);opacity:0;transform:translateX(16px);
                  animation:tt-in .18s ease forwards}
  .tt-toast .success{background:#198754}.tt-toast .error{background:#dc3545}
  .tt-toast .info{background:#0d6efd}.tt-toast .warn{background:#fd7e14}
  @keyframes tt-in{to{opacity:1;transform:none}}
  @keyframes tt-out{to{opacity:0;transform:translateX(16px)}}
  `;
      ttAddCss(css);
      const box = document.createElement('div'); box.className = 'tt-toast'; document.body.appendChild(box);

      // Uso: notify('mensaje','success'|'error'|'info'|'warn', milisegundos)
      window.notify = function (msg, type='info', ms=3500) {
        const el = document.createElement('div');
        el.className = `item ${type}`; el.textContent = msg; box.appendChild(el);
        setTimeout(() => { ttSetStyle(el, 'animation', 'tt-out .18s ease forwards');
          setTimeout(() => el.remove(), 200); }, ms);
      };

      // Helper para fetch con mensajes por estado (opcional, ver usos abajo)
      window.fetchOrThrow = async function (url, opts) {
        const r = await fetch(url, opts);
        if (!r.ok) {
          let serverMsg = '';
          try { const data = await r.json(); serverMsg = (data?.message || data?.error || '').trim(); } catch {}
          let msg = serverMsg || `Error ${r.status}`;
          if (r.status === 409 && !serverMsg) msg = 'Datos ya guardados / registro duplicado.';
          if (r.status >= 500 && !serverMsg) msg = 'Error en el servidor.';
          const err = new Error(msg); err.status = r.status; throw err;
        }
        return r;
      };
    })();
  
