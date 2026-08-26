function showSection(sectionId, el) {
            // Ocultar todas las secciones
            document.querySelectorAll('.admin-section').forEach(s => s.classList.remove('active'));
            // Quitar active de todos los nav items
            document.querySelectorAll('.nav-item').forEach(i => i.classList.remove('active'));
            // Mostrar la sección seleccionada y activar el item
            document.getElementById(sectionId)?.classList.add('active');
            el?.classList.add('active');
        }

        // Funcionalidad adicional para botones
        document.addEventListener('DOMContentLoaded', function() {
            // Agregar eventos a los botones
            const buttons = document.querySelectorAll('.btn');
            buttons.forEach(button => {
                button.addEventListener('click', function() {
                    const action = this.textContent.trim();

                    // Simular acciones
                    if(action.includes('Agregar') || action.includes('Actualizar') || action.includes('Guardar')) {
                        ttSetStyle(this, 'backgroundColor', '#4caf50');
                        this.textContent = '✓ Completado';
                        setTimeout(() => {
                            ttSetStyle(this, 'backgroundColor', '');
                            this.textContent = action;
                        }, 2000);
                    }

                    if(action.includes('Eliminar') || action.includes('Bloquear')) {
                        if(confirm('¿Estás seguro de realizar esta acción?')) {
                            ttSetStyle(this, 'backgroundColor', '#ff5722');
                            this.textContent = '✓ Realizado';
                            setTimeout(() => {
                                ttSetStyle(this, 'backgroundColor', '');
                                this.textContent = action;
                            }, 2000);
                        }
                    }
                });
            });
        });
    
