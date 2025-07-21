let salesCart = [];
let salesHistory = [];
let currentNota = null;

function addSalesToCart() {
  const name = document.getElementById('sales-name').value.trim();
  const qty = document.getElementById('sales-qty').value;
  const price = document.getElementById('sales-price').value;

  if (!name || !qty || !price) {
    alert("Please complete all fields.");
    return;
  }

  salesCart.push({ name, qty, price });
  renderSalesCart();

  document.getElementById('sales-name').value = '';
  document.getElementById('sales-qty').value = '';
  document.getElementById('sales-price').value = '';
}

function renderSalesCart() {
  const el = document.getElementById('sales-cart');
  if (salesCart.length === 0) {
    el.innerHTML = "<p>Cart is empty.</p>";
    return;
  }

  let table = `
    <table>
      <thead>
        <tr><th>No</th><th>Product</th><th>Qty</th><th>Price</th><th>Actions</th></tr>
      </thead><tbody>
  `;

  salesCart.forEach((item, i) => {
    table += `
      <tr>
        <td>${i + 1}</td>
        <td>${item.name}</td>
        <td><input type="number" value="${item.qty}" onchange="editQty(${i}, this.value)" /></td>
        <td><input type="number" value="${item.price}" onchange="editPrice(${i}, this.value)" /></td>
        <td class="actions">
          <button onclick="removeCartItem(${i})">Delete</button>
        </td>
      </tr>
    `;
  });

  table += '</tbody></table>';
  el.innerHTML = table;
}

function editQty(index, value) {
  salesCart[index].qty = value;
}

function editPrice(index, value) {
  salesCart[index].price = value;
}

function removeCartItem(index) {
  salesCart.splice(index, 1);
  renderSalesCart();
}

function checkoutSales() {
  if (salesCart.length === 0) {
    alert("Cart is empty.");
    return;
  }

  const marketplace = document.getElementById('marketplace').value;
  const date = new Date().toLocaleString();
  const sale = {
    id: Date.now(),
    marketplace,
    date,
    items: [...salesCart]
  };

  salesHistory.push(sale);
  currentNota = sale;
  salesCart = [];
  renderSalesCart();
  renderSalesHistory();
  showModal();
}

function renderSalesHistory() {
    const offlineEl = document.getElementById('history-offline');
    const shopeeEl = document.getElementById('history-shopee');
    const tokopediaEl = document.getElementById('history-tokopedia');
  
    offlineEl.innerHTML = '';
    shopeeEl.innerHTML = '';
    tokopediaEl.innerHTML = '';
  
    salesHistory.forEach(sale => {
      const details = sale.items.map(i =>
        `<div>- ${i.name}: ${i.qty} x ${i.price}</div>`
      ).join('');
  
      const historyItem = `
        <div class="history-item">
          <strong>Marketplace:</strong> ${sale.marketplace}<br>
          <strong>Date:</strong> ${sale.date}<br>
          <button onclick="toggleDetails(${sale.id})">View Details</button>
          <button onclick="generateNota(${sale.id})">Show Nota</button>
          <div id="detail-${sale.id}" class="details" style="display:none;">
            ${details}
          </div>
        </div>
      `;
  
      if (sale.marketplace === "Offline") {
        offlineEl.innerHTML += historyItem;
      } else if (sale.marketplace === "Shopee") {
        shopeeEl.innerHTML += historyItem;
      } else if (sale.marketplace === "Tokopedia") {
        tokopediaEl.innerHTML += historyItem;
      }
    });
  }
  

function toggleDetails(id) {
  const el = document.getElementById(`detail-${id}`);
  el.style.display = el.style.display === 'none' ? 'block' : 'none';
}

function showModal() {
  document.getElementById('confirm-modal').style.display = 'flex';
}

function closeModal() {
  document.getElementById('confirm-modal').style.display = 'none';
  currentNota = null;
}

function downloadNota() {
  if (!currentNota) return;
  let nota = `Nota Penjualan\nMarketplace: ${currentNota.marketplace}\nDate: ${currentNota.date}\n\nItems:\n`;
  currentNota.items.forEach(i => {
    nota += `- ${i.name}: ${i.qty} x ${i.price}\n`;
  });

  const blob = new Blob([nota], { type: "text/plain" });
  const link = document.createElement("a");
  link.href = URL.createObjectURL(blob);
  link.download = "nota-penjualan.txt";
  link.click();

  closeModal();
}

function generateNota(id) {
  const notaData = salesHistory.find(s => s.id === id);
  if (!notaData) return;

  let nota = `Nota Penjualan\nMarketplace: ${notaData.marketplace}\nDate: ${notaData.date}\n\nItems:\n`;
  notaData.items.forEach(i => {
    nota += `- ${i.name}: ${i.qty} x ${i.price}\n`;
  });

  const blob = new Blob([nota], { type: "text/plain" });
  const link = document.createElement("a");
  link.href = URL.createObjectURL(blob);
  link.download = "nota-penjualan.txt";
  link.click();
}