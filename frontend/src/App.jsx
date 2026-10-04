import { useEffect, useMemo, useState } from 'react';
import { Link, Navigate, NavLink, Route, Routes, useLocation, useNavigate, useParams, useSearchParams } from 'react-router-dom';
import { authApi, portalApi } from './services/api';
import StudentProfilePage from './pages/StudentProfilePage';

const roleMenus = {
  STUDENT: [
    ['Overview', '/app'], ['Profile', '/app/profile'], ['Skills', '/app/skills'],
    ['Assessment', '/app/assessment'], ['Skill gaps', '/app/skill-gaps'],
    ['Certifications', '/app/certifications'], ['Projects', '/app/projects'], ['Interests', '/app/interests'],
    ['Recommendations', '/app/recommendations'], ['Opportunities', '/app/opportunities'], ['Applications', '/app/applications'],
    ['Internships', '/app/internships'], ['Jobs', '/app/jobs'],
    ['Collaborations', '/app/collaborations'], ['Placement', '/app/placement'],
    ['Student profile', '/app/student-profile'],
  ],
  INDUSTRY: [
    ['Overview', '/app'], ['Company profile', '/app/profile'], ['Opportunities', '/app/opportunities'],
    ['Create opportunity', '/app/opportunities/new'], ['Applicants', '/app/applicants'],
    ['Internships', '/app/internships'], ['Jobs', '/app/jobs'],
    ['Find students', '/app/students'],
    ['Placement tracking', '/app/placement'],
    ['Collaborations', '/app/collaborations'],
  ],
  FACULTY: [['Overview', '/app'], ['Students & skills', '/app/students'], ['Collaborations', '/app/collaborations']],
  ACADEMICIAN: [['Overview', '/app'], ['Students', '/app/students'], ['Skill mapping', '/app/skills'], ['Collaborations', '/app/collaborations']],
  INSTITUTION: [['Overview', '/app'], ['Institution profile', '/app/profile'], ['Students', '/app/students'], ['Collaborations', '/app/collaborations']],
  ADMIN: [['Overview', '/app'], ['Users', '/app/users'], ['Institutions', '/app/users?view=institutions'], ['Skills', '/app/skills'], ['Opportunities', '/app/opportunities'], ['Applications', '/app/applications'], ['Collaborations', '/app/collaborations'], ['Placements', '/app/placement']],
};

const appRoles = ['STUDENT', 'INDUSTRY', 'FACULTY', 'ACADEMICIAN', 'INSTITUTION', 'ADMIN'];

function canAccessAppPath(role, pathname) {
  if (!appRoles.includes(role)) return false;

  const path = pathname.replace(/^\/app/, '') || '/';
  const rules = [
    ['/student-profile', ['STUDENT']],
    ['/institution-profile', ['INSTITUTION']],
    ['/assessment', ['STUDENT']],
    ['/skill-gaps', ['STUDENT']],
    ['/certifications', ['STUDENT']],
    ['/projects', ['STUDENT']],
    ['/interests', ['STUDENT']],
    ['/recommendations', ['STUDENT']],
    ['/opportunities/new', ['INDUSTRY', 'ADMIN']],
    ['/applications', ['STUDENT', 'ADMIN']],
    ['/applicants', ['INDUSTRY', 'ADMIN']],
    ['/placement', ['STUDENT', 'INDUSTRY', 'ADMIN']],
    ['/users', ['ADMIN']],
    ['/students', ['ADMIN', 'FACULTY', 'ACADEMICIAN', 'INDUSTRY', 'INSTITUTION']],
    ['/internships', ['STUDENT', 'INDUSTRY', 'ADMIN']],
    ['/jobs', ['STUDENT', 'INDUSTRY', 'ADMIN']],
    ['/skills', ['STUDENT', 'ADMIN', 'ACADEMICIAN']],
  ];
  const rule = rules.find(([route]) => path === route || path.startsWith(`${route}/`));
  return !rule || rule[1].includes(role);
}

function useSession() {
  const [session, setSession] = useState(() => {
    try { return JSON.parse(localStorage.getItem('portal_session')) || null; } catch { return null; }
  });
  useEffect(() => {
    const logout = () => setSession(null);
    window.addEventListener('portal:logout', logout);
    return () => window.removeEventListener('portal:logout', logout);
  }, []);
  const save = (next) => { localStorage.setItem('portal_session', JSON.stringify(next)); setSession(next); };
  const logout = () => { localStorage.removeItem('portal_session'); setSession(null); };
  return { session, save, logout };
}

function App() {
  const auth = useSession();
  return <Routes>
    <Route path="/" element={<Home />} />
    <Route path="/about" element={<About />} />
    <Route path="/login" element={<AuthPage mode="login" onAuth={auth.save} />} />
    <Route path="/register" element={<AuthPage mode="register" onAuth={auth.save} />} />
    <Route path="/app/*" element={auth.session ? <AppShell auth={auth} /> : <Navigate to="/login" replace />} />
    <Route path="*" element={<Navigate to="/" replace />} />
  </Routes>;
}

function Home() {
  return <div className="public-page home-page"><PublicNav /><main className="home-content">
    <p className="eyebrow">ACADEMIA × INDUSTRY</p>
    <h1>Turn capability<br /><em>into opportunity.</em></h1>
    <p className="lead">A practical workspace for students, educators, and employers to make skills visible and collaboration easier.</p>
    <div className="hero-actions"><Link className="button primary" to="/register">Create an account</Link><Link className="text-link" to="/about">See how it works <span>→</span></Link></div>
    <div className="signal-row"><span><strong>01</strong> Map skills clearly</span><span><strong>02</strong> Meet the right people</span><span><strong>03</strong> Move forward</span></div>
  </main></div>;
}

function About() {
  return <div className="public-page"><PublicNav /><main className="about-content"><p className="eyebrow">THE PORTAL</p><h1>One shared view of readiness.</h1><p className="lead">Students bring their goals. Faculty bring context. Industry brings real work. The portal connects those signals in one focused workspace.</p><div className="about-grid"><InfoCard number="01" title="Skills, with evidence" text="Track proficiency, assessments, and gaps without turning growth into a black box." /><InfoCard number="02" title="Opportunities, in context" text="Browse roles and projects, then understand what each one asks for." /><InfoCard number="03" title="Collaboration that moves" text="Keep people, applications, and outcomes connected from request to placement." /></div></main></div>;
}

function InfoCard({ number, title, text }) { return <article className="info-card"><span>{number}</span><h2>{title}</h2><p>{text}</p></article>; }
function PublicNav() { return <nav className="public-nav"><Link className="brand" to="/">CAMPUS<span>×</span>WORK</Link><div><Link to="/about">About</Link><Link className="nav-cta" to="/login">Sign in</Link></div></nav>; }

function AuthPage({ mode, onAuth }) {
  const navigate = useNavigate();
  const [form, setForm] = useState({
    name: '', email: '', password: '', role: 'STUDENT', code: '', newPassword: '',
  });
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [busy, setBusy] = useState(false);
  const [resetStep, setResetStep] = useState('');
  const register = mode === 'register';
  const submit = async (event) => {
    event.preventDefault(); setError(''); setSuccess(''); setBusy(true);
    try {
      if (resetStep === 'request') {
        const response = await authApi.requestPasswordReset({ email: form.email });
        setSuccess(response.data.message);
        setResetStep('complete');
      } else if (resetStep === 'complete') {
        const response = await authApi.completePasswordReset({
          email: form.email, code: form.code, newPassword: form.newPassword,
        });
        setSuccess(response.data.message);
        setResetStep('');
        setForm((current) => ({ ...current, password: '', code: '', newPassword: '' }));
      } else {
        const response = await (register
          ? authApi.register(form)
          : authApi.login({ email: form.email, password: form.password }));
        onAuth(response.data);
        navigate('/app');
      }
    }
    catch (err) { setError(err.response?.data?.message || 'We could not complete that request. Check the API is running and try again.'); }
    finally { setBusy(false); }
  };
  const resetMode = Boolean(resetStep);
  return <div className="auth-page"><PublicNav /><div className="auth-panel"><p className="eyebrow">{register ? 'JOIN THE NETWORK' : resetMode ? 'ACCOUNT RECOVERY' : 'WELCOME BACK'}</p><h1>{register ? 'Start with your next step.' : resetMode ? 'Reset your password.' : 'Good to see you.'}</h1><p className="muted">{register ? 'Your role shapes the workspace you see.' : resetStep === 'request' ? 'We’ll email a one-time verification code if an account matches.' : resetStep === 'complete' ? 'Enter the six-digit code from your email and choose a new password.' : 'Sign in to continue your work.'}</p><form onSubmit={submit} className="form-stack">
    {register && <Field label="Name"><input required maxLength="150" value={form.name} onChange={update(setForm, 'name')} /></Field>}
    <Field label="Email"><input required type="email" maxLength="320" value={form.email} onChange={update(setForm, 'email')} disabled={resetStep === 'complete'} /></Field>
    {!register && !resetMode && <Field label="Password"><input required minLength="8" maxLength="255" type="password" value={form.password} onChange={update(setForm, 'password')} /></Field>}
    {resetStep === 'complete' && <>
      <Field label="Verification code"><input required inputMode="numeric" pattern="[0-9]{6}" maxLength="6" value={form.code} onChange={update(setForm, 'code')} /></Field>
      <Field label="New password"><input required minLength="8" maxLength="255" type="password" value={form.newPassword} onChange={update(setForm, 'newPassword')} /></Field>
    </>}
    {register && <Field label="Role"><select value={form.role} onChange={update(setForm, 'role')}><option value="STUDENT">Student</option><option value="INDUSTRY">Industry</option><option value="INSTITUTION">Institution</option></select></Field>}
    {error && <p className="form-error">{error}</p>}{success && <p className="muted" role="status">{success}</p>}<button className="button primary full" disabled={busy}>{busy ? 'Working…' : register ? 'Create account' : resetStep === 'request' ? 'Send verification code' : resetStep === 'complete' ? 'Reset password' : 'Sign in'}</button>
  </form>{!register && !resetMode && <p className="switch-auth"><button type="button" className="text-link link-button" onClick={() => { setError(''); setSuccess(''); setResetStep('request'); }}>Forgot password?</button></p>}{resetMode ? <p className="switch-auth"><button type="button" className="text-link link-button" onClick={() => { setError(''); setSuccess(''); setResetStep(''); }}>Back to sign in</button></p> : <p className="switch-auth">{register ? 'Already registered?' : 'New to the portal?'} <Link to={register ? '/login' : '/register'}>{register ? 'Sign in' : 'Create an account'}</Link></p>}</div></div>;
}
function update(setter, key) { return (event) => setter((current) => ({ ...current, [key]: event.target.value })); }
function Field({ label, children }) { return <label className="field"><span>{label}</span>{children}</label>; }

