export interface User {
    id: string
    email: string
    name: string
    role: 'passenger' | 'driver' | 'owner'
    phone?: string
    created_at: string
}

export interface Bus {
    id: string
    bus_number: string
    owner_id: string
    driver_id?: string
    route_id?: string
    status: 'active' | 'inactive' | 'maintenance'
}

export interface Route {
    id: string
    name: string
    stops: string[]
    distance: number
    base_fare: number
    estimated_time: number
}

export interface Trip {
    id: string
    route_id: string
    bus_id: string
    driver_id: string
    status: 'pending' | 'active' | 'completed' | 'cancelled'
    started_at?: string
    completed_at?: string
}

export interface Rating {
    id: string
    trip_id: string
    user_id: string
    rating: number
    comment?: string
    created_at: string
}
