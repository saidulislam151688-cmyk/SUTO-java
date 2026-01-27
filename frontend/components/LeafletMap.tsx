
'use client'

import { useEffect, useState } from 'react'
import { MapContainer, TileLayer, Marker, Popup, Polyline, useMap } from 'react-leaflet'
import 'leaflet/dist/leaflet.css'
import L from 'leaflet'

// Fix for default marker icons in Next.js
const iconUrl = 'https://unpkg.com/leaflet@1.7.1/dist/images/marker-icon.png'
const iconRetinaUrl = 'https://unpkg.com/leaflet@1.7.1/dist/images/marker-icon-2x.png'
const shadowUrl = 'https://unpkg.com/leaflet@1.7.1/dist/images/marker-shadow.png'

const customIcon = L.icon({
    iconUrl: iconUrl,
    iconRetinaUrl: iconRetinaUrl,
    shadowUrl: shadowUrl,
    iconSize: [25, 41],
    iconAnchor: [12, 41],
    popupAnchor: [1, -34],
    shadowSize: [41, 41]
})

// Dhaka Center
const DHAKA_CENTER: [number, number] = [23.8103, 90.4125]

// Component to update map center dynamically
function ChangeView({ center, zoom }: { center: [number, number], zoom: number }) {
    const map = useMap()
    useEffect(() => {
        map.setView(center, zoom)
    }, [center, zoom, map])
    return null
}

interface MapProps {
    center?: [number, number]
    zoom?: number
    markers?: Array<{
        id: string
        lat: number
        lng: number
        title: string
        description?: string
    }>
    polylines?: Array<{
        positions: [number, number][]
        color: string
    }>
}

export default function LeafletMap({ center = DHAKA_CENTER, zoom = 13, markers = [], polylines = [] }: MapProps) {
    return (
        <MapContainer
            center={center}
            zoom={zoom}
            style={{ height: '100%', width: '100%', borderRadius: '0.5rem' }}
        >
            <ChangeView center={center} zoom={zoom} />
            <TileLayer
                attribution='&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
                url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"
            />

            {markers.map((marker) => (
                <Marker key={marker.id} position={[marker.lat, marker.lng]} icon={customIcon}>
                    <Popup>
                        <div className="font-bold">{marker.title}</div>
                        {marker.description && <div className="text-sm">{marker.description}</div>}
                    </Popup>
                </Marker>
            ))}

            {polylines.map((line, idx) => (
                <Polyline key={idx} positions={line.positions} color={line.color} />
            ))}
        </MapContainer>
    )
}
