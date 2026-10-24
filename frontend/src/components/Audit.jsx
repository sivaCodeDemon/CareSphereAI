import { useEffect, useState } from 'react'
import { api } from '../api.js'

export default function Audit() {
  const [page, setPage] = useState(0)
  const [data, setData] = useState({ content: [], totalElements: 0, totalPages: 0 })

  useEffect(() => {
    api.listAudit(page).then(setData).catch(() => {})
  }, [page])

  return (
    <div>
      <h3>Security audit log</h3>
      <table className="table">
        <thead>
          <tr><th>Time</th><th>Actor</th><th>Action</th><th>Resource</th><th>Patient</th><th>Status</th></tr>
        </thead>
        <tbody>
          {data.content.map((e) => (
            <tr key={e.id}>
              <td>{new Date(e.createdAt).toLocaleString()}</td>
              <td>{e.actor || '—'}</td>
              <td>{e.action}</td>
              <td>{e.resourceType}{e.resourceId ? ` #${e.resourceId}` : ''}</td>
              <td>{e.patientId || '—'}</td>
              <td>{e.status}</td>
            </tr>
          ))}
          {data.content.length === 0 && <tr><td colSpan="6" className="muted">No audit events</td></tr>}
        </tbody>
      </table>
      <div className="pager">
        <button disabled={page === 0} onClick={() => setPage(page - 1)}>Prev</button>
        <span>{data.totalElements} events</span>
        <button disabled={page + 1 >= data.totalPages} onClick={() => setPage(page + 1)}>Next</button>
      </div>
    </div>
  )
}
