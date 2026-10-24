import { useEffect, useState } from 'react'
import { api } from '../api.js'

const RECORD_TYPES = ['NOTE', 'LAB', 'PRESCRIPTION', 'DIAGNOSIS', 'VITALS', 'IMAGING']

export default function PatientDetail({ patientId, onBack }) {
  const [patient, setPatient] = useState(null)
  const [tab, setTab] = useState('records')
  const [records, setRecords] = useState({ content: [] })
  const [documents, setDocuments] = useState({ content: [] })
  const [newRecord, setNewRecord] = useState({ title: '', type: 'NOTE', content: '', recordedAt: '' })
  const [uploadMsg, setUploadMsg] = useState(null)
  const [error, setError] = useState(null)

  async function load() {
    setPatient(await api.getPatient(patientId))
    setRecords(await api.listRecords(patientId))
    setDocuments(await api.listDocuments(patientId))
  }

  useEffect(() => { load() }, [patientId])

  async function addRecord(e) {
    e.preventDefault()
    setError(null)
    try {
      await api.addRecord(patientId, {
        ...newRecord,
        recordedAt: newRecord.recordedAt ? new Date(newRecord.recordedAt).toISOString() : null
      })
      setNewRecord({ title: '', type: 'NOTE', content: '', recordedAt: '' })
      load()
    } catch (err) { setError(err.message) }
  }

  async function upload(e) {
    e.preventDefault()
    const input = e.currentTarget.elements.file
    const file = input && input.files ? input.files[0] : null
    if (!file) return
    setUploadMsg(null)
    setError(null)
    try {
      const doc = await api.uploadDocument(patientId, file)
      setUploadMsg(`Uploaded "${doc.filename}" — ${doc.status}, ${doc.pageCount} pages`)
      e.currentTarget.reset()
      load()
    } catch (err) { setError(err.message) }
  }

  async function indexDoc(documentId) {
    setError(null)
    try {
      await api.indexDocument(patientId, documentId)
      setUploadMsg('Document indexed into the vector store')
      load()
    } catch (err) { setError(err.message) }
  }

  if (!patient) return <div className="muted">Loading…</div>

  return (
    <div>
      <button className="link" onClick={onBack}>← Back to patients</button>
      <div className="card">
        <h3>{patient.firstName} {patient.lastName}</h3>
        <div className="grid">
          <span><b>MRN:</b> {patient.mrn}</span>
          <span><b>DOB:</b> {patient.dateOfBirth}</span>
          <span><b>Gender:</b> {patient.gender || '—'}</span>
          <span><b>Email:</b> {patient.email || '—'}</span>
          <span><b>Phone:</b> {patient.phone || '—'}</span>
          <span><b>Address:</b> {patient.address || '—'}</span>
        </div>
      </div>

      <div className="tabs">
        <button className={tab === 'records' ? 'active' : ''} onClick={() => setTab('records')}>Records</button>
        <button className={tab === 'documents' ? 'active' : ''} onClick={() => setTab('documents')}>Documents</button>
      </div>

      {error && <div className="error">{error}</div>}

      {tab === 'records' && (
        <div>
          <form className="card" onSubmit={addRecord}>
            <h4>Add record</h4>
            <input placeholder="Title" value={newRecord.title} onChange={(e) => setNewRecord({ ...newRecord, title: e.target.value })} required />
            <select value={newRecord.type} onChange={(e) => setNewRecord({ ...newRecord, type: e.target.value })}>
              {RECORD_TYPES.map((t) => <option key={t}>{t}</option>)}
            </select>
            <textarea placeholder="Content" value={newRecord.content} onChange={(e) => setNewRecord({ ...newRecord, content: e.target.value })} required />
            <button type="submit">Add</button>
          </form>
          <div>
            {records.content.map((r) => (
              <div className="card" key={r.id}>
                <div className="row"><b>{r.title}</b><span className="tag">{r.type}</span></div>
                <p className="muted">{new Date(r.recordedAt).toLocaleString()}</p>
                <p>{r.content}</p>
              </div>
            ))}
          </div>
        </div>
      )}

      {tab === 'documents' && (
        <div>
          <form className="card" onSubmit={upload}>
            <h4>Upload PDF</h4>
            <input name="file" type="file" accept="application/pdf" required />
            <button type="submit">Upload</button>
            {uploadMsg && <div className="success">{uploadMsg}</div>}
          </form>
          <table className="table">
            <thead><tr><th>File</th><th>Status</th><th>Pages</th><th></th></tr></thead>
            <tbody>
              {documents.content.map((d) => (
                <tr key={d.id}>
                  <td>{d.filename}</td>
                  <td>{d.status}</td>
                  <td>{d.pageCount}</td>
                  <td>{d.status === 'EXTRACTED' && <button className="link" onClick={() => indexDoc(d.id)}>Index for RAG</button>}</td>
                </tr>
              ))}
              {documents.content.length === 0 && <tr><td colSpan="4" className="muted">No documents</td></tr>}
            </tbody>
          </table>
        </div>
      )}
    </div>
  )
}
