// Component: Login
export async function renderLogin() {
  const root = document.getElementById('app');
  const container = document.createElement('div');
  container.className = 'card';
  container.innerHTML = `
    <h2>Login</h2>
    <form id="loginForm">
      <input type="email" placeholder="Email" required />
      <input type="password" placeholder="Password" required />
      <button type="submit">Login</button>
    </form>
    <p>Don\'t have an account? <a href="#/register">Register</a></p>
    <div class="spinner" id="loginSpinner" style="display:none;"></div>
  `;
  root.appendChild(container);

  const form = document.getElementById('loginForm');
  const spinner = document.getElementById('loginSpinner');
  form.addEventListener('submit', async (e) => {
    e.preventDefault();
    const email = form.elements[0].value.trim();
    const password = form.elements[1].value.trim();
    spinner.style.display = 'block';
    try {
      const resp = await api('/api/auth/login', 'POST', { email, password });
      onAuthSuccess(resp.token);
    } catch (err) {
      alert('Login failed: ' + err.message);
    } finally {
      spinner.style.display = 'none';
    }
  });
}
