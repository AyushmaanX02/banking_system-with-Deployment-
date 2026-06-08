import React from 'react'
import { NavLink } from 'react-router-dom'

export default function Sidebar({ user, onLogout }) {
  const isAdmin = user?.role === 'ADMIN'

  return (
    <div className="sidebar" style={sidebarStyle}>
      <div className="sidebar-brand" style={brandStyle}>
        <div className="auth-logo-icon" style={{ width: 36, height: 36, fontSize: 18, borderRadius: 10 }}>N</div>
        <span style={{ fontSize: '1.2rem', fontWeight: 700 }}>Nex<span style={{ color: 'var(--accent)' }}>Bank</span></span>
      </div>

      <nav className="sidebar-nav" style={navStyle}>
        {isAdmin ? (
          <NavLink
            to="/admin"
            style={({ isActive }) => (isActive ? { ...linkStyle, ...activeLinkStyle } : linkStyle)}
          >
            <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><rect x="3" y="3" width="18" height="18" rx="2" ry="2"></rect><line x1="9" y1="3" x2="9" y2="21"></line></svg>
            Admin Panel
          </NavLink>
          ) : (
            <>
              <NavLink
                to="/dashboard"
                style={({ isActive }) => (isActive ? { ...linkStyle, ...activeLinkStyle } : linkStyle)}
              >
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><rect x="3" y="3" width="7" height="9"></rect><rect x="14" y="3" width="7" height="5"></rect><rect x="14" y="12" width="7" height="9"></rect><rect x="3" y="16" width="7" height="5"></rect></svg>
                Dashboard
              </NavLink>

              <NavLink
                to="/accounts"
                style={({ isActive }) => (isActive ? { ...linkStyle, ...activeLinkStyle } : linkStyle)}
              >
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><rect x="1" y="4" width="22" height="16" rx="2" ry="2"></rect><line x1="1" y1="10" x2="23" y2="10"></line></svg>
                Accounts
              </NavLink>

              <NavLink
                to="/transactions"
                style={({ isActive }) => (isActive ? { ...linkStyle, ...activeLinkStyle } : linkStyle)}
              >
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><polyline points="22 12 18 12 15 21 9 3 6 12 2 12"></polyline></svg>
                Transactions
              </NavLink>

              <NavLink
                to="/beneficiaries"
                style={({ isActive }) => (isActive ? { ...linkStyle, ...activeLinkStyle } : linkStyle)}
              >
                <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"></path><circle cx="9" cy="7" r="4"></circle><path d="M23 21v-2a4 4 0 0 0-3-3.87"></path><path d="M16 3.13a4 4 0 0 1 0 7.75"></path></svg>
                Beneficiaries
              </NavLink>
            </>
          )}
      </nav>

      <div style={{ marginTop: 'auto', padding: '0 20px 20px' }}>
        <button className="btn btn-ghost btn-full btn-sm" onClick={onLogout} style={{ justifyContent: 'center' }}>
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" style={{ marginRight: 6 }}><path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"></path><polyline points="16 17 21 12 16 7"></polyline><line x1="21" y1="12" x2="9" y2="12"></line></svg>
          Logout
        </button>
      </div>
    </div>
  )
}

const sidebarStyle = {
  width: '260px',
  background: 'var(--surface)',
  borderRight: '1px solid var(--border)',
  display: 'flex',
  flexDirection: 'column',
}

const brandStyle = {
  display: 'flex',
  alignItems: 'center',
  gap: '12px',
  padding: '24px 20px',
  borderBottom: '1px solid var(--border)',
}

const navStyle = {
  display: 'flex',
  flexDirection: 'column',
  gap: '6px',
  padding: '24px 16px',
}

const linkStyle = {
  display: 'flex',
  alignItems: 'center',
  gap: '12px',
  padding: '12px 16px',
  borderRadius: 'var(--radius-sm)',
  color: 'var(--text-muted)',
  textDecoration: 'none',
  fontSize: '0.92rem',
  fontWeight: '500',
  transition: 'all 0.2s ease',
}

const activeLinkStyle = {
  background: 'hsl(170, 65%, 42%, 12%)',
  color: 'var(--accent)',
  fontWeight: '600',
}
