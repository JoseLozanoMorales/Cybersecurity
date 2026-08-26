// Función para mostrar mensajes
        function showMessage(message, type) {
            // Remover mensaje anterior si existe
            const existingMessage = document.querySelector('.message');
            if (existingMessage) {
                existingMessage.remove();
            }

            // Crear nuevo mensaje
            const messageDiv = document.createElement('div');
            messageDiv.className = `message ${type}`;
            messageDiv.textContent = message;
            ttSetCssText(messageDiv, `
                position: fixed;
                top: 20px;
                right: 20px;
                padding: 15px 20px;
                border-radius: 8px;
                color: white;
                font-weight: 500;
                z-index: 1000;
                animation: slideIn 0.3s ease;
                ${type === 'success' ? 'background: #4CAF50;' : 'background: #dc3545;'}
            `);

            document.body.appendChild(messageDiv);

            // Remover después de 4 segundos
            setTimeout(() => {
                ttSetStyle(messageDiv, 'animation', 'slideOut 0.3s ease');
                setTimeout(() => messageDiv.remove(), 300);
            }, 4000);
        }

        // Agregar estilos para animaciones de mensajes
        ttAddCss(`
            @keyframes slideIn {
                from { transform: translateX(100%); opacity: 0; }
                to { transform: translateX(0); opacity: 1; }
            }
            @keyframes slideOut {
                from { transform: translateX(0); opacity: 1; }
                to { transform: translateX(100%); opacity: 0; }
            }
        `);

        function showAlert(message) {
            alert(message);
        }

        // Efecto de partículas en el fondo
        function createParticle() {
            const leftSection = document.querySelector('.left-section');
            const particle = document.createElement('div');
            
            ttSetStyle(particle, 'position', 'absolute');
            const particleSize = Math.random() * 4 + 2 + 'px';
            ttSetStyle(particle, 'width', particleSize);
            ttSetStyle(particle, 'height', particleSize);
            ttSetStyle(particle, 'background', 'rgba(255, 255, 255, 0.1)');
            ttSetStyle(particle, 'borderRadius', '50%');
            ttSetStyle(particle, 'left', Math.random() * 100 + '%');
            ttSetStyle(particle, 'top', '100%');
            ttSetStyle(particle, 'pointerEvents', 'none');
            
            leftSection.appendChild(particle);
            
            // Animación de la partícula
            let position = 100;
            const speed = Math.random() * 2 + 1;
            
            const animateParticle = () => {
                position -= speed;
                ttSetStyle(particle, 'top', position + '%');
                
                if (position > -10) {
                    requestAnimationFrame(animateParticle);
                } else {
                    particle.remove();
                }
            };
            
            animateParticle();
        }

        // Crear partículas cada 3 segundos
        setInterval(createParticle, 3000);

        // Efectos de hover en los inputs
        const inputs = document.querySelectorAll('.form-input');
        inputs.forEach(input => {
            input.addEventListener('focus', function() {
                ttSetStyle(this, 'transform', 'translateY(-2px)');
            });
            
            input.addEventListener('blur', function() {
                ttSetStyle(this, 'transform', 'translateY(0)');
            });
        });
    
