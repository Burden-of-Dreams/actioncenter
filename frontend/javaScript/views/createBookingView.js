"use strict";

import { createBooking } from "../APIs/bookingApi.js";
import { fetchActivities } from "../APIs/activityApi";
import { fetchEquipment } from "../APIs/equipmentApi.js";

import {createSessionForm} from "../util/sessionTool.js";



export function createBookingView({ onCreated, onCancel }) {
    const section = document.createElement("section");
    section.id = "create-booking-view";

    section.innerHTML = `
    <h1>Ny booking</h1>

    <p id="booking-form-status" role="status">
        Loader aktiviteter...
    </p>

    <form id="booking-form" hidden>
        <h2>Kunde</h2>

        <div class="form-field">
            <label for="customer-name">
                Kundenavn
            </label>

            <input
                id="customer-name"
                name="nameOfCustomer"
                type="text"
                autocomplete="off"
                required
            >
        </div>

        <div class="form-field">
            <label for="customer-phone">
                Telefon
            </label>

            <input
                id="customer-phone"
                name="phoneNumber"
                type="tel"
                autocomplete="off"
                required
            >
        </div>

        <div class="form-field">
            <label for="customer-email">
                Email
            </label>

            <input
                id="customer-email"
                name="emailOfCustomer"
                type="email"
                autocomplete="off"
            >
        </div>

        <h2>Sessions</h2>

        <div id="session-list"></div>

        <button
            id="add-session-button"
            type="button"
        >
            + Tilføj session
        </button>

        <div class="form-actions">
            <button
                id="save-booking-button"
                type="submit"
            >
                Gem booking
            </button>

            <button
                id="cancel-booking-button"
                type="button"
            >
                Annuller
            </button>
        </div>
    </form>

    <p id="booking-message"></p>
`;

    const status = section.querySelector("#booking-form-status");
    const form = section.querySelector("#booking-form");
    const sessionList = section.querySelector("#session-list");
    const addSessionButton = section.querySelector("#add-session-button");
    const saveButton = section.querySelector("#save-booking-button");
    const cancelButton = section.querySelector("#cancel-booking-button");
    const message = section.querySelector("#booking-message");


    let activities = [];
    let equipment = [];

    const sessionForms = [];

// -------------------------
// SESSION MANAGEMENT
// -------------------------

    function updateSessionTitles() {
        sessionForms.forEach((sessionForm, index) => {
            sessionForm.setTitle(`Session ${index + 1}`);

            sessionForm.setRemoveVisible(
                sessionForms.length > 1
            );
        });
    }

    function addSession() {
        let sessionForm;

        sessionForm = createSessionForm({
            activities,
            equipment,

            onRemove: () => {
                const index =
                    sessionForms.indexOf(sessionForm);

                if (index !== -1) {
                    sessionForms.splice(index, 1);
                }

                updateSessionTitles();
            }
        });

        sessionForms.push(sessionForm);

        sessionList.appendChild(
            sessionForm.element
        );

        updateSessionTitles();
    }

// -------------------------
// BUILD BOOKING REQUEST
// -------------------------

    function buildBookingRequest() {
        return {
            nameOfCustomer:
                form.elements.nameOfCustomer.value.trim(),

            phoneNumber:
                form.elements.phoneNumber.value.trim(),

            emailOfCustomer:
                form.elements.emailOfCustomer.value.trim(),

            sessions:
                sessionForms.map(
                    sessionForm => sessionForm.getData()
                )
        };
    }

// -------------------------
// EVENTS
// -------------------------

    addSessionButton.addEventListener(
        "click",
        addSession
    );

    cancelButton.addEventListener(
        "click",
        onCancel
    );

    form.addEventListener(
        "submit",
        async event => {
            event.preventDefault();

            if (saveButton.disabled) {
                return;
            }

            const bookingRequest =
                buildBookingRequest();

            // A session cannot end after midnight.
            // sessionForm.js returns an empty endOfSession
            // if that would happen.
            for (const session of bookingRequest.sessions) {
                if (!session.endOfSession) {
                    message.textContent =
                        "En session må ikke slutte efter midnat. " +
                        "Vælg en tidligere starttid.";

                    return;
                }
            }

            saveButton.disabled = true;
            message.textContent =
                "Gemmer booking...";

            try {
                await createBooking(
                    bookingRequest
                );

                message.textContent = "";

                onCreated();

            } catch (error) {
                if (error.status === 400) {
                    message.textContent =
                        "Tjek at alle felter er udfyldt korrekt.";

                } else if (error.status) {
                    message.textContent =
                        "Booking kunne ikke gemmes. " +
                        "Prøv venligst igen.";

                } else {
                    message.textContent =
                        "Kan ikke forbinde til netværket.";
                }

                console.error(error);

            } finally {
                saveButton.disabled = false;
            }
        }
    );

// -------------------------
// LOAD FORM DATA
// -------------------------

    async function loadFormData() {
        try {
            [
                activities,
                equipment
            ] = await Promise.all([
                fetchActivities(),
                fetchEquipment()
            ]);

            if (activities.length === 0) {
                status.textContent =
                    "Der er ingen aktiviteter at booke endnu.";

                return;
            }

            status.textContent = "";

            form.hidden = false;

            // Start with one session.
            addSession();

        } catch (error) {
            status.textContent =
                "Kan ikke loade aktiviteter. " +
                "Prøv venligst igen.";

            console.error(error);
        }
    }

    loadFormData();

    return section;


}