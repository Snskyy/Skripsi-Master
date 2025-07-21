
const stockData = [
    { name: "Kaos Polos", warehouse: "Gudang A", stock: 5 },
    { name: "Kemeja Flanel", warehouse: "Gudang A", stock: 25 },
    { name: "Celana Jeans", warehouse: "Gudang B", stock: 8 },
    { name: "Hoodie", warehouse: "Gudang B", stock: 40 },
  ];

  function renderTable(data) {
    const table = document.getElementById("stockTable");
    table.innerHTML = "";
    data.forEach((item, index) => {
      const statusClass = item.stock <= 10 ? 'status-low' : 'status-ok';
      const statusText = item.stock <= 10 ? 'Minimum!' : 'Aman';
  
      table.innerHTML += `
        <tr onclick="openModalFromRow('${item.name}', '${item.warehouse}')" style="cursor: pointer;">
          <td>${item.name}</td>
          <td>${item.warehouse}</td>
          <td>${item.stock}</td>
          <td>${item.stock}</td>
          

        </tr>
      `;
    });
  }

  function openModalFromRow(productName, warehouseName) {
    document.getElementById("modalProduct").value = productName;
    document.getElementById("modalWarehouse").value = warehouseName;
    document.getElementById("modalQty").value = "";
    openModal();
  }
    

  function adjustStock() {
    const name = document.getElementById("productName").value.trim();
    const qty = parseInt(document.getElementById("qtyChange").value);
    const warehouse = document.getElementById("warehouse").value;

    if (!name || isNaN(qty)) return alert("Mohon isi semua kolom dengan benar.");

    const item = stockData.find(p => p.name.toLowerCase() === name.toLowerCase() && p.warehouse === warehouse);
    if (item) {
      item.stock += qty;
    } else {
      stockData.push({ name, warehouse, stock: qty });
    }

    renderTable(stockData);
    document.getElementById("productName").value = '';
    document.getElementById("qtyChange").value = '';
  }

  document.getElementById("search").addEventListener("input", function () {
    const keyword = this.value.toLowerCase();
    const filtered = stockData.filter(p => p.name.toLowerCase().includes(keyword));
    renderTable(filtered);
  });

  document.getElementById("warehouse").addEventListener("change", function () {
    const selected = this.value;
    const filtered = stockData.filter(p => p.warehouse === selected);
    renderTable(filtered);
  });

 


  function openModal() {
    document.getElementById("stockModal").style.display = "block";
  }
  
  function closeModal() {
    document.getElementById("stockModal").style.display = "none";
  }
  
  function addStockFromModal() {
    const name = document.getElementById("modalProduct").value.trim();
    const qty = parseInt(document.getElementById("modalQty").value);
    const warehouse = document.getElementById("modalWarehouse").value;
  
    if (!name || isNaN(qty)) return alert("Isi semua field dengan benar!");
  
    const existing = stockData.find(p => p.name.toLowerCase() === name.toLowerCase() && p.warehouse === warehouse);
    if (existing) {
      existing.stock += qty;
    } else {
      stockData.push({ name, warehouse, stock: qty });
    }
  
    renderTable(stockData);
    closeModal();
    
  
    // Reset form
    document.getElementById("modalProduct").value = "";
    document.getElementById("modalQty").value = "";
  }

//   document.getElementById("searchInput").addEventListener("input", function () {
//     const keyword = this.value.toLowerCase();
//     const filtered = stockData.filter(item =>
//       item.name.toLowerCase().includes(keyword)
//     );
//     renderTable(filtered);
//   });
renderTable(stockData);