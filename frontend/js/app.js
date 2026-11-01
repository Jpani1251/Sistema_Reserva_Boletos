// CONFIGURACIÓN DINÁMICA DE APIS (Local vs Azure / Nginx Gateway)
const isHosted = window.location.protocol === 'http:' || window.location.protocol === 'https:';
const API = isHosted ? {
  usuarios: '/api/usuarios',
  lugares: '/api/lugares',
  eventos: '/api/eventos',
  compras: '/api/compras'
} : {
  usuarios: 'http://127.0.0.1:8081',
  lugares: 'http://127.0.0.1:8082',
  eventos: 'http://127.0.0.1:8083',
  compras: 'http://127.0.0.1:8084'
};

// ESTADO GLOBAL
let currentUser = JSON.parse(localStorage.getItem('eventpass_user')) || null;
let currentEvents = [];
let currentPlaces = [];

// INICIALIZACIÓN
document.addEventListener('DOMContentLoaded', () => {
  updateUIForUser();
  if (currentUser) {
    loadEvents();
    if (currentUser.rol === 'cliente') {
      updateCartBadge();
    }
    if (currentUser.rol === 'administrador') {
      loadPlaces();
    }
  }

  // Búsqueda en vivo
  const searchInput = document.getElementById('search-input');
  if (searchInput) {
    let debounceTimer;
    searchInput.addEventListener('input', (e) => {
      clearTimeout(debounceTimer);
      debounceTimer = setTimeout(() => {
        loadEvents(e.target.value.trim());
      }, 350);
    });
  }
});

// GESTIÓN DE NOTIFICACIONES (TOAST)
function showToast(message, type = 'info') {
  const container = document.getElementById('toast-container');
  const toast = document.createElement('div');
  toast.className = `toast toast-${type}`;
  toast.innerHTML = `<span>${message}</span>`;
  container.appendChild(toast);

  setTimeout(() => {
    toast.style.animation = 'slideIn 0.3s ease reverse';
    setTimeout(() => toast.remove(), 300);
  }, 3500);
}

// COMPRESIÓN DE IMÁGENES AUTOMÁTICA EN CANVAS
function compressImage(file, maxWidth = 1200, maxHeight = 1200, quality = 0.8) {
  return new Promise((resolve, reject) => {
    const reader = new FileReader();
    reader.readAsDataURL(file);
    reader.onload = (event) => {
      const img = new Image();
      img.src = event.target.result;
      img.onload = () => {
        let width = img.width;
        let height = img.height;

        if (width > height) {
          if (width > maxWidth) {
            height = Math.round((height * maxWidth) / width);
            width = maxWidth;
          }
        } else {
          if (height > maxHeight) {
            width = Math.round((width * maxHeight) / height);
            height = maxHeight;
          }
        }

        const canvas = document.createElement('canvas');
        canvas.width = width;
        canvas.height = height;
        const ctx = canvas.getContext('2d');
        ctx.drawImage(img, 0, 0, width, height);

        const compressedBase64 = canvas.toDataURL('image/jpeg', quality);
        resolve(compressedBase64);
      };
      img.onerror = () => reject(new Error('No se pudo procesar la imagen seleccionada'));
    };
    reader.onerror = () => reject(new Error('Error al leer el archivo de imagen'));
  });
}

// CONTROL DE MODALES
function openModal(modalId) {
  const modal = document.getElementById(modalId);
  if (modal) {
    modal.classList.add('active');
    if (modalId === 'evento-modal') {
      loadPlaces();
    }
  }
}

function closeModal(modalId) {
  const modal = document.getElementById(modalId);
  if (modal) modal.classList.remove('active');
}

// ACTUALIZAR INTERFAZ SEGÚN SESIÓN
function updateUIForUser() {
  const authBtns = document.getElementById('auth-buttons');
  const userMenu = document.getElementById('user-menu');
  const adminControls = document.getElementById('admin-controls');
  const cartBtn = document.getElementById('cart-btn');
  const userNameSpan = document.getElementById('user-name');
  const userRoleBadge = document.getElementById('user-role-badge');
  const guestBanner = document.getElementById('guest-banner');

  if (currentUser) {
    authBtns.style.display = 'none';
    userMenu.style.display = 'flex';
    userNameSpan.textContent = currentUser.nombre || currentUser.email;
    userRoleBadge.textContent = currentUser.rol;
    guestBanner.style.display = 'none';

    if (currentUser.rol === 'administrador') {
      adminControls.style.display = 'flex';
      cartBtn.style.display = 'none';
    } else {
      adminControls.style.display = 'none';
      cartBtn.style.display = 'flex';
    }
  } else {
    authBtns.style.display = 'flex';
    userMenu.style.display = 'none';
    adminControls.style.display = 'none';
    cartBtn.style.display = 'none';
    guestBanner.style.display = 'block';
    renderEmptyState("Inicia sesión o regístrate para ver y comprar boletos de eventos.");
  }
}

