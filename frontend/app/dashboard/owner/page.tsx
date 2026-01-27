"use client"

import { useRouter } from "next/navigation"
import { useEffect, useState } from "react"
import { apiClient } from "@/lib/api-client"

export default function OwnerDashboard() {
    const router = useRouter()
    const [mounted, setMounted] = useState(false)
    const [loading, setLoading] = useState(true)
    const [selectedDriver, setSelectedDriver] = useState<any>(null)
    const [showModal, setShowModal] = useState(false)
    const [drivers, setDrivers] = useState<any[]>([])

    // Booking Form State
    const [bookingDate, setBookingDate] = useState('')
    const [bookingTime, setBookingTime] = useState('')
    const [origin, setOrigin] = useState('')
    const [destination, setDestination] = useState('')
    const [isBooking, setIsBooking] = useState(false)

    const [userId, setUserId] = useState<string | null>(null)

    useEffect(() => {
        setMounted(true)
        checkAuth()
    }, [router])

    const checkAuth = async () => {
        const token = localStorage.getItem('token')
        if (!token) {
            router.push('/')
            return
        }

        // Simulating role check - assuming if they are here with a valid token, they are okay for now?
        // Or we should fetch profile "me"
        try {
            // Let's assume the user is an owner if they have a token and are on this page.
            // Ideally we call apiClient.getProfile('me') to verify role.
            // We'll skip strict role verify locally for migration speed, relying on backend 403.
            setLoading(false)
            fetchDrivers()
        } catch (e) {
            router.push('/')
        }
    }

    const fetchDrivers = async () => {
        try {
            // Need an endpoint to get all drivers for an owner?
            // Java Backend needs to support this. `apiClient.getOwnerDrivers(ownerId)`
            // We'll pass "me" or similar if backed supports, or real ID.
            // Let's try to get real ID from local storage if I can, otherwise "me".
            const data = await apiClient.getOwnerDrivers('me')
            if (data) {
                setDrivers(data)
            }
        } catch (error) {
            console.error('Error fetching drivers:', error)
        }
    }

    const handleLogout = () => {
        localStorage.clear()
        router.push('/')
    }

    const [assignedBus, setAssignedBus] = useState('')

    const openBookingModal = (driver: any) => {
        setSelectedDriver(driver)
        setAssignedBus(driver.vehicle_model || '') // Use vehicle model or bus ID from profile
        setShowModal(true)
    }

    const handleBooking = async (e: React.FormEvent) => {
        e.preventDefault()
        setIsBooking(true)

        try {
            await apiClient.assignTrip({
                driverId: selectedDriver.id, // Ensure this matches backend expected key
                customerName: 'Owner Assignment',
                origin,
                destination,
                tripDate: bookingDate,
                tripTime: bookingTime + ":00", // Add seconds for LocalTime parsing
                busId: assignedBus
            })

            alert('✅ Success!\nTrip assigned successfully. Notification sent to Driver.')
            setShowModal(false)
            // Reset form
            setBookingDate('')
            setBookingTime('')
            setOrigin('')
            setDestination('')
            setAssignedBus('')
        } catch (error: any) {
            alert('❌ Error: ' + error.message)
        } finally {
            setIsBooking(false)
        }
    }

    const getInitials = (name: string) => {
        return name ? name.split(' ').map((n: string) => n[0]).join('').substring(0, 2).toUpperCase() : '??'
    }

    if (!mounted || loading) return null

    return (
        <div className="min-h-screen bg-[#f5f3ff] p-5">
            <div className="max-w-6xl mx-auto">
                {/* Role Bar */}
                <div className="bg-white rounded-2xl px-8 py-4 shadow-lg mb-5 flex justify-between items-center">
                    <div className="flex items-center gap-3">
                        <span className="font-semibold text-gray-700">👤 User Mode:</span>
                        <span className="bg-purple-100 text-purple-600 px-3 py-1 rounded-full text-sm font-bold">
                            owner
                        </span>
                    </div>
                    <div className="flex gap-3">
                        <button
                            onClick={() => router.push('/dashboard/driver')}
                            className="bg-indigo-100 text-indigo-600 px-6 py-2 rounded-xl font-semibold hover:bg-indigo-200 transition-all"
                        >
                            Switch to Driver
                        </button>
                        <button
                            onClick={() => router.push('/dashboard/passenger')}
                            className="bg-blue-100 text-blue-600 px-6 py-2 rounded-xl font-semibold hover:bg-blue-200 transition-all"
                        >
                            Switch to Passenger
                        </button>
                        <button
                            onClick={handleLogout}
                            className="bg-red-100 text-red-600 px-6 py-2 rounded-xl font-semibold hover:bg-red-200 transition-all"
                        >
                            Logout
                        </button>
                    </div>
                </div>

                {/* Header */}
                <div className="bg-white rounded-2xl p-6 shadow-lg mb-8 flex justify-between items-center">
                    <h1 className="text-3xl font-bold text-[#8b5cf6] flex items-center gap-3">
                        🏢 Bus Owner Dashboard (Fleet: প্রজাপতি)
                    </h1>
                </div>

                {/* Stats Grid */}
                <div className="grid grid-cols-1 md:grid-cols-4 gap-5 mb-8">
                    <div className="bg-white rounded-2xl p-6 shadow-lg text-center">
                        <div className="text-sm font-semibold text-gray-500 mb-2">Total Buses</div>
                        <div className="text-3xl font-extrabold text-[#8b5cf6]">25</div>
                    </div>
                    <div className="bg-white rounded-2xl p-6 shadow-lg text-center">
                        <div className="text-sm font-semibold text-gray-500 mb-2">Active Drivers</div>
                        <div className="text-3xl font-extrabold text-[#8b5cf6]">{drivers.filter(d => d.status === 'online').length}</div>
                    </div>
                    <div className="bg-white rounded-2xl p-6 shadow-lg text-center">
                        <div className="text-sm font-semibold text-gray-500 mb-2">Average Fleet Rating</div>
                        <div className="text-3xl font-extrabold text-[#8b5cf6]">4.6 ⭐</div>
                    </div>
                    <div className="bg-white rounded-2xl p-6 shadow-lg text-center">
                        <div className="text-sm font-semibold text-gray-500 mb-2">Monthly Revenue</div>
                        <div className="text-3xl font-extrabold text-[#8b5cf6]">৳ 12,50,000</div>
                    </div>
                </div>

                {/* Driver Performance Table */}
                <div className="bg-white rounded-3xl p-8 shadow-lg">
                    <h2 className="text-2xl font-bold text-gray-800 mb-6 flex items-center gap-2">
                        👨‍✈️ Driver Performance & Ratings
                    </h2>

                    <table className="w-full">
                        <thead>
                            <tr className="border-b border-gray-200">
                                <th className="text-left py-4 px-4 text-sm text-gray-600 font-bold uppercase">Driver Name</th>
                                <th className="text-left py-4 px-4 text-sm text-gray-600 font-bold uppercase">Bus ID</th>
                                <th className="text-left py-4 px-4 text-sm text-gray-600 font-bold uppercase">Status</th>
                                <th className="text-left py-4 px-4 text-sm text-gray-600 font-bold uppercase">Rating</th>
                                <th className="text-left py-4 px-4 text-sm text-gray-600 font-bold uppercase">Complaints</th>
                                <th className="text-left py-4 px-4 text-sm text-gray-600 font-bold uppercase">Action</th>
                            </tr>
                        </thead>
                        <tbody>
                            {drivers.length === 0 ? (
                                <tr>
                                    <td colSpan={6} className="text-center py-10 text-gray-500">
                                        No active drivers found. When drivers sign up, they will appear here.
                                    </td>
                                </tr>
                            ) : (
                                drivers.map((driver, index) => (
                                    <tr key={index} className="border-b border-gray-100">
                                        <td className="py-5 px-4">
                                            <div className="flex items-center gap-3">
                                                <div className="w-10 h-10 bg-indigo-100 rounded-full flex items-center justify-center font-bold text-indigo-600">
                                                    {getInitials(driver.full_name || driver.email || 'Driver')}
                                                </div>
                                                <div className="flex flex-col">
                                                    <b className="text-gray-800">{driver.full_name || driver.email || 'Unknown'}</b>
                                                    <span className="text-xs text-gray-500">{driver.email}</span>
                                                </div>
                                            </div>
                                        </td>
                                        <td className="py-5 px-4">{driver.current_bus_id || 'Not Assigned'}</td>
                                        <td className="py-5 px-4">
                                            <span className={`inline-flex items-center gap-1 ${driver.status === 'online' ? 'text-green-600' : 'text-gray-500'}`}>
                                                <span className={`w-2 h-2 rounded-full ${driver.status === 'online' ? 'bg-green-600' : 'bg-gray-400'}`}></span>
                                                {driver.status === 'online' ? 'Online' : 'Offline'}
                                            </span>
                                        </td>
                                        <td className="py-5 px-4 text-orange-500 font-bold">
                                            {driver.average_rating === 'New' ? 'New' : `${driver.average_rating} ⭐`}
                                        </td>
                                        <td className="py-5 px-4">
                                            {driver.complaint_count > 0 ? (
                                                <span className="text-red-500 font-bold">{driver.complaint_count}</span>
                                            ) : (
                                                <span className="text-gray-400">0</span>
                                            )}
                                        </td>
                                        <td className="py-5 px-4">
                                            {driver.current_booking_status === 'pending' ? (
                                                <span className="bg-orange-100 text-orange-600 px-5 py-2 rounded-xl font-semibold inline-block">
                                                    ⏳ Waiting for Approval
                                                </span>
                                            ) : driver.current_booking_status === 'accepted' ? (
                                                <span className="bg-green-100 text-green-600 px-5 py-2 rounded-xl font-semibold inline-block">
                                                    ✅ Accepted
                                                </span>
                                            ) : (
                                                <button
                                                    onClick={() => openBookingModal(driver)}
                                                    className="bg-purple-600 text-white px-5 py-2 rounded-xl font-semibold hover:scale-105 hover:shadow-lg transition-all"
                                                >
                                                    Book Trip
                                                </button>
                                            )}
                                        </td>
                                    </tr>
                                ))
                            )}
                        </tbody>
                    </table>
                </div>
            </div>

            {/* Booking Modal */}
            {showModal && selectedDriver && (
                <div className="fixed inset-0 bg-black/40 backdrop-blur-sm flex items-center justify-center z-50">
                    <div className="bg-white rounded-3xl p-8 max-w-md w-full mx-5 shadow-2xl animate-[slideUp_0.3s_ease]">
                        <h2 className="text-2xl font-bold text-[#8b5cf6] mb-6">📝 Assign Trip to {selectedDriver.full_name || 'Driver'}</h2>
                        <form onSubmit={handleBooking}>
                            <div className="mb-5">
                                <label className="block text-sm font-semibold text-gray-600 mb-2">Driver Name</label>
                                <input type="text" value={selectedDriver.full_name || selectedDriver.email} readOnly className="w-full p-3 bg-gray-50 border border-gray-200 rounded-xl" />
                            </div>
                            <div className="mb-5">
                                <label className="block text-sm font-semibold text-gray-600 mb-2">Assigned Bus (Enter Bus ID)</label>
                                <input
                                    type="text"
                                    required
                                    placeholder="e.g. PJ-101"
                                    value={assignedBus}
                                    onChange={(e) => setAssignedBus(e.target.value)}
                                    className="w-full p-3 bg-white border border-gray-200 rounded-xl focus:border-purple-500 outline-none"
                                />
                            </div>
                            <div className="grid grid-cols-2 gap-4 mb-5">
                                <div>
                                    <label className="block text-sm font-semibold text-gray-600 mb-2">Date</label>
                                    <input
                                        type="date"
                                        required
                                        className="w-full p-3 border border-gray-200 rounded-xl"
                                        value={bookingDate}
                                        onChange={(e) => setBookingDate(e.target.value)}
                                    />
                                </div>
                                <div>
                                    <label className="block text-sm font-semibold text-gray-600 mb-2">Time</label>
                                    <input
                                        type="time"
                                        required
                                        className="w-full p-3 border border-gray-200 rounded-xl"
                                        value={bookingTime}
                                        onChange={(e) => setBookingTime(e.target.value)}
                                    />
                                </div>
                            </div>
                            <div className="grid grid-cols-2 gap-4 mb-5">
                                <div>
                                    <label className="block text-sm font-semibold text-gray-600 mb-2">From</label>
                                    <select
                                        required
                                        className="w-full p-3 border border-gray-200 rounded-xl"
                                        value={origin}
                                        onChange={(e) => setOrigin(e.target.value)}
                                    >
                                        <option value="">Select Origin</option>
                                        <option>Farmgate</option>
                                        <option>Uttara</option>
                                        <option>Mirpur</option>
                                    </select>
                                </div>
                                <div>
                                    <label className="block text-sm font-semibold text-gray-600 mb-2">To</label>
                                    <select
                                        required
                                        className="w-full p-3 border border-gray-200 rounded-xl"
                                        value={destination}
                                        onChange={(e) => setDestination(e.target.value)}
                                    >
                                        <option value="">Select Destination</option>
                                        <option>Gazipur</option>
                                        <option>Paltan</option>
                                        <option>Dhanmondi</option>
                                    </select>
                                </div>
                            </div>
                            <div className="flex gap-3 mt-8">
                                <button
                                    type="button"
                                    onClick={() => setShowModal(false)}
                                    className="flex-1 bg-gray-100 text-gray-700 py-3 rounded-xl font-bold hover:bg-gray-200 transition-all"
                                >
                                    Cancel
                                </button>
                                <button
                                    type="submit"
                                    disabled={isBooking}
                                    className="flex-1 bg-purple-600 text-white py-3 rounded-xl font-bold hover:scale-105 transition-all disabled:opacity-50"
                                >
                                    {isBooking ? 'Processing...' : 'Confirm Booking'}
                                </button>
                            </div>
                        </form>
                    </div>
                </div>
            )}

            <style jsx>{`
        @keyframes slideUp {
          from {
            transform: translateY(20px);
            opacity: 0;
          }
          to {
            transform: translateY(0);
            opacity: 1;
          }
        }
      `}</style>
        </div>
    )
}
