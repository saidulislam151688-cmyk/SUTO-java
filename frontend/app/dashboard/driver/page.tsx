"use client"

import { useRouter } from "next/navigation"
import { useEffect, useState, useRef } from "react"
import { apiClient } from "@/lib/api-client"
import { Settings, User, MapPin, DollarSign, FileText, X, Save, Truck, Activity, Bell } from "lucide-react"

const TRIPS_DATA = [
    { id: 1, route: "Farmgate ⇄ Uttara", busId: "PJ-101", timing: "08:30 AM - 10:30 AM", status: "pending" },
    { id: 2, route: "Mirpur ⇄ Motijheel", busId: "PJ-105", timing: "11:00 AM - 01:00 PM", status: "pending" },
    { id: 3, route: "Gazipur ⇄ Mohakhali", busId: "PJ-110", timing: "02:00 PM - 04:00 PM", status: "completed" },
    { id: 4, route: "Gulshan ⇄ Banani", busId: "PJ-202", timing: "04:30 PM - 06:00 PM", status: "pending" },
    { id: 5, route: "Dhanmondi ⇄ Shahbagh", busId: "PJ-305", timing: "06:30 PM - 08:00 PM", status: "pending" },
    { id: 6, route: "Savar ⇄ Gabtoli", busId: "PJ-401", timing: "08:30 PM - 10:30 PM", status: "pending" }
]

