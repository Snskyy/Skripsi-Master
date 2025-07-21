const toggleBtn = document.getElementById('toggleBtn');
const toggleIcon = document.getElementById('toggleIcon');
const sidebar = document.getElementById('sidebar');

toggleBtn.addEventListener('click', () => {
  sidebar.classList.toggle('collapsed');

  // Toggle icon between burger and X
  if (sidebar.classList.contains('collapsed')) {
    toggleIcon.classList.remove('fa-xmark');
    toggleIcon.classList.add('fa-bars');
  } else {
   
    toggleIcon.classList.remove('fa-bars');
    toggleIcon.classList.add('fa-xmark');
  }
});