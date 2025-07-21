const users = [
    { id: 1, username: 'admin_user', role: 'admin' },
    { id: 2, username: 'staff_john', role: 'staff' },
    { id: 3, username: 'viewer_anna', role: 'viewer' }
  ];

  const roles = {
    admin:    { canAdd: true, canEdit: true, canDelete: true, canView: true },
    staff:    { canAdd: true, canEdit: true, canDelete: false, canView: true },
    viewer:   { canAdd: false, canEdit: false, canDelete: false, canView: true }
  };

  const auditTrail = [];

  function renderUsers() {
    const tbody = document.querySelector('#user-table tbody');
    tbody.innerHTML = '';
    users.forEach(user => {
      const row = document.createElement('tr');
      row.innerHTML = `
        <td>${user.id}</td>
        <td>${user.username}</td>
        <td>${user.role}</td>
        <td>
          <select onchange="changeRole(${user.id}, this.value)">
            <option value="admin" ${user.role === 'admin' ? 'selected' : ''}>Admin</option>
            <option value="staff" ${user.role === 'staff' ? 'selected' : ''}>Staff</option>
            <option value="viewer" ${user.role === 'viewer' ? 'selected' : ''}>Viewer</option>
          </select>
        </td>
      `;
      tbody.appendChild(row);
    });
  }

  function renderPermissions() {
    const tbody = document.getElementById('role-settings');
    tbody.innerHTML = '';
    Object.keys(roles).forEach(role => {
      const p = roles[role];
      const row = document.createElement('tr');
      row.innerHTML = `
        <td>${role}</td>
        <td><input type="checkbox" ${p.canAdd ? 'checked' : ''} onchange="togglePermission('${role}', 'canAdd', this.checked)" /></td>
        <td><input type="checkbox" ${p.canEdit ? 'checked' : ''} onchange="togglePermission('${role}', 'canEdit', this.checked)" /></td>
        <td><input type="checkbox" ${p.canDelete ? 'checked' : ''} onchange="togglePermission('${role}', 'canDelete', this.checked)" /></td>
        <td><input type="checkbox" ${p.canView ? 'checked' : ''} onchange="togglePermission('${role}', 'canView', this.checked)" /></td>
      `;
      tbody.appendChild(row);
    });
  }

  function changeRole(userId, newRole) {
    const user = users.find(u => u.id === userId);
    if (user) {
      const oldRole = user.role;
      user.role = newRole;
      addAudit(`🔄 Changed role for "${user.username}" from "${oldRole}" to "${newRole}"`);
      renderUsers();
    }
  }

  function togglePermission(role, permission, value) {
    roles[role][permission] = value;
    addAudit(`⚙️ Updated "${permission}" permission for role "${role}" to ${value}`);
  }

  function addAudit(message) {
    const time = new Date().toLocaleString();
    auditTrail.unshift(`[${time}] ${message}`);
    renderAudit();
  }

  function renderAudit() {
    const log = document.getElementById('audit-trail');
    log.innerHTML = auditTrail.slice(0, 10).map(entry => `<div>${entry}</div>`).join('');
  }

  // Initialize
  renderUsers();
  renderPermissions();
  renderAudit();