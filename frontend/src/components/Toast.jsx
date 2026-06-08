import React, { useState, useCallback } from 'react'

export function useToast() {
  const [toasts, setToasts] = useState([])

  const showToast = useCallback((message, type = 'success') => {
    const id = Date.now() + Math.random()
    setToasts(prev => [...prev, { id, message, type }])
    setTimeout(() => {
      setToasts(prev => prev.filter(t => t.id !== id))
    }, 4000)
  }, [])

  const removeToast = useCallback((id) => {
    setToasts(prev => prev.filter(t => t.id !== id))
  }, [])

  return { toasts, showToast, removeToast }
}

export default function ToastContainer({ toasts, removeToast }) {
  return (
    <div id="toast-container">
      {toasts.map(t => (
        <div key={t.id} className={`toast ${t.type}`} onClick={() => removeToast(t.id)}>
          {t.type === 'success' ? (
            <span style={{ color: 'var(--success)', fontWeight: 'bold' }}>✓</span>
          ) : (
            <span style={{ color: 'var(--danger)', fontWeight: 'bold' }}>✕</span>
          )}
          <div>{t.message}</div>
        </div>
      ))}
    </div>
  )
}
