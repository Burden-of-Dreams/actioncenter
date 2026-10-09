import { addMinutes } from "../util/timeTool.js";

const STATUS_TEXT = {
    READY: "",
    CHARGING: " (oplader)",
    UNAVAILABLE: " (ikke tilgængelig)"
};

export function createSessionForm({ activities, equipment, session = null, onRemove }) {
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
                <input class="session-start"
                       type="time"
                       required>
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
    const startInput = fieldset.querySelector(".session-start");
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
            // Allerede reserveret udstyr kan beholdes eller fravælges ved redigering
            const alreadySelected = session?.activityId === activity.id
                && session.equipmentIds.includes(item.id);
            checkbox.disabled = item.status !== "READY" && !alreadySelected;

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

    activitySelect.addEventListener(
        "change",
        updateActivity
    );

    startInput.addEventListener(
        "input",
        updateEndTime
    );

    removeButton.addEventListener("click", () => {
        fieldset.remove();

        if (onRemove) {
            onRemove();
        }
    });


    // Hvis vi ændre en eksisterende session -> lav fields
    if (session) {
        activitySelect.value = session.activityId;
        customersInput.value = session.amountOfCustomers;
        dateInput.value = session.dateOfActivity;
        startInput.value = session.startOfSession;

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

        updateEndTime();
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

export async function fetchEmployeeSessions(employeeId) {
    const response = await fetch(
        `/api/sessions/employee/${employeeId}`
    );

    if (!response.ok) {
        throw new Error("HTTP " + response.status);
    }

    return response.json();
}

