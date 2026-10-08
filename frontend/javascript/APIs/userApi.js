"use strict";

export async function fetchCurrentUser() {
    const response = await fetch("/api/user/me", { credentials: "same-origin" });

    if (response.status === 401) {
        const error = new Error("Unauthorized");
        error.status = 401;
        throw error;
    }
    if (!response.ok) {
        throw new Error("HTTP " + response.status);
    }
    return response.json();
}