import React, { useState, useEffect } from 'react'
import { Link } from 'react-router-dom'
import { analyticsApi, accountApi, transactionApi } from '../api/api'
import {
  AreaChart, Area, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer,
  BarChart, Bar, Legend
} from 'recharts'

export default function Dashboard({ showToast }) {
  const [analytics, setAnalytics] = useState(null)
  const [accounts, setAccounts] = useState([])
  const [recentTxns, setRecentTxns] = useState([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    async function loadData() {
      try {
        const [analyticsData, accountsData] = await Promise.all([
          analyticsApi.summary(),
          accountApi.getAll()
        ])
        setAnalytics(analyticsData)
        setAccounts(accountsData)

        // Fetch recent transactions for the first active account if available
        if (accountsData.length > 0) {
          const txns = await transactionApi.history(accountsData[0].accountNumber)
          setRecentTxns(txns.slice(0, 5))
        }
      } catch (err) {
        showToast('Error loading dashboard data', 'error')
      } finally {
        setLoading(false)
      }
    }
    loadData()
  }, [showToast])

  if (loading) {
    return (
      <div className="loading-center">
        <div className="spinner"></div>
      </div>
    )
  }

  // Format balance history map to array for Recharts
  const chartData = analytics ? Object.entries(analytics.balanceHistory).map(([month, val]) => ({
    month,
    balance: parseFloat(val)
  })) : []

  // Format monthly totals for deposits / withdrawals / transfers
  const monthlyData = analytics ? analytics.monthlyTotals.map(item => ({
    month: item.month,
    Deposits: parseFloat(item.deposited),
    Withdrawals: parseFloat(item.withdrawn) + parseFloat(item.transferred)
  })) : []

  const totalDeposited = analytics ? parseFloat(analytics.totalDeposited) : 0
  const totalWithdrawn = analytics ? parseFloat(analytics.totalWithdrawn) + parseFloat(analytics.totalTransferred) : 0
  const totalBalance = accounts.reduce((acc, current) => acc + parseFloat(current.balance), 0)

  // Custom tooltips for nice styling
  const CustomAreaTooltip = ({ active, payload }) => {
    if (active && payload && payload.length) {
      return (
        <div className="custom-tooltip">
          <p className="label">{payload[0].payload.month}</p>
          <p className="item">Balance: <span style={{ color: 'var(--accent)', fontWeight: 600 }}>${payload[0].value.toFixed(2)}</span></p>
        </div>
      )
    }
    return null
  }

  const CustomBarTooltip = ({ active, payload }) => {
    if (active && payload && payload.length) {
      return (
        <div className="custom-tooltip">
          <p className="label">{payload[0].payload.month}</p>
          {payload.map((p, idx) => (
            <p key={idx} className="item" style={{ color: p.color }}>
              {p.name}: <span>${p.value.toFixed(2)}</span>
            </p>
          ))}
        </div>
      )
    }
    return null
  }

  return (
    <div className="page page-anim">
      {/* ── Metric Summary Grid ── */}
      <div className="grid-4">
        <div className="card" style={statCardStyle}>
          <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>TOTAL NET BALANCE</div>
          <div style={{ fontSize: '1.8rem', fontWeight: 700, margin: '6px 0', color: 'var(--accent)' }}>
            ${totalBalance.toLocaleString('en-US', { minimumFractionDigits: 2 })}
          </div>
          <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>Across {accounts.length} active accounts</div>
        </div>

        <div className="card" style={statCardStyle}>
          <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>TOTAL INFLOWS</div>
          <div style={{ fontSize: '1.8rem', fontWeight: 700, margin: '6px 0', color: 'var(--success)' }}>
            +${totalDeposited.toLocaleString('en-US', { minimumFractionDigits: 2 })}
          </div>
          <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>All-time total deposits</div>
        </div>

        <div className="card" style={statCardStyle}>
          <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>TOTAL OUTFLOWS</div>
          <div style={{ fontSize: '1.8rem', fontWeight: 700, margin: '6px 0', color: 'var(--danger)' }}>
            -${totalWithdrawn.toLocaleString('en-US', { minimumFractionDigits: 2 })}
          </div>
          <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>Withdrawals & outgoing transfers</div>
        </div>

        <div className="card" style={statCardStyle}>
          <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>TRANSACTION VELOCITY</div>
          <div style={{ fontSize: '1.8rem', fontWeight: 700, margin: '6px 0' }}>
            {analytics?.transactionCount || 0}
          </div>
          <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>Completed transaction entries</div>
        </div>
      </div>

      {/* ── Main Analytics Section (Recharts) ── */}
      <div className="grid-2">
        <div className="card">
          <div className="section-header">
            <h3 className="section-title">Net Balance Trend</h3>
            <span style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>Past 6 Months</span>
          </div>
          <div style={{ height: 260 }}>
            {chartData.length > 0 ? (
              <ResponsiveContainer width="100%" height="100%">
                <AreaChart data={chartData} margin={{ top: 10, right: 10, left: -20, bottom: 0 }}>
                  <defs>
                    <linearGradient id="balanceGrad" x1="0" y1="0" x2="0" y2="1">
                      <stop offset="5%" stopColor="var(--accent)" stopOpacity={0.4}/>
                      <stop offset="95%" stopColor="var(--accent)" stopOpacity={0}/>
                    </linearGradient>
                  </defs>
                  <CartesianGrid strokeDasharray="3 3" vertical={false} />
                  <XAxis dataKey="month" />
                  <YAxis />
                  <Tooltip content={<CustomAreaTooltip />} />
                  <Area type="monotone" dataKey="balance" stroke="var(--accent)" strokeWidth={2.5} fillOpacity={1} fill="url(#balanceGrad)" />
                </AreaChart>
              </ResponsiveContainer>
            ) : (
              <div className="empty-state">No historical balance snapshot available</div>
            )}
          </div>
        </div>

        <div className="card">
          <div className="section-header">
            <h3 className="section-title">Inflows vs Outflows</h3>
            <span style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>Monthly Comparison</span>
          </div>
          <div style={{ height: 260 }}>
            {monthlyData.length > 0 ? (
              <ResponsiveContainer width="100%" height="100%">
                <BarChart data={monthlyData} margin={{ top: 10, right: 10, left: -20, bottom: 0 }}>
                  <CartesianGrid strokeDasharray="3 3" vertical={false} />
                  <XAxis dataKey="month" />
                  <YAxis />
                  <Tooltip content={<CustomBarTooltip />} />
                  <Legend iconType="circle" />
                  <Bar dataKey="Deposits" fill="var(--accent)" radius={[4, 4, 0, 0]} />
                  <Bar dataKey="Withdrawals" fill="var(--danger)" radius={[4, 4, 0, 0]} />
                </BarChart>
              </ResponsiveContainer>
            ) : (
              <div className="empty-state">No monthly summary stats available</div>
            )}
          </div>
        </div>
      </div>

      <div className="grid-3">
        {/* ── My Accounts ── */}
        <div className="card" style={{ gridColumn: 'span 2' }}>
          <div className="section-header">
            <h3 className="section-title">My Accounts</h3>
            <Link to="/accounts" className="btn btn-ghost btn-sm">Manage Accounts</Link>
          </div>
          {accounts.length > 0 ? (
            <div className="grid-2">
              {accounts.slice(0, 4).map(acc => (
                <div key={acc.id} className={`account-card ${acc.accountType.toLowerCase()}`}>
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
            <div className="empty-state">
              <p className="empty-icon">🏦</p>
              <p>No active bank accounts found.</p>
              <Link to="/accounts" className="btn btn-primary btn-sm mt-4">Create Account</Link>
            </div>
          )}
        </div>

        {/* ── Recent Transactions ── */}
        <div className="card">
          <div className="section-header">
            <h3 className="section-title">Recent Activity</h3>
            <Link to="/transactions" className="btn btn-ghost btn-sm">History</Link>
          </div>
          {recentTxns.length > 0 ? (
            <div style={{ display: 'flex', flexDirection: 'column', gap: 12 }}>
              {recentTxns.map(txn => {
                const isPositive = txn.transactionType === 'DEPOSIT'
                const sign = isPositive ? '+' : '-'
                const color = isPositive ? 'var(--success)' : 'var(--danger)'
                return (
                  <div key={txn.id} style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', paddingBottom: 10, borderBottom: '1px solid hsl(222,10%,18%)' }}>
                    <div>
                      <div style={{ fontSize: '0.88rem', fontWeight: 600 }}>{txn.transactionType}</div>
                      <div style={{ fontSize: '0.72rem', color: 'var(--text-muted)' }}>{new Date(txn.createdAt).toLocaleDateString()}</div>
                    </div>
                    <div style={{ color, fontWeight: 700, fontSize: '0.9rem' }}>
                      {sign}${parseFloat(txn.amount).toFixed(2)}
                    </div>
                  </div>
                )
              })}
            </div>
          ) : (
            <div className="empty-state">
              <p className="empty-icon">🧾</p>
              <p>No transactions yet.</p>
            </div>
          )}
        </div>
      </div>
    </div>
  )
}

const statCardStyle = {
  display: 'flex',
  flexDirection: 'column',
  justifyContent: 'center',
}
