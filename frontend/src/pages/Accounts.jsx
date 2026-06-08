import React, { useState, useEffect } from 'react'
import { accountApi, transactionApi } from '../api/api'

export default function Accounts({ showToast }) {
  const [accounts, setAccounts] = useState([])
  const [loading, setLoading] = useState(true)
  const [isModalOpen, setIsModalOpen] = useState(false)
  const [newAccType, setNewAccType] = useState('SAVINGS')
  const [submitting, setSubmitting] = useState(false)

  // Details modal
  const [selectedAccount, setSelectedAccount] = useState(null)
  const [txns, setTxns] = useState([])
  const [txnsLoading, setTxnsLoading] = useState(false)

  useEffect(() => {
    loadAccounts()
  }, [])

  async function loadAccounts() {
    try {
      const data = await accountApi.getAll()
      setAccounts(data)
    } catch (err) {
      showToast('Failed to load accounts', 'error')
    } finally {
      setLoading(false)
    }
  }

  const handleOpenAccount = async (e) => {
    e.preventDefault()
    setSubmitting(true)
    try {
      await accountApi.create({ accountType: newAccType })
      showToast(`Successfully created new ${newAccType} account!`, 'success')
      setIsModalOpen(false)
      loadAccounts()
    } catch (err) {
      showToast(err.response?.data?.message || 'Failed to create account', 'error')
    } finally {
      setSubmitting(false)
    }
  }

  const handleSelectAccount = async (acc) => {
    setSelectedAccount(acc)
    setTxnsLoading(true)
    try {
      const data = await transactionApi.history(acc.accountNumber)
      setTxns(data)
    } catch (err) {
      showToast('Error loading transaction history', 'error')
    } finally {
      setTxnsLoading(false)
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
      <div className="section-header">
        <div>
          <h2 className="section-title">My Accounts</h2>
          <p style={{ fontSize: '0.85rem', color: 'var(--text-muted)' }}>Open and view your savings, current, and fixed-deposit accounts</p>
        </div>
        <button className="btn btn-primary" onClick={() => setIsModalOpen(true)}>
          + Open New Account
        </button>
      </div>

      {accounts.length > 0 ? (
        <div className="grid-3">
          {accounts.map(acc => (
            <div
              key={acc.id}
              className={`account-card ${acc.accountType.toLowerCase()}`}
              onClick={() => handleSelectAccount(acc)}
            >
              <div className="account-num">{acc.accountNumber}</div>
              <div className="account-balance">${parseFloat(acc.balance).toFixed(2)}</div>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                <span className="account-type-label">{acc.accountType}</span>
                <span className={`badge badge-${acc.status === 'ACTIVE' ? 'success' : 'danger'}`}>
                  {acc.status}
                </span>
              </div>
            </div>
          ))}
        </div>
      ) : (
        <div className="card empty-state">
          <p className="empty-icon">🏦</p>
          <h3>No Accounts Found</h3>
          <p style={{ margin: '10px 0 20px', color: 'var(--text-muted)' }}>You don't have any banking accounts yet. Create one to get started.</p>
          <button className="btn btn-primary" onClick={() => setIsModalOpen(true)}>Open New Account</button>
        </div>
      )}

      {/* ── Open Account Modal ── */}
      {isModalOpen && (
        <div className="modal-overlay">
          <div className="modal">
            <div className="modal-header">
              <h3 className="modal-title">Open New Bank Account</h3>
              <button className="modal-close" onClick={() => setIsModalOpen(false)}>×</button>
            </div>
            <form onSubmit={handleOpenAccount}>
              <div className="form-group">
                <label className="form-label">Account Type</label>
                <select
                  className="form-select"
                  value={newAccType}
                  onChange={e => setNewAccType(e.target.value)}
                >
                  <option value="SAVINGS">Savings Account (Standard interest)</option>
                  <option value="CURRENT">Current Account (No limits, low interest)</option>
                  <option value="FIXED">Fixed Deposit (Locked periods, higher yield)</option>
                </select>
              </div>
              <div style={{ padding: '12px', background: 'hsl(222, 13%, 12%)', borderRadius: 'var(--radius-sm)', marginBottom: '16px', fontSize: '0.8rem', color: 'var(--text-muted)' }}>
                ℹ️ Opening a new account is instant. Your new balance will start at $0.00. You can fund it immediately from the Transactions tab.
              </div>
              <div className="modal-footer">
                <button type="button" className="btn btn-ghost" onClick={() => setIsModalOpen(false)}>Cancel</button>
                <button type="submit" className="btn btn-primary" disabled={submitting}>
                  {submitting ? 'Creating...' : 'Open Account'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* ── Account Details & History Modal ── */}
      {selectedAccount && (
        <div className="modal-overlay">
          <div className="modal" style={{ maxWidth: '640px' }}>
            <div className="modal-header">
              <div>
                <h3 className="modal-title">Account Details</h3>
                <span className="mono" style={{ color: 'var(--text-muted)' }}>{selectedAccount.accountNumber}</span>
              </div>
              <button className="modal-close" onClick={() => setSelectedAccount(null)}>×</button>
            </div>
            <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 24, paddingBottom: 16, borderBottom: '1px solid var(--border)' }}>
              <div>
                <span className="form-label" style={{ display: 'block' }}>Current Balance</span>
                <span style={{ fontSize: '1.6rem', fontWeight: 700, color: 'var(--accent)' }}>
                  ${parseFloat(selectedAccount.balance).toFixed(2)}
                </span>
              </div>
              <div style={{ textAlign: 'right' }}>
                <span className="form-label" style={{ display: 'block' }}>Type & Status</span>
                <span style={{ marginRight: 8, fontWeight: 600 }}>{selectedAccount.accountType}</span>
                <span className={`badge badge-${selectedAccount.status === 'ACTIVE' ? 'success' : 'danger'}`}>
                  {selectedAccount.status}
                </span>
              </div>
            </div>

            <h4 style={{ fontSize: '0.9rem', marginBottom: 12 }}>Transaction History</h4>
            <div style={{ maxHeight: '280px', overflowY: 'auto' }}>
              {txnsLoading ? (
                <div style={{ textAlign: 'center', padding: '20px' }}>
                  <div className="spinner"></div>
                </div>
              ) : txns.length > 0 ? (
                <div className="table-wrapper">
                  <table>
                    <thead>
                      <tr>
                        <th>Date</th>
                        <th>Reference</th>
                        <th>Type</th>
                        <th style={{ textAlign: 'right' }}>Amount</th>
                      </tr>
                    </thead>
                    <tbody>
                      {txns.map(t => {
                        const isPositive = t.transactionType === 'DEPOSIT'
                        return (
                          <tr key={t.id}>
                            <td>{new Date(t.createdAt).toLocaleDateString()}</td>
                            <td className="mono">{t.referenceNumber}</td>
                            <td>
                              <span className={`badge badge-${isPositive ? 'success' : 'danger'}`}>
                                {t.transactionType}
                              </span>
                            </td>
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
                <div style={{ textAlign: 'center', padding: '30px', color: 'var(--text-muted)' }}>
                  No transaction history for this account.
                </div>
              )}
            </div>

            <div className="modal-footer">
              <button className="btn btn-ghost" onClick={() => setSelectedAccount(null)}>Close</button>
            </div>
          </div>
        </div>
      )}
    </div>
  )
}
