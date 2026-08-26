(function () {
        'use strict';

        // Mensajes flotantes
        function showMessage(message, type) {
            const existingMessage = document.querySelector('.message');
            if (existingMessage) existingMessage.remove();

            const messageDiv = document.createElement('div');
            messageDiv.className = 'message ' + type;
            messageDiv.textContent = message;
            ttSetCssText(messageDiv, `
      position: fixed; top: 20px; right: 20px; padding: 15px 20px;
      border-radius: 8px; color: #fff; font-weight: 500; z-index: 1000;
      animation: slideIn 0.25s ease; ${type === 'success' ? 'background:#4CAF50;' : 'background:#dc3545;'}
    `);
            document.body.appendChild(messageDiv);

            setTimeout(() => {
                ttSetStyle(messageDiv, 'animation', 'slideOut 0.25s ease');
                setTimeout(() => messageDiv.remove(), 250);
            }, 4000);
        }
        window.ttShowMessage = showMessage;

        // Animaciones CSS para mensajes
        ttAddCss(`
    @keyframes slideIn { from { transform: translateX(120%); opacity: 0; } to { transform: translateX(0); opacity: 1; } }
    @keyframes slideOut { from { transform: translateX(0); opacity: 1; } to { transform: translateX(120%); opacity: 0; } }
  `);

        // Partículas del fondo
        function createParticle() {
            const leftSection = document.querySelector('.left-section');
            if (!leftSection) return;
            const particle = document.createElement('div');
            const size = Math.random() * 4 + 2;
            ttSetStyle(particle, 'position', 'absolute');
            ttSetStyle(particle, 'width', size + 'px');
            ttSetStyle(particle, 'height', size + 'px');
            ttSetStyle(particle, 'background', 'rgba(255,255,255,0.3)');
            ttSetStyle(particle, 'borderRadius', '50%');
            ttSetStyle(particle, 'left', Math.random() * 100 + '%');
            ttSetStyle(particle, 'top', '100%');
            ttSetStyle(particle, 'pointerEvents', 'none');
            leftSection.appendChild(particle);

            let position = 100;
            const speed = Math.random() * 2 + 1;
            (function anim() {
                position -= speed;
                ttSetStyle(particle, 'top', position + '%');
                if (position > -10) requestAnimationFrame(anim);
                else particle.remove();
            })();
        }
        setInterval(createParticle, 3000);

        // Efecto de focus en inputs
        document.addEventListener('DOMContentLoaded', () => {
            document.querySelectorAll('.form-input').forEach(input => {
                input.addEventListener('focus', function(){ ttSetStyle(this, 'transform', 'translateY(-2px)'); });
                input.addEventListener('blur', function(){ ttSetStyle(this, 'transform', 'translateY(0)'); });
            });
        });
    })();

