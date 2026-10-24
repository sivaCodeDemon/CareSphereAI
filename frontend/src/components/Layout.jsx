export default function Layout({ children, role, view, setView, onLogout, canViewAudit }) {
  return (
    <div className="layout">
      <header className="topbar">
        <div className="brand">CareSphere AI</div>
        <nav>
          <button className={view === 'patients' ? 'active' : ''} onClick={() => setView('patients')}>Patients</button>
          <button className={view === 'ask' ? 'active' : ''} onClick={() => setView('ask')}>Ask AI</button>
          {canViewAudit && (
            <button className={view === 'audit' ? 'active' : ''} onClick={() => setView('audit')}>Audit</button>
          )}
        </nav>
        <div className="user">
          <span className="role">{role}</span>
          <button onClick={onLogout}>Sign out</button>
        </div>
      </header>
      <main className="content">{children}</main>
    </div>
  )
}
