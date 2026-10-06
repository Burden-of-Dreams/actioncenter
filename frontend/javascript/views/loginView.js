"use strict";

export function createLoginView({ onLogin }) {
    const container = document.createElement("section");
    container.id = "login-view";

    container.innerHTML = `
        <h1>Action Center</h1>
        <h2>Login</h2>

        <form id="login-form">
            <div>
                <label for="username">Brugernavn</label>
                <input id="username" name="username"
                       type="text" autocomplete="username" required>
            </div>

            <div>
                <label for="password">Kodeord</label>
                <input id="password" name="password"
                       type="password" autocomplete="current-password" required>
            </div>

            <button id="login-button" type="submit">Login</button>
        </form>

        <p id="login-message" role="status" aria-live="polite"></p>
    `;

    const form = container.querySelector("form");
    const button = container.querySelector("button");
    const message = container.querySelector("#login-message");

    form.addEventListener("submit", async (event) => {
        event.preventDefault();

        if (button.disabled) {
            return;
        }

        button.disabled = true;
        message.textContent = "Logging in...";

        try {
            const response = await fetch("/api/user/login", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                credentials: "same-origin",
                body: JSON.stringify({
                    username: form.elements.username.value,
                    password: form.elements.password.value
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

    return container;
}