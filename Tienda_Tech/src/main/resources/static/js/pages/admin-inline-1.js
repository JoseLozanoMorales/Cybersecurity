document.addEventListener('DOMContentLoaded', () => {
          const sel     = document.getElementById('rep-select');
          const radios  = Array.from(document.querySelectorAll('input[name="rep-modo"]'));
          const iDesde  = document.getElementById('rep-desde');
          const iHasta  = document.getElementById('rep-hasta');
          const iProd   = document.getElementById('rep-producto-id');
          const btnDl   = document.getElementById('rep-descargar');
          const btnView = document.getElementById('rep-ver');

          const card    = document.getElementById('rep-viewer-card');
          const iframe  = document.getElementById('rep-iframe');
          const ttl     = document.getElementById('rep-viewer-title');
          const btnClose= document.getElementById('rep-cerrar-visor');

          // IMPORTANTE: como admin.html vive en http://localhost:8080, usamos exactamente ese origen
          const API_ORIGIN = window.location.origin; // http://localhost:8080

          const ENDPOINTS = {
              general:          '/api/report/admin/pdf',
              usuarios:         '/api/report/usuarios/pdf',
              productos:        '/api/report/productos/pdf',
              ordenes:          '/api/report/ordenes/pdf',
              'stock-bajo':     '/api/report/stock-bajo/pdf',
              'ventas-producto':'/api/report/ventas-producto/pdf',
              roles:            '/api/report/roles/pdf',
              ciudades:         '/api/report/ciudades/pdf',
              provincias:       '/api/report/provincias/pdf',
              'metodos-pago':   '/api/report/metodos-pago/pdf',
              kardex:           '/api/report/kardex/pdf'
          };

          const hoy = new Date();
          const dfltHasta = hoy.toISOString().slice(0,10);
          const dfltDesde = new Date(hoy.getTime() - 30*24*60*60*1000).toISOString().slice(0,10);
          if (!iDesde.value) iDesde.value = dfltDesde;
          if (!iHasta.value) iHasta.value = dfltHasta;

          function refreshFechas() {
              const byRange = (radios.find(r => r.checked) || {}).value === 'rango';
              iDesde.disabled = !byRange;
              iHasta.disabled = !byRange;
          }
          radios.forEach(r => r.addEventListener('change', refreshFechas));
          refreshFechas();

          function refreshProducto() {
              const isKardex = sel.value === 'kardex';
              iProd.disabled = !isKardex;
              if (!isKardex) iProd.value = '';
          }
          sel.addEventListener('change', refreshProducto);
          refreshProducto();

          function buildUrl({ inline } = { inline:false }) {
              const key = sel.value || 'general';
              const base = ENDPOINTS[key] || ENDPOINTS.general;
              const url = new URL(base, API_ORIGIN); // mismo origen: http://localhost:8080

              const modo = (radios.find(r => r.checked) || {}).value;
              if (modo === 'rango') {
                  if (iDesde.value) url.searchParams.set('desde', iDesde.value);
                  if (iHasta.value) url.searchParams.set('hasta', iHasta.value);
              }
              if (key === 'kardex') {
                  const pid = (iProd.value || '').trim();
                  if (pid) url.searchParams.set('productoId', pid);
              }
              if (inline) url.searchParams.set('inline', 'true');
              return url.toString();
          }

          btnDl.addEventListener('click', (e) => {
              e.preventDefault();
              window.location.href = buildUrl({ inline:false });
          });

          btnView.addEventListener('click', (e) => {
              e.preventDefault();
              iframe.src = buildUrl({ inline:true });
              ttl.textContent = 'Visor de PDF — ' + (sel.options[sel.selectedIndex]?.text || '');
              ttSetStyle(card, 'display', '');
              card.scrollIntoView({ behavior:'smooth', block:'start' });
          });

          btnClose.addEventListener('click', () => {
              iframe.src = 'about:blank';
              ttSetStyle(card, 'display', 'none');
          });
      });
  
