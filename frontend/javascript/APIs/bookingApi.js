"use strict";

const API_BASE = "/api";

export async function fetchBookings() {
    const response = await fetch(API_BASE + "/bookings");
    if (!response.ok) {
        throw new Error("HTTP " + response.status);
    }
    return response.json();
}

export async function createBooking(booking) {
    const response = await fetch(API_BASE + "/bookings",
        {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(booking)
        });
    if (!response.ok) {
        const error = new Error("HTTP " + response.status);
        error.status = response.status;
        throw error;
    }
    return await response.json();
}

// Slet booking
export async function deleteBooking(bookingId) {
    const response = await fetch(API_BASE + "/bookings/delete/" + bookingId, {
        method: "DELETE"
    });

    if (!response.ok) {
        throw new Error("HTTP " + response.status);
    }
}

// Gem ændringer til booking
export async function updateBooking(bookingId, booking) {
    const response = await fetch(API_BASE + "/bookings/" + bookingId, {
        method: "PATCH",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(booking)
    });

    if (!response.ok) {
        throw new Error("HTTP " + response.status);
    }
    return response.json();
}


// Rediger session
export async function updateBookingSession(bookingId, sessionId, session) {
    const response = await fetch(API_BASE + "/bookings/" + bookingId + "/sessions/" + sessionId, {
        method: "PUT",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(session)
    });
    if (!response.ok) {
        const error = new Error("HTTP " + response.status);
        error.status = response.status;
        throw error;
    }
    return response.json();
}

// Slet session og modtag den opdaterede booking
export async function deleteBookingSession(bookingId, sessionId) {
    const response = await fetch(API_BASE + "/bookings/" + bookingId + "/sessions/" + sessionId, {
        method: "DELETE"
    });
    if (!response.ok) {
        const error = new Error("HTTP " + response.status);
        error.status = response.status;
        throw error;
    }
    return response.json();
}

