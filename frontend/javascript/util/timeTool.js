export function addMinutes(time, minutes) {
    const [hours, mins] = time.split(":").map(Number);
    const total = hours * 60 + mins + minutes;

    if (total >= 24 * 60) {
        return "";
    }

    const endHours = String(Math.floor(total / 60)).padStart(2, "0");
    const endMinutes = String(total % 60).padStart(2, "0");

    return `${endHours}:${endMinutes}`;
}

export function timeToMinutes(time) {
    const [hours, minutes] = time.split(":").map(Number);
    return hours * 60 + minutes;
}