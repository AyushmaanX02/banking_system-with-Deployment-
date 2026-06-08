import React, { useState, useEffect } from 'react'
import { Routes, Route, Navigate, useNavigate, useLocation } from 'react-router-dom'
import Dashboard from './pages/Dashboard'
import Accounts from './pages/Accounts'
import Transactions from './pages/Transactions'
import Beneficiaries from './pages/Beneficiaries'
import Login from './pages/Login'
import Register from './pages/Register'
import AdminDashboard from './pages/AdminDashboard'
import Sidebar from './components/Sidebar'
import Topbar from './components/Topbar'
import ToastContainer, { useToast } from './components/Toast'

function App() {
  const [user, setUser] = useState(null)
  const [loading, setLoading] = useState(true)
  const navigate = useNavigate()
  const location = useLocation()
  const { toasts, showToast, removeToast } = useToast()

  useEffect(() => {
    // Check if token exists and retrieve user details if possible
    const token = localStorage.getItem('nexbank_token')
    const savedUser = localStorage.getItem('nexbank_user')
    if (token && savedUser) {
      try {
        setUser(JSON.parse(savedUser))
      } catch (e) {
        localStorage.removeItem('nexbank_token')
        localStorage.removeItem('nexbank_user')
      }
    }
    setLoading(false)
  }, [])

  const handleLogin = (token, userData) => {
    localStorage.setItem('nexbank_token', token)
    localStorage.setItem('nexbank_user', JSON.stringify(userData))
    setUser(userData)
    showToast('Welcome back, ' + userData.firstName + '!', 'success')
    if (userData.role === 'ADMIN') {
      navigate('/admin')
    } else {
      navigate('/dashboard')
    }
  }

  const handleLogout = () => {
    localStorage.removeItem('nexbank_token')
    localStorage.removeItem('nexbank_user')
    setUser(null)
    showToast('Logged out successfully', 'success')
    navigate('/login')
  }

  if (loading) {
    return (
      <div className="loading-center">
        <div className="spinner"></div>
      </div>
    )
  }

  const isAuthenticated = !!user
  const isAdmin = user?.role === 'ADMIN'

  // If not authenticated, allow only login/register
  if (!isAuthenticated) {
    return (
      <>
        <Routes>
          <Route path="/login" element={<Login onLogin={handleLogin} showToast={showToast} />} />
          <Route path="/register" element={<Register showToast={showToast} />} />
          <Route path="*" element={<Navigate to="/login" replace />} />
        </Routes>
        <ToastContainer toasts={toasts} removeToast={removeToast} />
      </>
    )
  }

  // Admin routing
  if (isAdmin) {
    return (
      <div className="shell">
        <Sidebar user={user} onLogout={handleLogout} />
        <div className="main-area">
          <Topbar user={user} onLogout={handleLogout} />
          <div className="content">
            <Routes>
              <Route path="/admin" element={<AdminDashboard showToast={showToast} />} />
              <Route path="*" element={<Navigate to="/admin" replace />} />
            </Routes>
          </div>
        </div>
        <ToastContainer toasts={toasts} removeToast={removeToast} />
      </div>
    )
  }

  // Customer routing
  return (
    <div className="shell">
      <Sidebar user={user} onLogout={handleLogout} />
      <div className="main-area">
        <Topbar user={user} onLogout={handleLogout} />
        <div className="content">
          <Routes>
            <Route path="/dashboard" element={<Dashboard showToast={showToast} />} />
            <Route path="/accounts" element={<Accounts showToast={showToast} />} />
            <Route path="/transactions" element={<Transactions showToast={showToast} />} />
            <Route path="/beneficiaries" element={<Beneficiaries showToast={showToast} />} />
            <Route path="*" element={<Navigate to="/dashboard" replace />} />
          </Routes>
        </div>
      </div>
      <ToastContainer toasts={toasts} removeToast={removeToast} />
    </div>
  )
}

export default App
