"use strict";

const API_BASE = "/api";

export async function fetchActivities() {
    const response = await fetch(API_BASE + "/activities");

    if (!response.ok) {
        throw new Error("HTTP " + response.status);
    }

    return response.json();
}
