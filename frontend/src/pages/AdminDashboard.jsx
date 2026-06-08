import React, { useState, useEffect } from 'react'
import { adminApi } from '../api/api'

export default function AdminDashboard({ showToast }) {
  const [stats, setStats] = useState(null)
  const [users, setUsers] = useState([])
  const [transactions, setTransactions] = useState([])
  const [auditLogs, setAuditLogs] = useState([])
  const [loading, setLoading] = useState(true)
  const [activeTab, setActiveTab] = useState('users') // 'users', 'transactions', 'audit'

  useEffect(() => {
    loadAllAdminData()
  }, [])

  async function loadAllAdminData() {
    setLoading(true)
    try {
      const [statsData, usersData, txnsData, auditData] = await Promise.all([
        adminApi.dashboard(),
        adminApi.users(),
        adminApi.transactions(),
        adminApi.auditLogs()
      ])
      setStats(statsData)
      setUsers(usersData)
      setTransactions(txnsData)
      setAuditLogs(auditData)
    } catch (err) {
      showToast('Error loading administrative data', 'error')
    } finally {
      setLoading(false)
    }
  }

  const handleFreeze = async (accountNumber) => {
    if (!window.confirm(`Are you sure you want to FREEZE account ${accountNumber}?`)) return
    try {
      await adminApi.freeze(accountNumber)
      showToast(`Account ${accountNumber} has been frozen`, 'success')
      loadAllAdminData()
    } catch (err) {
      showToast('Failed to freeze account', 'error')
    }
  }

  const handleUnfreeze = async (accountNumber) => {
    if (!window.confirm(`Are you sure you want to UNFREEZE account ${accountNumber}?`)) return
    try {
      await adminApi.unfreeze(accountNumber)
      showToast(`Account ${accountNumber} has been unfrozen`, 'success')
      loadAllAdminData()
    } catch (err) {
      showToast('Failed to unfreeze account', 'error')
    }
  }

  if (loading) {
    return (
      <div className="loading-center">
        <div className="spinner"></div>
      </div>
    )
  }

  return (
    <div className="page page-anim">
      {/* ── System Stats Grid ── */}
      <div className="grid-5">
        <div className="card" style={statCardStyle}>
          <div style={labelStyle}>TOTAL USERS</div>
          <div style={numStyle}>{stats?.totalUsers || 0}</div>
        </div>
        <div className="card" style={statCardStyle}>
          <div style={labelStyle}>TOTAL ACCOUNTS</div>
          <div style={numStyle}>{stats?.totalAccounts || 0}</div>
        </div>
        <div className="card" style={statCardStyle}>
          <div style={labelStyle}>ACTIVE ACCOUNTS</div>
          <div style={{ ...numStyle, color: 'var(--success)' }}>{stats?.activeAccounts || 0}</div>
        </div>
        <div className="card" style={statCardStyle}>
          <div style={labelStyle}>FROZEN ACCOUNTS</div>
          <div style={{ ...numStyle, color: 'var(--danger)' }}>{stats?.frozenAccounts || 0}</div>
        </div>
        <div className="card" style={statCardStyle}>
          <div style={labelStyle}>TOTAL OPERATIONS</div>
          <div style={numStyle}>{stats?.totalTransactions || 0}</div>
        </div>
      </div>

      {/* ── Tab Selector ── */}
      <div style={tabContainerStyle}>
        <button
          style={activeTab === 'users' ? activeTabStyle : tabStyle}
          onClick={() => setActiveTab('users')}
        >
          👥 Customers & Accounts
        </button>
        <button
          style={activeTab === 'transactions' ? activeTabStyle : tabStyle}
          onClick={() => setActiveTab('transactions')}
        >
          🧾 Global Transactions
        </button>
        <button
          style={activeTab === 'audit' ? activeTabStyle : tabStyle}
          onClick={() => setActiveTab('audit')}
        >
          🛡️ Security Audit Logs
        </button>
      </div>

      {/* ── Tab Content ── */}
      <div className="card">
        {activeTab === 'users' && (
          <div>
            <h3 className="section-title" style={{ marginBottom: 16 }}>System Customers</h3>
            {users.length > 0 ? (
              <div className="table-wrapper">
                <table>
                  <thead>
                    <tr>
                      <th>Customer Info</th>
                      <th>Email</th>
                      <th>Phone</th>
                      <th>Accounts & Balances</th>
                    </tr>
                  </thead>
                  <tbody>
                    {users.map(u => (
                      <tr key={u.id}>
                        <td>
                          <div className="fw-600">{u.firstName} {u.lastName}</div>
                          <span style={{ fontSize: '0.72rem', color: 'var(--text-muted)' }}>Role: {u.role}</span>
                        </td>
                        <td>{u.email}</td>
                        <td>{u.phone || 'N/A'}</td>
                        <td>
                          {u.accounts && u.accounts.length > 0 ? (
                            <div style={{ display: 'flex', flexDirection: 'column', gap: 6 }}>
                              {u.accounts.map(acc => (
                                <div key={acc.id} style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', padding: '6px 10px', background: 'var(--surface2)', borderRadius: 'var(--radius-sm)', gap: 12 }}>
                                  <span className="mono" style={{ fontSize: '0.8rem' }}>{acc.accountNumber} ({acc.accountType})</span>
                                  <div style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
                                    <span style={{ fontWeight: 600 }}>${parseFloat(acc.balance).toFixed(2)}</span>
                                    <span className={`badge badge-${acc.status === 'ACTIVE' ? 'success' : 'danger'}`} style={{ fontSize: '0.65rem' }}>{acc.status}</span>
                                    {acc.status === 'ACTIVE' ? (
                                      <button className="btn btn-danger btn-sm" style={{ padding: '3px 8px', fontSize: '0.7rem' }} onClick={() => handleFreeze(acc.accountNumber)}>Freeze</button>
                                    ) : (
                                      <button className="btn btn-primary btn-sm" style={{ padding: '3px 8px', fontSize: '0.7rem' }} onClick={() => handleUnfreeze(acc.accountNumber)}>Unfreeze</button>
                                    )}
                                  </div>
                                </div>
                              ))}
                            </div>
                          ) : (
                            <span style={{ color: 'var(--text-muted)' }}>No accounts open</span>
                          )}
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            ) : (
              <div className="empty-state">No registered customers found.</div>
            )}
          </div>
        )}

        {activeTab === 'transactions' && (
          <div>
            <h3 className="section-title" style={{ marginBottom: 16 }}>All System Transactions</h3>
            {transactions.length > 0 ? (
              <div className="table-wrapper" style={{ maxHeight: '500px', overflowY: 'auto' }}>
                <table>
                  <thead>
                    <tr>
                      <th>Timestamp</th>
                      <th>Ref Number</th>
                      <th>Account</th>
                      <th>Type</th>
                      <th>Description</th>
                      <th style={{ textAlign: 'right' }}>Amount</th>
                    </tr>
                  </thead>
                  <tbody>
                    {transactions.map(t => {
                      const isPositive = t.transactionType === 'DEPOSIT'
                      return (
                        <tr key={t.id}>
                          <td>{new Date(t.createdAt).toLocaleString()}</td>
                          <td className="mono">{t.referenceNumber}</td>
                          <td className="mono">{t.accountNumber}</td>
                          <td>
                            <span className={`badge badge-${isPositive ? 'success' : 'danger'}`}>
                              {t.transactionType}
                            </span>
                          </td>
                          <td>{t.description || 'N/A'}</td>
                          <td style={{ textAlign: 'right' }} className={isPositive ? 'amount-positive' : 'amount-negative'}>
                            {isPositive ? '+' : '-'}${parseFloat(t.amount).toFixed(2)}
                          </td>
                        </tr>
                      )
                    })}
                  </tbody>
                </table>
              </div>
            ) : (
              <div className="empty-state">No transactions logged in the system.</div>
            )}
          </div>
        )}

        {activeTab === 'audit' && (
          <div>
            <h3 className="section-title" style={{ marginBottom: 16 }}>System Security Audit Logs</h3>
            {auditLogs.length > 0 ? (
              <div className="table-wrapper" style={{ maxHeight: '500px', overflowY: 'auto' }}>
                <table>
                  <thead>
                    <tr>
                      <th>Timestamp</th>
                      <th>Actor</th>
                      <th>Action</th>
                      <th>Entity</th>
                      <th>Details</th>
                    </tr>
                  </thead>
                  <tbody>
                    {auditLogs.map(log => (
                      <tr key={log.id}>
                        <td>{new Date(log.timestamp).toLocaleString()}</td>
                        <td className="fw-600">{log.userId || 'SYSTEM'}</td>
                        <td>
                          <span className="badge badge-accent" style={{ background: 'hsl(220,10%,25%)', color: 'var(--text)' }}>
                            {log.action}
                          </span>
                        </td>
                        <td>{log.affectedEntity} (ID: {log.entityId})</td>
                        <td style={{ color: 'var(--text-muted)' }}>{log.message}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            ) : (
              <div className="empty-state">No system audit logs found.</div>
            )}
          </div>
        )}
      </div>
    </div>
  )
}

const statCardStyle = {
  textAlign: 'center',
  padding: '16px',
}

const labelStyle = {
  fontSize: '0.72rem',
  color: 'var(--text-muted)',
  letterSpacing: '0.05em',
  marginBottom: '6px',
}

const numStyle = {
  fontSize: '1.6rem',
  fontWeight: '700',
}

const tabContainerStyle = {
  display: 'flex',
  gap: '10px',
  borderBottom: '1px solid var(--border)',
  paddingBottom: '2px',
}

const tabStyle = {
  background: 'none',
  border: 'none',
  color: 'var(--text-muted)',
  padding: '12px 20px',
  cursor: 'pointer',
  fontSize: '0.9rem',
  fontWeight: '600',
  borderBottom: '2px solid transparent',
  transition: 'all var(--transition)',
  fontFamily: 'inherit',
}

const activeTabStyle = {
  ...tabStyle,
  color: 'var(--accent)',
  borderBottom: '2px solid var(--accent)',
}