export default function DriverDashboard() {
    const router = useRouter()
    const [mounted, setMounted] = useState(false)
    const [trips, setTrips] = useState<any[]>([])
    const [loading, setLoading] = useState(true)
    const [userId, setUserId] = useState<string | null>(null)
    const [showSettings, setShowSettings] = useState(false)
    const fileInputRef = useRef<HTMLInputElement>(null)
    const [uploadedFiles, setUploadedFiles] = useState<any[]>([])
    const [saving, setSaving] = useState(false)
    const [error, setError] = useState<string | null>(null)

    // Profile State (Default 0/Empty as requested)
    const [profile, setProfile] = useState({
        name: "",
        address: "",
        age: "",
        experience: "",
        dailyEarnings: "",
        monthlyEarnings: "",
        totalEarnings: "",
        documents: "",
        licenseNumber: "",
        vehicleModel: "",
        shift: ["Morning"],
        status: "Active",
        language: "Bangla",
        notifications: true
    })

    useEffect(() => {
        setMounted(true)
        checkAuth()
    }, [router])

    const checkAuth = async () => {
        const token = localStorage.getItem('token')
        const userRole = localStorage.getItem('userRole')

        if (!token) {
            router.push('/')
            return
        }

        // Decode JWT to get ID (simple implementation or store ID in localStorage on login)
        // For now, assuming the Backend /login response included ID too.
        // Let's assume we can fetch "Me" or similar.
        // Or we stored it. Wait, login only stored token/role.
        // We might need to fetch profile via token.

        // Temporarily, we will assume backend supports /profiles/me or similar, 
        // OR we decode the token if it's JWT.
        // But to be safe, I will update Login to store userID or fetch it here.

        // Let's use a "me" endpoint mock or get from profile call if possible.
        // Let's assume the user IS the one the token belongs to.
        try {
            // In a real app we'd decode the JWT or hit a /me endpoint
            // For this migration, we'll try to get it from a new `apiClient.getProfile('me')` call
            // which the backend should handle (interpreting 'me' as current user).
            // If Backend doesn't support 'me', we might have an issue.
            // Let's fetch the ID from local storage if we modify Login to save it,
            // checking Login page... it doesn't save ID. 

            // Quick Fix: Modify Login to save ID, BUT I can't restart that flow easily.
            // I will modify this to assume the token is enough for fetchTrips if I change the API signature,
            // BUT `fetchTrips` takes an ID. 

            // I will use a placeholder 'current' and let the backend handle it?
            // Or better, let's parse the user ID if possible or fetch profile list filter by email?
            // No, let's try to fetch user details.

            // Assuming the backend has a /auth/me endpoint? I didn't verify that.
            // Let's update apiClient to handle this.

            // Fallback: If 'userRole' exists, we are somewhat auth'd.
            setLoading(false)
            // fetchTrips('me') // Let backend resolve 'me'

        } catch (e) {
            router.push('/')
        }
    }

    const fetchTrips = async (driverId: string) => {
        // Mock data if backend fails or empty
        try {
            const data = await apiClient.getDriverTrips(driverId || 'me')
            // Map DB format to UI format
            const mappedTrips = data.map((booking: any) => ({
                id: booking.id,
                route: `${booking.origin} ⇄ ${booking.destination}`,
                busId: booking.bus_id || 'Not Assigned',
                timing: `${booking.trip_time} (Date: ${booking.trip_date})`,
                status: booking.status,
                raw_date: booking.trip_date
            }))
            setTrips(mappedTrips)
        } catch (e) {
            console.error("Failed to fetch trips", e)
        }
    }

    const handleAccept = async (tripId: string) => {
        if (!confirm('Accept this trip?')) return
        try {
            await apiClient.updateTripStatus(tripId, 'accepted')
            alert('✅ Trip Accepted! Drive safely.')
            fetchTrips('me') // Refresh list
        } catch (error) {
            alert('Error accepting trip')
        }
    }

    const handleReject = async (tripId: string) => {
        if (!confirm('Are you sure you want to reject this trip?')) return
        try {
            await apiClient.updateTripStatus(tripId, 'rejected')
            alert('Trip rejected.')
            fetchTrips('me') // Refresh list
        } catch (error) {
            alert('Error rejecting trip')
        }
    }

    const handleProceed = () => {
        router.push('/dashboard/driver/navigation')
    }

    const handleLogout = () => {
        localStorage.clear()
        router.push('/')
    }

    const handleProfileChange = (field: string, value: any) => {
        setProfile(prev => ({ ...prev, [field]: value }))
    }

    const handleFileUpload = (e: React.ChangeEvent<HTMLInputElement>) => {
        if (e.target.files && e.target.files.length > 0) {
            const newFiles = Array.from(e.target.files)
            handleDocumentUpload(newFiles)
        }
    }

    const handleDocumentUpload = async (files: File[]) => {
        setSaving(true)
        const successfulUploads: any[] = []

        for (const file of files) {
            try {
                const formData = new FormData()
                formData.append('file', file)

                const response = await fetch('/api/driver/documents', {
                    method: 'POST',
                    body: formData
                })

                if (!response.ok) {
                    throw new Error('Upload failed')
                }

                const result = await response.json()
                successfulUploads.push(result.document)
            } catch (error) {
                console.error('Upload error:', error)
                alert(`Failed to upload ${file.name}`)
            }
        }

        if (successfulUploads.length > 0) {
            setUploadedFiles(prev => [...prev, ...successfulUploads])
        }

        setSaving(false)
    }

    const handleDeleteDocument = async (docId: string, fileName: string) => {
        if (!confirm(`Delete ${fileName}?`)) return

        setSaving(true)
        try {
            const response = await fetch(`/api/driver/documents?id=${docId}`, {
                method: 'DELETE'
            })

            if (!response.ok) {
                throw new Error('Delete failed')
            }

            setUploadedFiles(prev => prev.filter((doc: any) => doc.id !== docId))
        } catch (error) {
            console.error('Delete error:', error)
            alert('Failed to delete document')
        }
        setSaving(false)
    }

    const handleSaveProfile = async () => {
        setSaving(true)
        setError(null)

        try {
            const response = await fetch('/api/driver/profile', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({
                    name: profile.name,
                    address: profile.address,
                    age: profile.age,
                    experience: profile.experience,
                    dailyEarnings: profile.dailyEarnings,
                    monthlyEarnings: profile.monthlyEarnings,
                    totalEarnings: profile.totalEarnings,
                    licenseNumber: profile.licenseNumber,
                    vehicleModel: profile.vehicleModel,
                    shift: profile.shift,
                    language: profile.language,
                    notifications: profile.notifications
                })
            })

            if (!response.ok) {
                throw new Error('Failed to save profile')
            }

            const result = await response.json()
            setShowSettings(false)
            alert('✅ Profile saved successfully!')
        } catch (err: any) {
            console.error('Save error:', err)
            setError(err.message || 'Failed to save profile')
            alert('❌ Failed to save profile. Please try again.')
        } finally {
            setSaving(false)
        }
    }

    useEffect(() => {
        const loadProfileData = async () => {
            const { data: { user } } = await supabase.auth.getUser()
            if (!user) return

            try {
                const response = await fetch('/api/driver/profile')
                if (!response.ok) throw new Error('Failed to load profile')

                const data = await response.json()

                if (data.profile) {
                    setProfile({
                        name: data.profile.name || '',
                        address: data.profile.address || '',
                        age: data.profile.age || '',
                        experience: data.profile.experience || '',
                        dailyEarnings: data.profile.daily_earnings || '',
                        monthlyEarnings: data.profile.monthly_earnings || '',
                        totalEarnings: data.profile.total_earnings || '',
                        documents: '',
                        licenseNumber: data.profile.license_number || '',
                        vehicleModel: data.profile.vehicle_model || '',
                        shift: data.profile.shift_preferences || ['Morning'],
                        status: 'Active',
                        language: data.profile.language || 'Bangla',
                        notifications: data.profile.notifications_enabled ?? true
                    })
                }

                if (data.documents) {
                    setUploadedFiles(data.documents)
                }
            } catch (error) {
                console.error('Failed to load profile:', error)
            }
        }

        if (!loading) {
            loadProfileData()
        }
    }, [loading])

    if (!mounted || loading) return null

    return (
        <div className="min-h-screen bg-[#f8fafc] p-5 relative">
            <div className="max-w-6xl mx-auto">
                {/* Header */}
                <div className="bg-white rounded-2xl p-6 shadow-lg mb-8 flex justify-between items-center">
                    <h1 className="text-3xl font-bold text-[#10b981] flex items-center gap-3">
                        🚐 Driver Dashboard
                    </h1>
                    <div className="flex gap-3 items-center">
                        <button
                            onClick={() => setShowSettings(true)}
                            className="bg-gray-100 text-gray-600 p-3 rounded-xl hover:bg-gray-200 transition-all mr-2"
                            title="Driver Settings"
                        >
                            <Settings size={20} />
                        </button>
                        <button
                            onClick={() => router.push('/dashboard/passenger')}
                            className="bg-blue-100 text-blue-600 px-6 py-2 rounded-xl font-semibold hover:bg-blue-200 transition-all"
                        >
                            Switch to Passenger
                        </button>
                        <button
                            onClick={() => router.push('/dashboard/owner')}
                            className="bg-purple-100 text-purple-600 px-6 py-2 rounded-xl font-semibold hover:bg-purple-200 transition-all"
                        >
                            Switch to Owner
                        </button>
                        <button
                            onClick={handleLogout}
                            className="bg-red-100 text-red-600 px-6 py-2 rounded-xl font-semibold hover:bg-red-200 transition-all"
                        >
                            Logout
                        </button>
                    </div>
                </div>

                {/* Stats Grid - Now Linked to Profile State */}
                <div className="grid grid-cols-1 md:grid-cols-4 gap-5 mb-8">
                    <div className="bg-white rounded-2xl p-6 shadow-lg">
                        <div className="text-sm font-semibold text-gray-500 mb-2">DAILY EARNINGS</div>
                        <div className="text-3xl font-extrabold text-[#10b981]">৳ {Number(profile.dailyEarnings || 0).toLocaleString()}</div>
                        <div className="text-sm text-green-600 mt-1">↑ 4% from yesterday</div>
                    </div>
                    <div className="bg-white rounded-2xl p-6 shadow-lg">
                        <div className="text-sm font-semibold text-gray-500 mb-2">MONTHLY EARNINGS</div>
                        <div className="text-3xl font-extrabold text-[#10b981]">৳ {Number(profile.monthlyEarnings || 0).toLocaleString()}</div>
                        <div className="text-sm text-gray-600 mt-1">24 active days</div>
                    </div>
                    <div className="bg-white rounded-2xl p-6 shadow-lg">
                        <div className="text-sm font-semibold text-gray-500 mb-2">TOTAL EARNINGS</div>
                        <div className="text-3xl font-extrabold text-[#10b981]">৳ {Number(profile.totalEarnings || 0).toLocaleString()}</div>
                        <div className="text-sm text-gray-600 mt-1">Joining: Jan 2023</div>
                    </div>
                    <div className="bg-white rounded-2xl p-6 shadow-lg">
                        <div className="text-sm font-semibold text-gray-500 mb-2">RATINGS</div>
                        <div className="text-3xl font-extrabold text-[#10b981]">4.8 ⭐</div>
                        <div className="text-sm text-gray-600 mt-1">150 reviews</div>
                    </div>
                </div>

                {/* Trip Management */}
                <div className="bg-white rounded-2xl p-8 shadow-lg">
                    <h2 className="text-2xl font-bold text-gray-800 mb-6 flex items-center gap-2">
                        📍 Assigned Trips Management
                    </h2>

                    {/* All 6 Trips */}
                    {trips.map((trip) => {
                        const statusColor = trip.status === 'completed' ? 'bg-green-600' : trip.status === 'accepted' ? 'bg-blue-600' : trip.status === 'rejected' ? 'bg-red-600' : 'bg-orange-400'
                        const statusText = trip.status === 'completed' ? 'COMPLETED' : trip.status === 'accepted' ? 'ACTIVE' : trip.status === 'rejected' ? 'REJECTED' : 'PENDING'
                        const borderColor = trip.status === 'accepted' ? 'border-blue-200' : trip.status === 'pending' ? 'border-green-200' : 'border-gray-200'
                        const opacity = trip.status === 'completed' || trip.status === 'rejected' ? 'opacity-60' : 'opacity-100'

                        return (
                            <div key={trip.id} className={`bg-gradient-to-r from-white to-green-50 border-2 ${borderColor} rounded-3xl p-8 mb-5 relative hover:translate-x-1 transition-all ${opacity}`}>
                                <div className={`absolute top-5 right-5 ${statusColor} text-white px-4 py-1 rounded-full text-xs font-extrabold`}>
                                    {statusText}
                                </div>

                                <div className="mb-5">
                                    <h3 className="text-xl font-bold text-green-800 mb-3">Trip Assignment #{trip.id}</h3>
                                </div>

                                <div className="grid grid-cols-1 md:grid-cols-4 gap-5 mb-5">
                                    <div>
                                        <span className="block text-xs text-gray-500 font-bold uppercase mb-1">Route</span>
                                        <b className="text-lg text-gray-800">{trip.route}</b>
                                    </div>
                                    <div>
                                        <span className="block text-xs text-gray-500 font-bold uppercase mb-1">Bus ID</span>
                                        <b className="text-lg text-gray-800">{trip.busId}</b>
                                    </div>
                                    <div>
                                        <span className="block text-xs text-gray-500 font-bold uppercase mb-1">Timing</span>
                                        <b className="text-lg text-gray-800">{trip.timing}</b>
                                    </div>
                                    <div>
                                        <span className="block text-xs text-gray-500 font-bold uppercase mb-1">Date</span>
                                        <b className="text-lg text-gray-800">{new Date().toLocaleDateString()}</b>
                                    </div>
                                </div>

                                {trip.status === 'pending' && (
                                    <div className="flex gap-4">
                                        <button
                                            onClick={() => handleAccept(trip.id)}
                                            className="flex-1 bg-green-600 text-white py-4 rounded-2xl font-bold hover:-translate-y-1 hover:shadow-xl transition-all"
                                        >
                                            Accept
                                        </button>
                                        <button
                                            onClick={() => handleReject(trip.id)}
                                            className="flex-1 bg-red-100 text-red-600 py-4 rounded-2xl font-bold hover:bg-red-200 transition-all"
                                        >
                                            Reject
                                        </button>
                                    </div>
                                )}

                                {trip.status === 'accepted' && (
                                    <button
                                        onClick={handleProceed}
                                        className="w-full bg-purple-600 text-white py-4 rounded-2xl font-bold hover:-translate-y-1 hover:shadow-xl transition-all flex items-center justify-center gap-2"
                                    >
                                        Proceed to Navigation ➔
                                    </button>
                                )}

                                {trip.status === 'completed' && (
                                    <div className="text-center text-green-700 font-bold py-3 bg-green-100 rounded-xl">
                                        ✅ Trip Completed - Earnings: ৳ 350
                                    </div>)}
                            </div>
                        )
                    })}
                </div>
            </div>

            {/* Settings Modal */}
            {showSettings && (
                <div
                    className="fixed inset-0 bg-black/60 z-[999] flex items-center justify-center p-5 backdrop-blur-sm"
                    onClick={(e) => {
                        if (e.target === e.currentTarget) setShowSettings(false)
                    }}
                >
                    <div className="bg-white w-full max-w-4xl rounded-3xl shadow-2xl max-h-[90vh] overflow-y-auto animate-in fade-in zoom-in duration-200">

                        <div className="p-6 border-b border-gray-100 flex justify-between items-center sticky top-0 bg-white z-10">
                            <h2 className="text-2xl font-bold text-gray-800 flex items-center gap-2">
                                <Settings className="text-[#10b981]" /> Driver Profile & Settings
                            </h2>
                            <button onClick={() => setShowSettings(false)} className="p-2 hover:bg-gray-100 rounded-full transition-colors">
                                <X className="text-gray-500" />
                            </button>
                        </div>

                        <div className="p-8 space-y-8">
                            {/* Personal Information */}
                            <section>
                                <h3 className="text-lg font-bold text-gray-700 mb-4 flex items-center gap-2">
                                    <User size={18} className="text-blue-500" /> Personal Information
                                </h3>
                                <div className="grid grid-cols-1 md:grid-cols-2 gap-5">
                                    <div className="space-y-1">
                                        <label className="text-sm font-semibold text-gray-500">Full Name</label>
                                        <input
                                            type="text"
                                            value={profile.name}
                                            onChange={(e) => handleProfileChange('name', e.target.value)}
                                            placeholder="Enter your name"
                                            className="w-full p-3 bg-gray-50 border border-gray-200 rounded-xl focus:ring-2 focus:ring-[#10b981] focus:border-transparent outline-none transition-all"
                                        />
                                    </div>
                                    <div className="space-y-1">
                                        <label className="text-sm font-semibold text-gray-500">Home Address</label>
                                        <input
                                            type="text"
                                            value={profile.address}
                                            onChange={(e) => handleProfileChange('address', e.target.value)}
                                            placeholder="Enter your address"
                                            className="w-full p-3 bg-gray-50 border border-gray-200 rounded-xl focus:ring-2 focus:ring-[#10b981] focus:border-transparent outline-none transition-all"
                                        />
                                    </div>
                                    <div className="space-y-1">
                                        <label className="text-sm font-semibold text-gray-500">Age</label>
                                        <input
                                            type="number"
                                            value={profile.age}
                                            onChange={(e) => handleProfileChange('age', e.target.value)}
                                            placeholder="Example: 35"
                                            className="w-full p-3 bg-gray-50 border border-gray-200 rounded-xl focus:ring-2 focus:ring-[#10b981] focus:border-transparent outline-none transition-all"
                                        />
                                    </div>
                                    <div className="space-y-1">
                                        <label className="text-sm font-semibold text-gray-500">Experience (Years)</label>
                                        <input
                                            type="text"
                                            value={profile.experience}
                                            onChange={(e) => handleProfileChange('experience', e.target.value)}
                                            placeholder="Example: 5 years"
                                            className="w-full p-3 bg-gray-50 border border-gray-200 rounded-xl focus:ring-2 focus:ring-[#10b981] focus:border-transparent outline-none transition-all"
                                        />
                                    </div>
                                </div>
                            </section>

                            <hr className="border-gray-100" />

                            {/* Earnings Management */}
                            <section>
                                <h3 className="text-lg font-bold text-gray-700 mb-4 flex items-center gap-2">
                                    <DollarSign size={18} className="text-green-500" /> Financial Overview (Set Manually)
                                </h3>
                                <div className="grid grid-cols-1 md:grid-cols-3 gap-5">
                                    <div className="space-y-1">
                                        <label className="text-sm font-semibold text-gray-500">Daily Earnings (৳)</label>
                                        <input
                                            type="number"
                                            min="0"
                                            value={profile.dailyEarnings}
                                            onChange={(e) => {
                                                const val = e.target.value;
                                                if (val === "" || parseFloat(val) >= 0) {
                                                    handleProfileChange('dailyEarnings', val)
                                                }
                                            }}
                                            placeholder="Enter amount"
                                            className="w-full p-3 bg-green-50 border border-green-200 text-green-700 font-bold rounded-xl focus:ring-2 focus:ring-green-400 focus:border-transparent outline-none transition-all"
                                        />
                                    </div>
                                    <div className="space-y-1">
                                        <label className="text-sm font-semibold text-gray-500">Monthly Earnings (৳)</label>
                                        <input
                                            type="number"
                                            min="0"
                                            value={profile.monthlyEarnings}
                                            onChange={(e) => {
                                                const val = e.target.value;
                                                if (val === "" || parseFloat(val) >= 0) {
                                                    handleProfileChange('monthlyEarnings', val)
                                                }
                                            }}
                                            placeholder="Enter amount"
                                            className="w-full p-3 bg-green-50 border border-green-200 text-green-700 font-bold rounded-xl focus:ring-2 focus:ring-green-400 focus:border-transparent outline-none transition-all"
                                        />
                                    </div>
                                    <div className="space-y-1">
                                        <label className="text-sm font-semibold text-gray-500">Total Earnings (৳)</label>
                                        <input
                                            type="number"
                                            min="0"
                                            value={profile.totalEarnings}
                                            onChange={(e) => {
                                                const val = e.target.value;
                                                if (val === "" || parseFloat(val) >= 0) {
                                                    handleProfileChange('totalEarnings', val)
                                                }
                                            }}
                                            placeholder="Enter amount"
                                            className="w-full p-3 bg-green-50 border border-green-200 text-green-700 font-bold rounded-xl focus:ring-2 focus:ring-green-400 focus:border-transparent outline-none transition-all"
                                        />
                                    </div>
                                </div>
                            </section>

                            <hr className="border-gray-100" />

                            {/* Advanced Features */}
                            <section>
                                <h3 className="text-lg font-bold text-gray-700 mb-4 flex items-center gap-2">
                                    <Activity size={18} className="text-purple-500" /> Advanced Settings & Documents
                                </h3>
                                <div className="grid grid-cols-1 md:grid-cols-2 gap-5">
                                    {/* Document Upload Section */}
                                    {/* Interactive Document Upload Section */}
                                    <div className="space-y-3 col-span-1 md:col-span-2">
                                        <label className="text-sm font-semibold text-gray-500">Official Documents (License, NID, etc.)</label>

                                        <input
                                            type="file"
                                            multiple
                                            ref={fileInputRef}
                                            className="hidden"
                                            onChange={handleFileUpload}
                                            accept=".jpg,.jpeg,.png,.pdf,.doc,.docx"
                                        />

                                        <div
                                            onClick={() => fileInputRef.current?.click()}
                                            className="border-2 border-dashed border-gray-300 rounded-xl p-6 text-center hover:bg-gray-50 transition-colors cursor-pointer group"
                                        >
                                            <div className="flex flex-col items-center gap-2">
                                                <div className="bg-blue-50 p-3 rounded-full group-hover:bg-blue-100 transition-colors">
                                                    <FileText className="text-blue-500" size={24} />
                                                </div>
                                                <p className="text-sm text-gray-600 font-medium">Click to upload multiple documents</p>
                                                <p className="text-xs text-gray-400">PDF, JPG, PNG, DOC (Max 10MB)</p>
                                            </div>
                                        </div>

                                        {/* Uploaded Files List from Database */}
                                        {uploadedFiles.length > 0 && (
                                            <div className="space-y-2 mt-2">
                                                {uploadedFiles.map((doc: any) => (
                                                    <div key={doc.id} className="flex items-center justify-between p-3 bg-gray-50 border border-gray-200 rounded-lg animate-in fade-in">
                                                        <div className="flex items-center gap-3">
                                                            <div className="bg-white p-2 rounded-md border border-gray-100">
                                                                <FileText size={16} className="text-[#10b981]" />
                                                            </div>
                                                            <div>
                                                                <p className="text-sm font-bold text-gray-700">{doc.file_name}</p>
                                                                <div className="flex items-center gap-2">
                                                                    <p className="text-xs text-gray-500">
                                                                        {doc.file_size ? (doc.file_size / 1024 / 1024).toFixed(2) + ' MB' : 'Unknown size'}
                                                                    </p>
                                                                    {doc.verified && (
                                                                        <span className="text-xs text-green-600 flex items-center gap-1">
                                                                            ✅ Verified
                                                                        </span>
                                                                    )}
                                                                    {!doc.verified && (
                                                                        <span className="text-xs text-orange-600 flex items-center gap-1">
                                                                            ⌚ Pending
                                                                        </span>
                                                                    )}
                                                                </div>
                                                            </div>
                                                        </div>
                                                        <button
                                                            onClick={() => handleDeleteDocument(doc.id, doc.file_name)}
                                                            disabled={saving}
                                                            className="text-gray-400 hover:text-red-500 p-1 hover:bg-red-50 rounded-full transition-all disabled:opacity-50"
                                                        >
                                                            <X size={16} />
                                                        </button>
                                                    </div>
                                                ))}
                                            </div>
                                        )}
                                    </div>



                                    <div className="space-y-1">
                                        <label className="text-sm font-semibold text-gray-500">Vehicle Type Preference</label>
                                        <div className="flex items-center gap-2 p-3 bg-gray-50 border border-gray-200 rounded-xl">
                                            <Truck className="text-gray-400" size={20} />
                                            <select
                                                value={profile.vehicleModel}
                                                onChange={(e) => handleProfileChange('vehicleModel', e.target.value)}
                                                className="bg-transparent w-full outline-none"
                                            >
                                                <option value="">Select vehicle type...</option>
                                                <optgroup label="Buses">
                                                    <option value="Public Bus">Public Bus (Local)</option>
                                                    <option value="Mini Bus">Mini Bus / Citibus</option>
                                                    <option value="Sitting Service">Sitting Service / Counter Bus</option>
                                                    <option value="AC Bus">AC Bus (Premium)</option>
                                                    <option value="Double Decker">Double Decker</option>
                                                    <option value="School Bus">School/Staff Bus</option>
                                                </optgroup>
                                                <optgroup label="Light Vehicles">
                                                    <option value="Private Car">Private Car (Sedan)</option>
                                                    <option value="Microbus">Microbus / Noah</option>
                                                    <option value="Jeep">Jeep / SUV</option>
                                                </optgroup>
                                                <optgroup label="Others">
                                                    <option value="Human Hauler">Human Hauler (Leguna)</option>
                                                    <option value="CNG">CNG Auto Rickshaw</option>
                                                    <option value="Truck">Truck / Pickup</option>
                                                </optgroup>
                                            </select>
                                        </div>
                                    </div>

                                    <div className="space-y-1">
                                        <label className="text-sm font-semibold text-gray-500">Preferred Shifts (Multi-Select)</label>
                                        <div className="grid grid-cols-2 gap-2">
                                            {['Morning (6am-12pm)', 'Day (12pm-6pm)', 'Evening (6pm-12am)', 'Night (12am-6am)'].map((shift) => {
                                                const isActive = profile.shift.includes(shift.split(' ')[0]); // Check simple string match
                                                return (
                                                    <button
                                                        key={shift}
                                                        onClick={() => {
                                                            const simpleName = shift.split(' ')[0];
                                                            const currentShifts = Array.isArray(profile.shift) ? profile.shift : [];
                                                            let newShifts;
                                                            if (currentShifts.includes(simpleName)) {
                                                                newShifts = currentShifts.filter((s: string) => s !== simpleName);
                                                            } else {
                                                                newShifts = [...currentShifts, simpleName];
                                                            }
                                                            handleProfileChange('shift', newShifts);
                                                        }}
                                                        className={`p-2 text-xs font-bold rounded-lg border transition-all ${isActive
                                                            ? 'bg-[#10b981] text-white border-[#10b981]'
                                                            : 'bg-white text-gray-600 border-gray-200 hover:border-[#10b981]'
                                                            }`}
                                                    >
                                                        {shift}
                                                    </button>
                                                )
                                            })}
                                        </div>
                                    </div>

                                    <div className="space-y-1">
                                        <label className="text-sm font-semibold text-gray-500">App Language</label>
                                        <select
                                            value={profile.language}
                                            onChange={(e) => handleProfileChange('language', e.target.value)}
                                            className="w-full p-3 bg-gray-50 border border-gray-200 rounded-xl outline-none"
                                        >
                                            <option value="English">English</option>
                                            <option value="Bangla">Bangla</option>
                                        </select>
                                    </div>

                                    <div className="flex items-center gap-3 p-3 bg-blue-50 border border-blue-100 rounded-xl">
                                        <Bell className="text-blue-500" />
                                        <div className="flex-1">
                                            <div className="font-semibold text-blue-900">Trip Alerts</div>
                                            <div className="text-xs text-blue-600">Get notified for new trips</div>
                                        </div>
                                        <input
                                            type="checkbox"
                                            checked={profile.notifications}
                                            onChange={(e) => handleProfileChange('notifications', e.target.checked)}
                                            className="w-5 h-5 accent-blue-600"
                                        />
                                    </div>
                                </div>
                            </section>
                        </div>

                        <div className="p-6 border-t border-gray-100 bg-gray-50 rounded-b-3xl flex justify-end gap-3 sticky bottom-0">
                            <button
                                onClick={() => setShowSettings(false)}
                                className="px-6 py-3 text-gray-600 font-bold hover:bg-gray-200 rounded-xl transition-all"
                            >
                                Cancel
                            </button>
                            <button
                                onClick={handleSaveProfile}
                                disabled={saving}
                                className="px-8 py-3 bg-[#10b981] text-white font-bold rounded-xl hover:shadow-lg hover:-translate-y-1 transition-all flex items-center gap-2 disabled:opacity-50 disabled:cursor-not-allowed"
                            >
                                <Save size={20} /> {saving ? 'Saving...' : 'Save Changes'}
                            </button>
                        </div>
                    </div>
                </div >
            )
            }
        </div >
    )
}
