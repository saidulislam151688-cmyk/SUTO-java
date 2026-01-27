import { NextResponse, type NextRequest } from 'next/server'

/**
 * Middleware for Java Backend Authentication
 * Checks for auth token and redirects if not authenticated
 */
export async function middleware(request: NextRequest) {
    const token = request.cookies.get('auth_token')?.value

    // Public paths that don't require authentication
    const publicPaths = ['/', '/login', '/signup']
    const isPublicPath = publicPaths.some(path => request.nextUrl.pathname.startsWith(path))

    // If trying to access protected route without token, redirect to login
    if (!isPublicPath && !token) {
        return NextResponse.redirect(new URL('/', request.url))
    }

    // If logged in and trying to access login/signup, redirect to dashboard
    if (token && (request.nextUrl.pathname === '/' || request.nextUrl.pathname === '/login')) {
        return NextResponse.redirect(new URL('/dashboard/passenger', request.url))
    }

    return NextResponse.next()
}

export const config = {
    matcher: [
        /*
         * Match all request paths except:
         * - _next/static (static files)
         * - _next/image (image optimization files)
         * - favicon.ico (favicon file)
         * - public folder
         */
        '/((?!_next/static|_next/image|favicon.ico|.*\\.(?:svg|png|jpg|jpeg|gif|webp)$).*)',
    ],
}
