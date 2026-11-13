import { Navigate, Route, Routes } from 'react-router-dom'
import ProtectedRoute from './ProtectedRoute'
import RoleGate from './RoleGate'
import AppLayout from '../components/layout/AppLayout'
import PublicLayout from '../components/layout/PublicLayout'
import { useAuth, dashboardFor, primaryRole } from '../context/AuthContext'
import Spinner from '../components/ui/Spinner'
import Landing from '../pages/public/Landing'
import Login from '../pages/public/Login'
import Register from '../pages/public/Register'
import Jobs from '../pages/public/Jobs'
import JobDetail from '../pages/public/JobDetail'
import Courses from '../pages/public/Courses'
import Freelancers from '../pages/public/Freelancers'
import FreelancerProfile from '../pages/public/FreelancerProfile'
import CertificateVerify from '../pages/public/CertificateVerify'
import StaticPage from '../pages/public/StaticPage'
import Dashboard from '../pages/dashboard/Dashboard'
import NotFound from '../pages/common/NotFound'
import AccessDenied from '../pages/common/AccessDenied'
import Settings from '../pages/common/Settings'
import ProfileLayout from '../pages/profile/ProfileLayout'
import ProfileView from '../pages/profile/ProfileView'
import ProfileEdit from '../pages/profile/ProfileEdit'
import ProfileSkills from '../pages/profile/ProfileSkills'
import ProfileCrud from '../pages/profile/ProfileCrud'
import PostJob from '../pages/jobs/PostJob'
import EditJob from '../pages/jobs/EditJob'
import JobApplicants from '../pages/jobs/JobApplicants'
import MyApplications from '../pages/jobs/MyApplications'
import ApplyJob from '../pages/jobs/ApplyJob'
import Contracts from '../pages/contracts/Contracts'
import ContractDetail from '../pages/contracts/ContractDetail'
import AcademyEnroll from '../pages/academy/AcademyEnroll'
import AcademyCourses from '../pages/academy/AcademyCourses'
import CourseDetail from '../pages/academy/CourseDetail'
import LessonPlayer from '../pages/academy/LessonPlayer'
import CourseProgress from '../pages/academy/CourseProgress'
import Assignment from '../pages/academy/Assignment'
import MySubmission from '../pages/academy/MySubmission'
import MyCertificates from '../pages/academy/MyCertificates'
import WalletLayout from '../pages/wallet/WalletLayout'
import WalletHome from '../pages/wallet/WalletHome'
import Transactions from '../pages/wallet/Transactions'
import Withdraw from '../pages/wallet/Withdraw'
import Withdrawals from '../pages/wallet/Withdrawals'
import Challenges from '../pages/challenges/Challenges'
import ChallengeDetail from '../pages/challenges/ChallengeDetail'
import MyChallengeSubmissions from '../pages/challenges/MyChallengeSubmissions'
import AiLayout from '../pages/ai/AiLayout'
import Tutor from '../pages/ai/Tutor'
import Quota from '../pages/ai/Quota'
import Conversations from '../pages/messages/Conversations'
import Conversation from '../pages/messages/Conversation'
import Notifications from '../pages/messages/Notifications'
import AdminCourses from '../pages/admin/AdminCourses'
import CreateCourse from '../pages/admin/CreateCourse'
import CreateLesson from '../pages/admin/CreateLesson'
import CreateAssignment from '../pages/admin/CreateAssignment'
import Submissions from '../pages/admin/Submissions'
import GradeSubmission from '../pages/admin/GradeSubmission'
import AiQuota from '../pages/admin/AiQuota'

function DashboardRedirect() {
  const { user, restoring } = useAuth()
  if (restoring) return <Spinner full />
  return <Navigate to={dashboardFor(primaryRole(user.roles))} replace />
}
const MEMBER = ['STUDENT', 'FREELANCER', 'CLIENT']
const WORKER = ['STUDENT', 'FREELANCER']

