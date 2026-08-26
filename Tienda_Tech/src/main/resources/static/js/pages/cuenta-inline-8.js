(function(){
      // --- USER_ID desde session/local storage
      const USER_ID = (()=>{
        try{
          const u = JSON.parse(sessionStorage.getItem('user') || localStorage.getItem('user') || 'null');
          return u?.usuarioId ?? u?.id ?? u?.userId ?? u?.id_usuario ?? null;
        }catch{ return null; }
      })();

      // --- Endpoints backend
      const API = {
        tipos: '/api/tipo_metodopago',
        base:  '/api/mis-metodos-pago'
      };

      // --- Elementos UI
      const listEl   = document.getElementById('mp-list');
      const toastEl  = document.getElementById('mp-feedback');

      const modalEl  = document.getElementById('paymentModal');
      const modal    = () => (bootstrap.Modal.getInstance(modalEl) || new bootstrap.Modal(modalEl));

      const form     = document.getElementById('paymentForm');
      const inputNum = document.getElementById('cardNumber');
      const helpNum  = document.getElementById('cardHelp');
      const inputExp = document.getElementById('expiryDate'); // <input type="month"> -> "YYYY-MM"
      const selectTp = document.getElementById('paymentTipo');
      const wrapPref = document.getElementById('preferidoWrap');

      // Oculta el check de preferido (no hay endpoint para eso aún)
      if (wrapPref) ttSetStyle(wrapPref, 'display', 'none');

      // --- Helpers auth/headers
      function authHeaders(extra = {}){
        const token = localStorage.getItem('token');
        const h = { 'X-User-Id': USER_ID, ...extra };
        if (token) h['Authorization'] = `Bearer ${token}`;
        return h;
      }
      const withAuth = (opts={}) => ({
        credentials: 'include',
        ...opts,
        headers: authHeaders({ 'Content-Type':'application/json', ...(opts.headers||{}) })
      });

      // --- Helpers UI
      function showToast(msg){
        if (!toastEl) return;
        toastEl.textContent = msg;
        ttSetStyle(toastEl, 'display', 'block');
        setTimeout(()=> ttSetStyle(toastEl, 'display', 'none'), 2000);
      }
      function toMMYYYY(s){
        if (!s) return null;
        // acepta "YYYY-MM" o "YYYY-MM-DD"
        if (/^\d{4}-\d{2}(-\d{2})?$/.test(s)) {
          const [y,m] = s.split('-');
          return `${m}/${y}`;
        }
        return s;
      }
      function luhnOk(num){
        num=(num||'').replace(/\s+/g,'');
        if (!/^\d{12,19}$/.test(num)) return false;
        let sum=0,d=false; for(let i=num.length-1;i>=0;i--){ let k=+num[i]; if(d){ k*=2; if(k>9)k-=9; } sum+=k; d=!d; }
        return sum%10===0;
      }
      function expFuture(yyyyMm){
        if (!yyyyMm) return false;
        const [y,m]=yyyyMm.split('-').map(Number);
        const now=new Date();
        const ymSel=y*100+m, ymNow=now.getFullYear()*100+(now.getMonth()+1);
        return ymSel>=ymNow;
      }

      // --- API calls
      async function loadTipos(){
        try{
          const r = await fetch(API.tipos, { credentials:'include' });
          const arr = await r.json();
          selectTp.innerHTML = '<option value="">Seleccionar</option>' + (arr||[])
                  .map(t => `<option value="${t.tipoId}">${t.nombre}</option>`).join('');
        }catch(e){
          console.error(e);
          selectTp.innerHTML = '<option value="">(error)</option>';
        }
      }

      async function apiList(){
        const r = await fetch(API.base, withAuth());
        if (!r.ok) throw new Error(await r.text());
        return await r.json(); // List<MetodoPagoDTO>
      }

      async function apiAdd(payload){
        const r = await fetch(API.base, withAuth({ method:'POST', body:JSON.stringify(payload) }));
        if (!r.ok) throw new Error(await r.text());
        return await r.json(); // lista actualizada
      }

      async function apiDelete(id){
        const r = await fetch(`${API.base}/${id}`, withAuth({ method:'DELETE' }));
        if (r.status !== 204) throw new Error(await r.text());
      }

      // --- Render
      function card(it){
        const vence = toMMYYYY(it.fechaExpiracion);
        const tipo  = it.nombre || it.tipoId || '—';
        return `
      <div class="col-12 col-md-6 col-lg-4">
        <div class="p-3 border rounded-3 h-100">
          <div class="d-flex justify-content-between align-items-start">
            <div>
              <div class="fw-semibold">${it.mascara || '****'}</div>
              <div class="text-muted small">Vence: ${vence || '—'} · Tipo: ${tipo}</div>
              <div class="text-muted small">Estado: ${it.habilitado ? 'Habilitado' : 'Deshabilitado'}</div>
            </div>
            <button class="btn btn-sm btn-outline-danger" data-act="delete" data-id="${it.metodoId}" title="Eliminar">
              <i class="bi bi-trash"></i>
            </button>
          </div>
        </div>
      </div>`;
      }

      function render(list){
        if (!list || list.length===0){
          listEl.innerHTML = `<div class="col-12 text-center text-muted py-4">Aún no tienes métodos de pago.</div>`;
          return;
        }
        listEl.innerHTML = list.map(card).join('');
      }

      // --- Formato del input de tarjeta (espaciado 4-4-4-4)
      inputNum?.addEventListener('input', function(){
        let value = this.value.replace(/\s/g,'').replace(/[^0-9]/g,'');
        let formatted = value.match(/.{1,4}/g)?.join(' ') || value;
        if (formatted.length <= 19) this.value = formatted;
      });

      // --- Envío del formulario
      form?.addEventListener('submit', async (e)=>{
        e.preventDefault();

        const rawNum = inputNum.value.trim().replace(/\s+/g,'');
        const month  = inputExp.value; // "YYYY-MM"
        const tipoId = parseInt(selectTp.value,10) || null;

        if (!rawNum){ helpNum.textContent='Ingresa el número de tarjeta.'; helpNum.classList.add('text-danger'); return; }
        if (!luhnOk(rawNum)){ helpNum.textContent='Número inválido (Luhn).'; helpNum.classList.add('text-danger'); return; }
        helpNum.textContent='No se almacenan espacios'; helpNum.classList.remove('text-danger');

        if (!month || !/^\d{4}-\d{2}$/.test(month)){ alert('La fecha de expiración debe ser YYYY-MM.'); return; }
        if (!expFuture(month)){ alert('La fecha de expiración debe ser actual o futura.'); return; }

        if (!tipoId){ alert('Selecciona un tipo de tarjeta.'); return; }

        try{
          const payload = { numeroTarjeta: rawNum, mesExpiracion: month, tipoId };
          const lista = await apiAdd(payload); // backend responde lista actualizada
          render(lista);
          showToast('Método agregado.');
          modal().hide();
          form.reset();
        }catch(err){
          console.error(err);
          alert('No se pudo guardar el método de pago.');
        }
      });

      // --- Click en eliminar
      listEl?.addEventListener('click', async (ev)=>{
        const btn = ev.target.closest('button[data-act="delete"]'); if(!btn) return;
        const id = +btn.dataset.id; if(!id) return;
        if (!confirm('¿Eliminar este método de pago?')) return;
        try{
          await apiDelete(id);
          render(await apiList());
          showToast('Eliminado.');
        }catch(err){
          console.error(err);
          alert('No se pudo eliminar.');
        }
      });

      // --- Inicialización
      (async function init(){
        if (!USER_ID) console.warn('USER_ID no resuelto. Asegúrate de tener la sesión cargada.');
        await loadTipos().catch(()=>{});
        try { render(await apiList()); }
        catch { listEl.innerHTML = `<div class="col-12 text-danger text-center py-4">No se pudieron cargar los métodos de pago.</div>`; }
      })();

    })();
  
