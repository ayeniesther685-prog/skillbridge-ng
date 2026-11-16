import { useEffect, useState } from 'react';
import { Link, useNavigate, useSearchParams } from 'react-router-dom';
import { authApi } from '../../api/auth';

export default function VerifyEmail() {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const token = searchParams.get('token');
  const [state, setState] = useState({ status: 'verifying', message: '' });

  useEffect(() => {
    if (!token) {
      setState({ status: 'error', message: 'Missing verification token.' });
      return;
    }
    authApi.verifyEmail(token)
      .then(() => {
        setState({ status: 'success', message: 'Your email is verified! Redirecting to login…' });
        setTimeout(() => navigate('/login'), 2500);
      })
      .catch((err) => {
        const msg = err?.response?.data?.message || 'Verification failed. The link may have expired.';
        setState({ status: 'error', message: msg });
      });
  }, [token, navigate]);

  return (
    <div className="min-h-screen flex items-center justify-center bg-slate-950 px-4">
      <div className="w-full max-w-md rounded-xl border border-slate-800 bg-slate-900 p-8 text-center">
        <h1 className="text-2xl font-semibold text-slate-100 mb-4">Email Verification</h1>

        {state.status === 'verifying' && (
          <div className="space-y-4">
            <div className="animate-spin h-8 w-8 mx-auto rounded-full border-2 border-indigo-500 border-t-transparent" />
            <p className="text-slate-400">Verifying your email…</p>
          </div>
        )}

        {state.status === 'success' && (
          <div className="space-y-4">
            <div className="text-emerald-400 text-5xl">✓</div>
            <p className="text-slate-200">{state.message}</p>
          </div>
        )}

        {state.status === 'error' && (
          <div className="space-y-4">
            <div className="text-rose-400 text-5xl">✗</div>
            <p className="text-slate-300">{state.message}</p>
            <div className="pt-2 space-x-3">
              <Link to="/login" className="text-sm text-indigo-400 hover:underline">Back to login</Link>
              <Link to="/resend-verification" className="text-sm text-indigo-400 hover:underline">Resend link</Link>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}