function AppShell({ auth }) {
  const { session, logout } = auth;
  const role = session?.user?.role || 'STUDENT';
  const location = useLocation();
  const menus = roleMenus[role] || [];
  if (!appRoles.includes(role)) return <Navigate to="/login" replace />;
  if (!canAccessAppPath(role, location.pathname)) return <Navigate to="/app" replace />;
  if (role === 'FACULTY' && !session?.user?.facultyInstitutionId) {
    return <FacultyInstitutionSetup session={session} onComplete={auth.save} logout={logout} />;
  }
  return <div className="app-frame"><aside className="sidebar"><Link className="brand sidebar-brand" to="/app">CAMPUS<span>×</span>WORK</Link><div className="workspace-label">WORKSPACE <b>{role}</b></div><nav className="side-nav">{menus.map(([label, path]) => <NavLink key={path} end={path === '/app'} to={path}>{label}</NavLink>)}</nav><div className="sidebar-bottom"><div className="user-chip"><div className="avatar">{(session?.user?.name || 'U').slice(0, 1).toUpperCase()}</div><div><strong>{session?.user?.name || session?.user?.email}</strong><small>{session?.user?.email}</small></div></div><button className="logout" onClick={logout}>Sign out</button></div></aside><main className="app-main"><header className="mobile-header"><Link className="brand" to="/app">CAMPUS<span>×</span>WORK</Link><button className="logout" onClick={logout}>Sign out</button></header><Routes><Route index element={<Overview role={role} />} /><Route path="profile" element={<Profile />} /><Route path="student-profile" element={<StudentProfilePage />} /><Route path="skills" element={role === 'ADMIN' ? <AdminSkills /> : <Skills role={role} />} /><Route path="assessment" element={<Assessment />} /><Route path="skill-gaps" element={<SkillGaps />} /><Route path="certifications" element={<PortfolioPage type="CERTIFICATION" />} /><Route path="projects" element={<PortfolioPage type="PROJECT" />} /><Route path="interests" element={<PortfolioPage type="INTEREST" />} /><Route path="recommendations" element={<UnavailablePage title="Recommendations" />} /><Route path="opportunities" element={<Opportunities role={role} />} /><Route path="opportunities/new" element={<OpportunityForm />} /><Route path="opportunities/:id" element={<OpportunityDetails />} /><Route path="applications" element={<Applications role={role} />} /><Route path="applicants" element={<Applicants />} /><Route path="collaborations" element={<Collaborations />} /><Route path="placement" element={<Placements />} /><Route path="users" element={<AdminUsers />} /><Route path="students" element={role === 'FACULTY' ? <FacultyStudents /> : <Students />} /><Route path="internships" element={<CareerListings kind="internship" />} /><Route path="jobs" element={<CareerListings kind="job" />} /><Route path="*" element={<Overview role={role} />} /></Routes></main></div>;
}

function Page({ eyebrow, title, description, actions, children }) { return <div className="page"><div className="page-heading"><div><p className="eyebrow">{eyebrow}</p><h1>{title}</h1>{description && <p className="lead compact">{description}</p>}</div>{actions}</div>{children}</div>; }

function FacultyInstitutionSetup({ session, onComplete, logout }) {
  const [institutionName, setInstitutionName] = useState('');
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');
  useEffect(() => {
    portalApi.profile()
      .then((response) => setInstitutionName(response.data.facultyInstitutionName || ''))
      .catch(() => setError('Your faculty profile could not be loaded.'))
      .finally(() => setLoading(false));
  }, []);
  const submit = async (event) => {
    event.preventDefault();
    setSaving(true);
    setError('');
    try {
      const response = await portalApi.updateFacultyInstitution({ name: institutionName });
      onComplete({ ...session, user: response.data });
    } catch (requestError) {
      setError(requestError.response?.data?.message || 'College or university could not be saved.');
    } finally {
      setSaving(false);
    }
  };
  return <div className="app-main"><Page eyebrow="FACULTY SETUP" title="Connect your college or university." description="This lets us show you only the students enrolled at your institution.">
    <form className="card form-stack" onSubmit={submit}>
      <p className="muted">Enter the college or university name used in student profiles. If it is new, it will be added to the institution directory.</p>
      <Field label="College or university name"><input required maxLength="200" value={institutionName} onChange={(event) => setInstitutionName(event.target.value)} placeholder="e.g. Demo University" disabled={loading} /></Field>
      {error && <p className="form-error">{error}</p>}
      <div className="form-actions"><button className="button primary" disabled={loading || saving || !institutionName.trim()}>{saving ? 'Saving…' : 'Continue to faculty workspace'}</button><button type="button" className="button" onClick={logout}>Sign out</button></div>
    </form>
  </Page></div>;
}

function FacultyStudents() {
  const [items, setItems] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  useEffect(() => {
    portalApi.facultyStudents()
      .then((response) => setItems(response.data))
      .catch((requestError) => setError(requestError.response?.data?.message || 'Students for your institution could not be loaded.'))
      .finally(() => setLoading(false));
  }, []);
  return <Page eyebrow="FACULTY VIEW" title="Students at your institution." description="Review student skills and application progress for your college or university.">
    {error ? <p className="form-error">{error}</p> : loading ? <Loading /> : !items.length ? <Empty text="No student profiles are linked to this college or university yet." /> : <div className="table-wrap"><table><thead><tr><th>STUDENT</th><th>PROGRAM</th><th>SKILLS</th><th>APPLICATIONS</th></tr></thead><tbody>{items.map(({ student, skills, applications }) => <tr key={student.id}>
      <td><strong>{student.userName}</strong><br />{student.userEmail}<br /><small>{student.institutionName}</small></td>
      <td>{student.branch}<br />Graduates {student.graduationYear}</td>
      <td>{skills.length ? skills.map((skill) => <div key={skill.id}><strong>{skill.skillName}</strong> · {skill.proficiencyLevel}</div>) : 'No skills recorded'}</td>
      <td>{applications.length ? applications.map((application) => <div key={application.id}><strong>{application.opportunityTitle}</strong><br /><small>{application.opportunityType} · {application.status}</small></div>) : 'No applications yet'}</td>
    </tr>)}</tbody></table></div>}
  </Page>;
}
function Overview({ role }) {
  const [data, setData] = useState({ opportunities: null, applications: null, collaborations: null, summary: null });
  const [loading, setLoading] = useState(true);
  useEffect(() => {
    const load = async () => {
      let userId = null;
      try { userId = JSON.parse(localStorage.getItem('portal_session') || '{}').user?.id; } catch { /* Ignore a corrupt local session. */ }

      const opportunityRequest = Promise.all([portalApi.opportunities(), portalApi.internships(), portalApi.jobs()])
        .then((responses) => {
          const today = new Date().toISOString().slice(0, 10);
          return responses.flatMap((response) => response.data)
            .filter((item) => item.status === 'OPEN' && item.applicationDeadline >= today);
        });
      const applicationsRequest = role === 'STUDENT'
        ? portalApi.applications()
        : role === 'ADMIN'
          ? portalApi.admin.applications()
          : role === 'INDUSTRY'
            ? Promise.all([portalApi.opportunities(), portalApi.internships(), portalApi.jobs()]).then(async ([opportunitiesResponse, internshipsResponse, jobsResponse]) => {
              const owned = [
                ...opportunitiesResponse.data,
                ...internshipsResponse.data.map((item) => ({ ...item, type: 'INTERNSHIP' })),
                ...jobsResponse.data.map((item) => ({ ...item, type: 'JOB' })),
              ]
                .filter((item) => String(item.industryId) === String(userId));
              const lists = await Promise.all(owned.map((item) => portalApi.industryApplications(item.type, item.id)));
              return { data: lists.flatMap((list) => list.data) };
            })
            : Promise.resolve(null);
      const summaryRequest = role === 'ADMIN'
        ? Promise.all([portalApi.admin.users(), portalApi.admin.placements()])
            .then(([users, placements]) => ({ users: users.data.length, placements: placements.data.length }))
        : role === 'INDUSTRY'
          ? portalApi.industryPlacements().then((response) => ({ placements: response.data.length }))
          : role === 'STUDENT'
            ? portalApi.placements().then((response) => ({ placements: response.data.length }))
            : portalApi.students().then((response) => ({ students: response.data.length }));

      const [opportunities, applications, collaborations, summary] = await Promise.allSettled([
        opportunityRequest,
        applicationsRequest,
        portalApi.collaborations(),
        summaryRequest,
      ]);
      setData({
        opportunities: opportunities.status === 'fulfilled' ? opportunities.value : null,
        applications: applications.status === 'fulfilled' ? applications.value?.data ?? null : null,
        collaborations: collaborations.status === 'fulfilled' ? collaborations.value.data : null,
        summary: summary.status === 'fulfilled' ? summary.value : null,
      });
      setLoading(false);
    };
    load();
  }, [role]);

  const count = (items, activeOnly = false) => {
    if (loading) return '—';
    if (items === null) return '—';
    if (!activeOnly) return items.length;
    return items.filter((item) => ['APPLIED', 'SHORTLISTED', 'INTERVIEW', 'REQUESTED', 'ACCEPTED'].includes(item.status)).length;
  };
  const extraMetrics = data.summary ? Object.entries(data.summary).map(([key, value]) => (
    <Metric key={key} label={key === 'users' ? 'Registered users' : key === 'placements' ? 'Placement records' : 'Student profiles'} value={loading ? '—' : value} detail="Current records" />
  )) : [];
  return <Page eyebrow={`${role} workspace`} title={role === 'STUDENT' ? 'Make your next move count.' : role === 'INDUSTRY' ? 'Build the right pipeline.' : 'See the network clearly.'} description="A focused view of the work that needs your attention.">
    <div className="metric-grid">
      <Metric label="Open opportunities" value={count(data.opportunities)} detail={data.opportunities === null && !loading ? 'Could not load opportunities' : 'Including internships and jobs'} />
      <Metric label="Active applications" value={count(data.applications, true)} detail={data.applications === null && !loading ? 'Could not load applications' : 'In review or interview'} />
      <Metric label="Active collaborations" value={count(data.collaborations, true)} detail={data.collaborations === null && !loading ? 'Could not load collaborations' : 'Requested or accepted'} />
      {extraMetrics}
    </div>
    <section className="section-block"><div className="section-title"><h2>Start here</h2><span>SHORTCUTS</span></div><div className="shortcut-grid"><Link to="/app/opportunities" className="shortcut"><b>Browse opportunities</b><span>Find work that fits your trajectory →</span></Link><Link to="/app/skills" className="shortcut"><b>Update your skills</b><span>Make your capabilities easier to read →</span></Link><Link to="/app/collaborations" className="shortcut"><b>Open collaborations</b><span>Keep conversations moving →</span></Link></div></section>
  </Page>;
}
function Metric({ label, value, detail }) { return <article className="metric"><span>{label}</span><strong>{value}</strong><small>{detail}</small></article>; }

function Profile() {
  const [profile, setProfile] = useState({});
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');
  let role = '';
  try { role = JSON.parse(localStorage.getItem('portal_session') || '{}').user?.role || ''; } catch { /* Ignore a corrupt local session. */ }
  const isIndustry = role === 'INDUSTRY';
  const isInstitution = role === 'INSTITUTION';

  useEffect(() => {
    if (isInstitution) return;
    portalApi.profile().then((response) => setProfile(response.data))
      .catch(() => setError('Profile could not be loaded.'));
  }, [isInstitution]);

  const save = async (event) => {
    event.preventDefault();
    setError('');
    setMessage('');
    try {
      const response = await portalApi.updateProfile(profile);
      setProfile(response.data);
      setMessage('Profile updated.');
    } catch (requestError) {
      setError(requestError.response?.data?.message || 'Profile could not be updated.');
    }
  };

  if (isInstitution) return <InstitutionProfilePage />;

  return <Page eyebrow={isIndustry ? 'COMPANY PROFILE' : 'YOUR PROFILE'} title={isIndustry ? 'Give the company a clear identity.' : 'Make the context visible.'} description={isIndustry ? 'Help students and academic partners understand your organization.' : 'Keep the identity behind your work current.'}>
    <form className="card form-grid" onSubmit={save}>
      <Field label={isIndustry ? 'Contact name' : 'Name'}><input required maxLength="150" value={profile.name || ''} onChange={(event) => setProfile({ ...profile, name: event.target.value })} /></Field>
      <Field label="Email"><input required type="email" maxLength="320" value={profile.email || ''} onChange={(event) => setProfile({ ...profile, email: event.target.value })} /></Field>
      {isIndustry && <>
        <Field label="Company name"><input maxLength="200" value={profile.companyName || ''} onChange={(event) => setProfile({ ...profile, companyName: event.target.value })} /></Field>
        <Field label="Website"><input type="url" maxLength="1000" value={profile.website || ''} onChange={(event) => setProfile({ ...profile, website: event.target.value })} /></Field>
        <Field label="Industry sector"><input maxLength="150" value={profile.industrySector || ''} onChange={(event) => setProfile({ ...profile, industrySector: event.target.value })} /></Field>
        <Field label="Headquarters"><input maxLength="200" value={profile.headquarters || ''} onChange={(event) => setProfile({ ...profile, headquarters: event.target.value })} /></Field>
        <Field label="Company description"><textarea maxLength="2000" rows="4" value={profile.companyDescription || ''} onChange={(event) => setProfile({ ...profile, companyDescription: event.target.value })} /></Field>
      </>}
      {error && <p className="form-error">{error}</p>}
      <div className="form-actions"><button className="button primary">Save profile</button>{message && <span className="success">{message}</span>}</div>
    </form>
  </Page>;
}

