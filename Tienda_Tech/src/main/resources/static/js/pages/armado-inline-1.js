function toggleMenu() {
        const menu = document.getElementById('hamburgerMenu');
        const overlay = document.getElementById('overlay');
        menu.classList.toggle('active');
        overlay.classList.toggle('active');
    }

    function closeMenu() {
        const menu = document.getElementById('hamburgerMenu');
        const overlay = document.getElementById('overlay');
        menu.classList.remove('active');
        overlay.classList.remove('active');
    }

    document.addEventListener('DOMContentLoaded', function() {
        const sliderLeft = document.querySelector('.slider-nav.left');
        const sliderRight = document.querySelector('.slider-nav.right');

        sliderLeft.addEventListener('click', function() { console.log('Slider: Anterior'); });
        sliderRight.addEventListener('click', function() { console.log('Slider: Siguiente'); });

        const carouselLeft = document.querySelector('.carousel-nav.left');
        const carouselRight = document.querySelector('.carousel-nav.right');

        carouselLeft.addEventListener('click', function() { console.log('Carrusel: Anterior'); });
        carouselRight.addEventListener('click', function() { console.log('Carrusel: Siguiente'); });

        document.querySelectorAll('.category-card').forEach(card => {
            card.addEventListener('click', function() {
                console.log('Categoría seleccionada:', this.textContent);
            });
        });

        const pcBuilderBtn = document.querySelector('.pc-builder-btn');
        if (pcBuilderBtn) {
            pcBuilderBtn.addEventListener('click', () => console.log('PC Builder: Ver más'));
        }

    });

