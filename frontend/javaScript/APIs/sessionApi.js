"use strict";

const API_BASE = "/api";

export async function fetchSession() {
    const response = await fetch(API_BASE + "/sessions");

    if (!response.ok) {
        throw new Error("HTTP " + response.status);
    }

    return response.json();
}