function InstitutionProfilePage() {
  const [profile, setProfile] = useState({ name: '', website: '', location: '', description: '' });
  const [error, setError] = useState('');
  const [message, setMessage] = useState('');
  useEffect(() => {
    portalApi.institutionProfile().then((response) => setProfile(response.data))
      .catch((requestError) => setError(requestError.response?.data?.message || 'Institution profile could not be loaded.'));
  }, []);
  const save = async (event) => {
    event.preventDefault();
    setError('');
    setMessage('');
    try {
      const response = await portalApi.updateInstitutionProfile(profile);
      setProfile(response.data);
      setMessage('Institution profile updated.');
    } catch (requestError) {
      setError(requestError.response?.data?.message || 'Institution profile could not be saved.');
    }
  };
  return <Page eyebrow="INSTITUTION PROFILE" title="Make your campus visible." description="Keep your institution details current for students and industry collaborators.">
    <form className="card form-grid" onSubmit={save}>
      <Field label="Institution name"><input required maxLength="200" value={profile.name || ''} onChange={(event) => setProfile({ ...profile, name: event.target.value })} /></Field>
      <Field label="Website"><input type="url" maxLength="1000" value={profile.website || ''} onChange={(event) => setProfile({ ...profile, website: event.target.value })} /></Field>
      <Field label="Location"><input maxLength="200" value={profile.location || ''} onChange={(event) => setProfile({ ...profile, location: event.target.value })} /></Field>
      <Field label="Description"><textarea maxLength="2000" rows="4" value={profile.description || ''} onChange={(event) => setProfile({ ...profile, description: event.target.value })} /></Field>
      {error && <p className="form-error">{error}</p>}{message && <p className="success">{message}</p>}
      <div className="form-actions"><button className="button primary">Save institution profile</button></div>
    </form>
  </Page>;
}

