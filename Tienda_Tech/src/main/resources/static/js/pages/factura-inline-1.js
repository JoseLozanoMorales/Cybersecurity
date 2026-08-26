const fmt = n => '$' + Number(n||0).toFixed(2);
    async function fetchJSON(u){
      const r=await fetch(u,{credentials:'include'});
      if(!r.ok) throw new Error(await r.text());
      return r.json();
    }
    (async function(){
      const fid = new URL(location.href).searchParams.get('fid');
      if(!fid){ alert('Factura no especificada'); return; }

      const data = await fetchJSON(`/api/facturas/${encodeURIComponent(fid)}`);
      const e = data.encabezado || {}, items = data.detalle || [];

      document.getElementById('inv-numero').textContent = 'Factura ' + (e.numero || '#'+fid);
      document.getElementById('inv-fecha').textContent  = e.fechaemision || '';
      document.getElementById('inv-nombre').textContent = e.nombre || '';
      document.getElementById('inv-correo').textContent = e.correo || '';
      document.getElementById('inv-telefono').textContent = e.telefono || '';
      document.getElementById('inv-direccion').textContent = e.direccionentrega || '';

      document.getElementById('inv-body').innerHTML = items.map(it => `
        <tr>
          <td>${it.nombre_producto||''}</td>
          <td class="text-end">${it.cantidad||0}</td>
          <td class="text-end">${fmt(it.precio)}</td>
          <td class="text-end">${fmt(it.subtotal)}</td>
          <td class="text-end">${fmt(it.iva)}</td>
          <td class="text-end">${fmt(it.total)}</td>
        </tr>`).join('');

      document.getElementById('tot-sub').textContent = fmt(e.subtotal);
      document.getElementById('tot-tot').textContent = fmt(e.total);

      const a = document.getElementById('btn-pdf');
      if (a) a.href = `/api/facturas/${encodeURIComponent(fid)}/pdf`; // sólo funcionará si implementas el servicio PDF
    })();

