
/**
 * Utility functions for Transport Time & Fare Estimation
 */

export const TRAVEL_CONFIG = {
    // Traffic Multipliers as per IMPLEMENTATION_SUMMARY.md
    factors: {
        bus: 1.5,       // Adjusts for loading/unloading and traffic
        car: 1.0,       // OSRM base duration is for private car
        metro: 1.0,     // Metro is unaffected by traffic
        rickshaw: 1.2,  // slow but bypasses jams
        walking: 1.0
    },
    fare_rate_bus: 2.42, // BRTA 2024/25 guideline
    min_fare_bus: 10
};

interface TravelEstimate {
    time: number;       // in minutes
    fare: number;       // in BDT
    formattedTime: string;
}

/**
 * Calculates realistic travel time and fare based on mode
 * @param distanceKm Distance in Kilometers
 * @param osrmTimeMin The "ideal" driving time from OSRM (in minutes)
 * @param mode Transport mode
 */
export function calculateTripDetails(
    distanceKm: number,
    osrmTimeMin: number,
    mode: 'bus' | 'metro' | 'car' = 'bus'
): TravelEstimate {

    // 1. Time Calculation (Formula: Bus_Minutes = OSRM_Min * Multiplier)
    let estimatedTime = osrmTimeMin * (TRAVEL_CONFIG.factors[mode] || 1.0);

    if (mode === 'metro') {
        // Metro Logic: Fixed average speed as fallback
        const avgSpeed = 45;
        estimatedTime = (distanceKm / avgSpeed) * 60;
    }

    // 2. Fare Calculation (Formula: Max(10, KM * FareRate))
    let fare = 0;
    if (mode === 'bus') {
        fare = Math.ceil(distanceKm * TRAVEL_CONFIG.fare_rate_bus);
        if (fare < TRAVEL_CONFIG.min_fare_bus) fare = TRAVEL_CONFIG.min_fare_bus;
    } else if (mode === 'metro') {
        // Metro Example Rate: Base 20 + approx 5 Tk/km
        fare = Math.ceil(distanceKm * 5) + 20;
        if (fare < 20) fare = 20;
    }

    return {
        time: Math.round(estimatedTime),
        fare: fare,
        formattedTime: formatTime(estimatedTime)
    };
}

function formatTime(minutes: number): string {
    if (minutes < 60) return `${Math.round(minutes)} min`;
    const hrs = Math.floor(minutes / 60);
    const mins = Math.round(minutes % 60);
    return `${hrs} hr ${mins} min`;
}