function Skills({ role }) { const [skills, setSkills] = useState([]); const [studentSkills, setStudentSkills] = useState([]); const [studentId, setStudentId] = useState(null);
  const [profileMissing, setProfileMissing] = useState(false);
  const [loading, setLoading] = useState(true);
  const [form, setForm] = useState({ skillId: '', newSkillName: '', proficiencyLevel: 'BEGINNER' });
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');
  const [editingId, setEditingId] = useState(null);

  useEffect(() => {
    let active = true;
    const load = async () => {
      setLoading(true);
      setError('');
      try {
        const [catalog, records] = await Promise.all([
          portalApi.skills(),
          role === 'STUDENT' ? portalApi.studentSkills() : portalApi.allStudentSkills(),
        ]);
        if (!active) return;
        setSkills(catalog.data);
        setStudentSkills(records.data);
      } catch (requestError) {
        if (active) setError(requestError.response?.data?.message || 'Skills could not be loaded. Please refresh and try again.');
      } finally {
        if (active) setLoading(false);
      }
      if (active && role === 'STUDENT') {
        try {
          const profile = await portalApi.studentProfile();
          if (active) {
            setStudentId(profile.data.id);
            setProfileMissing(false);
          }
        } catch {
          if (active) setProfileMissing(true);
        }
      }
    };
    load();
    return () => { active = false; };
  }, [role]);

  const add = async (event) => {
    event.preventDefault();
    setError('');
    setMessage('');
    if (!studentId) {
      setError('Create your student profile before adding skills.');
      return;
    }
    try {
      const skill = form.skillId === 'new'
        ? await portalApi.createStudentCatalogSkill(form.newSkillName.trim()).then((response) => response.data)
        : null;
      if (skill) {
        if (studentSkills.some((studentSkill) => studentSkill.skillId === skill.id)) {
          setError('You have already added this skill. Edit its proficiency from your skills list.');
          return;
        }
        setSkills((current) => current.some((item) => item.id === skill.id) ? current : [...current, skill]);
      } else if (studentSkills.some((studentSkill) => studentSkill.skillId === Number(form.skillId))) {
        setError('You have already added this skill. Edit its proficiency from your skills list.');
        return;
      }
      const payload = {
        studentId,
        skillId: skill?.id ?? Number(form.skillId),
        proficiencyLevel: form.proficiencyLevel,
      };
      if (editingId) await portalApi.updateStudentSkill(editingId, payload);
      else await portalApi.createStudentSkill(payload);
      setStudentSkills((await portalApi.studentSkills()).data);
      setForm({ skillId: '', newSkillName: '', proficiencyLevel: 'BEGINNER' });
      setEditingId(null);
      setMessage(editingId ? 'Skill proficiency updated.' : 'Skill added.');
    } catch (requestError) {
      setError(requestError.response?.data?.message || (editingId
        ? 'Skill proficiency could not be updated.'
        : 'Could not add this skill. Please try again.'));
    }
  };

  const edit = (skill) => {
    setEditingId(skill.id);
    setForm({ skillId: String(skill.skillId), newSkillName: '', proficiencyLevel: skill.proficiencyLevel });
    setError('');
    setMessage('');
  };
  const cancelEdit = () => {
    setEditingId(null);
    setForm({ skillId: '', newSkillName: '', proficiencyLevel: 'BEGINNER' });
    setError('');
    setMessage('');
  };
  const remove = async (skill) => {
    if (!window.confirm(`Remove ${skill.skillName || `skill #${skill.skillId}`} from your profile?`)) return;
    setError('');
    setMessage('');
    try {
      await portalApi.deleteStudentSkill(skill.id);
      setStudentSkills((current) => current.filter((item) => item.id !== skill.id));
      if (editingId === skill.id) cancelEdit();
      setMessage('Skill removed from your profile.');
    } catch (requestError) {
      setError(requestError.response?.data?.message || 'Skill could not be removed.');
    }
  };

  return <Page eyebrow="SKILL MAP" title="Capabilities, made legible." description="Record the skills you bring and the level you can demonstrate."><div className="split-layout"><section className="card"><div className="section-title"><h2>My skills</h2><span>{studentSkills.length} RECORDED</span></div>{loading ? <Loading /> : studentSkills.length ? <div className="student-skill-list">{studentSkills.map((skill) => <div className="student-skill-row" key={skill.id}><div><strong>{skill.skillName || skills.find((item) => item.id === skill.skillId)?.name || `Skill #${skill.skillId}`}</strong><span>{skill.proficiencyLevel}</span></div>{role === 'STUDENT' && <div className="student-skill-actions"><button type="button" className="text-link" onClick={() => edit(skill)}>Edit</button><button type="button" className="text-link" onClick={() => remove(skill)}>Delete</button></div>}</div>)}</div> : <Empty text={role === 'STUDENT' ? 'No skills recorded yet. Add a skill using the form.' : 'Student skill records appear here.'} />}{error && <p className="form-error">{error}</p>}{message && <p className="success">{message}</p>}</section>{role === 'STUDENT' && <form className="card form-stack" onSubmit={add}><h2>{editingId ? 'Update skill proficiency' : 'Add a skill'}</h2>{profileMissing && <p className="muted">Set up your academic profile first. <Link to="/app/student-profile">Create student profile →</Link></p>}<Field label="Skill"><select required value={form.skillId} onChange={update(setForm, 'skillId')} disabled={Boolean(editingId)}><option value="">Choose a skill</option>{skills.map((skill) => <option key={skill.id} value={skill.id}>{skill.name}</option>)}{!editingId && <option value="new">Add a new skill…</option>}</select></Field>{form.skillId === 'new' && <Field label="New skill name"><input required maxLength="100" value={form.newSkillName} onChange={update(setForm, 'newSkillName')} placeholder="e.g. Figma or Data visualization" /></Field>}<Field label="Proficiency"><select value={form.proficiencyLevel} onChange={update(setForm, 'proficiencyLevel')}>{['BEGINNER', 'INTERMEDIATE', 'ADVANCED', 'EXPERT'].map((level) => <option key={level}>{level}</option>)}</select></Field>{error && <p className="form-error">{error}</p>}{message && <p className="success">{message}</p>}<div className="form-actions"><button className="button primary" disabled={!studentId || loading}>{editingId ? 'Save proficiency' : 'Add skill'}</button>{editingId && <button type="button" className="button" onClick={cancelEdit}>Cancel</button>}</div></form>}</div></Page>; }

function Assessment() {
  const [items, setItems] = useState([]);
  const [skills, setSkills] = useState([]);
  const [studentId, setStudentId] = useState(null);
  const [saving, setSaving] = useState(false);
  const [form, setForm] = useState({ skillId: '', score: '' });
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');

  const refresh = async () => setItems((await portalApi.assessments()).data);
  useEffect(() => {
    portalApi.assessments().then((response) => setItems(response.data)).catch(() => setError('Assessments could not be loaded.'));
    portalApi.skills().then((response) => setSkills(response.data)).catch(() => setError('Skills could not be loaded.'));
    portalApi.studentProfile().then((response) => setStudentId(response.data.id)).catch(() => setError('Create your student profile before submitting an assessment.'));
  }, []);

  const submit = async (event) => {
    event.preventDefault();
    if (saving) return;
    setError('');
    setMessage('');
    setSaving(true);
    try {
      await portalApi.submitAssessment({ studentId, skillId: Number(form.skillId), score: Number(form.score) });
      await refresh();
      setMessage('Assessment saved. If you already assessed this skill, your score and proficiency level have been updated.');
    } catch (requestError) {
      setError(requestError.response?.data?.message || 'Assessment could not be saved.');
    } finally {
      setSaving(false);
    }
  };

  return <Page eyebrow="ASSESSMENT" title="Measure what you know." description="Submit a score to record evidence of your current proficiency.">
    <form className="card form-grid" onSubmit={submit}>
      <Field label="Skill"><select required value={form.skillId} onChange={update(setForm, 'skillId')}><option value="">Choose a skill</option>{skills.map((skill) => <option key={skill.id} value={skill.id}>{skill.name}</option>)}</select></Field>
      <Field label="Score (0–100)"><input required type="number" min="0" max="100" value={form.score} onChange={update(setForm, 'score')} /></Field>
      {error && <p className="form-error">{error}</p>}{message && <p className="success">{message}</p>}
      <div className="form-actions"><button className="button primary" disabled={!studentId || !skills.length || saving}>{saving ? 'Saving…' : 'Submit or update assessment'}</button></div>
    </form>
    <section className="section-block"><div className="section-title"><h2>Assessment history</h2></div><DataList items={items} empty="No assessments submitted yet." fields={['skillId', 'score', 'level', 'assessedAt']} /></section>
  </Page>;
}
function SkillGaps() {
  const [searchParams] = useSearchParams();
  const [id, setId] = useState(() => searchParams.get('opportunity') || '');
  const [result, setResult] = useState(null);
  const check = async (event) => {
    event.preventDefault();
    try {
      setResult((await portalApi.skillGap(id)).data);
    } catch {
      setResult({ error: 'Opportunity or student profile not found.' });
    }
  };
  return <Page eyebrow="SKILL GAPS" title="Know what to close." description="Compare your current skills with an opportunity's requirements.">
    <form className="inline-form" onSubmit={check}><input required type="number" min="1" placeholder="Opportunity ID" value={id} onChange={(event) => setId(event.target.value)} /><button className="button primary">Calculate match</button></form>
    {result && (result.error ? <p className="form-error">{result.error}</p> : <div className="match-card"><strong>{result.matchPercentage}%</strong><div><p>{result.matchedCount} of {result.requiredCount} requirements met</p><p className="muted">Skills to add: {(result.missingSkills || []).join(', ') || 'None'}</p><p className="muted">Skills to improve: {(result.insufficientProficiencySkills || []).join(', ') || 'None'}</p></div></div>)}
  </Page>;
}

function Opportunities({ role }) {
  const { session } = useSession();
  const [items, setItems] = useState([]);
  const [loading, setLoading] = useState(true);
  const [deletingId, setDeletingId] = useState(null);
  const [error, setError] = useState('');

  useEffect(() => {
    portalApi.opportunities()
      .then((response) => setItems(response.data))
      .catch((requestError) => setError(requestError.response?.data?.message || 'Opportunities could not be loaded.'))
      .finally(() => setLoading(false));
  }, []);

  const remove = async (item) => {
    if (!window.confirm(`Delete "${item.title}"? This cannot be undone.`)) return;
    setDeletingId(item.id);
    setError('');
    try {
      await portalApi.deleteOpportunity(item.id);
      setItems((current) => current.filter((opportunity) => opportunity.id !== item.id));
    } catch (requestError) {
      setError(requestError.response?.data?.message || 'The opportunity could not be deleted. Please try again.');
    } finally {
      setDeletingId(null);
    }
  };

  return <Page eyebrow="OPPORTUNITIES" title="Work worth stepping into." description="Browse projects, programs, internships, and roles across the network." actions={role === 'INDUSTRY' || role === 'ADMIN' ? <Link className="button primary" to="/app/opportunities/new">New opportunity</Link> : null}>
    {error && <p className="form-error" role="alert">{error}</p>}
    <div className="opportunity-grid">{loading ? <Loading /> : items.length ? items.map((item) => (
      <OpportunityCard
        key={item.id}
        item={item}
        canDelete={role === 'INDUSTRY' && String(session?.user?.id) === String(item.industryId)}
        deleting={deletingId === item.id}
        onDelete={() => remove(item)}
      />
    )) : <Empty text="No opportunities are available yet." />}</div>
  </Page>;
}

function OpportunityCard({ item, canDelete, deleting, onDelete }) {
  return <article className="opportunity-card">
    <div className="card-top"><span className="pill">{item.type || 'OPPORTUNITY'}</span><span>{item.status}</span></div>
    <h2>{item.title}</h2>
    <p>{item.description}</p>
    <footer><span>{item.companyName || item.industrySector || 'Industry partner'} · {item.location}</span><Link to={`/app/opportunities/${item.id}`}>View details →</Link></footer>
    {canDelete && <button type="button" className="text-link opportunity-delete" disabled={deleting} onClick={onDelete}>{deleting ? 'Deleting…' : 'Delete opportunity'}</button>}
  </article>;
}
function OpportunityDetails() {
  const { id } = useParams();
  const [item, setItem] = useState(null);
  const [message, setMessage] = useState('');
  const [studentId, setStudentId] = useState(null);
  const [profileMissing, setProfileMissing] = useState(false);
  const [showEvidenceSharing, setShowEvidenceSharing] = useState(false);
  const session = JSON.parse(localStorage.getItem('portal_session') || '{}');
  const canManageRequirements = session.user?.role === 'ADMIN'
    || (session.user?.role === 'INDUSTRY' && String(session.user?.id) === String(item?.industryId));

  useEffect(() => {
    portalApi.opportunity(id).then((response) => setItem(response.data)).catch(() => setItem({ error: true }));
  }, [id]);

  useEffect(() => {
    if (session.user?.role !== 'STUDENT') return;
    portalApi.studentProfile()
      .then((response) => setStudentId(response.data.id))
      .catch(() => setProfileMissing(true));
  }, [session.user?.role]);

  const apply = async (sharedPortfolioItemIds) => {
    if (!studentId) return;
    try {
      await portalApi.apply({ studentId, opportunityType: item.type, opportunityId: Number(id), sharedPortfolioItemIds });
      setMessage('Application submitted. Your selected certificates and projects were shared with this company.');
      setShowEvidenceSharing(false);
    } catch (requestError) {
      setMessage(requestError.response?.data?.message || 'Application could not be submitted.');
      throw requestError;
    }
  };

  if (!item) return <Page eyebrow="OPPORTUNITY" title="Loading…" />;
  return <Page eyebrow={item.type || 'OPPORTUNITY'} title={item.title || 'Opportunity unavailable'} description={item.description}><div className="detail-grid"><div className="card"><div className="detail-row"><span>Opportunity ID</span><b>{id}</b></div><div className="detail-row"><span>Company</span><b>{item.companyName || 'Industry partner'}</b></div>{item.companyWebsite && <div className="detail-row"><span>Website</span><a href={item.companyWebsite} target="_blank" rel="noreferrer">Visit company</a></div>}<div className="detail-row"><span>Status</span><b>{item.status}</b></div><div className="detail-row"><span>Location</span><b>{item.location}</b></div><div className="detail-row"><span>Deadline</span><b>{item.applicationDeadline}</b></div>{session.user?.role === 'STUDENT' && (studentId ? <><button className="button primary full" onClick={() => setShowEvidenceSharing(true)}>Apply now</button>{showEvidenceSharing && <PortfolioSharingForm onSubmit={apply} onCancel={() => setShowEvidenceSharing(false)} />}{message && <p className={message.startsWith('Application could') ? 'form-error' : 'success'}>{message}</p>}</> : profileMissing ? <p className="muted">Set up your student profile before applying. <Link to="/app/student-profile">Create student profile →</Link></p> : <p className="muted">Checking student profile…</p>)}</div><div className="card dark-card"><p className="eyebrow">A GOOD FIT STARTS HERE</p><h2>Read the requirements. Bring the evidence.</h2><Link className="text-link light" to={`/app/skill-gaps?opportunity=${id}`}>Check your skill match →</Link></div></div><OpportunitySkillRequirements opportunityId={id} canEdit={canManageRequirements} /></Page>;
}
function PortfolioSharingForm({ onSubmit, onCancel, initialSelectedIds = [], submitLabel = 'Submit application' }) {
  const [items, setItems] = useState([]);
  const [selectedIds, setSelectedIds] = useState(initialSelectedIds);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    let active = true;
    portalApi.portfolioItems()
      .then((response) => {
        if (active) {
          setItems(response.data.filter((item) => ['CERTIFICATION', 'PROJECT'].includes(item.type)));
          setSelectedIds(initialSelectedIds);
        }
      })
      .catch((requestError) => {
        if (active) setError(requestError.response?.data?.message || 'Your certificates and projects could not be loaded. You can still apply without sharing them.');
      })
      .finally(() => { if (active) setLoading(false); });
    return () => { active = false; };
  }, []);

  const submit = async (event) => {
    event.preventDefault();
    setSaving(true);
    setError('');
    try {
      await onSubmit(selectedIds);
    } catch (requestError) {
      setError(requestError.response?.data?.message || 'Your application could not be submitted.');
    } finally {
      setSaving(false);
    }
  };

  const toggle = (id) => setSelectedIds((current) => current.includes(id)
    ? current.filter((itemId) => itemId !== id)
    : [...current, id]);

  return <form className="portfolio-sharing-form" onSubmit={submit}>
    <h3>Choose what to share</h3>
    <p className="muted">Only items you select will be visible to this company with your application.</p>
    {loading ? <Loading /> : items.length ? <div className="portfolio-sharing-list">{items.map((item) => <label className="portfolio-sharing-item" key={item.id}>
      <input type="checkbox" checked={selectedIds.includes(item.id)} onChange={() => toggle(item.id)} />
      <span><strong>{item.title}</strong><small>{item.type === 'CERTIFICATION' ? 'Certificate' : 'Project'}{item.organization ? ` · ${item.organization}` : ''}</small></span>
    </label>)}</div> : <p className="muted">No certificates or projects saved yet. You can still apply without sharing portfolio items.</p>}
    {error && <p className="form-error">{error}</p>}
    <div className="form-actions"><button className="button primary" disabled={saving}>{saving ? 'Saving…' : submitLabel}</button><button type="button" className="button" disabled={saving} onClick={onCancel}>Cancel</button></div>
  </form>;
}
function OpportunityForm() {
  const { session } = useSession();
  const [form, setForm] = useState(() => ({ type: 'PROJECT', industryId: session?.user?.role === 'INDUSTRY' ? String(session.user.id) : '', title: '', description: '', location: '', duration: '', stipend: '', applicationDeadline: '', status: 'DRAFT' }));
  const [message, setMessage] = useState('');
  const [createdOpportunityId, setCreatedOpportunityId] = useState(null);
  const submit = async (event) => {
    event.preventDefault();
    setMessage('');
    setCreatedOpportunityId(null);
    try {
      const response = await portalApi.createOpportunity({ ...form, industryId: Number(form.industryId), stipend: form.stipend ? Number(form.stipend) : null });
      setCreatedOpportunityId(response.data.id);
      setMessage('Opportunity created.');
    } catch (error) {
      setMessage(error.response?.data?.message || 'Could not create opportunity.');
    }
  };
  const fields = [['title', 'Title', 'text'], ['location', 'Location', 'text'], ['duration', 'Duration', 'text'], ['stipend', 'Stipend', 'number'], ['applicationDeadline', 'Application deadline', 'date']];
  return <Page eyebrow="INDUSTRY STUDIO" title="Put real work on the table." description="Create a clear opportunity with the details candidates need.">
    <form className="card form-grid" onSubmit={submit}>
      {session?.user?.role !== 'INDUSTRY' && <Field label="Industry user ID"><input required type="number" min="1" value={form.industryId} onChange={update(setForm, 'industryId')} /></Field>}
      {session?.user?.role === 'INDUSTRY' && <p className="muted">This opportunity will be published under your industry account.</p>}
        {createdOpportunityId && <p className="success">Share this Opportunity ID with students for Skill Gaps: <strong>{createdOpportunityId}</strong></p>}
      {fields.map(([key, label, type]) => <Field key={key} label={label}><input required={key !== 'stipend'} type={type} value={form[key]} onChange={update(setForm, key)} /></Field>)}
      <Field label="Type"><select value={form.type} onChange={update(setForm, 'type')}>{['PROJECT', 'APPRENTICESHIP', 'PROGRAM'].map((type) => <option key={type}>{type}</option>)}</select></Field>
      <Field label="Status"><select value={form.status} onChange={update(setForm, 'status')}><option>DRAFT</option><option>OPEN</option></select></Field>
      <Field label="Description"><textarea required rows="5" value={form.description} onChange={update(setForm, 'description')} /></Field>
      <div className="form-actions"><button className="button primary">Create opportunity</button>{message && <span className={message.startsWith('Could not') ? 'form-error' : 'success'}>{message}</span>}</div>
    </form>
    {createdOpportunityId && <OpportunitySkillRequirements opportunityId={createdOpportunityId} canEdit />}
  </Page>;
}

