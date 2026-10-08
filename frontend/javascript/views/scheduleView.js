"use strict";

import { fetchCurrentUser } from "../APIs/userApi.js";
import { fetchEmployeeSessions } from "../APIs/sessionApi.js";


const dateFormat = new Intl.DateTimeFormat("da-DK", {
    weekday: "long", day: "numeric", month: "long"
});

function formatDate(isoDate) {
    return dateFormat.format(new Date(isoDate + "T00:00:00"));
}

function formatTime(time) {
    return time.slice(0, 5);
}

export function createScheduleView({ onUnauthorized }) {
    const section = document.createElement("section");
    section.id = "schedule-view";

    section.innerHTML = `
        <div class="view-header">
            <h1>Min vagtplan</h1>
            <span id="schedule-user"></span>
        </div>
        <p id="schedule-status" role="status">Loader vagtplan...</p>
        <div id="schedule-list"></div>
    `;

    const status = section.querySelector("#schedule-status");
    const list = section.querySelector("#schedule-list");
    const userLabel = section.querySelector("#schedule-user");

    async function load() {
        try {
            const user = await fetchCurrentUser();
            userLabel.textContent = user.username;

            const sessions = await fetchEmployeeSessions(user.id);

            if (sessions.length === 0) {
                status.textContent = "Du har ingen vagter planlagt.";
                return;
            }

            sessions.sort((a, b) =>
                (a.date + a.startTime).localeCompare(b.date + b.startTime));


            const byDate = Map.groupBy(sessions, s => s.date);

            for (const [date, daySessions] of byDate) {
                const day = document.createElement("article");
                day.className = "schedule-day";

                const heading = document.createElement("h2");
                heading.textContent = formatDate(date);
                day.appendChild(heading);

                for (const s of daySessions) {
                    const item = document.createElement("div");
                    item.className = "schedule-item";

                    const time = document.createElement("span");
                    time.className = "schedule-time";
                    time.textContent = `${formatTime(s.startTime)} – ${formatTime(s.endTime)}`;

                    const name = document.createElement("span");
                    name.className = "schedule-activity";
                    name.textContent = s.activityName;

                    item.append(time, name);
                    day.appendChild(item);
                }
                list.appendChild(day);
            }
            status.textContent = "";

        } catch (error) {
            if (error.status === 401) {
                onUnauthorized();
                return;
            }
            status.textContent = "Kan ikke loade vagtplan. Prøv venligst igen.";
            console.error(error);
        }
    }

    load();
    return section;
}