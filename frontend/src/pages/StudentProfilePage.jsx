import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { portalApi } from '../services/api';

const emptyProfile = {
  institutionId: '',
  branch: '',
  graduationYear: '',
  cgpa: '',
  careerGoal: '',
  bio: '',
};

function getSessionUserId() {
  try {
    return JSON.parse(localStorage.getItem('portal_session') || '{}').user?.id;
  } catch {
    return null;
  }
}

export default function StudentProfilePage() {
  const [profile, setProfile] = useState(null);
  const [form, setForm] = useState(emptyProfile);
  const [institutions, setInstitutions] = useState([]);
  const [manualInstitutionName, setManualInstitutionName] = useState('');
  const [showManualInstitution, setShowManualInstitution] = useState(false);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');

  useEffect(() => {
    Promise.all([
      portalApi.institutions().catch(() => ({ data: [] })),
      portalApi.studentProfile().catch((requestError) => {
        if (requestError.response?.status === 404) {
          return null;
        }
        throw requestError;
      }),
    ])
      .then(([institutionResponse, studentResponse]) => {
        const institutionList = institutionResponse?.data ?? [];
        setInstitutions(institutionList);

        if (!studentResponse) {
          setLoading(false);
          return;
        }

        const selectedInstitutionId = studentResponse.data.institutionId ? String(studentResponse.data.institutionId) : '';
        const institutionIsListed = studentResponse.data.institutionId
          ? institutionList.some((institution) => String(institution.id) === String(studentResponse.data.institutionId))
          : false;

        setProfile(studentResponse.data);
        setForm({
          institutionId: selectedInstitutionId,
          branch: studentResponse.data.branch ?? '',
          graduationYear: String(studentResponse.data.graduationYear ?? ''),
          cgpa: String(studentResponse.data.cgpa ?? ''),
          careerGoal: studentResponse.data.careerGoal ?? '',
          bio: studentResponse.data.bio ?? '',
        });
        setShowManualInstitution(!institutionIsListed && Boolean(studentResponse.data.institutionName));
        setManualInstitutionName(institutionIsListed ? '' : studentResponse.data.institutionName ?? '');
      })
      .catch(() => {
        setError('Student profile could not be loaded. Please try again.');
      })
      .finally(() => setLoading(false));
  }, []);

  const update = (event) => {
    const { name, value } = event.target;
    setForm((current) => ({ ...current, [name]: value }));
  };

  const submit = async (event) => {
    event.preventDefault();
    setSaving(true);
    setError('');
    setMessage('');

    try {
      let institutionId = Number(form.institutionId);
      if ((!institutionId || Number.isNaN(institutionId)) && showManualInstitution && manualInstitutionName.trim()) {
        const customInstitution = await portalApi.resolveInstitution({ name: manualInstitutionName.trim() });
        institutionId = customInstitution.data.id;
      }

      if (!institutionId || Number.isNaN(institutionId)) {
        setError('Please select an institution or enter your institution name.');
        return;
      }

      const payload = {
        ...form,
        userId: Number(getSessionUserId()),
        institutionId,
        graduationYear: Number(form.graduationYear),
        cgpa: Number(form.cgpa),
      };

      const response = profile
        ? await portalApi.updateStudentProfile(profile.id, payload)
        : await portalApi.createStudentProfile(payload);
      setProfile(response.data);
      setForm((current) => ({ ...current, institutionId: String(response.data.institutionId ?? '') }));
      setManualInstitutionName(response.data.institutionName ?? '');
      setShowManualInstitution(false);
      setMessage('Student profile saved.');
    } catch (requestError) {
      setError(requestError.response?.data?.message || 'Student profile could not be saved.');
    } finally {
      setSaving(false);
    }
  };

  if (loading) return <section className="page"><p className="loading">Loading student profile…</p></section>;

  return (
    <section className="page">
      <div className="page-heading">
        <div>
          <p className="eyebrow">STUDENT PROFILE</p>
          <h1>Build your academic profile.</h1>
          <p className="lead compact">Add the details needed to apply and map your skills.</p>
        </div>
      </div>
      <form className="card form-grid" onSubmit={submit}>
        <label className="field">
          <span>Institution</span>
          <select name="institutionId" value={form.institutionId} onChange={update}>
            <option value="">Choose an institution</option>
            {institutions.map((institution) => (
              <option key={institution.id} value={institution.id}>{institution.name}</option>
            ))}
          </select>
        </label>

        <div className="field">
          <span>Can’t find your institution?</span>
          <button type="button" className="text-link" onClick={() => setShowManualInstitution((current) => !current)}>
            {showManualInstitution ? 'Hide manual entry' : 'Add institution manually'}
          </button>
        </div>

        {showManualInstitution && (
          <label className="field">
            <span>University or college name</span>
            <input
              type="text"
              value={manualInstitutionName}
              onChange={(event) => setManualInstitutionName(event.target.value)}
              placeholder="Enter your institution name"
            />
          </label>
        )}

        <label className="field">
          <span>Branch or discipline</span>
          <input name="branch" required maxLength="100" value={form.branch} onChange={update} />
        </label>
        <label className="field">
          <span>Graduation year</span>
          <input name="graduationYear" required type="number" min="1900" max="2100" value={form.graduationYear} onChange={update} />
        </label>
        <label className="field">
          <span>CGPA</span>
          <input name="cgpa" required type="number" min="0" max="10" step="0.01" value={form.cgpa} onChange={update} />
        </label>
        <label className="field">
          <span>Career goal</span>
          <input name="careerGoal" maxLength="500" value={form.careerGoal} onChange={update} />
        </label>
        <label className="field">
          <span>About you</span>
          <textarea name="bio" rows="4" maxLength="2000" value={form.bio} onChange={update} />
        </label>
        {institutions.length === 0 && !showManualInstitution && <p className="form-error">No institutions are available yet. Ask an administrator or faculty member to add one.</p>}
        {error && <p className="form-error">{error}</p>}
        {message && <p className="success">{message}</p>}
        <div className="form-actions">
          <button className="button primary" disabled={saving}>{saving ? 'Saving…' : profile ? 'Save profile' : 'Create profile'}</button>
          {profile && <Link className="text-link" to="/app/skills">Continue to skills →</Link>}
        </div>
      </form>
    </section>
  );
}
