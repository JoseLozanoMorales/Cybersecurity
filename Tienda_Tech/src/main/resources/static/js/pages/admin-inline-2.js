(function () {
          if (!document.body.classList.contains('tt-admin')) return; // ← solo en Admin
          const stored = Number(localStorage.getItem('tt_idle_ms') || 0);
          const IDLE_MS = 60 * 1000;   // 1 min
          if (IDLE_MS <= 0) return;

          const REDIRECT = "/index.html";
          let last = Date.now(), t;

          function schedule(){ clearTimeout(t); t = setTimeout(triggerLogout, IDLE_MS); }
          function reset(ev){ if (!ev || ev.isTrusted) { last = Date.now(); schedule(); } }

          async function serverLogout(){ try{ await fetch('/api/logout',{method:'POST',credentials:'include'});}catch{} }
          function clearClientState(){ localStorage.removeItem('tt_user'); localStorage.removeItem('tt_idle_ms'); }

          function triggerLogout(){
              if (Date.now() - last < IDLE_MS - 50) { schedule(); return; } // hubo actividad real

              sessionStorage.removeItem('user');
              localStorage.removeItem('user');
              sessionStorage.removeItem('token');
              localStorage.removeItem('token');

              clearClientState();
              serverLogout();                 // no esperamos
              location.href = REDIRECT;       // vuelves a login/público
          }

          ["pointerdown","keydown","wheel","touchstart","mousemove"]
              .forEach(ev => window.addEventListener(ev, reset, { passive:true }));
          window.addEventListener('beforeunload', () => clearTimeout(t));

          schedule();
      })();
  
