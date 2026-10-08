import { addMinutes, timeToMinutes } from "./timeTool.js";
import { fetchAvailableStartTimes } from "../APIs/sessionApi.js";

const STATUS_TEXT = {
    READY: "",
    CHARGING: " (oplader)",
    UNAVAILABLE: " (ikke tilgængelig)"
};

export function createSessionForm({
                                      activities,
                                      equipment,
                                      session = null,
                                      onRemove,
                                      getOtherSessions = () => [],
                                      excludeBookingId = null
                                  }) {
    const fieldset = document.createElement("fieldset");
    fieldset.className = "session";

    fieldset.innerHTML = `
        <legend class="session-title"></legend>

        <div class="form-field">
            <label>Aktivitet
                <select class="session-activity" required>
                    <option value="">Vælg aktivitet...</option>
                </select>
            </label>
            <small class="session-info"></small>
        </div>

        <div class="form-field">
            <label>Antal personer
                <input class="session-customers"
                       type="number"
                       min="1"
                       step="1"
                       required>
            </label>
        </div>

        <div class="form-field">
            <label>Dato
                <input class="session-date"
                       type="date"
                       required>
            </label>
        </div>

        <div class="form-field">
            <label>Start
                <select class="session-start" required>
                    <option value="">Vælg aktivitet og dato først</option>
                </select>
            </label>
        </div>

        <div class="form-field">
            <label>Slut (udfyldes automatisk)
                <input class="session-end"
                       type="time"
                       readonly>
            </label>
        </div>

        <div class="form-field">
            <span>Udstyr</span>
            <div class="session-equipment">
                <small>Vælg først en aktivitet.</small>
            </div>
        </div>

        <button class="remove-session-button"
                type="button">
            Fjern session
        </button>
    `;

    const activitySelect = fieldset.querySelector(".session-activity");
    const info = fieldset.querySelector(".session-info");
    const customersInput = fieldset.querySelector(".session-customers");
    const dateInput = fieldset.querySelector(".session-date");
    const startInput = fieldset.querySelector(".session-start");   // now a <select>
    const endInput = fieldset.querySelector(".session-end");
    const equipmentBox = fieldset.querySelector(".session-equipment");
    const removeButton = fieldset.querySelector(".remove-session-button");

    // Populate activities
    for (const activity of activities) {
        const option = document.createElement("option");

        option.value = activity.id;
        option.textContent = activity.name;

        activitySelect.appendChild(option);
    }

    function findActivity(activityId) {
        return activities.find(
            activity => activity.id === Number(activityId)
        );
    }

    function updateEndTime() {
        const activity = findActivity(activitySelect.value);

        if (!activity || !startInput.value) {
            endInput.value = "";
            return;
        }

        endInput.value = addMinutes(
            startInput.value,
            activity.durationMinutes
        );
    }

    // -------------------------
    // START TIME DROPDOWN
    // -------------------------

    function showMessageInStartSelect(messageText) {
        startInput.replaceChildren();

        const messageOption = document.createElement("option");
        messageOption.value = "";
        messageOption.textContent = messageText;

        startInput.appendChild(messageOption);
    }

    // Sessions that are in the same form but not saved yet
    // cannot be seen by the server, so we check them here.
    function isBlockedByOtherSessionInForm(startTime, activity, date) {
        const newStartMinutes = timeToMinutes(startTime);
        const newEndMinutes = newStartMinutes + activity.durationMinutes;

        for (const otherSession of getOtherSessions(fieldset)) {
            const isSameActivityAndDate =
                otherSession.activityId === activity.id &&
                otherSession.dateOfActivity === date;

            if (!isSameActivityAndDate ||
                !otherSession.startOfSession ||
                !otherSession.endOfSession) {
                continue;
            }

            const otherStartMinutes = timeToMinutes(otherSession.startOfSession);
            const otherEndMinutes = timeToMinutes(otherSession.endOfSession);

            if (newStartMinutes < otherEndMinutes && newEndMinutes > otherStartMinutes) {
                return true;
            }
        }

        return false;
    }

    async function updateStartTimeOptions() {
        const activity = findActivity(activitySelect.value);
        const date = dateInput.value;
        const previouslySelectedTime = startInput.value;

        if (!activity || !date) {
            showMessageInStartSelect("Vælg aktivitet og dato først");
            updateEndTime();
            return;
        }

        try {
            const availableStartTimes =
                await fetchAvailableStartTimes(activity.id, date, excludeBookingId);

            startInput.replaceChildren();

            const placeholderOption = document.createElement("option");
            placeholderOption.value = "";
            placeholderOption.textContent = "Vælg starttid...";
            startInput.appendChild(placeholderOption);

            let numberOfOptionsAdded = 0;

            for (const startTime of availableStartTimes) {
                if (isBlockedByOtherSessionInForm(startTime, activity, date)) {
                    continue;
                }

                const option = document.createElement("option");
                option.value = startTime;
                option.textContent = startTime;

                startInput.appendChild(option);
                numberOfOptionsAdded++;
            }

            if (numberOfOptionsAdded === 0) {
                showMessageInStartSelect("Ingen ledige tider");
            } else {
                // Keeps the earlier choice if it is still available
                startInput.value = previouslySelectedTime;
            }

        } catch (error) {
            console.error(error);
            showMessageInStartSelect("Kunne ikke hente tider");
        }

        updateEndTime();
    }

    // -------------------------
    // EQUIPMENT / ACTIVITY
    // -------------------------

    function updateEquipment(activity) {
        equipmentBox.replaceChildren();

        const activityEquipment = equipment.filter(item =>
            activity.equipmentIds.includes(item.id)
        );

        if (activityEquipment.length === 0) {
            const empty = document.createElement("small");

            empty.textContent =
                "Ingen udstyr til denne aktivitet.";

            equipmentBox.appendChild(empty);

            return;
        }

        for (const item of activityEquipment) {
            const label = document.createElement("label");
            const checkbox = document.createElement("input");

            checkbox.type = "checkbox";
            checkbox.value = item.id;
            checkbox.disabled = item.status !== "READY";

            label.appendChild(checkbox);

            label.append(
                " " +
                item.name +
                (STATUS_TEXT[item.status] ?? "")
            );

            equipmentBox.appendChild(label);
        }
    }

    function updateActivity() {
        const activity = findActivity(activitySelect.value);

        if (!activity) {
            info.textContent = "";
            customersInput.removeAttribute("max");
            equipmentBox.replaceChildren();
            updateEndTime();

            return;
        }

        info.textContent =
            `Max ${activity.capacity} personer · ` +
            `Min. alder ${activity.ageLimit} år · ` +
            `${activity.durationMinutes} min.`;

        customersInput.max = activity.capacity;

        updateEquipment(activity);
        updateEndTime();
    }

    // -------------------------
    // EVENTS
    // -------------------------

    activitySelect.addEventListener("change", () => {
        updateActivity();
        updateStartTimeOptions();
    });

    dateInput.addEventListener("change", updateStartTimeOptions);

    startInput.addEventListener("change", updateEndTime);

    removeButton.addEventListener("click", () => {
        fieldset.remove();

        if (onRemove) {
            onRemove();
        }
    });

    // If we're editing an existing session,
    // populate the fields.
    if (session) {
        activitySelect.value = session.activityId;
        customersInput.value = session.amountOfCustomers;
        dateInput.value = session.dateOfActivity;

        updateActivity();

        const selectedEquipmentIds =
            session.equipmentIds ?? [];

        for (const checkbox of equipmentBox.querySelectorAll(
            "input[type='checkbox']"
        )) {
            checkbox.checked =
                selectedEquipmentIds.includes(
                    Number(checkbox.value)
                );
        }

        // The start time option must exist before it can be selected,
        // so we wait for the dropdown to be built first.
        updateStartTimeOptions().then(() => {
            // "09:00:00" -> "09:00"
            startInput.value = session.startOfSession.substring(0, 5);
            updateEndTime();
        });
    }

    function getData() {
        const equipmentIds = [
            ...fieldset.querySelectorAll(
                ".session-equipment input:checked"
            )
        ].map(checkbox => Number(checkbox.value));

        return {
            activityId: Number(activitySelect.value),
            amountOfCustomers: Number(
                customersInput.value
            ),
            equipmentIds,
            dateOfActivity: dateInput.value,
            startOfSession: startInput.value,
            endOfSession: endInput.value
        };
    }

    function setTitle(title) {
        fieldset.querySelector(".session-title")
            .textContent = title;
    }

    function setRemoveVisible(visible) {
        removeButton.hidden = !visible;
    }

    return {
        element: fieldset,
        getData,
        setTitle,
        setRemoveVisible
    };
}