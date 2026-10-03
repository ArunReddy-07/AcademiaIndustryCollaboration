import axios from 'axios';

const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  headers: { 'Content-Type': 'application/json' },
});

api.interceptors.request.use((config) => {
  const session = localStorage.getItem('portal_session');
  if (session) {
    try {
      const { accessToken } = JSON.parse(session);
      if (accessToken) config.headers.Authorization = `Bearer ${accessToken}`;
    } catch {
      localStorage.removeItem('portal_session');
      window.dispatchEvent(new Event('portal:logout'));
    }
  }
  return config;
});

api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('portal_session');
      window.dispatchEvent(new Event('portal:logout'));
    }
    return Promise.reject(error);
  },
);

export const authApi = {
  login: (payload) => api.post('/auth/login', payload),
  register: (payload) => api.post('/auth/register', payload),
};

export const portalApi = {
  profile: () => api.get('/users/me'),
  industryDirectory: () => api.get('/users/industry-directory'),
  updateProfile: (payload) => api.put('/users/me', payload),
  updateFacultyInstitution: (payload) => api.put('/users/me/faculty-institution', payload),
  facultyStudents: () => api.get('/faculty/students'),
  studentProfile: () => api.get('/students/me'),
  students: (skillId) => api.get('/students', { params: skillId ? { skillId } : {} }),
  createStudentProfile: (payload) => api.post('/students', payload),
  updateStudentProfile: (id, payload) => api.put(`/students/${id}`, payload),
  portfolioItems: () => api.get('/students/me/portfolio-items'),
  createPortfolioItem: (payload) => api.post('/students/me/portfolio-items', payload),
  deletePortfolioItem: (id) => api.delete(`/students/me/portfolio-items/${id}`),
  institutions: () => api.get('/institutions').catch(() => ({ data: [] })),
  resolveInstitution: (payload) => api.post('/institutions/resolve', payload),
  institutionProfile: () => api.get('/institutions/me'),
  updateInstitutionProfile: (payload) => api.put('/institutions/me', payload),
  skills: () => api.get('/skills'),
  createSkill: (payload) => api.post('/skills', payload),
  createStudentCatalogSkill: (name) => api.post('/skills/student', { name }),
  allStudentSkills: () => api.get('/student-skills'),
  studentSkills: () => api.get('/student-skills/me'),
  createStudentSkill: (payload) => api.post('/student-skills', payload),
  updateStudentSkill: (id, payload) => api.put(`/student-skills/${id}`, payload),
  deleteStudentSkill: (id) => api.delete(`/student-skills/${id}`),
  opportunities: () => api.get('/opportunities'),
  opportunity: (id) => api.get(`/opportunities/${id}`),
  applications: () => api.get('/applications/me'),
  apply: (payload) => api.post('/applications', payload),
  updateApplicationSharedPortfolio: (id, sharedPortfolioItemIds) => api.put(
    `/applications/${id}/shared-portfolio-items`,
    { sharedPortfolioItemIds },
  ),
  assessments: () => api.get('/assessments/me'),
  submitAssessment: (payload) => api.post('/assessments', payload),
  skillGap: (opportunityId) => api.get(`/skill-gaps/${opportunityId}`),
  careerSkillGap: (type, listingId) => api.get(`/skill-gaps/${type}/${listingId}`),
  recommendations: () => api.get('/recommendations'),
  collaborations: () => api.get('/collaborations'),
  placements: () => api.get('/placements/me'),
  industryPlacements: () => api.get('/placements/industry'),
  createPlacement: (payload) => api.post('/placements', payload),
  updatePlacement: (id, payload) => api.put(`/placements/${id}`, payload),
  internships: () => api.get('/internships'),
  jobs: () => api.get('/jobs'),
  createInternship: (payload) => api.post('/internships', payload),
  createJob: (payload) => api.post('/jobs', payload),
  createOpportunity: (payload) => api.post('/opportunities', payload),
  updateOpportunity: (id, payload) => api.put(`/opportunities/${id}`, payload),
  opportunitySkills: (id) => api.get(`/opportunities/${id}/skills`),
  addOpportunitySkill: (id, payload) => api.post(`/opportunities/${id}/skills`, payload),
  careerListingSkills: (type, listingId) => api.get(`/career-listings/${type}/${listingId}/skills`),
  addCareerListingSkill: (type, listingId, payload) => api.post(`/career-listings/${type}/${listingId}/skills`, payload),
  updateCareerListingSkill: (type, listingId, requirementId, payload) => api.put(`/career-listings/${type}/${listingId}/skills/${requirementId}`, payload),
  deleteCareerListingSkill: (type, listingId, requirementId) => api.delete(`/career-listings/${type}/${listingId}/skills/${requirementId}`),
  industryApplications: (type, opportunityId) => api.get(`/applications/opportunity/${type}/${opportunityId}`),
  updateApplicationStatus: (id, status) => api.patch(`/applications/${id}/status`, { status }),
  createCollaboration: (payload) => api.post('/collaborations', payload),
  updateCollaborationStatus: (id, status) => api.patch(`/collaborations/${id}/status`, { status }),
  admin: {
    users: () => api.get('/admin/users'),
    createUser: (payload) => api.post('/admin/users', payload),
    updateUser: (id, payload) => api.put(`/admin/users/${id}`, payload),
    deleteUser: (id) => api.delete(`/admin/users/${id}`),
    institutions: () => api.get('/admin/institutions').catch(() => ({ data: [] })),
    createInstitution: (payload) => api.post('/admin/institutions', payload),
    updateInstitution: (id, payload) => api.put(`/admin/institutions/${id}`, payload),
    deleteInstitution: (id) => api.delete(`/admin/institutions/${id}`),
    skills: () => api.get('/admin/skills'),
    createSkill: (payload) => api.post('/admin/skills', payload),
    updateSkill: (id, payload) => api.put(`/admin/skills/${id}`, payload),
    deleteSkill: (id) => api.delete(`/admin/skills/${id}`),
    opportunities: () => api.get('/admin/opportunities'),
    applications: () => api.get('/admin/applications'),
    collaborations: () => api.get('/admin/collaborations'),
    placements: () => api.get('/admin/placements'),
    updatePlacement: (id, payload) => api.put(`/admin/placements/${id}`, payload),
  },
};

export default api;
