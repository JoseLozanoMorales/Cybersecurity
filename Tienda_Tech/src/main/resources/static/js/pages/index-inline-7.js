// === mismas utilidades que usas en Búsqueda/Armado ===
    function hasActiveSession(){
        const raw = sessionStorage.getItem('user') || localStorage.getItem('user');
        let ok = false; try { ok = !!(raw && JSON.parse(raw)); } catch { ok = !!raw; }
        return ok || !!localStorage.getItem('token') || !!localStorage.getItem('refreshToken');
    }
    async function tryPost(url, body){
        return fetch(url, {
            method:'POST',
            headers:{'Content-Type':'application/json'},
            credentials:'include',
            body: JSON.stringify(body)
        });
    }
    async function addToCart(productoId, cantidad=1){
        try{
            if (hasActiveSession()){
                let res = await tryPost('/api/carrito/items',  { productoId:Number(productoId), cantidad });
                if(!res.ok) res = await tryPost('/api/carrito/agregar', { productoId:Number(productoId), cantidad });
                if(!res.ok) throw new Error('backend_error');
            } else {
                const key = 'cart';
                const cart = JSON.parse(localStorage.getItem(key) || '[]');
                const f = cart.find(x => String(x.productoId) === String(productoId));
                if (f) f.cantidad += cantidad; else cart.push({ productoId:Number(productoId), cantidad });
                localStorage.setItem(key, JSON.stringify(cart));
            }
            window.showToast && showToast('Añadido al carrito');
            return true;
        } catch (e){
            console.error('No se pudo agregar al carrito', e);
            alert('No se pudo agregar al carrito. Inténtalo de nuevo.');
            return false;
        }
    }