function OpportunitySkillRequirements({ opportunityId, canEdit }) {
  const [skills, setSkills] = useState([]);
  const [requirements, setRequirements] = useState([]);
  const [skillId, setSkillId] = useState('');
  const [newSkillName, setNewSkillName] = useState('');
  const [requiredLevel, setRequiredLevel] = useState('BEGINNER');
  const [error, setError] = useState('');
  const [message, setMessage] = useState('');

  const refresh = async () => {
    const response = await portalApi.opportunitySkills(opportunityId);
    setRequirements(response.data);
  };

  useEffect(() => {
    refresh().catch(() => setError('Skill requirements could not be loaded.'));
    if (canEdit) portalApi.skills().then((response) => setSkills(response.data))
      .catch(() => setError('The skill catalog could not be loaded.'));
  }, [opportunityId, canEdit]);

  const add = async (event) => {
    event.preventDefault();
    setError('');
    setMessage('');
    try {
      let selectedSkillId = Number(skillId);
      if (skillId === 'new') {
        const normalizedName = newSkillName.trim();
        const existingSkill = skills.find((skill) => skill.name.trim().toLowerCase() === normalizedName.toLowerCase());
        if (existingSkill) {
          selectedSkillId = existingSkill.id;
        } else {
          const response = await portalApi.createSkill({
            name: normalizedName,
            category: 'Company-added',
            description: '',
          });
          selectedSkillId = response.data.id;
          setSkills((current) => [...current, response.data]);
        }
      }
      if (requirements.some((requirement) => requirement.skillId === selectedSkillId)) {
        setError('This skill is already a requirement for the opportunity.');
        return;
      }
      await portalApi.addOpportunitySkill(opportunityId, { skillId: selectedSkillId, requiredLevel });
      await refresh();
      setSkillId('');
      setNewSkillName('');
      setMessage('Required skill added.');
    } catch (requestError) {
      setError(requestError.response?.data?.message || 'Required skill could not be added.');
    }
  };

  return <section className="section-block"><div className="section-title"><h2>Required skills</h2><span>{requirements.length} SKILLS</span></div>
    {requirements.length ? <div className="tag-list">{requirements.map((requirement) => <span className="tag" key={requirement.id}>{requirement.skillName} <b>{requirement.requiredLevel}</b></span>)}</div> : <Empty text="No skill requirements have been added yet." />}
    {canEdit && <form className="inline-form" onSubmit={add}><Field label="Skill"><select required value={skillId} onChange={(event) => setSkillId(event.target.value)}><option value="">Choose a skill</option>{skills.filter((skill) => !requirements.some((requirement) => requirement.skillId === skill.id)).map((skill) => <option key={skill.id} value={skill.id}>{skill.name}</option>)}<option value="new">Enter a new skill…</option></select></Field>{skillId === 'new' && <Field label="New required skill"><input required maxLength="100" value={newSkillName} onChange={(event) => setNewSkillName(event.target.value)} placeholder="e.g. Python or Cloud security" /></Field>}<Field label="Minimum proficiency"><select value={requiredLevel} onChange={(event) => setRequiredLevel(event.target.value)}>{['BEGINNER', 'INTERMEDIATE', 'ADVANCED', 'EXPERT'].map((level) => <option key={level}>{level}</option>)}</select></Field><button className="button primary" disabled={!skillId || (skillId === 'new' && !newSkillName.trim())}>Add requirement</button></form>}
    {error && <p className="form-error">{error}</p>}{message && <p className="success">{message}</p>}
  </section>;
}

