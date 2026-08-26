(function(){
      const fmt = n => '$' + Number(n||0).toFixed(2);

      // Tu Client ID sandbox
      const PAYPAL_CLIENT_ID = 'Acrd3u7YQ4mgh0CkKbk8EIm3nqVnUYitUa4x1Ftoazxg0Zcm6VaheMRw9-2oSo3YK5tkzsJ8zc0piv_t';

      let ORDER_ID = null;
      let hostedFieldsInstance = null;
      let DIRECCION_ID = null;
      let METODOPAGO_ID = null;
      let DIRECCIONES = [];

      const $selDir   = () => document.getElementById('selDireccion');
      const $selMet   = () => document.getElementById('selMetodo');
      const $dirDet   = () => document.getElementById('dirDetalle');
      const $msg      = () => document.getElementById('msg');
      const $btnPagar = () => document.getElementById('btnPagar');

      function getUsuarioId(){
        try{
          const raw = sessionStorage.getItem('user') || localStorage.getItem('user');
          const u = raw ? JSON.parse(raw) : null;
          return u?.UsuarioID ?? u?.usuarioId ?? u?.usuario_id ?? u?.id ?? null;
        }catch{ return null; }
      }

      function setMsg(html, cls){
        const el = $msg();
        el.className = 'mt-3 small ' + (cls||'');
        el.innerHTML = html;
      }

      async function fetchJSON(url, opt){
        const uid = getUsuarioId();
        const headers = Object.assign({}, (opt && opt.headers) || {});
        if (uid) headers['X-User-Id'] = uid;
        const r = await fetch(url, Object.assign({credentials:'include', headers}, opt||{}));
        if (!r.ok) throw new Error(await r.text());
        const ct = r.headers.get('content-type') || '';
        return ct.includes('application/json') ? r.json() : r.text();
      }

      async function loadResumen(){
        const r = await fetchJSON('/api/carrito/resumen');
        document.getElementById('p-sub').textContent = fmt(r.subtotal||0);
        document.getElementById('p-imp').textContent = fmt(r.impuestos||0);
        document.getElementById('p-tot').textContent = fmt(r.total||0);
      }

      function pintarDireccion(d){
        const calle  = document.getElementById('addrCalle');
        const ref    = document.getElementById('addrRef');
        const ciudad = document.getElementById('addrCiudad');
        const prov   = document.getElementById('addrProv');

        if (calle)  calle.value  = d?.calle ?? '';
        if (ref)    ref.value    = d?.referencia ?? '';
        if (ciudad) ciudad.value = d?.ciudadNombre ?? d?.nombre_de_ciudad ?? '';
        if (prov)   prov.value   = d?.provinciaNombre ?? d?.nombre_de_provincia ?? '';

        DIRECCION_ID = d?.direccionId ?? d?.direccion_id ?? null;

        if ($dirDet()){
          const cTxt = ciudad ? ciudad.value : (d?.ciudadNombre ?? '');
          const pTxt = prov   ? prov.value   : (d?.provinciaNombre ?? '');
          $dirDet().textContent = [cTxt, pTxt].filter(Boolean).join(', ');
        }
      }

      async function loadDirecciones(){
        const uid = getUsuarioId();
        if (!uid){ console.warn('Sin UsuarioID'); return; }

        DIRECCIONES = await fetchJSON(`/api/usuarios/${uid}/direcciones?view=full`);
        const sel = $selDir();
        sel.innerHTML = '';

        DIRECCIONES.forEach(x=>{
          const opt = document.createElement('option');
          opt.value = x.direccionId;
          opt.textContent = `${x.calle ?? ''}${x.referencia ? ' - ' + x.referencia : ''}`;
          sel.appendChild(opt);
        });

        if (DIRECCIONES.length){
          sel.value = String(DIRECCIONES[0].direccionId);
          pintarDireccion(DIRECCIONES[0]);
        } else {
          pintarDireccion({calle:'',referencia:'',ciudadNombre:'',provinciaNombre:'',direccionId:null});
        }

        sel.onchange = ()=>{
          const idSel = Number(sel.value);
          const d = DIRECCIONES.find(x => Number(x.direccionId) === idSel);
          if (d) pintarDireccion(d);
        };
      }

      async function loadMetodos(){
        const rows = [{ id: 2, nombre: 'Tarjeta (PayPal)' }];
        const sel = $selMet();
        sel.innerHTML = '';
        rows.forEach(x=>{
          const opt = document.createElement('option');
          opt.value = x.id;
          opt.textContent = x.nombre;
          sel.appendChild(opt);
        });
        sel.value = rows[0].id;
        METODOPAGO_ID = Number(sel.value);
        sel.addEventListener('change', ()=> METODOPAGO_ID = Number(sel.value));
      }

      // Inyecta el SDK con client_token
      async function loadPaypalSdk(clientToken){
    return new Promise((resolve, reject)=>{
      console.log('[PayPal] clientToken len =', (clientToken||'').length);

      const s = document.createElement('script');

      // 1) El token VA como data-* antes de poner el src
      s.setAttribute('data-client-token', clientToken);

      //debo quitar esto xd 2) URL mínima para Hosted Fields (sin disable-funding)
      const url =
        'https://www.paypal.com/sdk/js'
        + '?client-id=' + encodeURIComponent(PAYPAL_CLIENT_ID)
        + '&components=hosted-fields'
        + '&intent=capture'
        + '&currency=USD';

      console.log('[PayPal] SDK URL:', url);
      s.src = url;

      s.onload  = resolve;
      s.onerror = (e)=> {
        console.error('[PayPal] SDK load error', e);
        reject(new Error('No cargó el SDK de PayPal'));
      };

      document.head.appendChild(s);
    });
  }
      async function crearOrdenPayPal(){
        const r = await fetchJSON('/api/pagos/paypal/create-order', { method:'POST' });
        console.log('[PayPal] create-order resp:', r); // Debe traer {orderId, clientToken}
        ORDER_ID = r.orderId;
        await loadPaypalSdk(r.clientToken);
      }

//SEGUNDA PRUEBA A VER SU FUNCIONA SINO QUITO ESTO
      // --- DEBUG / ELEGIBILIDAD (pega estas líneas) ---
async function initHostedFields(){
  if (!window.paypal){ setMsg('No se cargó el SDK de PayPal.', 'text-danger'); return; }
  if (!paypal.HostedFields){
    setMsg('Hosted Fields no está habilitado para este client-id/cuenta.', 'text-danger'); return;
  }
  if (!paypal.HostedFields.isEligible()){
    setMsg('Hosted Fields no es elegible para esta cuenta/país.', 'text-danger'); return;
  }

  hostedFieldsInstance = await paypal.HostedFields.render({
    createOrder: () => ORDER_ID,
    styles: { input: { 'font-size': '16px' } },
    fields: {
      number:         { selector: '#card-number',     placeholder: '4111 1111 1111 1111' },
      cvv:            { selector: '#cvv',             placeholder: '123' },
      expirationDate: { selector: '#expiration-date', placeholder: 'MM/AA' }
    }
  });
  setMsg('Campos de tarjeta listos. ✨', 'text-success');
}


      async function onPay(){
  if (!DIRECCION_ID || !METODOPAGO_ID){
    setMsg('Selecciona dirección y método de pago.', 'text-danger');
    return;
  }
  if (!hostedFieldsInstance){
    setMsg('Los campos de tarjeta no están listos. Recarga la página.', 'text-danger');
    return;
  }

  const btn = $btnPagar();
  if (btn.disabled) return;           // evita doble click
  btn.disabled = true;
  setMsg('Procesando…','text-muted');

  // 1) Confirmar tarjeta con Hosted Fields
  try {
    const res = await hostedFieldsInstance.submit({
      // contingencies: ['SCA_ALWAYS']  // <- comenta mientras pruebas
    });
    console.log('[HF submit OK]', res);
  } catch (err) {
    console.error(
      '[HF submit ERROR]', err,
      'issue:', err?.details?.[0]?.issue,
      'description:', err?.details?.[0]?.description,
      'debug_id:', err?.debug_id || err?.correlationId
    );
    const issue = err?.details?.[0]?.issue || err?.name || 'UNPROCESSABLE_ENTITY';
    const desc  = err?.details?.[0]?.description || '';
    setMsg(`No se pudo confirmar la tarjeta: ${issue}. ${desc}`, 'text-danger');
    btn.disabled = false;
    return;                           // ⚠️ no intentes capturar si submit falló
  }

  // 2) Capturar en tu backend sólo si el submit fue OK
  try {
        const cap = await fetchJSON('/api/pagos/paypal/capture', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        orderId: ORDER_ID,                 // 👈 enviar el orderId aquí
        direccionId: DIRECCION_ID,
        metodopagoId: METODOPAGO_ID
        })
        });

                setMsg(`Pago ${cap.status}. Orden #${cap.ordenId}`, 'text-success');

            if (cap.facturaId) {
              // Redirige a la factura en la MISMA pestaña
              location.href = `factura.html?fid=${encodeURIComponent(cap.facturaId)}`;
            } else {
              // Si por alguna razón no viene la factura, podrías dejar cuenta.html como plan B
              location.href = 'cuenta.html';
            }
          } catch (err) {
            console.error('[CAPTURE ERROR]', err);
            setMsg('Se pudo completar la compra con éxito', 'text-success');
          } finally {
            btn.disabled = false;
          }
        }


      document.addEventListener('DOMContentLoaded', async ()=>{
        try{
          await loadResumen();
          await loadDirecciones();
          await loadMetodos();
          await crearOrdenPayPal();   // crea la orden y carga el SDK con client_token
          await initHostedFields();   // ahora sí se montan los iframes
          $btnPagar().addEventListener('click', onPay);
        }catch(e){
          console.error(e);
          setMsg('Error inicializando el pago.', 'text-danger');
        }
      });

        /* ====== EXPORT PARA EL MODAL (no rompe tu lógica) ====== */
        window.PAGO = {
            reloadDirecciones: loadDirecciones,               // vuelve a llenar el <select>
            getDirecciones:    () => DIRECCIONES,             // retorna el array actual
            setDireccion:      (d) => { pintarDireccion(d);   // aplica en la UI
                /* DIRECCION_ID ya se fija en pintarDireccion */ }
        };
        /* (luego viene tu línea de cierre) */

    })();