// ==========================================
// 1. AUTENTICACIÓN (ms-usuarios)
// ==========================================
async function handleLogin(e) {
  e.preventDefault();
  const email = document.getElementById('login-email').value.trim();
  const password = document.getElementById('login-password').value.trim();

  try {
    const res = await fetch(`${API.usuarios}/usuarios/login?email=${encodeURIComponent(email)}&password=${encodeURIComponent(password)}`);
    const data = await res.json();

    if (!res.ok) {
      throw new Error(data.mensaje || 'Error al iniciar sesión');
    }

    currentUser = {
      id_usuario: data.id_usuario,
      token: data.token,
      rol: data.rol,
      email: email,
      nombre: email.split('@')[0]
    };

    localStorage.setItem('eventpass_user', JSON.stringify(currentUser));
    closeModal('login-modal');
    showToast(`¡Bienvenido de nuevo, ${currentUser.nombre}!`, 'success');
    updateUIForUser();
    loadEvents();
    if (currentUser.rol === 'administrador') loadPlaces();
    if (currentUser.rol === 'cliente') updateCartBadge();

  } catch (err) {
    showToast(err.message, 'error');
  }
}

async function handleRegister(e) {
  e.preventDefault();
  const nombre = document.getElementById('reg-nombre').value.trim();
  const apellidos = document.getElementById('reg-apellidos').value.trim();
  const email = document.getElementById('reg-email').value.trim();
  const password = document.getElementById('reg-password').value.trim();
  const rol = document.getElementById('reg-rol').value;

  try {
    const res = await fetch(`${API.usuarios}/usuarios/alta_usuario`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ nombre, apellidos, email, password, rol })
    });

    const data = await res.json();
    if (!res.ok) throw new Error(data.mensaje || 'Error al registrarse');

    showToast('Cuenta creada con éxito. Ahora inicia sesión.', 'success');
    closeModal('register-modal');
    openModal('login-modal');
    document.getElementById('login-email').value = email;
  } catch (err) {
    showToast(err.message, 'error');
  }
}

function handleLogout() {
  localStorage.removeItem('eventpass_user');
  currentUser = null;
  currentEvents = [];
  updateUIForUser();
  showToast('Has cerrado sesión correctamente.', 'info');
}

// ==========================================
// 2. EVENTOS (ms-eventos)
// ==========================================
async function loadEvents(palabra = '') {
  if (!currentUser) return;

  const grid = document.getElementById('events-grid');
  grid.innerHTML = '<p style="grid-column: 1/-1; text-align: center; color: var(--gray-600);">Cargando eventos...</p>';

  try {
    const url = `${API.eventos}/eventos/consulta_eventos?palabra=${encodeURIComponent(palabra)}&id_usuario=${currentUser.id_usuario}&token=${encodeURIComponent(currentUser.token)}`;
    const res = await fetch(url);
    const data = await res.json();

    if (!res.ok) throw new Error(data.mensaje || 'Error al cargar eventos');

    currentEvents = data;
    renderEvents(data);
  } catch (err) {
    grid.innerHTML = `<p style="grid-column: 1/-1; text-align: center; color: var(--danger);">${err.message}</p>`;
  }
}

function renderEvents(events) {
  const grid = document.getElementById('events-grid');
  grid.innerHTML = '';

  if (!events || events.length === 0) {
    renderEmptyState('No se encontraron eventos disponibles por el momento.');
    return;
  }

  events.forEach(ev => {
    const card = document.createElement('div');
    card.className = 'event-card';

    const imgHtml = ev.foto
      ? `<img src="data:image/jpeg;base64,${ev.foto}" class="event-img" alt="${ev.nombre}" />`
      : `<div class="event-img-placeholder"></div>`;

    const buySection = currentUser.rol === 'cliente' ? `
      <div class="buy-group">
        <input type="number" id="qty-${ev.id_evento}" min="1" max="10" value="1" />
        <button class="btn btn-primary btn-sm" onclick="addToCart(${ev.id_evento})">
          Comprar
        </button>
      </div>
    ` : `<span class="badge badge-role">Admin</span>`;

    card.innerHTML = `
      ${imgHtml}
      <div class="event-body">
        <h3 class="event-title">${escapeHtml(ev.nombre)}</h3>
        <p class="event-desc">${escapeHtml(ev.descripcion || 'Sin descripción disponible')}</p>
        <div class="event-meta">
          <span> <strong>Lugar:</strong> ${escapeHtml(ev.lugar)}</span>
          <span> <strong>Dirección:</strong> ${escapeHtml(ev.direccion)}</span>
          <span> <strong>Fecha:</strong> ${ev.fecha} ${ev.hora}</span>
        </div>
        <div class="event-footer">
          <div class="event-price">$${parseFloat(ev.precio_boleto).toFixed(2)} <small>MXN</small></div>
          ${buySection}
        </div>
      </div>
    `;
    grid.appendChild(card);
  });
}