function Applications({ role }) {
  const [items, setItems] = useState([]);
  const [error, setError] = useState('');
  const [message, setMessage] = useState('');
  const [sharingApplicationId, setSharingApplicationId] = useState(null);
  useEffect(() => {
    const request = role === 'ADMIN' ? portalApi.admin.applications() : portalApi.applications();
    request.then((response) => setItems(response.data)).catch(() => setError('Applications could not be loaded.'));
  }, [role]);

  const changeStatus = async (id, status) => {
    setError('');
    setMessage('');
    try {
      await portalApi.updateApplicationStatus(id, status);
      setItems((current) => current.map((item) => item.id === id ? { ...item, status } : item));
      setMessage('Application status updated.');
    } catch (requestError) {
      setError(requestError.response?.data?.message || 'Application status could not be updated.');
    }
  };

  return <Page eyebrow="APPLICATIONS" title={role === 'STUDENT' ? 'Your application trail.' : 'Candidate pipeline.'} description="Status stays visible from first submission to final decision.">
    {error && <p className="form-error">{error}</p>}{message && <p className="success">{message}</p>}
    {!items.length ? <DataList items={items} empty="No applications to show." fields={['id']} /> : <div className="table-wrap"><table><thead><tr>{['id', 'studentId', 'opportunityType', 'opportunityId', 'status', 'updatedAt'].map((field) => <th key={field}>{field.replace(/[A-Z]/g, (character) => ` ${character}`).toUpperCase()}</th>)}{role === 'STUDENT' && <th>SHARED CERTIFICATES & PROJECTS</th>}{role === 'ADMIN' && <th>UPDATE</th>}</tr></thead><tbody>{items.map((item) => <tr key={item.id}><td>{item.id}</td><td>{item.studentId ?? '—'}</td><td>{item.opportunityType ?? '—'}</td><td>{item.opportunityId ?? '—'}</td><td>{item.status ?? '—'}</td><td>{item.updatedAt ?? '—'}</td>{role === 'STUDENT' && <td>{item.sharedPortfolioItems?.length ? <ul className="shared-evidence-list">{item.sharedPortfolioItems.map((portfolioItem) => <li key={portfolioItem.id}><strong>{portfolioItem.title}</strong>{portfolioItem.referenceUrl && <> · <a href={portfolioItem.referenceUrl} target="_blank" rel="noreferrer">Open URL</a></>}</li>)}</ul> : 'None shared'}<button type="button" className="text-link" onClick={() => setSharingApplicationId(sharingApplicationId === item.id ? null : item.id)}>{sharingApplicationId === item.id ? 'Close' : 'Choose or update items'}</button>{sharingApplicationId === item.id && <PortfolioSharingForm initialSelectedIds={item.sharedPortfolioItems?.map((portfolioItem) => portfolioItem.id) || []} submitLabel="Save shared items" onSubmit={async (selectedIds) => { const response = await portalApi.updateApplicationSharedPortfolio(item.id, selectedIds); setItems((current) => current.map((application) => application.id === item.id ? response.data : application)); setSharingApplicationId(null); setMessage('Shared certificates and project links updated. The company can now see the selected items under Applicants.'); }} onCancel={() => setSharingApplicationId(null)} />}</td>}{role === 'ADMIN' && <td><select aria-label={`Status for application ${item.id}`} value={item.status} onChange={(event) => changeStatus(item.id, event.target.value)}>{['APPLIED', 'SHORTLISTED', 'INTERVIEW', 'SELECTED', 'REJECTED', 'WITHDRAWN'].map((status) => <option key={status}>{status}</option>)}</select></td>}</tr>)}</tbody></table></div>}
  </Page>;
}
function Applicants() {
  const [opportunities, setOpportunities] = useState([]);
  const [selectedId, setSelectedId] = useState('');
  const [items, setItems] = useState([]);
  const [loading, setLoading] = useState(true);
  const [message, setMessage] = useState('');
  let userId = null;
  try { userId = JSON.parse(localStorage.getItem('portal_session') || '{}').user?.id; } catch { /* Ignore a corrupt local session. */ }

  useEffect(() => {
    Promise.all([portalApi.opportunities(), portalApi.internships(), portalApi.jobs()])
      .then(([opportunitiesResponse, internshipsResponse, jobsResponse]) => {
        const owned = [
          ...opportunitiesResponse.data,
          ...internshipsResponse.data.map((item) => ({ ...item, type: 'INTERNSHIP' })),
          ...jobsResponse.data.map((item) => ({ ...item, type: 'JOB' })),
        ].filter((opportunity) => String(opportunity.industryId) === String(userId));
        setOpportunities(owned);
        if (owned.length) setSelectedId(`${owned[0].type}:${owned[0].id}`);
      })
      .catch(() => setMessage('Your opportunities could not be loaded.'))
      .finally(() => setLoading(false));
  }, [userId]);

  const selected = opportunities.find((opportunity) => `${opportunity.type}:${opportunity.id}` === selectedId);
  useEffect(() => {
    if (!selected) {
      setItems([]);
      return;
    }
    let active = true;
    portalApi.industryApplications(selected.type, selected.id)
      .then((response) => { if (active) setItems(response.data); })
      .catch(() => { if (active) setMessage('Applicants could not be loaded for this opportunity.'); })
      .finally(() => { if (active) setLoading(false); });
    return () => { active = false; };
  }, [selected]);

  const changeStatus = async (applicationId, status) => {
    setMessage('');
    try {
      const current = items.find((item) => item.id === applicationId);
      await portalApi.updateApplicationStatus(applicationId, status);
      setItems((current) => current.map((item) => item.id === applicationId ? { ...item, status } : item));
      setMessage('Application status updated.');
      if (status === 'SELECTED' && current?.status !== 'SELECTED') {
        try {
          await portalApi.createPlacement({ applicationId, status: 'SELECTED', interviewDate: null, notes: '' });
          setMessage('Candidate selected and placement tracking started.');
        } catch (placementError) {
          setMessage(placementError.response?.data?.message || 'Candidate selected, but placement tracking could not be started.');
        }
      }
    } catch {
      setMessage('Application status could not be updated.');
    }
  };

  return <Page eyebrow="APPLICANTS" title="Review the candidate pipeline." description="Choose one of your opportunities to review its applications.">
    {opportunities.length > 0 && <Field label="Opportunity"><select value={selectedId} onChange={(event) => { setSelectedId(event.target.value); setLoading(true); setMessage(''); }}>
      {opportunities.map((opportunity) => <option key={`${opportunity.type}:${opportunity.id}`} value={`${opportunity.type}:${opportunity.id}`}>{opportunity.title} · {opportunity.type}</option>)}
    </select></Field>}
    {message && <p className={message.includes('could not') ? 'form-error' : 'success'}>{message}</p>}
    {loading ? <Loading /> : !opportunities.length ? <Empty text="You have not created any opportunities yet." /> : !items.length ? <Empty text="No applications for this opportunity yet." /> : <div className="table-wrap"><table><thead><tr><th>APPLICATION</th><th>CANDIDATE</th><th>INSTITUTION</th><th>DISCIPLINE</th><th>CGPA</th><th>SHARED CERTIFICATES & PROJECTS</th><th>STATUS</th><th>UPDATE</th></tr></thead><tbody>{items.map((item) => <tr key={item.id}><td>{item.id}</td><td><strong>{item.studentName || `Student ${item.studentId}`}</strong><br />{item.studentEmail}</td><td>{item.institutionName || '—'}</td><td>{item.branch || '—'} · {item.graduationYear || '—'}</td><td>{item.cgpa ?? '—'}</td><td>{item.sharedPortfolioItems?.length ? <ul className="shared-evidence-list">{item.sharedPortfolioItems.map((portfolioItem) => <li key={portfolioItem.id}><strong>{portfolioItem.title}</strong> <span>({portfolioItem.type === 'CERTIFICATION' ? 'Certificate' : 'Project'})</span>{portfolioItem.organization && <span> · {portfolioItem.organization}</span>}{portfolioItem.referenceUrl && <> · <a href={portfolioItem.referenceUrl} target="_blank" rel="noreferrer">Open link</a></>}</li>)}</ul> : 'Not shared'}</td><td>{item.status}</td><td><select aria-label={`Status for application ${item.id}`} value={item.status} onChange={(event) => changeStatus(item.id, event.target.value)}>{['APPLIED', 'SHORTLISTED', 'INTERVIEW', 'SELECTED', 'REJECTED', 'WITHDRAWN'].map((status) => <option key={status}>{status}</option>)}</select></td></tr>)}</tbody></table></div>}
  </Page>;
}
function Collaborations() {
  const sessionState = useSession().session;
  const user = sessionState?.user || {};
  const role = user.role || 'STUDENT';
  const canCreate = ['INDUSTRY', 'FACULTY', 'ACADEMICIAN', 'INSTITUTION', 'ADMIN'].includes(role);
  const [items, setItems] = useState([]);
  const [institutions, setInstitutions] = useState([]);
  const [industryUsers, setIndustryUsers] = useState([]);
  const [institutionStudents, setInstitutionStudents] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [message, setMessage] = useState('');
  const [form, setForm] = useState({ industryId: role === 'INDUSTRY' ? String(user.id || '') : '', institutionId: '', academicianId: ['FACULTY', 'ACADEMICIAN'].includes(role) ? String(user.id || '') : '', studentId: '', title: '', description: '' });

  const refresh = () => portalApi.collaborations()
    .then((response) => setItems(response.data))
    .catch(() => setError('Collaborations could not be loaded.'))
    .finally(() => setLoading(false));

  useEffect(() => {
    refresh();
    if (!canCreate) return;
    if (role !== 'INSTITUTION') portalApi.institutions().then((response) => setInstitutions(response.data))
      .catch(() => setError('Institutions could not be loaded.'));
    if (role === 'INSTITUTION') {
      portalApi.institutionProfile().then((response) => {
        setForm((current) => ({ ...current, institutionId: String(response.data.id) }));
      }).catch((requestError) => setError(requestError.response?.data?.message || 'Institution profile could not be loaded.'));
      portalApi.industryDirectory().then((response) => setIndustryUsers(response.data))
        .catch(() => setError('Industry partners could not be loaded.'));
      portalApi.students().then((response) => setInstitutionStudents(response.data))
        .catch(() => setError('Institution students could not be loaded.'));
    }
  }, [canCreate]);

  const submit = async (event) => {
    event.preventDefault();
    setError('');
    setMessage('');
    const payload = {
      ...form,
      industryId: Number(form.industryId),
      institutionId: Number(form.institutionId),
      academicianId: form.academicianId ? Number(form.academicianId) : null,
      studentId: form.studentId ? Number(form.studentId) : null,
      status: 'REQUESTED',
    };
    try {
      await portalApi.createCollaboration(payload);
      await refresh();
      setMessage('Collaboration request created.');
      setForm((current) => ({ ...current, title: '', description: '', studentId: '' }));
    } catch (requestError) {
      setError(requestError.response?.data?.message || 'Collaboration request could not be created.');
    }
  };

  const changeStatus = async (id, status) => {
    setError('');
    setMessage('');
    try {
      await portalApi.updateCollaborationStatus(id, status);
      setItems((current) => current.map((item) => item.id === id ? { ...item, status } : item));
      setMessage('Collaboration status updated.');
    } catch (requestError) {
      setError(requestError.response?.data?.message || 'Collaboration status could not be updated.');
    }
  };

  return <Page eyebrow="COLLABORATIONS" title="Keep the right people close." description="Requests and projects shared with your participant network.">
    {canCreate && <form className="card form-grid" onSubmit={submit}>
      <h2>New collaboration request</h2>
      {(role === 'ADMIN' || role === 'FACULTY' || role === 'ACADEMICIAN') && <Field label="Industry user ID"><input required type="number" min="1" value={form.industryId} onChange={update(setForm, 'industryId')} /></Field>}
      {role === 'INSTITUTION' && <Field label="Industry partner"><select required value={form.industryId} onChange={update(setForm, 'industryId')}><option value="">Choose an industry partner</option>{industryUsers.map((partner) => <option key={partner.id} value={partner.id}>{partner.companyName || partner.name} ({partner.email})</option>)}</select></Field>}
      {role === 'INSTITUTION' ? <Field label="Institution"><input readOnly value={user.name || 'Your institution'} /></Field> : <Field label="Institution"><select required value={form.institutionId} onChange={update(setForm, 'institutionId')}><option value="">Choose an institution</option>{institutions.map((institution) => <option key={institution.id} value={institution.id}>{institution.name}</option>)}</select></Field>}
      {role === 'INDUSTRY' && <Field label="Academic user ID (optional)"><input type="number" min="1" value={form.academicianId} onChange={update(setForm, 'academicianId')} /></Field>}
      {role === 'INDUSTRY' && <Field label="Student profile ID (optional)"><input type="number" min="1" value={form.studentId} onChange={update(setForm, 'studentId')} /></Field>}
      {role === 'INSTITUTION' && <Field label="Student participant (optional)"><select value={form.studentId} onChange={update(setForm, 'studentId')}><option value="">All institution students</option>{institutionStudents.map((student) => <option key={student.id} value={student.id}>{student.userName} · {student.branch}</option>)}</select></Field>}
      <Field label="Title"><input required maxLength="200" value={form.title} onChange={update(setForm, 'title')} /></Field>
      <Field label="Description"><textarea required maxLength="5000" rows="4" value={form.description} onChange={update(setForm, 'description')} /></Field>
      <div className="form-actions"><button className="button primary" disabled={role === 'INSTITUTION' ? !form.institutionId || !industryUsers.length : !institutions.length}>Send request</button></div>
    </form>}
    {error && <p className="form-error">{error}</p>}{message && <p className="success">{message}</p>}
    {loading ? <Loading /> : !items.length ? <DataList items={items} empty="No participant collaborations yet." fields={['id']} /> : <div className="table-wrap"><table><thead><tr><th>ID</th><th>TITLE</th><th>STATUS</th><th>INDUSTRY</th><th>ACADEMICIAN</th><th>STUDENT</th>{role !== 'STUDENT' && <th>UPDATE</th>}</tr></thead><tbody>{items.map((item) => <tr key={item.id}><td>{item.id}</td><td>{item.title}</td><td>{item.status}</td><td>{item.industryId}</td><td>{item.academicianId ?? '—'}</td><td>{item.studentId ?? '—'}</td>{role !== 'STUDENT' && <td><select aria-label={`Status for collaboration ${item.id}`} value={item.status} onChange={(event) => changeStatus(item.id, event.target.value)}>{['REQUESTED', 'ACCEPTED', 'REJECTED', 'COMPLETED'].map((status) => <option key={status}>{status}</option>)}</select></td>}</tr>)}</tbody></table></div>}
  </Page>;
}
function Placements() {
  const role = JSON.parse(localStorage.getItem('portal_session') || '{}').user?.role;
  const [items, setItems] = useState([]);
  const [error, setError] = useState('');
  const [message, setMessage] = useState('');
  useEffect(() => {
    (role === 'ADMIN' ? portalApi.admin.placements() : role === 'INDUSTRY' ? portalApi.industryPlacements() : portalApi.placements())
      .then((response) => setItems(response.data))
      .catch(() => setError('Placement records could not be loaded.'));
  }, [role]);

  const changeStatus = async (item, status) => {
    setError('');
    setMessage('');
    try {
      const payload = { applicationId: item.applicationId, status, interviewDate: item.interviewDate, notes: item.notes };
      const response = await (role === 'ADMIN'
        ? portalApi.admin.updatePlacement(item.id, payload)
        : portalApi.updatePlacement(item.id, payload));
      setItems((current) => current.map((placement) => placement.id === item.id ? response.data : placement));
      setMessage('Placement status updated.');
    } catch (requestError) {
      setError(requestError.response?.data?.message || 'Placement status could not be updated.');
    }
  };

  const nextStatuses = { SHORTLISTED: ['INTERVIEW', 'REJECTED'], INTERVIEW: ['SELECTED', 'REJECTED'], SELECTED: ['COMPLETED'], REJECTED: [], COMPLETED: [] };
  return <Page eyebrow="PLACEMENT" title="Track the outcome." description="Follow shortlist, interview, selection, rejection, and completion in one view.">
    {error && <p className="form-error">{error}</p>}{message && <p className="success">{message}</p>}
    {error && !items.length ? null : role !== 'STUDENT' && items.length ? <div className="table-wrap"><table><thead><tr><th>ID</th><th>APPLICATION</th><th>STUDENT</th><th>STATUS</th><th>INTERVIEW DATE</th><th>UPDATE</th></tr></thead><tbody>{items.map((item) => <tr key={item.id}><td>{item.id}</td><td>{item.applicationId}</td><td>{item.studentId}</td><td>{item.status}</td><td>{item.interviewDate ?? '—'}</td><td><select aria-label={`Next status for placement ${item.id}`} value={item.status} onChange={(event) => changeStatus(item, event.target.value)}>{[item.status, ...(nextStatuses[item.status] || [])].map((status) => <option key={status}>{status}</option>)}</select></td></tr>)}</tbody></table></div> : <DataList items={items} empty="No placement records yet." fields={['id', 'applicationId', 'status', 'interviewDate', 'updatedAt']} />}
  </Page>;
}
function AdminUsers() {
  const [searchParams] = useSearchParams();
  return searchParams.get('view') === 'institutions' ? <AdminInstitutions /> : <AdminUsersManagement />;
}
function AdminUsersManagement() {
  const emptyForm = { name: '', email: '', password: '', role: 'STUDENT' };
  const [items, setItems] = useState([]);
  const [form, setForm] = useState(emptyForm);
  const [editingId, setEditingId] = useState(null);
  const [error, setError] = useState('');
  const [message, setMessage] = useState('');

  const refresh = async () => {
    try {
      setItems((await portalApi.admin.users()).data);
      setError('');
    } catch {
      setError('Users could not be loaded.');
    }
  };
  useEffect(() => { refresh(); }, []);

  const submit = async (event) => {
    event.preventDefault();
    setError('');
    setMessage('');
    try {
      if (editingId) await portalApi.admin.updateUser(editingId, form);
      else await portalApi.admin.createUser(form);
      setForm(emptyForm);
      setEditingId(null);
      await refresh();
      setMessage(editingId ? 'User updated.' : 'User created.');
    } catch (requestError) {
      setError(requestError.response?.data?.message || 'User could not be saved.');
    }
  };

  const remove = async (id) => {
    setError('');
    setMessage('');
    try {
      await portalApi.admin.deleteUser(id);
      await refresh();
      setMessage('User deleted.');
    } catch (requestError) {
      setError(requestError.response?.data?.message || 'User could not be deleted.');
    }
  };

  return <Page eyebrow="ADMINISTRATION" title="People make the platform." description="Manage the accounts and roles that keep the network moving.">
    <form className="card form-grid" onSubmit={submit}>
      <Field label="Name"><input required maxLength="150" value={form.name} onChange={update(setForm, 'name')} /></Field>
      <Field label="Email"><input required type="email" maxLength="320" value={form.email} onChange={update(setForm, 'email')} /></Field>
      <Field label={editingId ? 'New password' : 'Password'}><input required type="password" minLength="8" maxLength="255" value={form.password} onChange={update(setForm, 'password')} /></Field>
      <Field label="Role"><select value={form.role} onChange={update(setForm, 'role')}>{['STUDENT', 'ACADEMICIAN', 'FACULTY', 'INDUSTRY', 'INSTITUTION', 'ADMIN'].map((role) => <option key={role}>{role}</option>)}</select></Field>
      {error && <p className="form-error">{error}</p>}{message && <p className="success">{message}</p>}
      <div className="form-actions"><button className="button primary">{editingId ? 'Save user' : 'Create user'}</button>{editingId && <button type="button" className="button" onClick={() => { setEditingId(null); setForm(emptyForm); }}>Cancel</button>}</div>
    </form>
    {items.length ? <div className="table-wrap"><table><thead><tr><th>ID</th><th>NAME</th><th>EMAIL</th><th>ROLE</th><th>ACTIONS</th></tr></thead><tbody>{items.map((item) => <tr key={item.id}><td>{item.id}</td><td>{item.name}</td><td>{item.email}</td><td>{item.role}</td><td><button type="button" className="text-link" onClick={() => { setEditingId(item.id); setForm({ name: item.name, email: item.email, password: '', role: item.role }); }}>Edit</button> <button type="button" className="text-link" onClick={() => remove(item.id)}>Delete</button></td></tr>)}</tbody></table></div> : <DataList items={items} fields={['id']} empty="No users found." />}
  </Page>;
}
function AdminInstitutions() {
  const [items, setItems] = useState([]);
  const [name, setName] = useState('');
  const [editingId, setEditingId] = useState(null);
  const [error, setError] = useState('');
  const [message, setMessage] = useState('');

  const refresh = async () => {
    try {
      setItems((await portalApi.admin.institutions()).data);
      setError('');
    } catch {
      setError('Institutions could not be loaded.');
    }
  };
  useEffect(() => { refresh(); }, []);

  const save = async (event) => {
    event.preventDefault();
    setError('');
    setMessage('');
    try {
      if (editingId) await portalApi.admin.updateInstitution(editingId, { name });
      else await portalApi.admin.createInstitution({ name });
      setName('');
      setEditingId(null);
      await refresh();
      setMessage('Institution saved.');
    } catch (requestError) {
      setError(requestError.response?.data?.message || 'Institution could not be saved.');
    }
  };

  const remove = async (id) => {
    setError('');
    setMessage('');
    try {
      await portalApi.admin.deleteInstitution(id);
      await refresh();
      setMessage('Institution deleted.');
    } catch (requestError) {
      setError(requestError.response?.data?.message || 'Institution could not be deleted.');
    }
  };

  return <Page eyebrow="ADMINISTRATION" title="Institution registry" description="Maintain the institutions available to student profiles and collaboration requests." actions={<Link className="text-link" to="/app/users">Back to users</Link>}>
    <form className="card form-grid" onSubmit={save}>
      <Field label="Institution name"><input required maxLength="200" value={name} onChange={(event) => setName(event.target.value)} /></Field>
      {error && <p className="form-error">{error}</p>}{message && <p className="success">{message}</p>}
      <div className="form-actions"><button className="button primary">{editingId ? 'Save institution' : 'Add institution'}</button>{editingId && <button type="button" className="button" onClick={() => { setEditingId(null); setName(''); }}>Cancel</button>}</div>
    </form>
    {items.length ? <div className="table-wrap"><table><thead><tr><th>ID</th><th>NAME</th><th>ACTIONS</th></tr></thead><tbody>{items.map((item) => <tr key={item.id}><td>{item.id}</td><td>{item.name}</td><td><button type="button" className="text-link" onClick={() => { setEditingId(item.id); setName(item.name); }}>Edit</button> <button type="button" className="text-link" onClick={() => remove(item.id)}>Delete</button></td></tr>)}</tbody></table></div> : <DataList items={items} fields={['id']} empty="No institutions have been added." />}
  </Page>;
}

