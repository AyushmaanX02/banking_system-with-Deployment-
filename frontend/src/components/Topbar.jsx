import React from 'react'

export default function Topbar({ user, onLogout }) {
  const name = user ? `${user.firstName} ${user.lastName}` : 'Guest'
  const initials = user ? `${user.firstName[0]}${user.lastName[0]}`.toUpperCase() : 'G'

  return (
    <header style={topbarStyle}>
      <div>
        <h2 style={{ fontSize: '1.05rem', fontWeight: 600 }}>
          {user?.role === 'ADMIN' ? 'Admin Dashboard' : 'Welcome back'}
        </h2>
        <p style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>
          {user?.role === 'ADMIN' ? 'System Management Area' : 'Manage your money on the go'}
        </p>
      </div>

      <div style={{ display: 'flex', alignItems: 'center', gap: 14 }}>
        <div style={avatarStyle}>
          {initials}
        </div>
        <div style={{ textAlign: 'left' }}>
          <div style={{ fontSize: '0.88rem', fontWeight: 600 }}>{name}</div>
          <div style={{ fontSize: '0.72rem', color: 'var(--accent)', textTransform: 'uppercase', fontWeight: 'bold', letterSpacing: '0.05em' }}>
            {user?.role}
          </div>
        </div>
      </div>
    </header>
  )
}

const topbarStyle = {
  height: '70px',
  background: 'var(--surface)',
  borderBottom: '1px solid var(--border)',
  display: 'flex',
  alignItems: 'center',
  justifyContent: 'space-between',
  padding: '0 28px',
}

const avatarStyle = {
  width: '38px',
  height: '38px',
  borderRadius: '50%',
  background: 'linear-gradient(135deg, var(--accent), var(--accent-d))',
  color: '#fff',
  display: 'flex',
  alignItems: 'center',
  justifyContent: 'center',
  fontSize: '0.85rem',
  fontWeight: '700',
  boxShadow: '0 2px 10px rgba(0,0,0,0.2)',
}
