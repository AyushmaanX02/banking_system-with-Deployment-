import React, { useState, useEffect } from 'react'
import { beneficiaryApi } from '../api/api'

export default function Beneficiaries({ showToast }) {
  const [beneficiaries, setBeneficiaries] = useState([])
  const [loading, setLoading] = useState(true)
  const [isModalOpen, setIsModalOpen] = useState(false)
  const [formData, setFormData] = useState({ nickname: '', accountNumber: '', bankName: '' })
  const [submitting, setSubmitting] = useState(false)

  useEffect(() => {
    loadBeneficiaries()
  }, [])

  async function loadBeneficiaries() {
    try {
      const data = await beneficiaryApi.getAll()
      setBeneficiaries(data)
    } catch (err) {
      showToast('Error loading beneficiaries', 'error')
    } finally {
      setLoading(false)
    }
  }

  const handleInputChange = (e) => {
    const { name, value } = e.target
    setFormData(prev => ({ ...prev, [name]: value }))
  }

  const handleAdd = async (e) => {
    e.preventDefault()
    setSubmitting(true)
    try {
      await beneficiaryApi.add(formData)
      showToast('Beneficiary added successfully!', 'success')
      setIsModalOpen(false)
      setFormData({ nickname: '', accountNumber: '', bankName: '' })
      loadBeneficiaries()
    } catch (err) {
      showToast(err.response?.data?.message || 'Failed to add beneficiary', 'error')
    } finally {
      setSubmitting(false)
    }
  }

  const handleDelete = async (id) => {
    if (!window.confirm('Are you sure you want to delete this beneficiary?')) return
    try {
      await beneficiaryApi.delete(id)
      showToast('Beneficiary deleted successfully', 'success')
      loadBeneficiaries()
    } catch (err) {
      showToast(err.response?.data?.message || 'Failed to delete beneficiary', 'error')
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
          <h2 className="section-title">Beneficiaries</h2>
          <p style={{ fontSize: '0.85rem', color: 'var(--text-muted)' }}>Manage your transfer contacts for quick payments</p>
        </div>
        <button className="btn btn-primary" onClick={() => setIsModalOpen(true)}>
          + Add Beneficiary
        </button>
      </div>

      {beneficiaries.length > 0 ? (
        <div className="card">
          <div className="table-wrapper">
            <table>
              <thead>
                <tr>
                  <th>Nickname</th>
                  <th>Bank Name</th>
                  <th>Account Number</th>
                  <th>Added On</th>
                  <th style={{ textAlign: 'right' }}>Actions</th>
                </tr>
              </thead>
              <tbody>
                {beneficiaries.map(b => (
                  <tr key={b.id}>
                    <td>
                      <div className="fw-600">{b.nickname}</div>
                    </td>
                    <td>{b.bankName}</td>
                    <td className="mono">{b.accountNumber}</td>
                    <td>{b.createdAt ? new Date(b.createdAt).toLocaleDateString() : 'N/A'}</td>
                    <td style={{ textAlign: 'right' }}>
                      <button className="btn btn-danger btn-sm" onClick={() => handleDelete(b.id)}>
                        Delete
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      ) : (
        <div className="card empty-state">
          <p className="empty-icon">👥</p>
          <h3>No Beneficiaries Added</h3>
          <p style={{ margin: '10px 0 20px', color: 'var(--text-muted)' }}>Save accounts you transfer money to regularly to see them here.</p>
          <button className="btn btn-primary" onClick={() => setIsModalOpen(true)}>Add Beneficiary</button>
        </div>
      )}

      {/* ── Add Beneficiary Modal ── */}
      {isModalOpen && (
        <div className="modal-overlay">
          <div className="modal">
            <div className="modal-header">
              <h3 className="modal-title">Add Beneficiary Contact</h3>
              <button className="modal-close" onClick={() => setIsModalOpen(false)}>×</button>
            </div>
            <form onSubmit={handleAdd}>
              <div className="form-group">
                <label className="form-label">Nickname (e.g. Landlord, Mom)</label>
                <input
                  type="text"
                  name="nickname"
                  className="form-input"
                  placeholder="Enter a contact name"
                  value={formData.nickname}
                  onChange={handleInputChange}
                  required
                />
              </div>

              <div className="form-group">
                <label className="form-label">Bank Name</label>
                <input
                  type="text"
                  name="bankName"
                  className="form-input"
                  placeholder="e.g. Chase, NexBank"
                  value={formData.bankName}
                  onChange={handleInputChange}
                  required
                />
              </div>

              <div className="form-group">
                <label className="form-label">Account Number</label>
                <input
                  type="text"
                  name="accountNumber"
                  className="form-input mono"
                  placeholder="e.g. ACC8882939"
                  value={formData.accountNumber}
                  onChange={handleInputChange}
                  required
                />
              </div>

              <div className="modal-footer">
                <button type="button" className="btn btn-ghost" onClick={() => setIsModalOpen(false)}>Cancel</button>
                <button type="submit" className="btn btn-primary" disabled={submitting}>
                  {submitting ? 'Adding...' : 'Add Contact'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  )
}
