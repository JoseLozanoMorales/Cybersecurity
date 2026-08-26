function toggleMenu() {
      const menu = document.getElementById('hamburgerMenu');
      const overlay = document.getElementById('overlay');
      menu.classList.toggle('active');
      if (overlay) overlay.classList.toggle('active');
    }

    function closeMenu() {
      const menu = document.getElementById('hamburgerMenu');
      const overlay = document.getElementById('overlay');
      menu.classList.remove('active');
      if (overlay) overlay.classList.remove('active');
    }




