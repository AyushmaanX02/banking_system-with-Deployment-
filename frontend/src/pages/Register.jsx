import React, { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { authApi } from '../api/api'

export default function Register({ showToast }) {
  const [formData, setFormData] = useState({
    firstName: '',
    lastName: '',
    email: '',
    password: '',
    phone: '',
  })
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const navigate = useNavigate()

  const handleChange = (e) => {
    const { name, value } = e.target
    setFormData(prev => ({ ...prev, [name]: value }))
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    setLoading(true)
    setError('')
    try {
      await authApi.register(formData)
      showToast('Registration successful! Please login.', 'success')
      navigate('/login')
    } catch (err) {
      setError(err.response?.data?.message || 'Registration failed. Try again.')
      showToast(err.response?.data?.message || 'Registration failed', 'error')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="auth-screen">
      <div className="auth-box" style={{ maxWidth: 460 }}>
        <div className="auth-card" style={{ padding: '30px' }}>
          <div className="auth-logo" style={{ marginBottom: 20 }}>
            <div className="auth-logo-icon">N</div>
            <span className="auth-logo-text">Nex<span>Bank</span></span>
          </div>

          <h3 className="auth-title">Create Account</h3>
          <p className="auth-sub" style={{ marginBottom: 20 }}>Start managing your wealth smarter today</p>

          <form onSubmit={handleSubmit}>
            <div className="grid-2" style={{ marginBottom: 0 }}>
              <div className="form-group">
                <label className="form-label">First Name</label>
                <input
                  type="text"
                  name="firstName"
                  className="form-input"
                  placeholder="John"
                  value={formData.firstName}
                  onChange={handleChange}
                  required
                />
              </div>
              <div className="form-group">
                <label className="form-label">Last Name</label>
                <input
                  type="text"
                  name="lastName"
                  className="form-input"
                  placeholder="Doe"
                  value={formData.lastName}
                  onChange={handleChange}
                  required
                />
              </div>
            </div>

            <div className="form-group">
              <label className="form-label">Email Address</label>
              <input
                type="email"
                name="email"
                className="form-input"
                placeholder="john.doe@example.com"
                value={formData.email}
                onChange={handleChange}
                required
              />
            </div>

            <div className="form-group">
              <label className="form-label">Phone Number</label>
              <input
                type="text"
                name="phone"
                className="form-input"
                placeholder="+1 555-0199"
                value={formData.phone}
                onChange={handleChange}
                required
              />
            </div>

            <div className="form-group">
              <label className="form-label">Password</label>
              <input
                type="password"
                name="password"
                className="form-input"
                placeholder="••••••••"
                value={formData.password}
                onChange={handleChange}
                required
              />
            </div>

            {error && <div className="error-text" style={{ marginBottom: 16 }}>{error}</div>}

            <button type="submit" className="btn btn-primary btn-full" disabled={loading}>
              {loading ? <span className="spinner" style={{ width: 16, height: 16 }}></span> : 'Sign Up'}
            </button>
          </form>

          <div className="auth-switch" style={{ marginTop: 16 }}>
            Already have an account?{' '}
            <button onClick={() => navigate('/login')}>Sign In</button>
          </div>
        </div>
      </div>
    </div>
  )
}
