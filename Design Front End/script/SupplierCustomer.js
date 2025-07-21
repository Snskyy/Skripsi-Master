const modal = document.getElementById("modalForm");
    const form = document.getElementById("contactForm");
    const tbody = document.querySelector("#contactTable tbody");
    const topList = document.getElementById("topList");
    let dataKontak = [];

    function openModal() {
      modal.style.display = "block";
    }

function closeModal() {
    modal.style.display = "none";
    form.reset();
}

    form.addEventListener("submit", function(e) {
      e.preventDefault();
      const kontak = {
        nama: document.getElementById("nama").value,
        // kontak: document.getElementById("kontak").value,
        alamat: document.getElementById("address").value,
        tipe: document.getElementById("tipe").value,
        total: Math.floor(Math.random() * 1000000) // simulasi transaksi
      };
      dataKontak.push(kontak);
      renderTable();
      renderTopList();
      closeModal();
    });

    function renderTable() {
      tbody.innerHTML = "";
      const filter = document.getElementById("filterTipe").value;
      const search = document.getElementById("searchInput").value.toLowerCase();

      const filtered = dataKontak.filter(item =>
        (filter === "all" || item.tipe === filter || item.tipe === "keduanya") &&
        item.nama.toLowerCase().includes(search)
      );

      filtered.forEach(item => {
        const tr = document.createElement("tr");
        tr.innerHTML = `
          <td>${item.nama}</td>
          <td>${item.tipe}</td>
          <td>${item.kontak}</td>
          <td>Rp${item.total.toLocaleString()}</td>
          <td><button class="btn" onclick="alert('Riwayat belum dibuat')">Detail</button></td>
        `;
        tbody.appendChild(tr);
      });
    }

    function renderTopList() {
      const sorted = [...dataKontak].sort((a, b) => b.total - a.total).slice(0, 3);
      topList.innerHTML = "";
      sorted.forEach(item => {
        topList.innerHTML += `<li>${item.nama} - Rp${item.total.toLocaleString()}</li>`;
      });
    }

    document.getElementById("searchInput").addEventListener("input", renderTable);
    document.getElementById("filterTipe").addEventListener("change", renderTable);

window.onclick = function(event) {
    if (event.target == modal) closeModal();
}

    // Dummy produk untuk riwayat
const dummyProduk = ["Kaos Polos", "Celana Jeans", "Kemeja Flanel", "Jaket Hoodie", "Sepatu Sneakers"];
const jenisTransaksi = ["Pembelian", "Penjualan"];

function generateRiwayat(nama) {
  const riwayat = [];
  for (let i = 0; i < 5; i++) {
    const tanggal = new Date();
    tanggal.setDate(tanggal.getDate() - i);
    const jenis = jenisTransaksi[Math.floor(Math.random() * 2)];
    const produk = dummyProduk[Math.floor(Math.random() * dummyProduk.length)];
    const jumlah = Math.floor(Math.random() * 20) + 1;
    const total = jumlah * (Math.floor(Math.random() * 10000) + 10000);

    riwayat.push({
      tanggal: tanggal.toISOString().split('T')[0],
      jenis,
      produk,
      jumlah,
      total
    });
  }
  return riwayat;
}

function showRiwayat(nama) {
  const modal = document.getElementById("modalRiwayat");
  const tbody = document.getElementById("riwayatBody");
  const title = document.getElementById("riwayatTitle");

  title.textContent = `Nama Kontak: ${nama}`;
  tbody.innerHTML = "";

  const riwayat = generateRiwayat(nama);

  riwayat.forEach(item => {
    const tr = document.createElement("tr");
    tr.innerHTML = `
      <td>${item.tanggal}</td>
      <td>${item.jenis}</td>
      <td>${item.produk}</td>
      <td>${item.jumlah}</td>
      <td>Rp${item.total.toLocaleString()}</td>
    `;
    tbody.appendChild(tr);
  });

  modal.style.display = "block";
}

function closeRiwayat() {
  document.getElementById("modalRiwayat").style.display = "none";
}
