// Component: Register
export async function renderRegister() {
  const root = document.getElementById('app');
  const container = document.createElement('div');
  container.className = 'card';
  container.innerHTML = `
    <h2>Register</h2>
    <form id="registerForm">
      <input type="text" placeholder="Full Name" required />
      <input type="email" placeholder="Email" required />
      <input type="password" placeholder="Password" required />
      <button type="submit">Register</button>
    </form>
    <p>Already have an account? <a href="#/login">Login</a></p>
    <div class="spinner" id="registerSpinner" style="display:none;"></div>
  `;
  root.appendChild(container);

  const form = document.getElementById('registerForm');
  const spinner = document.getElementById('registerSpinner');
  form.addEventListener('submit', async (e) => {
    e.preventDefault();
    const fullName = form.elements[0].value.trim();
    const email = form.elements[1].value.trim();
    const password = form.elements[2].value.trim();
    spinner.style.display = 'block';
    try {
      const resp = await api('/api/auth/register', 'POST', { fullName, email, password });
      onAuthSuccess(resp.token);
    } catch (err) {
      alert('Registration failed: ' + err.message);
    } finally {
      spinner.style.display = 'none';
    }
  });
}
