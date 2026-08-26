(() => {
          let siemEventsRaw = [];

          window.loadSiemEvents = async function() {
              const box = document.getElementById('siemLogs');
              if (!box) return;
              box.innerHTML = '<div class="csp-s-71d35497595d">Cargando…</div>';
              try {
                  const res = await fetch('/api/siem/events');
                  if (!res.ok) throw new Error('Error al obtener eventos SIEM');
                  siemEventsRaw = await res.json();
                  renderSiemEvents();
              } catch (err) {
                  box.innerHTML = `<div class="csp-s-427a6f9dbd31">❌ ${err.message}</div>`;
              }
          };

          window.renderSiemEvents = function() {
              const box = document.getElementById('siemLogs');
              if (!box) return;
              const filter = document.getElementById('siemFilter')?.value || 'TODOS';
              const filtered = siemEventsRaw.filter(e =>
                  filter === 'TODOS' || String(e.nivel).toUpperCase() === filter
              );
              if (!filtered.length) {
                  box.innerHTML = '<div class="csp-s-71d35497595d">No hay eventos que coincidan.</div>';
                  return;
              }
              box.innerHTML = filtered.map(e => {
                  let cls = '';
                  if (e.nivel === 'ADVERTENCIA') cls = ' log-warn';
                  if (e.nivel === 'ALERTA')      cls = ' log-err';
                  return `<div class="siem-log-entry${cls}">
                      <strong>[${e.nivel}] - ${e.tipo}</strong> | Fecha: ${e.fecha} | IP: ${e.ip}<br>
                      <strong>Módulo:</strong> ${e.modulo} | <strong>Resultado:</strong> ${e.resultado} | <strong>Usuario:</strong> ${e.usuario}<br>
                      <strong>Detalle:</strong> ${e.detalle}
                  </div>`;
              }).join('');
          };

          window.clearSiemEvents = async function() {
              if (!confirm('¿Limpiar el historial de auditoría en memoria?')) return;
              try {
                  const res = await fetch('/api/siem/clear', { method: 'POST' });
                  if (!res.ok) throw new Error('No se pudo limpiar');
                  alert('✅ Historial limpiado');
                  loadSiemEvents();
              } catch (err) {
                  alert('❌ Error: ' + err.message);
              }
          };

          document.addEventListener('DOMContentLoaded', () => {
              const orig = window.showSection;
              window.showSection = function(id, el) {
                  if (typeof orig === 'function') orig(id, el);
                  if (id === 'siem-audit') loadSiemEvents();
              };
          });
      })();
  
