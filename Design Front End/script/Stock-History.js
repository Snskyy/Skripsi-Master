const historyLog = JSON.parse(localStorage.getItem("inventoryLog")) || [];

function renderHistoryTable() {
  const keyword = document.getElementById("searchHistory").value.toLowerCase();
  const filter = document.getElementById("filterType").value;
  const startDate = document.getElementById("startDate").value;
  const endDate = document.getElementById("endDate").value;

  const tbody = document.getElementById("historyTable");
  tbody.innerHTML = "";

  historyLog
    .filter(item => {
      const nameMatch = item.name.toLowerCase().includes(keyword);
      const typeMatch = filter === "" || item.type === filter;

      const logDate = new Date(item.time);
      const afterStart = !startDate || new Date(startDate) <= logDate;
      const beforeEnd = !endDate || logDate <= new Date(endDate + "T23:59:59");

      return nameMatch && typeMatch && afterStart && beforeEnd;
    })
    .forEach(log => {
      const row = `
        <tr>
          <td>${log.time}</td>
          <td>${log.name}</td>
          <td>${log.type}</td>
          <td>${log.qty}</td>
          <td>${log.warehouse}</td>
        </tr>
      `;
      tbody.innerHTML += row;
    });
}

document.getElementById("startDate").addEventListener("change", renderHistoryTable);
document.getElementById("endDate").addEventListener("change", renderHistoryTable);

    document.getElementById("searchHistory").addEventListener("input", renderHistoryTable);
    document.getElementById("filterType").addEventListener("change", renderHistoryTable);

    // initial load
    renderHistoryTable();

    new Date().toLocaleString()

    function resetFilter() {
      document.getElementById("searchHistory").value = "";
      document.getElementById("filterType").value = "";
      document.getElementById("startDate").value = "";
      document.getElementById("endDate").value = "";
      renderHistoryTable();
    }
    