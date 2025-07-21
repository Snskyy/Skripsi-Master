function saveUser() {
    const username = document.getElementById('username').value.trim();
    const role = document.getElementById('role').value;

    if (!username) {
      alert("Username is required.");
      return;
    }

    const users = JSON.parse(localStorage.getItem('users')) || [];
    const newUser = {
      id: Date.now(),
      username,
      role
    };

    users.push(newUser);
    localStorage.setItem('users', JSON.stringify(users));
    alert("User added!");
    window.location.href = 'user-management.html';
  }