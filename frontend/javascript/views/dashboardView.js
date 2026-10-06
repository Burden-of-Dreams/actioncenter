"use strict";

import { fetchBookings } from "../../javaScript/bookingApi.js";


export function createDashboardView() {
    const section = document.createElement("section");
    section.id = "dashboard-view";

    section.innerHTML = `
        <h1>Booking dashboard</h1>
        <p id="booking-status" role="status">Loading bookings...</p>

        <table hidden>
            <thead>
                <tr>
                    <th>Customer</th>
                    <th>Date</th>
                    <th>Email</th>
                    <th>Phone</th>
                    <th>Total price</th>
                </tr>
            </thead>
            <tbody></tbody>
        </table>
    `;

    const status = section.querySelector("#booking-status");
    const table = section.querySelector("table");
    const tableBody = section.querySelector("tbody");

    async function displayBookings() {
        try {
            const bookings = await fetchBookings();

            bookings.sort((a, b) => a.date.localeCompare(b.date));

            if (bookings.length === 0) {
                status.textContent = "No bookings yet.";
                return;
            }

            for (const booking of bookings) {
                const row = document.createElement("tr");

                const values = [
                    booking.nameOfCustomer,
                    booking.date,
                    booking.emailOfCustomer,
                    booking.phoneNumber,
                    booking.totalPrice
                ];

                for (const value of values) {
                    const cell = document.createElement("td");
                    cell.textContent = value ?? "—";
                    row.appendChild(cell);
                }

                tableBody.appendChild(row);
            }

            status.textContent = "";
            table.hidden = false;
        } catch (error) {
            status.textContent = "Could not load bookings. Please try again.";
            console.error(error);
        }
    }

    displayBookings();

    return section;
}