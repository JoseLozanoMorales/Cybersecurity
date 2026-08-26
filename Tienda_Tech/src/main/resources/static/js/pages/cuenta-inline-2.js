document.addEventListener('DOMContentLoaded', () => {
      const userJSON = sessionStorage.getItem('user');
      if (!userJSON) return;
      const user = JSON.parse(userJSON);
      document.querySelector('input[name="nombre"]').value   = user.nombre   || '';
      document.querySelector('input[name="usuario"]').value  = user.usuario  || '';
      document.querySelector('input[name="telefono"]').value = user.telefono || '';
      document.querySelector('input[name="cedula"]').value   = user.cedula   || '';
      document.querySelector('input[name="correo"]').value   = user.correo   || '';
    });
  
