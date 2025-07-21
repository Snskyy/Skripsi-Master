const cart = [];
const history = [];

function addToCart() {
  const name = document.getElementById('product-name').value.trim();
  const qty = document.getElementById('product-qty').value;
  const price = document.getElementById('product-price').value;

  if (!name || !qty || !price) {
    alert("Please complete all product fields.");
    return;
  }

  cart.push({ name, qty, price });
  renderCart();

  // Clear inputs
  document.getElementById('product-name').value = '';
  document.getElementById('product-qty').value = '';
  document.getElementById('product-price').value = '';
}

function renderCart() {
  const cartEl = document.getElementById('cart');
  if (cart.length === 0) {
    cartEl.innerHTML = '<p>No items in cart.</p>';
    return;
  }

  let table = `
    <table>
      <thead>
        <tr>
          <th>No</th>
          <th>Product Name</th>
          <th>Quantity</th>
          <th>Price</th>
        </tr>
      </thead>
      <tbody>
  `;

  cart.forEach((item, index) => {
    table += `
      <tr>
        <td>${index + 1}</td>
        <td>${item.name}</td>
        <td>${item.qty}</td>
        <td>${item.price}</td>
      </tr>
    `;
  });

  table += `</tbody></table>`;
  cartEl.innerHTML = table;
}

function checkout() {
  if (cart.length === 0) {
    alert("Cart is empty.");
    return;
  }

  const supplier = document.getElementById('supplier').value;
  const date = new Date().toLocaleString();

  const purchase = {
    id: Date.now(),
    supplier,
    date,
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
      `<div>- ${i.name}: ${i.qty} pcs @ ${i.price}</div>`
    ).join('');

    historyEl.innerHTML += `
      <div class="history-item">
        <strong>Supplier:</strong> ${p.supplier}<br>
        <strong>Date:</strong> ${p.date}<br>
        <button onclick="toggleDetails(${p.id})">View Details</button>
        <div id="details-${p.id}" class="details" style="display:none;">${itemsHTML}</div>
      </div>
    `;
  });
}

function toggleDetails(id) {
  const el = document.getElementById(`details-${id}`);
  el.style.display = el.style.display === 'none' ? 'block' : 'none';
}