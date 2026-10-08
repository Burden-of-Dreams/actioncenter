"use strict";

const API_BASE = "/api";

export async function fetchEmployeeSessions(employeeId) {
    const response = await fetch(`${API_BASE}/sessions/employee/${employeeId}`);

    if (!response.ok) {
        throw new Error("HTTP " + response.status);
    }
    return response.json();
}