export default function AppRoutes() {
  return (
    <Routes>
      <Route element={<PublicLayout />}>
        <Route path="/" element={<Landing />} />
        <Route path="/jobs" element={<Jobs />} />
        <Route path="/jobs/:id" element={<JobDetail />} />
        <Route path="/courses" element={<Courses />} />
        <Route path="/courses/:id" element={<CourseDetail />} />
        <Route path="/freelancers" element={<Freelancers />} />
        <Route path="/freelancers/:userId" element={<FreelancerProfile />} />
        <Route path="/challenges" element={<Challenges />} />
        <Route path="/challenges/:id" element={<ChallengeDetail />} />
        <Route path="/certificates/verify" element={<CertificateVerify />} />
        <Route path="/certificates/verify/:num" element={<CertificateVerify />} />
        {['about', 'faq', 'pricing', 'contact', 'terms', 'privacy'].map((p) => <Route key={p} path={`/${p}`} element={<StaticPage page={p} />} />)}
        <Route path="/access-denied" element={<AccessDenied />} />
      </Route>
      <Route path="/login" element={<Login />} />
      <Route path="/register" element={<Register />} />

      <Route element={<ProtectedRoute />}>
        <Route path="/dashboard" element={<DashboardRedirect />} />
        <Route path="/admin/dashboard" element={<Navigate to="/dashboard/admin" replace />} />
        <Route element={<AppLayout />}>
          {['STUDENT', 'FREELANCER', 'CLIENT', 'ADMIN'].map((r) => <Route key={r} element={<RoleGate roles={[r]} />}><Route path={`/dashboard/${r.toLowerCase()}`} element={<Dashboard role={r} />} /></Route>)}

          <Route path="/settings" element={<Settings />} />
          <Route path="/messages" element={<Conversations />} />
          <Route path="/messages/:id" element={<Conversation />} />
          <Route path="/notifications" element={<Notifications />} />
          <Route path="/challenges/me/submissions" element={<MyChallengeSubmissions />} />

          <Route element={<RoleGate roles={MEMBER} />}>
            <Route path="/profile" element={<ProfileLayout />}>
              <Route index element={<ProfileView />} />
              <Route path="edit" element={<ProfileEdit />} />
              <Route path="skills" element={<ProfileSkills />} />
              {['education', 'experience', 'certifications', 'portfolio'].map((k) => <Route key={k} path={k} element={<ProfileCrud kind={k} />} />)}
            </Route>
            <Route path="/contracts" element={<Contracts />} />
            <Route path="/contracts/:id" element={<ContractDetail />} />
          </Route>
          <Route element={<RoleGate roles={['CLIENT']} />}>
            <Route path="/jobs/new" element={<PostJob />} />
            <Route path="/jobs/:id/edit" element={<EditJob />} />
            <Route path="/jobs/:id/applications" element={<JobApplicants />} />
          </Route>
          <Route element={<RoleGate roles={WORKER} />}>
            <Route path="/applications" element={<MyApplications />} />
            <Route path="/jobs/:id/apply" element={<ApplyJob />} />
            <Route path="/wallet" element={<WalletLayout />}>
              <Route index element={<WalletHome />} />
              <Route path="transactions" element={<Transactions />} />
              <Route path="withdraw" element={<Withdraw />} />
              <Route path="withdrawals" element={<Withdrawals />} />
            </Route>
            <Route path="/ai" element={<AiLayout />}>
              <Route index element={<Navigate to="tutor" replace />} />
              <Route path="tutor" element={<Tutor />} />
              <Route path="quota" element={<Quota />} />
            </Route>
          </Route>
          <Route element={<RoleGate roles={['STUDENT']} />}>
            <Route path="/academy/enroll" element={<AcademyEnroll />} />
            <Route path="/academy/courses" element={<AcademyCourses />} />
            <Route path="/academy/courses/:id" element={<CourseDetail />} />
            <Route path="/academy/courses/:id/lessons/:lessonId" element={<LessonPlayer />} />
            <Route path="/academy/courses/:id/progress" element={<CourseProgress />} />
            <Route path="/academy/assignments/:id" element={<Assignment />} />
            <Route path="/academy/assignments/:id/my-submission" element={<MySubmission />} />
            <Route path="/academy/certificates" element={<MyCertificates />} />
          </Route>
          <Route element={<RoleGate roles={['ADMIN']} />}>
            <Route path="/admin/courses" element={<AdminCourses />} />
            <Route path="/admin/courses/new" element={<CreateCourse />} />
            <Route path="/admin/courses/:id/lessons/new" element={<CreateLesson />} />
            <Route path="/admin/courses/:id/assignments/new" element={<CreateAssignment />} />
            <Route path="/admin/assignments/:id/submissions" element={<Submissions />} />
            <Route path="/admin/submissions/:id/grade" element={<GradeSubmission />} />
            <Route path="/admin/ai-quota" element={<AiQuota />} />
          </Route>
        </Route>
      </Route>
      <Route path="*" element={<NotFound />} />
    </Routes>
  )
}
