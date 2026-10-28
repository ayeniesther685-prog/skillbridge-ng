import { Outlet, Route, Routes } from 'react-router-dom'
import Navbar from './components/Navbar.jsx'
import ProtectedRoute from './components/ProtectedRoute.jsx'
import PublicRoute from './components/PublicRoute.jsx'
import Home from './pages/Home.jsx'
import Login from './pages/Login.jsx'
import Register from './pages/Register.jsx'
import Dashboard from './pages/Dashboard.jsx'
import Profile from './pages/Profile.jsx'
import EditProfile from './pages/EditProfile.jsx'
import Skills from './pages/Skills.jsx'
import Education from './pages/Education.jsx'
import Experience from './pages/Experience.jsx'
import Certifications from './pages/Certifications.jsx'
import Portfolio from './pages/Portfolio.jsx'
import Jobs from './pages/Jobs.jsx'
import JobDetail from './pages/JobDetail.jsx'
import PostJob from './pages/PostJob.jsx'
import EditJob from './pages/EditJob.jsx'
import JobApplications from './pages/JobApplications.jsx'
import MyApplications from './pages/MyApplications.jsx'
import Contracts from './pages/Contracts.jsx'
import ContractDetail from './pages/ContractDetail.jsx'
import Messages from './pages/Messages.jsx'
import ConversationDetail from './pages/ConversationDetail.jsx'
import Notifications from './pages/Notifications.jsx'
import Settings from './pages/Settings.jsx'
import Freelancers from './pages/Freelancers.jsx'
import NotFound from './pages/NotFound.jsx'

function AppLayout() {
  return (
    <div className="min-h-screen">
      <Navbar />
      <main className="mx-auto max-w-7xl px-4 py-8 sm:px-6"><Outlet /></main>
    </div>
  )
}

export default function App() {
  return (
    <Routes>
      <Route element={<PublicRoute />}>
        <Route path="/login" element={<Login />} />
        <Route path="/register" element={<Register />} />
      </Route>

      <Route element={<AppLayout />}>
        <Route path="/" element={<Home />} />
        <Route path="/jobs" element={<Jobs />} />
        <Route path="/freelancers" element={<Freelancers />} />

        <Route element={<ProtectedRoute />}>
          <Route path="/dashboard" element={<Dashboard />} />
          <Route path="/profile" element={<Profile />} />
          <Route path="/profile/edit" element={<EditProfile />} />
          <Route path="/skills" element={<Skills />} />
          <Route path="/education" element={<Education />} />
          <Route path="/experience" element={<Experience />} />
          <Route path="/certifications" element={<Certifications />} />
          <Route path="/portfolio" element={<Portfolio />} />
          <Route path="/jobs/new" element={<PostJob />} />
          <Route path="/jobs/:id/edit" element={<EditJob />} />
          <Route path="/jobs/:id/applications" element={<JobApplications />} />
          <Route path="/applications" element={<MyApplications />} />
          <Route path="/contracts" element={<Contracts />} />
          <Route path="/contracts/:id" element={<ContractDetail />} />
          <Route path="/messages" element={<Messages />} />
          <Route path="/messages/:id" element={<ConversationDetail />} />
          <Route path="/notifications" element={<Notifications />} />
          <Route path="/settings" element={<Settings />} />
        </Route>

        {/* /jobs/:id is public; /jobs/new is matched first (static segments win) */}
        <Route path="/jobs/:id" element={<JobDetail />} />
        <Route path="*" element={<NotFound />} />
      </Route>
    </Routes>
  )
}
