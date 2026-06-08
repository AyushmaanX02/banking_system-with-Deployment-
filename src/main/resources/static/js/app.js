// Core SPA logic
const ROOT = document.getElementById('app');

// Utility: get JWT token
function getToken() {
  return localStorage.getItem('jwt');
}

function setToken(token) {
  localStorage.setItem('jwt', token);
}

function clearToken() {
  localStorage.removeItem('jwt');
}

// API helper
async function api(endpoint, method = 'GET', body = null) {
  const headers = { 'Content-Type': 'application/json' };
  const token = getToken();
  if (token) {
    headers['Authorization'] = `Bearer ${token}`;
  }
  const config = { method, headers };
  if (body) config.body = JSON.stringify(body);
  const response = await fetch(endpoint, config);
  if (!response.ok) {
    const err = await response.text();
    throw new Error(err || response.statusText);
  }
  return response.json();
}

// Simple router based on hash
function router() {
  const hash = location.hash || '#/login';
  const route = hash.replace('#', '');
  renderRoute(route);
}

async function renderRoute(route) {
  ROOT.innerHTML = '';
  // Load component based on route
  switch (route) {
    case '/login':
      await import('/js/components/login.js').then(m => m.renderLogin());
      break;
    case '/register':
      await import('/js/components/register.js').then(m => m.renderRegister());
      break;
    case '/dashboard':
      await import('/js/components/dashboard.js').then(m => m.renderDashboard());
      break;
    case '/accounts':
      await import('/js/components/accounts.js').then(m => m.renderAccounts());
      break;
    case '/transactions':
      await import('/js/components/transactions.js').then(m => m.renderTransactions());
      break;
    case '/beneficiaries':
      await import('/js/components/beneficiaries.js').then(m => m.renderBeneficiaries());
      break;
    case '/admin':
      await import('/js/components/admin.js').then(m => m.renderAdmin());
      break;
    default:
      ROOT.innerHTML = `<div class="card"><h2>404 – Not Found</h2></div>`;
  }
  // Highlight active nav (if any)
  highlightNav(route);
}

function highlightNav(route) {
  const links = document.querySelectorAll('.nav-link');
  links.forEach(l => l.classList.toggle('active', l.getAttribute('href') === `#${route}`));
}

// Navigation bar (shown after login)
function renderNav() {
  const nav = document.createElement('nav');
  nav.innerHTML = `
    <a class="nav-link" href="#/dashboard">Dashboard</a>
    <a class="nav-link" href="#/accounts">Accounts</a>
    <a class="nav-link" href="#/transactions">Transactions</a>
    <a class="nav-link" href="#/beneficiaries">Beneficiaries</a>
    <a class="nav-link admin-link" href="#/admin" style="display:none;">Admin</a>
    <a class="nav-link" href="#" id="logoutBtn">Logout</a>
  `;
  ROOT.prepend(nav);
  document.getElementById('logoutBtn').addEventListener('click', e => {
    e.preventDefault();
    clearToken();
    location.hash = '#/login';
  });
}

// After successful login/registration, show nav and go to dashboard
function onAuthSuccess(token) {
  setToken(token);
  renderNav();
  // Show admin link if token contains ROLE_ADMIN (simple check)
  const payload = JSON.parse(atob(token.split('.')[1]));
  if (payload.role && payload.role === 'ADMIN') {
    document.querySelector('.admin-link').style.display = 'inline-block';
  }
  location.hash = '#/dashboard';
}

// Listen to hash changes
window.addEventListener('hashchange', router);
// Initial load
router();
