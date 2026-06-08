// Component: Dashboard
export async function renderDashboard() {
  const root = document.getElementById('app');
  const container = document.createElement('div');
  container.className = 'card';
  container.innerHTML = `
    <h2>Dashboard</h2>
    <p>Welcome! Use the navigation to access your accounts.</p>
  `;
  root.appendChild(container);
}
