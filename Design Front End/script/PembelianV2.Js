const cart = [];
    const history = [];

    function addToCart() {
      const name = document.getElementById('product-name').value;
      const qty = document.getElementById('product-qty').value;
      const price = document.getElementById('product-price').value;

      if (!name || !qty || !price) return alert("Please fill all fields.");

      cart.push({ name, qty, price });
      renderCart();
      
      // Clear fields
      document.getElementById('product-name').value = '';
      document.getElementById('product-qty').value = '';
      document.getElementById('product-price').value = '';
    }

    function renderCart() {
      const cartEl = document.getElementById('cart');
      cartEl.innerHTML = '';
      cart.forEach((item, index) => {
        cartEl.innerHTML += `<div class="cart-item">${item.name} - ${item.qty} pcs @ ${item.price}</div>`;
      });
    }

    function checkout() {
      if (cart.length === 0) return alert("Cart is empty.");
      
      const supplier = document.getElementById('supplier').value;
      const timestamp = new Date().toLocaleString();
      
      const purchase = {
        id: Date.now(),
        supplier,
        date: timestamp,
        items: [...cart]
      };

      history.push(purchase);
      cart.length = 0;
      renderCart();
      renderHistory();
    }

    function renderHistory() {
      const historyEl = document.getElementById('history');
      historyEl.innerHTML = '';

      history.forEach(p => {
        const itemsHTML = p.items.map(i =>
          `<div class="details">- ${i.name}: ${i.qty} pcs @ ${i.price}</div>`
        ).join('');

        historyEl.innerHTML += `
          <div class="history-item">
            <strong>Purchase from ${p.supplier} on ${p.date}</strong>
            <button onclick="toggleDetails(${p.id})">View Details</button>
            <div id="details-${p.id}" style="display:none;">${itemsHTML}</div>
          </div>
        `;
      });
    }

    function toggleDetails(id) {
      const el = document.getElementById(`details-${id}`);
      el.style.display = el.style.display === 'none' ? 'block' : 'none';
    }