"use strict";

const API_BASE = "/api";

export async function fetchBookings() {
    const response = await fetch(API_BASE + "/bookings");
    if (!response.ok) {
        throw new Error("HTTP " + response.status);
    }
    return response.json();
}

async function createBooking(booking) {
    const response = await fetch(API_BASE + "/bookings",
        {
            method: POST,
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(booking)
        });
    if (!response.ok()) {
        throw new Error("HTTP " + response.status);
        }
    return await response.json();
}