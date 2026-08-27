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

          function escSiem(s){
              return String(s ?? '').replace(/[&<>"']/g, m => ({
                  '&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'
              }[m]));
          }

          window.renderSiemEvents = function() {
              const box = document.getElementById('siemLogs');
              if (!box) return;
              const filter = document.getElementById('siemFilter')?.value || 'TODOS';
              const filtered = siemEventsRaw.filter(e =>
                  filter === 'TODOS' || String(e.nivel).toUpperCase() === filter
              );
              if (!filtered.length) {
                  box.innerHTML = '<div class="tt-muted-row">No hay eventos que coincidan.</div>';
                  return;
              }

              const nivelClase = { INFO: 'siem-info', ADVERTENCIA: 'siem-advertencia', ALERTA: 'siem-alerta' };
              const resultadoOk   = new Set(['EXITOSO', 'PERMITIDO']);
              const resultadoBad  = new Set(['DENEGADO', 'FALLIDO', 'ERROR', 'RECHAZADO']);

              const filas = filtered.map(e => {
                  const nivel = String(e.nivel || '').toUpperCase();
                  const resultado = String(e.resultado || '').toUpperCase();
                  const resCls = resultadoOk.has(resultado) ? 'siem-result-ok'
                               : resultadoBad.has(resultado) ? 'siem-result-bad'
                               : 'siem-result-warn';
                  const rowCls = nivel === 'ALERTA' ? ' siem-row-alerta'
                               : nivel === 'ADVERTENCIA' ? ' siem-row-advertencia' : '';

                  return `<tr class="${rowCls.trim()}">
                      <td><span class="siem-badge ${nivelClase[nivel] || 'siem-info'}">${escSiem(e.nivel)}</span></td>
                      <td class="siem-fecha">${escSiem(e.fecha)}</td>
                      <td class="siem-tipo">${escSiem(e.tipo)}</td>
                      <td>${escSiem(e.modulo)}</td>
                      <td><span class="siem-result ${resCls}">${escSiem(e.resultado)}</span></td>
                      <td>${escSiem(e.usuario)}</td>
                      <td class="siem-ip">${escSiem(e.ip)}</td>
                      <td class="siem-detalle">${escSiem(e.detalle)}</td>
                  </tr>`;
              }).join('');

              box.innerHTML = `<div class="data-table">
                  <table class="siem-table">
                      <thead><tr>
                          <th>Nivel</th><th>Fecha</th><th>Tipo</th><th>Módulo</th>
                          <th>Resultado</th><th>Usuario</th><th>IP</th><th>Detalle</th>
                      </tr></thead>
                      <tbody>${filas}</tbody>
                  </table>
              </div>`;
          };

          // ---- Exportar a CSV los eventos visibles (según el filtro actual) ----
          function csvCampo(valor) {
              const s = String(valor ?? '');
              // RFC 4180: si contiene coma, comillas o salto de línea, hay que encerrarlo entre comillas
              // y duplicar las comillas internas.
              if (/[",\n\r]/.test(s)) {
                  return '"' + s.replace(/"/g, '""') + '"';
              }
              return s;
          }

          window.exportSiemEvents = function() {
              const filter = document.getElementById('siemFilter')?.value || 'TODOS';
              const filtered = siemEventsRaw.filter(e =>
                  filter === 'TODOS' || String(e.nivel).toUpperCase() === filter
              );

              if (!filtered.length) {
                  alert('No hay eventos para exportar.');
                  return;
              }

              const columnas = ['fecha', 'tipo', 'usuario', 'ip', 'modulo', 'resultado', 'nivel', 'detalle'];
              const encabezados = ['Fecha', 'Tipo', 'Usuario', 'IP', 'Módulo', 'Resultado', 'Nivel', 'Detalle'];

              const filas = filtered.map(e => columnas.map(c => csvCampo(e[c])).join(','));
              // BOM UTF-8 al inicio para que Excel abra bien los acentos/ñ.
              const csv = '﻿' + encabezados.join(',') + '\r\n' + filas.join('\r\n');

              const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' });
              const url = URL.createObjectURL(blob);

              const ahora = new Date();
              const pad = n => String(n).padStart(2, '0');
              const marca = `${ahora.getFullYear()}${pad(ahora.getMonth() + 1)}${pad(ahora.getDate())}-${pad(ahora.getHours())}${pad(ahora.getMinutes())}${pad(ahora.getSeconds())}`;

              const a = document.createElement('a');
              a.href = url;
              a.download = `siem-eventos-${marca}.csv`;
              document.body.appendChild(a);
              a.click();
              document.body.removeChild(a);
              URL.revokeObjectURL(url);
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

          // ---- Auto-actualización mientras el panel SIEM esté visible ----
          const SIEM_REFRESH_MS = 8000;
          let siemAutoRefreshTimer = null;

          function startSiemAutoRefresh(){
              stopSiemAutoRefresh();
              siemAutoRefreshTimer = setInterval(() => {
                  if (!document.hidden) loadSiemEvents();
              }, SIEM_REFRESH_MS);
          }
          function stopSiemAutoRefresh(){
              if (siemAutoRefreshTimer) { clearInterval(siemAutoRefreshTimer); siemAutoRefreshTimer = null; }
          }
          document.addEventListener('visibilitychange', () => {
              if (!document.hidden && document.getElementById('siem-audit')?.classList.contains('active')) {
                  loadSiemEvents();
              }
          });

          document.addEventListener('DOMContentLoaded', () => {
              const orig = window.showSection;
              window.showSection = function(id, el) {
                  if (typeof orig === 'function') orig(id, el);
                  if (id === 'siem-audit') {
                      loadSiemEvents();
                      startSiemAutoRefresh();
                  } else {
                      stopSiemAutoRefresh();
                  }
              };
          });
      })();
  