function Students() {
  const [items, setItems] = useState([]);
  const [skills, setSkills] = useState([]);
  const [skillId, setSkillId] = useState('');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  useEffect(() => {
    setLoading(true);
    portalApi.students(skillId)
      .then((response) => setItems(response.data))
      .catch(() => setError('Student records could not be loaded.'))
      .finally(() => setLoading(false));
    portalApi.skills().then((response) => setSkills(response.data)).catch(() => setError('Skill catalog could not be loaded.'));
  }, [skillId]);
  return <Page eyebrow="ACADEMIC VIEW" title="Student readiness." description="Review student profiles and filter candidates by a demonstrated skill.">
    <div className="inline-form"><Field label="Filter by skill"><select value={skillId} onChange={(event) => setSkillId(event.target.value)}><option value="">All students</option>{skills.map((skill) => <option key={skill.id} value={skill.id}>{skill.name}</option>)}</select></Field></div>
    {error ? <p className="form-error">{error}</p> : loading ? <Loading /> : <DataList items={items} empty="No student profiles match this skill." fields={['userName', 'userEmail', 'institutionName', 'branch', 'graduationYear', 'cgpa', 'careerGoal', 'profileCompletionPercentage']} />}
  </Page>;
}
function PortfolioPage({ type }) {
  const title = type[0] + type.slice(1).toLowerCase() + (type === 'INTEREST' ? 's' : type === 'PROJECT' ? 's' : 's');
  const [items, setItems] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [message, setMessage] = useState('');
  const [form, setForm] = useState({ title: '', description: '', organization: '', referenceUrl: '', completedOn: '' });

  const refresh = async () => {
    try {
      const response = await portalApi.portfolioItems();
      setItems(response.data.filter((item) => item.type === type));
      setError('');
    } catch (requestError) {
      setError(requestError.response?.data?.message || `${title} could not be loaded.`);
    } finally {
      setLoading(false);
    }
  };
  useEffect(() => { refresh(); }, [type]);

  const submit = async (event) => {
    event.preventDefault();
    setError('');
    setMessage('');
    try {
      await portalApi.createPortfolioItem({ ...form, type, completedOn: form.completedOn || null });
      setForm({ title: '', description: '', organization: '', referenceUrl: '', completedOn: '' });
      await refresh();
      setMessage(`${title} entry added.`);
    } catch (requestError) {
      setError(requestError.response?.data?.message || `${title} entry could not be added.`);
    }
  };

  const remove = async (id) => {
    setError('');
    setMessage('');
    try {
      await portalApi.deletePortfolioItem(id);
      await refresh();
      setMessage(`${title} entry deleted.`);
    } catch (requestError) {
      setError(requestError.response?.data?.message || `${title} entry could not be deleted.`);
    }
  };

  return <Page eyebrow="STUDENT PORTFOLIO" title={title} description={`Keep your ${title.toLowerCase()} visible in your student portfolio.`}>
    <form className="card form-grid" onSubmit={submit}>
      <Field label="Title"><input required maxLength="200" value={form.title} onChange={update(setForm, 'title')} /></Field>
      <Field label="Organization"><input maxLength="200" value={form.organization} onChange={update(setForm, 'organization')} /></Field>
      <Field label="Completed on"><input type="date" value={form.completedOn} onChange={update(setForm, 'completedOn')} /></Field>
      <Field label="Reference URL"><input type="url" maxLength="1000" value={form.referenceUrl} onChange={update(setForm, 'referenceUrl')} /></Field>
      <Field label="Description"><textarea maxLength="2000" rows="4" value={form.description} onChange={update(setForm, 'description')} /></Field>
      {error && <p className="form-error">{error}</p>}{message && <p className="success">{message}</p>}
      <div className="form-actions"><button className="button primary">Add {type.toLowerCase()}</button></div>
    </form>
    {loading ? <Loading /> : items.length ? <div className="table-wrap"><table><thead><tr><th>TITLE</th><th>ORGANIZATION</th><th>COMPLETED</th><th>REFERENCE</th><th>ACTION</th></tr></thead><tbody>{items.map((item) => <tr key={item.id}><td>{item.title}</td><td>{item.organization || '—'}</td><td>{item.completedOn || '—'}</td><td>{item.referenceUrl ? <a href={item.referenceUrl} target="_blank" rel="noreferrer">Open</a> : '—'}</td><td><button type="button" className="text-link" onClick={() => remove(item.id)}>Delete</button></td></tr>)}</tbody></table></div> : <DataList items={items} fields={['id']} empty={`No ${title.toLowerCase()} entries yet.`} />}
  </Page>;
}
function AdminSkills() {
  const emptyForm = { name: '', category: '', description: '' };
  const [items, setItems] = useState([]);
  const [form, setForm] = useState(emptyForm);
  const [editingId, setEditingId] = useState(null);
  const [error, setError] = useState('');
  const [message, setMessage] = useState('');

  const refresh = async () => {
    try {
      setItems((await portalApi.admin.skills()).data);
      setError('');
    } catch {
      setError('Skill catalog could not be loaded.');
    }
  };
  useEffect(() => { refresh(); }, []);

  const submit = async (event) => {
    event.preventDefault();
    setError('');
    setMessage('');
    try {
      if (editingId) await portalApi.admin.updateSkill(editingId, form);
      else await portalApi.admin.createSkill(form);
      setForm(emptyForm);
      setEditingId(null);
      await refresh();
      setMessage(editingId ? 'Skill updated.' : 'Skill added.');
    } catch (requestError) {
      setError(requestError.response?.data?.message || 'Skill could not be saved.');
    }
  };

  const remove = async (id) => {
    setError('');
    setMessage('');
    try {
      await portalApi.admin.deleteSkill(id);
      await refresh();
      setMessage('Skill deleted.');
    } catch (requestError) {
      setError(requestError.response?.data?.message || 'Skill could not be deleted.');
    }
  };

  return <Page eyebrow="ADMINISTRATION" title="Skill catalog" description="Maintain the skills used across student profiles and assessments.">
    <form className="card form-grid" onSubmit={submit}>
      <Field label="Name"><input required maxLength="100" value={form.name} onChange={update(setForm, 'name')} /></Field>
      <Field label="Category"><input required maxLength="100" value={form.category} onChange={update(setForm, 'category')} /></Field>
      <Field label="Description"><textarea maxLength="1000" rows="3" value={form.description} onChange={update(setForm, 'description')} /></Field>
      {error && <p className="form-error">{error}</p>}{message && <p className="success">{message}</p>}
      <div className="form-actions"><button className="button primary">{editingId ? 'Save skill' : 'Add skill'}</button>{editingId && <button type="button" className="button" onClick={() => { setEditingId(null); setForm(emptyForm); }}>Cancel</button>}</div>
    </form>
    {items.length ? <div className="table-wrap"><table><thead><tr><th>NAME</th><th>CATEGORY</th><th>DESCRIPTION</th><th>ACTIONS</th></tr></thead><tbody>{items.map((item) => <tr key={item.id}><td>{item.name}</td><td>{item.category}</td><td>{item.description || '—'}</td><td><button className="text-link" onClick={() => { setEditingId(item.id); setForm({ name: item.name, category: item.category, description: item.description || '' }); }}>Edit</button> <button className="text-link" onClick={() => remove(item.id)}>Delete</button></td></tr>)}</tbody></table></div> : <DataList items={items} fields={['id']} empty="No skills in the catalog yet." />}
  </Page>;
}
function CareerListings({ kind }) {
  const { session } = useSession();
  const isStudent = session?.user?.role === 'STUDENT';
  const isInternship = kind === 'internship';
  const title = isInternship ? 'Internships' : 'Jobs';
  const list = isInternship ? portalApi.internships : portalApi.jobs;
  const create = isInternship ? portalApi.createInternship : portalApi.createJob;
  const [items, setItems] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [message, setMessage] = useState('');
  const [applicationMessage, setApplicationMessage] = useState('');
  const [studentId, setStudentId] = useState(null);
  const [appliedIds, setAppliedIds] = useState([]);
  const [selectedListingId, setSelectedListingId] = useState('');
  const [applicationTarget, setApplicationTarget] = useState(null);
  const [form, setForm] = useState({
    title: '', description: '', location: '', duration: '', stipend: '',
    employmentType: 'FULL_TIME', minimumCgpa: '', applicationDeadline: '', status: 'OPEN',
  });

  const refresh = async () => {
    try {
      const response = await list();
      const today = new Date().toISOString().slice(0, 10);
      setItems(isStudent
        ? response.data.filter((item) => item.status === 'OPEN' && item.applicationDeadline >= today)
        : response.data);
      setError('');
    } catch {
      setError(`${title} could not be loaded.`);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    refresh();
    if (isStudent) portalApi.studentProfile().then((response) => setStudentId(response.data.id))
      .catch(() => setApplicationMessage('Create your student profile before applying.'));
  }, [kind, isStudent]);

  const submit = async (event) => {
    event.preventDefault();
    setError('');
    setMessage('');
    const payload = {
      industryId: Number(session?.user?.id),
      title: form.title,
      description: form.description,
      location: form.location,
      applicationDeadline: form.applicationDeadline,
      status: form.status,
      ...(isInternship
        ? { duration: form.duration, stipend: form.stipend ? Number(form.stipend) : null }
        : { employmentType: form.employmentType, minimumCgpa: form.minimumCgpa ? Number(form.minimumCgpa) : null }),
    };
    try {
      const response = await create(payload);
      await refresh();
      setSelectedListingId(String(response.data.id));
      setForm((current) => ({ ...current, title: '', description: '', location: '', duration: '', stipend: '', minimumCgpa: '', applicationDeadline: '' }));
      setMessage(`${isInternship ? 'Internship' : 'Job'} created.`);
    } catch (requestError) {
      setError(requestError.response?.data?.message || `Could not create ${isInternship ? 'internship' : 'job'}.`);
    }
  };

  const fields = isInternship
    ? ['id', 'title', 'location', 'duration', 'stipend', 'applicationDeadline', 'status']
    : ['id', 'title', 'location', 'employmentType', 'minimumCgpa', 'applicationDeadline', 'status'];

  const apply = async (item, sharedPortfolioItemIds) => {
    setApplicationMessage('');
    try {
      await portalApi.apply({
        studentId,
        opportunityType: isInternship ? 'INTERNSHIP' : 'JOB',
        opportunityId: item.id,
        sharedPortfolioItemIds,
      });
      setAppliedIds((current) => [...current, item.id]);
      setApplicationTarget(null);
      setApplicationMessage(`Application submitted for ${item.title}.`);
    } catch (requestError) {
      setApplicationMessage(requestError.response?.data?.message || `Could not apply for ${item.title}.`);
      throw requestError;
    }
  };

  if (isStudent) return <Page eyebrow="CAREER OPPORTUNITIES" title={title} description={`Browse open ${title.toLowerCase()} and apply with your student profile.`}>
    {applicationMessage && <p className={applicationMessage.startsWith('Could not') ? 'form-error' : 'success'}>{applicationMessage}</p>}
    {loading ? <Loading /> : items.length ? <div className="opportunity-grid">{items.map((item) => <article className="opportunity-card" key={item.id}><div className="card-top"><span className="pill">{isInternship ? item.duration : item.employmentType}</span><span>Deadline {item.applicationDeadline}</span></div><h2>{item.title}</h2><p>{item.description}</p><p className="muted">{item.companyName || 'Industry partner'} · {item.location}</p><CareerSkillRequirements type={isInternship ? 'INTERNSHIP' : 'JOB'} listingId={item.id} />{applicationTarget?.id === item.id && <PortfolioSharingForm onSubmit={(selectedIds) => apply(item, selectedIds)} onCancel={() => setApplicationTarget(null)} />}<footer><span>{item.location}{item.stipend ? ` · ${item.stipend} stipend` : ''}</span><button className="button primary" disabled={!studentId || appliedIds.includes(item.id)} onClick={() => setApplicationTarget(item)}>{appliedIds.includes(item.id) ? 'Applied' : 'Apply'}</button></footer></article>)}</div> : <Empty text={`No open ${title.toLowerCase()} are available right now.`} />}
  </Page>;

  return <Page eyebrow="INDUSTRY LISTINGS" title={title} description={`Manage the ${title.toLowerCase()} available through your industry account.`}>
    <form className="card form-grid" onSubmit={submit}>
      <Field label="Title"><input required maxLength="200" value={form.title} onChange={update(setForm, 'title')} /></Field>
      <Field label="Location"><input required maxLength="200" value={form.location} onChange={update(setForm, 'location')} /></Field>
      {isInternship
        ? <>
          <Field label="Duration"><input required maxLength="100" value={form.duration} onChange={update(setForm, 'duration')} /></Field>
          <Field label="Stipend"><input type="number" min="0" step="0.01" value={form.stipend} onChange={update(setForm, 'stipend')} /></Field>
        </>
        : <>
          <Field label="Employment type"><select value={form.employmentType} onChange={update(setForm, 'employmentType')}>{['FULL_TIME', 'PART_TIME', 'CONTRACT', 'TEMPORARY'].map((type) => <option key={type}>{type}</option>)}</select></Field>
          <Field label="Minimum CGPA"><input type="number" min="0" max="10" step="0.01" value={form.minimumCgpa} onChange={update(setForm, 'minimumCgpa')} /></Field>
        </>}
      <Field label="Application deadline"><input required type="date" value={form.applicationDeadline} onChange={update(setForm, 'applicationDeadline')} /></Field>
      <Field label="Status"><select value={form.status} onChange={update(setForm, 'status')}>{['DRAFT', 'OPEN', 'CLOSED'].map((status) => <option key={status}>{status}</option>)}</select></Field>
      <Field label="Description"><textarea required maxLength="5000" rows="4" value={form.description} onChange={update(setForm, 'description')} /></Field>
      {error && <p className="form-error">{error}</p>}{message && <p className="success">{message}</p>}
      <div className="form-actions"><button className="button primary">Create {isInternship ? 'internship' : 'job'}</button></div>
    </form>
    <section className="section-block">
      <div className="section-title"><h2>Published listings</h2></div>
      {loading ? <Loading /> : error && !items.length ? <p className="form-error">{error}</p> : <DataList items={items} fields={fields} empty={`No ${title.toLowerCase()} yet.`} />}
      {items.length > 0 && <><Field label="Manage required skills for"><select value={selectedListingId || String(items[0].id)} onChange={(event) => setSelectedListingId(event.target.value)}>{items.map((item) => <option key={item.id} value={item.id}>{item.title}</option>)}</select></Field><CareerSkillRequirements type={isInternship ? 'INTERNSHIP' : 'JOB'} listingId={Number(selectedListingId || items[0].id)} canEdit /></>}
    </section>
  </Page>;
}

