"use strict";

import { fetchBookings } from "../APIs/bookingApi.js";

// Opret dashboard
export function createDashboardView({ onNewBooking }) {
    // Dashboardets HTML
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
                    <th>Detaljer</th>
                </tr>
            </thead>
            <tbody></tbody>
        </table>
    `;

    // Hent dashboardets elementer
    const status = section.querySelector("#booking-status");
    const table = section.querySelector("table");
    const tableBody = section.querySelector("tbody");
    const searchInput = section.querySelector("#booking-search");
    const newBookingButton = section.querySelector("#new-booking-button");

    // Ny booking knap
    newBookingButton.addEventListener("click", onNewBooking);

    // Booking modal
    const dialog = document.createElement("dialog");
    dialog.className = "booking-details";
    dialog.setAttribute("aria-labelledby", "booking-details-title");
    dialog.innerHTML = `
        <div class="booking-details-header">
            <h2 id="booking-details-title">Bookingoplysninger</h2>
            <button type="button" class="booking-details-close" aria-label="Luk bookingoplysninger" autofocus>×</button>
        </div>
        <dl class="booking-details-fields"></dl>
    `;
    section.appendChild(dialog);

    const details = dialog.querySelector("dl");

    // Luk booking modal
    dialog.querySelector("button").addEventListener("click", () => dialog.close());

    // Søg efter bookings
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

    // Hent og vis liste af bookings
    async function displayBookings() {
        try {
            const bookings = await fetchBookings();

            // Sortér bookings efter dato
            bookings.sort((a, b) => a.date.localeCompare(b.date));

            if (bookings.length === 0) {
                status.textContent = "Ingen bookinger endnu.";
                return;
            }

            // Opret en række for hver booking
            for (const booking of bookings) {
                const row = document.createElement("tr");

                // Bookingens oplysninger
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

                // Booking visningsknap
                const actionCell = document.createElement("td");
                const viewButton = document.createElement("button");

                viewButton.type = "button";
                viewButton.className = "booking-eye-button";
                viewButton.setAttribute("aria-label", "Vis booking " + booking.id);
                viewButton.title = "Vis booking";

                // Øje ikon
                const eyeIcon = document.createElement("img");
                eyeIcon.src = "/images/eye.png";
                eyeIcon.alt = "";
                eyeIcon.className = "booking-eye-icon";

                viewButton.appendChild(eyeIcon);
                viewButton.addEventListener("click", () => showBooking(booking));

                // Tilføj knappen og rækken til tabellen
                actionCell.appendChild(viewButton);
                row.appendChild(actionCell);
                tableBody.appendChild(row);
            }

            status.textContent = "";
            table.hidden = false;
        } catch (error) {
            status.textContent = "Kan ikke loade bookinger. Prøv venligst igen.";
            console.error(error);
        }
    }

    // Vis oplysninger for den valgte booking
    function showBooking(booking) {
        details.replaceChildren();

        const fields = [
            ["Booking-ID", booking.id],
            ["Kundenavn", booking.nameOfCustomer],
            ["Dato", booking.date],
            ["E-mail", booking.emailOfCustomer],
            ["Telefon", booking.phoneNumber],
            ["Total pris", booking.totalPrice == null ? null : booking.totalPrice + " kr."],
            ["Session-ID'er", booking.sessionIds?.join(", ")]
        ];

        for (const [label, value] of fields) {
            const term = document.createElement("dt");
            const description = document.createElement("dd");
            term.textContent = label;
            description.textContent = value == null || value === "" ? "—" : value;
            details.append(term, description);
        }

        dialog.showModal();
    }

    // Indlæs dashboard
    displayBookings();

    return section;
}

