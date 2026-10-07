"use strict";

const API_BASE = "/api";

export async function fetchEquipment() {
    const response = await fetch(API_BASE + "/equipment");

    if (!response.ok) {
        throw new Error("HTTP " + response.status);
    }

    return response.json();
}