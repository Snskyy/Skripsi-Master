const dummyProduk = ["Kaos", "Celana", "Sepatu", "Topi", "Jaket"];

function tampilkanLaporan() {
  const tbody = document.querySelector("#tabelLaporan tbody");
  tbody.innerHTML = "";

  const jenis = document.getElementById("jenisLaporan").value;
  const mulai = document.getElementById("tanggalMulai").value;
  const akhir = document.getElementById("tanggalAkhir").value;

  for (let i = 0; i < 10; i++) {
    const tanggal = new Date();
    tanggal.setDate(tanggal.getDate() - i);
    const jenisTransaksi = jenis === "penjualan" ? "Penjualan" :
                           jenis === "pembelian" ? "Pembelian" :
                           jenis === "retur" ? "Retur" : "Stok";

    const produk = dummyProduk[Math.floor(Math.random() * dummyProduk.length)];
    const jumlah = Math.floor(Math.random() * 20) + 1;
    const total = jumlah * (Math.floor(Math.random() * 10000) + 10000);

    const tr = document.createElement("tr");
    tr.innerHTML = `
      <td>${tanggal.toISOString().split("T")[0]}</td>
      <td>${jenisTransaksi}</td>
      <td>${produk}</td>
      <td>${jumlah}</td>
      <td>Rp${total.toLocaleString()}</td>
    `;
    tbody.appendChild(tr);
  }
}

function exportExcel() {
  alert("Export ke Excel belum aktif. (Next step pakai SheetJS)");
}

function exportPDF() {
  alert("Export ke PDF belum aktif. (Next step pakai jsPDF)");
}