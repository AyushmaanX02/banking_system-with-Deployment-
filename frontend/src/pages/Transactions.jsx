import React, { useState, useEffect } from 'react'
import { accountApi, transactionApi, beneficiaryApi } from '../api/api'

export default function Transactions({ showToast }) {
  const [accounts, setAccounts] = useState([])
  const [beneficiaries, setBeneficiaries] = useState([])
  const [selectedAccNum, setSelectedAccNum] = useState('')
  const [txns, setTxns] = useState([])
  const [loading, setLoading] = useState(true)
  const [txnsLoading, setTxnsLoading] = useState(false)

  // Modals
  const [actionType, setActionType] = useState('') // 'DEPOSIT', 'WITHDRAW', 'TRANSFER'
  const [amount, setAmount] = useState('')
  const [description, setDescription] = useState('')
  const [targetAccNum, setTargetAccNum] = useState('')
  const [submitting, setSubmitting] = useState(false)

  useEffect(() => {
    async function init() {
      try {
        const [accsData, beneficiariesData] = await Promise.all([
          accountApi.getAll(),
          beneficiaryApi.getAll()
        ])
        setAccounts(accsData)
        setBeneficiaries(beneficiariesData)
        if (accsData.length > 0) {
          setSelectedAccNum(accsData[0].accountNumber)
          loadHistory(accsData[0].accountNumber)
        } else {
          setLoading(false)
        }
      } catch (err) {
        showToast('Error loading accounts information', 'error')
        setLoading(false)
      }
    }
    init()
  }, [])

  async function loadHistory(accNum) {
    setTxnsLoading(true)
    try {
      const history = await transactionApi.history(accNum)
      setTxns(history)
    } catch (err) {
      showToast('Error loading transaction history', 'error')
    } finally {
      setTxnsLoading(false)
      setLoading(false)
    }
  }

  const handleAccountChange = (e) => {
    const accNum = e.target.value
    setSelectedAccNum(accNum)
    loadHistory(accNum)
  }

  const handleCloseModal = () => {
    setActionType('')
    setAmount('')
    setDescription('')
    setTargetAccNum('')
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    if (!amount || parseFloat(amount) <= 0) {
      showToast('Please enter a valid amount', 'error')
      return
    }
    setSubmitting(true)
    try {
      if (actionType === 'DEPOSIT') {
        await transactionApi.deposit({
          accountNumber: selectedAccNum,
          amount: parseFloat(amount),
          description
        })
        showToast('Deposit completed successfully!', 'success')
      } else if (actionType === 'WITHDRAW') {
        await transactionApi.withdraw({
          accountNumber: selectedAccNum,
          amount: parseFloat(amount),
          description
        })
        showToast('Withdrawal completed successfully!', 'success')
      } else if (actionType === 'TRANSFER') {
        await transactionApi.transfer({
          fromAccountNumber: selectedAccNum,
          toAccountNumber: targetAccNum,
          amount: parseFloat(amount),
          description
        })
        showToast('Fund transfer completed successfully!', 'success')
      }
      handleCloseModal()
      // Reload accounts and history
      const accsData = await accountApi.getAll()
      setAccounts(accsData)
      loadHistory(selectedAccNum)
    } catch (err) {
      showToast(err.response?.data?.message || 'Transaction failed. Check funds/status.', 'error')
    } finally {
      setSubmitting(false)
    }
  }

  if (loading) {
    return (
      <div className="loading-center">
        <div className="spinner"></div>
      </div>
    )
  }

  const currentAccount = accounts.find(a => a.accountNumber === selectedAccNum)

  return (
    <div className="page page-anim">
      <div className="section-header">
        <div>
          <h2 className="section-title">Transactions</h2>
          <p style={{ fontSize: '0.85rem', color: 'var(--text-muted)' }}>Deposit, withdraw, or transfer funds instantly</p>
        </div>
        {accounts.length > 0 && (
          <div style={{ display: 'flex', gap: 10 }}>
            <button className="btn btn-ghost" onClick={() => setActionType('DEPOSIT')}>💰 Deposit</button>
            <button className="btn btn-ghost" onClick={() => setActionType('WITHDRAW')}>🏧 Withdraw</button>
            <button className="btn btn-primary" onClick={() => setActionType('TRANSFER')}>💸 Send Money</button>
          </div>
        )}
      </div>

      {accounts.length > 0 ? (
        <div className="grid-3" style={{ gridTemplateColumns: '1fr 2fr' }}>
          {/* ── Left Side Panel: Account details & selector ── */}
          <div style={{ display: 'flex', flexDirection: 'column', gap: 16 }}>
            <div className="card">
              <div className="form-group">
                <label className="form-label">Select Account</label>
                <select className="form-select" value={selectedAccNum} onChange={handleAccountChange}>
                  {accounts.map(acc => (
                    <option key={acc.id} value={acc.accountNumber}>
                      {acc.accountType} ({acc.accountNumber})
                    </option>
                  ))}
                </select>
              </div>

              {currentAccount && (
                <div style={{ marginTop: 20, paddingTop: 20, borderTop: '1px solid var(--border)' }}>
                  <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)', textTransform: 'uppercase', letterSpacing: '0.05em' }}>Available Balance</div>
                  <div style={{ fontSize: '2rem', fontWeight: 700, color: 'var(--accent)', margin: '6px 0' }}>
                    ${parseFloat(currentAccount.balance).toFixed(2)}
                  </div>
                  <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.85rem', color: 'var(--text-muted)' }}>
                    <span>Account Status:</span>
                    <span className="fw-600" style={{ color: currentAccount.status === 'ACTIVE' ? 'var(--success)' : 'var(--danger)' }}>{currentAccount.status}</span>
                  </div>
                </div>
              )}
            </div>

            <div className="card" style={{ padding: 20, background: 'linear-gradient(135deg, hsl(222, 13%, 12%), var(--surface))' }}>
              <h4 style={{ fontSize: '0.85rem', marginBottom: 10 }}>🔒 Secure Transactions</h4>
              <p style={{ fontSize: '0.75rem', color: 'var(--text-muted)', lineHeight: '1.4' }}>
                All transfers, withdrawals, and deposits are logged, verified, and audited in real-time. Keep your credentials private.
              </p>
            </div>
          </div>

          {/* ── Right Side Panel: History ── */}
          <div className="card">
            <h3 className="section-title" style={{ marginBottom: 16 }}>Account Activity</h3>
            {txnsLoading ? (
              <div className="loading-center">
                <div className="spinner"></div>
              </div>
            ) : txns.length > 0 ? (
              <div className="table-wrapper">
                <table>
                  <thead>
                    <tr>
                      <th>Date</th>
                      <th>Ref Number</th>
                      <th>Type</th>
                      <th>Description</th>
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
                          <td style={{ color: 'var(--text-muted)', maxWidth: 180, overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
                            {t.description || 'N/A'}
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
              <div className="empty-state">
                <p className="empty-icon">🧾</p>
                <p>No transactions recorded for this account.</p>
              </div>
            )}
          </div>
        </div>
      ) : (
        <div className="card empty-state">
          <p className="empty-icon">🏦</p>
          <h3>Create an Account first</h3>
          <p style={{ margin: '10px 0 20px', color: 'var(--text-muted)' }}>You must open at least one active bank account to make transactions.</p>
        </div>
      )}

      {/* ── Action Modals (Deposit, Withdraw, Transfer) ── */}
      {actionType && (
        <div className="modal-overlay">
          <div className="modal">
            <div className="modal-header">
              <h3 className="modal-title" style={{ textTransform: 'capitalize' }}>
                {actionType.toLowerCase()} Funds
              </h3>
              <button className="modal-close" onClick={handleCloseModal}>×</button>
            </div>
            <form onSubmit={handleSubmit}>
              <div className="form-group">
                <label className="form-label">Operating Account</label>
                <input
                  type="text"
                  className="form-input"
                  value={`${currentAccount?.accountType} (${selectedAccNum})`}
                  disabled
                />
              </div>

              {actionType === 'TRANSFER' && (
                <div className="form-group">
                  <label className="form-label">Destination Account</label>
                  <select
                    className="form-select"
                    value={targetAccNum}
                    onChange={e => setTargetAccNum(e.target.value)}
                    required
                  >
                    <option value="">-- Choose Beneficiary or enter account --</option>
                    {beneficiaries.map(b => (
                      <option key={b.id} value={b.accountNumber}>
                        {b.nickname} ({b.bankName} - {b.accountNumber})
                      </option>
                    ))}
                  </select>
                  <input
                    type="text"
                    className="form-input mt-4"
                    placeholder="Or enter custom Account Number manually"
                    value={targetAccNum}
                    onChange={e => setTargetAccNum(e.target.value)}
                    required
                  />
                </div>
              )}

              <div className="form-group">
                <label className="form-label">Amount ($)</label>
                <input
                  type="number"
                  step="0.01"
                  min="0.01"
                  className="form-input"
                  placeholder="0.00"
                  value={amount}
                  onChange={e => setAmount(e.target.value)}
                  required
                />
              </div>

              <div className="form-group">
                <label className="form-label">Description (Optional)</label>
                <input
                  type="text"
                  className="form-input"
                  placeholder="e.g. Rent, Grocery deposit"
                  value={description}
                  onChange={e => setDescription(e.target.value)}
                />
              </div>

              <div className="modal-footer">
                <button type="button" className="btn btn-ghost" onClick={handleCloseModal}>Cancel</button>
                <button type="submit" className="btn btn-primary" disabled={submitting}>
                  {submitting ? 'Processing...' : 'Confirm'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  )
}
