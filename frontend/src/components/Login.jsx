import { useState } from 'react'
import { api, setAuth } from '../api.js'

export default function Login({ onAuthed }) {
  const [mode, setMode] = useState('login')
  const [email, setEmail] = useState('admin@caresphere.dev')
  const [password, setPassword] = useState('Admin12345!')
  const [fullName, setFullName] = useState('')
  const [error, setError] = useState(null)
  const [busy, setBusy] = useState(false)

  async function submit(e) {
    e.preventDefault()
    setError(null)
    setBusy(true)
    try {
      const res = mode === 'login'
        ? await api.login(email, password)
        : await api.register(email, password, fullName)
      setAuth(res.token, res.role)
      onAuthed()
    } catch (err) {
      setError(err.message)
    } finally {
      setBusy(false)
    }
  }

  return (
    <div className="login">
      <div className="login-card">
        <h1>CareSphere AI</h1>
        <p className="subtitle">Healthcare document intelligence</p>
        <div className="tabs">
          <button className={mode === 'login' ? 'active' : ''} onClick={() => setMode('login')}>Sign in</button>
          <button className={mode === 'register' ? 'active' : ''} onClick={() => setMode('register')}>Register</button>
        </div>
        <form onSubmit={submit}>
          {mode === 'register' && (
            <input placeholder="Full name" value={fullName} onChange={(e) => setFullName(e.target.value)} required />
          )}
          <input placeholder="Email" value={email} onChange={(e) => setEmail(e.target.value)} required type="email" />
          <input placeholder="Password" value={password} onChange={(e) => setPassword(e.target.value)} required type="password" />
          {error && <div className="error">{error}</div>}
          <button disabled={busy}>{busy ? 'Working…' : mode === 'login' ? 'Sign in' : 'Create account'}</button>
        </form>
        <p className="hint">Default admin: admin@caresphere.dev / Admin12345!</p>
      </div>
    </div>
  )
}