function renderEmptyState(message) {
  const grid = document.getElementById('events-grid');
  grid.innerHTML = `
    <div class="empty-state" style="grid-column: 1/-1;">
      <h3>No hay eventos para mostrar</h3>
      <p>${message}</p>
    </div>
  `;
}

// ==========================================
// 3. LUGARES (ms-lugares - Solo Admin)
// ==========================================
async function loadPlaces() {
  try {
    const res = await fetch(`${API.lugares}/lugares/obtiene_lugares`);
    const data = await res.json();
    currentPlaces = data;

    const select = document.getElementById('evento-lugar');
    if (select) {
      select.innerHTML = '<option value="">-- Selecciona un lugar --</option>';
      data.forEach(l => {
        const opt = document.createElement('option');
        opt.value = l.id_lugar;
        opt.textContent = `${l.nombre} (${l.direccion})`;
        select.appendChild(opt);
      });
    }
  } catch (err) {
    console.error('Error al cargar lugares:', err);
  }
}

async function handleAltaLugar(e) {
  e.preventDefault();
  const nombre = document.getElementById('lugar-nombre').value.trim();
  const direccion = document.getElementById('lugar-direccion').value.trim();

  try {
    const res = await fetch(`${API.lugares}/lugares/alta_lugar`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        id_usuario: currentUser.id_usuario,
        token: currentUser.token,
        nombre,
        direccion
      })
    });

    const data = await res.json();
    if (!res.ok) throw new Error(data.mensaje || 'Error al crear lugar');

    showToast('Lugar registrado exitosamente.', 'success');
    closeModal('lugar-modal');
    document.getElementById('form-lugar').reset();
    loadPlaces();
  } catch (err) {
    showToast(err.message, 'error');
  }
}

// ==========================================
// 4. ALTA DE EVENTO (ms-eventos - Solo Admin)
// ==========================================
async function handleAltaEvento(e) {
  e.preventDefault();
  const submitBtn = e.target.querySelector('button[type="submit"]');
  const originalText = submitBtn ? submitBtn.textContent : 'Publicar Evento';

  const nombre = document.getElementById('evento-nombre').value.trim();
  const descripcion = document.getElementById('evento-desc').value.trim();
  const id_lugar = document.getElementById('evento-lugar').value;
  const fechaHora = document.getElementById('evento-fecha').value;
  const precio_boleto = document.getElementById('evento-precio').value;
  const cantidad_boletos = document.getElementById('evento-boletos').value;
  const fotoInput = document.getElementById('evento-foto');

  if (!fotoInput.files || fotoInput.files.length === 0) {
    showToast('Por favor selecciona una foto para el evento', 'error');
    return;
  }

  if (submitBtn) {
    submitBtn.disabled = true;
    submitBtn.textContent = 'Procesando y publicando...';
  }

  try {
    const fotoBase64 = await compressImage(fotoInput.files[0]);

    const payload = {
      id_usuario: currentUser.id_usuario,
      token: currentUser.token,
      nombre,
      descripcion,
      id_lugar: parseInt(id_lugar),
      fecha_hora_evento: fechaHora.replace('T', ' ') + ':00',
      precio_boleto: parseFloat(precio_boleto),
      cantidad_boletos: parseInt(cantidad_boletos),
      foto: fotoBase64
    };

    const res = await fetch(`${API.eventos}/eventos/alta_evento`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload)
    });

    const data = await res.json();
    if (!res.ok) throw new Error(data.mensaje || 'Error al dar de alta el evento');

    showToast('¡Evento y boletos creados exitosamente!', 'success');
    closeModal('evento-modal');
    document.getElementById('form-evento').reset();
    loadEvents();
  } catch (err) {
    showToast(err.message, 'error');
  } finally {
    if (submitBtn) {
      submitBtn.disabled = false;
      submitBtn.textContent = originalText;
    }
  }
}

// ==========================================
// 5. COMPRAS Y CARRITO (ms-compras)
// ==========================================
async function addToCart(idEvento) {
  if (!currentUser) {
    showToast('Debes iniciar sesión para comprar boletos', 'info');
    openModal('login-modal');
    return;
  }

  const qtyInput = document.getElementById(`qty-${idEvento}`);
  const cantidad = parseInt(qtyInput ? qtyInput.value : 1);

  try {
    const res = await fetch(`${API.compras}/compras/compra_boletos`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        id_usuario: currentUser.id_usuario,
        token: currentUser.token,
        id_evento: idEvento,
        cantidad_boletos: cantidad
      })
    });

    const data = await res.json();
    if (!res.ok) throw new Error(data.mensaje || 'Error al agregar al carrito');

    showToast(`Se agregaron ${cantidad} boleto(s) a tu carrito.`, 'success');
    updateCartBadge();
  } catch (err) {
    showToast(err.message, 'error');
  }
}

