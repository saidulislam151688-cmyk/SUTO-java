/**
 * Java Backend API Client
 * Replaces Supabase - All calls go to Java Spring Boot backend
 */

const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080/api';

interface SignupData {
    email: string;
    password: string;
    fullName: string;
    role: 'passenger' | 'driver' | 'owner';
}

interface LoginData {
    email: string;
    password: string;
}

interface AuthResponse {
    token: string;
    role: string;
    message: string;
}

interface RouteRequest {
    origin: string;
    destination: string;
}

class ApiClient {
    private token: string | null = null;

    constructor() {
        // Load token from localStorage if available
        if (typeof window !== 'undefined') {
            this.token = localStorage.getItem('auth_token');
        }
    }

    private async request<T>(
        endpoint: string,
        options: RequestInit = {}
    ): Promise<T> {
        const headers: HeadersInit = {
            'Content-Type': 'application/json',
            ...options.headers,
        };

        if (this.token) {
            headers['Authorization'] = `Bearer ${this.token}`;
        }

        const response = await fetch(`${API_BASE_URL}${endpoint}`, {
            ...options,
            headers,
        });

        if (!response.ok) {
            const error = await response.text();
            throw new Error(error || `HTTP error! status: ${response.status}`);
        }

        return response.json();
    }

    // Auth methods
    async signup(data: SignupData): Promise<AuthResponse> {
        const response = await this.request<AuthResponse>('/auth/signup', {
            method: 'POST',
            body: JSON.stringify(data),
        });

        // Save token
        this.token = response.token;
        if (typeof window !== 'undefined') {
            localStorage.setItem('auth_token', response.token);
            localStorage.setItem('user_role', response.role);
        }

        return response;
    }

    async login(data: LoginData): Promise<AuthResponse> {
        const response = await this.request<AuthResponse>('/auth/login', {
            method: 'POST',
            body: JSON.stringify(data),
        });

        // Save token
        this.token = response.token;
        if (typeof window !== 'undefined') {
            localStorage.setItem('auth_token', response.token);
            localStorage.setItem('user_role', response.role);
        }

        return response;
    }

    logout() {
        this.token = null;
        if (typeof window !== 'undefined') {
            localStorage.removeItem('auth_token');
            localStorage.removeItem('user_role');
        }
    }

    getToken(): string | null {
        return this.token;
    }

    isAuthenticated(): boolean {
        return !!this.token;
    }

    getUserRole(): string | null {
        if (typeof window !== 'undefined') {
            return localStorage.getItem('user_role');
        }
        return null;
    }

    // Route methods
    async findRoute(data: RouteRequest): Promise<any> {
        return this.request('/routes/find', {
            method: 'POST',
            body: JSON.stringify(data),
        });
    }

    // User methods
    async getCurrentUser() {
        return this.request('/users/me', {
            method: 'GET',
        });
    }

    async getUsersByRole(role: string) {
        return this.request(`/users/role/${role}`, {
            method: 'GET',
        });
    }

    // Booking methods
    async createBooking(data: any) {
        return this.request('/bookings', {
            method: 'POST',
            body: JSON.stringify(data),
        });
    }

    async getUserBookings() {
        return this.request('/bookings/user', {
            method: 'GET',
        });
    }

    async getDriverBookings() {
        return this.request('/bookings/driver', {
            method: 'GET',
        });
    }

    async updateBookingStatus(id: string, status: string) {
        return this.request(`/bookings/${id}/status`, {
            method: 'PUT',
            body: JSON.stringify({ status }),
        });
    }
}

// Export singleton instance
export const apiClient = new ApiClient();

// Export types
export type { SignupData, LoginData, AuthResponse, RouteRequest };
