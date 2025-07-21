const pembelianData = JSON.parse(localStorage.getItem("pembelianData")) || [];
const supplierList = JSON.parse(localStorage.getItem("supplierList")) || [];

function renderTable() {
  const keyword = document.getElementById("searchInput").value.toLowerCase();
  const startDate = document.getElementById("startDate").value;
  const endDate = document.getElementById("endDate").value;

  const tbody = document.getElementById("pembelianTable");
  tbody.innerHTML = "";

  pembelianData
    .filter(item => {
      const matchKeyword =
        item.produk.toLowerCase().includes(keyword) ||
        item.supplier.toLowerCase().includes(keyword);

      const tanggalItem = new Date(item.tanggal);
      const isAfterStart = !startDate || tanggalItem >= new Date(startDate);
      const isBeforeEnd = !endDate || tanggalItem <= new Date(endDate + "T23:59:59");

      return matchKeyword && isAfterStart && isBeforeEnd;
    })
    .forEach(entry => {
      const row = `
        <tr>
          <td>${entry.tanggal}</td>
          <td>${entry.produk}</td>
          <td>${entry.jumlah}</td>
          <td>${entry.supplier}</td>
          <td>Rp ${entry.totalHarga.toLocaleString()}</td>
        </tr>
      `;
      tbody.innerHTML += row;
    });
}

function tambahPembelian() {
  const produk = document.getElementById("produkSelect").value;
  const jumlah = parseInt(document.getElementById("jumlahInput").value);
  const supplier = document.getElementById("supplierSelect").value;
  const harga = parseInt(document.getElementById("hargaInput").value);
  const tanggal = document.getElementById("tanggalInput").value;

  if (!produk || !jumlah || !supplier || !harga || !tanggal) {
    alert("Semua field wajib diisi!");
    return;
  }

  const newEntry = {
    produk,
    jumlah,
    supplier,
    harga,
    tanggal,
    totalHarga: jumlah * harga
  };

  pembelianData.push(newEntry);
  localStorage.setItem("pembelianData", JSON.stringify(pembelianData));
  renderTable();

  // Reset form
  document.getElementById("produkSelect").value = "";
  document.getElementById("jumlahInput").value = "";
  document.getElementById("supplierSelect").value = "";
  document.getElementById("hargaInput").value = "";
  document.getElementById("tanggalInput").value = "";
}

function tambahSupplier() {
    const nama = document.getElementById("namaSupplier").value;
    const kontak = document.getElementById("kontakSupplier").value;
    const alamat = document.getElementById("alamatSupplier").value;
  
    if (!nama || !kontak || !alamat) {
      alert("Lengkapi semua field supplier!");
      return;
    }
  
    // Menambahkan supplier ke localStorage
    const supplierList = JSON.parse(localStorage.getItem("supplierList")) || [];
    const newSupplier = { nama, kontak, alamat };
    supplierList.push(newSupplier);
    localStorage.setItem("supplierList", JSON.stringify(supplierList));
  
    // Update dropdown supplier
    updateSupplierDropdown();
  
    // Menutup modal setelah data disimpan
    closeModal();
  }

function renderSupplierTable() {
  const tbody = document.getElementById("supplierTable");
  tbody.innerHTML = "";

  supplierList.forEach(s => {
    const row = `
      <tr>
        <td>${s.nama}</td>
        <td>${s.kontak}</td>
        <td>${s.alamat}</td>
      </tr>
    `;
    tbody.innerHTML += row;
  });
}
function updateSupplierDropdown() {
    const supplierSelect = document.getElementById("supplierSelect");
    supplierSelect.innerHTML = "<option value=''>-- Pilih Supplier --</option>"; // reset dropdown
  
    const supplierList = JSON.parse(localStorage.getItem("supplierList")) || [];
  
    supplierList.forEach(s => {
      const option = document.createElement("option");
      option.value = s.nama;
      option.textContent = s.nama;
      supplierSelect.appendChild(option);
    });
  }

// Modal functions
function openModal() {
    document.getElementById("supplierModal").style.display = "block";
  }
  
  function closeModal() {
    document.getElementById("supplierModal").style.display = "none";
  }
// Initialize
renderTable();
renderSupplierTable();
updateSupplierDropdown();
