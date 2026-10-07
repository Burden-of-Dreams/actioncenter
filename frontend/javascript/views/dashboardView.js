"use strict";

import { fetchBookings } from "../APIs/bookingApi.js";


export function createDashboardView({ onNewBooking }) {
    const section = document.createElement("section");
    section.id = "dashboard-view";

    section.innerHTML = `

        <div class="view-header">
            <h1>Alle bookings</h1>
            <button id="new-booking-button" type="button">+ Ny booking</button>
        </div>
        
        <input
        id="booking-search"
        type="search"
        placeholder= "Søg efter id, navn, mail eller telefon..."
        aria-label="Search bookings"
        >
        
        <p id="booking-status" role="status">Loader bookings...</p>

        <table hidden>
            <thead>
                <tr>
                    <th>ID</th>
                    <th>Kundenavn</th>
                    <th>Dato</th>
                    <th>Email</th>
                    <th>Telefon</th>
                    <th>Total pris</th>
                </tr>
            </thead>
            <tbody></tbody>
        </table>
    `;

    const status = section.querySelector("#booking-status");
    const table = section.querySelector("table");
    const tableBody = section.querySelector("tbody");
    const searchInput = section.querySelector("#booking-search");
    const newBookingButton = section.querySelector("#new-booking-button");

    newBookingButton.addEventListener("click", onNewBooking);

    //SEARCH BAR
    searchInput.addEventListener("input", () => {
        const search = searchInput.value.trim().toLowerCase();

        for (const row of tableBody.rows) {
            const id = row.cells[0].textContent;
            const name = row.cells[1].textContent;
            const email = row.cells[3].textContent;
            const phone = row.cells[4].textContent;

            const bookingText = `${id} ${name} ${email} ${phone}`.toLowerCase();

            row.hidden = !bookingText.includes(search);
        }
    });

    //SHOW BOOKINGS
    async function displayBookings() {
        try {
            const bookings = await fetchBookings();

            bookings.sort((a, b) => a.date.localeCompare(b.date));

            if (bookings.length === 0) {
                status.textContent = "Ingen bookinger endnu.";
                return;
            }

            for (const booking of bookings) {
                const row = document.createElement("tr");

                const values = [
                    booking.id,
                    booking.nameOfCustomer,
                    booking.date,
                    booking.emailOfCustomer,
                    booking.phoneNumber,
                    booking.totalPrice + " kr."
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
            status.textContent = "Kan ikke loade bookinger. Prøv venligst igen.";
            console.error(error);
        }
    }

    displayBookings();

    return section;
}