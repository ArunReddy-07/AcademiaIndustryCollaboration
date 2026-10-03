# Academia Industry Collaboration Portal

Initial project structure for SIH 2026 problem statement **SIH26044**: Portal for Academia-Industry Collaboration for Skill Mapping, Internships and Placement.

## Modules

- `backend/`: Java 21 Spring Boot REST API with layered architecture, JPA/Hibernate, Spring Security, JWT, and PostgreSQL support.
- `frontend/`: React JavaScript client using Axios and React Router.
- `database/`: PostgreSQL schema, migration, and seed script locations.
- `docs/`: Requirements, architecture, database, and API documentation.

## Skill matching workflow

Companies can add required skills to an opportunity, either by selecting a catalog skill or entering a new one, and set a minimum proficiency for each. The opportunity ID is shown after creation and on its details page. Students can add catalog skills or enter a new skill on their profile, then record their proficiency. In **Skill Gaps**, a student enters the opportunity ID to see the percentage of requirements met, skills to add, and skills to improve. This comparison is guidance for students; companies make hiring decisions.

When applying, students can optionally choose individual certificates and projects from their portfolio to share with that application. Students can also review or change the shared items later from **Applications**. Companies see only the items selected for applications to their opportunities; other portfolio items remain private.

## Faculty workflow

Faculty accounts are created or assigned by an administrator rather than through public registration. On first sign-in, a faculty member enters their college or university name; the portal reuses a matching institution or adds it to the institution directory. The **Students & skills** view is then limited to student profiles linked to that same institution and shows each student's recorded skills and application statuses.

## Planned backend package

The backend Java source will use `com.academiaindustry` with `config`, `controller`, `dto`, `entity`, `exception`, `repository`, `security`, `service`, `service.impl`, and `util` layers.

Implementation classes and business logic will be added incrementally.

## Local development

The backend expects PostgreSQL and the `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, and `JWT_SECRET` environment variables. The JWT secret must contain at least 32 characters. From `backend/`, run `./run-local.ps1` in PowerShell to enter your existing local database name and username and provide the database password through a masked prompt; the script generates a development JWT secret for that run. It uses the `dev` profile to load reference data from `backend/src/main/resources/data-dev.sql` and does not create or modify database credentials or persist secrets. Set `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, or `JWT_SECRET` in the environment beforehand to override the corresponding local prompt/default.

The development profile can create the first Admin account only when `BOOTSTRAP_ADMIN_ENABLED=true`, `ADMIN_EMAIL` is set, and `ADMIN_PASSWORD` is set to a value of at least 12 characters. This bootstrap is disabled by default, only available in the `dev` profile, creates no default credentials, and does not change an existing account. Supply credentials through your local environment; do not commit them to project files.

The Vite dev server uses the backend at `http://localhost:8081` by default; configure another target in `frontend/vite.config.js` only if your backend runs on a different port. This avoids the local Oracle listener on port 8080.

## Deployment-ready setup

This project includes a containerized setup for local evaluation and as a starting point for deployment.

1. Copy `.env.example` to `.env`. Replace every `replace_with_*` value with unique credentials and a randomly generated JWT signing secret of at least 32 characters. Set `ALLOWED_ORIGINS` to the exact public frontend origin(s), including scheme and port where applicable. Never commit `.env`.
2. Build and start the app stack:

   `docker compose up --build`

3. Open the frontend on `http://localhost` (or the configured `FRONTEND_PORT`). API requests are routed through the frontend's `/api` path.

4. The backend health endpoint is available locally at `http://localhost:8081/actuator/health`.

The Compose stack:

- Keeps PostgreSQL internal to the Compose network.
- Binds the backend health/API port to `127.0.0.1:8081` for local diagnostics.
- Publishes the React frontend served by Nginx on port `80` by default.

For internet-facing production use, terminate TLS at a managed ingress/reverse proxy, configure its trusted forwarded headers and exact allowed origins, and use managed secret storage and database backups. The Docker profile currently uses Hibernate `ddl-auto=update`; introduce and validate versioned database migrations before storing production data. Review student-directory visibility and obtain the required privacy/consent policy before loading real student records.

### Render deployment

For the current Render services, the frontend production build uses `https://academiaindustrycollaboration-1.onrender.com/api` as its API base. In the backend Render service, set `ALLOWED_ORIGINS` to `https://academiaindustryfrontend.onrender.com` (origin only, with no path or trailing slash). If a `VITE_API_BASE_URL` variable is set on the Render Static Site, set it to the backend API base above; Vite embeds it at build time, so redeploy the frontend after changing it. Redeploy the backend after changing its environment variables.
