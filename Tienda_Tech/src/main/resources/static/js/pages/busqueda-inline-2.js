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
  
