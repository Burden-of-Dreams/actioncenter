"use strict";


const API_BASE = "/api";


async function fetchBookings() {
    const response = await fetch(API_BASE + "api/bookings/bookings/");
    if (!response.ok) {
        throw new Error("HTTP " + response.status);
    }
    await response.json()
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