    document.getElementById("formLogin").addEventListener("submit", function(e) {
        e.preventDefault();
        
        const usuario = document.getElementById("usuario").value;
        const contraseña = document.getElementById("contraseña").value;

        fetch('/api/login', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ usuario, contraseña })
        })
        .then(res => res.json())
        .then(data => {
            if (data.success) {
                alert("Login exitoso");
        // Aquí puedes redirigir si quieres
        } else {
        alert("Credenciales inválidas");
        }
        });
    });