# SkillBridge NG — Frontend

React 18 + Vite 5 + JavaScript, Tailwind 3, React Router 6, Axios (one client), TanStack Query 5, React Hook Form + Zod, Framer Motion, Lucide. No mock data; unavailable features render `ComingSoon`.

## Prerequisites
Node 20+, npm 10+, the Spring Boot backend running on http://localhost:8081 with CORS allowing http://localhost:5173.

## Setup
```
cp .env.example .env     # VITE_API_BASE_URL=http://localhost:8081
npm install
npm run dev              # http://localhost:5173
npm run build
```
`.env` is git-ignored; only `.env.example` is committed.

## Demo accounts
- ada@skillbridge.ng / Password123 (STUDENT)
- admin@skillbridge.ng / Password123 (ADMIN)

## Feature map (page → endpoints)
| Area | Routes | Endpoints |
|---|---|---|
| Auth | /login /register | POST /api/auth/login, /register, /refresh, /logout; GET /api/auth/me |
| Public | /, /jobs, /jobs/:id | GET /api/jobs, /api/jobs/{id} |
| Public | /courses, /courses/:id | GET /api/academy/programs, departments, courses, courses/{id}/lessons, assignments |
| Public | /freelancers, /freelancers/:userId | GET /api/users/{id}/profile, skills, education, experience, certifications, portfolio |
| Public | /certificates/verify[/:num] | GET /api/certificates/verify/{n} |
| Profile | /profile, /profile/edit, skills, education, experience, certifications, portfolio | GET/PUT /api/users/me/profile; CRUD /api/users/me/{kind}, /api/{kind}/{id}; POST/DELETE /api/users/me/skills/{id}; GET /api/skills |
| Jobs | /jobs/new, /jobs/:id/edit, /jobs/:id/applications | POST/PUT/DELETE /api/jobs, PUT /api/jobs/{id}/status, GET /api/jobs/{id}/applications, PUT /api/applications/{id}/status |
| Applications | /applications, /jobs/:id/apply | GET /api/applications, POST /api/jobs/{id}/applications, PUT /api/applications/{id}/withdraw |
| Contracts | /contracts, /contracts/:id | /api/contracts, /api/contracts/{id}/milestones, /api/milestones/{id}[/status], POST /api/reviews |
| Academy | /academy/* | enroll, me/record, me/enrollments, lessons/{id}/progress, me/courses/{id}/progress, assignments submit & my-submission, courses/{id}/certificate, me/certificates |
| Wallet | /wallet, /transactions, /withdraw, /withdrawals | /api/wallet/me, /me/transactions, /withdraw, /me/withdrawals |
| Challenges | /challenges, /challenges/:id, /challenges/me/submissions | /api/challenges, submit, me/submissions |
| AI | /ai/tutor, /ai/quota | /api/ai/tutor, quota, usage, tiers |
| Messaging | /messages, /messages/:id, /notifications | /api/conversations, messages, /api/messages/{id}/read, /api/notifications* |
| Admin | /admin/courses[/new], lessons/new, assignments/new, assignments/:id/submissions, submissions/:id/grade, ai-quota | /api/admin/** |
| Account | /settings | GET /api/auth/me |

## Known limitations (Coming in v2)
Payments/Paystack; real bank payouts (withdrawals are recorded only); instructor and training-provider roles; AI CV builder/analyzer; job eligibility scoring; external job listings; attendance; timetable; file uploads (links only); admin statistics, reports, user management; editing/deleting courses and lessons; forgot/reset password, email verification; subscriptions.
Also missing endpoints that shape the UI: no user search (so /freelancers opens a profile by ID), no "my jobs" list, no semester list (course enrollment asks for a semester ID), no get-course-by-id (course titles are passed through navigation state).

## Architecture notes
- **Auth:** tokens in localStorage. On load, `/api/auth/me`; a 401 triggers one shared refresh (never retries `/auth/refresh`), then retries the request once; failure clears storage and redirects to `/login?next=`. A spinner covers restore. Logout fires `/auth/logout` and clears state.
- **Errors:** `normalizeError` → `{status,message,fieldErrors}`. `AutoForm` maps `fieldErrors` onto inputs; mutations toast other errors. 403 → `/access-denied` (opt out per request with `skipForbiddenRedirect`). 429 on the tutor shows the reset time from `fieldErrors`.
- **Roles:** `RoleGate` per route group; sidebar shows the union of the user's roles; topbar switcher moves between dashboards.
- **Pagination:** always reads `data.content` via `usePaged`.

## DTO assumptions — verify against the backend
Section 3 lists endpoints but not every field name. These are guesses, centralised so they are easy to fix:
- Job: `title, description, location, jobType, experienceLevel, budgetMin, budgetMax, deadline, skillIds[]`.
- Application create: `coverLetter, proposedAmount`. Contract create: `applicationId, totalAmount?, terms?`.
- Milestone: `title, description, amount, dueDate`. Review: `jobId, revieweeId, rating, comment`.
- Profile: `headline, bio, location, hourlyRate, profilePicture, githubUrl, linkedinUrl, websiteUrl`. Education: `institution, degree, fieldOfStudy, startDate, endDate, description`. Experience: `title, company, location, startDate, endDate, description`. Certification: `name, issuingOrganization, issueDate, expiryDate, credentialUrl`. Portfolio: `title, description, projectUrl, imageUrl`.
- Admin: course `departmentId, code, title, description, creditUnits`; lesson `title, content, videoUrl, orderIndex`; assignment `title, description, dueDate, maxScore`.
- Wallet: `balancePoints` (or `points`/`balance`), optional `balanceNaira`.
- Client-only buttons (Edit job, View applicants) show for any CLIENT; the backend enforces ownership.
If a form field is rejected, the backend's `fieldErrors` message appears under it, which points to the right name.

## API contract reference
See the original spec, Section 3; the endpoint wrappers live in `src/api/`.
