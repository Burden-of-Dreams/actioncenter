"use strict";

const API_BASE = "/api";

export async function fetchEmployeeSessions(employeeId) {
    const response = await fetch(`${API_BASE}/sessions/employee/${employeeId}`);

    if (!response.ok) {
        throw new Error("HTTP " + response.status);
    }
    return response.json();
}

export async function fetchAvailableStartTimes(activityId, date, excludeBookingId = null) {
    let url = `${API_BASE}/sessions/available-starts?activityId=${activityId}&date=${date}`;

    if (excludeBookingId !== null) {
        url += `&excludeBookingId=${excludeBookingId}`;
    }

    const response = await fetch(url);
    if (!response.ok) {
        throw new Error("HTTP " + response.status);
    }
    return response.json();
}