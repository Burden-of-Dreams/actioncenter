"use strict";


const API_BASE = "/api";

//TODO Skriv Export foran alle functions ift. at bruge dem i andre JS filer?

export async function fetchBookings() {
    const response = await fetch(API_BASE + "/bookings");
    if (!response.ok) {
        throw new Error("HTTP " + response.status);
    }
    return response.json();
}

async function loadBookings() {
    const statusMessage = document.querySelector("#status");
    try {
        const bookings = await fetchBookings()
        console.log(bookings);
        statusMessage.textContent = "";
    } catch (error) {
        console.error(error);
        statusMessage.textContent = "Could not load Bookings";
    }
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