import { useState } from 'react'
import { api } from '../api.js'

export default function Ask() {
  const [question, setQuestion] = useState('')
  const [patientId, setPatientId] = useState('')
  const [answer, setAnswer] = useState(null)
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState(null)

  async function ask(e) {
    e.preventDefault()
    setBusy(true)
    setError(null)
    setAnswer(null)
    try {
      setAnswer(await api.ask(question, patientId ? Number(patientId) : null))
    } catch (err) {
      setError(err.message)
    } finally {
      setBusy(false)
    }
  }

  return (
    <div>
      <form className="card" onSubmit={ask}>
        <h3>Ask the documents</h3>
        <p className="muted">Answers are grounded only in the indexed PDFs and include source citations.</p>
        <textarea placeholder="Ask a question about the patient's documents…" value={question}
          onChange={(e) => setQuestion(e.target.value)} required rows={3} />
        <input placeholder="Optional patient id" type="number" value={patientId}
          onChange={(e) => setPatientId(e.target.value)} />
        <button disabled={busy}>{busy ? 'Searching documents…' : 'Ask'}</button>
      </form>

      {error && <div className="error">{error}</div>}

      {answer && (
        <div className="card answer">
          <div className="row">
            <h4>Answer</h4>
            <span className={answer.evidenceSufficient ? 'tag ok' : 'tag warn'}>
              {answer.evidenceSufficient ? 'Sufficient evidence' : 'Insufficient evidence'}
            </span>
          </div>
          <p>{answer.answer}</p>

          {answer.citations?.length > 0 && (
            <div className="citations">
              <h5>Sources</h5>
              {answer.citations.map((c, i) => (
                <div className="citation" key={i}>
                  <span className="src">{c.filename} · page {c.pageNumber}</span>
                  <p className="snippet">{c.snippet}</p>
                </div>
              ))}
            </div>
          )}
        </div>
      )}
    </div>
  )
}
