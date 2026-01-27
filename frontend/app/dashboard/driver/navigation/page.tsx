"use client"

import { useRouter } from "next/navigation"
import { useEffect, useState } from "react"

export default function DriverNavigation() {
    const router = useRouter()
    const [mounted, setMounted] = useState(false)
    const [currentStop, setCurrentStop] = useState(0)
    const [completedStops, setCompletedStops] = useState<number[]>([])

    const tripData = {
        route: "Savar ⇄ Gabtoli",
        busId: "PJ-401",
        stops: ["Savar", "Hemayetpur", "Aminbazar", "Gabtoli"]
    }

    useEffect(() => {
        setMounted(true)
        const role = localStorage.getItem('userRole')
        if (!role || (role !== 'driver' && role !== 'owner')) {
            router.push('/')
        }
    }, [router])

    const handleArrived = (index: number) => {
        if (!completedStops.includes(index)) {
            setCompletedStops([...completedStops, index])
            if (index < tripData.stops.length - 1) {
                setCurrentStop(index + 1)
            }
        }
    }

    const handleFinish = () => {
        if (confirm('Have you safely reached the final destination?')) {
            alert('🏆 Excellent! Trip assignment completed.\nYour performance rating and earnings have been updated.')
            router.push('/dashboard/driver')
        }
    }

    if (!mounted) return null

    return (
        <div className="min-h-screen bg-[#f8fafc] p-5">
            <div className="max-w-3xl mx-auto">
                {/* Header */}
                <div className="bg-white/80 backdrop-blur-sm rounded-3xl p-8 shadow-xl mb-8 flex justify-between items-center border border-white/30">
                    <div>
                        <h1 className="text-3xl font-extrabold text-[#7c3aed] mb-2">{tripData.route}</h1>
                        <p className="text-gray-600 font-semibold flex items-center gap-2">
                            <span className="opacity-60">🚌</span> Bus ID: {tripData.busId}
                        </p>
                    </div>
                    <button
                        onClick={() => router.push('/dashboard/driver')}
                        className="px-6 py-3 rounded-2xl border-none bg-white text-gray-700 cursor-pointer font-bold transition-all shadow-lg hover:bg-gray-50 hover:-translate-x-1 flex items-center gap-2"
                    >
                        <span>🔙</span> Dashboard
                    </button>
                </div>

                {/* Route Flow */}
                <div className="relative pl-12">
                    {/* Vertical Line */}
                    <div className="absolute left-3.5 top-2.5 bottom-2.5 w-1 bg-gray-200 rounded"></div>

                    {tripData.stops.map((stop, index) => {
                        const isCompleted = completedStops.includes(index)
                        const isActive = currentStop === index && !isCompleted

                        return (
                            <div
                                key={index}
                                className={`relative mb-9 bg-white rounded-[28px] p-7 shadow-lg transition-all duration-300 border ${isActive ? 'border-2 border-[#7c3aed] scale-105 shadow-2xl' : 'border border-gray-100'
                                    } ${isCompleted ? 'opacity-50 scale-95' : ''}`}
                            >
                                {/* Stop Marker */}
                                <div className={`absolute -left-[43px] top-7 w-5 h-5 rounded-full border-[5px] z-10 transition-all ${isActive
                                        ? 'border-[#7c3aed] bg-[#7c3aed] shadow-[0_0_0_6px_rgba(124,58,237,0.1)]'
                                        : isCompleted
                                            ? 'border-green-500 bg-green-500'
                                            : 'border-gray-300 bg-white'
                                    }`}></div>

                                {/* Stop Name */}
                                <div className="flex justify-between items-center mb-5">
                                    <h2 className="text-2xl font-extrabold text-gray-800">{stop}</h2>
                                    <span className="bg-purple-100 text-[#7c3aed] px-4 py-1.5 rounded-xl text-xs font-extrabold">
                                        POINT #{index + 1}
                                    </span>
                                </div>

                                {/* Passenger Analytics */}
                                <div className="grid grid-cols-3 gap-4 mb-5">
                                    {[
                                        { radius: '1km', count: Math.floor(Math.random() * 20) + 5, time: Math.floor(Math.random() * 5) + 2 },
                                        { radius: '2km', count: Math.floor(Math.random() * 60) + 20, time: Math.floor(Math.random() * 10) + 6 },
                                        { radius: '4km', count: Math.floor(Math.random() * 200) + 100, time: Math.floor(Math.random() * 15) + 15 }
                                    ].map((data, i) => (
                                        <div key={i} className="bg-gray-50 rounded-2xl p-4 text-center border border-gray-100 hover:bg-gray-100 transition-all">
                                            <span className="block text-xs font-bold text-gray-500 mb-1 uppercase tracking-wide">
                                                {data.radius} Radius
                                            </span>
                                            <div className={`text-2xl font-black ${data.count > 40 ? 'text-green-600' : data.count > 15 ? 'text-orange-500' : 'text-gray-800'}`}>
                                                {data.count}
                                            </div>
                                            <span className="text-xs text-gray-600 font-semibold">Active People</span>
                                            <div className="text-xs text-[#7c3aed] font-extrabold mt-2 pt-2 border-t border-dashed border-gray-200">
                                                ⏱ Est: {data.time} min
                                            </div>
                                        </div>
                                    ))}
                                </div>

                                {/* Action Button */}
                                <button
                                    onClick={() => handleArrived(index)}
                                    disabled={isCompleted}
                                    className={`w-full py-4 rounded-2xl font-extrabold transition-all flex items-center justify-center gap-2 ${isCompleted
                                            ? 'bg-green-600 text-white cursor-default'
                                            : isActive
                                                ? 'bg-gray-100 text-gray-700 hover:bg-gray-200'
                                                : 'bg-gray-100 text-gray-400'
                                        }`}
                                >
                                    {isCompleted ? '✅ Stop Completed' : index === 0 && currentStop === 0 ? '📍 Currently Here' : 'Mark as Arrived'}
                                </button>
                            </div>
                        )
                    })}
                </div>

                {/* Finish Button */}
                <div className="mt-12 text-center">
                    <button
                        onClick={handleFinish}
                        className="bg-gradient-to-r from-[#7c3aed] to-[#6d28d9] text-white px-12 py-5 text-xl font-bold rounded-2xl hover:-translate-y-1 hover:shadow-2xl transition-all shadow-[0_10px_15px_-3px_rgba(124,58,237,0.4)]"
                    >
                        🏁 Finish Trip Assignment
                    </button>
                </div>
            </div>
        </div>
    )
}
