export function createLoginView({ onLogin }) {

    const section = document.createElement("section");
    section.id = "login-view";
    const heading = document.createElement("h1");
    heading.textContent = "Action Center";
    const subheading = document.createElement("h2");
    subheading.textContent = "Login";
    const form = document.createElement("form");
    form.id = "login-form";
    const username = createField("username", "Brugernavn", "text", "username");
    const password = createField("password", "Kodeord", "password", "current-password");
    const button = document.createElement("button");
    button.id = "login-button";
    button.type = "submit";
    button.textContent = "Login";
    form.append(username.container, password.container, button);
    const message = document.createElement("p");
    message.id = "login-message";
    message.setAttribute("role", "status");
    message.setAttribute("aria-live", "polite");

    form.addEventListener("submit", async (event) => {
        event.preventDefault();
        if (button.disabled) return;
        button.disabled = true;
        message.textContent = "Logging in...";
        try {
            const response = await fetch("/api/user/login", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                credentials: "same-origin",
                body: JSON.stringify({
                    username: username.input.value,
                    password: password.input.value
                })
            });
            if (response.status === 204) {
                form.reset();
                message.textContent = "";
                onLogin();
            } else if (response.status === 401) {
                message.textContent = "Invalid username or password.";
            } else {
                message.textContent = "Login failed. Please try again.";
            }
        } catch (error) {
            message.textContent = "Could not connect to the server.";
        } finally {
            button.disabled = false;
        }
    });
    section.append(heading, subheading, form, message);
    return section;
}

function createField(name, labelText, type, autocomplete) {
    const container = document.createElement("div");
    const label = document.createElement("label");
    label.htmlFor = name;
    label.textContent = labelText;
    const input = document.createElement("input");
    input.id = name;
    input.name = name;
    input.type = type;
    input.autocomplete = autocomplete;
    input.required = true;
    container.append(label, input);
    return { container, input };
}