async function updateCartBadge() {
  if (!currentUser || currentUser.rol !== 'cliente') return;

  try {
    const res = await fetch(`${API.compras}/compras/obtiene_carrito?id_usuario=${currentUser.id_usuario}&token=${encodeURIComponent(currentUser.token)}`);
    const data = await res.json();
    if (res.ok && Array.isArray(data)) {
      const totalItems = data.reduce((acc, item) => acc + item.cantidad_boletos, 0);
      document.getElementById('cart-count').textContent = totalItems;
    }
  } catch (err) {
    console.error('Error al actualizar contador del carrito:', err);
  }
}

async function openCart() {
  if (!currentUser) return;

  const cartList = document.getElementById('cart-items-list');
  const cartTotalSpan = document.getElementById('cart-total-amount');
  cartList.innerHTML = '<p style="text-align:center; color: var(--gray-600);">Cargando tu carrito...</p>';
  openModal('cart-modal');

  try {
    const res = await fetch(`${API.compras}/compras/obtiene_carrito?id_usuario=${currentUser.id_usuario}&token=${encodeURIComponent(currentUser.token)}`);
    const items = await res.json();

    if (!res.ok) throw new Error(items.mensaje || 'Error al obtener carrito');

    cartList.innerHTML = '';
    if (!items || items.length === 0) {
      cartList.innerHTML = '<p style="text-align:center; color: var(--gray-600); padding: 1.5rem 0;">Tu carrito está vacío.</p>';
      cartTotalSpan.textContent = '$0.00 MXN';
      return;
    }

    let totalDinero = 0;
    items.forEach(item => {
      const eventDetails = currentEvents.find(e => e.id_evento === item.id_evento);
      const nombreEvento = eventDetails ? eventDetails.nombre : `Evento #${item.id_evento}`;
      const precioUnitario = eventDetails ? parseFloat(eventDetails.precio_boleto) : 0;
      const subtotal = precioUnitario * item.cantidad_boletos;
      totalDinero += subtotal;

      const div = document.createElement('div');
      div.className = 'cart-item';
      div.innerHTML = `
        <div class="cart-item-info">
          <h4>${escapeHtml(nombreEvento)}</h4>
          <p>${item.cantidad_boletos} boleto(s) × $${precioUnitario.toFixed(2)} = <strong>$${subtotal.toFixed(2)} MXN</strong></p>
        </div>
        <button class="btn btn-danger btn-sm" onclick="removeFromCart(${item.id_evento})">Eliminar</button>
      `;
      cartList.appendChild(div);
    });

    cartTotalSpan.textContent = `$${totalDinero.toFixed(2)} MXN`;
  } catch (err) {
    cartList.innerHTML = `<p style="color:var(--danger); text-align:center;">${err.message}</p>`;
  }
}

async function removeFromCart(idEvento) {
  try {
    const url = `${API.compras}/compras/elimina_evento_carrito_compra?id_usuario=${currentUser.id_usuario}&id_evento=${idEvento}&token=${encodeURIComponent(currentUser.token)}`;
    const res = await fetch(url, { method: 'DELETE' });
    const data = await res.json();

    if (!res.ok) throw new Error(data.mensaje || 'Error al eliminar');

    showToast('Boleto(s) devuelto(s) al stock.', 'info');
    openCart();
    updateCartBadge();
  } catch (err) {
    showToast(err.message, 'error');
  }
}

async function clearCart() {
  if (!confirm('¿Deseas vaciar todo tu carrito y liberar los boletos?')) return;

  try {
    const url = `${API.compras}/compras/elimina_carrito_compra?id_usuario=${currentUser.id_usuario}&token=${encodeURIComponent(currentUser.token)}`;
    const res = await fetch(url, { method: 'DELETE' });
    const data = await res.json();

    if (!res.ok) throw new Error(data.mensaje || 'Error al vaciar');

    showToast('Carrito vaciado exitosamente.', 'info');
    openCart();
    updateCartBadge();
  } catch (err) {
    showToast(err.message, 'error');
  }
}

function checkout() {
  showToast('¡Compra confirmada! Los boletos han quedado reservados a tu nombre.', 'success');
  closeModal('cart-modal');
}

// UTILIDADES
function escapeHtml(str) {
  if (!str) return '';
  return str.replace(/[&<>"']/g, function(m) {
    return {
      '&': '&amp;',
      '<': '&lt;',
      '>': '&gt;',
      '"': '&quot;',
      "'": '&#039;'
    }[m];
  });
}