function CareerSkillRequirements({ type, listingId, canEdit = false }) {
  const [requirements, setRequirements] = useState([]);
  const [skills, setSkills] = useState([]);
  const [skillId, setSkillId] = useState('');
  const [requiredLevel, setRequiredLevel] = useState('BEGINNER');
  const [match, setMatch] = useState(null);
  const [error, setError] = useState('');

  const refresh = async () => {
    const response = await portalApi.careerListingSkills(type, listingId);
    setRequirements(response.data);
  };
  useEffect(() => {
    refresh().catch(() => setError('Required skills could not be loaded.'));
    setMatch(null);
    if (canEdit) portalApi.skills().then((response) => setSkills(response.data))
      .catch(() => setError('Skill catalog could not be loaded.'));
  }, [type, listingId, canEdit]);

  const add = async (event) => {
    event.preventDefault();
    setError('');
    try {
      await portalApi.addCareerListingSkill(type, listingId, { skillId: Number(skillId), requiredLevel });
      setSkillId('');
      await refresh();
    } catch (requestError) {
      setError(requestError.response?.data?.message || 'Required skill could not be added.');
    }
  };
  const update = async (requirement, level) => {
    try {
      await portalApi.updateCareerListingSkill(type, listingId, requirement.id, { requiredLevel: level });
      await refresh();
    } catch (requestError) {
      setError(requestError.response?.data?.message || 'Required skill could not be updated.');
    }
  };
  const remove = async (requirement) => {
    try {
      await portalApi.deleteCareerListingSkill(type, listingId, requirement.id);
      await refresh();
    } catch (requestError) {
      setError(requestError.response?.data?.message || 'Required skill could not be removed.');
    }
  };
  const checkMatch = async () => {
    setError('');
    try {
      setMatch((await portalApi.careerSkillGap(type, listingId)).data);
    } catch (requestError) {
      setError(requestError.response?.data?.message || 'Skill match could not be calculated.');
    }
  };

  return <div className="career-skills"><strong>Required skills</strong>{requirements.length ? <ul>{requirements.map((requirement) => <li key={requirement.id}><span>{requirement.skillName}</span>{canEdit ? <><select aria-label={`Minimum level for ${requirement.skillName}`} value={requirement.requiredLevel} onChange={(event) => update(requirement, event.target.value)}>{['BEGINNER', 'INTERMEDIATE', 'ADVANCED', 'EXPERT'].map((level) => <option key={level}>{level}</option>)}</select><button type="button" className="text-link" onClick={() => remove(requirement)}>Remove</button></> : <b>{requirement.requiredLevel}</b>}</li>)}</ul> : <p className="muted">No required skills listed.</p>}
    {canEdit && <form className="inline-form" onSubmit={add}><Field label="Skill"><select required value={skillId} onChange={(event) => setSkillId(event.target.value)}><option value="">Choose a skill</option>{skills.filter((skill) => !requirements.some((item) => item.skillId === skill.id)).map((skill) => <option key={skill.id} value={skill.id}>{skill.name}</option>)}</select></Field><Field label="Minimum level"><select value={requiredLevel} onChange={(event) => setRequiredLevel(event.target.value)}>{['BEGINNER', 'INTERMEDIATE', 'ADVANCED', 'EXPERT'].map((level) => <option key={level}>{level}</option>)}</select></Field><button className="button" disabled={!skillId}>Add skill</button></form>}
    {!canEdit && <button className="text-link" type="button" onClick={checkMatch}>Check skill match</button>}
    {match && <p className="success">{match.matchPercentage}% match · Missing: {match.missingSkills.join(', ') || 'none'} · Needs practice: {match.insufficientProficiencySkills.join(', ') || 'none'}</p>}{error && <p className="form-error">{error}</p>}
  </div>;
}
function RecommendationsPage() {
  const [items, setItems] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  useEffect(() => {
    Promise.all([portalApi.recommendations(), portalApi.opportunities(), portalApi.internships(), portalApi.jobs()])
      .then(([matches, opportunities, internships, jobs]) => {
        const byId = new Map([
          ...opportunities.data.map((item) => [`${item.type}:${item.id}`, item]),
          ...internships.data.map((item) => [`INTERNSHIP:${item.id}`, item]),
          ...jobs.data.map((item) => [`JOB:${item.id}`, item]),
        ]);
        setItems(matches.data.map((match) => ({
          match,
          listing: byId.get(`${match.opportunityType}:${match.opportunityId}`),
        })).filter((item) => item.listing));
      })
      .catch((requestError) => setError(requestError.response?.data?.message || 'Recommendations could not be loaded.'))
      .finally(() => setLoading(false));
  }, []);
  return <Page eyebrow="RECOMMENDATIONS" title="Opportunities matched to your skills." description="Open opportunities are ranked by how closely your recorded skills match their requirements.">
    {error ? <p className="form-error">{error}</p> : loading ? <Loading /> : items.length ? <div className="opportunity-grid">{items.map(({ match, listing }) => {
      const type = match.opportunityType;
      const destination = type === 'INTERNSHIP' ? '/app/internships' : type === 'JOB' ? '/app/jobs' : `/app/opportunities/${match.opportunityId}`;
      return <Link className="opportunity-card" key={`${type}:${match.opportunityId}`} to={destination}><div className="card-top"><span className="pill">{match.matchPercentage}% match</span><span>{type} · {match.matchedCount}/{match.requiredCount} skills</span></div><h2>{listing.title}</h2><p>{listing.description}</p><footer><span>{listing.companyName || 'Industry partner'} · {listing.location}</span><b>View listing →</b></footer></Link>;
    })}</div> : <Empty text="No open opportunities are available yet." />}
  </Page>;
}
function UnavailablePage({ title }) { if (title === 'Recommendations') return <RecommendationsPage />; return <Page eyebrow="BACKEND CONTRACT" title={title} description="This workspace is reserved for the capability when its backend contract is available."><div className="card"><Empty text={`The current Spring Boot API does not expose ${title.toLowerCase()} data yet. No mock records are shown.`} /></div></Page>; }
function DataList({ items, fields, empty }) { if (!items?.length) return <div className="card"><Empty text={empty} /></div>; return <div className="table-wrap"><table><thead><tr>{fields.map((field) => <th key={field}>{field.replace(/[A-Z]/g, (x) => ` ${x}`).toUpperCase()}</th>)}</tr></thead><tbody>{items.map((item, index) => <tr key={item.id || index}>{fields.map((field) => <td key={field}>{Array.isArray(item[field]) ? item[field].join(', ') : String(item[field] ?? '—')}</td>)}</tr>)}</tbody></table></div>; }
function Empty({ text }) { return <div className="empty"><span>○</span><p>{text}</p></div>; }
function Loading() { return <div className="loading">Loading workspace data…</div>; }
function ProtectedRoute({ children }) { return children; }

export default App;
