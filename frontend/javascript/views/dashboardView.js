"use strict";

import { fetchBookings, deleteBooking, updateBooking } from "../APIs/bookingApi.js";

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
            <div class="booking-details-actions">
                <button type="button" class="booking-edit-button" aria-label="Rediger booking" title="Rediger booking">
                    <img src="/images/edit.png" alt="" class="booking-eye-icon">
                </button>
                <button type="button" class="booking-delete-button" aria-label="Slet booking" title="Slet booking">
                    <img src="/images/trash.png" alt="" class="booking-eye-icon">
                </button>
                <button type="button" class="booking-details-close" aria-label="Luk bookingoplysninger" autofocus>×</button>
            </div>
        </div>
        <p class="booking-action-message" role="status"></p>
        <form class="booking-edit-form" hidden>
            <fieldset>
                <legend>Rediger kundeoplysninger</legend>
                <label>Kundenavn
                    <input name="nameOfCustomer" type="text" required>
                </label>
                <label>Telefon
                    <input name="phoneNumber" type="tel" maxlength="15" pattern="^\\+?[0-9]+$" required>
                </label>
                <label>E-mail
                    <input name="emailOfCustomer" type="email">
                </label>
                <button type="submit" class="booking-save-button">Gem</button>
                <button type="button" class="booking-cancel-button">Annuller</button>
            </fieldset>
        </form>
        <dl class="booking-details-fields"></dl>
        <h3>Sessions</h3>
        <div class="booking-session-list"></div>
    `;
    section.appendChild(dialog);

    const details = dialog.querySelector("dl");
    const sessionList = dialog.querySelector(".booking-session-list");

    const closeButton = dialog.querySelector(".booking-details-close");
    const editButton = dialog.querySelector(".booking-edit-button");
    const deleteButton = dialog.querySelector(".booking-delete-button");
    const actionMessage = dialog.querySelector(".booking-action-message");

    const editForm = dialog.querySelector(".booking-edit-form");
    const editFields = editForm.querySelector("fieldset");

    let selectedBooking;
    let selectedRow;
    let saving = false;

    // Knapper i booking modal
    closeButton.addEventListener("click", () => dialog.close());
    deleteButton.addEventListener("click", removeBooking);
    editButton.addEventListener("click", editBooking);
    editForm.addEventListener("submit", saveBooking);
    editForm.querySelector(".booking-cancel-button").addEventListener("click", cancelEditing);

    dialog.addEventListener("cancel", event => {
        if (saving) event.preventDefault();
    });

    // Slet den valgte booking
    async function removeBooking() {
        if (!window.confirm("Vil du slette booking " + selectedBooking.id + "?")) return;

        setSaving(true);
        actionMessage.textContent = "Sletter booking...";

        try {
            await deleteBooking(selectedBooking.id);
            selectedRow.remove();
            dialog.close();

            if (tableBody.rows.length === 0) {
                table.hidden = true;
                status.textContent = "Ingen bookinger endnu.";
            }
        } catch (error) {
            actionMessage.textContent = "Booking kunne ikke slettes. Prøv igen.";
            console.error(error);
        } finally {
            setSaving(false);
        }
    }

    // Undgå flere klik mens en ændring gemmes
    function setSaving(value) {
        saving = value;
        closeButton.disabled = value;
        editButton.disabled = value;
        deleteButton.disabled = value;
        editFields.disabled = value;
    }

    // Rediger kundeoplysninger
    function editBooking() {
        editForm.elements.nameOfCustomer.value = selectedBooking.nameOfCustomer ?? "";
        editForm.elements.phoneNumber.value = selectedBooking.phoneNumber ?? "";
        editForm.elements.emailOfCustomer.value = selectedBooking.emailOfCustomer ?? "";
        actionMessage.textContent = "";
        details.hidden = true;
        editForm.hidden = false;
        editButton.hidden = true;
        editForm.elements.nameOfCustomer.focus();
    }

    // Annuller redigering
    function cancelEditing() {
        editForm.hidden = true;
        details.hidden = false;
        editButton.hidden = false;
        actionMessage.textContent = "";
        editButton.focus();
    }

    // Gem kundeoplysninger
    async function saveBooking(event) {
        event.preventDefault();
        if (saving) return;

        const changes = {
            nameOfCustomer: editForm.elements.nameOfCustomer.value.trim(),
            phoneNumber: editForm.elements.phoneNumber.value.trim(),
            emailOfCustomer: editForm.elements.emailOfCustomer.value.trim()
        };

        setSaving(true);
        actionMessage.textContent = "Gemmer booking...";

        try {
            const updated = await updateBooking(selectedBooking.id, changes);
            Object.assign(selectedBooking, updated);

            selectedRow.cells[1].textContent = updated.nameOfCustomer;
            selectedRow.cells[3].textContent = updated.emailOfCustomer ?? "—";
            selectedRow.cells[4].textContent = updated.phoneNumber;
            searchInput.dispatchEvent(new Event("input"));

            showBooking(selectedBooking, selectedRow);
            actionMessage.textContent = "Booking gemt.";
        } catch (error) {
            actionMessage.textContent = "Booking kunne ikke gemmes. Tjek felterne og prøv igen.";
            console.error(error);
        } finally {
            setSaving(false);
            if (editForm.hidden) editButton.focus();
        }
    }

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
                viewButton.addEventListener("click", () => showBooking(booking, row));

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
    function showBooking(booking, row) {
        selectedBooking = booking;
        selectedRow = row;
        actionMessage.textContent = "";
        editForm.hidden = true;
        details.hidden = false;
        editButton.hidden = false;
        details.replaceChildren();

        const fields = [
            ["Booking-ID", booking.id],
            ["Kundenavn", booking.nameOfCustomer],
            ["Dato", booking.date],
            ["E-mail", booking.emailOfCustomer],
            ["Telefon", booking.phoneNumber],
            ["Total pris", booking.totalPrice == null ? null : booking.totalPrice + " kr."]
        ];

        for (const [label, value] of fields) {
            const term = document.createElement("dt");
            const description = document.createElement("dd");
            term.textContent = label;
            description.textContent = value == null || value === "" ? "—" : value;
            details.append(term, description);
        }

        showSessions(booking.sessions);
        if (!dialog.open) dialog.showModal();
    }

    // Liste af bookingens sessions
    function showSessions(sessions) {
        sessionList.replaceChildren();

        if (!Array.isArray(sessions) || sessions.length === 0) {
            const message = document.createElement("p");
            message.textContent = Array.isArray(sessions)
                ? "Ingen sessions på denne booking."
                : "Sessions kunne ikke vises. Tjek at backend er opdateret.";
            sessionList.appendChild(message);
            return;
        }

        sessions.forEach((session, index) => {
            sessionList.appendChild(createSessionDetails(session, index));
        });
    }

    // Session der kan foldes ud
    function createSessionDetails(session, index) {
        const item = document.createElement("details");
        item.className = "booking-session";

        const summary = document.createElement("summary");
        summary.textContent = `Session ${index + 1} · ${session.activityName ?? "Ukendt aktivitet"}`;
        item.appendChild(summary);

        const fields = document.createElement("dl");
        fields.className = "booking-details-fields";

        const values = [
            ["Dato", session.dateOfActivity],
            ["Start", session.startOfSession?.slice(0, 5)],
            ["Slut", session.endOfSession?.slice(0, 5)],
            ["Antal personer", session.amountOfCustomers]
        ];

        for (const [label, value] of values) {
            const term = document.createElement("dt");
            const description = document.createElement("dd");
            term.textContent = label;
            description.textContent = value == null || value === "" ? "—" : value;
            fields.append(term, description);
        }

        // Udstyr til sessionen
        const equipmentTitle = document.createElement("dt");
        equipmentTitle.textContent = "Udstyr";
        const equipmentDetails = document.createElement("dd");

        if (session.equipmentNames?.length) {
            const equipmentList = document.createElement("ul");
            for (const name of session.equipmentNames) {
                const equipmentItem = document.createElement("li");
                equipmentItem.textContent = name;
                equipmentList.appendChild(equipmentItem);
            }
            equipmentDetails.appendChild(equipmentList);
        } else {
            equipmentDetails.textContent = "Intet udstyr valgt.";
        }

        fields.append(equipmentTitle, equipmentDetails);
        item.appendChild(fields);
        return item;
    }

    // Indlæs dashboard
    displayBookings();

    return section;
}



