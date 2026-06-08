import React, { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { authApi } from '../api/api'

export default function Login({ onLogin, showToast }) {
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const navigate = useNavigate()

  const handleSubmit = async (e) => {
    e.preventDefault()
    setLoading(true)
    setError('')
    try {
      const response = await authApi.login({ email, password })
      onLogin(response.token, response.user)
    } catch (err) {
      setError(err.response?.data?.message || 'Invalid email or password')
      showToast(err.response?.data?.message || 'Login failed', 'error')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="auth-screen">
      <div className="auth-box">
        <div className="auth-card">
          <div className="auth-logo">
            <div className="auth-logo-icon">N</div>
            <span className="auth-logo-text">Nex<span>Bank</span></span>
          </div>

          <h3 className="auth-title">Welcome back</h3>
          <p className="auth-sub">Enter your credentials to access your account</p>

          <form onSubmit={handleSubmit}>
            <div className="form-group">
              <label className="form-label">Email Address</label>
              <input
                type="email"
                className="form-input"
                placeholder="you@example.com"
                value={email}
                onChange={e => setEmail(e.target.value)}
                required
              />
            </div>

            <div className="form-group">
              <label className="form-label">Password</label>
              <input
                type="password"
                className="form-input"
                placeholder="••••••••"
                value={password}
                onChange={e => setPassword(e.target.value)}
                required
              />
            </div>

            {error && <div className="error-text" style={{ marginBottom: 16 }}>{error}</div>}

            <button type="submit" className="btn btn-primary btn-full" disabled={loading}>
              {loading ? <span className="spinner" style={{ width: 16, height: 16 }}></span> : 'Sign In'}
            </button>
          </form>

          <div className="auth-switch">
            Don't have an account?{' '}
            <button onClick={() => navigate('/register')}>Sign Up</button>
          </div>
        </div>
      </div>
    </div>
  )
}
