"use client"

import { useRouter } from "next/navigation"
import { useEffect, useState, useRef } from "react"
import dynamic from 'next/dynamic'
import { apiClient } from "@/lib/api-client"

// Dynamically import LeafletMap to avoid SSR issues
const LeafletMap = dynamic(() => import('@/components/LeafletMap'), {
    ssr: false,
    loading: () => <div className="h-96 w-full bg-gray-100 animate-pulse rounded-2xl flex items-center justify-center text-gray-400">Loading Map...</div>
})

export default function PassengerDashboard() {
    const router = useRouter()
    const [mounted, setMounted] = useState(false)

    // Input states
    const [fromLocation, setFromLocation] = useState("")
    const [toLocation, setToLocation] = useState("")


    // View states
    const [showResults, setShowResults] = useState(false)
    const [showJourney, setShowJourney] = useState(false)
    const [selectedRoute, setSelectedRoute] = useState<any>(null)
    const [apiResult, setApiResult] = useState("")

    const [isLoading, setIsLoading] = useState(false)

    // Autocomplete states
    const [stops, setStops] = useState<string[]>([])
    const [fromSuggestions, setFromSuggestions] = useState<string[]>([])
    const [toSuggestions, setToSuggestions] = useState<string[]>([])
    const [showFromDropdown, setShowFromDropdown] = useState(false)
    const [showToDropdown, setShowToDropdown] = useState(false)

    useEffect(() => {
        fetch('/unique_stops.json')
            .then(res => res.json())
            .then(data => setStops(data))
            .catch(err => console.error("Failed to load stops:", err));
    }, []);

    // Refs for click outside
    const fromRef = useRef<HTMLDivElement>(null)
    const toRef = useRef<HTMLDivElement>(null)

    useEffect(() => {
        function handleClickOutside(event: MouseEvent) {
            if (fromRef.current && !fromRef.current.contains(event.target as Node)) {
                setShowFromDropdown(false)
            }
            if (toRef.current && !toRef.current.contains(event.target as Node)) {
                setShowToDropdown(false)
            }
        }

        document.addEventListener("mousedown", handleClickOutside)
        return () => {
            document.removeEventListener("mousedown", handleClickOutside)
        }
    }, [])

    // Filter logic
    const handleFromChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        const val = e.target.value
        setFromLocation(val)
        if (val.trim()) {
            const filtered = stops.filter(s => s.toLowerCase().includes(val.toLowerCase()))
            setFromSuggestions(filtered)
            setShowFromDropdown(true)
        } else {
            setShowFromDropdown(false)
        }
    }

    const selectFrom = (stop: string) => {
        setFromLocation(stop)
        setShowFromDropdown(false)
    }

    const handleToChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        const val = e.target.value
        setToLocation(val)
        if (val.trim()) {
            const filtered = stops.filter(s => s.toLowerCase().includes(val.toLowerCase()))
            setToSuggestions(filtered)
            setShowToDropdown(true)
        } else {
            setShowToDropdown(false)
        }
    }

    const selectTo = (stop: string) => {
        setToLocation(stop)
        setShowToDropdown(false)
    }


    // Chatbot states
    const [showChat, setShowChat] = useState(false)
    const [chatMessages, setChatMessages] = useState([
        { text: "<strong>🚀 সিস্টেম ওয়ার্কফ্লো:</strong><br/><br/>👤 <strong>ইউজার ইনপুট</strong><br/>⬇️<br/>🤖 <strong>AI & MCP সার্ভার</strong> (এনালাইসিস)<br/>⬇️<br/>🗺️ <strong>Google Maps API</strong> (লাইভ ট্রাফিক)<br/>⬇️<br/>⚙️ <strong>Routing Logic</strong> (৳ ২.৫/কিমি)<br/>⬇️<br/>✅ <strong>সেরা রুট ফলাফল</strong>", sender: "bot" }
    ])
    const [chatInput, setChatInput] = useState("")
    const [isTyping, setIsTyping] = useState(false)

    useEffect(() => {
        setMounted(true)
    }, [router])

    const handleLogout = () => {
        localStorage.clear()
        router.push('/')
    }



    const handleFindRoutes = async () => {
        if (!fromLocation || !toLocation) {
            alert("Please select both From and To locations")
            return
        }

        // Validate if inputs match a valid stop (case-insensitive check)
        const validFrom = stops.find(s => s.toLowerCase() === fromLocation.toLowerCase())
        const validTo = stops.find(s => s.toLowerCase() === toLocation.toLowerCase())

        if (!validFrom || !validTo) {
            alert("Please select valid locations from the list.")
            return
        }

        // Use the exact case from the list
        setFromLocation(validFrom)
        setToLocation(validTo)

        setIsLoading(true)
        setShowResults(true)
        setApiResult("")

        try {
            // Call API with correct object structure
            const response = await apiClient.findRoute({
                origin: fromLocation,
                destination: toLocation
            })

            // Backend now returns the correct structure (directRoutes, combinedRoutes)
            // But the frontend expects snake_case keys (direct_routes, combined_routes) 
            // from the Python version, whereas Java DTO uses camelCase.
            // Let's map it or ensure frontend handles camelCase.

            // Looking at the render logic below (lines 392+), it expects:
            // result.direct_routes || []

            // Java returns: directRoutes, combinedRoutes.
            // So we need a small mapping here to bridge Java camelCase to frontend snake_case expectation

            const mappedResult = {
                direct_routes: response.directRoutes || [],
                combined_routes: response.combinedRoutes ? response.combinedRoutes.map((r: any) => ({
                    ...r,
                    total_stops: r.totalStops,
                    total_steps: r.totalSteps
                })) : []
            }

            setApiResult(JSON.stringify(mappedResult))

        } catch (error: any) {
            setApiResult("Failed to calculate route: " + error.message)
            console.error(error)
        } finally {
            setIsLoading(false)
        }
    }

    const handleBack = () => {
        setShowResults(false)
        setShowJourney(false)
    }

    const handleStartJourney = (route: any) => {
        setSelectedRoute(route)
        setShowJourney(true)
    }

    const sendChatMessage = () => {
        if (!chatInput.trim()) return

        // Add user message
        setChatMessages([...chatMessages, { text: chatInput, sender: "user" }])
        const userMsg = chatInput
        setChatInput("")

        // Show typing indicator
        setIsTyping(true)

        // Simulate AI response
        setTimeout(() => {
            setIsTyping(false)
            let response = "আপনার প্রশ্নের উত্তর দিতে আমি এখানে আছি। আপনি রুট, ভাড়া, বা ট্রান্সপোর্ট সম্পর্কে যেকোনো প্রশ্ন করতে পারেন।"

            if (userMsg.toLowerCase().includes("route") || userMsg.toLowerCase().includes("রুট")) {
                response = "রুট খুঁজতে উপরের 'From' এবং 'To' লোকেশন সিলেক্ট করে 'FIND BEST ROUTES' বাটনে ক্লিক করুন। আমরা আপনার প্রায়োরিটি অনুযায়ী সেরা রুট দেখাবো!"
            } else if (userMsg.toLowerCase().includes("cost") || userMsg.toLowerCase().includes("ভাড়া")) {
                response = "আমাদের সিস্টেম সরকারি রেট (৳ ২.৫/কিমি) হিসেবে ভাড়া ক্যালকুলেট করে। সবচেয়ে সস্তা রুট পেতে 'Cost Priority' বাড়িয়ে দিন।"
            }

            setChatMessages(prev => [...prev, { text: response, sender: "bot" }])
        }, 1500)
    }

    const mockRoutes = [
        { rank: 1, path: "Farmgate → Bus 1 → Banani → Metro → Uttara", time: "35 min", cost: "৳ 60", reason: "Fast Multi-modal (Bus + Metro)" },
        { rank: 2, path: "Farmgate → Metro Rail → Uttara", time: "30 min", cost: "৳ 60", reason: "Direct Metro route" },
        { rank: 3, path: "Farmgate → Bus 2 → Mohakhali → Bus 3 → Uttara", time: "50 min", cost: "৳ 50", reason: "Reliable bus combination" }
    ]

    if (!mounted) return null

    return (
        <div className="min-h-screen bg-gradient-to-br from-[#667eea] to-[#764ba2] p-5 relative">
            <div className="max-w-4xl mx-auto">
                {/* Role Bar */}
                <div className="bg-white rounded-2xl px-8 py-4 shadow-lg mb-5 flex justify-between items-center">
                    <div className="flex items-center gap-3">
                        <span className="font-semibold text-gray-700">👤 User Mode:</span>
                        <span className="bg-indigo-100 text-indigo-600 px-3 py-1 rounded-full text-sm font-bold">
                            {typeof window !== 'undefined' && localStorage.getItem('userRole') || 'Passenger'}
                        </span>
                    </div>
                    <div className="flex gap-3">
                        <button
                            onClick={() => router.push('/dashboard/driver')}
                            className="bg-green-100 text-green-600 px-6 py-2 rounded-xl font-semibold hover:bg-green-200 transition-all"
                        >
                            Switch to Driver
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

                {/* Header */}
                <div className="text-center text-white mb-8">
                    <h1 className="text-4xl font-bold mb-2">🚍 Smart Transport Route Finder</h1>
                    <p className="text-xl opacity-90">Find the best route for your journey!</p>
                </div>

                {/* Input Section */}
                {!showResults && !showJourney && (
                    <div className="bg-white rounded-3xl p-10 shadow-2xl">
                        <div className="mb-8">
                            <label className="block text-xl font-semibold text-gray-800 mb-4">📍 Select Locations</label>
                            <div className="grid grid-cols-1 md:grid-cols-2 gap-5">
                                <div className="relative" ref={fromRef}>
                                    <label className="block text-sm text-gray-600 mb-2">From</label>
                                    <input
                                        type="text"
                                        value={fromLocation}
                                        onChange={handleFromChange}
                                        onFocus={() => {
                                            if (!fromLocation) {
                                                setFromSuggestions(stops)
                                                setShowFromDropdown(true)
                                            } else {
                                                handleFromChange({ target: { value: fromLocation } } as React.ChangeEvent<HTMLInputElement>)
                                            }
                                        }}
                                        placeholder="Type to search..."
                                        className="w-full p-4 text-lg border-2 border-gray-200 rounded-xl focus:border-[#667eea] outline-none transition-all"
                                    />
                                    {showFromDropdown && (
                                        <ul className="absolute z-10 w-full bg-white border border-gray-200 rounded-xl mt-1 max-h-60 overflow-y-auto shadow-lg">
                                            {fromSuggestions.length > 0 ? (
                                                fromSuggestions.map(stop => (
                                                    <li
                                                        key={stop}
                                                        onClick={() => selectFrom(stop)}
                                                        className="p-3 hover:bg-gray-100 cursor-pointer text-gray-700"
                                                    >
                                                        {stop}
                                                    </li>
                                                ))
                                            ) : (
                                                <li className="p-3 text-gray-400">No matching stops found</li>
                                            )}
                                        </ul>
                                    )}
                                </div>
                                <div className="relative" ref={toRef}>
                                    <label className="block text-sm text-gray-600 mb-2">To</label>
                                    <input
                                        type="text"
                                        value={toLocation}
                                        onChange={handleToChange}
                                        onFocus={() => {
                                            if (!toLocation) {
                                                setToSuggestions(stops)
                                                setShowToDropdown(true)
                                            } else {
                                                handleToChange({ target: { value: toLocation } } as React.ChangeEvent<HTMLInputElement>)
                                            }
                                        }}
                                        placeholder="Type to search..."
                                        className="w-full p-4 text-lg border-2 border-gray-200 rounded-xl focus:border-[#667eea] outline-none transition-all"
                                    />
                                    {showToDropdown && (
                                        <ul className="absolute z-10 w-full bg-white border border-gray-200 rounded-xl mt-1 max-h-60 overflow-y-auto shadow-lg">
                                            {toSuggestions.length > 0 ? (
                                                toSuggestions.map(stop => (
                                                    <li
                                                        key={stop}
                                                        onClick={() => selectTo(stop)}
                                                        className="p-3 hover:bg-gray-100 cursor-pointer text-gray-700"
                                                    >
                                                        {stop}
                                                    </li>
                                                ))
                                            ) : (
                                                <li className="p-3 text-gray-400">No matching stops found</li>
                                            )}
                                        </ul>
                                    )}
                                </div>
                            </div>
                        </div>





                        <button
                            onClick={handleFindRoutes}
                            className="w-full py-5 mt-5 bg-gradient-to-r from-[#667eea] to-[#764ba2] text-white text-xl font-bold rounded-2xl transition-all hover:-translate-y-1 hover:shadow-xl"
                        >
                            🔍 FIND BEST ROUTES
                        </button>
                    </div>
                )}

                {/* Results Section */}
                {showResults && !showJourney && (
                    <div>
                        <button
                            onClick={handleBack}
                            className="mb-5 px-8 py-4 bg-gradient-to-r from-[#764ba2] to-[#667eea] text-white font-bold rounded-2xl hover:-translate-y-1 transition-all"
                        >
                            ← Back to Input
                        </button>

                        <div className="bg-white rounded-3xl p-8 shadow-2xl">
                            <div className="bg-gradient-to-r from-[#667eea] to-[#764ba2] text-white p-6 rounded-2xl mb-6">
                                <h2 className="text-2xl font-bold mb-3">🗺️ Your Recommended Routes</h2>
                                <div className="flex gap-8 text-sm">
                                    <span><strong>From:</strong> {fromLocation}</span>
                                    <span><strong>To:</strong> {toLocation}</span>
                                </div>

                            </div>


                            {isLoading ? (
                                <div className="text-center p-10">
                                    <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-[#667eea] mx-auto mb-4"></div>
                                    <p className="text-gray-600 font-semibold">Calculating best routes across Dhaka...</p>
                                </div>
                            ) : apiResult ? (
                                (() => {
                                    let result;
                                    try {
                                        result = JSON.parse(apiResult);
                                    } catch (e) {
                                        return <pre className="whitespace-pre-wrap text-sm text-red-500">{apiResult}</pre>;
                                    }

                                    const directRoutes = result.direct_routes || [];
                                    const combinedRoutes = result.combined_routes || [];

                                    return (
                                        <div className="space-y-8">
                                            {/* Direct Routes Section */}
                                            {directRoutes.length > 0 && (
                                                <div className="bg-white rounded-2xl shadow-md p-6 border-l-8 border-green-500">
                                                    <h3 className="text-2xl font-bold text-gray-800 mb-4 flex items-center gap-2">
                                                        🚀 Direct Routes <span className="text-sm font-normal text-white bg-green-500 px-3 py-1 rounded-full">Best Choice</span>
                                                    </h3>
                                                    <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                                                        {directRoutes.map((route: any, idx: number) => (
                                                            <div key={idx} className="flex items-center justify-between bg-green-50 p-4 rounded-xl border border-green-100 hover:shadow-lg transition-all">
                                                                <div className="flex items-center gap-3">
                                                                    <span className="text-3xl">{route.type === 'METRO' ? '🚆' : '🚌'}</span>
                                                                    <div>
                                                                        <h4 className="font-bold text-lg text-gray-800">{route.name}</h4>
                                                                        <p className="text-sm text-gray-600">{route.stops} Stops</p>
                                                                    </div>
                                                                </div>
                                                                <span className="bg-green-200 text-green-800 text-xs font-bold px-3 py-1 rounded-lg">DIRECT</span>
                                                            </div>
                                                        ))}
                                                    </div>
                                                </div>
                                            )}

                                            {/* Combined Routes Section */}
                                            {combinedRoutes.length > 0 && (
                                                <div className="bg-white rounded-2xl shadow-md p-6 border-l-8 border-indigo-500">
                                                    <h3 className="text-2xl font-bold text-gray-800 mb-6 flex items-center gap-2">
                                                        🔀 Multi-modal Routes <span className="text-sm font-normal text-gray-500 bg-gray-100 px-3 py-1 rounded-full">{combinedRoutes.length} Options</span>
                                                    </h3>
                                                    <div className="space-y-6">
                                                        {combinedRoutes.map((route: any, idx: number) => (
                                                            <div key={idx} className="bg-gray-50 rounded-xl p-5 border border-gray-200 hover:border-[#667eea] transition-all">
                                                                <div className="flex justify-between items-center mb-4 border-b pb-2">
                                                                    <h4 className="font-bold text-gray-700">Option {idx + 1}</h4>
                                                                    <div className="flex gap-3 text-sm">
                                                                        <span className="bg-blue-100 text-blue-700 px-3 py-1 rounded-lg font-bold">⏱ {route.total_steps} Steps</span>
                                                                        <span className="bg-orange-100 text-orange-700 px-3 py-1 rounded-lg font-bold">🛑 {route.total_stops} Stops</span>
                                                                    </div>
                                                                </div>

                                                                <div className="relative pl-4 border-l-2 border-dashed border-indigo-300 space-y-6">
                                                                    {route.legs.map((leg: any, legIdx: number) => (
                                                                        <div key={legIdx} className="relative">
                                                                            <div className="absolute -left-[21px] top-0 w-4 h-4 rounded-full bg-indigo-500 border-2 border-white"></div>
                                                                            <div className="bg-white p-3 rounded-lg shadow-sm border border-gray-100">
                                                                                <div className="flex items-center gap-2 mb-1">
                                                                                    <span className="text-xl">{leg.transport_mode === 'METRO' ? '🚆' : '🚌'}</span>
                                                                                    <span className="font-bold text-gray-800">{leg.from}</span>
                                                                                    <span className="text-gray-400">➜</span>
                                                                                    <span className="font-bold text-gray-800">{leg.to}</span>
                                                                                </div>
                                                                                <p className="text-sm font-semibold text-gray-700 mb-2">Mode ({leg.options.length} options):</p>
                                                                                <div className="flex gap-2 flex-wrap max-h-40 overflow-y-auto">
                                                                                    {leg.options.map((opt: string, i: number) => (
                                                                                        <span key={i} className="text-xs bg-gray-100 text-gray-700 px-2 py-1 rounded-md border border-gray-200 shadow-sm">{opt}</span>
                                                                                    ))}
                                                                                </div>
                                                                            </div>
                                                                        </div>
                                                                    ))}
                                                                </div>
                                                            </div>
                                                        ))}
                                                    </div>
                                                </div>
                                            )}

                                            {directRoutes.length === 0 && combinedRoutes.length === 0 && (
                                                <div className="text-center p-8 bg-yellow-50 rounded-2xl border border-yellow-200 text-yellow-800">
                                                    😕 No routes found. Try different locations.
                                                </div>
                                            )}
                                        </div>
                                    );
                                })()
                            ) : (
                                mockRoutes.map((route) => (
                                    <div key={route.rank} className="bg-white border-2 border-gray-200 rounded-2xl p-6 mb-4 hover:border-[#667eea] transition-all">
                                        <div className="flex items-start justify-between mb-4">
                                            <div className="flex items-center gap-3">
                                                <span className="text-4xl">
                                                    {route.rank === 1 ? "🥇" : route.rank === 2 ? "🥈" : "🥉"}
                                                </span>
                                                <div>
                                                    <h3 className="text-xl font-bold text-gray-800">Route #{route.rank}</h3>
                                                    <p className="text-gray-600">{route.path}</p>
                                                </div>
                                            </div>
                                        </div>
                                        <div className="grid grid-cols-3 gap-4 mb-4">
                                            <div className="bg-blue-50 p-3 rounded-xl text-center">
                                                <div className="text-sm text-gray-600">Time</div>
                                                <div className="text-lg font-bold text-blue-600">{route.time}</div>
                                            </div>
                                            <div className="bg-green-50 p-3 rounded-xl text-center">
                                                <div className="text-sm text-gray-600">Cost</div>
                                                <div className="text-lg font-bold text-green-600">{route.cost}</div>
                                            </div>
                                            <div className="bg-purple-50 p-3 rounded-xl text-center">
                                                <div className="text-sm text-gray-600">Type</div>
                                                <div className="text-sm font-bold text-purple-600">{route.reason}</div>
                                            </div>
                                        </div>
                                        <button
                                            onClick={() => handleStartJourney(route)}
                                            className="w-full py-3 bg-gradient-to-r from-[#667eea] to-[#764ba2] text-white font-bold rounded-xl hover:shadow-lg transition-all"
                                        >
                                            🚀 Start Journey
                                        </button>
                                    </div>
                                )))}
                        </div>
                    </div>
                )}

                {/* Live Journey Tracking */}
                {showJourney && (
                    <div>
                        <button
                            onClick={() => setShowJourney(false)}
                            className="mb-5 px-8 py-4 bg-gradient-to-r from-[#764ba2] to-[#667eea] text-white font-bold rounded-2xl hover:-translate-y-1 transition-all"
                        >
                            ← Back to Routes
                        </button>

                        <div className="bg-white rounded-3xl p-0 overflow-hidden shadow-2xl mb-5">
                            <div className="bg-gradient-to-r from-[#667eea] to-[#764ba2] text-white p-6">
                                <h2 className="text-2xl font-bold">🚍 Live Journey Tracking</h2>
                                <p className="mt-2"><strong>Route:</strong> {selectedRoute?.path}</p>
                            </div>

                            {/* Real Leaflet Map */}
                            <div className="relative h-96 bg-gray-100 rounded-2xl overflow-hidden shadow-inner">
                                <LeafletMap
                                    center={[23.7612, 90.3907]} // Farmgate approx
                                    zoom={12}
                                    markers={[
                                        { id: '1', lat: 23.7612, lng: 90.3907, title: 'Farmgate', description: 'Start Location' },
                                        { id: '2', lat: 23.8759, lng: 90.3795, title: 'Uttara', description: 'Destination' }
                                    ]}
                                    polylines={[
                                        {
                                            positions: [
                                                [23.7612, 90.3907], // Farmgate
                                                [23.7937, 90.4066], // Banani
                                                [23.8759, 90.3795]  // Uttara
                                            ],
                                            color: 'blue'
                                        }
                                    ]}
                                />
                            </div>
                        </div>

                        <div className="bg-white rounded-3xl p-8 shadow-2xl">
                            <h3 className="text-2xl font-bold text-gray-800 mb-5">📍 Journey Steps</h3>

                            <div className="space-y-4 mb-6">
                                <div className="bg-green-50 border-l-4 border-green-500 p-5 rounded-xl">
                                    <div className="flex items-center gap-3 mb-2">
                                        <span className="w-8 h-8 bg-green-600 text-white rounded-full flex items-center justify-center font-bold">1</span>
                                        <h4 className="text-lg font-bold text-gray-800">Bus 1: Farmgate → Banani</h4>
                                    </div>

                                    {/* Star Rating Section */}
                                    <div className="mb-4">
                                        <div className="flex flex-col items-center p-3 bg-white/50 rounded-lg">
                                            <span className="text-xs text-gray-500 font-semibold mb-1">Rate this segment:</span>
                                            <div className="flex gap-1">
                                                {[1, 2, 3, 4, 5].map((star) => (
                                                    <span
                                                        key={star}
                                                        className="text-2xl cursor-pointer hover:scale-125 transition-transform text-yellow-400"
                                                        onClick={() => alert(`You rated this segment ${star} stars! ⭐`)}
                                                    >
                                                        ★
                                                    </span>
                                                ))}
                                            </div>
                                        </div>
                                    </div>

                                    <div className="grid grid-cols-3 gap-3 mt-3">
                                        <div className="bg-white p-2 rounded-xl text-center shadow-sm">
                                            <div className="text-xl mb-1">⏱</div>
                                            <div className="text-xs text-gray-500 font-semibold uppercase">Time</div>
                                            <div className="font-bold text-gray-800">15 min</div>
                                        </div>
                                        <div className="bg-white p-2 rounded-xl text-center shadow-sm">
                                            <div className="text-xl mb-1">📏</div>
                                            <div className="text-xs text-gray-500 font-semibold uppercase">Distance</div>
                                            <div className="font-bold text-gray-800">10 km</div>
                                        </div>

                                        {/* Cost Box with Warning Popup */}
                                        <div className="bg-white p-2 rounded-xl text-center shadow-sm relative group">
                                            <button
                                                onClick={(e) => {
                                                    e.stopPropagation();
                                                    // Toggle popup logic
                                                    const popup = document.getElementById('warning-popup');
                                                    if (popup) popup.classList.toggle('hidden');
                                                }}
                                                className="absolute -top-2 -left-2 w-6 h-6 bg-red-100 text-red-500 border-2 border-red-500 rounded-full flex items-center justify-center text-xs font-bold hover:bg-red-500 hover:text-white hover:scale-110 transition-all shadow-md z-10"
                                                title="Report extra fare"
                                            >
                                                ⚠️
                                            </button>

                                            {/* Custom Popup Implementation */}
                                            <div
                                                id="warning-popup"
                                                className="hidden absolute bottom-full left-1/2 -translate-x-1/2 mb-3 w-64 bg-white p-4 rounded-xl shadow-2xl border border-gray-100 z-50 text-left animate-in fade-in slide-in-from-bottom-2"
                                            >
                                                <h5 className="flex items-center gap-2 font-bold text-gray-800 mb-2 border-b pb-2">
                                                    ⚠️ Report Extra Fare
                                                </h5>
                                                <p className="text-xs text-gray-600 mb-3 leading-relaxed">
                                                    বাস যদি সরকারি রেটের চেয়ে বেশি ভাড়া রাখে তবে এখানে জানান।
                                                </p>
                                                <textarea
                                                    className="w-full text-xs p-2 border border-gray-200 rounded-lg mb-3 focus:outline-none focus:border-red-400 focus:ring-1 focus:ring-red-400"
                                                    placeholder="কত টাকা বেশি নিয়েছে এবং কেন?"
                                                    rows={3}
                                                ></textarea>
                                                <button
                                                    onClick={() => {
                                                        const popup = document.getElementById('warning-popup');
                                                        if (popup) popup.classList.add('hidden');
                                                        alert("Report submitted successfully!");
                                                    }}
                                                    className="w-full bg-[#667eea] text-white py-2 rounded-lg text-xs font-bold hover:bg-[#5a6fd6] transition-colors"
                                                >
                                                    Report Issue
                                                </button>
                                                {/* Triangle Arrow */}
                                                <div className="absolute top-full left-1/2 -translate-x-1/2 -mt-1 border-8 border-transparent border-t-white"></div>
                                            </div>

                                            <div className="text-xl mb-1">💰</div>
                                            <div className="text-xs text-gray-500 font-semibold uppercase">Cost</div>
                                            <div className="font-bold text-gray-800">৳ 30</div>
                                        </div>
                                    </div>
                                </div>

                                <div className="bg-gray-50 border-l-4 border-gray-300 p-5 rounded-xl opacity-60">
                                    <div className="flex items-center gap-3 mb-2">
                                        <span className="w-8 h-8 bg-gray-400 text-white rounded-full flex items-center justify-center font-bold">2</span>
                                        <h4 className="text-lg font-bold text-gray-700">Metro: Banani → Uttara</h4>
                                    </div>
                                    <p className="text-sm text-gray-600 ml-11">Arriving soon...</p>
                                </div>
                            </div>

                            <div className="bg-gradient-to-r from-blue-50 to-purple-50 p-5 rounded-2xl mb-5">
                                <div className="grid grid-cols-3 gap-4">
                                    <div className="text-center">
                                        <div className="text-sm text-gray-600">Estimated Time</div>
                                        <div className="text-2xl font-bold text-blue-600">35 min</div>
                                    </div>
                                    <div className="text-center">
                                        <div className="text-sm text-gray-600">Total Cost</div>
                                        <div className="text-2xl font-bold text-green-600">৳ 60</div>
                                    </div>
                                    <div className="text-center">
                                        <div className="text-sm text-gray-600">Distance</div>
                                        <div className="text-2xl font-bold text-purple-600">18 km</div>
                                    </div>
                                </div>
                            </div>

                            <button
                                onClick={handleBack}
                                className="w-full py-4 bg-red-500 text-white font-bold rounded-xl hover:bg-red-600 transition-all"
                            >
                                🛑 End Journey
                            </button>
                        </div>
                    </div>
                )}
            </div>

            {/* AI Chatbot Button */}
            <button
                onClick={() => setShowChat(!showChat)}
                className="fixed bottom-8 right-8 w-16 h-16 bg-gradient-to-r from-[#667eea] to-[#764ba2] rounded-full shadow-2xl flex items-center justify-center text-3xl hover:scale-110 transition-all z-50"
            >
                💬
            </button>

            {/* AI Chatbot Window */}
            {showChat && (
                <div className="fixed bottom-28 right-8 w-96 h-[550px] bg-white rounded-3xl shadow-2xl flex flex-col z-50 animate-slideUp">
                    <div className="bg-gradient-to-r from-[#667eea] to-[#764ba2] text-white p-5 rounded-t-3xl flex justify-between items-center">
                        <h3 className="text-xl font-bold flex items-center gap-2">
                            🤖 AI Assistant
                        </h3>
                        <button
                            onClick={() => setShowChat(false)}
                            className="w-8 h-8 hover:bg-white/20 rounded-full flex items-center justify-center text-2xl transition-all"
                        >
                            ✕
                        </button>
                    </div>

                    <div className="flex-1 p-5 overflow-y-auto bg-gray-50">
                        {chatMessages.map((msg, idx) => (
                            <div key={idx} className={`mb-4 flex ${msg.sender === 'user' ? 'justify-end' : 'justify-start'}`}>
                                <div
                                    className={`max-w-[85%] p-4 rounded-2xl ${msg.sender === 'user'
                                        ? 'bg-gradient-to-r from-[#667eea] to-[#764ba2] text-white rounded-br-sm'
                                        : 'bg-white text-gray-800 shadow-md rounded-bl-sm'
                                        }`}
                                    dangerouslySetInnerHTML={{ __html: msg.text }}
                                />
                            </div>
                        ))}

                        {isTyping && (
                            <div className="flex justify-start mb-4">
                                <div className="bg-white p-4 rounded-2xl shadow-md flex gap-1">
                                    <div className="w-2 h-2 bg-[#667eea] rounded-full animate-bounce"></div>
                                    <div className="w-2 h-2 bg-[#667eea] rounded-full animate-bounce" style={{ animationDelay: '0.2s' }}></div>
                                    <div className="w-2 h-2 bg-[#667eea] rounded-full animate-bounce" style={{ animationDelay: '0.4s' }}></div>
                                </div>
                            </div>
                        )}
                    </div>

                    <div className="p-4 bg-white border-t border-gray-200 rounded-b-3xl flex gap-3">
                        <input
                            type="text"
                            value={chatInput}
                            onChange={(e) => setChatInput(e.target.value)}
                            onKeyPress={(e) => e.key === 'Enter' && sendChatMessage()}
                            placeholder="Type your message..."
                            className="flex-1 p-3 border-2 border-gray-200 rounded-full text-sm outline-none focus:border-[#667eea]"
                        />
                        <button
                            onClick={sendChatMessage}
                            className="w-12 h-12 bg-gradient-to-r from-[#667eea] to-[#764ba2] rounded-full flex items-center justify-center text-white text-xl hover:scale-110 transition-all"
                        >
                            ➤
                        </button>
                    </div>
                </div>
            )}

            <style jsx>{`
        .slider::-webkit-slider-thumb {
          -webkit-appearance: none;
          appearance: none;
          width: 24px;
          height: 24px;
          border-radius: 50%;
          background: #667eea;
          cursor: pointer;
          box-shadow: 0 2px 5px rgba(0,0,0,0.2);
        }
        
        .slider::-moz-range-thumb {
          width: 24px;
          height: 24px;
          border-radius: 50%;
          background: #667eea;
          cursor: pointer;
          box-shadow: 0 2px 5px rgba(0,0,0,0.2);
          border: none;
        }

        @keyframes slideUp {
          from {
            opacity: 0;
            transform: translateY(20px);
          }
          to {
            opacity: 1;
            transform: translateY(0);
          }
        }

        .animate-slideUp {
          animation: slideUp 0.3s ease;
        }
      `}</style>
        </div>
    )
}
