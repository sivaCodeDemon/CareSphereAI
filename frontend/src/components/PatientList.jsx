import { useEffect, useState } from 'react'
import { api } from '../api.js'

const EMPTY = { mrn: '', firstName: '', lastName: '', dateOfBirth: '', gender: '', email: '', phone: '', address: '' }

export default function PatientList({ onOpenPatient }) {
  const [search, setSearch] = useState('')
  const [page, setPage] = useState(0)
  const [data, setData] = useState({ content: [], totalElements: 0, totalPages: 0 })
  const [creating, setCreating] = useState(false)
  const [form, setForm] = useState(EMPTY)
  const [error, setError] = useState(null)
  const [loading, setLoading] = useState(false)

  async function load() {
    setLoading(true)
    try {
      setData(await api.listPatients(search, page))
    } catch (e) {
      setError(e.message)
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => { load() }, [search, page])

  async function create(e) {
    e.preventDefault()
    setError(null)
    try {
      await api.createPatient(form)
      setForm(EMPTY)
      setCreating(false)
      load()
    } catch (err) {
      setError(err.message)
    }
  }

  return (
    <div>
      <div className="toolbar">
        <input placeholder="Search by name or MRN…" value={search} onChange={(e) => { setSearch(e.target.value); setPage(0) }} />
        <button onClick={() => setCreating(true)}>New patient</button>
      </div>

      {creating && (
        <form className="card" onSubmit={create}>
          <h3>Register patient</h3>
          <div className="grid">
            <input placeholder="MRN" required value={form.mrn} onChange={(e) => setForm({ ...form, mrn: e.target.value })} />
            <input placeholder="First name" required value={form.firstName} onChange={(e) => setForm({ ...form, firstName: e.target.value })} />
            <input placeholder="Last name" required value={form.lastName} onChange={(e) => setForm({ ...form, lastName: e.target.value })} />
            <input type="date" required value={form.dateOfBirth} onChange={(e) => setForm({ ...form, dateOfBirth: e.target.value })} />
            <input placeholder="Gender" value={form.gender} onChange={(e) => setForm({ ...form, gender: e.target.value })} />
            <input placeholder="Email" type="email" value={form.email} onChange={(e) => setForm({ ...form, email: e.target.value })} />
            <input placeholder="Phone" value={form.phone} onChange={(e) => setForm({ ...form, phone: e.target.value })} />
            <input placeholder="Address" value={form.address} onChange={(e) => setForm({ ...form, address: e.target.value })} />
          </div>
          <div className="actions">
            <button type="submit">Save</button>
            <button type="button" className="link" onClick={() => setCreating(false)}>Cancel</button>
          </div>
        </form>
      )}

      {error && <div className="error">{error}</div>}

      <table className="table">
        <thead><tr><th>MRN</th><th>Name</th><th>Date of birth</th><th>Created by</th></tr></thead>
        <tbody>
          {data.content.map((p) => (
            <tr key={p.id} className="clickable" onClick={() => onOpenPatient(p.id)}>
              <td>{p.mrn}</td>
              <td>{p.firstName} {p.lastName}</td>
              <td>{p.dateOfBirth}</td>
              <td>{p.createdBy || '—'}</td>
            </tr>
          ))}
          {!loading && data.content.length === 0 && (
            <tr><td colSpan="4" className="muted">No patients</td></tr>
          )}
        </tbody>
      </table>

      <div className="pager">
        <button disabled={page === 0} onClick={() => setPage(page - 1)}>Prev</button>
        <span>Page {page + 1} of {Math.max(1, data.totalPages)} · {data.totalElements} patients</span>
        <button disabled={page + 1 >= data.totalPages} onClick={() => setPage(page + 1)}>Next</button>
      </div>
    </div>
  )
}
