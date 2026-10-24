import { useEffect, useState } from 'react'
import { getToken, clearAuth, getRole } from './api.js'
import Login from './components/Login.jsx'
import Layout from './components/Layout.jsx'
import PatientList from './components/PatientList.jsx'
import PatientDetail from './components/PatientDetail.jsx'
import Ask from './components/Ask.jsx'
import Audit from './components/Audit.jsx'

export default function App() {
  const [authed, setAuthed] = useState(!!getToken())
  const [view, setView] = useState('patients')
  const [selectedPatient, setSelectedPatient] = useState(null)
  const role = getRole()

  useEffect(() => {
    const onUnauthorized = () => setAuthed(false)
    window.addEventListener('unauthorized', onUnauthorized)
    return () => window.removeEventListener('unauthorized', onUnauthorized)
  }, [])

  function logout() {
    clearAuth()
    setAuthed(false)
    setSelectedPatient(null)
    setView('patients')
  }

  if (!authed) {
    return <Login onAuthed={() => setAuthed(true)} />
  }

  let content
  if (selectedPatient) {
    content = (
      <PatientDetail
        patientId={selectedPatient}
        onBack={() => setSelectedPatient(null)}
      />
    )
  } else if (view === 'ask') {
    content = <Ask />
  } else if (view === 'audit') {
    content = <Audit />
  } else {
    content = (
      <PatientList
        onOpenPatient={(id) => setSelectedPatient(id)}
      />
    )
  }

  return (
    <Layout role={role} view={view} setView={setView} onLogout={logout} canViewAudit={role === 'ADMIN'}>
      {content}
    </Layout>
  )
}
