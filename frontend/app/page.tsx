"use client"

import { useRouter } from "next/navigation"
import { useState } from "react"
import { apiClient } from "@/lib/api-client"

export default function LoginPage() {
  const router = useRouter()
  const [role, setRole] = useState<"passenger" | "driver" | "owner">("passenger")
  const [isSignup, setIsSignup] = useState(false)
  const [email, setEmail] = useState("")
  const [password, setPassword] = useState("")
  const [fullName, setFullName] = useState("")
  const [loading, setLoading] = useState(false)

  // Debug State
  const [logs, setLogs] = useState<string[]>([])
  const addLog = (msg: string) => setLogs(prev => [...prev, `${new Date().toLocaleTimeString()}: ${msg}`])

  const handleSignup = async (e: React.FormEvent) => {
    e.preventDefault()
    setLoading(true)
    addLog('🚀 Signup Started...')

    try {
      addLog(`Sign up request for: ${email}, role: ${role}`)
      // Call Java Backend
      const data = await apiClient.signup({
        email,
        password,
        fullName,
        role
      })

      addLog('✅ Signup success! Please login.')
      alert('✅ Account created directly in SUTO-Java! You can now login.')
      setIsSignup(false)
      // Clear form
      setEmail('')
      setPassword('')
      setFullName('')

    } catch (error: any) {
      addLog(`❌ Signup error: ${error.message}`)
      console.error('Signup error:', error)
      alert('Error: ' + error.message)
    } finally {
      setLoading(false)
      addLog('Signup process finished.')
    }
  }

  const handleLogin = async (e: React.FormEvent) => {
    e.preventDefault()
    setLoading(true)
    setLogs([])
    addLog(`🚀 Login Process Started for: ${email}`)

    try {
      // Authenticate with Java Backend
      addLog('Step 1: Authenticating with SUTO-Java Backend...')
      const response = await apiClient.login({ email, password })

      addLog(`✅ Auth Successful. Token received.`)

      // Store Token & Role
      localStorage.setItem('token', response.token)
      localStorage.setItem('userRole', response.role)

      const userRole = response.role
      addLog(`📋 Role: ${userRole}`)

      // Verify Role Hierarchy - FIXED LOGIC
      addLog('Step 2: Verifying Role Access...')

      // Owner can access everything, driver can be driver/passenger, passenger only passenger
      const allowed =
        userRole === 'owner' || // Owner has full access
        (userRole === 'driver' && (role === 'driver' || role === 'passenger')) ||
        (userRole === 'passenger' && role === 'passenger')

      if (!allowed) {
        addLog(`❌ Access Denied. Your Role: ${userRole}, Selected: ${role}`)
        localStorage.clear()
        throw new Error(`Access Denied: You are ${userRole}, cannot access ${role} features.`)
      }

      addLog('✅ ALL CHECKS PASSED!')
      alert(`✅ Login Successful!`)

      addLog('⏳ Preparing to redirect...')
      setTimeout(() => {
        let target = '/dashboard/passenger'
        if (role === 'driver') target = '/dashboard/driver'
        if (role === 'owner') target = '/dashboard/owner'

        window.location.href = target
      }, 500)

    } catch (error: any) {
      addLog(`❌ FINAL ERROR: ${error.message}`)
      alert(error.message)
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="min-h-screen bg-gradient-to-br from-[#0f172a] to-[#1e293b] flex flex-col items-center justify-center p-5 overflow-hidden">
      <style jsx>{`
        @keyframes slideUp {
          from {
            transform: translateY(30px);
            opacity: 0;
          }
          to {
            transform: translateY(0);
            opacity: 1;
          }
        }
        .login-container {
          animation: slideUp 0.6s ease-out;
        }
      `}</style>

      <div className="login-container w-full max-w-[450px] p-10 bg-white/[0.03] backdrop-blur-[20px] border border-white/10 rounded-3xl shadow-2xl relative z-10">
        <div className="text-center mb-8">
          <h1 className="text-4xl font-extrabold bg-gradient-to-r from-[#818cf8] to-[#c084fc] bg-clip-text text-transparent mb-3">
            {isSignup ? 'Create Account' : 'Welcome Back'}
          </h1>
          <p className="text-[#94a3b8] text-base">
            {isSignup ? 'Join us for smarter journeys' : 'Ready to smarter your journey?'}
          </p>
        </div>

        <form onSubmit={isSignup ? handleSignup : handleLogin}>
          {/* Role Selection */}
          <div className="grid grid-cols-3 gap-3 mb-8">
            {[
              { value: 'passenger', icon: '👤', label: 'Passenger' },
              { value: 'driver', icon: '🚐', label: 'Driver' },
              { value: 'owner', icon: '🏢', label: 'Bus Owner' }
            ].map((item) => (
              <label key={item.value} className="cursor-pointer">
                <input
                  type="radio"
                  name="role"
                  value={item.value}
                  checked={role === item.value}
                  onChange={(e) => setRole(e.target.value as typeof role)}
                  className="sr-only"
                />
                <div className={`flex flex-col items-center justify-center p-4 rounded-2xl border-2 transition-all ${role === item.value
                  ? 'bg-[#6366f1]/10 border-[#6366f1] shadow-[0_0_15px_rgba(99,102,241,0.2)]'
                  : 'bg-white/5 border-transparent'
                  }`}>
                  <span className="text-2xl mb-2">{item.icon}</span>
                  <span className="text-xs font-semibold text-[#cbd5e1]">{item.label}</span>
                </div>
              </label>
            ))}
          </div>

          {/* Full Name (Signup only) */}
          {isSignup && (
            <div className="mb-5">
              <label className="block text-sm text-[#94a3b8] mb-2 ml-1">Full Name</label>
              <input
                type="text"
                placeholder="Your full name"
                value={fullName}
                onChange={(e) => setFullName(e.target.value)}
                required
                className="w-full px-5 py-3.5 bg-white/5 border border-white/10 rounded-xl text-white text-base outline-none transition-all focus:border-[#6366f1] focus:bg-white/8 focus:shadow-[0_0_0_4px_rgba(99,102,241,0.1)]"
              />
            </div>
          )}

          {/* Email Input */}
          <div className="mb-5">
            <label className="block text-sm text-[#94a3b8] mb-2 ml-1">Email Address</label>
            <input
              type="email"
              placeholder="name@example.com"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              required
              className="w-full px-5 py-3.5 bg-white/5 border border-white/10 rounded-xl text-white text-base outline-none transition-all focus:border-[#6366f1] focus:bg-white/8 focus:shadow-[0_0_0_4px_rgba(99,102,241,0.1)]"
            />
          </div>

          {/* Password Input */}
          <div className="mb-5">
            <label className="block text-sm text-[#94a3b8] mb-2 ml-1">Password</label>
            <input
              type="password"
              placeholder="••••••••"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              required
              minLength={6}
              className="w-full px-5 py-3.5 bg-white/5 border border-white/10 rounded-xl text-white text-base outline-none transition-all focus:border-[#6366f1] focus:bg-white/8 focus:shadow-[0_0_0_4px_rgba(99,102,241,0.1)]"
            />
          </div>

          {/* Login/Signup Button */}
          <button
            type="submit"
            disabled={loading}
            className="w-full py-3.5 mt-2.5 bg-gradient-to-r from-[#6366f1] to-[#a855f7] text-white text-lg font-bold rounded-xl transition-all hover:-translate-y-0.5 hover:shadow-[0_10px_20px_-5px_rgba(99,102,241,0.4)] active:translate-y-0 disabled:opacity-50"
          >
            {loading ? 'Please wait...' : isSignup ? 'CREATE ACCOUNT' : 'LOGIN NOW'}
          </button>
        </form>

        <div className="text-center mt-6">
          <p className="text-gray-600">
            {isSignup ? 'Already have an account?' : "Don't have an account?"}{' '}
            <button
              onClick={() => setIsSignup(!isSignup)}
              className="text-[#667eea] font-semibold hover:underline"
            >
              {isSignup ? 'Login' : 'Create Account'}
            </button>
          </p>
        </div>
      </div>

      {/* Debug Console */}
      {logs.length > 0 && (
        <div className="mt-8 w-full max-w-[450px] p-5 bg-black/80 backdrop-blur-md border border-red-500/30 rounded-xl text-xs font-mono text-green-400 overflow-hidden relative z-20">
          <h3 className="text-red-400 font-bold mb-2 border-b border-white/10 pb-1"> DEBUG CONSOLE (Check here for errors)</h3>
          <div className="flex flex-col gap-1 max-h-[150px] overflow-y-auto">
            {logs.map((log, i) => (
              <div key={i} className="break-all">{log}</div>
            ))}
          </div>
        </div>
      )}

      {/* Background gradients */}
      <div className="fixed top-0 left-0 w-full h-full pointer-events-none -z-10">
        <div className="absolute top-[20%] left-[20%] w-[500px] h-[500px] bg-[#6366f1]/15 rounded-full blur-[100px]"></div>
        <div className="absolute bottom-[20%] right-[20%] w-[500px] h-[500px] bg-[#a855f7]/15 rounded-full blur-[100px]"></div>
      </div>
    </div>
  )
}
